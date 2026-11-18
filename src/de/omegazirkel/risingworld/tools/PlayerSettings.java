package de.omegazirkel.risingworld.tools;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ArrayList;
import java.util.List;

public final class PlayerSettings {

    private final Connection connection;

    public PlayerSettings(Connection connection) {
        this.connection = connection;
        initTables();
    }

    private void initTables() {
        executeDDL("""
                    CREATE TABLE IF NOT EXISTS player_settings_bool (
                        player_id INTEGER NOT NULL,
                        key TEXT NOT NULL,
                        value INTEGER NOT NULL,
                        PRIMARY KEY (player_id, key)
                    )
                """);

        executeDDL("""
                    CREATE TABLE IF NOT EXISTS player_settings_int (
                        player_id INTEGER NOT NULL,
                        key TEXT NOT NULL,
                        value INTEGER NOT NULL,
                        PRIMARY KEY (player_id, key)
                    )
                """);

        executeDDL("""
                    CREATE TABLE IF NOT EXISTS player_settings_string (
                        player_id INTEGER NOT NULL,
                        key TEXT NOT NULL,
                        value TEXT NOT NULL,
                        PRIMARY KEY (player_id, key)
                    )
                """);

        executeDDL("""
                    CREATE TABLE IF NOT EXISTS player_settings_float (
                        player_id INTEGER NOT NULL,
                        key TEXT NOT NULL,
                        value REAL NOT NULL,
                        PRIMARY KEY (player_id, key)
                    )
                """);
    }

    private void executeDDL(String sql) {
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.execute();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to execute DDL", e);
        }
    }

    public Optional<Boolean> getBoolean(int playerId, String key) {
        return querySingle(
                "SELECT value FROM player_settings_bool WHERE player_id=? AND key=?",
                ps -> {
                    ps.setInt(1, playerId);
                    ps.setString(2, key);
                },
                rs -> rs.getInt("value") == 1);
    }

    public synchronized List<Integer> playerIdsWithBoolean(String key, boolean value) {
        List<Integer> ids = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT player_id FROM player_settings_bool WHERE key=? AND value=? ORDER BY player_id")) {
            ps.setString(1, key);
            ps.setInt(2, value ? 1 : 0);
            try (ResultSet rows = ps.executeQuery()) {
                while (rows.next()) ids.add(rows.getInt(1));
            }
            return ids;
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to list player settings", ex);
        }
    }

    public void setBoolean(int playerId, String key, boolean value) {
        upsert(
                "INSERT INTO player_settings_bool (player_id, key, value) VALUES (?, ?, ?) " +
                        "ON CONFLICT(player_id, key) DO UPDATE SET value=excluded.value",
                ps -> {
                    ps.setInt(1, playerId);
                    ps.setString(2, key);
                    ps.setInt(3, value ? 1 : 0);
                });
    }

    public Optional<Integer> getInt(int playerId, String key) {
        return querySingle(
                "SELECT value FROM player_settings_int WHERE player_id=? AND key=?",
                ps -> {
                    ps.setInt(1, playerId);
                    ps.setString(2, key);
                },
                rs -> rs.getInt("value"));
    }

    public void setInt(int playerId, String key, int value) {
        upsert(
                "INSERT INTO player_settings_int (player_id, key, value) VALUES (?, ?, ?) " +
                        "ON CONFLICT(player_id, key) DO UPDATE SET value=excluded.value",
                ps -> {
                    ps.setInt(1, playerId);
                    ps.setString(2, key);
                    ps.setInt(3, value);
                });
    }

    public Optional<String> getString(int playerId, String key) {
        return querySingle(
                "SELECT value FROM player_settings_string WHERE player_id=? AND key=?",
                ps -> {
                    ps.setInt(1, playerId);
                    ps.setString(2, key);
                },
                rs -> rs.getString("value"));
    }

    public void setString(int playerId, String key, String value) {
        upsert(
                "INSERT INTO player_settings_string (player_id, key, value) VALUES (?, ?, ?) " +
                        "ON CONFLICT(player_id, key) DO UPDATE SET value=excluded.value",
                ps -> {
                    ps.setInt(1, playerId);
                    ps.setString(2, key);
                    ps.setString(3, value);
                });
    }

    public Optional<Float> getFloat(int playerId, String key) {
        return querySingle(
                "SELECT value FROM player_settings_float WHERE player_id=? AND key=?",
                ps -> {
                    ps.setInt(1, playerId);
                    ps.setString(2, key);
                },
                rs -> rs.getFloat("value"));
    }

    public void setFloat(int playerId, String key, float value) {
        upsert(
                "INSERT INTO player_settings_float (player_id, key, value) VALUES (?, ?, ?) " +
                        "ON CONFLICT(player_id, key) DO UPDATE SET value=excluded.value",
                ps -> {
                    ps.setInt(1, playerId);
                    ps.setString(2, key);
                    ps.setFloat(3, value);
                });
    }

    /** Removes every shared plugin setting and consent decision for a player. Call last in an erasure workflow. */
    public synchronized void erasePlayer(int playerId) {
        if (playerId <= 0) throw new IllegalArgumentException("Player DBID must be positive");
        try {
            boolean previousAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                for (String table : List.of("player_settings_bool", "player_settings_int",
                        "player_settings_string", "player_settings_float")) {
                    try (PreparedStatement statement = connection.prepareStatement(
                            "DELETE FROM " + table + " WHERE player_id = ?")) {
                        statement.setInt(1, playerId);
                        statement.executeUpdate();
                    }
                }
                connection.commit();
            } catch (SQLException | RuntimeException ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(previousAutoCommit);
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to erase player settings", ex);
        }
    }

    private synchronized void upsert(String sql, SQLConsumer<PreparedStatement> binder) {
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            binder.accept(ps);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to execute upsert", e);
        }
    }

    private synchronized <T> Optional<T> querySingle(
            String sql,
            SQLConsumer<PreparedStatement> binder,
            SQLFunction<ResultSet, T> mapper) {
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            binder.accept(ps);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.ofNullable(mapper.apply(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to execute query", e);
        }
    }

    @FunctionalInterface
    private interface SQLConsumer<T> {
        void accept(T t) throws SQLException;
    }

    @FunctionalInterface
    private interface SQLFunction<T, R> {
        R apply(T t) throws SQLException;
    }
}
