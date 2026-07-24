package de.omegazirkel.risingworld.tools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Test;

import com.google.gson.JsonParser;

public class PluginUpdateServiceTest {
    @Test
    public void extractsCanonicalGitHubRepositoriesOnly() {
        assertEquals("Devidian/rw-plugin-oz-tools", PluginUpdateService.repositoryFrom(
                "https://api.github.com/repos/Devidian/rw-plugin-oz-tools/releases/latest"));
        assertEquals("Devidian/rw-plugin-oz-tools", PluginUpdateService.repositoryFrom(
                "https://github.com/Devidian/rw-plugin-oz-tools/releases/latest"));
        assertNull(PluginUpdateService.repositoryFrom("https://example.invalid/Devidian/rw-plugin-oz-tools"));
        assertNull(PluginUpdateService.repositoryFrom("https://github.com/invalid repository"));
    }

    @Test
    public void comparesReleaseVersionsNumerically() {
        assertEquals(0, PluginUpdateService.compare("v0.22.3", "0.22.3"));
        assertEquals(1, PluginUpdateService.compare("0.22.10", "0.22.3"));
        assertEquals(-1, PluginUpdateService.compare("0.22.2", "0.22.3"));
        assertEquals(0, PluginUpdateService.compare("1.2", "1.2.0"));
    }

    @Test
    public void selectsOnlyZipReleaseAssets() throws Exception {
        String asset = PluginUpdateService.selectZipAsset(JsonParser.parseString("""
                {"assets":[
                  {"name":"checksums.txt","browser_download_url":"https://example.invalid/checksums"},
                  {"name":"OZTools-0.22.3.zip","browser_download_url":"https://example.invalid/tools.zip"}
                ]}""").getAsJsonObject());
        assertEquals("https://example.invalid/tools.zip", asset);
    }

    @Test
    public void bundledCatalogueIncludesBosses() {
        assertTrue(PluginUpdateService.managedPluginNames().contains("OZ - Bosses"));
    }

    @Test
    public void parsesStrictOzPluginCatalogue() throws Exception {
        var catalog = PluginUpdateService.parseCatalog("""
                {
                  "schemaVersion": 1,
                  "plugins": [
                    {
                      "name": "OZ - Bosses",
                      "repository": "Devidian/rw-plugin-oz-bosses",
                      "directory": "OZBosses"
                    }
                  ]
                }
                """);

        assertEquals("Devidian/rw-plugin-oz-bosses", catalog.get("OZ - Bosses").repository());
        assertEquals("OZBosses", catalog.get("OZ - Bosses").directory());
    }

    @Test
    public void rejectsExternalOrUnsafeCatalogueEntries() throws Exception {
        assertInvalidCatalogue("""
                {
                  "schemaVersion": 1,
                  "plugins": [
                    {
                      "name": "Unsafe",
                      "repository": "OtherOwner/plugin",
                      "directory": "Unsafe"
                    }
                  ]
                }
                """);
        assertInvalidCatalogue("""
                {
                  "schemaVersion": 1,
                  "plugins": [
                    {
                      "name": "OZ - Bosses",
                      "repository": "Devidian/rw-plugin-oz-bosses",
                      "directory": "../OZBosses"
                    }
                  ]
                }
                """);
    }

    @Test
    public void rejectsReleasesWithoutZipAsset() throws Exception {
        try {
            PluginUpdateService.selectZipAsset(JsonParser.parseString("{\"assets\":[]}").getAsJsonObject());
            fail("Expected missing ZIP asset to fail");
        } catch (IOException expected) {
            assertEquals("No ZIP release asset", expected.getMessage());
        }
    }

    @Test
    public void preservesSettingsAndSqliteFilesDuringReplacement() throws Exception {
        Path installed = Files.createTempDirectory("installed-plugin");
        Path replacement = Files.createTempDirectory("replacement-plugin");
        Files.writeString(installed.resolve("settings.properties"), "configured=true");
        Files.writeString(installed.resolve("players.db"), "database");
        Files.writeString(installed.resolve("players.db-wal"), "wal");
        Files.writeString(installed.resolve("shop-zones.json"), "shops");
        Files.writeString(installed.resolve("readme.txt"), "do-not-copy");

        PluginUpdateService.preserveLocalFiles(installed, replacement);

        assertEquals("configured=true", Files.readString(replacement.resolve("settings.properties")));
        assertEquals("database", Files.readString(replacement.resolve("players.db")));
        assertEquals("wal", Files.readString(replacement.resolve("players.db-wal")));
        assertEquals("shops", Files.readString(replacement.resolve("shop-zones.json")));
        assertEquals("do-not-copy", Files.readString(replacement.resolve("readme.txt")));
    }

    @Test
    public void deletesNestedStagingTree() throws Exception {
        Path staging = Files.createTempDirectory("oz-update-");
        Path nestedFile = staging.resolve("content/OZAdminUtils/plugin.jar");
        Files.createDirectories(nestedFile.getParent());
        Files.writeString(nestedFile, "plugin");

        PluginUpdateService.deleteTree(staging);

        assertFalse(Files.exists(staging));
    }

    @Test
    public void excludesTemporaryUpdateDirectoriesFromWatching() {
        assertTrue(PluginFileWatcher.isTransientUpdatePath(Path.of("Plugins/.oz-update-123/content/OZGPS")));
        assertTrue(PluginFileWatcher.isTransientUpdatePath(Path.of("Plugins/OZGPS.oz-backup/settings.properties")));
        assertFalse(PluginFileWatcher.isTransientUpdatePath(Path.of("Plugins/OZGPS/settings.properties")));
    }

    @Test
    public void normalizesConfiguredPlayerLanguageValues() {
        assertEquals(ToolsPlayerPreferences.LANGUAGE_SOURCE_SYSTEM,
                ToolsPlayerPreferences.normalizeLanguageSource("unexpected"));
        assertEquals(ToolsPlayerPreferences.LANGUAGE_SOURCE_GAME,
                ToolsPlayerPreferences.normalizeLanguageSource("GAME"));
        assertEquals("de", ToolsPlayerPreferences.normalizeLanguageCode("de-DE"));
        assertEquals("en", ToolsPlayerPreferences.normalizeLanguageCode("123"));
    }

    private static void assertInvalidCatalogue(String json) {
        try {
            PluginUpdateService.parseCatalog(json);
            fail("Expected invalid plugin catalogue to fail");
        } catch (IOException expected) {
            assertTrue(expected.getMessage().contains("catalogue"));
        }
    }
}
