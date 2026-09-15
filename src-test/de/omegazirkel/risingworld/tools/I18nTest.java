package de.omegazirkel.risingworld.tools;

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

public class I18nTest {
    @Test
    public void reloadsCatalogueWhenTheSamePluginPathIsInitializedAgain() throws Exception {
        Path pluginDirectory = Files.createTempDirectory("oz-tools-i18n-test");
        Path i18nDirectory = Files.createDirectories(pluginDirectory.resolve("i18n"));
        Path english = i18nDirectory.resolve("en.json");
        Files.writeString(english, "{\"tc\":{\"setting\":{\"old\":{\"label\":\"Old\"}}}}");

        I18n catalogue = I18n.getInstance("i18n-reload-" + UUID.randomUUID());
        Method reload = I18n.class.getDeclaredMethod("loadLanguageData", String.class);
        reload.setAccessible(true);
        reload.invoke(catalogue, pluginDirectory.toString());
        Assert.assertEquals("Old", catalogue.get("tc.setting.old.label", "en"));

        Files.writeString(english, "{\"tc\":{\"setting\":{\"new\":{\"label\":\"New\"}}}}");
        reload.invoke(catalogue, pluginDirectory.toString());

        Assert.assertEquals("New", catalogue.get("tc.setting.new.label", "en"));
        Assert.assertEquals("tc.setting.old.label", catalogue.get("tc.setting.old.label", "en"));
    }
}
