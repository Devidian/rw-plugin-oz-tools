package de.omegazirkel.risingworld.tools.ui;

import java.util.List;

import net.risingworld.api.events.player.ui.PlayerUIElementClickEvent;
import net.risingworld.api.ui.UIElement;
import net.risingworld.api.ui.UIScrollView;
import net.risingworld.api.ui.UIScrollView.ScrollViewMode;
import net.risingworld.api.ui.style.Pivot;
import net.risingworld.api.ui.style.Unit;

public class Dropdown extends UIElement {

    private static final int ROW_HEIGHT = 30;
    private static final int MAX_VISIBLE_ROWS = 6;

    private final List<DropdownOption> options;
    private final DropdownCallback callback;

    private final BaseButton headerButton;
    private final UIScrollView dropdownList;

    private DropdownOption selectedOption;
    private boolean open = false;

    public Dropdown(List<DropdownOption> options, String initialKey, DropdownCallback callback) {
        this.options = options;
        this.callback = callback;

        // Root element
        style.width.set(100, Unit.Percent);
        style.height.set(30, Unit.Pixel);

        // Header
        headerButton = new CancelButton("Select...", (PlayerUIElementClickEvent event) -> toggle());
        headerButton.setSize(100, 100, true);
        headerButton.setPivot(Pivot.UpperLeft);

        this.setPivot(Pivot.UpperLeft);

        addChild(headerButton);

        // Dropdown list
        dropdownList = new UIScrollView(ScrollViewMode.Vertical);
        dropdownList.setVisible(false);
        dropdownList.setPosition(0, 30, false);
        dropdownList.setPivot(Pivot.UpperLeft);
        dropdownList.style.width.set(100, Unit.Percent);
        dropdownList.style.height.set(Math.min(options.size(), MAX_VISIBLE_ROWS) * ROW_HEIGHT, Unit.Pixel);

        addChild(dropdownList);
        buildOptions();
        setInitialValue(initialKey);
    }

    private void buildOptions() {
        dropdownList.removeAllChilds();

        for (int i = 0; i < options.size(); i++) {
            DropdownOption option = options.get(i);

            CancelButton btn = new CancelButton(option.getLabel(), (PlayerUIElementClickEvent event) -> {
                select(option);
            });
            btn.setPosition(0, i * ROW_HEIGHT, false);
            btn.setPivot(Pivot.UpperLeft);
            btn.style.width.set(100, Unit.Percent);
            btn.style.height.set(ROW_HEIGHT, Unit.Pixel);

            dropdownList.addChild(btn);
        }

        dropdownList.style.height.set(
                options.size() * ROW_HEIGHT,
                Unit.Pixel);
    }

    private void toggle() {
        open = !open;
        dropdownList.setVisible(open);
    }

    private void setInitialValue(String key) {
        if (key == null) {
            return;
        }

        for (DropdownOption option : options) {
            if (option.getKey().equals(key)) {
                selectedOption = option;
                headerButton.setText(option.getLabel());
                return;
            }
        }
    }

    private void select(DropdownOption option) {
        selectedOption = option;
        headerButton.setText(option.getLabel());
        dropdownList.setVisible(false);
        open = false;

        if (callback != null) {
            callback.onSelect(option.getKey());
        }
    }

    public String getSelectedKey() {
        return selectedOption != null ? selectedOption.getKey() : null;
    }

}
