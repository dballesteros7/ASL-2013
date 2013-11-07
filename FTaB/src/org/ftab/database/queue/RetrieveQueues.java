/**
 * RetrieveQueues.java
 * Created: Oct 10, 2013
 * Author: Diego Ballesteros (diegob)
 */
package org.ftab.database.queue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.ftab.server.Queue;

/**
 * DAO to retrieve information about all queues in the system for the management
 * console.
 */
public class RetrieveQueues {
    private static final String SQL = "SELECT name, COUNT(message_id) FROM "
            + "queue INNER JOIN msg_queue_assoc ON queue_id = id GROUP BY name";

    public ArrayList<Queue> execute(Connection conn)
            throws SQLException {
        PreparedStatement stmt = null;
        ArrayList<Queue> formattedResult = new ArrayList<Queue>();
        try {
            stmt = conn.prepareStatement(SQL);
            ResultSet result = stmt.executeQuery();
            while(result.next()){
                Queue next = new Queue(result.getString(1),
                        result.getLong(2));
                formattedResult.add(next);
            }
            return formattedResult;
        } finally {
            if (stmt != null)
                stmt.close();
        }
    }
}
