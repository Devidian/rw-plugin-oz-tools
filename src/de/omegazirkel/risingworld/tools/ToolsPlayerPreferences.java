package de.omegazirkel.risingworld.tools;

import net.risingworld.api.objects.Player;

public final class ToolsPlayerPreferences {
    public static final String SHOW_INVENTORY_SHORTCUT_LABELS = "oztools.inventoryShortcutLabels.visible";

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
}
