package de.omegazirkel.risingworld.tools.ui;

import de.omegazirkel.risingworld.OZTools;
import de.omegazirkel.risingworld.tools.I18n;
import de.omegazirkel.risingworld.tools.ToolsPlayerPreferences;
import net.risingworld.api.objects.Player;

public class ToolsPlayerPluginSettingsPanel extends BasePlayerPluginSettingsPanel {
    private final Player uiPlayer;

    private static I18n t() {
        return I18n.getInstance(OZTools.name);
    }

    public ToolsPlayerPluginSettingsPanel(Player uiPlayer, String pluginLabel) {
        super(uiPlayer, pluginLabel);
        this.uiPlayer = uiPlayer;
    }

    @Override
    protected void redrawContent() {
        flexWrapper.removeAllChilds();
        OZUIElement labels = defaultSettingsContainer();
        labels.addChild(defaultSettingsLabel(t().get("TC_TOOLS_SETTING_INVENTORY_LABELS", uiPlayer)));
        labels.addChild(switchButtons(uiPlayer, ToolsPlayerPreferences.showInventoryShortcutLabels(uiPlayer), event -> {
            boolean next = !ToolsPlayerPreferences.showInventoryShortcutLabels(uiPlayer);
            ToolsPlayerPreferences.setShowInventoryShortcutLabels(uiPlayer, next);
            InventoryOverlayPanel.refreshAllVisible();
            redrawContent();
        }));
        flexWrapper.addChild(labels);

        OZUIElement iconStyle = defaultSettingsContainer();
        iconStyle.addChild(defaultSettingsLabel(t().get("TC_TOOLS_SETTING_ICON_STYLE", uiPlayer)));
        iconStyle.addChild(switchButtons(
                uiPlayer,
                ToolsPlayerPreferences.classicIconStyle(uiPlayer),
                event -> {
                    String next = ToolsPlayerPreferences.classicIconStyle(uiPlayer)
                            ? ToolsPlayerPreferences.ICON_STYLE_MODERN
                            : ToolsPlayerPreferences.ICON_STYLE_CLASSIC;
                    ToolsPlayerPreferences.setIconStyle(uiPlayer, next);
                    redrawContent();
                },
                t().get("TC_TOOLS_ICON_STYLE_MODERN", uiPlayer),
                t().get("TC_TOOLS_ICON_STYLE_CLASSIC", uiPlayer)));
        flexWrapper.addChild(iconStyle);
    }

}
