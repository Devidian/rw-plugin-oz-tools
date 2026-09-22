package de.omegazirkel.risingworld.tools;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Test;

public class GameConnectorServiceTest {
    @Test public void requiresExplicitServerPluginWebExposure() throws Exception {
        Path server=Files.createTempDirectory("oz-tools-connector");
        Path plugin=Files.createDirectories(server.resolve("Plugins/OZTools"));
        Files.writeString(server.resolve("server.properties"),"Server_WebserverExposePlugins=true\n");
        assertTrue(GameConnectorService.pluginWebExposureEnabled(plugin.toString()));
        Files.writeString(server.resolve("server.properties"),"Server_WebserverExposePlugins=False\n");
        assertFalse(GameConnectorService.pluginWebExposureEnabled(plugin.toString()));
    }
}
