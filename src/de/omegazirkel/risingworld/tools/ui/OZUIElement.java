package de.omegazirkel.risingworld.tools.ui;

import net.risingworld.api.ui.UIElement;

/**
 * extends UIElement with some helper methods missing from UIElement
 */
public class OZUIElement extends UIElement {
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
}
