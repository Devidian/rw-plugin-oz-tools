package de.omegazirkel.risingworld.tools.ui.table;

import java.util.List;

import de.omegazirkel.risingworld.tools.ui.OZUIElement;
import net.risingworld.api.ui.UIScrollView;
import net.risingworld.api.ui.UIScrollView.ScrollViewMode;
import net.risingworld.api.ui.style.Pivot;
import net.risingworld.api.ui.style.Unit;

public class TableScrollView extends OZUIElement {

    private static final float HEADER_HEIGHT = 36;

    private OZUIElement headerContainer;
    private UIScrollView scrollView;
    private OZUIElement content;

    private float rowHeight = 30; // default
    private float rowGap = 2; // default

    public TableScrollView(List<String> headers, List<Float> widths) {
        buildUI(headers, widths);
    }

    private void buildUI(List<String> headers, List<Float> widths) {

        this.setSize(100, 100, true);
        this.setPivot(Pivot.UpperLeft);

        // Header
        headerContainer = new OZUIElement();
        headerContainer.style.width.set(100, Unit.Percent);
        headerContainer.style.height.set(HEADER_HEIGHT, Unit.Pixel);
        headerContainer.setPivot(Pivot.UpperLeft);

        TableHeaderRow headerRow = new TableHeaderRow(headers, widths);
        headerRow.setPivot(Pivot.UpperLeft);
        headerContainer.addChild(headerRow);

        this.addChild(headerContainer);

        // ScrollView
        scrollView = new UIScrollView(ScrollViewMode.Vertical);
        scrollView.setPivot(Pivot.UpperLeft);
        scrollView.setPosition(0, HEADER_HEIGHT, false);
        scrollView.setSize(100, 100, true);

        // Inner content where rows go
        content = new OZUIElement();
        content.setPivot(Pivot.UpperLeft);
        content.setSize(100, 0, true);
        content.setPosition(0, 0, false);

        scrollView.addChild(content);
        this.addChild(scrollView);
        scrollView.getHorizontalPageSize();
    }

    public void addRow(TableRow row) {
        float height = (Math.max(row.style.height.get(), rowHeight) + rowGap);
        float y = rowGap + content.getChildCount() * height;

        row.setPosition(0f, y, false);
        content.addChild(row);

        // Content height grows automatically
        content.style.height.set((content.getChildCount() * (rowHeight + rowGap)), Unit.Pixel);
    }

    public OZUIElement getRoot() {
        return this;
    }

    public void setScrollBodyHeight(float heightPixels) {
        scrollView.style.height.set(heightPixels, Unit.Pixel);
        style.height.set(HEADER_HEIGHT + heightPixels, Unit.Pixel);
    }
}
