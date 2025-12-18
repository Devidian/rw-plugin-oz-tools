package de.omegazirkel.risingworld.tools.ui.table;

import de.omegazirkel.risingworld.tools.ui.OZUIElement;
import net.risingworld.api.ui.UIElement;
import net.risingworld.api.ui.style.Pivot;

public class TableCell extends OZUIElement {

    public final float width;

    public TableCell(UIElement content, float widthPercent) {
        width = widthPercent;
        setSize(widthPercent, 100, true); // width%, height 100%
        setPivot(Pivot.UpperLeft);
        setBackgroundColor(0, 0, 0, 0); // transparent default

        if (content != null) {
            content.setPivot(Pivot.MiddleLeft);
            content.setPosition(2, 50, true); // 2% padding left
            addChild(content);
        }
    }
}
