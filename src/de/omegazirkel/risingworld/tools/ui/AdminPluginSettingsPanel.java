package de.omegazirkel.risingworld.tools.ui;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import de.omegazirkel.risingworld.OZTools;
import de.omegazirkel.risingworld.tools.I18n;
import de.omegazirkel.risingworld.tools.settings.AdminSettingsEntry;
import de.omegazirkel.risingworld.tools.settings.AdminSettingsType;
import de.omegazirkel.risingworld.tools.settings.PlayerPluginAdminSettings;
import net.risingworld.api.events.player.ui.PlayerUITextFieldChangeEvent;
import net.risingworld.api.objects.Player;
import net.risingworld.api.ui.UILabel;
import net.risingworld.api.ui.UIScrollView;
import net.risingworld.api.ui.UITextField;
import net.risingworld.api.ui.UIScrollView.ScrollViewMode;
import net.risingworld.api.ui.style.Align;
import net.risingworld.api.ui.style.DisplayStyle;
import net.risingworld.api.ui.style.FlexDirection;
import net.risingworld.api.ui.style.Font;
import net.risingworld.api.ui.style.Justify;
import net.risingworld.api.ui.style.Pivot;
import net.risingworld.api.ui.style.Position;
import net.risingworld.api.ui.style.TextAnchor;
import net.risingworld.api.ui.style.Unit;
import net.risingworld.api.ui.style.Wrap;

public class AdminPluginSettingsPanel extends OZUIElement {
    private static final Set<Integer> INTEGER_INPUT_IDS = new HashSet<>();

    private final Player uiPlayer;
    private final PlayerPluginAdminSettings adminSettings;
    private final Runnable refreshOverlay;

    private UIScrollView settingsContainer;
    private OZUIElement flexWrapper;

    private static I18n t() {
        return I18n.getInstance(OZTools.name);
    }

    private I18n pluginI18n() {
        return I18n.getInstance(adminSettings.pluginLabel);
    }

    public AdminPluginSettingsPanel(
            Player uiPlayer,
            PlayerPluginAdminSettings adminSettings,
            Runnable refreshOverlay) {
        this.uiPlayer = uiPlayer;
        this.adminSettings = adminSettings;
        this.refreshOverlay = refreshOverlay;
        setPivot(Pivot.UpperLeft);
        setSize(100, 100, true);
    }

    public void updateUI() {
        removeAllChilds();

        UILabel titleLabel = new UILabel(
                t().get("TC_PLUGIN_SETTINGS_TITLE", uiPlayer).replace("PH_PLUGIN_NAME", adminSettings.pluginLabel));
        titleLabel.setPivot(Pivot.UpperLeft);
        titleLabel.style.left.set(5, Unit.Percent);
        titleLabel.style.top.set(0, Unit.Pixel);
        titleLabel.style.width.set(66, Unit.Percent);
        titleLabel.style.height.set(32, Unit.Pixel);
        titleLabel.setFont(Font.DefaultBold);
        titleLabel.setFontSize(22);
        titleLabel.setFontColor(0xF4F0E6FF);
        titleLabel.setTextAlign(TextAnchor.MiddleLeft);
        addChild(titleLabel);

        if (adminSettings.canReload()) {
            OZUIElement reloadButton = actionButton(t().get("TC_PLUGIN_SETTINGS_RELOAD", uiPlayer));
            reloadButton.setPivot(Pivot.UpperLeft);
            reloadButton.style.position.set(Position.Absolute);
            reloadButton.style.right.set(5, Unit.Percent);
            reloadButton.style.top.set(0, Unit.Pixel);
            reloadButton.setClickAction(event -> {
                adminSettings.reload();
                refreshOverlay.run();
            });
            addChild(reloadButton);
        }

        UILabel descLabel = new UILabel(t().get("TC_PLUGIN_SETTINGS_DESC", uiPlayer));
        descLabel.setPivot(Pivot.UpperLeft);
        descLabel.style.left.set(5, Unit.Percent);
        descLabel.style.top.set(12, Unit.Pixel);
        descLabel.style.width.set(90, Unit.Percent);
        descLabel.style.height.set(24, Unit.Pixel);
        descLabel.setFont(Font.Default);
        descLabel.setFontSize(14);
        descLabel.setFontColor(0xC8C0B2FF);
        descLabel.setTextAlign(TextAnchor.MiddleLeft);
        addChild(descLabel);

        UIScrollView content = createDataContent();
        content.setPivot(Pivot.UpperLeft);
        content.style.left.set(4, Unit.Percent);
        content.style.top.set(90, Unit.Pixel);
        content.setSize(92, 78, true);
        content.style.borderTopWidth.set(1);
        content.style.borderTopColor.set(0x6A5228FF);
        addChild(content);
    }

    protected UIScrollView createDataContent() {
        settingsContainer = new UIScrollView(ScrollViewMode.Vertical);
        settingsContainer.style.position.set(Position.Absolute);
        settingsContainer.setSize(100, 100, true);
        settingsContainer.style.paddingBottom.set(12);
        settingsContainer.style.paddingTop.set(12);
        settingsContainer.style.paddingLeft.set(12);
        settingsContainer.style.paddingRight.set(12);
        flexWrapper = new OZUIElement();
        flexWrapper.setPosition(0, 0, false);
        flexWrapper.style.width.set(100, Unit.Percent);
        flexWrapper.style.height.set(100, Unit.Percent);
        flexWrapper.style.display.set(DisplayStyle.Flex);
        flexWrapper.style.alignContent.set(Align.FlexStart);
        flexWrapper.style.justifyContent.set(Justify.FlexStart);
        flexWrapper.style.flexDirection.set(FlexDirection.Row);
        flexWrapper.style.flexWrap.set(Wrap.Wrap);
        settingsContainer.addChild(flexWrapper);
        redrawRows();

        return settingsContainer;
    }

    private void redrawRows() {
        flexWrapper.removeAllChilds();
        List<AdminSettingsEntry> entries = adminSettings.getProvider().entries();
        if (entries == null || entries.isEmpty()) {
            flexWrapper.addChild(messageLabel(t().get("TC_PLUGIN_SETTINGS_EMPTY", uiPlayer)));
            return;
        }
        for (AdminSettingsEntry entry : entries) {
            flexWrapper.addChild(entry.isGroup() ? groupRow(entry) : row(entry));
        }
    }

    private OZUIElement groupRow(AdminSettingsEntry entry) {
        OZUIElement row = new OZUIElement();
        row.setPivot(Pivot.UpperLeft);
        row.style.width.set(100, Unit.Percent);
        row.style.height.set(48, Unit.Pixel);
        row.style.marginTop.set(8);
        row.style.marginBottom.set(8);
        row.setBackgroundColor(0x0F0E0ACC);
        row.style.borderBottomWidth.set(1);
        row.style.borderBottomColor.set(0xD7AE55AA);

        UILabel label = new UILabel(settingText(entry, "LABEL", entry.getLabel()));
        label.setPivot(Pivot.UpperLeft);
        label.style.left.set(8, Unit.Pixel);
        label.style.top.set(4, Unit.Pixel);
        label.style.width.set(92, Unit.Percent);
        label.style.height.set(22, Unit.Pixel);
        label.setFont(Font.DefaultBold);
        label.setFontSize(15);
        label.setFontColor(0xF2C766FF);
        label.setTextAlign(TextAnchor.MiddleLeft);
        label.setTextWrap(false);
        row.addChild(label);

        String descriptionText = settingText(entry, "DESC", entry.getDescription());
        if (!descriptionText.isBlank()) {
            UILabel description = new UILabel(descriptionText);
            description.style.position.set(Position.Absolute);
            description.setPivot(Pivot.UpperLeft);
            description.style.left.set(8, Unit.Pixel);
            description.style.top.set(25, Unit.Pixel);
            description.style.width.set(92, Unit.Percent);
            description.style.height.set(18, Unit.Pixel);
            description.setFontSize(12);
            description.setFontColor(0xC8C0B2FF);
            description.setTextAlign(TextAnchor.MiddleLeft);
            description.setTextWrap(false);
            row.addChild(description);
        }

        return row;
    }

    private OZUIElement row(AdminSettingsEntry entry) {
        OZUIElement row = new OZUIElement();
        row.setPivot(Pivot.UpperLeft);
        row.style.width.set(100, Unit.Percent);
        row.style.height.set(rowHeight(entry), Unit.Pixel);
        row.style.marginBottom.set(8);
        row.setBackgroundColor(0x181713D8);
        row.setBorder(1);
        row.setBorderColor(0x7A5D2AFF);

        UILabel key = new UILabel(settingText(entry, "LABEL", entry.getLabel()));
        key.setPivot(Pivot.UpperLeft);
        key.style.left.set(10, Unit.Pixel);
        key.style.top.set(8, Unit.Pixel);
        key.style.width.set(27, Unit.Percent);
        key.style.height.set(24, Unit.Pixel);
        key.setFont(Font.DefaultBold);
        key.setFontSize(13);
        key.setFontColor(0xF4F0E6FF);
        key.setTextAlign(TextAnchor.MiddleLeft);
        key.setTextWrap(false);
        row.addChild(key);

        UILabel description = new UILabel(rowDescription(entry));
        description.setPivot(Pivot.UpperLeft);
        description.style.left.set(10, Unit.Pixel);
        description.style.top.set(10, Unit.Pixel);
        description.style.width.set(40, Unit.Percent);
        description.style.height.set(32, Unit.Pixel);
        description.setFontSize(12);
        description.setFontColor(0xC8C0B2FF);
        description.setTextAlign(TextAnchor.UpperLeft);
        description.setTextWrap(true);
        row.addChild(description);

        if (entry.isSensitive()) {
            row.addChild(readOnlyValue(t().get("TC_PLUGIN_SETTINGS_HIDDEN", uiPlayer)));
            return row;
        }

        if (!entry.isEditable()) {
            row.addChild(readOnlyValue(entry.getValue()));
            return row;
        }

        if (entry.getType() == AdminSettingsType.BOOLEAN) {
            row.addChild(booleanSwitch(entry));
            return row;
        }

        if (entry.getType() == AdminSettingsType.SELECT) {
            row.addChild(selectDropdown(entry));
            return row;
        }

        UITextField input = input(entry);
        row.addChild(input);
        OZUIElement saveButton = actionButton(t().get("TC_PLUGIN_SETTINGS_SAVE", uiPlayer));
        saveButton.setPivot(Pivot.MiddleRight);
        saveButton.setAbsolute();
        saveButton.style.right.set(1, Unit.Pixel);
        saveButton.style.top.set(50, Unit.Percent);
        saveButton.setClickAction(event -> input.getCurrentText(uiPlayer, value -> {
            if (!isValidValue(entry, value)) {
                uiPlayer.sendTextMessage(t().get("TC_PLUGIN_SETTINGS_INVALID", uiPlayer)
                        .replace("PH_SETTING_KEY", entry.getKey()));
                return;
            }
            if (entry.write(writeValue(entry, value))) {
                adminSettings.reload();
                uiPlayer.sendTextMessage(t().get("TC_PLUGIN_SETTINGS_SAVED", uiPlayer)
                        .replace("PH_SETTING_KEY", entry.getKey()));
            } else {
                uiPlayer.sendTextMessage(t().get("TC_PLUGIN_SETTINGS_SAVE_FAILED", uiPlayer)
                        .replace("PH_SETTING_KEY", entry.getKey()));
            }
        }));
        row.addChild(saveButton);

        return row;
    }

    private int rowHeight(AdminSettingsEntry entry) {
        if (entry.getType() == AdminSettingsType.SELECT) {
            return 224;
        }
        if (entry.getType() == AdminSettingsType.TEXT) {
            return 154;
        }
        return 74;
    }

    private Dropdown selectDropdown(AdminSettingsEntry entry) {
        List<DropdownOption> options = entry.getOptions().stream()
                .map(option -> new DropdownOption(option, option))
                .collect(Collectors.toList());
        Dropdown dropdown = new Dropdown(options, entry.getValue(), selected -> {
            if (!isValidValue(entry, selected)) {
                uiPlayer.sendTextMessage(t().get("TC_PLUGIN_SETTINGS_INVALID", uiPlayer)
                        .replace("PH_SETTING_KEY", entry.getKey()));
                return;
            }
            if (entry.write(writeValue(entry, selected))) {
                adminSettings.reload();
                uiPlayer.sendTextMessage(t().get("TC_PLUGIN_SETTINGS_SAVED", uiPlayer)
                        .replace("PH_SETTING_KEY", entry.getKey()));
            } else {
                uiPlayer.sendTextMessage(t().get("TC_PLUGIN_SETTINGS_SAVE_FAILED", uiPlayer)
                        .replace("PH_SETTING_KEY", entry.getKey()));
            }
        });
        dropdown.setPivot(Pivot.MiddleRight);
        dropdown.style.position.set(Position.Absolute);
        dropdown.style.right.set(1, Unit.Pixel);
        dropdown.style.top.set(16, Unit.Pixel);
        dropdown.style.width.set(25, Unit.Percent);
        dropdown.style.height.set(34, Unit.Pixel);
        return dropdown;
    }

    private SwitchButton booleanSwitch(AdminSettingsEntry entry) {
        boolean currentValue = Boolean.parseBoolean(entry.getValue());
        SwitchButton toggle = new SwitchButton(currentValue, newValue -> {
            if (entry.write(Boolean.toString(newValue))) {
                adminSettings.reload();
                uiPlayer.sendTextMessage(t().get("TC_PLUGIN_SETTINGS_SAVED", uiPlayer)
                        .replace("PH_SETTING_KEY", entry.getKey()));
            } else {
                uiPlayer.sendTextMessage(t().get("TC_PLUGIN_SETTINGS_SAVE_FAILED", uiPlayer)
                        .replace("PH_SETTING_KEY", entry.getKey()));
            }
        });
        toggle.setPivot(Pivot.MiddleRight);
        toggle.style.position.set(Position.Absolute);
        toggle.style.right.set(18, Unit.Pixel);
        toggle.style.top.set(50, Unit.Percent);
        return toggle;
    }

    private UITextField input(AdminSettingsEntry entry) {
        UITextField input = new UITextField(entry.getValue());
        input.setPivot(Pivot.MiddleRight);
        input.style.position.set(Position.Absolute);
        input.style.right.set(50, Unit.Pixel);
        input.style.top.set(50, Unit.Percent);
        input.style.width.set(25, Unit.Percent);
        input.style.height.set(entry.getType() == AdminSettingsType.TEXT ? 104 : 34, Unit.Pixel);
        input.setFontSize(13);
        input.setFontColor(0xF4F0E6FF);
        input.setMaxCharacters(entry.getType() == AdminSettingsType.TEXT ? 4000
                : entry.getType() == AdminSettingsType.STRING ? 160 : 24);
        input.setMultiLine(entry.getType() == AdminSettingsType.TEXT);
        input.setBackgroundColor(0x10100EE8);
        input.setBorder(1);
        input.setBorderColor(0x5E4A25FF);
        if (entry.getType() == AdminSettingsType.INTEGER) {
            registerIntegerInput(input);
        }
        return input;
    }

    public static void handleTextFieldChange(PlayerUITextFieldChangeEvent event) {
        if (event == null || event.getUITextField() == null) {
            return;
        }
        synchronized (INTEGER_INPUT_IDS) {
            if (!INTEGER_INPUT_IDS.contains(event.getUITextField().getID())) {
                return;
            }
        }

        String sanitizedText = integerInputText(event.getNewText());
        if (!sanitizedText.equals(nullSafe(event.getNewText()))) {
            event.getUITextField().setText(sanitizedText);
        }
    }

    private static void registerIntegerInput(UITextField input) {
        synchronized (INTEGER_INPUT_IDS) {
            INTEGER_INPUT_IDS.add(input.getID());
        }
    }

    private UILabel readOnlyValue(String text) {
        UILabel value = new UILabel(text == null ? "" : text);
        value.setPivot(Pivot.MiddleRight);
        value.style.position.set(Position.Absolute);
        value.style.right.set(1, Unit.Pixel);
        value.style.top.set(50, Unit.Percent);
        value.style.width.set(25, Unit.Percent);
        value.style.height.set(34, Unit.Pixel);
        value.setFontSize(13);
        value.setFontColor(0xD8D0C0FF);
        value.setTextAlign(TextAnchor.MiddleLeft);
        value.setTextWrap(false);
        return value;
    }

    private OZUIElement actionButton(String label) {
        OZUIElement button = new OZUIElement();
        button.setSize(92, 32, false);
        button.setClickable(true);
        button.setBackgroundColor(0x3A2D18D8);
        button.setHoverBackgroundColor(0x2A2419E8);
        button.setBorder(1);
        button.setBorderColor(0xD7AE55FF);
        button.setHoverBorderColor(0xF2C766FF);

        UILabel buttonLabel = new UILabel(label);
        buttonLabel.setPivot(Pivot.MiddleCenter);
        buttonLabel.setPosition(50, 50, true);
        buttonLabel.setSize(100, 100, true);
        buttonLabel.setFontSize(13);
        buttonLabel.setFontColor(0xF4F0E6FF);
        buttonLabel.setTextAlign(TextAnchor.MiddleCenter);
        button.addChild(buttonLabel);
        return button;
    }

    private UILabel messageLabel(String text) {
        UILabel label = new UILabel(text);
        label.setPivot(Pivot.UpperLeft);
        label.style.width.set(90, Unit.Percent);
        label.style.height.set(40, Unit.Pixel);
        label.setFontSize(15);
        label.setFontColor(0xC8C0B2FF);
        label.setTextAlign(TextAnchor.MiddleLeft);
        label.setTextWrap(true);
        return label;
    }

    private String rowDescription(AdminSettingsEntry entry) {
        String description = settingText(entry, "DESC", entry.getDescription());
        if (entry.getDefaultValue().isBlank()) {
            return description;
        }
        String defaultText = t().get("TC_PLUGIN_SETTINGS_DEFAULT", uiPlayer)
                .replace("PH_DEFAULT_VALUE", entry.getDefaultValue());
        if (description.isBlank()) {
            return defaultText;
        }
        return description + " " + defaultText;
    }

    private String pluginText(String textOrKey) {
        if (textOrKey == null || textOrKey.isBlank()) {
            return "";
        }
        return pluginI18n().get(textOrKey, uiPlayer.getSystemLanguage());
    }

    private String settingText(AdminSettingsEntry entry, String suffix, String fallback) {
        String generatedKey = "TC_SETTING_" + normalizeSettingKey(entry.getKey()) + "_" + suffix;
        String generatedText = pluginText(generatedKey);
        if (!generatedKey.equals(generatedText)) {
            return generatedText;
        }
        String explicitText = pluginText(fallback);
        return fallback == null || fallback.equals(explicitText) ? (fallback == null ? "" : fallback) : explicitText;
    }

    private String normalizeSettingKey(String key) {
        return key == null ? "" : key.trim().replaceAll("[^A-Za-z0-9]+", "_").toUpperCase();
    }

    private boolean isValidValue(AdminSettingsEntry entry, String value) {
        if (value == null) {
            return false;
        }
        if (entry.getType() == AdminSettingsType.BOOLEAN) {
            return "true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value);
        }
        if (entry.getType() == AdminSettingsType.INTEGER) {
            try {
                Integer.parseInt(value);
                return true;
            } catch (NumberFormatException ex) {
                return false;
            }
        }
        if (entry.getType() == AdminSettingsType.SELECT) {
            return entry.getOptions().contains(value);
        }
        return true;
    }

    private String writeValue(AdminSettingsEntry entry, String value) {
        if (entry.getType() != AdminSettingsType.TEXT) {
            return value;
        }
        return value.replace("\r\n", "\n").replace("\r", "\n").replace("\n", "\\n");
    }

    private static String integerInputText(String value) {
        String text = nullSafe(value);
        StringBuilder sanitized = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char character = text.charAt(i);
            if (Character.isDigit(character)) {
                sanitized.append(character);
            } else if (character == '-' && i == 0) {
                sanitized.append(character);
            }
        }
        return sanitized.toString();
    }

    private static String nullSafe(String value) {
        return value == null ? "" : value;
    }
}
