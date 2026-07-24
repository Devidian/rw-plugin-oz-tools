
package de.omegazirkel.risingworld;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.List;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import de.omegazirkel.risingworld.tools.Colors;
import de.omegazirkel.risingworld.tools.FileChangeListener;
import de.omegazirkel.risingworld.tools.I18n;
import de.omegazirkel.risingworld.tools.OZLogger;
import de.omegazirkel.risingworld.tools.PluginFileWatcher;
import de.omegazirkel.risingworld.tools.PluginReloadDebouncer;
import de.omegazirkel.risingworld.tools.PluginSettings;
import de.omegazirkel.risingworld.tools.PluginUpdateService;
import de.omegazirkel.risingworld.tools.PlayerSettings;
import de.omegazirkel.risingworld.tools.ServerThreadDispatcher;
import de.omegazirkel.risingworld.tools.ThreadDiagnostics;
import de.omegazirkel.risingworld.tools.ToolsPlayerPreferences;
import de.omegazirkel.risingworld.tools.WSClientEndpoint;
import de.omegazirkel.risingworld.tools.db.SQLiteConnectionFactory;
import de.omegazirkel.risingworld.tools.settings.PlayerPluginAdminSettings;
import de.omegazirkel.risingworld.tools.ui.AdminPluginSettingsPanel;
import de.omegazirkel.risingworld.tools.ui.AssetManager;
import de.omegazirkel.risingworld.tools.ui.CursorManager;
import de.omegazirkel.risingworld.tools.ui.InventoryOverlayPanel;
import de.omegazirkel.risingworld.tools.ui.PlayerPluginSettingsOverlay;
import de.omegazirkel.risingworld.tools.ui.PluginInfoStatusProviders;
import de.omegazirkel.risingworld.tools.ui.PluginMenuManager;
import de.omegazirkel.risingworld.tools.ui.SharedIndicatorManager;
import de.omegazirkel.risingworld.tools.ui.ToolsPluginInfoStatusProvider;
import de.omegazirkel.risingworld.tools.ui.ToolsPlayerPluginData;
import de.omegazirkel.risingworld.tools.ui.ToolsPlayerPluginSettings;
import de.omegazirkel.risingworld.tools.ui.ToolsPlayerPluginSettingsPanel;
import net.risingworld.api.Plugin;
import net.risingworld.api.Server;
import net.risingworld.api.events.player.PlayerCommandEvent;
import net.risingworld.api.events.player.PlayerConnectEvent;
import net.risingworld.api.events.player.PlayerDisconnectEvent;
import net.risingworld.api.events.player.PlayerSpawnEvent;
import net.risingworld.api.events.player.ui.PlayerToggleInventoryEvent;
import net.risingworld.api.events.player.ui.PlayerUITextFieldChangeEvent;
import net.risingworld.api.objects.Player;

/**
 *
 * @author Maik 'Devidian' Laschober
 */
class OZToolsRuntime extends Plugin {
    private static final String LOGGER_NAME = "OZ.Tools";
    static final String pluginCMD = "ozt";
    private PluginFileWatcher fileWatcher;
    private PluginReloadDebouncer debouncer;
    private ServerThreadDispatcher serverThreadDispatcher;
    private ThreadDiagnostics threadDiagnostics;
    private PluginUpdateService pluginUpdateService;
    private static volatile PluginUpdateService activePluginUpdateService;
    private static volatile OZToolsRuntime activeTools;

    public static void checkPluginUpdates() {
        PluginUpdateService service = activePluginUpdateService;
        if (service != null) service.checkAsync();
    }

    public static void checkPluginUpdates(Player player) {
        checkPluginUpdates(player, null);
    }

    public static void checkPluginUpdates(Player player, Runnable onCompleted) {
        PluginUpdateService service = activePluginUpdateService;
        OZToolsRuntime tools = activeTools;
        if (service == null || tools == null || player == null || !player.isAdmin()) return;
        player.sendTextMessage(t.get("TC_PLUGIN_UPDATE_CHECK_STARTED", player));
        service.checkAsync(pluginName -> tools.serverThreadDispatcher.dispatch(() -> player.sendTextMessage(
                t.get("TC_PLUGIN_UPDATE_CHECK_PLUGIN", player).replace("PH_PLUGIN_NAME", pluginName))),
                ignored -> tools.serverThreadDispatcher.dispatch(() -> {
                    if (onCompleted != null) onCompleted.run();
                }),
                updatesAvailable -> tools.serverThreadDispatcher.dispatch(() -> {
                    player.sendTextMessage(t.get(updatesAvailable ? "TC_PLUGIN_UPDATE_CHECK_UPDATES_AVAILABLE"
                            : "TC_PLUGIN_UPDATE_CHECK_NONE", player));
                    if (onCompleted != null) onCompleted.run();
                }));
    }

    public static void checkPluginUpdate(String pluginName, Player player, Runnable onCompleted) {
        PluginUpdateService service = activePluginUpdateService;
        OZToolsRuntime tools = activeTools;
        if (service == null || tools == null || player == null || !player.isAdmin()) return;
        player.sendTextMessage(t.get("TC_PLUGIN_UPDATE_CHECK_PLUGIN", player).replace("PH_PLUGIN_NAME", pluginName));
        service.checkPluginAsync(pluginName, ignored -> tools.serverThreadDispatcher.dispatch(() -> {
            player.sendTextMessage(t.get("TC_PLUGIN_UPDATE_CHECK_PLUGIN_COMPLETED", player)
                    .replace("PH_PLUGIN_NAME", pluginName));
            if (onCompleted != null) onCompleted.run();
        }));
    }

    public static PluginUpdateService.Result pluginUpdateResult(String pluginName) {
        PluginUpdateService service = activePluginUpdateService;
        return service == null ? null : service.results().get(pluginName);
    }

    /** Resolves the configured player language for all OZ plugin translations. */
    public static String getPlayerLanguage(Player player) {
        return ToolsPlayerPreferences.language(player);
    }

    public static void installPluginUpdate(String pluginName, Player player, Runnable onStateChanged) {
        PluginUpdateService service = activePluginUpdateService;
        OZToolsRuntime tools = activeTools;
        if (service == null || tools == null) return;
        PluginUpdateService.Result result = pluginUpdateResult(pluginName);
        if (player != null && player.isAdmin() && result != null) {
            player.sendTextMessage(t.get("TC_PLUGIN_UPDATE_INSTALL_STARTED", player)
                    .replace("PH_PLUGIN_NAME", pluginName)
                    .replace("PH_INSTALLED_VERSION", result.installedVersion())
                    .replace("PH_LATEST_VERSION", result.latestVersion()));
        }
        service.installLatestAsync(pluginName, () -> tools.serverThreadDispatcher.dispatch(() ->
                {
                    if (player != null) player.sendTextMessage(t.get("TC_PLUGIN_UPDATE_INSTALL_COMPLETED", player));
                    service.markInstalledLatest(pluginName);
                    if (onStateChanged != null) onStateChanged.run();
                    tools.executeDelayed(5, () -> Server.sendInputCommand("reloadplugins"));
                }), reason -> tools.serverThreadDispatcher.dispatch(() -> {
                    if (player != null) player.sendTextMessage(t.get("untrusted-release-source".equals(reason)
                            ? "TC_PLUGIN_UPDATE_INSTALL_FAILED_UNTRUSTED_SOURCE" : "TC_PLUGIN_UPDATE_INSTALL_FAILED", player));
                    if (onStateChanged != null) onStateChanged.run();
                }));
        if (onStateChanged != null) onStateChanged.run();
    }

    /** Installs confirmed releases one after another and reloads only after the
     * complete batch has been staged successfully. */
    public static void installPluginUpdates(List<String> pluginNames, Player player, Runnable onStateChanged) {
        PluginUpdateService service = activePluginUpdateService;
        OZToolsRuntime tools = activeTools;
        if (service == null || tools == null || player == null || !player.isAdmin() || pluginNames == null) return;
        List<String> pending = pluginNames.stream()
                .filter(name -> {
                    PluginUpdateService.Result result = pluginUpdateResult(name);
                    return result != null && result.state() == PluginUpdateService.State.UPDATE_AVAILABLE;
                }).toList();
        if (pending.isEmpty()) return;
        installPluginUpdatesNext(service, tools, pending, 0, player, onStateChanged);
    }

    private static void installPluginUpdatesNext(PluginUpdateService service, OZToolsRuntime tools,
            List<String> pending,
            int index, Player player, Runnable onStateChanged) {
        if (index >= pending.size()) {
            player.sendTextMessage(t.get("TC_PLUGIN_UPDATE_INSTALL_COMPLETED", player));
            if (onStateChanged != null) onStateChanged.run();
            tools.executeDelayed(5, () -> Server.sendInputCommand("reloadplugins"));
            return;
        }
        String pluginName = pending.get(index);
        PluginUpdateService.Result result = pluginUpdateResult(pluginName);
        if (result != null) {
            player.sendTextMessage(t.get("TC_PLUGIN_UPDATE_INSTALL_STARTED", player)
                    .replace("PH_PLUGIN_NAME", pluginName)
                    .replace("PH_INSTALLED_VERSION", result.installedVersion())
                    .replace("PH_LATEST_VERSION", result.latestVersion()));
        }
        service.installLatestAsync(pluginName, () -> tools.serverThreadDispatcher.dispatch(() -> {
            service.markInstalledLatest(pluginName);
            if (onStateChanged != null) onStateChanged.run();
            installPluginUpdatesNext(service, tools, pending, index + 1, player, onStateChanged);
        }), reason -> tools.serverThreadDispatcher.dispatch(() -> {
            player.sendTextMessage(t.get("untrusted-release-source".equals(reason)
                    ? "TC_PLUGIN_UPDATE_INSTALL_FAILED_UNTRUSTED_SOURCE" : "TC_PLUGIN_UPDATE_INSTALL_FAILED", player));
            if (onStateChanged != null) onStateChanged.run();
        }));
    }
    private static final AtomicBoolean shutdownHookRegistered = new AtomicBoolean(false);

    public static OZLogger logger() {
        return OZLogger.getInstance(LOGGER_NAME);
    }

    private static PluginSettings s = null;
    private static I18n t = null;
    private static Connection sqliteCon;
    private static PlayerSettings playerSettings;
    private final static Colors c = Colors.getInstance();
    public static String name = null;

    /**
     *
     */
    @Override
    public void onEnable() {
        OZLogger.resetShutdownMode();
        // for loading correct I18n in other classes
        name = this.getDescription("name");
        s = PluginSettings.getInstance((OZTools) this);
        s.initSettings();
        logger().setLevel(s.logLevel);
        configureThreadDiagnostics();
        t = I18n.getInstance(this);
        AssetManager.loadDefaultIcons((OZTools) this);
        try {
            sqliteCon = SQLiteConnectionFactory.open(this);
            playerSettings = new PlayerSettings(sqliteCon);
        } catch (RuntimeException ex) {
            logger().error("Failed to initialize Tools player settings database: " + ex.getMessage());
        }

        serverThreadDispatcher = new ServerThreadDispatcher(this);
        SharedIndicatorManager.start(this::isMainThread, serverThreadDispatcher::dispatch);
        pluginUpdateService = new PluginUpdateService((OZTools) this, sqliteCon);
        activePluginUpdateService = pluginUpdateService;
        activeTools = this;
        if (s.automaticPluginUpdateCheck) {
            executeDelayed(Math.max(1, s.pluginUpdateCheckDelaySeconds), pluginUpdateService::checkAsync);
        }

        // Register the shutdown hook only once for the entire lifetime of the JVM.
        if (shutdownHookRegistered.compareAndSet(false, true)) {
            Runtime.getRuntime().addShutdownHook(new Thread(OZLogger::terminate, "OZ-Log4j-Terminator"));
            logger().info("JVM shutdown hook for Log4j termination registered.");
        }

        // Debounce: reload all plugins after 10 seconds from the last jar change
        debouncer = new PluginReloadDebouncer(() -> {
            if (!s.reloadOnChange) {
                logger().warn("⚠️ jar changed but plugin reloading on change is deactivated, see settings.properties");
                return;
            }
            logger().info("ℹ️ Detected jar changes, reloading all plugins...");
            serverThreadDispatcher.dispatch(() ->
                    this.executeDelayed(5, () -> Server.sendInputCommand("reloadplugins")));
        }, 15, TimeUnit.SECONDS);

        // Watcher start
        try {
            // e.g. directory for plugins
            Path pluginsDir = Paths.get("Plugins");
            fileWatcher = new PluginFileWatcher(pluginsDir, debouncer, serverThreadDispatcher::dispatch);

            // Register all plugins that implement listeners
            for (Plugin plugin : this.getAllPlugins()) {
                if (plugin instanceof FileChangeListener listener) {
                    Path pluginDir = Paths.get(plugin.getPath());
                    Path settings = pluginDir.resolve("settings.properties");
                    if (Files.exists(settings)) {
                        fileWatcher.addSettingsFile(settings, listener);
                    } else {
                        logger().info("Plugin has no settings.properties: " + plugin.getPath());
                    }
                    fileWatcher.addListener((FileChangeListener) plugin);
                }
            }

            logger().info("✅ File watcher started on " + pluginsDir);
        } catch (IOException e) {
            logger().fatal("Error while starting file watcher: " + e.getMessage());
            e.printStackTrace();
        }

        // register plugin settings
        PlayerPluginSettingsOverlay.registerPlayerPluginSettings(new ToolsPlayerPluginSettings(getDescription("version")));
        PlayerPluginSettingsOverlay.registerPlayerPluginData(new ToolsPlayerPluginData(getDescription("version")));
        PlayerPluginSettingsOverlay.registerPlayerPluginAdminSettings(
                new PlayerPluginAdminSettings(getDescription("name"), getDescription("version"),
                        () -> s.adminSettingsEntries(), s::initSettings));
        PluginInfoStatusProviders.registerProvider(
                new ToolsPluginInfoStatusProvider(getDescription("name"), getDescription("version"), pluginCMD));
        logger().info("✅ " + this.getName() + " Plugin is enabled version:" + this.getDescription("version"));
    }

    /**
     *
     */
    @Override
    public void onDisable() {
        logger().warn("⚠️ Disabling " + this.getName() + " and shutting down services...");

        if (serverThreadDispatcher != null) {
            serverThreadDispatcher.close();
        }
        if (threadDiagnostics != null) {
            threadDiagnostics.close();
            threadDiagnostics = null;
        }
        if (pluginUpdateService != null) {
            pluginUpdateService.close();
            pluginUpdateService = null;
            activePluginUpdateService = null;
            activeTools = null;
        }

        // 1. Close file watcher to prevent further actions
        if (fileWatcher != null) {
            fileWatcher.close();
            logger().info("File watcher closed.");
        }

        if (debouncer != null) {
            debouncer.shutdown();
            logger().info("Reload debouncer shut down.");
        }

        SharedIndicatorManager.stop();
        PluginInfoStatusProviders.unregisterProvider(getDescription("name"));
        playerSettings = null;
        if (sqliteCon != null) {
            try {
                sqliteCon.close();
            } catch (SQLException ex) {
                logger().warn("Failed to close Tools database connection: " + ex.getMessage());
            }
            sqliteCon = null;
        }
        // 2. Shut down all WebSocket clients
        WSClientEndpoint.shutdownAll();

        // 3. Shut down the logging framework as the very last step
        // This now only stops the contexts, but leaves Log4j running for reloads.
        OZLogger.shutdownAll();
    }

    public void onPlayerSpawn(PlayerSpawnEvent event) {
        if (s.enablePluginWelcomeMessage) {
            Player player = event.getPlayer();
            player.sendTextMessage(t.get("TC_MSG_PLUGIN_WELCOME", player)
                    .replace("PH_PLUGIN_NAME", getDescription("name"))
                    .replace("PH_PLUGIN_CMD", pluginCMD)
                    .replace("PH_PLUGIN_VERSION", getDescription("version")));
        }
        // Playersettings below
        SharedIndicatorManager.refreshAllPlayers();
    }

    public void onPlayerConnect(PlayerConnectEvent event) {
        SharedIndicatorManager.refreshAllPlayers();
    }

    public void onPlayerDisconnect(PlayerDisconnectEvent event) {
    }

    public void onPlayerToggleInventory(PlayerToggleInventoryEvent event) {
        Player player = event.getPlayer();
        if (event.isVisible() && event.getStorage() == null) {
            SharedIndicatorManager.hide(player);
            InventoryOverlayPanel.show(player);
        } else {
            InventoryOverlayPanel.remove(player);
            SharedIndicatorManager.refresh(player);
        }
    }

    public void onPlayerUITextFieldChange(PlayerUITextFieldChangeEvent event) {
        AdminPluginSettingsPanel.handleTextFieldChange(event);
        ToolsPlayerPluginSettingsPanel.handleTextFieldChange(event);
    }

    public void onPlayerCommand(PlayerCommandEvent event) {
        String[] commandArgs = event.getCommand().split(" ");
        if (!commandArgs[0].contentEquals("/" + pluginCMD)) {
            event.setCancelled(true);
            return;
        }
        String command = "";
        if (commandArgs.length > 1) {
            command = commandArgs[1];
        }
        Player player = event.getPlayer();
        switch (command) {
            case "status":
            case "info":
                PluginInfoStatusProviders.show(player, getDescription("name"));
                break;
            case "open":
                PluginMenuManager.showMainMenu(player);
                break;
            case "config":
                PlayerPluginSettingsOverlay overlay = (PlayerPluginSettingsOverlay) player
                        .getAttribute("tools.ui.overlay");
                if (overlay != null) {
                    overlay.close();
                }
                overlay = new PlayerPluginSettingsOverlay(player);
                CursorManager.show(player);
                player.addUIElement(overlay);
                player.setAttribute("tools.ui.overlay", overlay);
                break;
            case "help":
            case "":
            default:
                player.sendTextMessage(c.okay + this.getName() + c.endTag + "\n "
                        + t.get("TC_CMD_HELP", player).replace("PH_PLUGIN_CMD", pluginCMD));
                break;
        }
    }

    public void onSettingsChanged(Path settingsPath) {
        s.initSettings(settingsPath.toString());
        configureThreadDiagnostics();
    }

    private void configureThreadDiagnostics() {
        if (threadDiagnostics != null) {
            threadDiagnostics.close();
        }
        threadDiagnostics = ThreadDiagnostics.create(s.threadDiagnosticsEnabled,
                OZLogger.getInstance("OZ.ThreadDiagnostics")::info);
        threadDiagnostics.start();
    }

    public static PluginSettings getSettings() {
        return s;
    }

    public static PlayerSettings playerSettings() {
        return playerSettings;
    }
}
