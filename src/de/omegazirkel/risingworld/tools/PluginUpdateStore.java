package de.omegazirkel.risingworld.tools;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

/** Persistent cache of public release metadata. It avoids losing useful update
 * state on a server restart; explicit manual checks always refresh it. */
final class PluginUpdateStore {
    private final Connection connection;

    PluginUpdateStore(Connection connection) {
        this.connection = connection;
        init();
    }

    private void init() {
        try (PreparedStatement statement = connection.prepareStatement("""
                CREATE TABLE IF NOT EXISTS plugin_update_checks (
                    plugin_name TEXT PRIMARY KEY,
                    installed_version TEXT NOT NULL,
                    latest_version TEXT NOT NULL,
                    release_url TEXT NOT NULL,
                    release_notes TEXT NOT NULL,
                    state TEXT NOT NULL,
                    checked_at INTEGER NOT NULL
                )
                """)) {
            statement.execute();
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to initialize plugin update cache", ex);
        }
    }

    Map<String, PluginUpdateService.Result> load() {
        Map<String, PluginUpdateService.Result> loaded = new LinkedHashMap<>();
        try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM plugin_update_checks");
                ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                loaded.put(result.getString("plugin_name"), new PluginUpdateService.Result(
                        result.getString("installed_version"), result.getString("latest_version"),
                        result.getString("release_url"), result.getString("release_notes"),
                        PluginUpdateService.State.valueOf(result.getString("state")), result.getLong("checked_at")));
            }
        } catch (SQLException | IllegalArgumentException ex) {
            throw new IllegalStateException("Failed to load plugin update cache", ex);
        }
        return loaded;
    }

    void save(String pluginName, PluginUpdateService.Result update) {
        try (PreparedStatement statement = connection.prepareStatement("""
                INSERT INTO plugin_update_checks (plugin_name, installed_version, latest_version, release_url, release_notes, state, checked_at)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT(plugin_name) DO UPDATE SET installed_version=excluded.installed_version,
                    latest_version=excluded.latest_version, release_url=excluded.release_url,
                    release_notes=excluded.release_notes, state=excluded.state, checked_at=excluded.checked_at
                """)) {
            statement.setString(1, pluginName);
            statement.setString(2, update.installedVersion());
            statement.setString(3, update.latestVersion());
            statement.setString(4, update.releaseUrl());
            statement.setString(5, update.releaseNotes());
            statement.setString(6, update.state().name());
            statement.setLong(7, update.checkedAtEpochMillis());
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to save plugin update cache", ex);
        }
    }
}
