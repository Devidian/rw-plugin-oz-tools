package de.omegazirkel.risingworld.tools.ui.table;

import java.util.List;

import de.omegazirkel.risingworld.tools.ui.OZUIElement;
import net.risingworld.api.ui.style.Pivot;
import net.risingworld.api.ui.style.Unit;

public class TableRow extends OZUIElement {

    public TableRow(List<TableCell> cells) {

        style.height.set(32, Unit.Pixel);
        style.maxHeight.set(32, Unit.Pixel);

        style.width.set(100, Unit.Percent);
        setPivot(Pivot.UpperLeft);

        float x = 0f;

        for (TableCell cell : cells) {
            cell.setPosition(x, 0, true);
            addChild(cell);
            x += cell.width;
        }

        setBorder(1);
        setBorderColor(1f, 1f, 1f, 0.1f);
    }
}
