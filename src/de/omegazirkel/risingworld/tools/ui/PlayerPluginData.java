package de.omegazirkel.risingworld.tools.ui;

import net.risingworld.api.objects.Player;

public abstract class PlayerPluginData {
    public String pluginLabel;
    public String pluginVersion;

    public abstract BasePlayerPluginDataPanel createPlayerPluginDataUIElement(Player uiPlayer);
}
