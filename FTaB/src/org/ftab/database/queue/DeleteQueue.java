/**
 * DeleteQueue.java
 * Created: Oct 10, 2013
 * Author: Diego Ballesteros (diegob)
 */
package org.ftab.database.queue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.ftab.database.exceptions.InexistentQueueException;
import org.ftab.database.exceptions.QueueNotEmptyException;

/**
 * DAO for deleting a queue record in the database.
 */
public class DeleteQueue {

    /**
     * SQL statement to retrieve the id of a queue given its name.
     */
    private static final String SQL_RETRIEVE_ID = "SELECT id FROM queue WHERE "
            + "name = ?";

    /**
     * SQL statement to delete an empty queue given its id.
     */
    private static final String SQL_DELETE_BY_ID = "DELETE FROM queue WHERE "
            + "id = ? AND NOT EXISTS (SELECT message_id FROM msg_queue_assoc "
            + "WHERE queue_id = ?)";

    /**
     * Delete a queue given its name, it only deletes non-empty queues.
     * 
     * @param queueName
     *            name of the queue.
     * @param conn
     *            database connection.
     * @throws SQLException
     *             if there is a problem with the database access.
     * @throws InexistentQueueException
     *             if there is no queue with the given name.
     * @throws QueueNotEmptyException
     *             if the queue is not empty.
     */
    public void execute(String queueName, Connection conn) throws SQLException,
            InexistentQueueException, QueueNotEmptyException {
        PreparedStatement query = null;
        PreparedStatement update = null;
        try {
            query = conn.prepareStatement(SQL_RETRIEVE_ID);
            update = conn.prepareStatement(SQL_DELETE_BY_ID);
            query.setString(1, queueName);
            ResultSet idSet = query.executeQuery();
            if (!idSet.next())
                throw new InexistentQueueException("Queue " + queueName
                        + "doesn't exist in the system.");
            else {
                long id = idSet.getLong(1);
                update.setLong(1, id);
                update.setLong(2, id);
                int result = update.executeUpdate();
                if (result == 0)
                    throw new QueueNotEmptyException("Queue " + queueName
                            + " can't be deleted because it is not empty.");
            }
        } finally {
            if (query != null)
                query.close();
            if (update != null)
                update.close();
        }

    }
}
