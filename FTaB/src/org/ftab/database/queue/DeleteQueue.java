/**
* DeleteQueue.java
* Created: Oct 10, 2013
* Author: Diego Ballesteros (diegob)
*/
package org.ftab.database.queue;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * DAO for deleting a queue record in the database.
 */
public class DeleteQueue {

	private static final String SQL_BY_NAME = "DELETE FROM queue WHERE "
			+ "name = ? AND NOT EXISTS (SELECT message_id FROM msg_queue_assoc "
			+ "LEFT INNER JOIN queue ON queue.id = msg_queue_assoc.queue_id"
			+ "WHERE queue.name = ?)";
	
	private static final String SQL_BY_ID = "DELETE FROM queue WHERE id = ?"
			+ " AND NOT EXISTS (SELECT message_id FROM msg_queue_assoc "
			+ "WHERE queue_id = ?)";
	
	public void execute(String queueName, Connection conn) throws SQLException {
		
	}
}
