package de.omegazirkel.risingworld.tools.ui;

import de.omegazirkel.risingworld.OZTools;
import de.omegazirkel.risingworld.tools.I18n;
import net.risingworld.api.callbacks.Callback;
import net.risingworld.api.events.player.ui.PlayerUIElementClickEvent;
import net.risingworld.api.objects.Player;
import net.risingworld.api.ui.UIElement;
import net.risingworld.api.ui.UILabel;
import net.risingworld.api.ui.UIScrollView;
import net.risingworld.api.ui.UIScrollView.ScrollViewMode;
import net.risingworld.api.ui.style.Align;
import net.risingworld.api.ui.style.DisplayStyle;
import net.risingworld.api.ui.style.FlexDirection;
import net.risingworld.api.ui.style.Font;
import net.risingworld.api.ui.style.Justify;
import net.risingworld.api.ui.style.Pivot;
import net.risingworld.api.ui.style.TextAnchor;
import net.risingworld.api.ui.style.Unit;
import net.risingworld.api.ui.style.Wrap;

public abstract class BasePlayerPluginSettingsPanel extends OZUIElement {

    private static I18n t() {
        return I18n.getInstance(OZTools.name);
    }

    private String title;
    private Player uiPlayer;
    protected UIScrollView settingsContainer;
    protected OZUIElement flexWrapper;

    public BasePlayerPluginSettingsPanel(Player uiPlayer, String title) {
        this.uiPlayer = uiPlayer;
        this.title = title;
        setPivot(Pivot.UpperLeft);
        setSize(100, 100, true);
    }

    public void updateUI() {
        // clear all childs
        this.removeAllChilds();

        // add title label
        UILabel titleLabel = new UILabel(t().get("TC_SETTINGS_TITLE", uiPlayer).replace("PH_PLUGIN_NAME", title));
        titleLabel.setPivot(Pivot.UpperLeft);
        titleLabel.style.left.set(5, Unit.Percent);
        titleLabel.style.top.set(18, Unit.Pixel);
        titleLabel.style.width.set(90, Unit.Percent);
        titleLabel.style.height.set(32, Unit.Pixel);
        titleLabel.setFont(Font.DefaultBold);
        titleLabel.setFontSize(22);
        titleLabel.setFontColor(0xF4F0E6FF);
        titleLabel.setTextAlign(TextAnchor.MiddleLeft);
        this.addChild(titleLabel);
        // add description label
        UILabel descLabel = new UILabel(t().get("TC_SETTINGS_DESC", uiPlayer).replace("PH_PLUGIN_NAME", title));
        descLabel.setPivot(Pivot.UpperLeft);
        descLabel.style.left.set(5, Unit.Percent);
        descLabel.style.top.set(54, Unit.Pixel);
        descLabel.style.width.set(90, Unit.Percent);
        descLabel.style.height.set(24, Unit.Pixel);
        descLabel.setFont(Font.Default);
        descLabel.setFontSize(14);
        descLabel.setFontColor(0xC8C0B2FF);
        descLabel.setTextAlign(TextAnchor.MiddleLeft);
        this.addChild(descLabel);
        // add content
        UIScrollView content = createSettingsContent();
        content.setPivot(Pivot.UpperLeft);
        content.style.left.set(4, Unit.Percent);
        content.style.top.set(90, Unit.Pixel);
        content.setSize(92, 78, true);
        content.style.borderTopWidth.set(1);
        content.style.borderTopColor.set(0x6A5228FF);
        this.addChild(content);

    }

    protected OZUIElement defaultSettingsContainer() {
        OZUIElement element = new OZUIElement();
        element.setPivot(Pivot.UpperLeft);
        element.style.width.set(270, Unit.Pixel);
        element.style.minWidth.set(240, Unit.Pixel);
        element.style.height.set(96, Unit.Pixel);
        element.style.display.set(DisplayStyle.Flex);
        element.setBackgroundColor(0x181713D8);
        element.setMargin(8);
        element.setPadding(10);
        element.setBorder(1);
        element.setBorderColor(0x7A5D2AFF);
        return element;
    }

    protected UILabel defaultSettingsLabel(String labelText) {
        UILabel label = new UILabel(labelText);
        label.setPivot(Pivot.UpperLeft);
        label.setPosition(10, 8, false);
        label.setSize(250, 48, false);
        label.setFontSize(13);
        label.setFont(Font.DefaultBold);
        label.setFontColor(0xF4F0E6FF);
        label.setTextAlign(TextAnchor.UpperLeft);
        label.setTextWrap(true);
        return label;
    }

    protected UIElement switchButtons(Player uiPlayer, Boolean isEnabled,
            Callback<PlayerUIElementClickEvent> onSwitch) {
        return switchButtons(uiPlayer, isEnabled, onSwitch, t().get("TC_UI_BTN_OFF", uiPlayer),
                t().get("TC_UI_BTN_ON", uiPlayer));
    }

    protected UIElement switchButtons(Player uiPlayer, Boolean isEnabled,
            Callback<PlayerUIElementClickEvent> onSwitch, String labelA, String labelB) {

        // toggle button container
        UIElement buttonContainer = new UIElement();
        buttonContainer.setPivot(Pivot.LowerLeft);
        buttonContainer.setPosition(0, 100, true);
        buttonContainer.style.width.set(100, Unit.Percent);
        buttonContainer.style.height.set(38, Unit.Pixel);
        buttonContainer.style.paddingLeft.set(10);
        buttonContainer.style.paddingRight.set(10);
        buttonContainer.style.paddingTop.set(4);
        buttonContainer.style.paddingBottom.set(6);
        buttonContainer.style.borderTopColor.set(0x5E4A25FF);
        buttonContainer.style.borderTopWidth.set(1);
        buttonContainer.style.display.set(DisplayStyle.Flex);
        buttonContainer.style.flexDirection.set(FlexDirection.Row);
        buttonContainer.style.alignContent.set(Align.Center);
        buttonContainer.style.justifyContent.set(Justify.Center);
        // buttons
        OZUIElement buttonA = new OZUIElement(); // off
        OZUIElement buttonB = new OZUIElement(); // on
        UILabel buttonALabel = new UILabel(labelA);
        UILabel buttonBLabel = new UILabel(labelB);
        buttonA.setSize(82, 28, false);
        buttonB.setSize(82, 28, false);
        buttonA.setMargin(2);
        buttonB.setMargin(2);
        buttonA.setClickable(true);
        buttonB.setClickable(true);
        buttonA.setClickAction(onSwitch);
        buttonB.setClickAction(onSwitch);
        buttonA.style.borderBottomWidth.set(1);
        buttonA.style.borderTopWidth.set(1);
        buttonA.style.borderLeftWidth.set(1);
        buttonA.style.borderRightWidth.set(1);
        buttonB.style.borderBottomWidth.set(1);
        buttonB.style.borderTopWidth.set(1);
        buttonB.style.borderRightWidth.set(1);
        buttonB.style.borderLeftWidth.set(1);
        buttonA.setBorderColor(0x7A5D2AFF);
        buttonB.setBorderColor(0x7A5D2AFF);
        buttonA.setHoverBorderColor(0xD7AE55FF);
        buttonB.setHoverBorderColor(0xD7AE55FF);
        if (isEnabled) {
            buttonA.setBackgroundColor(0x2C1717FF);
            buttonA.setHoverBackgroundColor(0x482020FF);
            buttonALabel.setFontColor(0xD8D0C0FF);
            buttonB.setBackgroundColor(0x1D4D2AFF);
            buttonB.setHoverBackgroundColor(0x286B39FF);
            buttonBLabel.setFontColor(0xF2C766FF);
        } else {
            buttonA.setBackgroundColor(0x5A2424FF);
            buttonA.setHoverBackgroundColor(0x743030FF);
            buttonALabel.setFontColor(0xF2C766FF);
            buttonB.setBackgroundColor(0x182F20FF);
            buttonB.setHoverBackgroundColor(0x244D30FF);
            buttonBLabel.setFontColor(0xD8D0C0FF);
        }
        buttonALabel.setFontSize(13);
        buttonBLabel.setFontSize(13);
        buttonALabel.setTextAlign(TextAnchor.MiddleCenter);
        buttonBLabel.setTextAlign(TextAnchor.MiddleCenter);
        buttonALabel.setPivot(Pivot.MiddleCenter);
        buttonBLabel.setPivot(Pivot.MiddleCenter);
        buttonALabel.setPosition(50, 50, true);
        buttonBLabel.setPosition(50, 50, true);
        buttonA.addChild(buttonALabel);
        buttonB.addChild(buttonBLabel);
        buttonContainer.addChild(buttonA);
        buttonContainer.addChild(buttonB);

        return buttonContainer;
    }

    protected UIScrollView createSettingsContent() {
        settingsContainer = new UIScrollView(ScrollViewMode.Vertical);
        settingsContainer.setSize(100, 100, true);
        settingsContainer.style.paddingBottom.set(12);
        settingsContainer.style.paddingTop.set(12);
        settingsContainer.style.paddingLeft.set(12);
        settingsContainer.style.paddingRight.set(12);
        flexWrapper = new OZUIElement();
        flexWrapper.style.width.set(100, Unit.Percent);
        flexWrapper.style.height.set(100, Unit.Percent);
        flexWrapper.style.display.set(DisplayStyle.Flex);
        flexWrapper.style.alignContent.set(Align.FlexStart);
        flexWrapper.style.justifyContent.set(Justify.FlexStart);
        flexWrapper.style.flexDirection.set(FlexDirection.Row);
        flexWrapper.style.flexWrap.set(Wrap.Wrap);
        settingsContainer.addChild(flexWrapper);
        redrawContent();

        return settingsContainer;
    }

    protected abstract void redrawContent();
}
