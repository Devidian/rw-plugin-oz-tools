package de.omegazirkel.risingworld.tools;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import de.omegazirkel.risingworld.OZTools;
import net.risingworld.api.Plugin;

/** Bounded public-GitHub release checks. Package installation deliberately uses
 * the same allow-list decision as checking and is only exposed to admins. */
public final class PluginUpdateService implements AutoCloseable {
    public enum State { UNKNOWN, CURRENT, UPDATE_AVAILABLE, NOT_INSTALLED, INSTALLING, ERROR }
    public record Result(String installedVersion, String latestVersion, String releaseUrl, State state) { }
    private record CatalogEntry(String repository, String directory) { }
    private static final Map<String, CatalogEntry> CATALOG = Map.ofEntries(
            Map.entry("OZ - Tools", new CatalogEntry("Devidian/rw-plugin-oz-tools", "OZTools")),
            Map.entry("OZ - Marketplace", new CatalogEntry("Devidian/rw-plugin-oz-marketplace", "OZMarketplace")),
            Map.entry("OZ - Wallet", new CatalogEntry("Devidian/rw-plugin-oz-wallet", "OZWallet")),
            Map.entry("OZ - Shop", new CatalogEntry("Devidian/rw-plugin-oz-shop", "OZShop")),
            Map.entry("OZ - GPS", new CatalogEntry("Devidian/rw-plugin-oz-gps", "OZGPS")),
            Map.entry("OZ - Land Claim", new CatalogEntry("Devidian/rw-plugin-oz-land-claim", "OZLandClaim")),
            Map.entry("OZ - Rewards", new CatalogEntry("Devidian/rw-plugin-oz-rewards", "OZRewards")),
            Map.entry("OZ - Mail", new CatalogEntry("Devidian/rw-plugin-oz-mail", "OZMail")),
            Map.entry("OZ - Admin Utils", new CatalogEntry("Devidian/rw-plugin-oz-admin-utils", "OZAdminUtils")),
            Map.entry("OZ - Discord Connect", new CatalogEntry("Devidian/rw-plugin-oz-discord-connect", "OZDiscordConnect")),
            Map.entry("OZ - Global Intercom", new CatalogEntry("Devidian/rw-plugin-oz-global-intercom", "OZGlobalIntercom")));

    private static final String GITHUB_API_PREFIX = "https://api.github.com/repos/";
    private final OZTools tools;
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(8))
            .followRedirects(HttpClient.Redirect.NORMAL).build();
    private final ExecutorService executor = Executors.newSingleThreadExecutor(new DiagnosticThreadFactory(
            "OZ Tools", "public GitHub plugin release checks", "OZ-Plugin-Updates", true,
            message -> OZTools.logger().debug(message)));
    private volatile Map<String, Result> results = Map.of();
    private final AtomicBoolean checkQueued = new AtomicBoolean();

    public PluginUpdateService(OZTools tools) { this.tools = tools; }

    public Map<String, Result> results() { return results; }

    public void checkAsync() { checkAsync(null, null); }

    public void checkAsync(Consumer<String> checkedPlugin, Consumer<Boolean> completed) {
        if (!checkQueued.compareAndSet(false, true)) return;
        executor.execute(() -> {
            try {
                checkInstalled(checkedPlugin, completed);
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
                    State.INSTALLING));
        }
        executor.execute(() -> installLatest(pluginName, onSuccess, onFailure, previous));
    }

    private void installLatest(String pluginName, Runnable onSuccess, Consumer<String> onFailure, Result previous) {
        Plugin plugin = null;
        for (Plugin candidate : tools.getAllPlugins()) {
            if (pluginName.equals(candidate.getDescription("name"))) { plugin = candidate; break; }
        }
        CatalogEntry catalog = CATALOG.get(pluginName);
        if (plugin == null && catalog == null) { OZTools.logger().warn("Plugin installation rejected: unknown plugin: " + pluginName); return; }
        String repository = plugin == null ? catalog.repository() : repositoryFrom(plugin.getDescription("website"));
        if (repository == null || (!PluginSettings.getInstance().allowExternalPluginRepositories && !repository.startsWith("Devidian/"))) {
            OZTools.logger().warn("Plugin installation rejected: untrusted release source for " + pluginName); return;
        }
        try {
            String assetUrl = selectZipAsset(release(repository));
            Path target = plugin == null ? Path.of(tools.getPath()).toAbsolutePath().normalize().getParent().resolve(catalog.directory())
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
            } finally { deleteTree(staging); }
        } catch (Exception ex) {
            if (previous != null) updateResult(pluginName, previous);
            OZTools.logger().error("Plugin installation failed for " + pluginName + ": " + ex.getMessage());
            if (onFailure != null) onFailure.accept(ex.getMessage());
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
    }

    private JsonObject release(String repository) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(GITHUB_API_PREFIX + repository + "/releases/latest"))
                .timeout(Duration.ofSeconds(12)).header("Accept", "application/vnd.github+json").GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) throw new IOException("GitHub HTTP " + response.statusCode());
        return JsonParser.parseString(response.body()).getAsJsonObject();
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
    private static void deleteTree(Path path) throws IOException {
        if (path == null || !Files.exists(path)) return;
        try (var paths = Files.walk(path)) { paths.sorted(java.util.Comparator.reverseOrder()).forEach(p -> { try { Files.deleteIfExists(p); } catch (IOException ex) { throw new java.io.UncheckedIOException(ex); } }); }
    }

    private void checkInstalled(Consumer<String> checkedPlugin, Consumer<Boolean> completed) {
        Map<String, Result> checked = new LinkedHashMap<>();
        Map<String, Plugin> installed = new LinkedHashMap<>();
        for (Plugin plugin : tools.getAllPlugins()) installed.put(plugin.getDescription("name"), plugin);
        for (String name : CATALOG.keySet()) if (!installed.containsKey(name)) checked.put(name, new Result("N/A", "", "", State.NOT_INSTALLED));
        boolean firstRequest = true;
        for (Plugin plugin : installed.values()) {
            String website = plugin.getDescription("website");
            String repository = repositoryFrom(website);
            if (repository == null || (!PluginSettings.getInstance().allowExternalPluginRepositories
                    && !repository.startsWith("Devidian/"))) continue;
            String installedVersion = plugin.getDescription("version");
            try {
                if (!firstRequest) pauseBetweenChecks();
                firstRequest = false;
                if (checkedPlugin != null) checkedPlugin.accept(plugin.getDescription("name"));
                HttpRequest request = HttpRequest.newBuilder(URI.create(GITHUB_API_PREFIX + repository + "/releases/latest"))
                        .timeout(Duration.ofSeconds(12)).header("Accept", "application/vnd.github+json").GET().build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() != 200) throw new IOException("GitHub HTTP " + response.statusCode());
                JsonObject release = JsonParser.parseString(response.body()).getAsJsonObject();
                String latest = release.get("tag_name").getAsString().replaceFirst("^[vV]", "");
                String url = release.get("html_url").getAsString();
                checked.put(plugin.getDescription("name"), new Result(installedVersion, latest, url,
                        compare(latest, installedVersion) > 0 ? State.UPDATE_AVAILABLE : State.CURRENT));
            } catch (Exception ex) {
                checked.put(plugin.getDescription("name"), new Result(installedVersion, "", "", State.ERROR));
                OZTools.logger().warn("Plugin update check failed for " + plugin.getDescription("name") + ": " + ex.getMessage());
            }
        }
        results = Collections.unmodifiableMap(checked);
        if (completed != null) completed.accept(checked.values().stream()
                .anyMatch(result -> result.state() == State.UPDATE_AVAILABLE));
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

    public static java.util.Set<String> managedPluginNames() { return CATALOG.keySet(); }

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
