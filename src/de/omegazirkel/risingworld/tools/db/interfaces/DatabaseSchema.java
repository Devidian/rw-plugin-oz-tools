package de.omegazirkel.risingworld.tools.db.interfaces;

import java.sql.Connection;
import java.sql.SQLException;

public interface DatabaseSchema {
    void init(Connection con) throws SQLException;
}
