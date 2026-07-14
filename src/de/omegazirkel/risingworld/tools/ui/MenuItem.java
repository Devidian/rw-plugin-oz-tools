package de.omegazirkel.risingworld.tools.ui;

import de.omegazirkel.risingworld.OZTools;
import de.omegazirkel.risingworld.tools.I18n;
import java.util.function.Predicate;
import net.risingworld.api.assets.TextureAsset;
import net.risingworld.api.callbacks.Callback;
import net.risingworld.api.objects.Player;

public class MenuItem {
    private final String pluginName;
    private final TextureAsset icon;
    private final String iconKey;
    private final String label;
    private final Callback<Player> action;
    private final Predicate<Player> visibility;

    private static I18n t() {
        return I18n.getInstance(OZTools.name);
    };

    public MenuItem(TextureAsset icon, String label, Callback<Player> action) {
        this(label, icon, null, label, action, null);
    }

    public MenuItem(String iconKey, String label, Callback<Player> action) {
        this(label, null, iconKey, label, action, null);
    }

    public MenuItem(String pluginName, TextureAsset icon, String label, Callback<Player> action) {
        this(pluginName, icon, null, label, action, null);
    }

    public MenuItem(String pluginName, String iconKey, String label, Callback<Player> action) {
        this(pluginName, null, iconKey, label, action, null);
    }

    public MenuItem(String pluginName, TextureAsset icon, String label, Callback<Player> action,
            Predicate<Player> visibility) {
        this(pluginName, icon, null, label, action, visibility);
    }

    public MenuItem(String pluginName, String iconKey, String label, Callback<Player> action,
            Predicate<Player> visibility) {
        this(pluginName, null, iconKey, label, action, visibility);
    }

    private MenuItem(String pluginName, TextureAsset icon, String iconKey, String label, Callback<Player> action,
            Predicate<Player> visibility) {
        this.pluginName = pluginName == null || pluginName.isBlank() ? label : pluginName;
        this.icon = icon;
        this.iconKey = iconKey;
        this.label = label;
        this.action = action;
        this.visibility = visibility;
    }

    public static MenuItem iconKey(String iconKey, String label, Callback<Player> action) {
        return new MenuItem(label, null, iconKey, label, action, null);
    }

    public static MenuItem iconKey(String pluginName, String iconKey, String label, Callback<Player> action) {
        return new MenuItem(pluginName, null, iconKey, label, action, null);
    }

    public static MenuItem iconKey(String pluginName, String iconKey, String label, Callback<Player> action,
            Predicate<Player> visibility) {
        return new MenuItem(pluginName, null, iconKey, label, action, visibility);
    }

    public String getPluginName() {
        return pluginName;
    }

    public TextureAsset getIcon() {
        return icon;
    }

    public TextureAsset getIcon(Player player) {
        return iconKey == null ? icon : AssetManager.getIcon(player, iconKey);
    }

    public String getLabel() {
        return label;
    }

    public Callback<Player> getAction() {
        return action;
    }

    public boolean isVisible(Player player) {
        if (!PluginShortcutVisibility.isVisible(pluginName, player)) {
            return false;
        }
        if (visibility == null) {
            return true;
        }
        try {
            return visibility.test(player);
        } catch (RuntimeException ex) {
            return true;
        }
    }

    public static MenuItem closeMenu(Player player) {
        return MenuItem.iconKey(
                "menu-exit",
                t().get("TC_MENU_CLOSE", player),
                (p) -> {
                    p.hideRadialMenu(false);
                });
    }

    public static MenuItem backMenu(Player player, Callback<Player> action) {
        return MenuItem.iconKey(
                "undo",
                t().get("TC_MENU_BACK", player),
                action);
    }
}
