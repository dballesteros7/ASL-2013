/**
 * GetAllMessages.java
 * Created: Oct 10, 2013
 * Author: Diego Ballesteros (diegob)
 */
package org.ftab.database.message;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.ftab.database.Message;

/**
 * DAO to retrieve all messages in the database, sorted by priority. The
 * information provided with the message is the same as defined in the Message
 * class.
 * 
 * @see org.ftab.database.Message
 */
public class GetAllMessages {
    /**
     * SQL statement to retrieve all messages and their information sorted by
     * priority.
     */
    private final static String SQL = "SELECT message.id AS msg_id, "
            + "queue.id AS queue_id, context, prio, create_time, message,"
            + "rc.username AS receiver, sc.username AS sender, queue.name "
            + "FROM message INNER JOIN msg_queue_assoc "
            + "ON msg_queue_assoc.message_id = message.id "
            + "INNER JOIN queue ON msg_queue_assoc.queue_id = queue.id "
            + "INNER JOIN client sc ON sc.id = message.sender "
            + "LEFT OUTER JOIN client rc ON rc.id = message.receiver "
            + "ORDER BY prio DESC";

    /**
     * Execute the query to retrieve all messages and return a list with all the
     * Message objects.
     * 
     * @param conn
     *            database connection.
     * @return list with all the messages in the database.
     * @throws SQLException
     *             if there is an unexpected error while accessing the database.
     */
    public static ArrayList<Message> execute(Connection conn) throws SQLException {
        PreparedStatement stmt = null;
        ArrayList<Message> formattedResult = new ArrayList<Message>();
        try {
            stmt = conn.prepareStatement(SQL);
            ResultSet result = stmt.executeQuery();
            while (result.next()) {
                Message msg =
                        new Message(result.getLong("msg_id"),
                                result.getShort("context"),
                                result.getShort("prio"),
                                result.getString("message"),
                                result.getString("sender"),
                                result.getInt("create_time"),
                                result.getString("name"),
                                result.getLong("queue_id"),
                                result.getString("receiver"));
                formattedResult.add(msg);
            }
            conn.commit();
            return formattedResult;
        } finally {
            if (stmt != null)
                stmt.close();
        }
    }
}
