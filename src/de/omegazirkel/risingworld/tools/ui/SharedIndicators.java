package de.omegazirkel.risingworld.tools.ui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.omegazirkel.risingworld.OZTools;
import net.risingworld.api.objects.Player;

public final class SharedIndicators {
    private static final Map<String, SharedIndicatorProvider> PROVIDERS = new HashMap<>();
    private static final Comparator<SharedIndicator> INDICATOR_ORDER = Comparator
            .comparing((SharedIndicator indicator) -> sortKey(indicator.getPluginName()))
            .thenComparing(indicator -> sortKey(indicator.getIconKey()));

    private SharedIndicators() {
    }

    public static void registerProvider(String pluginName, SharedIndicatorProvider provider) {
        String normalizedPluginName = requiredText(pluginName, "pluginName");
        if (provider == null) {
            throw new IllegalArgumentException("provider must not be null");
        }

        synchronized (PROVIDERS) {
            PROVIDERS.put(normalizedPluginName, provider);
        }
        SharedIndicatorManager.refreshAllPlayers();
    }

    public static void unregisterProvider(String pluginName) {
        synchronized (PROVIDERS) {
            PROVIDERS.remove(requiredText(pluginName, "pluginName"));
        }
        SharedIndicatorManager.refreshAllPlayers();
    }

    static List<SharedIndicator> visibleIndicators(Player player) {
        List<Map.Entry<String, SharedIndicatorProvider>> providers;
        synchronized (PROVIDERS) {
            providers = new ArrayList<>(PROVIDERS.entrySet());
        }

        List<SharedIndicator> indicators = new ArrayList<>();
        for (Map.Entry<String, SharedIndicatorProvider> entry : providers) {
            String iconKey = visibleIcon(entry.getKey(), entry.getValue(), player);
            if (iconKey != null) {
                indicators.add(new SharedIndicator(entry.getKey(), iconKey));
            }
        }
        indicators.sort(INDICATOR_ORDER);
        return indicators;
    }

    private static String visibleIcon(String pluginName, SharedIndicatorProvider provider, Player player) {
        try {
            if (!provider.showIndicator(player)) {
                return null;
            }
            String iconKey = optionalText(provider.getIcon(player));
            if (iconKey == null || AssetManager.getIcon(iconKey) == null) {
                return null;
            }
            return iconKey;
        } catch (Exception e) {
            OZTools.logger().warn("Indicator provider failed for " + pluginName + ": " + e.getMessage());
            return null;
        }
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
