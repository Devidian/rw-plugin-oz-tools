package de.omegazirkel.risingworld.tools.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import de.omegazirkel.risingworld.tools.OZLogger;

public class AreaPermissionTemplatesTest {
    @Rule public TemporaryFolder directory = new TemporaryFolder();

    @Test
    public void loadsFromTheOwnerAndPreservesExistingPermissionsUnlessOverwriteIsRequested() throws Exception {
        var target = directory.getRoot().toPath().resolve("Permissions/Areas");
        ClassLoader owner = new ClassLoader() {
            @Override public InputStream getResourceAsStream(String resource) {
                if (!"permissions/example.json".equals(resource)) return null;
                return new ByteArrayInputStream("packaged".getBytes(StandardCharsets.UTF_8));
            }
        };
        var logger = OZLogger.getInstance("AreaPermissionTemplatesTest");
        assertTrue(AreaPermissionTemplates.copy(target.toFile(), owner, logger, "example.json", false));
        var permission = target.resolve("example.json");
        assertEquals("packaged", Files.readString(permission));
        Files.writeString(permission, "local");
        assertFalse(AreaPermissionTemplates.copy(target.toFile(), owner, logger, "example.json", false));
        assertEquals("local", Files.readString(permission));
        assertTrue(AreaPermissionTemplates.copy(target.toFile(), owner, logger, "example.json", true));
        assertEquals("packaged", Files.readString(permission));
        assertFalse(AreaPermissionTemplates.copy(target.toFile(), owner, logger, "missing.json", true));
        assertFalse(Files.exists(target.resolve("missing.json")));
    }
}
