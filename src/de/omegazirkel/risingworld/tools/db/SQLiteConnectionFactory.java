package de.omegazirkel.risingworld.tools.db;

import java.sql.Connection;

import net.risingworld.api.Plugin;
import net.risingworld.api.World;

public final class SQLiteConnectionFactory {

    private SQLiteConnectionFactory() {
    }

    public static Connection open(Plugin plugin) {
        return open(plugin, World.getName());
    }

    public static Connection open(Plugin plugin, String dbName) {
        String path = plugin.getPath() + "/" + dbName + ".db";
        return plugin.getSQLiteConnection(path).getConnection();
    }
}
