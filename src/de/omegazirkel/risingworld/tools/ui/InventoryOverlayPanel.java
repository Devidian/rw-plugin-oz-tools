package de.omegazirkel.risingworld.tools.ui;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import de.omegazirkel.risingworld.OZTools;
import de.omegazirkel.risingworld.tools.ToolsPlayerPreferences;
import net.risingworld.api.Server;
import net.risingworld.api.assets.TextureAsset;
import net.risingworld.api.objects.Player;
import net.risingworld.api.ui.UILabel;
import net.risingworld.api.ui.UIScrollView;
import net.risingworld.api.ui.UITarget;
import net.risingworld.api.ui.style.Align;
import net.risingworld.api.ui.style.DisplayStyle;
import net.risingworld.api.ui.style.FlexDirection;
import net.risingworld.api.ui.style.Font;
import net.risingworld.api.ui.style.Justify;
import net.risingworld.api.ui.style.Pivot;
import net.risingworld.api.ui.style.Position;
import net.risingworld.api.ui.style.ScaleMode;
import net.risingworld.api.ui.style.TextAnchor;
import net.risingworld.api.ui.style.Unit;
import net.risingworld.api.ui.style.Wrap;

public class InventoryOverlayPanel extends OZUIElement {
    private static final String PLAYER_ATTRIBUTE = "tools.ui.inventoryOverlayPanel";
    private static final String MENU_ATTRIBUTE = "tools.ui.shortcutMenuPanel";
    private static final float WIDTH_WITH_LABEL = 75;
    private static final int LARGE_MENU_WIDTH_PERCENT = 95;
    private static final int ICONS_PER_ROW = 16;
    private static final int LABELS_PER_ROW = 12;
    private static final int OVERFLOW_ICONS_PER_ROW = 7;
    private static final int OVERFLOW_LABELS_PER_ROW = 5;
    private final AtomicBoolean activated = new AtomicBoolean();

    public static void show(Player player) {
        remove(player);
        List<MenuItem> buttons = PluginMenuManager.mainMenuItems(player);
        if (buttons.isEmpty()) {
            return;
        }
        InventoryOverlayPanel panel = new InventoryOverlayPanel(player, buttons, false);
        player.addUIElement(panel, UITarget.Inventory);
        player.setAttribute(PLAYER_ATTRIBUTE, panel);
    }

    public static void showMenu(Player player) {
        if (player == null) return;
        // Escape can close a modal without notifying the server. Discard the old
        // reference so a late removal cannot close the new menu.
        player.deleteAttribute(MENU_ATTRIBUTE);
        List<MenuItem> buttons = PluginMenuManager.mainMenuItems(player);
        if (buttons.isEmpty()) return;
        InventoryOverlayPanel panel = new InventoryOverlayPanel(player, buttons, true);
        player.addUIElement(panel, UITarget.Modal);
        player.setAttribute(MENU_ATTRIBUTE, panel);
    }

    public static void remove(Player player) {
        InventoryOverlayPanel panel = (InventoryOverlayPanel) player.getAttribute(PLAYER_ATTRIBUTE);
        if (panel != null) {
            player.removeUIElement(panel);
            player.deleteAttribute(PLAYER_ATTRIBUTE);
        }
    }

    public static boolean isVisible(Player player) {
        return player != null && player.getAttribute(PLAYER_ATTRIBUTE) instanceof InventoryOverlayPanel;
    }

    public static void refreshAllVisible() {
        Player[] players = Server.getAllPlayers();
        if (players == null) {
            return;
        }
        for (Player player : players) {
            if (isVisible(player)) {
                show(player);
            }
        }
    }

    private InventoryOverlayPanel(Player player, List<MenuItem> buttons, boolean modal) {
        setPivot(Pivot.UpperLeft);
        setSize(100, 100, true);
        setBackgroundColor(0, 0, 0, 0);
        setClickable(false);
        // The inventory target covers the item slots. A transparent element is
        // still pickable by default, even when it is not clickable.
        if (!modal) setPickable(false);

        boolean showLabel = ToolsPlayerPreferences.showInventoryShortcutLabels(player);
        int scale = modal ? ToolsPlayerPreferences.quickMenuScale(player) : 1;
        boolean largeMenu = modal && scale > 1;
        int singleRowCapacity = showLabel ? LABELS_PER_ROW : ICONS_PER_ROW;
        boolean overflow = !modal && buttons.size() > singleRowCapacity;
        int largeMenuColumns = 3;
        if (largeMenu && player.getScreenResolutionX() > 0) {
            int cardWidth = (int) ((showLabel ? WIDTH_WITH_LABEL : 52) + 8) * scale;
            int usableWidth = (int) (player.getScreenResolutionX() * LARGE_MENU_WIDTH_PERCENT / 100f * 0.99f) - 8;
            // Flex wrapping uses the full available width. The height must use
            // the same column count or it creates empty scrollable rows.
            largeMenuColumns = Math.max(1, usableWidth / cardWidth);
        }
        int perRow = largeMenu ? largeMenuColumns
                : overflow ? (showLabel ? OVERFLOW_LABELS_PER_ROW : OVERFLOW_ICONS_PER_ROW)
                : singleRowCapacity;
        int rowHeight = (showLabel ? 74 : 60) * scale;
        int rows = (buttons.size() + perRow - 1) / perRow;
        int contentHeight = rows * rowHeight + 8;
        int screenHeight = player.getScreenResolutionY() > 0 ? player.getScreenResolutionY() : 720;
        boolean scrollMenu = largeMenu && contentHeight + 8 > screenHeight * 0.85f;
        OZUIElement container = new OZUIElement();
        container.setPivot(scrollMenu ? Pivot.UpperLeft : modal ? Pivot.MiddleCenter : overflow ? Pivot.UpperLeft : Pivot.UpperCenter);
        if (scrollMenu) container.setPosition(0, 0, false);
        else if (modal) container.setPosition(50, 50, true);
        else if (overflow) {
            container.style.left.set(16, Unit.Pixel);
            container.style.top.set(19, Unit.Percent);
        } else container.setPosition(50, 80, true);
        // Leave room for padding and rounding inside the vertical scroll view.
        if (largeMenu) container.style.width.set(scrollMenu ? 99 : LARGE_MENU_WIDTH_PERCENT, Unit.Percent);
        else container.style.width.set(overflow ? (showLabel ? 423 : 428) : (showLabel ? 1004 : 968), Unit.Pixel);
        container.style.height.set(contentHeight, Unit.Pixel);
        container.style.position.set(Position.Absolute);
        container.style.display.set(DisplayStyle.Flex);
        container.style.alignContent.set(Align.FlexStart);
        container.style.alignItems.set(Align.FlexStart);
        container.style.justifyContent.set(Justify.Center);
        container.style.flexDirection.set(FlexDirection.Row);
        container.style.flexWrap.set(Wrap.Wrap);
        container.style.paddingTop.set(4);
        container.style.paddingBottom.set(4);
        container.style.paddingLeft.set(4);
        container.style.paddingRight.set(4);
        container.setBackgroundColor(0, 0, 0, 0);
        if (!modal) container.setPickable(false);

        for (MenuItem button : buttons) {
            container.addChild(buttonElement(player, button, modal, scale));
        }

        if (scrollMenu) {
            UIScrollView viewport = new UIScrollView(UIScrollView.ScrollViewMode.Vertical);
            viewport.setPivot(Pivot.MiddleCenter);
            viewport.setPosition(50, 50, true);
            viewport.setSize(LARGE_MENU_WIDTH_PERCENT, 90, true);
            viewport.setHorizontalScrollerVisibility(UIScrollView.ScrollerVisibility.Hidden);
            viewport.setVerticalScrollerVisibility(UIScrollView.ScrollerVisibility.Auto);
            viewport.addChild(container);
            addChild(viewport);
        } else addChild(container);
    }

    private AdvancedButton buttonElement(Player player, MenuItem registration, boolean modal, int scale) {
        boolean showLabel = ToolsPlayerPreferences.showInventoryShortcutLabels(player);
        AdvancedButton button = AdvancedButtonFactory.custom(new AdvancedButtonState(
                AdvancedBaseButton.State.DEFAULT, 0xD7AE5577, 0x141414AA, 0xE8DDC6FF,
                0xF2C766BB, 0x2A2419DD, "", event -> {
                    if (modal && player.getAttribute(MENU_ATTRIBUTE) != this) return;
                    if (!activated.compareAndSet(false, true)) return;
                    if (modal) {
                        player.deleteAttribute(MENU_ATTRIBUTE);
                        player.removeUIElement(this);
                        player.closeAllActiveUIWindows();
                        OZTools.runAfterModalClose(player, () -> registration.getAction().onCall(player));
                    } else {
                        remove(player);
                        player.hideInventory();
                        registration.getAction().onCall(player);
                    }
                }));
        button.setPivot(Pivot.UpperLeft);
        button.style.position.set(Position.Relative);
        button.style.width.set((showLabel ? WIDTH_WITH_LABEL : 52) * scale, Unit.Pixel);
        button.style.height.set((showLabel ? 52 + 14 : 52) * scale, Unit.Pixel);
        button.style.marginLeft.set(4 * scale);
        button.style.marginRight.set(4 * scale);
        button.style.marginTop.set(4 * scale);
        button.style.marginBottom.set(4 * scale);
        button.setHoverBorderWidth(1);
        button.setBorderEdgeRadius(4, false);

        // Resolve icon-key registrations for the current player. Calling the
        // keyless accessor here made all plugin shortcuts render without an
        // icon even though the radial menu resolved them correctly.
        TextureAsset icon = registration.getIcon(player);
        if (icon != null) {
            OZUIElement iconElement = new OZUIElement();
            iconElement.setPivot(showLabel ? Pivot.UpperCenter : Pivot.MiddleCenter);
            iconElement.style.position.set(Position.Absolute);
            iconElement.style.left.set(50, Unit.Percent);
            if (showLabel) {
                iconElement.style.top.set(4 * scale, Unit.Pixel);
            } else {
                iconElement.style.top.set(50, Unit.Percent);
            }
            iconElement.style.width.set(36 * scale, Unit.Pixel);
            iconElement.style.height.set(36 * scale, Unit.Pixel);
            iconElement.style.backgroundImage.set(icon);
            iconElement.style.backgroundImageScaleMode.set(ScaleMode.ScaleToFit);
            button.addChild(iconElement);
        }

        if (showLabel) {
            UILabel label = new UILabel(registration.getLabel());
            label.setPivot(Pivot.UpperCenter);
            label.style.position.set(Position.Absolute);
            label.style.left.set(50, Unit.Percent);
            label.style.bottom.set(scale, Unit.Pixel);
            label.style.width.set((WIDTH_WITH_LABEL - 4) * scale, Unit.Pixel);
            label.style.height.set(14 * scale, Unit.Pixel);
            label.setFont(Font.Default);
            label.setFontSize(9 * scale);
            label.setFontColor(0xE8DDC6FF);
            label.setTextAlign(TextAnchor.MiddleCenter);
            label.setTextWrap(false);
            button.addChild(label);
        }

        return button;
    }
}
