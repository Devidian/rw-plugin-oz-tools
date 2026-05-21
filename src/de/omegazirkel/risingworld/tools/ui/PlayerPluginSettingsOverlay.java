package de.omegazirkel.risingworld.tools.ui;

import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import de.omegazirkel.risingworld.OZTools;
import de.omegazirkel.risingworld.tools.I18n;
import de.omegazirkel.risingworld.tools.settings.PlayerPluginAdminSettings;
import net.risingworld.api.objects.Player;
import net.risingworld.api.ui.UIElement;
import net.risingworld.api.ui.UILabel;
import net.risingworld.api.ui.style.Pivot;
import net.risingworld.api.ui.style.TextAnchor;
import net.risingworld.api.ui.style.Unit;

public class PlayerPluginSettingsOverlay extends OverlayBackPanel {
    private static final String TAB_SETTINGS = "settings";
    private static final String TAB_DATA = "data";
    private static final String TAB_PLUGIN_SETTINGS = "pluginSettings";

    private static I18n t() {
        return I18n.getInstance(OZTools.name);
    }

    private static ConcurrentHashMap<String, PlayerPluginSettings> playerPluginSettings = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, PlayerPluginData> playerPluginData = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, PlayerPluginAdminSettings> playerPluginAdminSettings = new ConcurrentHashMap<>();

    private UIElement navSidebar;
    private UIElement tabBar;
    private UIElement content;

    private String selectedPlugin;
    private String selectedTab = TAB_SETTINGS;

    public PlayerPluginSettingsOverlay(Player uiPlayer) {
        super(uiPlayer);

        // Main container
        UIElement containerPanel = new UIElement();
        containerPanel.setPivot(Pivot.MiddleCenter);
        containerPanel.setSize(80, 78, true);
        containerPanel.setPosition(50, 50, true);
        containerPanel.setBackgroundColor(0x10100EE8);
        containerPanel.setBorderColor(0xB8913CFF);
        containerPanel.setBorder(1);
        this.addChild(containerPanel);

        // navigation bar
        this.navSidebar = new UIElement();
        this.navSidebar.setPivot(Pivot.UpperLeft);
        this.navSidebar.setSize(22, 100, true);
        this.navSidebar.style.paddingTop.set(8);
        this.navSidebar.style.paddingLeft.set(8);
        this.navSidebar.style.paddingRight.set(8);
        this.navSidebar.style.borderRightWidth.set(1);
        this.navSidebar.style.borderRightColor.set(0x8A6A2DFF);
        containerPanel.addChild(this.navSidebar);

        // content panel
        UIElement contentPanel = new UIElement();
        contentPanel.setPivot(Pivot.UpperLeft);
        contentPanel.setPosition(22, 0, true);
        contentPanel.setSize(78, 100, true);
        containerPanel.addChild(contentPanel);

        this.tabBar = new UIElement();
        this.tabBar.setPivot(Pivot.UpperLeft);
        this.tabBar.style.left.set(4, Unit.Percent);
        this.tabBar.style.top.set(8, Unit.Pixel);
        this.tabBar.style.width.set(92, Unit.Percent);
        this.tabBar.style.height.set(42, Unit.Pixel);
        contentPanel.addChild(this.tabBar);

        this.content = new UIElement();
        this.content.setPivot(Pivot.UpperLeft);
        this.content.style.top.set(50, Unit.Pixel);
        this.content.setSize(100, 92, true);
        contentPanel.addChild(this.content);

        selectedPlugin = firstPluginLabel();
        updateUI();
    }

    public void updateUI() {
        // 1. clear navigation bar
        navSidebar.removeAllChilds();
        tabBar.removeAllChilds();
        if (selectedPlugin == null || !pluginLabels().contains(selectedPlugin)) {
            selectedPlugin = firstPluginLabel();
        }
        if (TAB_PLUGIN_SETTINGS.equals(selectedTab) && !canShowPluginSettingsTab(selectedPlugin)) {
            selectedTab = TAB_SETTINGS;
        }
        // fill navigation bar for every playerPluginSettings
        for (String pluginLabel : pluginLabels()) {
            OZUIElement navButton = new OZUIElement();
            navButton.setClickable(true);
            navButton.setPivot(Pivot.UpperLeft);
            navButton.style.width.set(100, Unit.Percent);
            navButton.style.height.set(38, Unit.Pixel);
            navButton.setBackgroundColor(pluginLabel.equals(selectedPlugin) ? 0x3A2D18D8 : 0x181713C8);
            navButton.setHoverBackgroundColor(0x2A2419E8);
            navButton.setPadding(8);
            navButton.style.marginTop.set(3);
            navButton.style.marginBottom.set(3);
            navButton.setBorder(1);
            navButton.setBorderColor(pluginLabel.equals(selectedPlugin) ? 0xD7AE55FF : 0x5E4A25FF);
            navButton.setHoverBorderColor(0xD7AE55FF);
            navButton.style.borderBottomWidth.set(1);
            navButton.style.borderBottomColor.set(0x5E4A25FF);
            UILabel btnLabel = new UILabel(pluginLabel);
            btnLabel.setPivot(Pivot.MiddleLeft);
            btnLabel.setPosition(8, 50, true);
            btnLabel.setSize(60, 24, true);
            btnLabel.setFontSize(13);
            btnLabel.setFontColor(0xF4F0E6FF);
            btnLabel.setTextAlign(TextAnchor.MiddleLeft);
            UILabel versionLabel = new UILabel(formatVersion(pluginLabel));
            versionLabel.setPivot(Pivot.MiddleRight);
            versionLabel.setPosition(96, 50, true);
            versionLabel.setSize(32, 22, true);
            versionLabel.setFontSize(12);
            versionLabel.setFontColor(0xF2C766FF);
            versionLabel.setTextAlign(TextAnchor.MiddleRight);
            navButton.setClickAction(event -> {
                selectedPlugin = pluginLabel;
                updateUI();
            });
            navButton.addChild(btnLabel);
            navButton.addChild(versionLabel);
            navSidebar.addChild(navButton);
        }
        // add close button at the bottom
        OZUIElement closeButton = new OZUIElement();
        closeButton.setClickable(true);
        closeButton.setPivot(Pivot.LowerLeft);
        closeButton.style.width.set(100, Unit.Percent);
        closeButton.style.height.set(38, Unit.Pixel);
        closeButton.setPosition(0, 100, true);
        closeButton.setBackgroundColor(0x201B13D8);
        closeButton.setHoverBackgroundColor(0x342915E8);
        closeButton.setBorder(1);
        closeButton.setBorderColor(0x8A6A2DFF);
        closeButton.setHoverBorderColor(0xD7AE55FF);
        UILabel btnLabel = new UILabel(t().get("TC_BTN_CLOSE", uiPlayer));
        btnLabel.setPivot(Pivot.MiddleCenter);
        btnLabel.setPosition(50, 50, true);
        btnLabel.setFontColor(0xF4F0E6FF);
        btnLabel.setTextAlign(TextAnchor.MiddleCenter);
        closeButton.setClickAction(event -> {
            uiPlayer.removeUIElement(this);
            CursorManager.hide(uiPlayer);
        });
        closeButton.addChild(btnLabel);
        navSidebar.addChild(closeButton);

        addTabButton(TAB_SETTINGS, t().get("TC_TAB_SETTINGS", uiPlayer), 0);
        addTabButton(TAB_DATA, t().get("TC_TAB_DATA", uiPlayer), 150);
        if (canShowPluginSettingsTab(selectedPlugin)) {
            addTabButton(TAB_PLUGIN_SETTINGS, t().get("TC_TAB_PLUGIN_SETTINGS", uiPlayer), 300);
        }

        // clear content
        content.removeAllChilds();
        // select content
        if (TAB_PLUGIN_SETTINGS.equals(selectedTab)) {
            PlayerPluginAdminSettings ppas = playerPluginAdminSettings.get(selectedPlugin);
            if (ppas != null) {
                AdminPluginSettingsPanel pluginSettingsContent = new AdminPluginSettingsPanel(uiPlayer, ppas,
                        this::updateUI);
                pluginSettingsContent.updateUI();
                content.addChild(pluginSettingsContent);
            }
        } else if (TAB_DATA.equals(selectedTab)) {
            PlayerPluginData ppd = playerPluginData.get(selectedPlugin);
            if (ppd != null) {
                BasePlayerPluginDataPanel dataContent = ppd.createPlayerPluginDataUIElement(uiPlayer);
                dataContent.updateUI();
                content.addChild(dataContent);
            } else {
                BasePlayerPluginDataPanel emptyContent = emptyDataContent(selectedPlugin);
                emptyContent.updateUI();
                content.addChild(emptyContent);
            }
        } else {
            PlayerPluginSettings pps = playerPluginSettings.get(selectedPlugin);
            if (pps == null) {
                content.addChild(emptySettingsContent(selectedPlugin));
                return;
            }
            BasePlayerPluginSettingsPanel settingsContent = pps.createPlayerPluginSettingsUIElement(uiPlayer);
            settingsContent.updateUI();
            content.addChild(settingsContent);
        }
    }

    public static void registerPlayerPluginSettings(PlayerPluginSettings pps) {
        playerPluginSettings.put(pps.pluginLabel, pps);
    }

    public static void registerPlayerPluginData(PlayerPluginData ppd) {
        playerPluginData.put(ppd.pluginLabel, ppd);
    }

    public static void registerPlayerPluginAdminSettings(PlayerPluginAdminSettings ppas) {
        playerPluginAdminSettings.put(ppas.pluginLabel, ppas);
    }

    private void addTabButton(String tab, String label, int x) {
        boolean active = tab.equals(selectedTab);
        OZUIElement tabButton = new OZUIElement();
        tabButton.setClickable(true);
        tabButton.setPivot(Pivot.UpperLeft);
        tabButton.setPosition(x, 0, false);
        tabButton.setSize(140, 34, false);
        tabButton.setBackgroundColor(active ? 0x3A2D18D8 : 0x181713C8);
        tabButton.setHoverBackgroundColor(0x2A2419E8);
        tabButton.setBorder(1);
        tabButton.setBorderColor(active ? 0xD7AE55FF : 0x5E4A25FF);
        tabButton.setHoverBorderColor(0xD7AE55FF);
        UILabel tabLabel = new UILabel(label);
        tabLabel.setPivot(Pivot.MiddleCenter);
        tabLabel.setPosition(50, 50, true);
        tabLabel.setFontSize(13);
        tabLabel.setFontColor(active ? 0xF2C766FF : 0xF4F0E6FF);
        tabLabel.setTextAlign(TextAnchor.MiddleCenter);
        tabButton.setClickAction(event -> {
            selectedTab = tab;
            updateUI();
        });
        tabButton.addChild(tabLabel);
        tabBar.addChild(tabButton);
    }

    private BasePlayerPluginDataPanel emptyDataContent(String pluginLabel) {
        return new BasePlayerPluginDataPanel(uiPlayer, pluginLabel) {
            @Override
            protected void redrawContent() {
                flexWrapper.removeAllChilds();
                flexWrapper.addChild(defaultEmptyStateLabel());
            }
        };
    }

    private UILabel emptySettingsContent(String pluginLabel) {
        UILabel label = new UILabel(t().get("TC_SETTINGS_EMPTY", uiPlayer).replace("PH_PLUGIN_NAME", pluginLabel));
        label.setPivot(Pivot.MiddleCenter);
        label.setPosition(50, 50, true);
        label.setFontSize(16);
        label.setFontColor(0xC8C0B2FF);
        label.setTextAlign(TextAnchor.MiddleCenter);
        return label;
    }

    private Set<String> pluginLabels() {
        Set<String> labels = new TreeSet<>();
        labels.addAll(playerPluginSettings.keySet());
        labels.addAll(playerPluginData.keySet());
        labels.addAll(playerPluginAdminSettings.keySet());
        return labels;
    }

    private String firstPluginLabel() {
        Set<String> labels = pluginLabels();
        return labels.isEmpty() ? null : labels.iterator().next();
    }

    private String formatVersion(String pluginLabel) {
        String pluginVersion = null;
        PlayerPluginSettings pps = playerPluginSettings.get(pluginLabel);
        if (pps != null) {
            pluginVersion = pps.pluginVersion;
        } else {
            PlayerPluginData ppd = playerPluginData.get(pluginLabel);
            if (ppd != null) {
                pluginVersion = ppd.pluginVersion;
            } else {
                PlayerPluginAdminSettings ppas = playerPluginAdminSettings.get(pluginLabel);
                if (ppas != null) {
                    pluginVersion = ppas.pluginVersion;
                }
            }
        }
        if (pluginVersion == null || pluginVersion.isBlank()) {
            return "";
        }
        return "v" + pluginVersion;
    }

    private boolean canShowPluginSettingsTab(String pluginLabel) {
        return uiPlayer.isAdmin() && pluginLabel != null && playerPluginAdminSettings.containsKey(pluginLabel);
    }
}
