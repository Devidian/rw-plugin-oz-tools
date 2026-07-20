package de.omegazirkel.risingworld.tools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.IOException;

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
    public void rejectsReleasesWithoutZipAsset() throws Exception {
        try {
            PluginUpdateService.selectZipAsset(JsonParser.parseString("{\"assets\":[]}").getAsJsonObject());
            fail("Expected missing ZIP asset to fail");
        } catch (IOException expected) {
            assertEquals("No ZIP release asset", expected.getMessage());
        }
    }
}
