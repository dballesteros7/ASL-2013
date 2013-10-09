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
	private static final String SQL_BY_USERNAME = "UPDATE client SET online = ?"
			+ " WHERE username = ?";
	
	/**
	 * Change the online status of the client given its username.
	 * @param username client's username.
	 * @param conn database connection.
	 * @throws SQLException if the update can't be performed.
	 */
	public void execute(String username, Connection conn) throws SQLException {
		PreparedStatement stmt = null;
		try{
			stmt = conn.prepareStatement(SQL_BY_USERNAME);
			stmt.setString(1, username);
			stmt.executeUpdate();
		} catch (SQLException ex) {
			throw ex;
		} finally {
			if(stmt != null)
				stmt.close();
		}
	}
	
	/**
	 * Change the online status of the client given its id.
	 * @param id client's unique id.
	 * @param conn database connection.
	 * @throws SQLException if the update can't be performed.
	 */
	public void execute(int id, Connection conn) throws SQLException {
		PreparedStatement stmt = null;
		try{
			stmt = conn.prepareStatement(SQL_BY_ID);
			stmt.setInt(1, id);
			stmt.executeUpdate();
		} catch (SQLException ex) {
			throw ex;
		} finally {
			if(stmt != null)
				stmt.close();
		}
	}
}
