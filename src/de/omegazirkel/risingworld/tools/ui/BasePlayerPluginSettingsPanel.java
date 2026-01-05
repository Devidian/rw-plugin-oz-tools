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
        titleLabel.setPivot(Pivot.UpperCenter);
        titleLabel.setPosition(50, 5, true);
        titleLabel.setFont(Font.DefaultBold);
        titleLabel.setFontSize(24);
        this.addChild(titleLabel);
        // add description label
        UILabel descLabel = new UILabel(t().get("TC_SETTINGS_DESC", uiPlayer).replace("PH_PLUGIN_NAME", title));
        descLabel.setPivot(Pivot.UpperCenter);
        descLabel.setPosition(50, 10, true);
        descLabel.setFont(Font.Default);
        descLabel.setFontSize(16);
        this.addChild(descLabel);
        // add content
        UIScrollView content = createSettingsContent();
        content.setPivot(Pivot.UpperLeft);
        content.setPosition(0, 15, true);
        content.setSize(100, 85, true);
        content.style.borderTopWidth.set(1);
        content.style.borderTopColor.set(0.7f, 0.7f, 0.7f, 1.0f);
        this.addChild(content);

    }

    protected OZUIElement defaultSettingsContainer() {
        OZUIElement element = new OZUIElement();
        element.setPivot(Pivot.UpperLeft);
        element.style.width.set(24, Unit.Percent);
        element.style.height.set(64, Unit.Pixel);
        element.style.display.set(DisplayStyle.Flex);
        element.setBackgroundColor(0.3f, 0.3f, 0.3f, 1.0f);
        element.setMargin(5);
        element.setBorder(1);
        element.setBorderColor(0.7f, 0.7f, 0.7f, 1.0f);
        return element;
    }

    protected UILabel defaultSettingsLabel(String labelText) {
        UILabel label = new UILabel(labelText);
        label.setPivot(Pivot.UpperLeft);
        label.setPosition(5, 5, false);
        label.setFontSize(14);
        label.setFont(Font.DefaultBold);
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
        buttonContainer.style.height.set(32, Unit.Pixel);
        buttonContainer.style.borderTopColor.set(0.7f, 0.7f, 0.7f, 1.0f);
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
        buttonA.setSize(35, 100, true);
        buttonB.setSize(35, 100, true);
        buttonA.setClickable(true);
        buttonB.setClickable(true);
        buttonA.setClickAction(onSwitch);
        buttonB.setClickAction(onSwitch);
        buttonA.style.borderBottomWidth.set(1);
        buttonA.style.borderTopWidth.set(1);
        buttonA.style.borderLeftWidth.set(1);
        buttonB.style.borderBottomWidth.set(1);
        buttonB.style.borderRightWidth.set(1);
        buttonB.style.borderLeftWidth.set(1);
        if (isEnabled) {
            buttonA.setBackgroundColor(0x800000FF);
            buttonA.setHoverBackgroundColor(0xAA0000FF);
            buttonALabel.setFontColor(0x000000FF);
            buttonB.setBackgroundColor(0x00EE00FF);
            buttonBLabel.setFontColor(0xFFFFFFFF);
        } else {
            buttonA.setBackgroundColor(0xEE0000FF);
            buttonALabel.setFontColor(0xFFFFFFFF);
            buttonB.setBackgroundColor(0x008000FF);
            buttonB.setHoverBackgroundColor(0x00AA00FF);
            buttonBLabel.setFontColor(0x000000FF);
        }
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
        settingsContainer.style.paddingBottom.set(10);
        settingsContainer.style.paddingTop.set(10);
        settingsContainer.style.paddingLeft.set(10);
        settingsContainer.style.paddingRight.set(10);
        flexWrapper = new OZUIElement();
        flexWrapper.style.width.set(100, Unit.Percent);
        flexWrapper.style.height.set(100, Unit.Percent);
        flexWrapper.style.display.set(DisplayStyle.Flex);
        flexWrapper.style.alignContent.set(Align.FlexStart);
        flexWrapper.style.justifyContent.set(Justify.SpaceAround);
        flexWrapper.style.flexDirection.set(FlexDirection.Row);
        flexWrapper.style.flexWrap.set(Wrap.Wrap);
        settingsContainer.addChild(flexWrapper);
        redrawContent();

        return settingsContainer;
    }

    protected abstract void redrawContent();
}
