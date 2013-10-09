/**
* CreateQueue.java
* Created: Oct 10, 2013
* Author: Diego Ballesteros (diegob)
*/
package org.ftab.database.queue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.ftab.database.exceptions.QueueAlreadyExistsException;

/**
 * DAO for creating a queue record in the database.
 */
public class CreateQueue {
	
	private static final String SQL = "INSERT INTO queue (name) VALUES (?)";
	
	/**
	 * Create the queue with the given name.
	 * @param queueName desired name for the queue.
	 * @param conn database connection.
	 * @throws SQLException if there is an unexpected error when accessing the
	 * 						database.
	 * @throws QueueAlreadyExistsException if there is already a queue with the
	 * 									   given name.
	 */
	public void execute(String queueName, Connection conn) throws SQLException,
		QueueAlreadyExistsException {
		PreparedStatement stmt = null;
		try{
			stmt = conn.prepareStatement(SQL);
			stmt.setString(1, queueName);
			stmt.executeUpdate();
		} catch (SQLException ex) {
			String sqlState = ex.getSQLState();
			if(sqlState == "23505"){
				// This means a unique violation which can only be caused
				// by trying to insert an already existing queue.
				throw new QueueAlreadyExistsException(ex);
			} else
				throw ex;
		} finally {
			if(stmt != null)
				stmt.close();
		}
	}

}
