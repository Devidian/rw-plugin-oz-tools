package de.omegazirkel.risingworld.tools.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import de.omegazirkel.risingworld.tools.OZLogger;
import net.risingworld.api.Plugin;

/** Copies area permission resources owned by a feature plugin into the server's permission directory. */
public final class AreaPermissionTemplates {
    private AreaPermissionTemplates() {}

    public static boolean copy(Plugin owner, OZLogger logger, String sourceName, boolean overwrite) {
        File directory = new File(owner.getPath() + "/../../Permissions/Areas/");
        return copy(directory, owner.getClass().getClassLoader(), logger, sourceName, overwrite);
    }

    static boolean copy(File directory, ClassLoader resources, OZLogger logger, String sourceName, boolean overwrite) {
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                logger.error("Failed to create Permissions/Areas directory: " + directory.getAbsolutePath());
                return false;
            }
            logger.info("Created permission target directory: " + directory.getAbsolutePath());
        }

        File target = new File(directory, sourceName);
        if (target.exists() && !overwrite) {
            logger.info("Permission file already exists (skipped): " + target.getName());
            return false;
        }

        try (InputStream resource = resources.getResourceAsStream("permissions/" + sourceName)) {
            if (resource == null) {
                logger.error("Permission resource not found in JAR: permissions/" + sourceName);
                return false;
            }
            Files.copy(resource, target.toPath(), StandardCopyOption.REPLACE_EXISTING);
            logger.info("Copied permission file: " + target.getAbsolutePath());
            return true;
        } catch (IOException ex) {
            logger.error("Failed to copy permission file " + sourceName + ": " + ex.getMessage());
            return false;
        }
    }
}
