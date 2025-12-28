package de.omegazirkel.risingworld.tools.ui;

import net.risingworld.api.callbacks.Callback;
import net.risingworld.api.events.player.ui.PlayerUIElementClickEvent;

public class ButtonFactory {
    public static OkButton ok(String label, Callback<PlayerUIElementClickEvent> cb) {
        return new OkButton(label, cb);
    }

    public static CancelButton cancel(String label, Callback<PlayerUIElementClickEvent> cb) {
        return new CancelButton(label, cb);
    }

    public static DangerButton danger(String label, Callback<PlayerUIElementClickEvent> cb) {
        return new DangerButton(label, cb);
    }

    public static InfoButton info(String label, Callback<PlayerUIElementClickEvent> cb) {
        return new InfoButton(label, cb);
    }
}
