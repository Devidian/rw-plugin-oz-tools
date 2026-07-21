package de.omegazirkel.risingworld.tools.ui;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.omegazirkel.risingworld.OZTools;
import de.omegazirkel.risingworld.tools.I18n;
import de.omegazirkel.risingworld.tools.ToolsPlayerPreferences;
import net.risingworld.api.events.player.ui.PlayerUITextFieldChangeEvent;
import net.risingworld.api.objects.Player;
import net.risingworld.api.ui.UITextField;
import net.risingworld.api.ui.style.Pivot;
import net.risingworld.api.ui.style.Position;
import net.risingworld.api.ui.style.Unit;

public class ToolsPlayerPluginSettingsPanel extends BasePlayerPluginSettingsPanel {
    private static final Set<Integer> CUSTOM_LANGUAGE_INPUT_IDS = new HashSet<>();
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
        flexWrapper.addChild(createIconStyleSettings());
        flexWrapper.addChild(createLanguageSettings());
        OZUIElement labels = defaultSettingsContainer();
        labels.addChild(defaultSettingsLabel(t().get("TC_TOOLS_SETTING_INVENTORY_LABELS", uiPlayer)));
        labels.addChild(switchButtons(uiPlayer, ToolsPlayerPreferences.showInventoryShortcutLabels(uiPlayer), event -> {
            boolean next = !ToolsPlayerPreferences.showInventoryShortcutLabels(uiPlayer);
            ToolsPlayerPreferences.setShowInventoryShortcutLabels(uiPlayer, next);
            InventoryOverlayPanel.refreshAllVisible();
            redrawContent();
        }));
        flexWrapper.addChild(labels);

    }

    private OZUIElement createLanguageSettings() {
        OZUIElement container = defaultSettingsContainer();
        boolean custom = ToolsPlayerPreferences.LANGUAGE_SOURCE_CUSTOM
                .equals(ToolsPlayerPreferences.languageSource(uiPlayer));
        container.style.height.set(custom ? 142 : 104, Unit.Pixel);
        container.addChild(defaultSettingsLabel(t().get("TC_TOOLS_SETTING_LANGUAGE", uiPlayer)));

        Dropdown source = new Dropdown(List.of(
                new DropdownOption(ToolsPlayerPreferences.LANGUAGE_SOURCE_SYSTEM,
                        t().get("TC_TOOLS_LANGUAGE_SYSTEM", uiPlayer)),
                new DropdownOption(ToolsPlayerPreferences.LANGUAGE_SOURCE_GAME,
                        t().get("TC_TOOLS_LANGUAGE_GAME", uiPlayer)),
                new DropdownOption(ToolsPlayerPreferences.LANGUAGE_SOURCE_CUSTOM,
                        t().get("TC_TOOLS_LANGUAGE_CUSTOM", uiPlayer))),
                ToolsPlayerPreferences.languageSource(uiPlayer), selected -> {
                    ToolsPlayerPreferences.setLanguageSource(uiPlayer, selected);
                    redrawContent();
                });
        source.style.position.set(Position.Absolute);
        source.style.left.set(10, Unit.Pixel);
        source.style.top.set(52, Unit.Pixel);
        source.style.width.set(92, Unit.Percent);
        container.addChild(source);

        if (custom) {
            UITextField language = new UITextField(ToolsPlayerPreferences.customLanguage(uiPlayer));
            language.setPivot(Pivot.UpperLeft);
            language.style.position.set(Position.Absolute);
            language.style.left.set(10, Unit.Pixel);
            language.style.top.set(88, Unit.Pixel);
            language.style.width.set(92, Unit.Percent);
            language.style.height.set(32, Unit.Pixel);
            language.setFontSize(13);
            language.setFontColor(0xF4F0E6FF);
            language.setMaxCharacters(8);
            language.setBackgroundColor(0x10100EE8);
            language.setBorder(1);
            language.setBorderColor(0x5E4A25FF);
            synchronized (CUSTOM_LANGUAGE_INPUT_IDS) {
                CUSTOM_LANGUAGE_INPUT_IDS.add(language.getID());
            }
            container.addChild(language);
        }
        return container;
    }

    public static void handleTextFieldChange(PlayerUITextFieldChangeEvent event) {
        if (event == null || event.getPlayer() == null || event.getUITextField() == null) return;
        synchronized (CUSTOM_LANGUAGE_INPUT_IDS) {
            if (!CUSTOM_LANGUAGE_INPUT_IDS.contains(event.getUITextField().getID())) return;
        }
        ToolsPlayerPreferences.setCustomLanguage(event.getPlayer(), event.getNewText());
    }

}
