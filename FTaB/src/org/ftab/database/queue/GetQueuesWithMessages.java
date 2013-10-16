/**
 * GetQueuesWithMessages.java
 * Created: Oct 15, 2013
 * Author: Diego Ballesteros (diegob)
 */
package org.ftab.database.queue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * DAO to retrieve the list of queues where there are messages addressed to a
 * specific client, i.e. their receiver field is not null and points to the
 * given client id.
 */
public class GetQueuesWithMessages {

    /**
     * SQL statement to retrieve the queues which have messages for the given
     * user.
     */
    private static final String SQL = "SELECT DISTINCT name FROM queue "
            + "INNER JOIN msg_queue_assoc "
            + "ON msg_queue_assoc.queue_id = queue.id " + "INNER JOIN message "
            + "ON message.id = msg_queue_assoc.message_id "
            + "WHERE message.receiver = ?";

    /**
     * Retrieve the list of queues with messages for the given receiver.
     * 
     * @param receiverId
     *            id of the client that should receive the messages.
     * @param conn
     *            database connection.
     * @return list with the queues with messages waiting for the client.
     * @throws SQLException
     *             if there is an error accessing the database.
     */
    public static ArrayList<String> execute(int receiverId, Connection conn)
            throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(SQL);
            stmt.setInt(1, receiverId);
            ResultSet result = stmt.executeQuery();
            ArrayList<String> formattedResult = new ArrayList<String>();
            while (result.next()) {
                formattedResult.add(result.getString(1));
            }
            return formattedResult;
        } finally {
            if (stmt != null)
                stmt.close();
        }
    }

}
