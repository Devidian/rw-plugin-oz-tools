package de.omegazirkel.risingworld.tools.ui;

import net.risingworld.api.callbacks.Callback;
import net.risingworld.api.events.player.ui.PlayerUIElementClickEvent;
import net.risingworld.api.ui.style.Pivot;

/** Compact two-state switch using the same stable advanced-button surface. */
public class SwitchButton extends AdvancedBaseButton {
    private boolean state;
    private final Callback<Boolean> callback;

    public SwitchButton(boolean initialState, Callback<Boolean> callback) {
        super(
                new AdvancedButtonState(State.DEFAULT, 0x7A5D2AFF, 0x182F20FF, 0xD8D0C0FF,
                        0xD7AE55FF, 0x244D30FF, "", null),
                new AdvancedButtonState(State.EXTRA_1, 0x7A5D2AFF, 0x5A2424FF, 0xF2C766FF,
                        0xD7AE55FF, 0x743030FF, "Aus", null),
                new AdvancedButtonState(State.EXTRA_2, 0x7A5D2AFF, 0x1D4D2AFF, 0xF2C766FF,
                        0xD7AE55FF, 0x286B39FF, "An", null));
        this.state = initialState;
        this.callback = callback;
        setPivot(Pivot.MiddleCenter);
        setSize(60, 22, false);
        setState(state ? State.EXTRA_2 : State.EXTRA_1);
    }

    @Override
    public void onClick(PlayerUIElementClickEvent event) {
        state = !state;
        setState(state ? State.EXTRA_2 : State.EXTRA_1);
        if (callback != null) {
            callback.onCall(state);
        }
    }
}
