package de.omegazirkel.risingworld.tools.ui;

import java.util.function.Consumer;

import net.risingworld.api.events.player.ui.PlayerUIElementClickEvent;

public class OkButton extends BaseButton {

    public OkButton(String text, Consumer<PlayerUIElementClickEvent> callback) {
        super(text, callback);

        setBackgroundColor(0.15f, 0.65f, 0.35f, 1f);  // green
        setBorderColor(0f, 0f, 0f, 0.5f);
        setBorder(1);
        setHoverBackgroundColor(0x32a05aff);
    }
}

