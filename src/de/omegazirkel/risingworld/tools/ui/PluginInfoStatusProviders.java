package de.omegazirkel.risingworld.tools.ui;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import de.omegazirkel.risingworld.OZTools;
import net.risingworld.api.objects.Player;

public final class PluginInfoStatusProviders {
    private static final Map<String, PluginInfoStatusProvider> PROVIDERS = new HashMap<>();

    private PluginInfoStatusProviders() {
    }

    public static void registerProvider(PluginInfoStatusProvider provider) {
        if (provider == null) {
            throw new IllegalArgumentException("provider must not be null");
        }

        String pluginName = requiredText(provider.getPluginName(), "pluginName");
        synchronized (PROVIDERS) {
            PROVIDERS.put(providerKey(pluginName), provider);
        }
    }

    public static void unregisterProvider(String pluginName) {
        synchronized (PROVIDERS) {
            PROVIDERS.remove(providerKey(requiredText(pluginName, "pluginName")));
        }
    }

    public static boolean show(Player player, String pluginName) {
        if (player == null) {
            return false;
        }

        PluginInfoStatusProvider provider = findProvider(pluginName);
        if (provider == null) {
            return false;
        }

        String displayName = safePluginName(provider, pluginName);
        String info = safeProviderText(provider, player, displayName, TabContent.INFO);
        String status = safeProviderText(provider, player, displayName, TabContent.STATUS);
        PluginInfoStatusPanel.show(player, displayName, info, status);
        return true;
    }

    public static MenuItem menuItem(String label, String pluginName) {
        return MenuItem.iconKey("icon-ki-info-status", label, player -> {
            player.hideRadialMenu(true);
            show(player, pluginName);
        });
    }

    private static PluginInfoStatusProvider findProvider(String pluginName) {
        String key = optionalProviderKey(pluginName);
        if (key == null) {
            return null;
        }

        synchronized (PROVIDERS) {
            return PROVIDERS.get(key);
        }
    }

    private static String safePluginName(PluginInfoStatusProvider provider, String fallbackPluginName) {
        try {
            String pluginName = optionalText(provider.getPluginName());
            if (pluginName != null) {
                return pluginName;
            }
        } catch (Exception e) {
            OZTools.logger().warn("Info/status provider failed while reading plugin name: " + e.getMessage());
        }
        String fallback = optionalText(fallbackPluginName);
        return fallback == null ? "" : fallback;
    }

    private static String safeProviderText(PluginInfoStatusProvider provider, Player player, String pluginName,
            TabContent content) {
        try {
            String text = content == TabContent.INFO ? provider.getInfo(player) : provider.getStatus(player);
            return text == null ? "" : text;
        } catch (Exception e) {
            OZTools.logger().warn("Info/status provider failed for " + pluginName + ": " + e.getMessage());
            return "";
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

    private static String providerKey(String pluginName) {
        return pluginName.toLowerCase(Locale.ROOT);
    }

    private static String optionalProviderKey(String pluginName) {
        String text = optionalText(pluginName);
        return text == null ? null : providerKey(text);
    }

    private enum TabContent {
        INFO,
        STATUS
    }
}
