/**
 * ChangeClientStatus.java
 * Created: Oct 10, 2013
 * Author: Diego Ballesteros (diegob)
 */
package org.ftab.database.client;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * DAO for changing the online status of a client.
 */
public class ChangeClientStatus {
    /**
     * SQL statement to modify the online status of a client using its id field.
     */
    private static final String SQL_BY_ID = "UPDATE client SET online = ? "
            + "WHERE id = ?";
    /**
     * SQL statement to modify the online status of client using its username
     * field.
     */
    private static final String SQL_BY_USERNAME =
            "UPDATE client SET online = ?" + " WHERE username = ?";

    /**
     * Change the online status of the client given its username.
     * 
     * @param username
     *            client's username.
     * @param status
     *            online status.
     * @param conn
     *            database connection.
     * @throws SQLException
     *             if the update can't be performed.
     */
    public void execute(String username, boolean status, Connection conn)
            throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(SQL_BY_USERNAME);
            stmt.setBoolean(1, status);
            stmt.setString(2, username);
            stmt.executeUpdate();
        } finally {
            if (stmt != null)
                stmt.close();
        }
    }

    /**
     * Change the online status of the client given its id.
     * 
     * @param id
     *            client's unique id.
     * @param status
     *            online status.
     * @param conn
     *            database connection.
     * @throws SQLException
     *             if the update can't be performed.
     */
    public void execute(int id, boolean status, Connection conn)
            throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(SQL_BY_ID);
            stmt.setBoolean(1, status);
            stmt.setInt(2, id);
            stmt.executeUpdate();
        } finally {
            if (stmt != null)
                stmt.close();
        }
    }
}
