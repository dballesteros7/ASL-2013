/**
* Create.java
* Created: Oct 10, 2013
* Author: Diego Ballesteros (diegob)
*/
package org.ftab.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * DAO for creating the database schema for the system
 */
public class Create {
	
	// -------------------------------------------------------------------------
	// Table creation
	// -------------------------------------------------------------------------
	
	/**
	 * SQL query to create the client table.
	 */
	private final static String SQL01_CREATE_CLIENT = "CREATE TABLE client ("
			+ "id SERIAL PRIMARY KEY,"
			+ "username VARCHAR(200) NOT NULL,"
			+ "online BOOLEAN DEFAULT false,"
			+ "UNIQUE(username))";
	
	/**
	 * SQL query to create the queue table.
	 */
	private final static String SQL01_CREATE_QUEUE = "CREATE TABLE queue ("
		+ "id BIGSERIAL PRIMARY KEY,"
		+ "name VARCHAR(200) NOT NULL,"
		+ "UNIQUE(name))";
	
	/**
	 * SQL query to create the message table.
	 */
	private final static String SQL02_CREATE_MSG = "CREATE TABLE message ("
			+ "id BIGSERIAL PRIMARY KEY,"
			+ "sender INTEGER NOT NULL,"
			+ "receiver INTEGER,"
			+ "context SMALLINT DEFAULT 0,"
			+ "prio SMALLINT DEFAULT 0,"
			+ "create_time INTEGER NOT NULL,"
			+ "message VARCHAR(2000) NOT NULL,"
			+ "FOREIGN KEY (sender) REFERENCES client(id) ON DELETE CASCADE,"
			+ "FOREIGN KEY (receiver) REFERENCES client(id) ON DELETE CASCADE)";
	
	private final static String SQL03_CREATE_MSG_QUEUE_ASSOC = "CREATE TABLE "
			+ "msg_queue_assoc ("
			+ "message_id BIGINT NOT NULL references message (id),"
			+ "queue_id BIGINT NOT NULL references queue (id),"
			+ "PRIMARY KEY (message_id, queue_id))";
	
	// -------------------------------------------------------------------------
	// Index creation
	// -------------------------------------------------------------------------
	
	/**
	 * SQL query to create an index on the message table by sender.
	 */
	private final static String SQL03_MSG_SENDER_IDX = "CREATE INDEX "
			+ "msg_sender_idx ON message (sender)";
	
	/**
	 * SQL query to create an index on the message table by receiver.
	 */
	private final static String SQL03_MSG_RECEIVER_IDX = "CREATE INDEX "
			+ "msg_receiver_idx ON message (receiver)";
	
	/**
	 * SQL query to create an index on the message table by priority.
	 */
	private final static String SQL03_MSG_PRIO_IDX = "CREATE INDEX "
			+ "msg_prio_idx ON message (prio DESC)";
	
	/**
	 * SQL query to create an index on the message table by create_time.
	 */
	private final static String SQL03_MSG_TIME_IDX = "CREATE INDEX "
			+ "msg_time_idx ON message (create_time DESC)";
	
	/**
	 * SQL query to create an index on the msg_queue_assoc table by message id.
	 */
	private final static String SQL04_MSG_QUEUE_ASSOC_MSG_IDX = "CREATE INDEX "
			+ "msg_queue_assoc_msg_idx ON msg_queue_assoc (message_id)";
	
	/**
	 * SQL query to create an index on the msg_queue_assoc table by queue id.
	 */
	private final static String SQL04_MSG_QUEUE_ASSOC_QUEUE_IDX = "CREATE INDEX"
			+ " msg_queue_assoc_queue_idx ON msg_queue_assoc (queue_id)";

	/**
	 * Auxiliary function to add the creation of the client table to a batch
	 * statement.
	 * @param stmt batch statement.
	 * @throws SQLException if the query can't be added to the statement.
	 */
	private void createClientTable(Statement stmt) throws SQLException {
		stmt.addBatch(SQL01_CREATE_CLIENT);
	}
	
	/**
	 * Auxiliary function to add the creation of the queue table to a batch
	 * statement.
	 * @param stmt batch statement.
	 * @throws SQLException if the query can't be added to the statement.
	 */
	private void createQueueTable(Statement stmt) throws SQLException {
		stmt.addBatch(SQL01_CREATE_QUEUE);
	}
	
	/**
	 * Auxiliary function to add the creation of the message table to a batch
	 * statement, it assumes that the queue and client table creation is already
	 * in the statement.
	 * @param stmt batch statement.
	 * @throws SQLException if the queries can't be added to the statement.
	 */
	private void createMessageTable(Statement stmt) throws SQLException {
		stmt.addBatch(SQL02_CREATE_MSG);
		stmt.addBatch(SQL03_CREATE_MSG_QUEUE_ASSOC);
		stmt.addBatch(SQL03_MSG_PRIO_IDX);
		stmt.addBatch(SQL03_MSG_RECEIVER_IDX);
		stmt.addBatch(SQL03_MSG_SENDER_IDX);
		stmt.addBatch(SQL03_MSG_TIME_IDX);
		stmt.addBatch(SQL04_MSG_QUEUE_ASSOC_MSG_IDX);
		stmt.addBatch(SQL04_MSG_QUEUE_ASSOC_QUEUE_IDX);
	}
	
	/**
	 * Create the requested tables in the database.
	 * @param client indicates if the client table should be created.
	 * @param queue indicates if the queue table should be created.
	 * @param msg indicates if the message table should be created,
	 * 			  creating the message table implies creating the client and
	 * 			  queue table.
	 * @param conn database connection.
	 * @throws SQLException if the table creation fails.
	 */
	public void execute(boolean client, boolean queue, boolean msg, 
			Connection conn) throws SQLException {
		Statement stmt = null;
		try{
			stmt = conn.createStatement();
			if(msg){
				createClientTable(stmt);
				createQueueTable(stmt);
				createMessageTable(stmt);
				stmt.executeBatch();
			} else {
				if(queue)
					createQueueTable(stmt);
				if(client)
					createClientTable(stmt);
				stmt.executeBatch();
			}
		} catch (SQLException ex) {
			throw ex;
		} finally {
			if(stmt != null)
				stmt.close();
		}
	}
	
}
