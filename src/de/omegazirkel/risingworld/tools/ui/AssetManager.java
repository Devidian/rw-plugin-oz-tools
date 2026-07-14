package de.omegazirkel.risingworld.tools.ui;

import java.util.HashMap;
import java.util.Map;

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

    /**
     * Compatibility aliases for icon keys persisted by older plugin releases.
     * Active registrations and assets use the semantic names from this map's values.
     */
    private static final Map<String, String> LEGACY_ICON_ALIASES = Map.ofEntries(
            Map.entry("admin-menu", "menu-administration"),
            Map.entry("exit-alt", "menu-exit"),
            Map.entry("left", "menu-back"),
            Map.entry("icon-gpt-plugin-config", "menu-plugin-config"),
            Map.entry("icon-ki-info-status", "info-status"),
            Map.entry("icon-ki-placeholder", "placeholder"),
            Map.entry("icon-ki-soon", "soon"),
            Map.entry("icon-ki-coin-default", "coin-default"),
            Map.entry("icon-ki-coin-omega-gold", "coin-omega-gold"),
            Map.entry("icon-ki-coin-omega-silver", "coin-omega-silver"),
            Map.entry("icon-ki-oz-wallet", "oz-wallet"),
            Map.entry("shop-icon", "oz-shop"),
            Map.entry("icon-ki-zone-indicator-shop", "zone-shop-indicator"),
            Map.entry("icon-oz-rewards", "oz-rewards"),
            Map.entry("icon-ki-zone-indicator-marketplace", "zone-marketplace-indicator"),
            Map.entry("marketplace-icon", "oz-marketplace"),
            Map.entry("error-bug", "menu-debug"),
            Map.entry("icon-ki-claim-visibility", "menu-zone-visibility"),
            Map.entry("icon-ki-combat-zone", "zone-combat-create"),
            Map.entry("icon-ki-create-claim", "zone-claim-create"),
            Map.entry("icon-ki-current-off", "zone-visibility-current-off"),
            Map.entry("icon-ki-current-on", "zone-visibility-current-on"),
            Map.entry("icon-ki-delete-claim", "zone-claim-delete"),
            Map.entry("icon-ki-expand-down", "zone-expand-down"),
            Map.entry("icon-ki-expand-east", "zone-expand-east"),
            Map.entry("icon-ki-expand-menu", "menu-expand-zone"),
            Map.entry("icon-ki-expand-north", "zone-expand-north"),
            Map.entry("icon-ki-expand-south", "zone-expand-south"),
            Map.entry("icon-ki-expand-up", "zone-expand-up"),
            Map.entry("icon-ki-expand-west", "zone-expand-west"),
            Map.entry("icon-ki-for-sale", "zone-sale"),
            Map.entry("icon-ki-manage-claim", "zone-claim-manage"),
            Map.entry("icon-ki-neutral-zone", "zone-neutral-create"),
            Map.entry("icon-ki-others-off", "zone-visibility-others-off"),
            Map.entry("icon-ki-others-on", "zone-visibility-others-on"),
            Map.entry("icon-ki-owned-off", "zone-visibility-owned-off"),
            Map.entry("icon-ki-owned-on", "zone-visibility-owned-on"),
            Map.entry("icon-ki-permissions", "menu-zone-permissions"),
            Map.entry("icon-ki-plugin-logo", "oz-land-claim"),
            Map.entry("icon-ki-rename-claim", "zone-claim-rename"),
            Map.entry("icon-ki-renew-zone", "zone-renew-create"),
            Map.entry("icon-ki-rest-zone", "zone-rest-create"),
            Map.entry("icon-ki-special-zones", "menu-zone-special"),
            Map.entry("icon-ki-split-claim", "zone-claim-split"),
            Map.entry("icon-ki-static-zone", "zone-static-create"),
            Map.entry("icon-ki-trap-zone", "zone-trap-create"),
            Map.entry("icon-ki-zone-admin", "menu-zone-admin"),
            Map.entry("icon-ki-zone-administration", "menu-zone-management"),
            Map.entry("icon-ki-zone-indicator-sale", "zone-sale-indicator"),
            Map.entry("icon-ki-alcatraz", "marker-jail-island"),
            Map.entry("icon-ki-alpine-pasture", "marker-farm-alpine"),
            Map.entry("icon-ki-animal-farm", "marker-farm-animals"),
            Map.entry("icon-ki-arctic-01", "marker-arctic-polarbear"),
            Map.entry("icon-ki-arctic-02", "marker-arctic-penguin"),
            Map.entry("icon-ki-arctic-03", "marker-arctic-seal"),
            Map.entry("icon-ki-arctic-04", "marker-arctic-northern-lights"),
            Map.entry("icon-ki-arctic-05", "marker-arctic-iglu"),
            Map.entry("icon-ki-cave-01", "marker-cave-stalactite"),
            Map.entry("icon-ki-cave-02", "marker-cave-bat"),
            Map.entry("icon-ki-cave-03", "marker-cave-painting"),
            Map.entry("icon-ki-cave-04", "marker-cave-minerals"),
            Map.entry("icon-ki-cave-05", "marker-cave-cavelers"),
            Map.entry("icon-ki-coast-01", "marker-coast-jetty"),
            Map.entry("icon-ki-coast-02", "marker-coast-lighthouse"),
            Map.entry("icon-ki-coast-03", "marker-coast-boat"),
            Map.entry("icon-ki-coast-04", "marker-coast-house"),
            Map.entry("icon-ki-coast-05", "marker-coast-fishing"),
            Map.entry("icon-ki-corn-fields", "marker-farm-fields"),
            Map.entry("icon-ki-desert-01", "marker-desert-palm"),
            Map.entry("icon-ki-desert-02", "marker-desert-pyramids"),
            Map.entry("icon-ki-desert-03", "marker-desert-camel"),
            Map.entry("icon-ki-desert-04", "marker-desert-oasis"),
            Map.entry("icon-ki-desert-05", "marker-desert-night"),
            Map.entry("icon-ki-factory-modern", "marker-factory-modern"),
            Map.entry("icon-ki-factory-old", "marker-factory-old"),
            Map.entry("icon-ki-farm", "marker-farm-default"),
            Map.entry("icon-ki-forest-01", "marker-forest-default"),
            Map.entry("icon-ki-forest-02", "marker-forest-clearing"),
            Map.entry("icon-ki-forest-03", "marker-forest-log-cabin"),
            Map.entry("icon-ki-forest-04", "marker-forest-camping"),
            Map.entry("icon-ki-forest-05", "marker-forest-woodworker"),
            Map.entry("icon-ki-gps-add-marker", "gps-marker-create"),
            Map.entry("icon-ki-gps-coin", "coin-gps-token"),
            Map.entry("icon-ki-gps-global", "menu-global-marker"),
            Map.entry("icon-ki-gps-grid-view", "menu-grid-view"),
            Map.entry("icon-ki-gps-group-alt", "menu-marker-group-alt"),
            Map.entry("icon-ki-gps-group", "menu-marker-group"),
            Map.entry("icon-ki-gps-next-page", "next-page"),
            Map.entry("icon-ki-gps-plugin", "oz-gps"),
            Map.entry("icon-ki-gps-previous-page", "previous-page"),
            Map.entry("icon-ki-gps-private", "menu-marker-private"),
            Map.entry("icon-ki-gps-static", "menu-marker-static"),
            Map.entry("icon-ki-mountain-01", "marker-mountain-near"),
            Map.entry("icon-ki-mountain-02", "marker-mountain-house"),
            Map.entry("icon-ki-mountain-03", "marker-mountain-lake"),
            Map.entry("icon-ki-mountain-04", "marker-mountain-animals"),
            Map.entry("icon-ki-mountain-05", "marker-mountain-cross"),
            Map.entry("icon-ki-savanna-01", "marker-savanna-elephant"),
            Map.entry("icon-ki-savanna-02", "marker-savanna-zebra"),
            Map.entry("icon-ki-savanna-03", "marker-savanna-giraffe"),
            Map.entry("icon-ki-savanna-04", "marker-savanna-lion"),
            Map.entry("icon-ki-savanna-05", "marker-savanna-rhino"),
            Map.entry("icon-ki-sleep-01", "marker-sleep-restroom"),
            Map.entry("icon-ki-sleep-02", "marker-sleep-tent"),
            Map.entry("icon-ki-sleep-03", "marker-sleep-king-size-bed"),
            Map.entry("icon-ki-sleep-04", "marker-sleep-sign"),
            Map.entry("icon-ki-sleep-05", "marker-sleep-rip"),
            Map.entry("icon-ki-space-station", "marker-space-station"),
            Map.entry("icon-ki-special-01", "marker-special-destination"),
            Map.entry("icon-ki-train-station", "marker-factory-train-station"),
            Map.entry("icon-ki-village-01", "marker-village-small"),
            Map.entry("icon-ki-village-02", "marker-village-medium"),
            Map.entry("icon-ki-village-03", "marker-village-market"),
            Map.entry("icon-ki-village-04", "marker-village-well"),
            Map.entry("icon-ki-village-05", "marker-village-pallisade"),
            Map.entry("trash-xmark", "gps-marker-delete"),
            Map.entry("icon-ki-global-intercom", "oz-global-intercom"),
            Map.entry("discord-circle", "discord-logo"),
            Map.entry("icon-ki-discord-connect", "oz-discord-connect"),
            Map.entry("icon-ki-create", "zone-prison-create"),
            Map.entry("icon-ki-indicator-prison", "zone-prison-indicator"),
            Map.entry("icon-ki-manage-prison", "zone-prison-manage"),
            Map.entry("icon-ki-manage-zone", "zone-manage"),
            Map.entry("icon-ki-name-sync", "zone-name-sync"),
            Map.entry("icon-ki-release", "zone-prison-release"),
            Map.entry("icon-ki-set-spawn", "zone-prison-set-spawn"),
            Map.entry("oz-admin-utils-logo", "oz-admin-utils"),
            Map.entry("template-icon", "maven-template")
    );

    private static OZLogger logger() {
        return OZTools.logger();
    }

    public static void loadIconFromPlugin(Plugin plugin, String key, String path) {
        loadIcon(key, TextureAsset.loadFromPlugin(plugin, path));
    }

    public static void loadIconFromPlugin(Plugin plugin, String key) {
        String canonicalKey = canonicalIconKey(key);
        loadIcon(canonicalKey, TextureAsset.loadFromPlugin(plugin,
                baseAssetIconPath + defaultIconStyle + "/" + canonicalKey + ".png"));
        loadStyledIconFromPlugin(plugin, canonicalKey, defaultIconStyle);
        loadStyledIconFromPlugin(plugin, canonicalKey, "classic");
    }

    public static void loadStyledIconFromPlugin(Plugin plugin, String key, String style) {
        key = canonicalIconKey(key);
        String normalizedStyle = normalizeStyle(style);
        String stylePath = baseAssetIconPath + normalizedStyle + "/" + key + ".png";
        String defaultPath = baseAssetIconPath + defaultIconStyle + "/" + key + ".png";
        String resolvedPath = resourceExists(plugin, stylePath)
                ? stylePath : defaultPath;
        loadIcon(styledKey(key, normalizedStyle), TextureAsset.loadFromPlugin(plugin, resolvedPath));
    }

    public static void loadIcon(String key, TextureAsset icon) {
        String canonicalKey = canonicalIconKey(key);
        if (iconMap.containsKey(canonicalKey)) {
            logger().warn("Key " + canonicalKey + " is already set and will be overridden");
        }
        iconMap.put(canonicalKey, icon);
    }

    public static TextureAsset getIcon(String key) {
        return iconMap.get(canonicalIconKey(key));
    }

    public static TextureAsset getIcon(Player player, String key) {
        String canonicalKey = canonicalIconKey(key);
        TextureAsset icon = iconMap.get(styledKey(canonicalKey, ToolsPlayerPreferences.iconStyle(player)));
        if (icon != null) {
            return icon;
        }
        icon = iconMap.get(styledKey(canonicalKey, defaultIconStyle));
        return icon == null ? getIcon(canonicalKey) : icon;
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
        return normalizeStyle(style) + ":" + canonicalIconKey(key);
    }

    private static String canonicalIconKey(String key) {
        if (key == null || key.isBlank()) {
            return key;
        }
        return LEGACY_ICON_ALIASES.getOrDefault(key, key);
    }

    public static void loadDefaultIcons(OZTools plugin) {
        loadIconFromPlugin(plugin, "close");
        loadIconFromPlugin(plugin, "menu-administration");
        loadIconFromPlugin(plugin, "rename");
        loadIconFromPlugin(plugin, "menu-back");
        loadIconFromPlugin(plugin, "menu-exit");
        loadIconFromPlugin(plugin, "undo");
        loadIconFromPlugin(plugin, "menu-plugin-config");
        loadIconFromPlugin(plugin, "info-status");
        loadIconFromPlugin(plugin, "placeholder");
        loadIconFromPlugin(plugin, "soon");
        loadIconFromPlugin(plugin, "menu-debug");
        loadIconFromPlugin(plugin, "tools");
    }

}
