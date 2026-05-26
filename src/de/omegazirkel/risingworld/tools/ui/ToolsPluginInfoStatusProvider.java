package de.omegazirkel.risingworld.tools.ui;

import de.omegazirkel.risingworld.OZTools;
import de.omegazirkel.risingworld.tools.I18n;
import net.risingworld.api.objects.Player;

public class ToolsPluginInfoStatusProvider implements PluginInfoStatusProvider {
    private final String pluginName;
    private final String pluginVersion;
    private final String pluginCommand;

    public ToolsPluginInfoStatusProvider(String pluginName, String pluginVersion, String pluginCommand) {
        this.pluginName = pluginName == null || pluginName.isBlank() ? "OZTools" : pluginName;
        this.pluginVersion = pluginVersion == null ? "" : pluginVersion;
        this.pluginCommand = pluginCommand == null ? "" : pluginCommand;
    }

    @Override
    public String getPluginName() {
        return pluginName;
    }

    @Override
    public String getInfo(Player player) {
        return t().get("TC_TOOLS_INFO_PANEL_INFO", player)
                .replace("PH_PLUGIN_NAME", pluginName)
                .replace("PH_VERSION", pluginVersion)
                .replace("PH_PLUGIN_CMD", pluginCommand);
    }

    @Override
    public String getStatus(Player player) {
        return t().get("TC_TOOLS_INFO_PANEL_STATUS", player);
    }

    private static I18n t() {
        return I18n.getInstance(OZTools.name);
    }
}
