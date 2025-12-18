package de.omegazirkel.risingworld.tools.ui;

import java.util.function.Consumer;

import net.risingworld.api.events.player.ui.PlayerUIElementClickEvent;

public class ButtonFactory {
    public static OkButton ok(String label, Consumer<PlayerUIElementClickEvent> cb) {
        return new OkButton(label, cb);
    }

    public static CancelButton cancel(String label, Consumer<PlayerUIElementClickEvent> cb) {
        return new CancelButton(label, cb);
    }

    public static DangerButton danger(String label, Consumer<PlayerUIElementClickEvent> cb) {
        return new DangerButton(label, cb);
    }

    public static InfoButton info(String label, Consumer<PlayerUIElementClickEvent> cb) {
        return new InfoButton(label, cb);
    }
}
