package de.omegazirkel.risingworld.tools.ui;

import net.risingworld.api.callbacks.Callback;
import net.risingworld.api.objects.Player;
import net.risingworld.api.ui.UILabel;
import net.risingworld.api.ui.style.Align;
import net.risingworld.api.ui.style.DisplayStyle;
import net.risingworld.api.ui.style.FlexDirection;
import net.risingworld.api.ui.style.Font;
import net.risingworld.api.ui.style.Justify;
import net.risingworld.api.ui.style.Pivot;
import net.risingworld.api.ui.style.Position;
import net.risingworld.api.ui.style.TextAnchor;
import net.risingworld.api.ui.style.Unit;
import net.risingworld.api.ui.style.Wrap;

public abstract class BasePluginOverlayWithTabs extends BasePluginOverlay {
    private static final float TAB_BAR_LEFT_PIXELS = 24f;
    private static final float TAB_BAR_TOP_PIXELS = 86f;
    private static final float TAB_BAR_HEIGHT_PIXELS = 38f;

    protected enum Tab {
        DEFAULT
    };

    protected Tab activeTab = null;
    protected OZUIElement tabContainer;
    private OZUIElement defaultTab;

    public BasePluginOverlayWithTabs(Player player, Callback<Player> onClose) {
        super(player, onClose);
    }

    @Override
    protected void rebuild() {
        super.rebuild();
        setupTabs();
    }

    protected void setupTabs() {
        setupTabContainer();
        if (activeTab == null) {
            activeTab = Tab.DEFAULT;
        }
        defaultTab = addTab(t().get("TC_UI_PLACEHOLDER_TAB_DEFAULT", uiPlayer), 180, activeTab == Tab.DEFAULT, () -> {
            activeTab = Tab.DEFAULT;
            rebuild();
        });
    }

    protected void applyTabStyles() {
        styleTab(defaultTab, activeTab == Tab.DEFAULT);
    }

    protected void styleTab(OZUIElement tab, boolean active) {
        styleTab(tab, active, false);
    }

    protected void setupTabContainer() {
        tabContainer = new OZUIElement();
        tabContainer.setPivot(Pivot.UpperLeft);
        tabContainer.style.position.set(Position.Absolute);
        tabContainer.style.left.set(TAB_BAR_LEFT_PIXELS, Unit.Pixel);
        tabContainer.style.top.set(TAB_BAR_TOP_PIXELS, Unit.Pixel);
        tabContainer.style.right.set(TAB_BAR_LEFT_PIXELS, Unit.Pixel);
        tabContainer.style.height.set(TAB_BAR_HEIGHT_PIXELS, Unit.Pixel);
        tabContainer.style.display.set(DisplayStyle.Flex);
        tabContainer.style.alignContent.set(Align.FlexStart);
        tabContainer.style.alignItems.set(Align.FlexStart);
        tabContainer.style.justifyContent.set(Justify.FlexStart);
        tabContainer.style.flexDirection.set(FlexDirection.Row);
        tabContainer.style.flexWrap.set(Wrap.NoWrap);
        tabContainer.setBackgroundColor(0, 0, 0, 0);
        panel.addChild(tabContainer);
    }

    protected OZUIElement addTab(String text, float width, boolean active, Runnable action) {
        return addTab(text, width, active, false, action);
    }

    protected OZUIElement addTab(String text, float width, boolean active, boolean adminTab, Runnable action) {
        if (tabContainer == null) {
            setupTabContainer();
        }
        OZUIElement tab = tab(text, width, action);
        styleTab(tab, active, adminTab);
        tabContainer.addChild(tab);
        return tab;
    }

    protected OZUIElement tab(String text, float x, float y, float width, Runnable action) {
        OZUIElement tab = tab(text, width, action);
        tab.setPosition(x, y, false);
        return tab;
    }

    protected OZUIElement tab(String text, float width, Runnable action) {
        OZUIElement tab = new OZUIElement();
        tab.setPivot(Pivot.UpperLeft);
        tab.setSize(width, 38, false);
        tab.setBorder(1);
        tab.setBorderEdgeRadius(4, false);
        tab.setClickable(true);
        tab.setClickAction(event -> {
            if (action != null) {
                action.run();
            }
        });

        UILabel label = new UILabel(text);
        label.setPivot(Pivot.MiddleCenter);
        label.setPosition(50, 50, true);
        label.setSize(100, 100, true);
        label.setFont(Font.DefaultBold);
        label.setFontSize(15);
        label.setTextAlign(TextAnchor.MiddleCenter);
        tab.addChild(label);
        return tab;
    }

    protected void styleTab(OZUIElement tab, boolean active, boolean adminTab) {
        if (tab == null) {
            return;
        }
        if (active && adminTab) {
            tab.setBackgroundColor(0.19f, 0.10f, 0.03f, 0.92f);
            tab.setBorderColor(1.0f, 0.48f, 0.12f, 0.86f);
        } else if (active) {
            tab.setBackgroundColor(0.08f, 0.08f, 0.08f, 0.82f);
            tab.setBorderColor(0.95f, 0.75f, 0.25f, 0.74f);
        } else if (adminTab) {
            tab.setBackgroundColor(0.16f, 0.07f, 0.03f, 0.58f);
            tab.setBorderColor(1.0f, 0.48f, 0.12f, 0.42f);
        } else {
            tab.setBackgroundColor(0.10f, 0.10f, 0.10f, 0.38f);
            tab.setBorderColor(0.95f, 0.75f, 0.25f, 0.24f);
        }
    }
}
