package de.omegazirkel.risingworld.tools.privacy;

import de.omegazirkel.risingworld.tools.PlayerSettings;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static de.omegazirkel.risingworld.tools.privacy.PlayerErasureCoordinator.Progress.*;
import static org.junit.Assert.*;

public class PlayerErasureCoordinatorTest {
    @Rule public TemporaryFolder temp = new TemporaryFolder();

    @Test
    public void resumesOnlyUnconfirmedStepsAfterRestartAndErasesSettingsLast() throws Exception {
        String url = "jdbc:sqlite:" + temp.newFile("tools.db").getAbsolutePath();
        PlayerErasureCoordinator.Request original;
        List<String> calls = new ArrayList<>();

        try (var connection = DriverManager.getConnection(url)) {
            var settings = new PlayerSettings(connection);
            settings.setBoolean(7, "consent", true);
            settings.setBoolean(8, "consent", true);
            var coordinator = new PlayerErasureCoordinator(connection, settings);
            original = coordinator.start(7, List.of("local", "remote"));
            assertEquals(WAITING, coordinator.resume(7, Map.of(
                    "local", request -> calls.add("local"),
                    "remote", request -> {
                        calls.add("remote pending");
                        return false;
                    })));
            assertEquals(List.of(7), coordinator.pendingPlayerIds());
            assertEquals(Boolean.TRUE, settings.getBoolean(7, "consent").orElseThrow());
        }

        try (var connection = DriverManager.getConnection(url)) {
            var settings = new PlayerSettings(connection);
            var coordinator = new PlayerErasureCoordinator(connection, settings);
            assertEquals(original, coordinator.start(7, List.of("different-steps")));
            assertEquals(WAITING, coordinator.resume(7, Map.of()));
            assertEquals(COMPLETE, coordinator.resume(7, Map.of("remote", request -> {
                assertEquals(original, request);
                assertEquals(Boolean.TRUE, settings.getBoolean(7, "consent").orElseThrow());
                return calls.add("remote confirmed");
            })));
            assertEquals(List.of("local", "remote pending", "remote confirmed"), calls);
            assertTrue(coordinator.pendingPlayerIds().isEmpty());
            assertEquals(NO_REQUEST, coordinator.resume(7, Map.of()));
            assertTrue(settings.getBoolean(7, "consent").isEmpty());
            assertEquals(Boolean.TRUE, settings.getBoolean(8, "consent").orElseThrow());
        }
    }

    @Test
    public void failedStepPreservesRequestAndDoesNotRunItsSuccessor() throws Exception {
        try (var connection = DriverManager.getConnection("jdbc:sqlite::memory:")) {
            var settings = new PlayerSettings(connection);
            var coordinator = new PlayerErasureCoordinator(connection, settings);
            settings.setBoolean(7, "consent", true);
            var original = coordinator.start(7, List.of("fails", "next"));

            assertThrows(IllegalStateException.class, () -> coordinator.resume(7, Map.of(
                    "fails", request -> { throw new IllegalStateException("recipient unavailable"); },
                    "next", request -> { throw new AssertionError("ran past failed step"); })));
            assertEquals(List.of(7), coordinator.pendingPlayerIds());
            assertEquals(original, coordinator.start(7, List.of("fails", "next")));
            assertEquals(Boolean.TRUE, settings.getBoolean(7, "consent").orElseThrow());
        }
    }
}
