package de.omegazirkel.risingworld.tools.privacy;

import de.omegazirkel.risingworld.tools.PlayerSettings;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

/** Persists the ordered steps of a player erasure owned by this game server. */
public final class PlayerErasureCoordinator {
    private static final Pattern STEP_ID = Pattern.compile("[a-z][a-z0-9._-]*");

    public record Request(String id, int playerDbId) { }

    public enum Progress { NO_REQUEST, WAITING, COMPLETE }

    @FunctionalInterface
    public interface Step {
        /** Return true only after completion. The same request may be retried after an interruption. */
        boolean erase(Request request) throws Exception;
    }

    private record Job(Request request, List<String> pendingSteps) { }

    private final Connection connection;
    private final PlayerSettings playerSettings;
    private final Object processing = new Object();

    public PlayerErasureCoordinator(Connection connection, PlayerSettings playerSettings) throws SQLException {
        if (!connection.getAutoCommit()) throw new IllegalArgumentException("Erasure journal requires autocommit");
        this.connection = connection;
        this.playerSettings = playerSettings;
        try (var statement = connection.createStatement()) {
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS player_erasure_jobs (
                        player_id INTEGER PRIMARY KEY,
                        request_id TEXT NOT NULL,
                        pending_steps TEXT NOT NULL
                    )
                    """);
        }
    }

    /** A duplicate start preserves the original request identifier and its remaining steps. */
    public synchronized Request start(int playerDbId, List<String> steps) throws SQLException {
        if (playerDbId <= 0) throw new IllegalArgumentException("Player DBID must be positive");
        List<String> required = List.copyOf(steps);
        if (required.isEmpty() || required.stream().anyMatch(step -> !STEP_ID.matcher(step).matches())
                || new HashSet<>(required).size() != required.size()) {
            throw new IllegalArgumentException("Erasure requires unique, valid step identifiers");
        }
        try (var statement = connection.prepareStatement("""
                INSERT INTO player_erasure_jobs (player_id, request_id, pending_steps)
                VALUES (?, ?, ?) ON CONFLICT(player_id) DO NOTHING
                """)) {
            statement.setInt(1, playerDbId);
            statement.setString(2, UUID.randomUUID().toString());
            statement.setString(3, String.join("\n", required));
            statement.executeUpdate();
        }
        return find(playerDbId).request();
    }

    public synchronized List<Integer> pendingPlayerIds() throws SQLException {
        try (var statement = connection.createStatement();
                var rows = statement.executeQuery("SELECT player_id FROM player_erasure_jobs ORDER BY player_id")) {
            var ids = new ArrayList<Integer>();
            while (rows.next()) ids.add(rows.getInt(1));
            return List.copyOf(ids);
        }
    }

    public synchronized boolean isPending(int playerDbId) throws SQLException {
        try (var statement = connection.prepareStatement("SELECT 1 FROM player_erasure_jobs WHERE player_id = ?")) {
            statement.setInt(1, playerDbId);
            try (var row = statement.executeQuery()) { return row.next(); }
        }
    }

    /** Run through consecutive acknowledged steps and checkpoint each before advancing. */
    public Progress resume(int playerDbId, Map<String, Step> handlers) throws Exception {
        synchronized (processing) {
            return process(playerDbId, handlers);
        }
    }

    private Progress process(int playerDbId, Map<String, Step> handlers) throws Exception {
        Job job = find(playerDbId);
        if (job == null) return Progress.NO_REQUEST;
        List<String> steps = job.pendingSteps();
        for (int index = 0; index < steps.size(); index++) {
            Step handler = handlers.get(steps.get(index));
            if (handler == null || !handler.erase(job.request())) return Progress.WAITING;
            checkpoint(playerDbId, steps.subList(index + 1, steps.size()));
        }
        // Player settings include the decisions needed during cleanup. Erase them only after every recipient.
        playerSettings.erasePlayer(playerDbId);
        remove(playerDbId);
        return Progress.COMPLETE;
    }

    private synchronized void remove(int playerDbId) throws SQLException {
        try (var statement = connection.prepareStatement("DELETE FROM player_erasure_jobs WHERE player_id = ?")) {
            statement.setInt(1, playerDbId);
            statement.executeUpdate();
        }
    }

    private synchronized Job find(int playerDbId) throws SQLException {
        try (var statement = connection.prepareStatement("""
                SELECT request_id, pending_steps FROM player_erasure_jobs WHERE player_id = ?
                """)) {
            statement.setInt(1, playerDbId);
            try (var row = statement.executeQuery()) {
                if (!row.next()) return null;
                String pending = row.getString(2);
                return new Job(new Request(row.getString(1), playerDbId),
                        pending.isEmpty() ? List.of() : List.of(pending.split("\n")));
            }
        }
    }

    private synchronized void checkpoint(int playerDbId, List<String> remaining) throws SQLException {
        try (var statement = connection.prepareStatement("""
                UPDATE player_erasure_jobs SET pending_steps = ? WHERE player_id = ?
                """)) {
            statement.setString(1, String.join("\n", remaining));
            statement.setInt(2, playerDbId);
            statement.executeUpdate();
        }
    }
}
