package de.omegazirkel.risingworld.tools.ui;

import net.risingworld.api.objects.Player;

public abstract class PlayerPluginSettings {
    public String pluginLabel;

    public abstract BasePlayerPluginSettingsPanel createPlayerPluginSettingsUIElement(Player uiPlayer);
}
