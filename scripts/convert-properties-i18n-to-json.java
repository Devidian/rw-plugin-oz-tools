import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

/** One-time mechanical migration helper for OZ translation catalogs. */
final class ConvertPropertiesI18nToJson {
    public static void main(String[] args) throws Exception {
        if (args.length != 2) throw new IllegalArgumentException("usage: <source.properties> <target.json>");
        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(Path.of(args[0]), StandardCharsets.UTF_8)) {
            properties.load(reader);
        }
        JsonObject root = new JsonObject();
        Map<String, String> sorted = new TreeMap<>();
        properties.stringPropertyNames().stream().filter(key -> !key.startsWith(";") && !key.startsWith("#"))
                .forEach(key -> sorted.put(key, properties.getProperty(key)));
        sorted.forEach((key, value) -> put(root, canonical(key), value));
        try (Writer writer = Files.newBufferedWriter(Path.of(args[1]), StandardCharsets.UTF_8)) {
            new GsonBuilder().setPrettyPrinting().create().toJson(root, writer);
        }
    }

    private static String canonical(String key) {
        return key.matches("[A-Z0-9_]+") ? key.toLowerCase().replace('_', '.') : key;
    }

    private static void put(JsonObject root, String path, String value) {
        String[] parts = path.split("\\.");
        JsonObject target = root;
        for (int i = 0; i < parts.length - 1; i++) {
            if (target.has(parts[i]) && !target.get(parts[i]).isJsonObject()) {
                JsonObject nested = new JsonObject();
                nested.add("$value", target.get(parts[i]));
                target.add(parts[i], nested);
            }
            if (!target.has(parts[i])) target.add(parts[i], new JsonObject());
            target = target.getAsJsonObject(parts[i]);
        }
        String leaf = parts[parts.length - 1];
        if (target.has(leaf) && target.get(leaf).isJsonObject()) {
            target.getAsJsonObject(leaf).addProperty("$value", value);
        } else target.addProperty(leaf, value);
    }
}
