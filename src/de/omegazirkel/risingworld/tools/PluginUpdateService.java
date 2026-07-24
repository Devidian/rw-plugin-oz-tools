package de.omegazirkel.risingworld.tools;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.sql.Connection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import de.omegazirkel.risingworld.OZTools;
import net.risingworld.api.Plugin;

/** Bounded public-GitHub release checks. Package installation deliberately uses
 * the same allow-list decision as checking and is only exposed to admins. */
public final class PluginUpdateService implements AutoCloseable {
    public enum State { UNKNOWN, CURRENT, UPDATE_AVAILABLE, NOT_INSTALLED, INSTALLING, ERROR }
    public record Result(String installedVersion, String latestVersion, String releaseUrl, String releaseNotes,
            State state, long checkedAtEpochMillis) { }
    private record ReleaseInfo(String version, String url, String notes) { }
    record CatalogEntry(String repository, String directory) { }

    private static final String GITHUB_API_PREFIX = "https://api.github.com/repos/";
    private static final URI REMOTE_CATALOG_URI = URI.create(
            "https://raw.githubusercontent.com/Devidian/rw-plugin-oz-tools/main/src/main/resources/plugin-catalog.json");
    private static final int MAX_CATALOG_BYTES = 64 * 1024;
    private static volatile Map<String, CatalogEntry> catalog = Map.of();
    private static volatile String bundledCatalogError;
    private final OZTools tools;
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(8))
            .followRedirects(HttpClient.Redirect.NORMAL).build();
    private final ExecutorService executor = Executors.newSingleThreadExecutor(new DiagnosticThreadFactory(
            "OZ Tools", "public GitHub plugin release checks", "OZ-Plugin-Updates", true,
            message -> OZTools.logger().debug(message)));
    private volatile Map<String, Result> results = Map.of();
    private final AtomicBoolean checkQueued = new AtomicBoolean();
    private final PluginUpdateStore store;

    public PluginUpdateService(OZTools tools, Connection connection) {
        this.tools = tools;
        if (!ensureBundledCatalog()) {
            OZTools.logger().error("Could not load bundled trusted plugin catalogue; remote checks remain available: "
                    + bundledCatalogError);
        }
        this.store = new PluginUpdateStore(connection);
        this.results = Collections.unmodifiableMap(store.load());
    }

    public Map<String, Result> results() { return results; }

    /** Records a successfully staged package as current before its plugin reload starts. */
    public void markInstalledLatest(String pluginName) {
        Result result = results.get(pluginName);
        if (result == null) return;
        updateResult(pluginName, new Result(result.latestVersion(), result.latestVersion(), result.releaseUrl(),
                result.releaseNotes(), State.CURRENT, result.checkedAtEpochMillis()));
    }

    public void checkAsync() { checkAsync(null, null, null); }

    public void checkAsync(Consumer<String> checkedPlugin, Consumer<String> resultUpdated, Consumer<Boolean> completed) {
        if (!checkQueued.compareAndSet(false, true)) return;
        executor.execute(() -> {
            try {
                checkInstalled(checkedPlugin, resultUpdated, completed);
            } finally {
                checkQueued.set(false);
            }
        });
    }

    /** Called only after an administrator has confirmed the UI action. */
    public void installLatestAsync(String pluginName, Runnable onSuccess, Consumer<String> onFailure) {
        if (pluginName == null || pluginName.isBlank()) return;
        Result previous = results.get(pluginName);
        if (previous != null) {
            updateResult(pluginName, new Result(previous.installedVersion(), previous.latestVersion(), previous.releaseUrl(),
                    previous.releaseNotes(), State.INSTALLING, previous.checkedAtEpochMillis()));
        }
        executor.execute(() -> installLatest(pluginName, onSuccess, onFailure, previous));
    }

    private void installLatest(String pluginName, Runnable onSuccess, Consumer<String> onFailure, Result previous) {
        refreshCatalog();
        Plugin plugin = null;
        for (Plugin candidate : tools.getAllPlugins()) {
            if (pluginName.equals(candidate.getDescription("name"))) { plugin = candidate; break; }
        }
        CatalogEntry catalogEntry = catalog.get(pluginName);
        if (plugin == null && catalogEntry == null) {
            failInstallation(pluginName, previous, onFailure, "unknown-plugin");
            return;
        }
        String repository = catalogEntry != null ? catalogEntry.repository()
                : repositoryFrom(plugin.getDescription("website"));
        if (!isTrustedRepository(pluginName, repository)) {
            failInstallation(pluginName, previous, onFailure, "untrusted-release-source");
            return;
        }
        try {
            String assetUrl = selectZipAsset(releaseJson(repository));
            Path target = plugin == null ? Path.of(tools.getPath()).toAbsolutePath().normalize().getParent()
                    .resolve(catalogEntry.directory())
                    : Path.of(plugin.getPath()).toAbsolutePath().normalize();
            Path parent = target.getParent();
            if (parent == null || !Files.isDirectory(parent)) throw new IOException("Invalid plugin path: " + target);
            Path staging = Files.createTempDirectory(parent, ".oz-update-");
            try {
                Path archive = staging.resolve("release.zip");
                download(assetUrl, archive);
                Path extracted = staging.resolve("content");
                Files.createDirectories(extracted);
                extractZip(archive, extracted);
                Files.delete(archive);
                Path source = singlePluginDirectory(extracted);
                try (var paths = Files.walk(source)) {
                    if (paths.noneMatch(path -> path.getFileName().toString().endsWith(".jar"))) {
                        throw new IOException("Release contains no plugin JAR");
                    }
                }
                if (Files.exists(target)) preserveLocalFiles(target, source);
                Path backup = parent.resolve(target.getFileName() + ".oz-backup");
                if (Files.exists(backup)) deleteTree(backup);
                if (Files.exists(target)) Files.move(target, backup, StandardCopyOption.ATOMIC_MOVE);
                try {
                    Files.move(source, target, StandardCopyOption.ATOMIC_MOVE);
                    deleteTree(backup);
                    if (onSuccess != null) onSuccess.run();
                } catch (Exception swapFailure) {
                    if (Files.exists(backup)) Files.move(backup, target, StandardCopyOption.ATOMIC_MOVE);
                    throw swapFailure;
                }
            } finally {
                try {
                    deleteTree(staging);
                } catch (IOException cleanupFailure) {
                    OZTools.logger().warn("Could not clean plugin update staging directory " + staging + ": "
                            + cleanupFailure.getMessage());
                }
            }
        } catch (Exception ex) {
            if (previous != null) updateResult(pluginName, previous);
            OZTools.logger().error("Plugin installation failed for " + pluginName + ": " + ex.getMessage());
            if (onFailure != null) onFailure.accept(ex.getMessage());
        }
    }

    private void failInstallation(String pluginName, Result previous, Consumer<String> onFailure, String reason) {
        if (previous != null) updateResult(pluginName, previous);
        OZTools.logger().warn("Plugin installation rejected: " + reason + " for " + pluginName);
        if (onFailure != null) onFailure.accept(reason);
    }

    static void preserveLocalFiles(Path installedPlugin, Path replacementPlugin) throws IOException {
        try (var paths = Files.walk(installedPlugin)) {
            for (Path file : paths.filter(Files::isRegularFile).toList()) {
                String name = file.getFileName().toString();
                Path relative = installedPlugin.relativize(file);
                Path destination = replacementPlugin.resolve(relative);
                boolean persistent = name.equals("settings.properties") || name.endsWith(".json") || name.endsWith(".db")
                        || name.endsWith(".db-wal") || name.endsWith(".db-shm");
                if (!persistent && Files.exists(destination)) continue;
                Files.createDirectories(destination.getParent());
                Files.copy(file, destination, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
            }
        }
    }

    static String selectZipAsset(JsonObject release) throws IOException {
        if (release == null || !release.has("assets") || !release.get("assets").isJsonArray()) {
            throw new IOException("Release contains no assets");
        }
        return release.getAsJsonArray("assets").asList().stream()
                .filter(element -> element.isJsonObject())
                .map(element -> element.getAsJsonObject())
                .filter(asset -> asset.has("name") && asset.has("browser_download_url")
                        && asset.get("name").getAsString().endsWith(".zip"))
                .map(asset -> asset.get("browser_download_url").getAsString())
                .findFirst().orElseThrow(() -> new IOException("No ZIP release asset"));
    }

    private void updateResult(String pluginName, Result result) {
        Map<String, Result> updated = new LinkedHashMap<>(results);
        updated.put(pluginName, result);
        results = Collections.unmodifiableMap(updated);
        store.save(pluginName, result);
    }

    private ReleaseInfo release(String repository) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(GITHUB_API_PREFIX + repository + "/releases/latest"))
                .timeout(Duration.ofSeconds(12)).header("Accept", "application/vnd.github+json").GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) throw new IOException("GitHub HTTP " + response.statusCode());
        JsonObject release = JsonParser.parseString(response.body()).getAsJsonObject();
        if (!release.has("tag_name") || !release.has("html_url")) throw new IOException("Incomplete GitHub release metadata");
        return new ReleaseInfo(release.get("tag_name").getAsString().replaceFirst("^[vV]", ""),
                release.get("html_url").getAsString(), release.has("body") && !release.get("body").isJsonNull()
                        ? release.get("body").getAsString() : "");
    }

    private void download(String url, Path file) throws Exception {
        HttpResponse<InputStream> response = client.send(HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(30)).GET().build(), HttpResponse.BodyHandlers.ofInputStream());
        if (response.statusCode() != 200) throw new IOException("Asset HTTP " + response.statusCode());
        try (InputStream body = response.body()) { Files.copy(body, file, StandardCopyOption.REPLACE_EXISTING); }
    }

    private static void extractZip(Path archive, Path target) throws IOException {
        try (ZipInputStream zip = new ZipInputStream(Files.newInputStream(archive))) {
            for (ZipEntry entry; (entry = zip.getNextEntry()) != null;) {
                Path destination = target.resolve(entry.getName()).normalize();
                if (!destination.startsWith(target)) throw new IOException("Unsafe ZIP entry");
                if (entry.isDirectory()) Files.createDirectories(destination);
                else { Files.createDirectories(destination.getParent()); Files.copy(zip, destination); }
            }
        }
    }
    private static Path singlePluginDirectory(Path content) throws IOException {
        try (var entries = Files.list(content)) {
            var list = entries.toList();
            return list.size() == 1 && Files.isDirectory(list.get(0)) ? list.get(0) : content;
        }
    }
    static void deleteTree(Path path) throws IOException {
        if (path == null || !Files.exists(path)) return;
        try (var paths = Files.walk(path)) {
            for (Path entry : paths.sorted(java.util.Comparator.reverseOrder()).toList()) {
                Files.deleteIfExists(entry);
            }
        }
    }

    public void checkPluginAsync(String pluginName, Consumer<Result> completed) {
        if (pluginName == null || pluginName.isBlank()) return;
        executor.execute(() -> {
            refreshCatalog();
            Map<String, Result> checked = new LinkedHashMap<>(results);
            CheckSource source = checkSource(pluginName);
            if (source == null) return;
            checkRepository(pluginName, source.repository(), source.installedVersion(), source.notInstalled(), checked,
                    null, name -> { if (completed != null) completed.accept(results.get(name)); }, true);
        });
    }

    private void checkInstalled(Consumer<String> checkedPlugin, Consumer<String> resultUpdated, Consumer<Boolean> completed) {
        refreshCatalog();
        Map<String, CatalogEntry> catalogSnapshot = catalog;
        Map<String, Result> checked = new LinkedHashMap<>(results);
        Map<String, Plugin> installed = new LinkedHashMap<>();
        for (Plugin plugin : tools.getAllPlugins()) installed.put(plugin.getDescription("name"), plugin);
        boolean firstRequest = true;
        for (Map.Entry<String, CatalogEntry> catalogEntry : catalogSnapshot.entrySet()) {
            String name = catalogEntry.getKey();
            Plugin plugin = installed.remove(name);
            String repository = catalogEntry.getValue().repository();
            String installedVersion = plugin == null ? "N/A" : plugin.getDescription("version");
            firstRequest = checkRepository(name, repository, installedVersion, plugin == null, checked, checkedPlugin, resultUpdated, firstRequest);
        }
        for (Plugin plugin : installed.values()) {
            String repository = repositoryFrom(plugin.getDescription("website"));
            if (!isTrustedRepository(plugin.getDescription("name"), repository)) continue;
            firstRequest = checkRepository(plugin.getDescription("name"), repository, plugin.getDescription("version"), false,
                    checked, checkedPlugin, resultUpdated, firstRequest);
        }
        results = Collections.unmodifiableMap(checked);
        if (completed != null) completed.accept(checked.values().stream()
                .anyMatch(result -> result.state() == State.UPDATE_AVAILABLE));
    }

    private boolean checkRepository(String name, String repository, String installedVersion, boolean notInstalled,
            Map<String, Result> checked, Consumer<String> checkedPlugin, Consumer<String> resultUpdated, boolean firstRequest) {
        if (!isTrustedRepository(name, repository)) {
            return firstRequest;
        }
        try {
            if (!firstRequest) pauseBetweenChecks();
            if (checkedPlugin != null) checkedPlugin.accept(name);
            ReleaseInfo release = release(repository);
            publishResult(name, new Result(installedVersion, release.version(), release.url(), release.notes(),
                    notInstalled ? State.NOT_INSTALLED
                            : compare(release.version(), installedVersion) > 0 ? State.UPDATE_AVAILABLE : State.CURRENT,
                    System.currentTimeMillis()), checked, resultUpdated);
        } catch (Exception ex) {
            publishResult(name, new Result(installedVersion, "", "", "", State.ERROR, System.currentTimeMillis()), checked, resultUpdated);
            OZTools.logger().warn("Plugin update check failed for " + name + ": " + ex.getMessage());
        }
        return false;
    }

    private void publishResult(String name, Result result, Map<String, Result> checked, Consumer<String> resultUpdated) {
        checked.put(name, result);
        updateResult(name, result);
        if (resultUpdated != null) resultUpdated.accept(name);
    }

    private record CheckSource(String repository, String installedVersion, boolean notInstalled) { }
    private CheckSource checkSource(String name) {
        CatalogEntry catalogEntry = catalog.get(name);
        for (Plugin plugin : tools.getAllPlugins()) {
            if (name.equals(plugin.getDescription("name"))) {
                String repository = catalogEntry != null ? catalogEntry.repository()
                        : repositoryFrom(plugin.getDescription("website"));
                return repository == null ? null : new CheckSource(repository, plugin.getDescription("version"), false);
            }
        }
        return catalogEntry == null ? null : new CheckSource(catalogEntry.repository(), "N/A", true);
    }

    private boolean isTrustedRepository(String pluginName, String repository) {
        if (repository == null) return false;
        CatalogEntry entry = catalog.get(pluginName);
        return (entry != null && entry.repository().equals(repository))
                || PluginSettings.getInstance().allowExternalPluginRepositories;
    }

    private void refreshCatalog() {
        try {
            HttpRequest request = HttpRequest.newBuilder(REMOTE_CATALOG_URI).timeout(Duration.ofSeconds(12))
                    .header("Accept", "application/json").GET().build();
            HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
            try (InputStream body = response.body()) {
                if (response.statusCode() != 200) throw new IOException("GitHub HTTP " + response.statusCode());
                catalog = parseCatalog(readBounded(body));
            }
        } catch (Exception | LinkageError ex) {
            OZTools.logger().warn("Could not refresh trusted plugin catalogue; using the last valid catalogue: "
                    + ex.getMessage());
        }
    }

    private static boolean ensureBundledCatalog() {
        if (!catalog.isEmpty()) return true;
        synchronized (PluginUpdateService.class) {
            if (!catalog.isEmpty()) return true;
            try {
                catalog = loadBundledCatalog();
                bundledCatalogError = null;
                return true;
            } catch (Exception | LinkageError ex) {
                bundledCatalogError = ex.getClass().getSimpleName() + ": " + ex.getMessage();
                return false;
            }
        }
    }

    private static Map<String, CatalogEntry> loadBundledCatalog() throws IOException {
        ClassLoader loader = PluginUpdateService.class.getClassLoader();
        InputStream bundledResource = loader == null ? null : loader.getResourceAsStream("plugin-catalog.json");
        if (bundledResource == null) {
            bundledResource = PluginUpdateService.class.getResourceAsStream("/plugin-catalog.json");
        }
        try (InputStream resource = bundledResource) {
            if (resource == null) throw new IOException("Bundled plugin catalogue is missing");
            return parseCatalog(readBounded(resource));
        }
    }

    private static String readBounded(InputStream input) throws IOException {
        byte[] data = input.readNBytes(MAX_CATALOG_BYTES + 1);
        if (data.length > MAX_CATALOG_BYTES) throw new IOException("Plugin catalogue exceeds 64 KiB");
        return new String(data, StandardCharsets.UTF_8);
    }

    static Map<String, CatalogEntry> parseCatalog(String json) throws IOException {
        try {
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            if (!root.has("schemaVersion") || root.get("schemaVersion").getAsInt() != 1) {
                throw new IOException("Unsupported plugin catalogue schema");
            }
            JsonArray plugins = root.getAsJsonArray("plugins");
            if (plugins == null || plugins.isEmpty() || plugins.size() > 100) {
                throw new IOException("Plugin catalogue must contain between 1 and 100 entries");
            }
            Map<String, CatalogEntry> parsed = new LinkedHashMap<>();
            Set<String> repositories = new HashSet<>();
            Set<String> directories = new HashSet<>();
            for (var element : plugins) {
                if (!element.isJsonObject()) throw new IOException("Plugin catalogue entry must be an object");
                JsonObject entry = element.getAsJsonObject();
                String name = requiredCatalogString(entry, "name");
                String repository = requiredCatalogString(entry, "repository");
                String directory = requiredCatalogString(entry, "directory");
                if (name.length() > 80 || !name.matches("[A-Za-z0-9 ._&'()-]+")
                        || !repository.matches("Devidian/rw-plugin-oz-[A-Za-z0-9][A-Za-z0-9._-]*")
                        || !directory.matches("[A-Za-z0-9][A-Za-z0-9._-]*")) {
                    throw new IOException("Invalid plugin catalogue entry: " + name);
                }
                if (parsed.putIfAbsent(name, new CatalogEntry(repository, directory)) != null
                        || !repositories.add(repository) || !directories.add(directory)) {
                    throw new IOException("Duplicate plugin catalogue entry: " + name);
                }
            }
            return Collections.unmodifiableMap(parsed);
        } catch (RuntimeException ex) {
            throw new IOException("Invalid plugin catalogue JSON", ex);
        }
    }

    private static String requiredCatalogString(JsonObject entry, String key) throws IOException {
        if (!entry.has(key) || !entry.get(key).isJsonPrimitive()) {
            throw new IOException("Plugin catalogue entry is missing " + key);
        }
        String value = entry.get(key).getAsString().trim();
        if (value.isEmpty()) throw new IOException("Plugin catalogue entry has empty " + key);
        return value;
    }

    private JsonObject releaseJson(String repository) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(GITHUB_API_PREFIX + repository + "/releases/latest"))
                .timeout(Duration.ofSeconds(12)).header("Accept", "application/vnd.github+json").GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) throw new IOException("GitHub HTTP " + response.statusCode());
        return JsonParser.parseString(response.body()).getAsJsonObject();
    }

    private void pauseBetweenChecks() {
        long delayMillis = PluginSettings.getInstance().pluginUpdateCheckDelayBetweenPluginsSeconds * 1000L;
        if (delayMillis <= 0) return;
        try {
            Thread.sleep(delayMillis);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    public static String repositoryFrom(String url) {
        if (url == null) return null;
        String value = url.trim();
        String prefix = "https://api.github.com/repos/";
        if (value.startsWith(prefix)) value = value.substring(prefix.length());
        else if (value.startsWith("https://github.com/")) value = value.substring("https://github.com/".length());
        else return null;
        String[] parts = value.split("/");
        return parts.length >= 2 && parts[0].matches("[A-Za-z0-9_.-]+") && parts[1].matches("[A-Za-z0-9_.-]+")
                ? parts[0] + "/" + parts[1] : null;
    }

    public static Set<String> managedPluginNames() {
        ensureBundledCatalog();
        return catalog.keySet();
    }

    static int compare(String left, String right) {
        String[] a = left.replaceFirst("^[vV]", "").split("[.-]");
        String[] b = right.replaceFirst("^[vV]", "").split("[.-]");
        for (int i = 0; i < Math.max(a.length, b.length); i++) {
            int x = i < a.length && a[i].matches("\\d+") ? Integer.parseInt(a[i]) : 0;
            int y = i < b.length && b[i].matches("\\d+") ? Integer.parseInt(b[i]) : 0;
            if (x != y) return Integer.compare(x, y);
        }
        return 0;
    }

    @Override public void close() { executor.shutdownNow(); }
}
