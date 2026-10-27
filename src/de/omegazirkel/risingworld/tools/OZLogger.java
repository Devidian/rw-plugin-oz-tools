package de.omegazirkel.risingworld.tools;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.NullConfiguration;

import de.omegazirkel.risingworld.OZTools;
import de.omegazirkel.risingworld.tools.settings.JsonSettingsFile;
import net.risingworld.api.World;

public class OZLogger {

    private static final Map<String, OZLogger> INSTANCES = new ConcurrentHashMap<>();
    private static final boolean DEBUG_MODE = false;
    private static final String CONFIG_FILE = "oz-log4j2.xml";
    private static boolean isInShutdownMode = false;
    private static volatile Level globalLevel = Level.DEBUG;
    private static volatile boolean globalInternalLogOnly;
    private static volatile boolean settingsConfigured;

    private Logger logger() {
        return ctx != null ? ctx.getLogger(loggerName) : null;
    }

    private LoggerContext ctx;
    private final String loggerName;

    public OZLogger(String loggerName, boolean registerOnly) {
        this.loggerName = loggerName;
        // prevent init on shutdown and if registerOnly is set
        if (!registerOnly && !isInShutdownMode) {
            init();
        }
    }

    public OZLogger(String loggerName) {
        this(loggerName, false);
    }

    private void init() {
        // Prevent any re-initialization once the shutdown process has started.
        if (isInShutdownMode) {
            System.out.println("[" + loggerName + "] 🆘 Logger initialization skipped, shutdown in progress.");
            return;
        }

        System.setProperty("logPath", "Logs");
        System.setProperty("loggerName", loggerName);
        try {
            ClassLoader cl = OZLogger.class.getClassLoader();

            if (DEBUG_MODE) {
                Enumeration<URL> urls = cl.getResources(CONFIG_FILE);
                while (urls.hasMoreElements()) {
                    System.out.println("🪲 [OzLogger] FOUND (candidate): " + urls.nextElement());
                }
            }

            URL configUrl = cl.getResource(CONFIG_FILE);
            if (configUrl == null) {
                throw new IllegalStateException("Log4j2 config not found: " + CONFIG_FILE);
            }

            if (DEBUG_MODE)
                System.out.println("🪲 [OzLogger] USING EXACT CONFIG: " + configUrl + " for " + loggerName);

            try (InputStream is = configUrl.openStream()) {
                ConfigurationSource source = new ConfigurationSource(is, configUrl);

                if (DEBUG_MODE)
                    System.setProperty("log4j2.debug", "true");

                LoggerContext ctx = new LoggerContext(loggerName);
                Configuration config = ConfigurationFactory.getInstance().getConfiguration(ctx, source);
                ctx.start(config);
                this.ctx = ctx;

                logger().setLevel(globalLevel);
                debug("🪲 Logger initialized: " + loggerName + " using config: " + configUrl);
            }

        } catch (Exception e) {
            System.out.println("[" + loggerName + "] 🆘 Failed to initialize logger: " + e.getMessage());
            e.printStackTrace();
            this.ctx = new LoggerContext(loggerName);
            this.ctx.start(new NullConfiguration());
        }
    }

    public static OZLogger getInstance(String loggerName) {
        if (!settingsConfigured)
            bootstrapSettings();
        PluginSettings s = OZTools.getSettings();
        if (s == null && !settingsConfigured) {
            System.out.println("[" + loggerName
                    + "] 🆘 OZTools.s is null, default to internalLogOnly=false, use getInstance(String, boolean) instead.");
        }
        return getInstance(loggerName, s == null ? globalInternalLogOnly : s.logInternal);
    }

    public static OZLogger getInstance(String loggerName, boolean internalLogOnly) {
        return INSTANCES.computeIfAbsent(loggerName, k -> {
            OZLogger logger = new OZLogger(loggerName, true); // "true" = only Map-Register
            if (!internalLogOnly)
                logger.init(); // Init separate, out of computeIfAbsent
            return logger;
        });
    }

    public static void shutdownAll() {
        isInShutdownMode = true;
        OZTools.logger().warn("⚠️ Shutting down all logger contexts ...");
        for (OZLogger logger : INSTANCES.values()) {
            try {
                if (logger.ctx != null) {
                    logger.info("Stopping logger context for " + logger.loggerName);
                    logger.ctx.stop(); // stop Log4J context
                    logger.ctx = null; // use fallback logger if any more logs occure
                }
            } catch (Exception e) {
                // optional: minimal log or ignore
                System.out.println(
                        "[OZLogger] 🆘 Failed to stop logger context for " + logger.loggerName + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
        INSTANCES.clear();
        // DO NOT shut down LogManager here, to allow for plugin reloads.
    }

    /**
     * Completely terminates the Log4j framework.
     * Should ONLY be called on full server shutdown, not on plugin reload.
     */
    public static void terminate() {
        System.out.println("[OZLogger] Terminating Log4j framework globally.");
        LogManager.shutdown();
    }

    /**
     * Resets the shutdown flag. Should be called from onEnable to ensure a clean
     * state for reloads.
     */
    public static void resetShutdownMode() {
        isInShutdownMode = false;
    }

    /** Configures all OZLogger instances, including those created before Tools starts. */
    public static void configure(String levelName, boolean internalLogOnly) {
        globalLevel = Level.toLevel(levelName, Level.DEBUG);
        globalInternalLogOnly = internalLogOnly;
        settingsConfigured = true;
        for (OZLogger instance : INSTANCES.values()) {
            Logger logger = instance.logger();
            if (logger != null)
                logger.setLevel(globalLevel);
        }
    }

    private static synchronized void bootstrapSettings() {
        if (settingsConfigured)
            return;
        String world;
        try {
            world = World.getName();
        } catch (LinkageError ex) {
            return;
        }
        if (world == null || world.isBlank())
            return;
        String safeWorld = world.replaceAll("[^A-Za-z0-9._-]", "_");
        Path pluginDirectory = Path.of("Plugins", "OZTools");
        try {
            var codeSource = OZLogger.class.getProtectionDomain().getCodeSource();
            URL location = codeSource == null ? null : codeSource.getLocation();
            if (location != null) {
                Path jarPath = Path.of(location.toURI());
                if (jarPath.getFileName().toString().equals("OZTools.jar"))
                    pluginDirectory = jarPath.getParent();
            }
            Path settingsFile = pluginDirectory.resolve("settings." + safeWorld + ".json");
            if (!Files.exists(settingsFile))
                settingsFile = pluginDirectory.resolve("settings.default.json");
            if (!Files.exists(settingsFile))
                return;
            Map<String, String> values = JsonSettingsFile.loadFlat(settingsFile);
            configure(values.getOrDefault("logLevel", "ALL"),
                    Boolean.parseBoolean(values.getOrDefault("logInternal", "false")));
        } catch (Exception ex) {
            settingsConfigured = true;
            System.err.println("[OZLogger] Could not read early Tools logging settings: " + ex.getMessage());
        }
    }

    private static boolean enabled(Level level) {
        return level.isMoreSpecificThan(globalLevel);
    }

    public OZLogger setLevel(String levelName) {
        return setLevel(Level.toLevel(levelName));
    }

    public OZLogger setLevel(Level level) {
        if (logger() == null)
            return this;
        logger().setLevel(level);
        return this;
    }

    public void debug(String message) {
        if (!enabled(Level.DEBUG))
            return;
        if (logger() == null)
            fallbackLog(message);
        else
            logger().debug(message);
    }

    public void info(String message) {
        if (!enabled(Level.INFO))
            return;
        if (logger() == null)
            fallbackLog(message);
        else
            logger().info(message);
    }

    public void warn(String message) {
        if (!enabled(Level.WARN))
            return;
        if (logger() == null)
            fallbackLog(message);
        else
            logger().warn(message);
    }

    public void error(String message) {
        if (!enabled(Level.ERROR))
            return;
        if (logger() == null)
            fallbackLog(message);
        else
            logger().error(message);
    }

    public void fatal(String message) {
        if (!enabled(Level.FATAL))
            return;
        if (logger() == null)
            fallbackLog(message);
        else
            logger().fatal(message);
    }

    private void fallbackLog(String message) {
        System.out.println("[" + loggerName + "] " + message);
    }

    // for non simple message logs like exceptions
    // maybe create wrapper methods if one kind is often used
    public Logger getLogger() {
        return logger();
    }

}
