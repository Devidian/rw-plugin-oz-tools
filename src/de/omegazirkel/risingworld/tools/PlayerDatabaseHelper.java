package de.omegazirkel.risingworld.tools;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.Set;

import de.omegazirkel.risingworld.OZTools;
import net.risingworld.api.Plugin;
import net.risingworld.api.database.Database;
import net.risingworld.api.database.WorldDatabase;
import net.risingworld.api.database.WorldDatabase.Target;

public final class PlayerDatabaseHelper {

    public static final class PlayerRecord {
        public final int dbId;
        public final String name;
        public final long lastSeenEpochSeconds;
        public final long totalPlayTimeSeconds;

        public PlayerRecord(int dbId, String name, long lastSeenEpochSeconds, long totalPlayTimeSeconds) {
            this.dbId = dbId;
            this.name = name;
            this.lastSeenEpochSeconds = lastSeenEpochSeconds;
            this.totalPlayTimeSeconds = totalPlayTimeSeconds;
        }
    }

    private PlayerDatabaseHelper() {
    }

    private static OZLogger logger() {
        return OZTools.logger();
    }

    public static List<Integer> findPlayersSeenSince(Plugin plugin, long cutoffEpochSeconds) {
        if (plugin == null) {
            return List.of();
        }

        WorldDatabase playersDatabase = plugin.getWorldDatabase(Target.Players);
        try {
            return findPlayersSeenSince(playersDatabase, cutoffEpochSeconds);
        } catch (UnsupportedOperationException ex) {
            logger().warn("Players WorldDatabase query is not supported, falling back to direct SQLite access.");
            return findPlayersSeenSinceViaSQLite(plugin, playersDatabase, cutoffEpochSeconds);
        }
    }

    public static Map<Integer, PlayerRecord> findPlayersByDbIds(Plugin plugin, Set<Integer> playerDbIds) {
        if (plugin == null || playerDbIds == null || playerDbIds.isEmpty()) {
            return Map.of();
        }

        WorldDatabase playersDatabase = plugin.getWorldDatabase(Target.Players);
        try {
            return findPlayersByDbIds(playersDatabase, playerDbIds);
        } catch (UnsupportedOperationException ex) {
            logger().warn("Players WorldDatabase query is not supported, falling back to direct SQLite access.");
            return findPlayersByDbIdsViaSQLite(plugin, playersDatabase, playerDbIds);
        }
    }

    /** Resolves a persisted player name without requiring the player to be online. */
    public static Optional<PlayerRecord> findPlayerByExactName(Plugin plugin, String playerName) {
        if (plugin == null || playerName == null || playerName.isBlank()) {
            return Optional.empty();
        }
        WorldDatabase playersDatabase = plugin.getWorldDatabase(Target.Players);
        try {
            return findPlayerByExactName(playersDatabase, playerName.trim());
        } catch (UnsupportedOperationException ex) {
            logger().warn("Players WorldDatabase query is not supported, falling back to direct SQLite access.");
            return findPlayerByExactNameViaSQLite(plugin, playersDatabase, playerName.trim());
        }
    }

    private static Optional<PlayerRecord> findPlayerByExactName(WorldDatabase playersDatabase, String playerName) {
        if (playersDatabase == null) {
            return Optional.empty();
        }
        try (ResultSet probe = playersDatabase.executeQuery("SELECT * FROM player LIMIT 1")) {
            ResultSetMetaData metaData = probe.getMetaData();
            String playerIdColumn = resolvePlayerIdColumn(metaData);
            String nameColumn = resolveFirstColumn(metaData, "name", "playername", "username");
            if (playerIdColumn == null || nameColumn == null) {
                return Optional.empty();
            }
            String lastSeenColumn = resolveFirstColumn(metaData, "lastseen", "last_seen", "lastonline");
            String playTimeColumn = resolveFirstColumn(metaData, "playtime", "totalplaytime", "total_playtime");
            String escaped = playerName.replace("'", "''");
            try (ResultSet result = playersDatabase.executeQuery("SELECT * FROM player WHERE LOWER(" + nameColumn
                    + ") = LOWER('" + escaped + "') LIMIT 1")) {
                Map<Integer, PlayerRecord> records = readPlayerRecords(result, playerIdColumn, nameColumn,
                        lastSeenColumn, playTimeColumn);
                return records.values().stream().findFirst();
            }
        } catch (SQLException ex) {
            logger().error("Failed to find player by name: " + ex.getMessage());
            return Optional.empty();
        }
    }

    private static Optional<PlayerRecord> findPlayerByExactNameViaSQLite(Plugin plugin, WorldDatabase playersDatabase,
            String playerName) {
        if (playersDatabase == null || playersDatabase.getPath() == null || playersDatabase.getPath().isBlank()) {
            return Optional.empty();
        }
        try (Database db = plugin.getSQLiteConnection(playersDatabase.getPath())) {
            if (db == null) {
                return Optional.empty();
            }
            try (PreparedStatement probe = db.getConnection().prepareStatement("SELECT * FROM player LIMIT 1");
                    ResultSet probeResult = probe.executeQuery()) {
                ResultSetMetaData metaData = probeResult.getMetaData();
                String playerIdColumn = resolvePlayerIdColumn(metaData);
                String nameColumn = resolveFirstColumn(metaData, "name", "playername", "username");
                if (playerIdColumn == null || nameColumn == null) {
                    return Optional.empty();
                }
                String lastSeenColumn = resolveFirstColumn(metaData, "lastseen", "last_seen", "lastonline");
                String playTimeColumn = resolveFirstColumn(metaData, "playtime", "totalplaytime", "total_playtime");
                try (PreparedStatement statement = db.getConnection().prepareStatement(
                        "SELECT * FROM player WHERE LOWER(" + nameColumn + ") = LOWER(?) LIMIT 1")) {
                    statement.setString(1, playerName);
                    try (ResultSet result = statement.executeQuery()) {
                        return readPlayerRecords(result, playerIdColumn, nameColumn, lastSeenColumn, playTimeColumn)
                                .values().stream().findFirst();
                    }
                }
            }
        } catch (SQLException ex) {
            logger().error("Failed to find player by name via SQLite fallback: " + ex.getMessage());
            return Optional.empty();
        }
    }

    public static Map<Integer, PlayerRecord> findPlayersByDbIds(WorldDatabase playersDatabase, Set<Integer> playerDbIds) {
        if (playersDatabase == null || playerDbIds == null || playerDbIds.isEmpty()) {
            return Map.of();
        }

        try {
            try (ResultSet probe = playersDatabase.executeQuery("SELECT * FROM player LIMIT 1")) {
                ResultSetMetaData metaData = probe.getMetaData();
                String playerIdColumn = resolvePlayerIdColumn(metaData);
                if (playerIdColumn == null) {
                    logger().warn("Players database table `player` has no supported player id column.");
                    return Map.of();
                }
                String nameColumn = resolveFirstColumn(metaData, "name", "playername", "username");
                String lastSeenColumn = resolveFirstColumn(metaData, "lastseen", "last_seen", "lastonline");
                String playTimeColumn = resolveFirstColumn(metaData, "playtime", "totalplaytime", "total_playtime");

                String sql = "SELECT * FROM player WHERE " + playerIdColumn + " IN (" + idList(playerDbIds) + ")";
                try (ResultSet rs = playersDatabase.executeQuery(sql)) {
                    return readPlayerRecords(rs, playerIdColumn, nameColumn, lastSeenColumn, playTimeColumn);
                }
            }
        } catch (SQLException ex) {
            logger().error("Failed to query players by db id: " + ex.getMessage());
            return Map.of();
        }
    }

    public static List<Integer> findPlayersSeenSince(WorldDatabase playersDatabase, long cutoffEpochSeconds) {
        if (playersDatabase == null) {
            return List.of();
        }

        try {
            String playerIdColumn = resolvePlayerIdColumn(playersDatabase);
            if (playerIdColumn == null) {
                logger().warn("Players database table `player` has no supported player id column.");
                return List.of();
            }

            String sql = "SELECT " + playerIdColumn + " FROM player WHERE lastseen >= " + cutoffEpochSeconds
                    + " ORDER BY lastseen DESC";
            Set<Integer> playerIds = new LinkedHashSet<>();
            try (ResultSet rs = playersDatabase.executeQuery(sql)) {
                while (rs.next()) {
                    playerIds.add(rs.getInt(playerIdColumn));
                }
            }
            return new ArrayList<>(playerIds);
        } catch (SQLException ex) {
            logger().error("Failed to query recently seen players: " + ex.getMessage());
            return List.of();
        }
    }

    private static List<Integer> findPlayersSeenSinceViaSQLite(
            Plugin plugin,
            WorldDatabase playersDatabase,
            long cutoffEpochSeconds) {
        if (playersDatabase == null || playersDatabase.getPath() == null || playersDatabase.getPath().isBlank()) {
            return List.of();
        }

        try (Database db = plugin.getSQLiteConnection(playersDatabase.getPath())) {
            if (db == null) {
                return List.of();
            }

            String playerIdColumn = resolvePlayerIdColumn(db.getConnection());
            if (playerIdColumn == null) {
                logger().warn("Players database table `player` has no supported player id column.");
                return List.of();
            }

            String sql = "SELECT " + playerIdColumn + " FROM player WHERE lastseen >= ? ORDER BY lastseen DESC";
            Set<Integer> playerIds = new LinkedHashSet<>();
            try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
                ps.setLong(1, cutoffEpochSeconds);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        playerIds.add(rs.getInt(playerIdColumn));
                    }
                }
            }
            return new ArrayList<>(playerIds);
        } catch (SQLException ex) {
            logger().error("Failed to query recently seen players via SQLite fallback: " + ex.getMessage());
            return List.of();
        }
    }

    private static Map<Integer, PlayerRecord> findPlayersByDbIdsViaSQLite(
            Plugin plugin,
            WorldDatabase playersDatabase,
            Set<Integer> playerDbIds) {
        if (playersDatabase == null || playersDatabase.getPath() == null || playersDatabase.getPath().isBlank()) {
            return Map.of();
        }

        try (Database db = plugin.getSQLiteConnection(playersDatabase.getPath())) {
            if (db == null) {
                return Map.of();
            }

            try (PreparedStatement probe = db.getConnection().prepareStatement("SELECT * FROM player LIMIT 1");
                    ResultSet probeResult = probe.executeQuery()) {
                ResultSetMetaData metaData = probeResult.getMetaData();
                String playerIdColumn = resolvePlayerIdColumn(metaData);
                if (playerIdColumn == null) {
                    logger().warn("Players database table `player` has no supported player id column.");
                    return Map.of();
                }
                String nameColumn = resolveFirstColumn(metaData, "name", "playername", "username");
                String lastSeenColumn = resolveFirstColumn(metaData, "lastseen", "last_seen", "lastonline");
                String playTimeColumn = resolveFirstColumn(metaData, "playtime", "totalplaytime", "total_playtime");

                String sql = "SELECT * FROM player WHERE " + playerIdColumn + " IN (" + idList(playerDbIds) + ")";
                try (PreparedStatement ps = db.getConnection().prepareStatement(sql);
                        ResultSet rs = ps.executeQuery()) {
                    return readPlayerRecords(rs, playerIdColumn, nameColumn, lastSeenColumn, playTimeColumn);
                }
            }
        } catch (SQLException ex) {
            logger().error("Failed to query players by db id via SQLite fallback: " + ex.getMessage());
            return Map.of();
        }
    }

    private static String resolvePlayerIdColumn(WorldDatabase playersDatabase) throws SQLException {
        try (ResultSet rs = playersDatabase.executeQuery("SELECT * FROM player LIMIT 1")) {
            return resolvePlayerIdColumn(rs.getMetaData());
        }
    }

    private static String resolvePlayerIdColumn(Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM player LIMIT 1");
                ResultSet rs = ps.executeQuery()) {
            return resolvePlayerIdColumn(rs.getMetaData());
        }
    }

    private static String resolvePlayerIdColumn(ResultSetMetaData metaData) throws SQLException {
        for (String candidate : List.of("dbid", "player_dbid", "playerid", "id")) {
            if (hasColumn(metaData, candidate)) {
                return candidate;
            }
        }
        return null;
    }

    private static String resolveFirstColumn(ResultSetMetaData metaData, String... candidates) throws SQLException {
        for (String candidate : candidates) {
            if (hasColumn(metaData, candidate)) {
                return candidate;
            }
        }
        return null;
    }

    private static String idList(Set<Integer> playerDbIds) {
        return playerDbIds.stream()
                .filter(id -> id != null && id > 0)
                .map(String::valueOf)
                .reduce((a, b) -> a + "," + b)
                .orElse("0");
    }

    private static Map<Integer, PlayerRecord> readPlayerRecords(
            ResultSet rs,
            String playerIdColumn,
            String nameColumn,
            String lastSeenColumn,
            String playTimeColumn) throws SQLException {
        Map<Integer, PlayerRecord> records = new HashMap<>();
        while (rs.next()) {
            int dbId = rs.getInt(playerIdColumn);
            String name = nameColumn == null ? null : rs.getString(nameColumn);
            long lastSeen = lastSeenColumn == null ? 0L : rs.getLong(lastSeenColumn);
            long playTime = playTimeColumn == null ? 0L : rs.getLong(playTimeColumn);
            records.put(dbId, new PlayerRecord(dbId, name, lastSeen, playTime));
        }
        return records;
    }

    private static boolean hasColumn(ResultSetMetaData metaData, String columnName) throws SQLException {
        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            if (columnName.equalsIgnoreCase(metaData.getColumnLabel(i))
                    || columnName.equalsIgnoreCase(metaData.getColumnName(i))) {
                return true;
            }
        }
        return false;
    }
}
