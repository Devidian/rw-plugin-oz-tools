package de.omegazirkel.risingworld.tools;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import net.risingworld.api.Plugin;
import net.risingworld.api.database.Database;
import net.risingworld.api.database.WorldDatabase;
import net.risingworld.api.database.WorldDatabase.Target;

public final class PlayerDatabaseHelper {

    private static final OZLogger LOGGER = OZLogger.getInstance("OZ.Tools.PlayerDatabaseHelper");

    private PlayerDatabaseHelper() {
    }

    public static List<Integer> findPlayersSeenSince(Plugin plugin, long cutoffEpochSeconds) {
        if (plugin == null) {
            return List.of();
        }

        WorldDatabase playersDatabase = plugin.getWorldDatabase(Target.Players);
        try {
            return findPlayersSeenSince(playersDatabase, cutoffEpochSeconds);
        } catch (UnsupportedOperationException ex) {
            LOGGER.warn("Players WorldDatabase query is not supported, falling back to direct SQLite access.");
            return findPlayersSeenSinceViaSQLite(plugin, playersDatabase, cutoffEpochSeconds);
        }
    }

    public static List<Integer> findPlayersSeenSince(WorldDatabase playersDatabase, long cutoffEpochSeconds) {
        if (playersDatabase == null) {
            return List.of();
        }

        try {
            String playerIdColumn = resolvePlayerIdColumn(playersDatabase);
            if (playerIdColumn == null) {
                LOGGER.warn("Players database table `player` has no supported player id column.");
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
            LOGGER.error("Failed to query recently seen players: " + ex.getMessage());
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
                LOGGER.warn("Players database table `player` has no supported player id column.");
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
            LOGGER.error("Failed to query recently seen players via SQLite fallback: " + ex.getMessage());
            return List.of();
        }
    }

    private static String resolvePlayerIdColumn(WorldDatabase playersDatabase) throws SQLException {
        try (ResultSet rs = playersDatabase.executeQuery("SELECT * FROM player LIMIT 1")) {
            return resolvePlayerIdColumn(rs.getMetaData());
        }
        return null;
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
