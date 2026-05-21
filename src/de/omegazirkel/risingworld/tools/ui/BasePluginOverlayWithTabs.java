package de.omegazirkel.risingworld.tools.ui;

import net.risingworld.api.callbacks.Callback;
import net.risingworld.api.objects.Player;
import net.risingworld.api.ui.UILabel;
import net.risingworld.api.ui.style.Font;
import net.risingworld.api.ui.style.Pivot;
import net.risingworld.api.ui.style.TextAnchor;

public abstract class BasePluginOverlayWithTabs extends BasePluginOverlay {

    protected enum Tab {
        DEFAULT
    };

    protected Tab activeTab = null;
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
        defaultTab = tab(t().get("TC_UI_PLACEHOLDER_TAB_DEFAULT", uiPlayer), 24, 86,
        180, () -> {
        activeTab = Tab.DEFAULT;
        rebuild();
        });
        // panel.addChild(defaultTab);
        applyTabStyles();
    }

    protected void applyTabStyles() {
        styleTab(defaultTab, activeTab == Tab.DEFAULT);
    }

    protected void styleTab(OZUIElement tab, boolean active) {
        styleTab(tab, active, false);
    }

    protected OZUIElement tab(String text, float x, float y, float width, Runnable action) {
        OZUIElement tab = new OZUIElement();
        tab.setPivot(Pivot.UpperLeft);
        tab.setPosition(x, y, false);
        tab.setSize(width, 38, false);
        tab.setBorder(1);
        tab.setBorderEdgeRadius(4, false);
        tab.setClickable(true);
        tab.setClickAction(event -> action.run());

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
