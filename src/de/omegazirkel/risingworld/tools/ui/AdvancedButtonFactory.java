package de.omegazirkel.risingworld.tools.ui;

import net.risingworld.api.callbacks.Callback;
import net.risingworld.api.events.player.ui.PlayerUIElementClickEvent;

/** Factory for state-driven OZ Tools buttons. */
public final class AdvancedButtonFactory {
    private AdvancedButtonFactory() {
    }

    public static AdvancedButton ok(String label, Callback<PlayerUIElementClickEvent> callback) {
        return button(0x00000080, 0x269F59FF, 0xFFFFFFFF, 0x000000AA, 0x32A05AFF, label, callback);
    }

    public static AdvancedButton cancel(String label, Callback<PlayerUIElementClickEvent> callback) {
        return button(0x00000066, 0x666666FF, 0xFFFFFFFF, 0x00000099, 0x787878FF, label, callback);
    }

    public static AdvancedButton danger(String label, Callback<PlayerUIElementClickEvent> callback) {
        return button(0x00000080, 0xCC3333FF, 0xFFFFFFFF, 0x000000AA, 0xDD4444FF, label, callback);
    }

    /** Standard Plugin Manager button; replaces legacy blue info buttons. */
    public static AdvancedButton defaultButton(String label, Callback<PlayerUIElementClickEvent> callback) {
        return button(0x8A6A2DFF, 0x201B13D8, 0xF4F0E6FF, 0xD7AE55FF, 0x342915E8, label, callback);
    }

    public static AdvancedButton custom(AdvancedButtonState... states) {
        return new AdvancedButton(states);
    }

    private static AdvancedButton button(int border, int background, int text, int hoverBorder, int hoverBackground,
            String label, Callback<PlayerUIElementClickEvent> callback) {
        return custom(new AdvancedButtonState(AdvancedBaseButton.State.DEFAULT, border, background, text, hoverBorder,
                hoverBackground, label, callback));
    }
}
