package de.omegazirkel.risingworld.tools;

import java.util.Locale;

import net.risingworld.api.objects.Player;

public final class ToolsPlayerPreferences {
    public static final String SHOW_INVENTORY_SHORTCUT_LABELS = "oztools.inventoryShortcutLabels.visible";
    public static final String ICON_STYLE = "oztools.iconStyle";
    public static final String ICON_STYLE_MODERN = "modern";
    public static final String ICON_STYLE_CLASSIC = "classic";
    public static final String LANGUAGE_SOURCE = "oztools.language.source";
    public static final String CUSTOM_LANGUAGE = "oztools.language.custom";
    public static final String LANGUAGE_SOURCE_SYSTEM = "system";
    public static final String LANGUAGE_SOURCE_GAME = "game";
    public static final String LANGUAGE_SOURCE_CUSTOM = "custom";

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

    public static String languageSource(Player player) {
        PlayerSettings settings = de.omegazirkel.risingworld.OZTools.playerSettings();
        if (player == null || settings == null) return LANGUAGE_SOURCE_SYSTEM;
        try {
            return normalizeLanguageSource(settings.getString(player.getDbID(), LANGUAGE_SOURCE)
                    .orElse(LANGUAGE_SOURCE_SYSTEM));
        } catch (RuntimeException ex) {
            de.omegazirkel.risingworld.OZTools.logger()
                    .warn("Failed to read player language source preference: " + ex.getMessage());
            return LANGUAGE_SOURCE_SYSTEM;
        }
    }

    public static void setLanguageSource(Player player, String value) {
        PlayerSettings settings = de.omegazirkel.risingworld.OZTools.playerSettings();
        if (player == null || settings == null) return;
        settings.setString(player.getDbID(), LANGUAGE_SOURCE, normalizeLanguageSource(value));
    }

    public static String customLanguage(Player player) {
        PlayerSettings settings = de.omegazirkel.risingworld.OZTools.playerSettings();
        if (player == null || settings == null) return "en";
        try {
            return normalizeLanguageCode(settings.getString(player.getDbID(), CUSTOM_LANGUAGE).orElse("en"));
        } catch (RuntimeException ex) {
            de.omegazirkel.risingworld.OZTools.logger()
                    .warn("Failed to read custom player language preference: " + ex.getMessage());
            return "en";
        }
    }

    public static void setCustomLanguage(Player player, String value) {
        PlayerSettings settings = de.omegazirkel.risingworld.OZTools.playerSettings();
        if (player == null || settings == null) return;
        settings.setString(player.getDbID(), CUSTOM_LANGUAGE, sanitizeCustomLanguage(value));
    }

    public static String language(Player player) {
        if (player == null) return "en";
        return switch (languageSource(player)) {
            case LANGUAGE_SOURCE_GAME -> normalizeLanguageCode(player.getLanguage());
            case LANGUAGE_SOURCE_CUSTOM -> customLanguage(player);
            default -> normalizeLanguageCode(player.getSystemLanguage());
        };
    }

    public static String normalizeLanguageSource(String value) {
        if (value == null) return LANGUAGE_SOURCE_SYSTEM;
        return switch (value.trim().toLowerCase(Locale.ROOT)) {
            case LANGUAGE_SOURCE_GAME -> LANGUAGE_SOURCE_GAME;
            case LANGUAGE_SOURCE_CUSTOM -> LANGUAGE_SOURCE_CUSTOM;
            default -> LANGUAGE_SOURCE_SYSTEM;
        };
    }

    public static String normalizeLanguageCode(String value) {
        if (value == null || value.isBlank()) return "en";
        String language = value.trim().toLowerCase(Locale.ROOT).split("[-_]", 2)[0];
        return language.matches("[a-z]{2,8}") ? language : "en";
    }

    private static String sanitizeCustomLanguage(String value) {
        if (value == null) return "";
        return value.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z_-]", "");
    }
}
