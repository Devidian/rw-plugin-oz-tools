package de.omegazirkel.risingworld.tools.settings;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import de.omegazirkel.risingworld.OZTools;

public final class SettingsFileEditor {
    private SettingsFileEditor() {
    }

    public static boolean writeValue(Path settingsFile, String key, String value) {
        if (settingsFile == null || key == null || key.isBlank()) {
            return false;
        }
        try {
            if (settingsFile.getFileName().toString().endsWith(".json")) {
                Map<String, String> values = JsonSettingsFile.loadFlat(settingsFile);
                values.put(key, value);
                JsonSettingsFile.writeFlatAtomically(settingsFile, values);
                return true;
            }
            /* Legacy editor retained solely for an unmigrated runtime file. */
            java.util.List<String> lines = Files.exists(settingsFile)
                    ? Files.readAllLines(settingsFile, java.nio.charset.StandardCharsets.UTF_8)
                    : new java.util.ArrayList<>();
            String prefix = key + "=";
            boolean replaced = false;
            for (int i = 0; i < lines.size(); i++) {
                String trimmed = lines.get(i).trim();
                if (trimmed.startsWith("#") || trimmed.startsWith(";")) {
                    continue;
                }
                if (trimmed.startsWith(prefix)) {
                    lines.set(i, prefix + value);
                    replaced = true;
                    break;
                }
            }
            if (!replaced) {
                lines.add(prefix + value);
            }
            Files.write(settingsFile, lines, java.nio.charset.StandardCharsets.UTF_8);
            return true;
        } catch (IOException ex) {
            OZTools.logger().error("Failed to update settings value " + key + ": " + ex.getMessage());
            return false;
        }
    }
}
