package de.omegazirkel.risingworld.tools.ui;

import net.risingworld.api.objects.Player;
import net.risingworld.api.ui.UILabel;

public class ToolsPlayerPluginSettingsPanel extends BasePlayerPluginSettingsPanel {

    public ToolsPlayerPluginSettingsPanel(Player uiPlayer, String pluginLabel) {
        super(uiPlayer, pluginLabel);
    }

    @Override
    protected void redrawContent() {
        flexWrapper.removeAllChilds();
        // TODO: implement actual settings content for OZ - Tools plugin
        UILabel placeholderLabel = new UILabel("OZ - Tools plugin settings will be here.");
        flexWrapper.addChild(placeholderLabel);
    }

}
