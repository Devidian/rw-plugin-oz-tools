package de.omegazirkel.risingworld.tools;

import net.risingworld.api.objects.Player;

public final class ToolsPlayerPreferences {
    public static final String SHOW_INVENTORY_SHORTCUT_LABELS = "oztools.inventoryShortcutLabels.visible";
    public static final String ICON_STYLE = "oztools.iconStyle";
    public static final String ICON_STYLE_MODERN = "modern";
    public static final String ICON_STYLE_CLASSIC = "classic";

    private ToolsPlayerPreferences() {
    }

    public static boolean showInventoryShortcutLabels(Player player) {
        PlayerSettings settings = de.omegazirkel.risingworld.OZTools.playerSettings();
        if (player == null || settings == null) {
            return true;
        }
        try {
            return settings.getBoolean(player.getDbID(), SHOW_INVENTORY_SHORTCUT_LABELS).orElse(true);
        } catch (RuntimeException ex) {
            de.omegazirkel.risingworld.OZTools.logger()
                    .warn("Failed to read inventory shortcut label preference: " + ex.getMessage());
            return true;
        }
    }

    public static void setShowInventoryShortcutLabels(Player player, boolean value) {
        PlayerSettings settings = de.omegazirkel.risingworld.OZTools.playerSettings();
        if (player == null || settings == null) {
            return;
        }
        settings.setBoolean(player.getDbID(), SHOW_INVENTORY_SHORTCUT_LABELS, value);
    }

    public static String iconStyle(Player player) {
        PlayerSettings settings = de.omegazirkel.risingworld.OZTools.playerSettings();
        if (player == null || settings == null) {
            return ICON_STYLE_MODERN;
        }
        try {
            return normalizeIconStyle(settings.getString(player.getDbID(), ICON_STYLE).orElse(ICON_STYLE_MODERN));
        } catch (RuntimeException ex) {
            de.omegazirkel.risingworld.OZTools.logger()
                    .warn("Failed to read icon style preference: " + ex.getMessage());
            return ICON_STYLE_MODERN;
        }
    }

    public static boolean classicIconStyle(Player player) {
        return ICON_STYLE_CLASSIC.equals(iconStyle(player));
    }

    public static void setIconStyle(Player player, String value) {
        PlayerSettings settings = de.omegazirkel.risingworld.OZTools.playerSettings();
        if (player == null || settings == null) {
            return;
        }
        settings.setString(player.getDbID(), ICON_STYLE, normalizeIconStyle(value));
    }

    public static String normalizeIconStyle(String value) {
        if (value == null || value.isBlank()) {
            return ICON_STYLE_MODERN;
        }
        String normalized = value.trim().toLowerCase();
        return ICON_STYLE_CLASSIC.equals(normalized) ? ICON_STYLE_CLASSIC : ICON_STYLE_MODERN;
    }
}
