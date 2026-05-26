package de.omegazirkel.risingworld.tools.ui;

import java.util.List;

import net.risingworld.api.Server;
import net.risingworld.api.assets.TextureAsset;
import net.risingworld.api.objects.Player;
import net.risingworld.api.ui.UITarget;
import net.risingworld.api.ui.style.Align;
import net.risingworld.api.ui.style.DisplayStyle;
import net.risingworld.api.ui.style.FlexDirection;
import net.risingworld.api.ui.style.Justify;
import net.risingworld.api.ui.style.Pivot;
import net.risingworld.api.ui.style.Position;
import net.risingworld.api.ui.style.ScaleMode;
import net.risingworld.api.ui.style.Unit;
import net.risingworld.api.ui.style.Wrap;

public class InventoryOverlayPanel extends OZUIElement {
    private static final String PLAYER_ATTRIBUTE = "tools.ui.inventoryOverlayPanel";

    public static void show(Player player) {
        remove(player);
        List<MenuItem> buttons = PluginMenuManager.mainMenuItems(player);
        if (buttons.isEmpty()) {
            return;
        }
        InventoryOverlayPanel panel = new InventoryOverlayPanel(player, buttons);
        player.addUIElement(panel, UITarget.Inventory);
        player.setAttribute(PLAYER_ATTRIBUTE, panel);
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

    private InventoryOverlayPanel(Player player, List<MenuItem> buttons) {
        setPivot(Pivot.UpperCenter);
        setSize(70, 10, true);
        setPosition(50, 80, true);
        setBackgroundColor(0, 0, 0, 0);
        setClickable(false);

        OZUIElement container = new OZUIElement();
        container.setPivot(Pivot.UpperCenter);
        container.setPosition(50, 0, true);
        container.style.width.set(58, Unit.Percent);
        container.style.height.set(112, Unit.Pixel);
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

        for (MenuItem button : buttons) {
            container.addChild(buttonElement(player, button));
        }

        addChild(container);
    }

    private OZUIElement buttonElement(Player player, MenuItem registration) {
        OZUIElement button = new OZUIElement();
        button.setPivot(Pivot.UpperLeft);
        button.style.width.set(38, Unit.Pixel);
        button.style.height.set(38, Unit.Pixel);
        button.style.marginLeft.set(4);
        button.style.marginRight.set(4);
        button.style.marginTop.set(4);
        button.style.marginBottom.set(4);
        button.setClickable(true);
        button.setBackgroundColor(0x141414AA);
        button.setHoverBackgroundColor(0x2A2419DD);
        button.setBorder(1);
        button.setBorderColor(0xD7AE5577);
        button.setHoverBorderColor(0xF2C766BB);
        button.setHoverBorderWidth(1);
        button.setBorderEdgeRadius(4, false);
        button.setClickAction(event -> {
            remove(player);
            player.hideInventory();
            registration.getAction().onCall(player);
        });

        TextureAsset icon = registration.getIcon();
        if (icon != null) {
            OZUIElement iconElement = new OZUIElement();
            iconElement.setPivot(Pivot.MiddleCenter);
            iconElement.style.position.set(Position.Absolute);
            iconElement.style.left.set(50, Unit.Percent);
            iconElement.style.top.set(50, Unit.Percent);
            iconElement.style.width.set(24, Unit.Pixel);
            iconElement.style.height.set(24, Unit.Pixel);
            iconElement.style.backgroundImage.set(icon);
            iconElement.style.backgroundImageScaleMode.set(ScaleMode.ScaleToFit);
            button.addChild(iconElement);
        }

        return button;
    }
}
