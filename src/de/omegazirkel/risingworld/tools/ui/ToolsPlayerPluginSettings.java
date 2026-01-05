package de.omegazirkel.risingworld.tools.ui;

import de.omegazirkel.risingworld.OZTools;
import net.risingworld.api.objects.Player;

public class ToolsPlayerPluginSettings extends PlayerPluginSettings {

    public ToolsPlayerPluginSettings() {
        this.pluginLabel = OZTools.name;
    }

    @Override
    public BasePlayerPluginSettingsPanel createPlayerPluginSettingsUIElement(Player uiPlayer) {
        return new ToolsPlayerPluginSettingsPanel(uiPlayer, pluginLabel);
    }

}
