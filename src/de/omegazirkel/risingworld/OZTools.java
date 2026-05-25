
package de.omegazirkel.risingworld;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import de.omegazirkel.risingworld.tools.Colors;
import de.omegazirkel.risingworld.tools.FileChangeListener;
import de.omegazirkel.risingworld.tools.I18n;
import de.omegazirkel.risingworld.tools.OZLogger;
import de.omegazirkel.risingworld.tools.PluginFileWatcher;
import de.omegazirkel.risingworld.tools.PluginReloadDebouncer;
import de.omegazirkel.risingworld.tools.PluginSettings;
import de.omegazirkel.risingworld.tools.WSClientEndpoint;
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
import net.risingworld.api.Plugin;
import net.risingworld.api.Server;
import net.risingworld.api.events.EventMethod;
import net.risingworld.api.events.Listener;
import net.risingworld.api.events.player.PlayerCommandEvent;
import net.risingworld.api.events.player.PlayerConnectEvent;
import net.risingworld.api.events.player.PlayerSpawnEvent;
import net.risingworld.api.events.player.ui.PlayerToggleInventoryEvent;
import net.risingworld.api.events.player.ui.PlayerUITextFieldChangeEvent;
import net.risingworld.api.objects.Player;

/**
 *
 * @author Maik 'Devidian' Laschober
 */
public class OZTools extends Plugin implements Listener, FileChangeListener {
    private static final String LOGGER_NAME = "OZ.Tools";
    static final String pluginCMD = "ozt";
    private PluginFileWatcher fileWatcher;
    private PluginReloadDebouncer debouncer;
    private static final AtomicBoolean shutdownHookRegistered = new AtomicBoolean(false);

    public static OZLogger logger() {
        return OZLogger.getInstance(LOGGER_NAME);
    }

    private static PluginSettings s = null;
    private static I18n t = null;
    private final static Colors c = Colors.getInstance();
    public static String name = null;

    /**
     *
     */
    @Override
    public void onEnable() {
        // for loading correct I18n in other classes
        name = this.getDescription("name");
        s = PluginSettings.getInstance(this);
        s.initSettings();
        logger().setLevel(s.logLevel);
        t = I18n.getInstance(this);
        AssetManager.loadDefaultIcons(this);

        registerEventListener(this);
        SharedIndicatorManager.start();
        // Ensure all subsystems are out of shutdown mode for plugin reloads.
        OZLogger.resetShutdownMode();

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
            this.executeDelayed(5, () -> {
                Server.sendInputCommand("reloadplugins");
            });
        }, 15, TimeUnit.SECONDS);

        // Watcher start
        try {
            // e.g. directory for plugins
            Path pluginsDir = Paths.get("Plugins");
            fileWatcher = new PluginFileWatcher(pluginsDir, debouncer);

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

        // 2. Shut down all WebSocket clients
        WSClientEndpoint.shutdownAll();

        // 3. Shut down the logging framework as the very last step
        // This now only stops the contexts, but leaves Log4j running for reloads.
        OZLogger.shutdownAll();
    }

    @EventMethod
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

    @EventMethod
    public void onPlayerConnect(PlayerConnectEvent event) {
        SharedIndicatorManager.refreshAllPlayers();
    }

    @EventMethod
    public void onPlayerToggleInventory(PlayerToggleInventoryEvent event) {
        Player player = event.getPlayer();
        if (event.isVisible() && event.getStorage() == null) {
            InventoryOverlayPanel.show(player);
        } else {
            InventoryOverlayPanel.remove(player);
        }
    }

    @EventMethod
    public void onPlayerUITextFieldChange(PlayerUITextFieldChangeEvent event) {
        AdminPluginSettingsPanel.handleTextFieldChange(event);
    }

    @EventMethod
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
                String statusMessage = t.get("TC_CMD_STATUS", player)
                        .replace("PH_VERSION", c.okay + this.getDescription("version") + c.endTag)
                        .replace("PH_LANGUAGE",
                                c.info + player.getLanguage() + " / " + player.getSystemLanguage() + c.endTag)
                        .replace("PH_USEDLANG", c.okay + t.getLanguageUsed(player.getSystemLanguage()) + c.endTag)
                        .replace("PH_LANG_AVAILABLE", c.warning + t.getLanguageAvailable() + c.endTag);
                player.sendTextMessage(c.okay + this.getName() + c.endTag + "\n " + statusMessage);
                break;
            case "open":
                PluginMenuManager.showMainMenu(player);
                break;
            case "config":
                PlayerPluginSettingsOverlay overlay = (PlayerPluginSettingsOverlay) player
                        .getAttribute("tools.ui.overlay");
                if (overlay != null) {
                    player.removeUIElement(overlay);
                    player.deleteAttribute("tools.ui.overlay");
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

    @Override
    public void onSettingsChanged(Path settingsPath) {
        s.initSettings(settingsPath.toString());
    }

    public static PluginSettings getSettings() {
        return s;
    }
}
