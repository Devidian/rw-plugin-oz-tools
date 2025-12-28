package de.omegazirkel.risingworld.tools.ui;

import net.risingworld.api.callbacks.Callback;
import net.risingworld.api.events.player.ui.PlayerUIElementClickEvent;
import net.risingworld.api.ui.UIElement;

/**
 * extends UIElement with some helper methods missing from UIElement
 */
public class OZUIElement extends UIElement {
    protected Callback<PlayerUIElementClickEvent> clickAction;

    public void setHoverBorderColor(int rgba) {
        hoverStyle.borderBottomColor.set(rgba);
        hoverStyle.borderLeftColor.set(rgba);
        hoverStyle.borderRightColor.set(rgba);
        hoverStyle.borderTopColor.set(rgba);
    }

    public void setHoverBorderWidth(float thickness) {
        hoverStyle.borderBottomWidth.set(thickness);
        hoverStyle.borderLeftWidth.set(thickness);
        hoverStyle.borderRightWidth.set(thickness);
        hoverStyle.borderTopWidth.set(thickness);
    }

    public void setMargin(float margin) {
        style.marginBottom.set(margin);
        style.marginLeft.set(margin);
        style.marginRight.set(margin);
        style.marginTop.set(margin);
    }

    public void setPadding(float padding) {
        style.paddingBottom.set(padding);
        style.paddingLeft.set(padding);
        style.paddingRight.set(padding);
        style.paddingTop.set(padding);
    }

    public void setHoverBackgroundColor(int rgba) {
        hoverStyle.backgroundColor.set(rgba);
    }

    public void setClickAction(Callback<PlayerUIElementClickEvent> clickAction) {
        this.clickAction = clickAction;
    }

    @Override
    public void onClick(PlayerUIElementClickEvent event) {
        if (clickAction != null) {
            clickAction.onCall(event);
        } else {
            super.onClick(event);
        }
    }
}
