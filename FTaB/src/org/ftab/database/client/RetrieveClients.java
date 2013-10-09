/**
* RetrieveClients.java
* Created: Oct 10, 2013
* Author: Diego Ballesteros (diegob)
*/
package org.ftab.database.client;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.ftab.server.UserClient;

/**
 * DAO for retrieving a snapshot of the client table in the database.
 */
public class RetrieveClients {
	
	/**
	 * SQL statement to retrieve all rows from the client table.
	 */
	private final static String SQL = "SELECT id, username, online FROM client";
	
	/**
	 * Retrieve information about all the clients in the database.
	 * @param conn database connection.
	 * @return list of UserClient objects with the information.
	 * @throws SQLException if the information can't be retrieved.
	 */
	public ArrayList<UserClient> execute(Connection conn) throws SQLException {
		PreparedStatement stmt = null;
		ArrayList<UserClient> formattedResult = new ArrayList<UserClient>();
		try {
			stmt = conn.prepareStatement(SQL);
			ResultSet result = stmt.executeQuery();
			while(result.next()){
				UserClient client = new UserClient(result.getInt(1),
						result.getString(2), result.getBoolean(3));
				formattedResult.add(client);
			}
			return formattedResult;
		} catch (SQLException ex) {
			throw ex;
		} finally {
			if(stmt != null)
				stmt.close();
		}
	}
}
