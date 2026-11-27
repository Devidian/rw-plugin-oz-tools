package de.omegazirkel.risingworld.tools.ui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import de.omegazirkel.risingworld.OZTools;
import de.omegazirkel.risingworld.tools.I18n;
import net.risingworld.api.assets.TextureAsset;
import net.risingworld.api.callbacks.Callback;
import net.risingworld.api.objects.Player;
import net.risingworld.api.ui.UITarget;

public class PluginMenuManager {
    private static final List<MenuItem> menuItems = new CopyOnWriteArrayList<>();
    private static final ConcurrentHashMap<String, ScopedMenu> scopedMenus = new ConcurrentHashMap<>();

    private static final class ScopedMenu {
        private final MenuItem item;

        ScopedMenu(MenuItem item) {
            this.item = item.whileRegistered(() -> scopedMenus.get(item.getPluginName()) == this);
        }
    }
    private static final Comparator<MenuItem> MENU_ITEM_ORDER = Comparator
            .comparing((MenuItem item) -> sortKey(item.getLabel()));

    private static I18n t() {
        return I18n.getInstance(OZTools.name);
    };

    public static void registerPluginMenu(MenuItem menu) {
        menuItems.add(menu);
    }

    /** Registers one menu for this plugin name and returns its idempotent cleanup action. */
    public static Runnable registerScopedPluginMenu(MenuItem menu) {
        ScopedMenu registration = new ScopedMenu(menu);
        String owner = menu.getPluginName();
        scopedMenus.put(owner, registration);
        return () -> scopedMenus.remove(owner, registration);
    }

    public static void showMainMenu(Player player) {
        InventoryOverlayPanel.showMenu(player);
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
        menuItemsCopy.add(MenuItem.iconKey("menu-privacy-shield",
                t().get("tc.menu.privacy", player), ToolsPrivacyOverlay::show));
        if (player.isAdmin()) menuItemsCopy.add(MenuItem.iconKey("menu-administration",
                t().get("tc.menu.privacyAdmin", player), DeclinedConsentAdminPanel::show));
        return menuItemsCopy;
    }

    static List<MenuItem> sortedPluginMenuItems() {
        List<MenuItem> sortedItems = new ArrayList<>(menuItems);
        for (ScopedMenu scopedMenu : scopedMenus.values()) sortedItems.add(scopedMenu.item);
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

        p.showRadialMenu(icons, labels, null, false, once(i -> {
            if (i < 0 || i >= visibleItems.size()) {
                p.hideRadialMenu(false);
                return;
            }
            visibleItems.get(i).getAction().onCall(p);
        }));
    }

    /**
     * The game can report a radial-menu selection more than once for the same
     * displayed menu. Menu actions can change persistent game state, so only
     * the first callback for one menu display may be dispatched.
     */
    static <T> Callback<T> once(Callback<T> action) {
        AtomicBoolean dispatched = new AtomicBoolean();
        return value -> {
            if (dispatched.compareAndSet(false, true)) {
                action.onCall(value);
            }
        };
    }

    private static String sortKey(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }
}
