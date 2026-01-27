package de.omegazirkel.risingworld.tools.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.omegazirkel.risingworld.tools.db.interfaces.DatabaseSchema;
import de.omegazirkel.risingworld.tools.db.interfaces.PersistenceBackend;
import de.omegazirkel.risingworld.tools.db.interfaces.SQLiteEntityMapper;

public final class SQLitePersistenceBackend<K, E>
        implements PersistenceBackend<E> {

    protected final Connection connection;
    protected final SQLiteEntityMapper<K, E> mapper;

    public SQLitePersistenceBackend(
            Connection connection,
            DatabaseSchema schema,
            SQLiteEntityMapper<K, E> mapper) throws SQLException {
        this.connection = connection;
        this.mapper = mapper;
        schema.init(connection);
    }

    // ---------------------------------------------------------------------
    // Save
    // ---------------------------------------------------------------------

    @Override
    public void saveAll(Collection<E> entities) {
        synchronized (connection) {
            try (
                    PreparedStatement insert = connection.prepareStatement(mapper.insertSql());
                    PreparedStatement update = connection.prepareStatement(mapper.updateSql())) {
                connection.setAutoCommit(false);

                for (E e : entities) {
                    if (!tryInsert(insert, e)) {
                        mapper.bindUpdate(update, e);
                        update.executeUpdate();
                    }
                }

                connection.commit();
            } catch (SQLException e) {
                try {
                    connection.rollback();
                } catch (SQLException ignored) {
                }
                throw new RuntimeException(e);
            } finally {
                try {
                    connection.setAutoCommit(true);
                } catch (SQLException ignored) {
                }
            }
        }
    }

    protected boolean tryInsert(
            PreparedStatement ps,
            E entity) throws SQLException {
        try {
            mapper.bindInsert(ps, entity);
            ps.executeUpdate();
            return true;
        } catch (SQLException ex) {
            // PK / UNIQUE constraint → UPDATE
            if (ex.getMessage().toLowerCase().contains("constraint")) {
                return false;
            }
            throw ex;
        }
    }

    // ---------------------------------------------------------------------
    // Load
    // ---------------------------------------------------------------------

    @Override
    public List<E> loadAll() {
        synchronized (connection) {
            try (
                    PreparedStatement ps = connection.prepareStatement(mapper.selectAllSql());
                    ResultSet rs = ps.executeQuery()) {
                List<E> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(mapper.fromResultSet(rs));
                }
                return result;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    // ---------------------------------------------------------------------
    // Delete
    // ---------------------------------------------------------------------

    @Override
    public void delete(E entity) {
        synchronized (connection) {
            try (
                    PreparedStatement ps = connection.prepareStatement(mapper.deleteSql())) {
                mapper.bindDelete(ps, entity);
                ps.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
}