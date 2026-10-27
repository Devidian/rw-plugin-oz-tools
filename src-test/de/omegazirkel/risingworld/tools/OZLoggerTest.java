package de.omegazirkel.risingworld.tools;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import org.apache.logging.log4j.core.LoggerContext;
import org.junit.Test;

public class OZLoggerTest {
    @Test
    public void filtersConsoleFallbackAndAppliesLevelChanges() throws Exception {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (PrintStream capture = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setOut(capture);
            OZLogger logger = OZLogger.getInstance("OZ.Test." + UUID.randomUUID(), true);
            OZLogger otherPlugin = OZLogger.getInstance("OZ.OtherTest." + UUID.randomUUID(), true);

            OZLogger.configure("WARN", true);
            logger.debug("hidden-debug");
            logger.info("hidden-info");
            otherPlugin.info("hidden-other-plugin-info");
            logger.warn("visible-warn");
            otherPlugin.warn("visible-other-plugin-warn");
            logger.error("visible-error");
            assertFalse(output.toString(StandardCharsets.UTF_8).contains("hidden-debug"));
            assertFalse(output.toString(StandardCharsets.UTF_8).contains("hidden-info"));
            assertFalse(output.toString(StandardCharsets.UTF_8).contains("hidden-other-plugin-info"));
            assertTrue(output.toString(StandardCharsets.UTF_8).contains("visible-warn"));
            assertTrue(output.toString(StandardCharsets.UTF_8).contains("visible-other-plugin-warn"));
            assertTrue(output.toString(StandardCharsets.UTF_8).contains("visible-error"));

            OZLogger.configure("DEBUG", true);
            logger.info("visible-after-reload");
            assertTrue(output.toString(StandardCharsets.UTF_8).contains("visible-after-reload"));

            OZLogger.configure("OFF", true);
            logger.fatal("hidden-fatal");
            assertFalse(output.toString(StandardCharsets.UTF_8).contains("hidden-fatal"));
        } finally {
            System.setOut(originalOut);
            OZLogger.configure("DEBUG", false);
        }
    }

    @Test
    public void filtersLog4jOutputAndAppliesLevelChanges() throws Exception {
        String name = "OZ.Test." + UUID.randomUUID();
        Path logDirectory = Path.of("Logs", name);
        OZLogger logger = null;
        try {
            OZLogger.configure("WARN", false);
            logger = OZLogger.getInstance(name, false);
            logger.info("hidden-info");
            logger.getLogger().info("hidden-direct-info");
            logger.warn("visible-warn");
            OZLogger.configure("DEBUG", false);
            logger.info("visible-after-reload");
            logger.getLogger().info("visible-direct-after-reload");
            OZLogger.configure("OFF", false);
            logger.error("hidden-error");

            String log = Files.readString(logDirectory.resolve("current.log"));
            assertFalse(log.contains("hidden-info"));
            assertFalse(log.contains("hidden-direct-info"));
            assertTrue(log.contains("visible-warn"));
            assertTrue(log.contains("visible-after-reload"));
            assertTrue(log.contains("visible-direct-after-reload"));
            assertFalse(log.contains("hidden-error"));
        } finally {
            if (logger != null) {
                Field contextField = OZLogger.class.getDeclaredField("ctx");
                contextField.setAccessible(true);
                ((LoggerContext) contextField.get(logger)).stop();
            }
            if (Files.exists(logDirectory)) {
                try (var files = Files.list(logDirectory)) {
                    for (Path file : files.toList()) {
                        Files.delete(file);
                    }
                }
                Files.delete(logDirectory);
            }
            OZLogger.configure("DEBUG", false);
        }
    }
}
