package de.omegazirkel.risingworld.tools.ui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import de.omegazirkel.risingworld.OZTools;
import de.omegazirkel.risingworld.tools.I18n;
import net.risingworld.api.assets.TextureAsset;
import net.risingworld.api.objects.Player;
import net.risingworld.api.ui.UITarget;

public class PluginMenuManager {
    private static List<MenuItem> menuItems = new ArrayList<>();
    private static final Comparator<MenuItem> MENU_ITEM_ORDER = Comparator
            .comparing((MenuItem item) -> sortKey(item.getLabel()));

    private static I18n t() {
        return I18n.getInstance(OZTools.name);
    };

    public static void registerPluginMenu(MenuItem menu) {
        menuItems.add(menu);
    }

    public static void showMainMenu(Player player) {
        List<MenuItem> menuItemsCopy = mainMenuItems(player);
        menuItemsCopy.add(MenuItem.closeMenu(player));

        showMenu(player, menuItemsCopy);
    }

    public static List<MenuItem> mainMenuItems(Player player) {
        List<MenuItem> menuItemsCopy = visiblePluginMenuItems(player);
        menuItemsCopy
                .add(MenuItem.iconKey("menu-plugin-config", t().get("tc.menu.settings", player),
                        (p) -> {
                            p.hideRadialMenu(true);
                            PlayerPluginSettingsOverlay overlay = (PlayerPluginSettingsOverlay) p
                                    .getAttribute("tools.ui.overlay");
                            if (overlay != null) {
                                p.deleteAttribute("tools.ui.overlay");
                            }
                            overlay = new PlayerPluginSettingsOverlay(p);
                            p.addUIElement(overlay, UITarget.Modal);
                            p.setAttribute("tools.ui.overlay", overlay);
                        }));
        return menuItemsCopy;
    }

    private static List<MenuItem> sortedPluginMenuItems() {
        List<MenuItem> sortedItems = new ArrayList<>(menuItems);
        sortedItems.sort(MENU_ITEM_ORDER);
        return sortedItems;
    }

    private static List<MenuItem> visiblePluginMenuItems(Player player) {
        return new ArrayList<>(sortedPluginMenuItems().stream()
                .filter(item -> item.isVisible(player))
                .toList());
    }

    public static void showMenu(Player p, List<MenuItem> items) {
        List<MenuItem> visibleItems = items.stream().filter(item -> item.isVisible(p)).toList();
        TextureAsset[] icons = visibleItems.stream().map(item -> item.getIcon(p)).toArray(TextureAsset[]::new);
        String[] labels = visibleItems.stream().map(MenuItem::getLabel).toArray(String[]::new);

        p.showRadialMenu(icons, labels, null, false, i -> {
            if (i < 0 || i >= visibleItems.size()) {
                p.hideRadialMenu(false);
                return;
            }
            visibleItems.get(i).getAction().onCall(p);
        });
    }

    private static String sortKey(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }
}
