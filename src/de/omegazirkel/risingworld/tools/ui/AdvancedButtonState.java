package de.omegazirkel.risingworld.tools.ui;

import net.risingworld.api.callbacks.Callback;
import net.risingworld.api.events.player.ui.PlayerUIElementClickEvent;

/**
 * One visual and behavioral state of an {@link AdvancedBaseButton}. Null style
 * values inherit from the DEFAULT state, so a state only needs to describe its
 * differences.
 */
public record AdvancedButtonState(
        AdvancedBaseButton.State state,
        Integer borderColor,
        Integer backgroundColor,
        Integer textColor,
        Integer hoverBorderColor,
        Integer hoverBackgroundColor,
        String label,
        Callback<PlayerUIElementClickEvent> callback) {
}
