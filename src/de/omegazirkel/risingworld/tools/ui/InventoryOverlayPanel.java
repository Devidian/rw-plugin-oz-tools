package de.omegazirkel.risingworld.tools.ui;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import de.omegazirkel.risingworld.OZTools;
import de.omegazirkel.risingworld.tools.ToolsPlayerPreferences;
import net.risingworld.api.Server;
import net.risingworld.api.assets.TextureAsset;
import net.risingworld.api.objects.Player;
import net.risingworld.api.ui.UILabel;
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
    private static final int ICONS_PER_ROW = 16;
    private static final int LABELS_PER_ROW = 12;
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
        int perRow = showLabel ? LABELS_PER_ROW : ICONS_PER_ROW;
        int rowHeight = showLabel ? 74 : 60;
        int rows = (buttons.size() + perRow - 1) / perRow;
        OZUIElement container = new OZUIElement();
        container.setPivot(modal ? Pivot.MiddleCenter : buttons.size() > perRow ? Pivot.UpperLeft : Pivot.UpperCenter);
        if (modal) container.setPosition(50, 50, true);
        else if (buttons.size() > perRow) {
            container.style.left.set(16, Unit.Pixel);
            container.style.top.set(80, Unit.Percent);
        } else container.setPosition(50, 80, true);
        container.style.width.set(showLabel ? 1004 : 968, Unit.Pixel);
        container.style.height.set(rows * rowHeight + 8, Unit.Pixel);
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
            container.addChild(buttonElement(player, button, modal));
        }

        addChild(container);
    }

    private AdvancedButton buttonElement(Player player, MenuItem registration, boolean modal) {
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
        button.style.width.set(showLabel ? WIDTH_WITH_LABEL : 52, Unit.Pixel);
        button.style.height.set(showLabel ? 52 + 14 : 52, Unit.Pixel);
        button.style.marginLeft.set(4);
        button.style.marginRight.set(4);
        button.style.marginTop.set(4);
        button.style.marginBottom.set(4);
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
                iconElement.style.top.set(4, Unit.Pixel);
            } else {
                iconElement.style.top.set(50, Unit.Percent);
            }
            iconElement.style.width.set(36, Unit.Pixel);
            iconElement.style.height.set(36, Unit.Pixel);
            iconElement.style.backgroundImage.set(icon);
            iconElement.style.backgroundImageScaleMode.set(ScaleMode.ScaleToFit);
            button.addChild(iconElement);
        }

        if (showLabel) {
            UILabel label = new UILabel(registration.getLabel());
            label.setPivot(Pivot.UpperCenter);
            label.style.position.set(Position.Absolute);
            label.style.left.set(50, Unit.Percent);
            label.style.bottom.set(1, Unit.Pixel);
            label.style.width.set(WIDTH_WITH_LABEL - 4, Unit.Pixel);
            label.style.height.set(14, Unit.Pixel);
            label.setFont(Font.Default);
            label.setFontSize(9);
            label.setFontColor(0xE8DDC6FF);
            label.setTextAlign(TextAnchor.MiddleCenter);
            label.setTextWrap(false);
            button.addChild(label);
        }

        return button;
    }
}
