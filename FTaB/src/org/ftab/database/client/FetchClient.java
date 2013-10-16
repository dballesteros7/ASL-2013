/**
 * FetchClient.java
 * Created: Oct 10, 2013
 * Author: Diego Ballesteros (diegob)
 */
package org.ftab.database.client;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.ftab.database.Client;

/**
 * DAO for retrieving an user from the database.
 */
public class FetchClient {

    /**
     * SQL statement to be executed by this DAO
     */
    private static final String SQL = "SELECT id, online FROM client "
            + "WHERE username = ?";

    /**
     * Retrieve a client given its username
     * 
     * @param username
     *            username of the client.
     * @param conn
     *            connection to the database.
     * @return object with the information about the client in the database if
     *         such exists, otherwise it is null.
     * @throws SQLException
     *             if a problem occurs while executing the query in the
     *             database.a
     */
    public static Client execute(String username, Connection conn) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(SQL);
            stmt.setString(1, username);
            ResultSet result = stmt.executeQuery();
            if (!result.next()) {
                return null;
            } else {
                Client formattedResult =
                        new Client(result.getInt(1), username,
                                result.getBoolean(2));
                return formattedResult;
            }
        } finally {
            if (stmt != null)
                stmt.close();
        }
    }

}
