package de.omegazirkel.risingworld.tools.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import de.omegazirkel.risingworld.tools.OZLogger;
import de.omegazirkel.risingworld.tools.PluginSettings;
import net.risingworld.api.Plugin;
import net.risingworld.api.World;
import net.risingworld.api.database.Database;

/**
 * A wrapper class for SQLite Database
 * @deprecated
 */
public class SQLite {
    private Plugin plugin = null;
    private Database db = null;

    private static PluginSettings s = PluginSettings.getInstance();

    public static OZLogger logger() {
        return OZLogger.getInstance("OZ.Tools.SQLite");
    }

    /**
     *
     * @param plugin
     */
    public SQLite(Plugin plugin) {
        this.plugin = plugin;
        logger().setLevel(s.logLevel);
        initDatabase();
    }

    public Connection getConnection(){
        if(db == null) return null;
        return db.getConnection();
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
