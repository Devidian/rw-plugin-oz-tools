package de.omegazirkel.risingworld.tools.ui;

import de.omegazirkel.risingworld.tools.I18n;
import net.risingworld.api.callbacks.Callback;
import net.risingworld.api.objects.Player;
import net.risingworld.api.ui.UILabel;
import net.risingworld.api.ui.style.Font;
import net.risingworld.api.ui.style.Pivot;
import net.risingworld.api.ui.style.Position;
import net.risingworld.api.ui.style.TextAnchor;
import net.risingworld.api.ui.style.Unit;

public abstract class BasePluginOverlay extends OverlayBackPanel {
    private static final float PANEL_WIDTH_PERCENT = 88f;
    private static final float PANEL_HEIGHT_PIXELS = 620f;
    private static final float BODY_HEIGHT_PIXELS = 438f;

    protected static String titleLabelKey = "TC_PLACEHOLDER_TITLE";
    protected static String descLabelKey = "TC_PLACEHOLDER_DESC";
    protected static String legendLabelKey = "TC_PLACEHOLDER_LEGEND";

    protected abstract I18n t();

    protected String titleText() {
        return t().get(titleLabelKey, uiPlayer);
    }

    protected String descriptionText() {
        return t().get(descLabelKey, uiPlayer);
    }

    protected String legendText() {
        return t().get(legendLabelKey, uiPlayer);
    }

    protected OZUIElement panel;
    protected OZUIElement body;
    
    protected Callback<Player> onClose;

    public BasePluginOverlay(Player player, Callback<Player> onClose){
        super(player);
        this.onClose = onClose;
        setClickable(false);
    }

    protected void rebuild() {
        removeAllChilds();
        panel = new OZUIElement();
        panel.setPivot(Pivot.MiddleCenter);
        panel.setPosition(50f, 50f, true);
        panel.style.width.set(PANEL_WIDTH_PERCENT, Unit.Percent);
        panel.style.height.set(PANEL_HEIGHT_PIXELS, Unit.Pixel);
        panel.setBackgroundColor(0, 0, 0, 0.86f);
        panel.setBorderColor(0.95f, 0.75f, 0.25f, 0.6f);
        panel.setBorder(1);
        panel.setBorderEdgeRadius(6, false);
        addChild(panel);

        setupHeader();
        setupBodyContainer();
        setupFooter();
    }

    private void setupHeader() {
        UILabel title = new UILabel(titleText());
        title.setPivot(Pivot.UpperLeft);
        title.setPosition(24, 18, false);
        title.setFont(Font.DefaultBold);
        title.setFontSize(24);
        panel.addChild(title);

        UILabel subtitle = new UILabel(descriptionText());
        subtitle.setPivot(Pivot.UpperLeft);
        subtitle.setPosition(24, 52, false);
        subtitle.setFont(Font.Default);
        subtitle.setFontSize(12);
        panel.addChild(subtitle);

        OZUIElement closeButton = new OZUIElement();
        closeButton.setPivot(Pivot.UpperRight);
        closeButton.style.position.set(Position.Absolute);
        closeButton.style.right.set(0, Unit.Pixel);
        closeButton.style.top.set(20, Unit.Pixel);
        closeButton.setSize(34, 34, false);
        closeButton.setBorder(1);
        closeButton.setBorderColor(0.95f, 0.75f, 0.25f, 0.54f);
        closeButton.setBorderEdgeRadius(4, false);
        closeButton.setBackgroundColor(0.12f, 0.10f, 0.08f, 0.9f);
        closeButton.setHoverBackgroundColor(0x611F1AF2);
        closeButton.setClickable(true);
        closeButton.setClickAction(event -> close());
        UILabel closeLabel = new UILabel("X");
        closeLabel.setPivot(Pivot.MiddleCenter);
        closeLabel.setPosition(50, 50, true);
        closeLabel.setSize(100, 100, true);
        closeLabel.setFont(Font.DefaultBold);
        closeLabel.setFontSize(18);
        closeLabel.setTextAlign(TextAnchor.MiddleCenter);
        closeButton.addChild(closeLabel);
        panel.addChild(closeButton);
    }

    private void setupBodyContainer() {
        body = new OZUIElement();
        body.setPivot(Pivot.UpperLeft);
        body.setPosition(24, 124, false);
        body.style.width.set(96, Unit.Percent);
        body.style.height.set(BODY_HEIGHT_PIXELS, Unit.Pixel);
        body.setBackgroundColor(0.08f, 0.08f, 0.08f, 0.55f);
        body.setBorder(1);
        body.setBorderColor(0.95f, 0.75f, 0.25f, 0.48f);
        body.setBorderEdgeRadius(4, false);
        panel.addChild(body);
    }

    private void setupFooter() {
        UILabel legend = new UILabel(legendText());
        legend.setPivot(Pivot.LowerLeft);
        legend.setPosition(24, PANEL_HEIGHT_PIXELS - 18, false);
        legend.setFontSize(12);
        panel.addChild(legend);
    }

    protected void close() {
        uiPlayer.removeUIElement(this);
        CursorManager.hide(uiPlayer);
        onClose.onCall(uiPlayer);
    }
}
