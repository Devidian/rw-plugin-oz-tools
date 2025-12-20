package de.omegazirkel.risingworld.tools.ui;

import java.util.function.Consumer;

import net.risingworld.api.events.player.ui.PlayerUIElementClickEvent;
import net.risingworld.api.ui.UIElement;

/**
 * extends UIElement with some helper methods missing from UIElement
 */
public class OZUIElement extends UIElement {
    protected Consumer<PlayerUIElementClickEvent> clickAction;

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

    public void setHoverBackgroundColor(int rgba) {
        hoverStyle.backgroundColor.set(rgba);
    }

    public void setClickAction(Consumer<PlayerUIElementClickEvent> clickAction) {
        this.clickAction = clickAction;
    }

    @Override
    public void onClick(PlayerUIElementClickEvent event) {
        if (clickAction != null) {
            clickAction.accept(event);
        }
    }
}
