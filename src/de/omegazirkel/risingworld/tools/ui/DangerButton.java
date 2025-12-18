package de.omegazirkel.risingworld.tools.ui;

import java.util.function.Consumer;

import net.risingworld.api.events.player.ui.PlayerUIElementClickEvent;

public class DangerButton extends BaseButton {

    public DangerButton(String text, Consumer<PlayerUIElementClickEvent> callback) {
        super(text, callback);

        setBackgroundColor(0.8f, 0.2f, 0.2f, 1f);
        setBorderColor(0f, 0f, 0f, 0.5f);
        setBorder(1);
        setHoverBackgroundColor(0xDD4444FF);
    }
}

