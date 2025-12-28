package de.omegazirkel.risingworld.tools.ui;

import de.omegazirkel.risingworld.OZTools;
import de.omegazirkel.risingworld.tools.I18n;
import net.risingworld.api.assets.TextureAsset;
import net.risingworld.api.callbacks.Callback;
import net.risingworld.api.objects.Player;

public class MenuItem {
    private final TextureAsset icon;
    private final String label;
    private final Callback<Player> action;

    private static I18n t() {
        return I18n.getInstance(OZTools.name);
    };

    public MenuItem(TextureAsset icon, String label, Callback<Player> action) {
        this.icon = icon;
        this.label = label;
        this.action = action;
    }

    public TextureAsset getIcon() {
        return icon;
    }

    public String getLabel() {
        return label;
    }

    public Callback<Player> getAction() {
        return action;
    }

    public static MenuItem closeMenu(Player player) {
        return new MenuItem(
                AssetManager.getIcon("exit-alt"),
                t().get("TC_MENU_CLOSE", player),
                (p) -> {
                    p.hideRadialMenu(false);
                });
    }

    public static MenuItem backMenu(Player player, Callback<Player> action) {
        return new MenuItem(
                AssetManager.getIcon("undo"),
                t().get("TC_MENU_BACK", player),
                action);
    }
}
