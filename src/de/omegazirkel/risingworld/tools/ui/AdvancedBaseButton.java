package de.omegazirkel.risingworld.tools.ui;

import java.util.EnumMap;
import java.util.Map;

import net.risingworld.api.callbacks.Callback;
import net.risingworld.api.assets.TextureAsset;
import net.risingworld.api.events.player.ui.PlayerUIElementClickEvent;
import net.risingworld.api.ui.UIElement;
import net.risingworld.api.ui.UILabel;
import net.risingworld.api.ui.style.Pivot;
import net.risingworld.api.ui.style.TextAnchor;
import net.risingworld.api.ui.style.ScaleMode;

/**
 * Stable button layout container with independently switchable visual states.
 * The child keeps its identity while styles change, avoiding Unity hover state
 * corruption when a button becomes enabled or disabled.
 */
public abstract class AdvancedBaseButton extends OZUIElement {
    public enum State {
        DEFAULT,
        DISABLED,
        EXTRA_1,
        EXTRA_2,
        EXTRA_3
    }

    private final Map<State, AdvancedButtonState> states = new EnumMap<>(State.class);
    private final OZUIElement button;
    private final UILabel label;
    private State currentState;
    private Callback<PlayerUIElementClickEvent> callback;
    private boolean clickable = true;
    private Boolean renderedClickable;

    protected AdvancedBaseButton(AdvancedButtonState... stateDefinitions) {
        if (stateDefinitions == null || stateDefinitions.length == 0) {
            throw new IllegalArgumentException("At least one button state is required");
        }
        for (AdvancedButtonState stateDefinition : stateDefinitions) {
            if (stateDefinition == null || stateDefinition.state() == null) {
                throw new IllegalArgumentException("Every button state needs a state identifier");
            }
            states.put(stateDefinition.state(), stateDefinition);
        }
        if (!states.containsKey(State.DEFAULT)) {
            throw new IllegalArgumentException("A DEFAULT button state is required");
        }

        setPivot(Pivot.MiddleCenter);
        setBackgroundColor(0x00000000);

        button = new OZUIElement();
        button.setPivot(Pivot.MiddleCenter);
        button.setPosition(50, 50, true);
        button.setSize(100, 100, true);
        button.setBorder(1);
        button.setClickAction(event -> onClick(event));
        super.addChild(button);

        label = new UILabel("");
        label.setPivot(Pivot.MiddleCenter);
        label.setPosition(50, 50, true);
        label.setSize(100, 100, true);
        label.setTextAlign(TextAnchor.MiddleCenter);
        button.addChild(label);

        setState(State.DEFAULT);
    }

    public final State getState() {
        return currentState;
    }

    public final void setState(State state) {
        AdvancedButtonState definition = states.get(state);
        if (definition == null) {
            throw new IllegalArgumentException("Button state is not configured: " + state);
        }
        boolean stateChanged = currentState != state;
        currentState = state;
        AdvancedButtonState defaults = states.get(State.DEFAULT);
        button.setBorderColor(value(definition.borderColor(), defaults.borderColor(), 0x00000088));
        button.setBackgroundColor(value(definition.backgroundColor(), defaults.backgroundColor(), 0x444444FF));
        button.setHoverBorderColor(value(definition.hoverBorderColor(), defaults.hoverBorderColor(), 0x000000AA));
        button.setHoverBorderWidth(1);
        button.setHoverBackgroundColor(value(definition.hoverBackgroundColor(), defaults.hoverBackgroundColor(), 0x555555FF));
        label.setFontColor(value(definition.textColor(), defaults.textColor(), 0xFFFFFFFF));
        label.setText(value(definition.label(), defaults.label(), ""));
        callback = definition.callback() != null ? definition.callback() : defaults.callback();
        applyClickableState();
        if (stateChanged) {
            // Rising World's UI keeps stale hover data after style changes. The
            // stable outer container preserves layout while re-attaching only
            // the actual interactive surface refreshes that hover state.
            super.removeChild(button);
            super.addChild(button);
        }
    }

    public final void setText(String text) {
        label.setText(text == null ? "" : text);
    }

    /** Applies an icon to the actual clickable surface, above its state background. */
    public final void setSurfaceIcon(TextureAsset icon) {
        button.style.backgroundImage.set(icon);
        button.style.backgroundImageScaleMode.set(ScaleMode.ScaleToFit);
    }

    /**
     * Keeps consumer-provided labels, icons, and overlays inside the clickable
     * surface instead of above it in the layout container.
     */
    @Override
    public void addChild(UIElement child) {
        if (button == null) {
            super.addChild(child);
        } else {
            button.addChild(child);
        }
    }

    @Override
    public void removeChild(UIElement child) {
        if (button == null) {
            super.removeChild(child);
        } else {
            button.removeChild(child);
        }
    }

    @Override
    public void setClickable(boolean clickable) {
        super.setClickable(clickable);
        this.clickable = clickable;
        applyClickableState();
    }

    @Override
    public void setBackgroundColor(int rgba) {
        if (button == null) {
            super.setBackgroundColor(rgba);
        } else {
            button.setBackgroundColor(rgba);
        }
    }

    @Override
    public void setBackgroundColor(float red, float green, float blue, float alpha) {
        if (button == null) {
            super.setBackgroundColor(red, green, blue, alpha);
        } else {
            button.setBackgroundColor(red, green, blue, alpha);
        }
    }

    @Override
    public void setBorder(float thickness) {
        if (button == null) {
            super.setBorder(thickness);
        } else {
            button.setBorder(thickness);
        }
    }

    @Override
    public void setBorderColor(int rgba) {
        if (button == null) {
            super.setBorderColor(rgba);
        } else {
            button.setBorderColor(rgba);
        }
    }

    @Override
    public void setBorderColor(float red, float green, float blue, float alpha) {
        if (button == null) {
            super.setBorderColor(red, green, blue, alpha);
        } else {
            button.setBorderColor(red, green, blue, alpha);
        }
    }

    @Override
    public void setBorderEdgeRadius(float radius, boolean relative) {
        if (button == null) {
            super.setBorderEdgeRadius(radius, relative);
        } else {
            button.setBorderEdgeRadius(radius, relative);
        }
    }

    @Override
    public void setHoverBackgroundColor(int rgba) {
        if (button == null) {
            super.setHoverBackgroundColor(rgba);
        } else {
            button.setHoverBackgroundColor(rgba);
        }
    }

    @Override
    public void setHoverBorderColor(int rgba) {
        if (button == null) {
            super.setHoverBorderColor(rgba);
        } else {
            button.setHoverBorderColor(rgba);
        }
    }

    @Override
    public void setHoverBorderWidth(float thickness) {
        if (button == null) {
            super.setHoverBorderWidth(thickness);
        } else {
            button.setHoverBorderWidth(thickness);
        }
    }

    @Override
    public void setClickAction(Callback<PlayerUIElementClickEvent> clickAction) {
        super.setClickAction(clickAction);
        callback = clickAction;
    }

    @Override
    public void onClick(PlayerUIElementClickEvent event) {
        if (currentState != State.DISABLED && callback != null) {
            callback.onCall(event);
        }
    }

    private static int value(Integer primary, Integer fallback, int hardDefault) {
        return primary != null ? primary : fallback != null ? fallback : hardDefault;
    }

    private static String value(String primary, String fallback, String hardDefault) {
        return primary != null ? primary : fallback != null ? fallback : hardDefault;
    }

    private void applyClickableState() {
        if (button == null || currentState == null) {
            return;
        }
        boolean nextClickable = clickable && currentState != State.DISABLED;
        if (renderedClickable == null || renderedClickable != nextClickable) {
            button.setClickable(nextClickable);
            renderedClickable = nextClickable;
        }
    }
}
