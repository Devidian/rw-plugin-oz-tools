package de.omegazirkel.risingworld.tools.db.interfaces;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public interface SQLiteEntityMapper<K, E> extends EntityMapper<K, E> {

    /* ---------- Metadata ---------- */

    String tableName();
    
    String insertSql();
    
    String updateSql();
    
    String deleteSql();
    
    String selectAllSql();

    /* ---------- Mapping ---------- */

    E fromResultSet(ResultSet rs) throws SQLException;

    /* ---------- Bindings ---------- */

    void bindInsert(PreparedStatement ps, E entity) throws SQLException;

    void bindUpdate(PreparedStatement ps, E entity) throws SQLException;

    void bindDelete(PreparedStatement ps, E entity) throws SQLException;
}

