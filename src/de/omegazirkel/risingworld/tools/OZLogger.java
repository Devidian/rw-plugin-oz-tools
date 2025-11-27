package de.omegazirkel.risingworld.tools;

import java.io.InputStream;
import java.net.URL;
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

public class OZLogger {

    private static final Map<String, OZLogger> INSTANCES = new ConcurrentHashMap<>();
    private static final boolean DEBUG_MODE = false;
    private static final String CONFIG_FILE = "oz-log4j2.xml";

    private static boolean isInShutdownMode = false;

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

                logger().debug("🪲 Logger initialized: " + loggerName + " using config: " + configUrl);
            }

        } catch (Exception e) {
            System.out.println("[" + loggerName + "] 🆘 Failed to initialize logger: " + e.getMessage());
            e.printStackTrace();
            this.ctx = new LoggerContext(loggerName);
            this.ctx.start(new NullConfiguration());
        }
    }

    public static OZLogger getInstance(String loggerName) {
        return INSTANCES.computeIfAbsent(loggerName, k -> {
            OZLogger logger = new OZLogger(loggerName, true); // "true" = only Map-Register
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

    public void setLevel(Level level) {
        logger().setLevel(level);
    }

    public void debug(String message) {
        if (logger() == null)
            fallbackLog(message);
        else
            logger().debug(message);
    }

    public void info(String message) {
        if (logger() == null)
            fallbackLog(message);
        else
            logger().info(message);
    }

    public void warn(String message) {
        if (logger() == null)
            fallbackLog(message);
        else
            logger().warn(message);
    }

    public void error(String message) {
        if (logger() == null)
            fallbackLog(message);
        else
            logger().error(message);
    }

    public void fatal(String message) {
        if (logger() == null)
            fallbackLog(message);
        else
            logger().fatal(message);
    }

    private void fallbackLog(String message) {
        System.out.println("[" + loggerName + "] 🆘 " + message);
    }

    // for non simple message logs like exceptions
    // maybe create wrapper methods if one kind is often used
    public Logger getLogger() {
        return logger();
    }

}