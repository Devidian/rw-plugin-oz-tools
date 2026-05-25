package de.omegazirkel.risingworld.tools.ui;

import net.risingworld.api.callbacks.Callback;
import net.risingworld.api.events.player.ui.PlayerUIElementClickEvent;

public final class InventoryOverlayButton {
    private final String pluginName;
    private final String label;
    private final String iconKey;
    private final Callback<PlayerUIElementClickEvent> callback;

    InventoryOverlayButton(
            String pluginName,
            String label,
            String iconKey,
            Callback<PlayerUIElementClickEvent> callback) {
        this.pluginName = pluginName;
        this.label = label;
        this.iconKey = iconKey;
        this.callback = callback;
    }

    public String getPluginName() {
        return pluginName;
    }

    public String getLabel() {
        return label;
    }

    public String getIconKey() {
        return iconKey;
    }

    public Callback<PlayerUIElementClickEvent> getCallback() {
        return callback;
    }
}
