package de.omegazirkel.risingworld.tools.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import de.omegazirkel.risingworld.OZTools;
import de.omegazirkel.risingworld.tools.I18n;
import de.omegazirkel.risingworld.tools.PluginUpdateService;
import de.omegazirkel.risingworld.tools.settings.PlayerPluginAdminSettings;
import net.risingworld.api.objects.Player;
import net.risingworld.api.ui.UIElement;
import net.risingworld.api.ui.UILabel;
import net.risingworld.api.ui.UIScrollView;
import net.risingworld.api.ui.UIScrollView.ScrollViewMode;
import net.risingworld.api.ui.style.Font;
import net.risingworld.api.ui.style.Pivot;
import net.risingworld.api.ui.style.Position;
import net.risingworld.api.ui.style.TextAnchor;
import net.risingworld.api.ui.style.Unit;

public class PlayerPluginSettingsOverlay extends OverlayBackPanel {
    private static final String TAB_SETTINGS = "settings";
    private static final String TAB_DATA = "data";
    private static final String TAB_RELEASE_NOTES = "releaseNotes";
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
        if (TAB_RELEASE_NOTES.equals(selectedTab) && !canShowReleaseNotesTab(selectedPlugin)) selectedTab = TAB_SETTINGS;
        // fill navigation bar for every playerPluginSettings
        for (String pluginLabel : pluginLabels()) {
            AdvancedButton navButton = AdvancedButtonFactory.custom(new AdvancedButtonState(
                    AdvancedBaseButton.State.DEFAULT,
                    pluginLabel.equals(selectedPlugin) ? 0xD7AE55FF : 0x5E4A25FF,
                    pluginLabel.equals(selectedPlugin) ? 0x3A2D18D8 : 0x181713C8,
                    0xF4F0E6FF, 0xD7AE55FF, 0x2A2419E8, "", null));
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
            versionLabel.setFontColor(versionColor(pluginLabel));
            versionLabel.setTextAlign(TextAnchor.MiddleRight);
            navButton.setClickAction(event -> {
                selectedPlugin = pluginLabel;
                if (!playerPluginSettings.containsKey(pluginLabel) && canShowReleaseNotesTab(pluginLabel)) {
                    selectedTab = TAB_RELEASE_NOTES;
                }
                updateUI();
            });
            navButton.addChild(btnLabel);
            navButton.addChild(versionLabel);
            navSidebar.addChild(navButton);
        }
        if (uiPlayer.isAdmin()) {
            List<String> pendingUpdates = pendingUpdates();
            if (pendingUpdates.size() >= 2) {
                AdvancedButton updateAllButton = AdvancedButtonFactory.danger(t().get("TC_PLUGIN_UPDATE_ALL_ACTION", uiPlayer), event ->
                        showAllUpdatesConfirmation(pendingUpdates));
                updateAllButton.setPivot(Pivot.LowerLeft);
                updateAllButton.style.position.set(Position.Absolute);
                updateAllButton.style.left.set(0, Unit.Pixel);
                updateAllButton.style.width.set(100, Unit.Percent);
                updateAllButton.style.height.set(38, Unit.Pixel);
                updateAllButton.setPosition(0, 91.2f, true);
                updateAllButton.setBackgroundColor(0x7A3018E8);
                updateAllButton.setHoverBackgroundColor(0xA84722FF);
                updateAllButton.setBorder(1);
                updateAllButton.setBorderColor(0xD7AE55FF);
                updateAllButton.setHoverBorderColor(0xF2C766FF);
                navSidebar.addChild(updateAllButton);
            }
            AdvancedButton checkUpdatesButton = AdvancedButtonFactory.defaultButton(t().get("TC_PLUGIN_UPDATE_CHECK_ACTION", uiPlayer),
                    event -> OZTools.checkPluginUpdates(uiPlayer, this::updateUI));
            checkUpdatesButton.setPivot(Pivot.LowerLeft);
            checkUpdatesButton.style.position.set(Position.Absolute);
            checkUpdatesButton.style.left.set(0, Unit.Pixel);
            checkUpdatesButton.style.width.set(100, Unit.Percent);
            checkUpdatesButton.style.height.set(38, Unit.Pixel);
            // Relative vertical positions keep the footer usable even though this
            // UI implementation ignores CSS bottom offsets for overlay children.
            checkUpdatesButton.setPosition(0, 95.7f, true);
            checkUpdatesButton.setBackgroundColor(0x201B13D8);
            checkUpdatesButton.setHoverBackgroundColor(0x342915E8);
            checkUpdatesButton.setBorder(1);
            checkUpdatesButton.setBorderColor(0x8A6A2DFF);
            checkUpdatesButton.setHoverBorderColor(0xD7AE55FF);
            navSidebar.addChild(checkUpdatesButton);
        }
        // add close button at the bottom
        AdvancedButton closeButton = AdvancedButtonFactory.defaultButton("", event -> close());
        closeButton.setPivot(Pivot.LowerLeft);
        closeButton.style.position.set(Position.Absolute);
        closeButton.style.left.set(0, Unit.Pixel);
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
        closeButton.addChild(btnLabel);
        navSidebar.addChild(closeButton);

        addTabButton(TAB_SETTINGS, t().get("TC_TAB_SETTINGS", uiPlayer), 0);
        addTabButton(TAB_DATA, t().get("TC_TAB_DATA", uiPlayer), 150);
        if (canShowReleaseNotesTab(selectedPlugin)) {
            addTabButton(TAB_RELEASE_NOTES, t().get("TC_TAB_RELEASE_NOTES", uiPlayer), 300);
        }
        if (canShowPluginSettingsTab(selectedPlugin)) {
            addTabButton(TAB_PLUGIN_SETTINGS, t().get("TC_TAB_PLUGIN_SETTINGS", uiPlayer), 450);
        }

        // clear content
        content.removeAllChilds();
        // select content
        if (TAB_RELEASE_NOTES.equals(selectedTab)) {
            content.addChild(releaseNotesContent(selectedPlugin));
        } else if (TAB_PLUGIN_SETTINGS.equals(selectedTab)) {
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

    public void close() {
        uiPlayer.removeUIElement(this);
        uiPlayer.deleteAttribute("tools.ui.overlay");
        CursorManager.hide(uiPlayer);
    }

    private void addTabButton(String tab, String label, int x) {
        boolean active = tab.equals(selectedTab);
        AdvancedButton tabButton = AdvancedButtonFactory.custom(new AdvancedButtonState(
                AdvancedBaseButton.State.DEFAULT, active ? 0xD7AE55FF : 0x5E4A25FF,
                active ? 0x3A2D18D8 : 0x181713C8, active ? 0xF2C766FF : 0xF4F0E6FF,
                0xD7AE55FF, 0x2A2419E8, "", event -> {
                    selectedTab = tab;
                    updateUI();
                }));
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

    private UIElement releaseNotesContent(String pluginLabel) {
        PluginUpdateService.Result result = OZTools.pluginUpdateResult(pluginLabel);
        UIElement panel = new UIElement();
        panel.setPivot(Pivot.UpperLeft);
        panel.setSize(100, 100, true);

        UILabel title = new UILabel(t().get("TC_PLUGIN_UPDATE_RELEASE_NOTES_TITLE", uiPlayer));
        title.setPivot(Pivot.UpperLeft);
        title.style.left.set(5, Unit.Percent);
        title.style.top.set(0, Unit.Pixel);
        title.style.width.set(90, Unit.Percent);
        title.style.height.set(32, Unit.Pixel);
        title.setFont(Font.DefaultBold);
        title.setFontSize(18);
        title.setFontColor(0xF4F0E6FF);
        panel.addChild(title);

        UILabel subtitle = new UILabel(t().get("TC_PLUGIN_UPDATE_RELEASE_NOTES_SUBTITLE", uiPlayer)
                .replace("PH_PLUGIN_NAME", pluginLabel)
                .replace("PH_LATEST_VERSION", result == null || result.latestVersion().isBlank() ? "?" : result.latestVersion()));
        subtitle.setPivot(Pivot.UpperLeft);
        subtitle.style.left.set(5, Unit.Percent);
        subtitle.style.top.set(12, Unit.Pixel);
        subtitle.style.width.set(90, Unit.Percent);
        subtitle.style.height.set(24, Unit.Pixel);
        subtitle.setFontSize(14);
        subtitle.setFontColor(0xC8C0B2FF);
        panel.addChild(subtitle);

        UIScrollView scroll = new UIScrollView(ScrollViewMode.Vertical);
        scroll.setPivot(Pivot.UpperLeft);
        scroll.style.left.set(4, Unit.Percent);
        scroll.style.top.set(50, Unit.Pixel);
        scroll.setSize(92, 68, true);
        scroll.style.borderTopWidth.set(1);
        scroll.style.borderTopColor.set(0x6A5228FF);
        scroll.style.paddingTop.set(12);
        scroll.style.paddingLeft.set(12);
        scroll.style.paddingRight.set(12);
        panel.addChild(scroll);

        String notes = result == null || result.releaseNotes().isBlank()
                ? t().get("TC_PLUGIN_UPDATE_RELEASE_NOTES_EMPTY", uiPlayer) : result.releaseNotes();
        UILabel notesLabel = new UILabel(notes.length() > 1800 ? notes.substring(0, 1800) + "..." : notes);
        notesLabel.setPivot(Pivot.UpperLeft);
        notesLabel.setPosition(0, 0, false);
        notesLabel.setSize(100, 220, true);
        notesLabel.setFontSize(13);
        notesLabel.setTextWrap(true);
        notesLabel.setFontColor(0xE0D8C8FF);
        scroll.addChild(notesLabel);

        AdvancedButton checkPlugin = AdvancedButtonFactory.defaultButton(t().get("TC_PLUGIN_UPDATE_CHECK_PLUGIN_ACTION", uiPlayer),
                event -> OZTools.checkPluginUpdate(pluginLabel, uiPlayer, this::updateUI));
        checkPlugin.setPivot(Pivot.LowerLeft);
        checkPlugin.setPosition(5, 94, true);
        checkPlugin.setSize(220, 34, false);
        checkPlugin.setBackgroundColor(0x201B13D8);
        checkPlugin.setHoverBackgroundColor(0x342915E8);
        checkPlugin.setBorder(1);
        checkPlugin.setBorderColor(0x8A6A2DFF);
        checkPlugin.setHoverBorderColor(0xD7AE55FF);
        panel.addChild(checkPlugin);

        if (updateAvailable(pluginLabel) || installAvailable(pluginLabel) || installing(pluginLabel)) {
            AdvancedButton updateButton = AdvancedButtonFactory.danger(installing(pluginLabel)
                    ? t().get("TC_PLUGIN_UPDATE_INSTALLING", uiPlayer)
                    : installAvailable(pluginLabel) ? t().get("TC_PLUGIN_UPDATE_INSTALL_ACTION", uiPlayer)
                            : t().get("TC_PLUGIN_UPDATE_ACTION", uiPlayer), event -> showUpdateConfirmation());
            updateButton.setPivot(Pivot.LowerLeft);
            updateButton.setPosition(0, 94, true);
            updateButton.style.left.set(290, Unit.Pixel);
            updateButton.setSize(180, 34, false);
            updateButton.setBackgroundColor(0x7A3018E8);
            updateButton.setHoverBackgroundColor(0xA84722FF);
            updateButton.setBorder(1);
            updateButton.setBorderColor(0xD7AE55FF);
            updateButton.setHoverBorderColor(0xF2C766FF);
            if (installing(pluginLabel)) updateButton.setClickable(false);
            panel.addChild(updateButton);
        }

        UILabel checkedAt = new UILabel(t().get("TC_PLUGIN_UPDATE_LAST_CHECKED", uiPlayer)
                .replace("PH_CHECKED_AT", result == null || result.checkedAtEpochMillis() <= 0 ? "-"
                        : DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm").withZone(ZoneId.systemDefault())
                                .format(Instant.ofEpochMilli(result.checkedAtEpochMillis()))));
        checkedAt.setPivot(Pivot.LowerRight);
        checkedAt.setPosition(95, 94, true);
        checkedAt.setSize(300, 24, false);
        checkedAt.setFontSize(12);
        checkedAt.setFontColor(0xC8C0B2FF);
        checkedAt.setTextAlign(TextAnchor.MiddleRight);
        panel.addChild(checkedAt);
        return panel;
    }

    private Set<String> pluginLabels() {
        Set<String> labels = new TreeSet<>();
        labels.addAll(playerPluginSettings.keySet());
        labels.addAll(playerPluginData.keySet());
        labels.addAll(playerPluginAdminSettings.keySet());
        labels.addAll(PluginUpdateService.managedPluginNames());
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
            PluginUpdateService.Result result = OZTools.pluginUpdateResult(pluginLabel);
            return result != null && result.state() == PluginUpdateService.State.NOT_INSTALLED ? "N/A" : "";
        }
        return "v" + pluginVersion;
    }

    private int versionColor(String pluginLabel) {
        PluginUpdateService.Result result = OZTools.pluginUpdateResult(pluginLabel);
        if (result == null) return 0xF2C766FF;
        if (result.state() == PluginUpdateService.State.CURRENT) return 0x4FC36AFF;
        if (result.state() == PluginUpdateService.State.UPDATE_AVAILABLE) return 0xE05252FF;
        return 0xF2C766FF;
    }

    private boolean updateAvailable(String pluginLabel) {
        PluginUpdateService.Result result = OZTools.pluginUpdateResult(pluginLabel);
        return result != null && result.state() == PluginUpdateService.State.UPDATE_AVAILABLE;
    }

    private boolean installAvailable(String pluginLabel) {
        PluginUpdateService.Result result = OZTools.pluginUpdateResult(pluginLabel);
        return result != null && result.state() == PluginUpdateService.State.NOT_INSTALLED;
    }

    private boolean installing(String pluginLabel) {
        PluginUpdateService.Result result = OZTools.pluginUpdateResult(pluginLabel);
        return result != null && result.state() == PluginUpdateService.State.INSTALLING;
    }

    private boolean canShowReleaseNotesTab(String pluginLabel) {
        return uiPlayer.isAdmin() && pluginLabel != null;
    }

    private void showUpdateConfirmation() {
        if (!uiPlayer.isAdmin() || (!updateAvailable(selectedPlugin) && !installAvailable(selectedPlugin))) return;
        PluginUpdateService.Result result = OZTools.pluginUpdateResult(selectedPlugin);
        boolean install = installAvailable(selectedPlugin);
        OZUIElement dialog = new OZUIElement();
        dialog.setPivot(Pivot.MiddleCenter);
        dialog.setPosition(50, 50, true);
        dialog.setSize(420, 210, false);
        dialog.setBackgroundColor(0x10100EF8);
        dialog.setBorder(1);
        dialog.setBorderColor(0xD7AE55FF);
        addChild(dialog);

        UILabel title = new UILabel(t().get(install ? "TC_PLUGIN_INSTALL_TITLE" : "TC_PLUGIN_UPDATE_INSTALL_TITLE", uiPlayer));
        title.setPivot(Pivot.UpperLeft);
        title.setPosition(20, 18, false);
        title.setSize(380, 28, false);
        title.setFontSize(18);
        title.setFontColor(0xF4F0E6FF);
        dialog.addChild(title);
        UILabel message = new UILabel(t().get(install ? "TC_PLUGIN_INSTALL_MESSAGE" : "TC_PLUGIN_UPDATE_INSTALL_MESSAGE", uiPlayer)
                .replace("PH_PLUGIN_NAME", selectedPlugin).replace("PH_INSTALLED_VERSION", result.installedVersion())
                .replace("PH_LATEST_VERSION", result.latestVersion()));
        message.setPivot(Pivot.UpperLeft);
        message.setPosition(20, 58, false);
        message.setSize(380, 68, false);
        message.setFontSize(13);
        message.setTextWrap(true);
        message.setFontColor(0xE0D8C8FF);
        dialog.addChild(message);
        AdvancedButton cancel = AdvancedButtonFactory.cancel(t().get("TC_BTN_CANCEL", uiPlayer), event -> removeChild(dialog));
        cancel.setPivot(Pivot.UpperLeft);
        cancel.setPosition(24, 154, false);
        cancel.setSize(140, 32, false);
        dialog.addChild(cancel);
        AdvancedButton confirm = AdvancedButtonFactory.danger(t().get(
                install ? "TC_PLUGIN_UPDATE_INSTALL_ACTION" : "TC_PLUGIN_UPDATE_INSTALL_CONFIRM", uiPlayer), event -> {
            removeChild(dialog);
            OZTools.installPluginUpdate(selectedPlugin, uiPlayer, this::updateUI);
        });
        confirm.setPivot(Pivot.UpperRight);
        confirm.setPosition(396, 154, false);
        confirm.setSize(170, 32, false);
        dialog.addChild(confirm);
    }

    private List<String> pendingUpdates() {
        List<String> pending = new ArrayList<>();
        for (String pluginLabel : pluginLabels()) {
            if (updateAvailable(pluginLabel)) {
                pending.add(pluginLabel);
            }
        }
        return pending;
    }

    private void showAllUpdatesConfirmation(List<String> pluginLabels) {
        if (!uiPlayer.isAdmin() || pluginLabels == null || pluginLabels.size() < 2) return;
        OZUIElement dialog = new OZUIElement();
        dialog.setPivot(Pivot.MiddleCenter);
        dialog.setPosition(50, 50, true);
        dialog.setSize(560, 370, false);
        dialog.setBackgroundColor(0x10100EF8);
        dialog.setBorder(1);
        dialog.setBorderColor(0xD7AE55FF);
        addChild(dialog);

        UILabel title = new UILabel(t().get("TC_PLUGIN_UPDATE_ALL_TITLE", uiPlayer));
        title.setPivot(Pivot.UpperLeft);
        title.setPosition(20, 18, false);
        title.setSize(520, 28, false);
        title.setFontSize(18);
        title.setFontColor(0xF4F0E6FF);
        dialog.addChild(title);

        UILabel message = new UILabel(t().get("TC_PLUGIN_UPDATE_ALL_MESSAGE", uiPlayer)
                .replace("PH_PLUGIN_COUNT", String.valueOf(pluginLabels.size())));
        message.setPivot(Pivot.UpperLeft);
        message.setPosition(20, 50, false);
        message.setSize(520, 34, false);
        message.setFontSize(13);
        message.setTextWrap(true);
        message.setFontColor(0xE0D8C8FF);
        dialog.addChild(message);

        UIScrollView notes = new UIScrollView(ScrollViewMode.Vertical);
        notes.setPivot(Pivot.UpperLeft);
        notes.setPosition(20, 92, false);
        notes.setSize(520, 208, false);
        StringBuilder text = new StringBuilder();
        for (String pluginLabel : pluginLabels) {
            PluginUpdateService.Result result = OZTools.pluginUpdateResult(pluginLabel);
            text.append(pluginLabel).append(" — ")
                    .append(result == null ? "" : "v" + result.installedVersion() + " → v" + result.latestVersion())
                    .append("\n");
            if (result != null && !result.releaseNotes().isBlank()) text.append(result.releaseNotes()).append("\n");
            text.append("\n");
        }
        String releaseNotes = text.toString().trim();
        UILabel noteText = new UILabel(releaseNotes);
        noteText.setPivot(Pivot.UpperLeft);
        noteText.setPosition(0, 0, false);
        noteText.style.width.set(100, Unit.Percent);
        int visualLines = Math.max(1, releaseNotes.split("\\R", -1).length + (releaseNotes.length() / 62));
        noteText.style.height.set(Math.max(208, visualLines * 16 + 20), Unit.Pixel);
        noteText.setFontSize(12);
        noteText.setTextWrap(true);
        noteText.setFontColor(0xE0D8C8FF);
        notes.addChild(noteText);
        dialog.addChild(notes);

        AdvancedButton cancel = AdvancedButtonFactory.cancel(t().get("TC_BTN_CANCEL", uiPlayer), event -> removeChild(dialog));
        cancel.setPivot(Pivot.UpperLeft);
        cancel.setPosition(24, 322, false);
        cancel.setSize(150, 32, false);
        dialog.addChild(cancel);
        AdvancedButton confirm = AdvancedButtonFactory.danger(t().get("TC_PLUGIN_UPDATE_ALL_ACTION", uiPlayer), event -> {
            removeChild(dialog);
            OZTools.installPluginUpdates(pluginLabels, uiPlayer, this::updateUI);
        });
        confirm.setPivot(Pivot.UpperRight);
        confirm.setPosition(536, 322, false);
        confirm.setSize(190, 32, false);
        dialog.addChild(confirm);
    }

    private boolean canShowPluginSettingsTab(String pluginLabel) {
        return uiPlayer.isAdmin() && pluginLabel != null && playerPluginAdminSettings.containsKey(pluginLabel);
    }
}
