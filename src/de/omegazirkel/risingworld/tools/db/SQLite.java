package de.omegazirkel.risingworld.tools.db;

import java.sql.ResultSet;
import java.sql.SQLException;

import de.omegazirkel.risingworld.tools.OZLogger;
import net.risingworld.api.Plugin;
import net.risingworld.api.World;
import net.risingworld.api.database.Database;

/**
 * A wrapper class for SQLite Database
 */
public class SQLite {
    private Plugin plugin = null;
    private Database db = null;

    public static OZLogger logger() {
        return OZLogger.getInstance("OZ.Tools.i18n");
    }

    /**
     *
     * @param plugin
     */
    public SQLite(Plugin plugin) {
        this.plugin = plugin;
        initDatabase();
    }

    /**
     *
     * @param plugin
     * @param logLevel
     */
    public SQLite(Plugin plugin, int logLevel) {
        this.plugin = plugin;
        initDatabase();
    }

    private void initDatabase() {
        if (db == null) {
            String path = plugin.getPath() + "/" + World.getName() + ".db";
            db = plugin.getSQLiteConnection(path);
            logger().info("Connected to " + path);
        }
    }

    public Database getRawDatabase() {
        return db;
    }

    public ResultSet executeQuery(String query) throws SQLException {
        initDatabase();
        try {
            return db.executeQuery(query);
        } catch (Exception e) {
            logger().fatal("Exception (" + e.getClass().getTypeName() + "): " + e.getMessage());
        }
        return null;
    }

    public void executeUpdate(String query) {
        initDatabase();
        try {
            db.executeUpdate(query);
        } catch (Exception e) {
            logger().fatal("Exception (" + e.getClass().getTypeName() + "): " + e.getMessage());
        }
    }

    public void execute(String query) {
        initDatabase();
        try {
            db.execute(query);
        } catch (Exception e) {
            logger().fatal("Exception (" + e.getClass().getTypeName() + "): " + e.getMessage());
        }
    }

    public void destroy() {
        try {
            db.close();
            db = null;
        } catch (Exception e) {
            logger().fatal("Exception (" + e.getClass().getTypeName() + "): " + e.getMessage());
        }
    }
}
