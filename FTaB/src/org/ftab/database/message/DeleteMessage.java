/**
 * DeleteMessage.java
 * Created: Oct 11, 2013
 * Author: Diego Ballesteros (diegob)
 */
package org.ftab.database.message;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * DAO to pop a message from a queue given the message and queue name.
 */
public class DeleteMessage {

    /**
     * SQL statement to delete a message-queue association given a message and
     * queue name and delete the corresponding message record if the deleted
     * association was the last one, i.e. the message is not in any queue.
     */
    private final static String SQL = "WITH temp AS ( "
            + "DELETE FROM msg_queue_assoc WHERE message_id = ? "
            + "AND queue_id = (SELECT id FROM queue WHERE name = ?) "
            + "RETURNING queue_id, message_id) "
            + "DELETE FROM message USING temp "
            + "WHERE message.id = temp.message_id AND NOT EXISTS ("
            + "SELECT 1 FROM msg_queue_assoc msa "
            + "WHERE msa.message_id = temp.message_id AND "
            + "msa.queue_id <> temp.queue_id)";

    /**
     * Delete a message from a queue and subsequently delete the message if it
     * is not in anymore queues.
     * 
     * @param messageId
     *            id of the message to delete, expected to be a valid id.
     * @param queueName
     *            name of the queue where the message is present, expected to be
     *            a valid name.
     * @param conn
     *            database connection.
     * @throws SQLException
     *             if there is an unexpected error while deleting the records.
     */
    public static void execute(long messageId, String queueName, Connection conn)
            throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(SQL);
            stmt.setLong(1, messageId);
            stmt.setString(2, queueName);
            stmt.executeUpdate();
        } finally {
            if (stmt != null)
                stmt.close();
        }
    }
}
