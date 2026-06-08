package de.omegazirkel.risingworld.tools.ui;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import net.risingworld.api.objects.Player;

public final class PluginShortcutVisibility {
    private static final Map<String, Predicate<Player>> VISIBILITY = new ConcurrentHashMap<>();

    private PluginShortcutVisibility() {
    }

    public static String playerSettingKey(String pluginName) {
        String normalized = pluginName == null ? "plugin" : pluginName.trim().toLowerCase().replaceAll("[^a-z0-9]+", ".");
        if (normalized.isBlank()) {
            normalized = "plugin";
        }
        return normalized + ".shortcut.visible";
    }

    public static void register(String pluginName, Predicate<Player> visibility) {
        String normalized = normalize(pluginName);
        if (normalized == null || visibility == null) {
            return;
        }
        VISIBILITY.put(normalized, visibility);
        InventoryOverlayPanel.refreshAllVisible();
    }

    public static void unregister(String pluginName) {
        String normalized = normalize(pluginName);
        if (normalized == null) {
            return;
        }
        VISIBILITY.remove(normalized);
        InventoryOverlayPanel.refreshAllVisible();
    }

    public static boolean isVisible(String pluginName, Player player) {
        String normalized = normalize(pluginName);
        if (normalized == null) {
            return true;
        }
        Predicate<Player> visibility = VISIBILITY.get(normalized);
        if (visibility == null) {
            return true;
        }
        try {
            return visibility.test(player);
        } catch (RuntimeException ex) {
            return true;
        }
    }

    private static String normalize(String pluginName) {
        if (pluginName == null || pluginName.isBlank()) {
            return null;
        }
        return pluginName.trim();
    }
}
