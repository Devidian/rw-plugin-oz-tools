
package de.omegazirkel.risingworld;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import de.omegazirkel.risingworld.tools.FileChangeListener;
import de.omegazirkel.risingworld.tools.I18n;
import de.omegazirkel.risingworld.tools.OZLogger;
import de.omegazirkel.risingworld.tools.PluginFileWatcher;
import de.omegazirkel.risingworld.tools.PluginReloadDebouncer;
import de.omegazirkel.risingworld.tools.PluginSettings;
import de.omegazirkel.risingworld.tools.WSClientEndpoint;
import net.risingworld.api.Plugin;
import net.risingworld.api.Server;
import net.risingworld.api.events.EventMethod;
import net.risingworld.api.events.Listener;
import net.risingworld.api.events.player.PlayerSpawnEvent;
import net.risingworld.api.objects.Player;

/**
 *
 * @author Maik 'Devidian' Laschober
 */
public class OZTools extends Plugin implements Listener, FileChangeListener {
    private PluginFileWatcher fileWatcher;
    private PluginReloadDebouncer debouncer;
    private static final AtomicBoolean shutdownHookRegistered = new AtomicBoolean(false);

    public static OZLogger logger() {
        return OZLogger.getInstance("OZ.Tools");
    }

    private static PluginSettings s = null;
    private static I18n t = null;

    /**
     *
     */
    @Override
    public void onEnable() {
        s = PluginSettings.getInstance(this);
        s.initSettings();
        logger().setLevel(s.logLevel);
        t = I18n.getInstance(this);
        registerEventListener(this);
        // Ensure all subsystems are out of shutdown mode for plugin reloads.
        OZLogger.resetShutdownMode();

        WSClientEndpoint.initLogger();

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
        }, 10, TimeUnit.SECONDS);

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

        // 2. Shut down all WebSocket clients
        WSClientEndpoint.shutdownAll();

        // 3. Shut down the logging framework as the very last step
        // This now only stops the contexts, but leaves Log4j running for reloads.
        OZLogger.shutdownAll();
    }

    @EventMethod
    public void onPlayerSpawn(PlayerSpawnEvent event) {
        if (s.sendPluginWelcome) {
            Player player = event.getPlayer();
            String lang = player.getSystemLanguage();
            player.sendTextMessage(t.get("MSG_PLUGIN_WELCOME", lang));
        }
    }

    @Override
    public void onSettingsChanged(Path settingsPath) {
        s.initSettings(settingsPath.toString());
    }
}