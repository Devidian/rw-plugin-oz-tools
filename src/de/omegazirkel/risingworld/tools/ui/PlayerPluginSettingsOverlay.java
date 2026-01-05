package de.omegazirkel.risingworld.tools.ui;

import java.util.concurrent.ConcurrentHashMap;

import de.omegazirkel.risingworld.OZTools;
import de.omegazirkel.risingworld.tools.I18n;
import net.risingworld.api.objects.Player;
import net.risingworld.api.ui.UIElement;
import net.risingworld.api.ui.UILabel;
import net.risingworld.api.ui.style.Pivot;

public class PlayerPluginSettingsOverlay extends OverlayBackPanel {
    private static I18n t() {
        return I18n.getInstance(OZTools.name);
    }

    private static ConcurrentHashMap<String, PlayerPluginSettings> playerPluginSettings = new ConcurrentHashMap<>();

    private UIElement navSidebar;
    private UIElement content;

    private String selectedPlugin;

    public PlayerPluginSettingsOverlay(Player uiPlayer) {
        super(uiPlayer);

        // Main container
        UIElement containerPanel = new UIElement();
        containerPanel.setPivot(Pivot.MiddleCenter);
        containerPanel.setSize(80, 80, true);
        containerPanel.setPosition(50, 50, true);
        containerPanel.setBackgroundColor(0, 0, 0, 0.8f);
        containerPanel.setBorderColor(0.9f, 0.9f, 0.9f, 1.0f);
        containerPanel.setBorder(2);
        this.addChild(containerPanel);

        // navigation bar
        this.navSidebar = new UIElement();
        this.navSidebar.setPivot(Pivot.UpperLeft);
        this.navSidebar.setSize(15, 100, true);
        this.navSidebar.style.borderRightWidth.set(2);
        this.navSidebar.style.borderRightColor.set(0.9f, 0.9f, 0.9f, 1.0f);
        containerPanel.addChild(this.navSidebar);

        // content panel
        this.content = new UIElement();
        this.content.setPivot(Pivot.UpperLeft);
        this.content.setPosition(15, 0, true);
        this.content.setSize(85, 100, true);
        containerPanel.addChild(this.content);

        // felect first plugin by default
        selectedPlugin = playerPluginSettings.keys().nextElement();
        updateUI();
    }

    public void updateUI() {
        // 1. clear navigation bar
        navSidebar.removeAllChilds();
        // fill navigation bar for every playerPluginSettings
        for (String pluginLabel : playerPluginSettings.keySet()) {
            OZUIElement navButton = new OZUIElement();
            navButton.setClickable(true);
            navButton.setPivot(Pivot.UpperLeft);
            navButton.setSize(100, 5, true);
            navButton.setBackgroundColor(0.2f, 0.2f, 0.2f, 1.0f);
            navButton.setHoverBackgroundColor(0x30303090);
            navButton.style.borderBottomWidth.set(1);
            navButton.style.borderBottomColor.set(0.7f, 0.7f, 0.7f, 1.0f);
            UILabel btnLabel = new UILabel(pluginLabel);
            btnLabel.setPivot(Pivot.MiddleCenter);
            btnLabel.setPosition(50, 50, true);
            navButton.setClickAction(event -> {
                selectedPlugin = pluginLabel;
                updateUI();
            });
            navButton.addChild(btnLabel);
            navSidebar.addChild(navButton);
        }
        // add close button at the bottom
        OZUIElement closeButton = new OZUIElement();
        closeButton.setClickable(true);
        closeButton.setPivot(Pivot.LowerLeft);
        closeButton.setSize(100, 5, true);
        closeButton.setPosition(0, 100, true);
        closeButton.setBackgroundColor(0.2f, 0.2f, 0.2f, 1.0f);
        closeButton.setHoverBackgroundColor(0x30303090);
        UILabel btnLabel = new UILabel(t().get("TC_BTN_CLOSE", uiPlayer));
        btnLabel.setPivot(Pivot.MiddleCenter);
        btnLabel.setPosition(50, 50, true);
        closeButton.setClickAction(event -> {
            uiPlayer.removeUIElement(this);
            CursorManager.hide(uiPlayer);
        });
        closeButton.addChild(btnLabel);
        navSidebar.addChild(closeButton);

        // clear content
        content.removeAllChilds();
        // select content
        PlayerPluginSettings pps = playerPluginSettings.get(selectedPlugin);
        if (pps != null) {
            BasePlayerPluginSettingsPanel settingsContent = pps.createPlayerPluginSettingsUIElement(uiPlayer);
            settingsContent.updateUI();
            content.addChild(settingsContent);
        }
    }

    public static void registerPlayerPluginSettings(PlayerPluginSettings pps) {
        playerPluginSettings.put(pps.pluginLabel, pps);
    }
}
