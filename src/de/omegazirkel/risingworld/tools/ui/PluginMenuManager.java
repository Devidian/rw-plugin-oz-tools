package de.omegazirkel.risingworld.tools.ui;

import java.util.ArrayList;
import java.util.List;

import net.risingworld.api.assets.TextureAsset;
import net.risingworld.api.objects.Player;

public class PluginMenuManager {
    private static List<MenuItem> menuItems = new ArrayList<>();

    public static void registerPluginMenu(MenuItem menu) {
        menuItems.add(menu);
    }

    public static void showMainMenu(Player player) {
        // copy menuItems locally and add close button
        List<MenuItem> menuItemsCopy = new ArrayList<>(menuItems);
        menuItemsCopy.add(MenuItem.closeMenu(player));

        showMenu(player, menuItemsCopy);
    }

    public static void showMenu(Player p, List<MenuItem> items) {
        TextureAsset[] icons = items.stream().map(MenuItem::getIcon).toArray(TextureAsset[]::new);
        String[] labels = items.stream().map(MenuItem::getLabel).toArray(String[]::new);

        p.showRadialMenu(icons, labels, null, false, i -> {
            if (i < 0 || i >= items.size()) {
                p.hideRadialMenu(false);
                return;
            }
            items.get(i).getAction().onCall(p);
        });
    }
}
