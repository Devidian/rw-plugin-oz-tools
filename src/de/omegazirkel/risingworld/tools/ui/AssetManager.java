package de.omegazirkel.risingworld.tools.ui;

import java.util.HashMap;

import de.omegazirkel.risingworld.OZTools;
import de.omegazirkel.risingworld.tools.OZLogger;
import de.omegazirkel.risingworld.tools.ToolsPlayerPreferences;
import net.risingworld.api.Plugin;
import net.risingworld.api.assets.TextureAsset;
import net.risingworld.api.objects.Player;

public class AssetManager {

    private static HashMap<String, TextureAsset> iconMap = new HashMap<>();
    public static final String baseAssetIconPath = "/assets/icons/";
    public static final String defaultIconStyle = "modern";

    private static OZLogger logger() {
        return OZTools.logger();
    }

    public static void loadIconFromPlugin(Plugin plugin, String key, String path) {
        loadIcon(key, TextureAsset.loadFromPlugin(plugin, path));
    }

    public static void loadIconFromPlugin(Plugin plugin, String key) {
        loadIcon(key, TextureAsset.loadFromPlugin(plugin, baseAssetIconPath + key + ".png"));
        loadStyledIconFromPlugin(plugin, key, defaultIconStyle);
        loadStyledIconFromPlugin(plugin, key, "classic");
    }

    public static void loadStyledIconFromPlugin(Plugin plugin, String key, String style) {
        String normalizedStyle = normalizeStyle(style);
        String stylePath = baseAssetIconPath + normalizedStyle + "/" + key + ".png";
        String defaultPath = baseAssetIconPath + defaultIconStyle + "/" + key + ".png";
        String legacyPath = baseAssetIconPath + key + ".png";
        String resolvedPath = resourceExists(plugin, stylePath)
                ? stylePath
                : resourceExists(plugin, defaultPath) ? defaultPath : legacyPath;
        loadIcon(styledKey(key, normalizedStyle), TextureAsset.loadFromPlugin(plugin, resolvedPath));
    }

    public static void loadIcon(String key, TextureAsset icon) {
        if (iconMap.containsKey(key)) {
            logger().warn("Key " + key + " is already set and will be overridden");
        }
        iconMap.put(key, icon);
    }

    public static TextureAsset getIcon(String key) {
        return iconMap.get(key);
    }

    public static TextureAsset getIcon(Player player, String key) {
        TextureAsset icon = iconMap.get(styledKey(key, ToolsPlayerPreferences.iconStyle(player)));
        if (icon != null) {
            return icon;
        }
        icon = iconMap.get(styledKey(key, defaultIconStyle));
        return icon == null ? getIcon(key) : icon;
    }

    public static String normalizeStyle(String style) {
        if (style == null || style.isBlank()) {
            return defaultIconStyle;
        }
        String normalized = style.trim().toLowerCase();
        return normalized.equals("classic") || normalized.equals(defaultIconStyle) ? normalized : defaultIconStyle;
    }

    private static boolean resourceExists(Plugin plugin, String path) {
        if (plugin == null || path == null || path.isBlank()) {
            return false;
        }
        return plugin.getClass().getResource(path) != null;
    }

    private static String styledKey(String key, String style) {
        return normalizeStyle(style) + ":" + key;
    }

    public static void loadDefaultIcons(OZTools plugin) {
        loadIconFromPlugin(plugin, "close");
        loadIconFromPlugin(plugin, "admin-menu");
        loadIconFromPlugin(plugin, "rename");
        loadIconFromPlugin(plugin, "left");
        loadIconFromPlugin(plugin, "exit-alt");
        loadIconFromPlugin(plugin, "undo");
        loadIconFromPlugin(plugin, "icon-gpt-plugin-config");
        loadIconFromPlugin(plugin, "icon-ki-info-status");
        loadIconFromPlugin(plugin, "icon-ki-placeholder");
        loadIconFromPlugin(plugin, "icon-ki-soon");
    }

}
