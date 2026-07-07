package de.omegazirkel.risingworld.tools.ui;

import java.util.List;

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

public class SharedIndicatorPanel extends OZUIElement {
    private static final String PLAYER_ATTRIBUTE = "tools.ui.sharedIndicatorPanel";

    public static void refresh(Player player) {
        if (player == null || !player.isConnected() || !player.isSpawned()) {
            remove(player);
            return;
        }
        if (InventoryOverlayPanel.isVisible(player)) {
            remove(player);
            return;
        }
        remove(player);
        List<SharedIndicator> indicators = SharedIndicators.visibleIndicators(player);
        if (indicators.isEmpty()) {
            return;
        }

        SharedIndicatorPanel panel = new SharedIndicatorPanel(player, indicators);
        player.addUIElement(panel, UITarget.HUD);
        player.setAttribute(PLAYER_ATTRIBUTE, panel);
    }

    public static void remove(Player player) {
        if (player == null) {
            return;
        }
        SharedIndicatorPanel panel = (SharedIndicatorPanel) player.getAttribute(PLAYER_ATTRIBUTE);
        if (panel != null) {
            player.removeUIElement(panel);
            player.deleteAttribute(PLAYER_ATTRIBUTE);
        }
    }

    private SharedIndicatorPanel(Player player, List<SharedIndicator> indicators) {
        setPivot(Pivot.MiddleCenter);
        style.position.set(Position.Absolute);
        style.left.set(50, Unit.Percent);
        style.top.set(50, Unit.Pixel);
        style.width.set(70, Unit.Percent);
        style.height.set(38, Unit.Pixel);
        setBackgroundColor(0, 0, 0, 0);
        setClickable(false);

        OZUIElement container = new OZUIElement();
        container.setPivot(Pivot.UpperLeft);
        container.style.position.set(Position.Absolute);
        container.style.left.set(22, Unit.Pixel);
        container.style.top.set(28, Unit.Pixel);
        container.style.width.set(indicators.size() * 30 + 10, Unit.Pixel);
        container.style.height.set(34, Unit.Pixel);
        container.style.display.set(DisplayStyle.Flex);
        container.style.alignContent.set(Align.Center);
        container.style.alignItems.set(Align.Center);
        container.style.justifyContent.set(Justify.FlexStart);
        container.style.flexDirection.set(FlexDirection.Row);
        container.style.flexWrap.set(Wrap.NoWrap);
        container.style.paddingLeft.set(4);
        container.style.paddingRight.set(4);
        container.style.paddingTop.set(4);
        container.style.paddingBottom.set(4);
        container.setBackgroundColor(0x00000000);
        container.setBorderEdgeRadius(4, false);

        for (SharedIndicator indicator : indicators) {
            OZUIElement element = indicatorElement(player, indicator);
            if (element != null) {
                container.addChild(element);
            }
        }

        addChild(container);
    }

    private OZUIElement indicatorElement(Player player, SharedIndicator indicator) {
        TextureAsset icon = AssetManager.getIcon(player, indicator.getIconKey());
        if (icon == null) {
            return null;
        }

        OZUIElement element = new OZUIElement();
        element.setPivot(Pivot.UpperLeft);
        element.style.width.set(24, Unit.Pixel);
        element.style.height.set(24, Unit.Pixel);
        element.style.marginLeft.set(3);
        element.style.marginRight.set(3);
        element.setPadding(5);
        element.setBorderEdgeRadius(4, false);
        element.style.backgroundImage.set(icon);
        element.style.backgroundImageScaleMode.set(ScaleMode.ScaleToFit);
        element.setBackgroundColor(0x00000044);
        element.setClickable(false);
        return element;
    }
}
