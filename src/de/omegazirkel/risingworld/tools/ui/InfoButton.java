package de.omegazirkel.risingworld.tools.ui;

import net.risingworld.api.callbacks.Callback;
import net.risingworld.api.events.player.ui.PlayerUIElementClickEvent;

public class InfoButton extends BaseButton {

    public InfoButton(String text, Callback<PlayerUIElementClickEvent> callback) {
        super(text, callback);

        setBackgroundColor(0.2f, 0.4f, 0.9f, 1f);
        setBorderColor(0f, 0f, 0f, 0.4f);
        setBorder(1);
        setHoverBackgroundColor(0x3F70FFFF);
    }
}

