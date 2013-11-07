/**
* Destroy.java
* Created: Oct 10, 2013
* Author: Diego Ballesteros (diegob)
*/
package org.ftab.database;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;

/**
 * DAO for dropping the system's table from the database.
 * It is meant only for tests and should be used carefully.
 */
public class Destroy {
	
	// ------------------------------------------------------------------------
	// Table drop statements
	// ------------------------------------------------------------------------
	/**
	 * SQL statement to drop the client table
	 */
	private static final String SQL_DROP_CLIENT = "DROP TABLE client";
	/**
	 * SQL statement to drop the client table
	 */
	private static final String SQL_DROP_QUEUE = "DROP TABLE queue";
	/**
	 * SQL statement to drop the client table
	 */
	private static final String SQL_DROP_MESSSAGE = "DROP TABLE message";
	/**
	 * SQL statement to drop the client table
	 */
	private static final String SQL_DROP_MSG_QUEUE_ASSOC = "DROP TABLE "
			+ "msg_queue_assoc";
	
	/**
	 * Drop the requested tables in the database.
	 * @param client indicates if the client table should be dropped.
	 * @param queue indicates if the queue table should be dropped.
	 * @param msg indicates if the message table should be dropped.
	 * @param conn database connection.
	 * @throws SQLException if one of the tables could not be dropped.
	 */
	public void execute(boolean client, boolean queue, boolean msg,
			Connection conn) throws SQLException{
		Statement stmt = null;
		try{
			stmt = conn.createStatement();
			if(msg){
				stmt.addBatch(SQL_DROP_MSG_QUEUE_ASSOC);
				stmt.addBatch(SQL_DROP_MESSSAGE);
			}
			if(queue)
				stmt.addBatch(SQL_DROP_QUEUE);
			if(client)
				stmt.addBatch(SQL_DROP_CLIENT);
			stmt.executeBatch();
		} catch (SQLException ex) {
			throw ex;
		} finally {
			if(stmt != null)
				stmt.close();
		}
	}

}
