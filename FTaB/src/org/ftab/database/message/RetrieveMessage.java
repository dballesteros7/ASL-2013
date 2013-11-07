/**
 * RetrieveMessage.java
 * Created: Oct 10, 2013
 * Author: Diego Ballesteros (diegob)
 */
package org.ftab.database.message;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.ftab.database.Message;

/**
 * DAO for peeking at messages in the database, in every realization of this DAO
 * at most one message is returned.
 */
public class RetrieveMessage {

    /**
     * SQL statement to get the top message ordered by priority first from a
     * given sender in any queue, which queue is selected is not specified and
     * depends on the first record returned from the database. Only messages
     * that can be accessed by the given receiver are considered.
     */
    private final static String SQL_PEEK_BY_SENDER_PRIO =
            "SELECT message.id AS msg_id, queue.id AS queue_id, context, prio, "
                    + "create_time, message, queue.name, "
                    + "rc.username AS receiver FROM message "
                    + "INNER JOIN msg_queue_assoc "
                    + "ON msg_queue_assoc.message_id = message.id "
                    + "INNER JOIN queue ON queue_id = queue.id "
                    + "LEFT OUTER JOIN client rc ON rc.id = receiver "
                    + "WHERE sender = "
                    + "(SELECT id FROM client WHERE username = ?) AND "
                    + "(receiver = ? OR receiver IS NULL) "
                    + "ORDER BY prio DESC, create_time DESC "
                    + "FETCH FIRST ROW ONLY";

    /**
     * SQL statement to get the top message ordered by creation time first from
     * a given sender in any queue, which queue is selected is not specified and
     * depends on the first record returned from the database. Only messages
     * that can be accessed by the given receiver are considered.
     */
    private final static String SQL_PEEK_BY_SENDER_TIME =
            "SELECT message.id AS msg_id, queue.id AS queue_id, context, prio, "
                    + "create_time, message, queue.name, "
                    + "rc.username AS receiver FROM message "
                    + "INNER JOIN msg_queue_assoc "
                    + "ON msg_queue_assoc.message_id = message.id "
                    + "INNER JOIN queue ON queue_id = queue.id "
                    + "LEFT OUTER JOIN client rc ON rc.id = receiver "
                    + "WHERE sender = "
                    + "(SELECT id FROM client WHERE username = ?) AND "
                    + "(receiver = ? OR receiver IS NULL) "
                    + "ORDER BY create_time DESC, prio DESC "
                    + "FETCH FIRST ROW ONLY";

    /**
     * SQL statement to get the top message in the given queue ordered by
     * priority first. Only messages that can be accessed by the given receiver
     * are considered.
     */
    private final static String SQL_PEEK_BY_QUEUE_PRIO =
            "SELECT message.id AS msg_id, queue.id AS queue_id, context, prio, "
                    + "create_time, message, queue.name, "
                    + "sc.username AS sender, rc.username AS receiver "
                    + "FROM message INNER JOIN msg_queue_assoc "
                    + "ON msg_queue_assoc.message_id = message.id "
                    + "INNER JOIN queue ON queue_id = queue.id "
                    + "INNER JOIN client sc ON sc.id = sender "
                    + "LEFT OUTER JOIN client rc ON rc.id = receiver "
                    + "WHERE queue.name = ? "
                    + "AND (receiver = ? OR receiver IS NULL) "
                    + "ORDER BY prio DESC, create_time DESC "
                    + "FETCH FIRST ROW ONLY";

    /**
     * SQL statement to get the top message in the given queue ordered by
     * creation time first. Only messages that can be accessed by the given
     * receiver are considered.
     */
    private final static String SQL_PEEK_BY_QUEUE_TIME =
            "SELECT message.id AS msg_id, queue.id AS queue_id, context, prio, "
                    + "create_time, message, queue.name, "
                    + "sc.username AS sender, rc.username AS receiver "
                    + "FROM message INNER JOIN msg_queue_assoc "
                    + "ON msg_queue_assoc.message_id = message.id "
                    + "INNER JOIN queue ON queue_id = queue.id "
                    + "INNER JOIN client sc ON sc.id = sender "
                    + "LEFT OUTER JOIN client rc ON rc.id = receiver "
                    + "WHERE queue.name = ? "
                    + "AND (receiver = ? OR receiver IS NULL) "
                    + "ORDER BY create_time DESC, prio DESC "
                    + "FETCH FIRST ROW ONLY";

    /**
     * Retrieve a message according to the specified criteria. If no message is
     * found then null is returned. Exceptions are thrown when the parameters
     * are not valid, e.g. the given sender doesn't exist.
     * 
     * @param receiver
     *            client that is retrieving the message.
     * @param argument
     *            either sender or queue that will be used as criteria for
     *            retrieving the message.
     * @param prioFirst
     *            indicates if the top priority message should be retrieved,
     *            otherwise it will be the newest one. true indicates by
     *            priority.
     * @param byQueue
     *            indicates if argument is a queue or a sender. true indicates
     *            queue.
     * @param conn
     *            database connection.
     * @return top message found, or null if there is not any.
     * @throws SQLException
     *             if there is an error accessing the database.
     */
    public Message execute(int receiver, String argument, boolean prioFirst,
            boolean byQueue, Connection conn) throws SQLException {
        PreparedStatement stmt = null;
        try {
            if (byQueue) {
                if (prioFirst)
                    stmt = conn.prepareStatement(SQL_PEEK_BY_QUEUE_PRIO);
                else
                    stmt = conn.prepareStatement(SQL_PEEK_BY_QUEUE_TIME);
            } else {
                if (prioFirst)
                    stmt = conn.prepareStatement(SQL_PEEK_BY_SENDER_PRIO);
                else
                    stmt = conn.prepareStatement(SQL_PEEK_BY_SENDER_TIME);
            }
            // TODO: Check queue/sender existence first and throw exception
            stmt.setString(1, argument);
            stmt.setInt(2, receiver);
            ResultSet result = stmt.executeQuery();
            if (!result.next())
                return null;
            String sender;
            if (byQueue)
                sender = result.getString("sender");
            else
                sender = argument;
            Message formattedResult =
                    new Message(result.getLong("msg_id"),
                            result.getShort("context"),
                            result.getShort("prio"),
                            result.getString("message"), sender,
                            result.getInt("create_time"),
                            result.getString("name"),
                            result.getLong("queue_id"),
                            result.getString("receiver"));
            return formattedResult;
        } finally {
            if (stmt != null)
                stmt.close();
        }
    }
}
