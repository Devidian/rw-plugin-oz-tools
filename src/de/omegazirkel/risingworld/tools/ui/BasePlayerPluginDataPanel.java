package de.omegazirkel.risingworld.tools.ui;

import de.omegazirkel.risingworld.OZTools;
import de.omegazirkel.risingworld.tools.I18n;
import net.risingworld.api.objects.Player;
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

public abstract class BasePlayerPluginDataPanel extends OZUIElement {

    private static I18n t() {
        return I18n.getInstance(OZTools.name);
    }

    private final Player uiPlayer;
    private final String title;
    protected UIScrollView dataContainer;
    protected OZUIElement flexWrapper;

    public BasePlayerPluginDataPanel(Player uiPlayer, String title) {
        this.uiPlayer = uiPlayer;
        this.title = title;
        setPivot(Pivot.UpperLeft);
        setSize(100, 100, true);
    }

    public void updateUI() {
        removeAllChilds();

        UILabel titleLabel = new UILabel(t().get("TC_DATA_TITLE", uiPlayer).replace("PH_PLUGIN_NAME", title));
        titleLabel.setPivot(Pivot.UpperLeft);
        titleLabel.style.left.set(5, Unit.Percent);
        titleLabel.style.top.set(18, Unit.Pixel);
        titleLabel.style.width.set(90, Unit.Percent);
        titleLabel.style.height.set(32, Unit.Pixel);
        titleLabel.setFont(Font.DefaultBold);
        titleLabel.setFontSize(22);
        titleLabel.setFontColor(0xF4F0E6FF);
        titleLabel.setTextAlign(TextAnchor.MiddleLeft);
        addChild(titleLabel);

        UILabel descLabel = new UILabel(t().get("TC_DATA_DESC", uiPlayer).replace("PH_PLUGIN_NAME", title));
        descLabel.setPivot(Pivot.UpperLeft);
        descLabel.style.left.set(5, Unit.Percent);
        descLabel.style.top.set(54, Unit.Pixel);
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
        dataContainer = new UIScrollView(ScrollViewMode.Vertical);
        dataContainer.setSize(100, 100, true);
        dataContainer.style.paddingBottom.set(12);
        dataContainer.style.paddingTop.set(12);
        dataContainer.style.paddingLeft.set(12);
        dataContainer.style.paddingRight.set(12);
        flexWrapper = new OZUIElement();
        flexWrapper.style.width.set(100, Unit.Percent);
        flexWrapper.style.height.set(100, Unit.Percent);
        flexWrapper.style.display.set(DisplayStyle.Flex);
        flexWrapper.style.alignContent.set(Align.FlexStart);
        flexWrapper.style.justifyContent.set(Justify.FlexStart);
        flexWrapper.style.flexDirection.set(FlexDirection.Row);
        flexWrapper.style.flexWrap.set(Wrap.Wrap);
        dataContainer.addChild(flexWrapper);
        redrawContent();

        return dataContainer;
    }

    protected UILabel defaultEmptyStateLabel() {
        UILabel placeholderLabel = new UILabel(t().get("TC_DATA_EMPTY", uiPlayer));
        placeholderLabel.setPivot(Pivot.UpperLeft);
        placeholderLabel.style.width.set(100, Unit.Percent);
        placeholderLabel.style.height.set(48, Unit.Pixel);
        placeholderLabel.setFont(Font.DefaultBold);
        placeholderLabel.setFontSize(15);
        placeholderLabel.setFontColor(0xC8C0B2FF);
        placeholderLabel.setTextAlign(TextAnchor.MiddleLeft);
        placeholderLabel.setTextWrap(true);
        return placeholderLabel;
    }

    protected abstract void redrawContent();
}
