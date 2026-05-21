package de.omegazirkel.risingworld.tools.settings;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import de.omegazirkel.risingworld.OZTools;

public final class SettingsFileEditor {
    private SettingsFileEditor() {
    }

    public static boolean writeValue(Path settingsFile, String key, String value) {
        if (settingsFile == null || key == null || key.isBlank()) {
            return false;
        }
        try {
            List<String> lines = Files.exists(settingsFile)
                    ? Files.readAllLines(settingsFile, StandardCharsets.UTF_8)
                    : new ArrayList<>();
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
            Files.write(settingsFile, lines, StandardCharsets.UTF_8);
            return true;
        } catch (IOException ex) {
            OZTools.logger().error("Failed to update settings value " + key + ": " + ex.getMessage());
            return false;
        }
    }
}
