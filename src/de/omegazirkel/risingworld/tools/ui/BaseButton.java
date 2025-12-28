package de.omegazirkel.risingworld.tools.ui;

import net.risingworld.api.callbacks.Callback;
import net.risingworld.api.events.player.ui.PlayerUIElementClickEvent;
import net.risingworld.api.ui.UILabel;
import net.risingworld.api.ui.style.Pivot;
import net.risingworld.api.ui.style.TextAnchor;

public abstract class BaseButton extends OZUIElement {

    private final UILabel label;

    // Colors for style
    protected float[] normalColor = { 0.4f, 0.4f, 0.4f, 1f };
    protected float[] hoverColor = { 0.5f, 0.5f, 0.5f, 1f };
    protected float[] disabledColor = { 0.2f, 0.2f, 0.2f, 0.5f };

    public BaseButton(String text, Callback<PlayerUIElementClickEvent> callback) {
        this.clickAction = callback;

        // Standard Button Setup
        setSize(120, 36, false);
        setPivot(Pivot.MiddleCenter);
        setClickable(true);

        label = new UILabel(text);
        label.setPivot(Pivot.MiddleCenter);
        label.setPosition(50f, 50f, true);
        label.setTextAlign(TextAnchor.MiddleCenter);
        addChild(label);
        setupDefaultStyles();
    }

    protected void setupDefaultStyles() {
        // normal
        setBackgroundColor(0x444444FF);
        setBorder(1);
        setBorderColor(0x00000088);
        // hover
        setHoverBackgroundColor(0x555555FF);
        setHoverBorderColor(0x000000AA);
        setHoverBorderWidth(1);
    }

    public void setText(String text) {
        label.setText(text);
    }
}
