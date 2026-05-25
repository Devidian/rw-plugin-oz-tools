package de.omegazirkel.risingworld.tools.ui;

import de.omegazirkel.risingworld.OZTools;
import de.omegazirkel.risingworld.tools.I18n;
import de.omegazirkel.risingworld.tools.PluginSettings;
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
                .replace("PH_PLUGIN_CMD", pluginCommand);
    }

    @Override
    public String getStatus(Player player) {
        PluginSettings settings = OZTools.getSettings();
        return t().get("TC_TOOLS_INFO_PANEL_STATUS", player)
                .replace("PH_VERSION", pluginVersion)
                .replace("PH_LOG_LEVEL", settings == null ? "" : settings.logLevel)
                .replace("PH_LOG_INTERNAL", booleanText(player, settings != null && settings.logInternal))
                .replace("PH_RELOAD_ON_CHANGE", booleanText(player, settings != null && settings.reloadOnChange))
                .replace("PH_WELCOME_MESSAGE",
                        booleanText(player, settings != null && settings.enablePluginWelcomeMessage));
    }

    private static I18n t() {
        return I18n.getInstance(OZTools.name);
    }

    private static String booleanText(Player player, boolean value) {
        return t().get(value ? "TC_UI_BTN_ON" : "TC_UI_BTN_OFF", player);
    }
}
