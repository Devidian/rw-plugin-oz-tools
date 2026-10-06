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
        OZUIElement quickMenu = defaultSettingsContainer();
        quickMenu.style.height.set(104, Unit.Pixel);
        quickMenu.addChild(defaultSettingsLabel(t().get("tc.tools.setting.quick.menu.scale", uiPlayer)));
        int selectedScale = ToolsPlayerPreferences.quickMenuScale(uiPlayer);
        for (int value = 1; value <= 3; value++) {
            final int nextScale = value;
            boolean selected = value == selectedScale;
            AdvancedButton button = AdvancedButtonFactory.custom(new AdvancedButtonState(
                    AdvancedBaseButton.State.DEFAULT,
                    selected ? 0xD7AE55FF : 0x7A5D2AFF,
                    selected ? 0x1D4D2AFF : 0x182F20FF,
                    selected ? 0xF2C766FF : 0xD8D0C0FF,
                    0xD7AE55FF, selected ? 0x286B39FF : 0x244D30FF,
                    "x" + value, event -> {
                        if (ToolsPlayerPreferences.quickMenuScale(uiPlayer) == nextScale) return;
                        ToolsPlayerPreferences.setQuickMenuScale(uiPlayer, nextScale);
                        redrawContent();
                    }));
            button.setPivot(Pivot.UpperLeft);
            button.setPosition(10 + (value - 1) * 78, 56, false);
            button.setSize(72, 28, false);
            quickMenu.addChild(button);
        }
        flexWrapper.addChild(quickMenu);
        OZUIElement labels = defaultSettingsContainer();
        labels.addChild(defaultSettingsLabel(t().get("tc.tools.setting.inventory.labels", uiPlayer)));
        labels.addChild(switchButtons(uiPlayer, ToolsPlayerPreferences.showInventoryShortcutLabels(uiPlayer), event -> {
            boolean next = !ToolsPlayerPreferences.showInventoryShortcutLabels(uiPlayer);
            ToolsPlayerPreferences.setShowInventoryShortcutLabels(uiPlayer, next);
            InventoryOverlayPanel.refreshAllVisible();
            redrawContent();
        }));
        flexWrapper.addChild(labels);
        OZUIElement shortcuts = defaultSettingsContainer();
        shortcuts.addChild(defaultSettingsLabel(t().get("tc.tools.setting.inventory.shortcuts", uiPlayer)));
        shortcuts.addChild(switchButtons(uiPlayer, ToolsPlayerPreferences.showInventoryShortcuts(uiPlayer), event -> {
            boolean next = !ToolsPlayerPreferences.showInventoryShortcuts(uiPlayer);
            ToolsPlayerPreferences.setShowInventoryShortcuts(uiPlayer, next);
            InventoryOverlayPanel.refreshAllVisible();
            redrawContent();
        }));
        flexWrapper.addChild(shortcuts);

    }

    private OZUIElement createLanguageSettings() {
        OZUIElement container = defaultSettingsContainer();
        boolean custom = ToolsPlayerPreferences.LANGUAGE_SOURCE_CUSTOM
                .equals(ToolsPlayerPreferences.languageSource(uiPlayer));
        container.style.height.set(custom ? 142 : 104, Unit.Pixel);
        container.addChild(defaultSettingsLabel(t().get("tc.tools.setting.language", uiPlayer)));

        Dropdown source = new Dropdown(List.of(
                new DropdownOption(ToolsPlayerPreferences.LANGUAGE_SOURCE_SYSTEM,
                        t().get("tc.tools.language.system", uiPlayer)),
                new DropdownOption(ToolsPlayerPreferences.LANGUAGE_SOURCE_GAME,
                        t().get("tc.tools.language.game", uiPlayer)),
                new DropdownOption(ToolsPlayerPreferences.LANGUAGE_SOURCE_CUSTOM,
                        t().get("tc.tools.language.custom", uiPlayer))),
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
