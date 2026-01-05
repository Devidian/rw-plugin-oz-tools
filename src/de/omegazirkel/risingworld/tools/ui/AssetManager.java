package de.omegazirkel.risingworld.tools.ui;

import java.util.HashMap;

import de.omegazirkel.risingworld.OZTools;
import de.omegazirkel.risingworld.tools.OZLogger;
import net.risingworld.api.Plugin;
import net.risingworld.api.assets.TextureAsset;

public class AssetManager {

    private static HashMap<String, TextureAsset> iconMap = new HashMap<>();
    public static final String baseAssetIconPath = "/assets/icons/";

    private static OZLogger logger() {
        return OZLogger.getInstance("OZ.Tools.AssetManager");
    }

    public static void loadIconFromPlugin(Plugin plugin, String key, String path) {
        loadIcon(key, TextureAsset.loadFromPlugin(plugin, path));
    }

    public static void loadIconFromPlugin(Plugin plugin, String key) {
        loadIcon(key, TextureAsset.loadFromPlugin(plugin, baseAssetIconPath + key + ".png"));
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

    public static void loadDefaultIcons(OZTools plugin) {
        loadIconFromPlugin(plugin, "close");
        loadIconFromPlugin(plugin, "admin-menu");
        loadIconFromPlugin(plugin, "rename");
        loadIconFromPlugin(plugin, "left");
        loadIconFromPlugin(plugin, "exit-alt");
        loadIconFromPlugin(plugin, "undo");
        loadIconFromPlugin(plugin, "icon-gpt-plugin-config");
    }

}
