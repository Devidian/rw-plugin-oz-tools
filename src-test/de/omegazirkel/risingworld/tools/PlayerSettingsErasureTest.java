package de.omegazirkel.risingworld.tools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.DriverManager;

import org.junit.Test;

public class PlayerSettingsErasureTest {
    @Test
    public void removesAllTypesAndLeavesOtherPlayersUntouched() throws Exception {
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite::memory:")) {
            PlayerSettings settings = new PlayerSettings(connection);
            settings.setBoolean(7, "consent", true);
            settings.setInt(7, "version", 1);
            settings.setString(7, "timestamp", "now");
            settings.setFloat(7, "ratio", 0.5f);
            settings.setBoolean(8, "consent", true);

            settings.erasePlayer(7);

            assertFalse(settings.getBoolean(7, "consent").isPresent());
            assertFalse(settings.getInt(7, "version").isPresent());
            assertFalse(settings.getString(7, "timestamp").isPresent());
            assertFalse(settings.getFloat(7, "ratio").isPresent());
            assertEquals(true, settings.getBoolean(8, "consent").orElse(false));
            settings.erasePlayer(7);
            assertTrue(settings.getBoolean(8, "consent").orElse(false));
        }
    }
}
