
package de.omegazirkel.risingworld;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import de.omegazirkel.risingworld.tools.FileChangeListener;
import de.omegazirkel.risingworld.tools.OZLogger;
import de.omegazirkel.risingworld.tools.PluginFileWatcher;
import de.omegazirkel.risingworld.tools.PluginReloadDebouncer;
import de.omegazirkel.risingworld.tools.WSClientEndpoint;
import net.risingworld.api.Plugin;
import net.risingworld.api.Server;
import net.risingworld.api.events.Listener;

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

    static int logLevel = 0;
    static boolean reloadOnChange = false;

    /**
     *
     */
    @Override
    public void onEnable() {
        initSettings();
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
            if (!reloadOnChange) {
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

    private void initSettings() {
        initSettings((getPath() != null ? getPath() : ".") + "/settings.properties");
    }

    private void initSettings(String filePath) {
        Properties settings = new Properties();
        FileInputStream in;
        try {
            in = new FileInputStream(filePath);
            settings.load(new InputStreamReader(in, "UTF8"));
            in.close();
            // fill global values
            logLevel = Integer.parseInt(settings.getProperty("logLevel", "0"));
            reloadOnChange = settings.getProperty("reloadOnChange", "false").contentEquals("true");
        } catch (IOException ex) {
            logger().fatal("❌ IOException on initSettings: " + ex.getMessage());
            // e.printStackTrace();
        } catch (NumberFormatException ex) {
            logger().fatal("❌ NumberFormatException on initSettings: " + ex.getMessage());
        } catch (Exception ex) {
            logger().fatal("❌ Exception on initSettings: " + ex.getMessage());
        }
    }

    @Override
    public void onSettingsChanged(Path settingsPath) {
        initSettings(settingsPath.toString());
    }
}