package de.omegazirkel.risingworld.tools.ui.table;

import java.util.ArrayList;
import java.util.List;

import net.risingworld.api.ui.UILabel;
import net.risingworld.api.ui.style.Font;
import net.risingworld.api.ui.style.Pivot;
import net.risingworld.api.ui.style.Unit;

public class TableHeaderRow extends TableRow {

    public TableHeaderRow(List<String> titles, List<Float> widths) {
        super(buildCells(titles, widths));

        // Header-Styling
        setBackgroundColor(0.15f, 0.15f, 0.15f, 1f);
        setBorder(1);
        setBorderColor(1f, 1f, 1f, 0.25f);
        style.width.set(100, Unit.Percent);
        style.height.set(36, Unit.Pixel);
    }

    private static List<TableCell> buildCells(List<String> titles, List<Float> widths) {
        List<TableCell> cells = new ArrayList<>();

        for (int i = 0; i < titles.size(); i++) {
            UILabel lbl = new UILabel(titles.get(i));
            lbl.setFontSize(17);
            lbl.setFont(Font.DefaultBold);
            lbl.setPivot(Pivot.MiddleLeft);
            lbl.setPosition(2, 50, true);

            TableCell cell = new TableCell(lbl, widths.get(i));
            cell.setBackgroundColor(0, 0, 0, 0); // transparent
            cells.add(cell);
        }
        return cells;
    }
}
