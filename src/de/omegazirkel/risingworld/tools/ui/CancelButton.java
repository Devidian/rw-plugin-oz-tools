package de.omegazirkel.risingworld.tools.ui;

import net.risingworld.api.callbacks.Callback;
import net.risingworld.api.events.player.ui.PlayerUIElementClickEvent;

public class CancelButton extends BaseButton {

    public CancelButton(String text, Callback<PlayerUIElementClickEvent> callback) {
        super(text, callback);

        setBackgroundColor(0.4f, 0.4f, 0.4f, 1f);
        setBorderColor(0f, 0f, 0f, 0.4f);
        setBorder(1);
        setHoverBackgroundColor(0x787878ff);
    }
}

