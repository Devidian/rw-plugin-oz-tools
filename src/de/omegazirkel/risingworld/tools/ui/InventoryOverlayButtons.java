package de.omegazirkel.risingworld.tools.ui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import net.risingworld.api.callbacks.Callback;
import net.risingworld.api.events.player.ui.PlayerUIElementClickEvent;

public final class InventoryOverlayButtons {
    private static final Map<String, InventoryOverlayButton> BUTTONS = new HashMap<>();
    private static final Comparator<InventoryOverlayButton> BUTTON_ORDER = Comparator
            .comparing((InventoryOverlayButton button) -> sortKey(button.getPluginName()))
            .thenComparing(button -> sortKey(button.getLabel()));

    private InventoryOverlayButtons() {
    }

    public static void registerButton(
            String pluginName,
            String label,
            Callback<PlayerUIElementClickEvent> callback) {
        registerButton(pluginName, label, null, callback);
    }

    public static void registerButton(
            String pluginName,
            String label,
            String iconKey,
            Callback<PlayerUIElementClickEvent> callback) {
        String normalizedPluginName = requiredText(pluginName, "pluginName");
        String normalizedLabel = requiredText(label, "label");
        if (callback == null) {
            throw new IllegalArgumentException("callback must not be null");
        }

        synchronized (BUTTONS) {
            BUTTONS.put(buttonKey(normalizedPluginName, normalizedLabel),
                    new InventoryOverlayButton(normalizedPluginName, normalizedLabel, optionalText(iconKey), callback));
        }
    }

    public static void unregisterButton(String pluginName, String label) {
        synchronized (BUTTONS) {
            BUTTONS.remove(buttonKey(requiredText(pluginName, "pluginName"), requiredText(label, "label")));
        }
    }

    public static void unregisterButtons(String pluginName) {
        String normalizedPluginName = requiredText(pluginName, "pluginName");
        synchronized (BUTTONS) {
            BUTTONS.entrySet().removeIf(entry -> entry.getValue().getPluginName().equals(normalizedPluginName));
        }
    }

    static List<InventoryOverlayButton> sortedButtons() {
        synchronized (BUTTONS) {
            List<InventoryOverlayButton> buttons = new ArrayList<>(BUTTONS.values());
            buttons.sort(BUTTON_ORDER);
            return buttons;
        }
    }

    private static String buttonKey(String pluginName, String label) {
        return pluginName + "\u0000" + label;
    }

    private static String requiredText(String value, String fieldName) {
        String text = optionalText(value);
        if (text == null) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return text;
    }

    private static String optionalText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private static String sortKey(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }
}
