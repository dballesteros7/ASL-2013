/**
* CreateClient.java
* Created: Oct 10, 2013
* Author: Diego Ballesteros (diegob)
*/
package org.ftab.database.client;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * DAO for creating a client record in the database.
 */
public class CreateClient {
	/**
	 * SQL statement to insert a record to the client table.
	 */
	private static final String SQL = "INSERT INTO client (username, online) "
			+ "VALUES (?, ?)";
	/**
	 * Insert a new client with the given username and online status.
	 * @param username desired username for the client.
	 * @param online online status of the client.
	 * @param conn database connection.
	 * @throws SQLException if the statement fails to be created or executed.
	 */
	public void execute(String username, boolean online, Connection conn) 
		throws SQLException {
		PreparedStatement stmt = null;
		try{
			stmt = conn.prepareStatement(SQL);
			stmt.setString(1, username);
			stmt.setBoolean(2, online);
			stmt.executeUpdate();
		} catch (SQLException ex) {
			throw ex;
		} finally {
			if(stmt != null)
				stmt.close();
		}
	}
}
