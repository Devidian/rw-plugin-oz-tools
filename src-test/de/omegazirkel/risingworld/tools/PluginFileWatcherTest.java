package de.omegazirkel.risingworld.tools;

import static org.junit.Assert.assertSame;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.junit.Test;

public class PluginFileWatcherTest {
    @Test
    public void resolvesNewWorldScopedSettingsThroughThePluginDirectory() throws Exception {
        Path pluginDirectory = Files.createTempDirectory("plugin-settings");
        Path generatedSettings = pluginDirectory.resolve("settings.New_World.json");
        FileChangeListener directoryListener = new FileChangeListener() {};

        assertSame(directoryListener, PluginFileWatcher.settingsListenerFor(generatedSettings, Map.of(),
                Map.of(pluginDirectory.toAbsolutePath(), directoryListener)));
    }
}
