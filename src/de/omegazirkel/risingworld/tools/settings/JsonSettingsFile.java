package de.omegazirkel.risingworld.tools.settings;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import net.risingworld.api.World;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonParser;

/** JSON configuration reader/writer with one-time legacy-properties migration. */
public final class JsonSettingsFile {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private JsonSettingsFile() {
    }

    public static Map<String, String> loadFlat(Path file) throws IOException {
        if (Files.notExists(file)) return new LinkedHashMap<>();
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            JsonElement element = JsonParser.parseReader(reader);
            if (!element.isJsonObject()) throw new IOException("Settings root must be a JSON object: " + file);
            Map<String, String> flat = new LinkedHashMap<>();
            flatten("", element.getAsJsonObject(), flat);
            return flat;
        }
    }

    public static Properties loadProperties(Path file) throws IOException {
        Properties properties = new Properties();
        loadFlat(file).forEach(properties::setProperty);
        return properties;
    }

    public static Path worldSettingsFile(String pluginPath) {
        String world = "default";
        try {
            world = World.getName();
        } catch (LinkageError ignored) {
            // PluginAPI's native World methods are intentionally unavailable in unit-test JVMs.
        }
        String safeWorld = (world == null || world.isBlank() ? "default" : world)
                .replaceAll("[^A-Za-z0-9._-]", "_");
        return Path.of(pluginPath == null ? "." : pluginPath).resolve("settings." + safeWorld + ".json");
    }

    public static void writeFlatAtomically(Path file, Map<String, String> values) throws IOException {
        JsonObject root = new JsonObject();
        values.forEach((key, value) -> put(root, key, value));
        Path directory = file.toAbsolutePath().getParent();
        if (directory != null) Files.createDirectories(directory);
        Path temporary = Files.createTempFile(directory, file.getFileName().toString(), ".tmp");
        try (Writer writer = Files.newBufferedWriter(temporary, StandardCharsets.UTF_8)) {
            GSON.toJson(root, writer);
        }
        try {
            Files.move(temporary, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException ex) {
            Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /** Copies a JSON settings document without flattening typed arrays. */
    public static void copyAtomically(Path source, Path target) throws IOException {
        Path directory = target.toAbsolutePath().getParent();
        if (directory != null) Files.createDirectories(directory);
        Path temporary = Files.createTempFile(directory, target.getFileName().toString(), ".tmp");
        Files.copy(source, temporary, StandardCopyOption.REPLACE_EXISTING);
        try {
            Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException ex) {
            Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public static boolean migrateCsvToArray(Path file, String path) throws IOException {
        if (Files.notExists(file)) return false;
        JsonObject root;
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            JsonElement element = JsonParser.parseReader(reader);
            if (!element.isJsonObject()) throw new IOException("Settings root must be a JSON object: " + file);
            root = element.getAsJsonObject();
        }
        String[] segments = path.split("\\.");
        JsonObject current = root;
        for (int i = 0; i < segments.length - 1; i++) {
            JsonElement child = current.get(segments[i]);
            if (child == null || !child.isJsonObject()) return false;
            current = child.getAsJsonObject();
        }
        JsonElement value = current.get(segments[segments.length - 1]);
        if (value == null || value.isJsonArray() || !value.isJsonPrimitive()) return false;
        JsonArray array = new JsonArray();
        for (String item : value.getAsString().split(",")) {
            String trimmed = item.trim();
            if (!trimmed.isEmpty()) array.add(trimmed);
        }
        current.add(segments[segments.length - 1], array);
        writeJsonAtomically(file, root);
        return true;
    }

    public static boolean migrateLegacyProperties(Path legacyFile, Path jsonFile) throws IOException {
        if (Files.exists(jsonFile) || Files.notExists(legacyFile)) return false;
        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(legacyFile, StandardCharsets.UTF_8)) {
            properties.load(reader);
        }
        Map<String, String> flat = new LinkedHashMap<>();
        properties.stringPropertyNames().stream().sorted()
                .forEach(key -> flat.put(key, properties.getProperty(key)));
        writeFlatAtomically(jsonFile, flat);
        Path backup = legacyFile.resolveSibling(legacyFile.getFileName() + ".migrated-" + Instant.now().toEpochMilli());
        Files.move(legacyFile, backup, StandardCopyOption.REPLACE_EXISTING);
        return true;
    }

    /**
     * Replaces former flat setting names with their canonical JSON paths without
     * changing the configured values. Existing canonical values always win.
     */
    public static boolean normalizePaths(Path file, Map<String, String> legacyToCanonical) throws IOException {
        if (Files.notExists(file) || legacyToCanonical.isEmpty()) return false;
        Map<String, String> values = loadFlat(file);
        boolean changed = false;
        for (Map.Entry<String, String> entry : legacyToCanonical.entrySet()) {
            String legacy = entry.getKey();
            String canonical = entry.getValue();
            String legacyValue = values.remove(legacy);
            if (legacyValue != null) {
                values.putIfAbsent(canonical, legacyValue);
                changed = true;
            }
        }
        if (changed) writeFlatAtomically(file, values);
        return changed;
    }

    public static boolean normalizePaths(Path file) throws IOException {
        if (Files.notExists(file) || file.getFileName().toString().endsWith(".properties")) return false;
        Map<String, String> values = loadFlat(file);
        Map<String, String> normalized = new LinkedHashMap<>();
        boolean changed = false;
        for (Map.Entry<String, String> entry : values.entrySet()) {
            String canonical = canonicalPath(entry.getKey());
            normalized.putIfAbsent(canonical, entry.getValue());
            changed |= !canonical.equals(entry.getKey());
        }
        if (changed) writeFlatAtomically(file, normalized);
        return changed;
    }

    public static String canonicalPath(String key) {
        if (key.startsWith("general.") || key.startsWith("feature.") || key.startsWith("discord.")
                || key.startsWith("color.") || key.startsWith("expose.") || key.startsWith("botCMD.")
                || key.startsWith("teleportToken.") || key.startsWith("useMarker.")
                || key.startsWith("restrictToSector.")) return key;
        if ("colorizeChat".equals(key)) return "feature.colorizeChat";
        if (key.startsWith("enable") && key.length() > 6) return "feature." + decapitalize(key.substring(6));
        if (key.startsWith("expose") && key.length() > 6) return "expose." + decapitalize(key.substring(6));
        if (key.startsWith("discord") && key.length() > 7) return "discord." + decapitalize(key.substring(7));
        if (key.startsWith("color") && key.length() > 5) return "color." + decapitalize(key.substring(5));
        if (key.startsWith("botCMD") && key.length() > 6) return "botCMD." + key.substring(6);
        if (key.startsWith("teleportToken") && key.length() > 13) return "teleportToken." + decapitalize(key.substring(13));
        if (key.startsWith("use") && key.endsWith("MarkerCooldownSeconds"))
            return "useMarker." + decapitalize(key.substring(3));
        if (key.startsWith("use") && key.endsWith("MarkerCost"))
            return "useMarker." + decapitalize(key.substring(3));
        if (key.startsWith("restrict") && key.endsWith("ToSector"))
            return "restrictToSector." + decapitalize(key.substring(8, key.length() - 8));
        return "general." + key;
    }

    public static void addCompatibilityAliases(Properties properties) {
        Map<String, String> aliases = new LinkedHashMap<>();
        for (String canonical : properties.stringPropertyNames()) {
            String legacy = legacyPath(canonical);
            if (legacy != null) aliases.put(legacy, canonical);
        }
        addCompatibilityAliases(properties, aliases);
    }

    private static String legacyPath(String canonical) {
        if (canonical.startsWith("general.")) return canonical.substring(8);
        if ("feature.colorizeChat".equals(canonical)) return "colorizeChat";
        if (canonical.startsWith("feature.")) return "enable" + capitalize(canonical.substring(8));
        if (canonical.startsWith("expose.")) return "expose" + capitalize(canonical.substring(7));
        if (canonical.startsWith("discord.")) return "discord" + capitalize(canonical.substring(8));
        if (canonical.startsWith("color.")) return "color" + capitalize(canonical.substring(6));
        if (canonical.startsWith("botCMD.")) return "botCMD" + canonical.substring(7);
        if (canonical.startsWith("teleportToken.")) return "teleportToken" + capitalize(canonical.substring(14));
        if (canonical.startsWith("useMarker.")) return "use" + capitalize(canonical.substring(10));
        if (canonical.startsWith("restrictToSector.")) return "restrict" + capitalize(canonical.substring(17)) + "ToSector";
        return null;
    }

    private static String decapitalize(String value) {
        return Character.toLowerCase(value.charAt(0)) + value.substring(1);
    }

    private static String capitalize(String value) {
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }

    /**
     * Provides compatibility aliases for consumers that still use the former
     * properties names while the JSON document is stored using canonical paths.
     */
    public static void addCompatibilityAliases(Properties properties, Map<String, String> legacyToCanonical) {
        for (Map.Entry<String, String> entry : legacyToCanonical.entrySet()) {
            String legacy = entry.getKey();
            String canonical = entry.getValue();
            String canonicalValue = properties.getProperty(canonical);
            String legacyValue = properties.getProperty(legacy);
            if (canonicalValue == null && legacyValue != null) properties.setProperty(canonical, legacyValue);
            if (legacyValue == null && canonicalValue != null) properties.setProperty(legacy, canonicalValue);
        }
    }

    private static void flatten(String prefix, JsonObject object, Map<String, String> flat) {
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            String path = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
            JsonElement value = entry.getValue();
            if (value.isJsonObject()) flatten(path, value.getAsJsonObject(), flat);
            else if (value.isJsonArray()) flat.put(path, arrayAsCsv(path, value.getAsJsonArray()));
            else if (!value.isJsonNull() && value.isJsonPrimitive()) flat.put(path, value.getAsString());
            else throw new IllegalArgumentException("Unsupported JSON settings value at " + path);
        }
    }

    private static void writeJsonAtomically(Path file, JsonObject root) throws IOException {
        Path directory = file.toAbsolutePath().getParent();
        if (directory != null) Files.createDirectories(directory);
        Path temporary = Files.createTempFile(directory, file.getFileName().toString(), ".tmp");
        try (Writer writer = Files.newBufferedWriter(temporary, StandardCharsets.UTF_8)) {
            GSON.toJson(root, writer);
        }
        try {
            Files.move(temporary, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException ex) {
            Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /**
     * Existing settings consumers use comma-separated strings for allowlists.
     * JSON arrays are accepted at the file boundary and exposed in that legacy
     * representation until each consumer has a typed list setting API.
     */
    private static String arrayAsCsv(String path, JsonArray array) {
        StringBuilder values = new StringBuilder();
        for (JsonElement element : array) {
            if (element.isJsonNull() || !element.isJsonPrimitive()) {
                throw new IllegalArgumentException("Unsupported JSON array value at " + path);
            }
            if (!values.isEmpty()) values.append(',');
            values.append(element.getAsString());
        }
        return values.toString();
    }

    private static void put(JsonObject root, String path, String value) {
        String[] segments = path.split("\\.");
        JsonObject current = root;
        for (int i = 0; i < segments.length - 1; i++) {
            JsonElement child = current.get(segments[i]);
            if (child == null || !child.isJsonObject()) {
                JsonObject nested = new JsonObject();
                current.add(segments[i], nested);
                current = nested;
            } else current = child.getAsJsonObject();
        }
        current.add(segments[segments.length - 1], primitive(value));
    }

    private static JsonPrimitive primitive(String value) {
        if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
            return new JsonPrimitive(Boolean.parseBoolean(value));
        }
        try {
            return new JsonPrimitive(Long.parseLong(value));
        } catch (NumberFormatException ignored) {
            // Decimal values are intentionally parsed only after integral values.
        }
        try {
            return new JsonPrimitive(Double.parseDouble(value));
        } catch (NumberFormatException ignored) {
            return new JsonPrimitive(value);
        }
    }
}
