package de.omegazirkel.risingworld.tools.db;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Opens a game-owned SQLite database without allowing a reader to checkpoint its WAL. */
public final class ReadOnlyWorldDatabase {
    private ReadOnlyWorldDatabase() {
    }

    public static Connection open(String path) throws SQLException {
        if (path == null || path.isBlank()) {
            throw new SQLException("World database path is missing");
        }
        String url = "jdbc:sqlite:" + Path.of(path).toAbsolutePath().toUri() + "?mode=ro";
        return DriverManager.getConnection(url);
    }
}
