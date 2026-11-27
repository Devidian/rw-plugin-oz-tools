package de.omegazirkel.risingworld.tools.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;

import de.omegazirkel.risingworld.tools.db.interfaces.SQLiteEntityMapper;

public class SQLiteCachedStoreShutdownTest {
    @Test
    public void shutdownPersistsLatestValuesAndRetiresTimerOnce() throws SQLException {
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite::memory:")) {
            AtomicInteger stops = new AtomicInteger();
            AtomicReference<Runnable> tick = new AtomicReference<>();
            Store store = new Store(connection, tick, stops);
            store.put(1, new Row(1, "initial"));
            tick.get().run();
            store.put(1, new Row(1, "pending"));
            store.put(2, new Row(2, "new"));
            store.shutdown();
            tick.get().run();
            store.shutdown();

            assertEquals("pending", value(connection, 1));
            assertEquals("new", value(connection, 2));
            assertEquals(1, stops.get());
            assertTrue(store.values().isEmpty());
        }
    }

    @Test
    public void failedShutdownLeavesDirtyStateAvailableForARetry() throws SQLException {
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite::memory:");
                var statement = connection.createStatement()) {
            AtomicInteger stops = new AtomicInteger();
            Store store = new Store(connection, new AtomicReference<>(), stops);
            store.put(1, new Row(1, "pending"));
            statement.execute("DROP TABLE rows");

            assertThrows(RuntimeException.class, store::shutdown);
            assertEquals(new Row(1, "pending"), store.get(1));
            statement.execute("CREATE TABLE rows (id INTEGER PRIMARY KEY, value TEXT NOT NULL)");
            store.shutdown();

            assertEquals("pending", value(connection, 1));
            assertEquals(1, stops.get());
        }
    }

    private static String value(Connection connection, int id) throws SQLException {
        try (var query = connection.prepareStatement("SELECT value FROM rows WHERE id = ?")) {
            query.setInt(1, id);
            try (var result = query.executeQuery()) {
                assertTrue(result.next());
                return result.getString(1);
            }
        }
    }

    private record Row(int id, String value) {}

    private static final class Store extends SQLiteCachedStore<Integer, Row> {
        Store(Connection connection, AtomicReference<Runnable> tick, AtomicInteger stops) throws SQLException {
            super(connection, con -> {
                try (var statement = con.createStatement()) {
                    statement.execute("CREATE TABLE rows (id INTEGER PRIMARY KEY, value TEXT NOT NULL)");
                }
            }, new Mapper(), run -> {
                tick.set(run);
                return stops::incrementAndGet;
            });
        }
    }

    private static final class Mapper implements SQLiteEntityMapper<Integer, Row> {
        @Override public String tableName() { return "rows"; }
        @Override public Integer keyOf(Row row) { return row.id(); }
        @Override public String insertSql() { return "INSERT INTO rows (value, id) VALUES (?, ?)"; }
        @Override public String updateSql() { return "UPDATE rows SET value = ? WHERE id = ?"; }
        @Override public String deleteSql() { return "DELETE FROM rows WHERE id = ?"; }
        @Override public String selectAllSql() { return "SELECT id, value FROM rows"; }
        @Override public Row fromResultSet(ResultSet rs) throws SQLException {
            return new Row(rs.getInt(1), rs.getString(2));
        }
        @Override public void bindInsert(PreparedStatement statement, Row row) throws SQLException {
            statement.setString(1, row.value());
            statement.setInt(2, row.id());
        }
        @Override public void bindUpdate(PreparedStatement statement, Row row) throws SQLException {
            bindInsert(statement, row);
        }
        @Override public void bindDelete(PreparedStatement statement, Row row) throws SQLException {
            statement.setInt(1, row.id());
        }
    }
}
