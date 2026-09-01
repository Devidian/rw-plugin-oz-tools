package de.omegazirkel.risingworld.tools.settings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.junit.Test;

public class JsonSettingsFileTest {
    @Test
    public void writesNestedValuesAndReadsThemBack() throws Exception {
        Path directory = Files.createTempDirectory("json-settings");
        Path file = directory.resolve("settings.world.json");
        JsonSettingsFile.writeFlatAtomically(file, Map.of("discord.channel", "42", "enabled", "true"));

        String json = Files.readString(file, StandardCharsets.UTF_8);
        assertTrue(json.contains("\"discord\""));
        assertEquals("42", JsonSettingsFile.loadFlat(file).get("discord.channel"));
        assertEquals("true", JsonSettingsFile.loadFlat(file).get("enabled"));
    }

    @Test
    public void migratesLegacyPropertiesOnlyWhenJsonDoesNotExist() throws Exception {
        Path directory = Files.createTempDirectory("json-settings-migration");
        Path legacy = directory.resolve("settings.properties");
        Path json = directory.resolve("settings.world.json");
        Files.writeString(legacy, "market.maxSlots=10\nenabled=false\n", StandardCharsets.UTF_8);

        assertTrue(JsonSettingsFile.migrateLegacyProperties(legacy, json));
        assertFalse(Files.exists(legacy));
        assertEquals("10", JsonSettingsFile.loadFlat(json).get("market.maxSlots"));
        assertFalse(JsonSettingsFile.migrateLegacyProperties(legacy, json));
    }

    @Test
    public void readsPrimitiveAndStringArrayValues() throws Exception {
        Path directory = Files.createTempDirectory("json-settings-array");
        Path file = directory.resolve("settings.world.json");
        Files.writeString(file, """
                { "feature": { "mail": true, "limit": 20 },
                  "integration": { "trustedPluginSenders": ["OZ - Shop", "OZ - Marketplace"] } }
                """, StandardCharsets.UTF_8);

        Map<String, String> values = JsonSettingsFile.loadFlat(file);
        assertEquals("true", values.get("feature.mail"));
        assertEquals("20", values.get("feature.limit"));
        assertEquals("OZ - Shop,OZ - Marketplace", values.get("integration.trustedPluginSenders"));
    }
}
