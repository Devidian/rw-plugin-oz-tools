package de.omegazirkel.risingworld.tools.ui;

import java.util.function.Consumer;

import net.risingworld.api.events.player.ui.PlayerUIElementClickEvent;
import net.risingworld.api.ui.style.Pivot;

public class SwitchButton extends OZUIElement {

    private boolean state;
    private final Consumer<Boolean> callback;

    private OZUIElement leftSide;
    private OZUIElement rightSide;

    public SwitchButton(boolean initialState, Consumer<Boolean> callback) {
        this.state = initialState;
        this.callback = callback;

        setPivot(Pivot.MiddleCenter);
        setSize(60, 22, false); // pixel size
        setClickable(true);

        buildUI();
        updateState();
    }

    private void buildUI() {
        // Left panel
        leftSide = new OZUIElement();
        leftSide.setPivot(Pivot.MiddleLeft);
        leftSide.setPosition(0, 50, true); // y=50% center
        leftSide.setSize(50, 100, true); // 50% width, 100% height
        addChild(leftSide);

        // Right panel
        rightSide = new OZUIElement();
        rightSide.setPivot(Pivot.MiddleRight);
        rightSide.setPosition(100, 50, true); // x=100% right, y=50% center
        rightSide.setSize(50, 100, true);
        addChild(rightSide);
    }

    private void updateState() {
        if (state) {
            // ON state
            leftSide.setBackgroundColor(0.0f, 0.8f, 0.0f, 1f); // green
            rightSide.setBackgroundColor(0.3f, 0.3f, 0.3f, 1f); // grey
        } else {
            leftSide.setBackgroundColor(0.3f, 0.3f, 0.3f, 1f); // grey
            rightSide.setBackgroundColor(0.8f, 0.1f, 0.1f, 1f); // red
        }

        setBorder(1);
        setBorderColor(0, 0, 0, 0.4f);
    }

    @Override
    public void onClick(PlayerUIElementClickEvent event) {
        state = !state;
        updateState();
        if (callback != null)
            callback.accept(state);
    }
}
