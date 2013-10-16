/**
 * CreateMessage.java
 * Created: Oct 10, 2013
 * Author: Diego Ballesteros (diegob)
 */
package org.ftab.database.message;

import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.ftab.database.exceptions.CreateMessageException;
import org.ftab.database.exceptions.InexistentClientException;
import org.ftab.database.exceptions.InexistentQueueException;

/**
 * DAO for creating messages in the database, it provides different overloaded
 * execute methods depending on the type of message being sent. Checking
 * restrictions such as maximum size of the inputs is expected to be done by the
 * caller of this class.
 */
public class CreateMessage {

    /**
     * SQL statement to insert a new message without receiver.
     */
    private final static String SQL_INSERT_NO_RECEIVER = "INSERT INTO message "
            + "(sender, context, prio, create_time, message) "
            + "VALUES (?, ?, ?, ?, ?) RETURNING id";
    /**
     * SQL statement to insert a new message with receiver.
     */
    private final static String SQL_INSERT_RECEIVER = "INSERT INTO message "
            + "(sender, receiver, context, prio, create_time, message)"
            + "VALUES (?, ?, ?, ?, ?, ?) RETURNING id";

    /**
     * SQL statement to insert association records between queues and messages.
     */
    private final static String SQL_ASSOC_QUEUE = "INSERT INTO msg_queue_assoc "
            + "(message_id, queue_id) VALUES (?, "
            + "(SELECT id FROM queue WHERE name = ?))";
    /**
     * SQL statement to retrieve a client id given its username.
     */
    private final static String SQL_RETRIEVE_CLIENT = "SELECT id FROM client "
            + "WHERE username = ?";

    /**
     * Create a message with a specific receiver and put it in multiple queues
     * as specified in the input list.
     * 
     * @param sender
     *            id of the client that sends the message, assumed to be valid.
     * @param receiver
     *            username of the client that should receive the message, may
     *            not exist in the database.
     * @param queues
     *            list of queues where to send the message.
     * @param context
     *            context of the message.
     * @param priority
     *            priority of the message, from 1 to 10.
     * @param message
     *            content of the message, assumed to be less than 2k characters.
     * @param conn
     *            database connection.
     * @throws SQLException
     *             if there is an unexpected error accessing the database.
     * @throws InexistentQueueException
     *             if one of the queue names in the list doesn't exist.
     * @throws InexistentClientException
     *             if the receiver doesn't exist.
     * @throws CreateMessageException
     *             if the message was not created but no error was triggered.
     */
    public static void execute(int sender, String receiver,
            Iterable<String> queues, short context, short priority,
            String message, Connection conn) throws SQLException,
            InexistentQueueException, InexistentClientException,
            CreateMessageException {
        PreparedStatement stmt = null;
        try {
            // Get the receiver's id and throw an exception if not found
            stmt = conn.prepareStatement(SQL_RETRIEVE_CLIENT);
            stmt.setString(1, receiver);
            ResultSet result = stmt.executeQuery();
            if (!result.next())
                throw new InexistentClientException("Receiver " + receiver
                        + " does not exist in the database.");
            int receiver_id = result.getInt(1);
            stmt.close();

            // Prepare the message insert and execute it, no expected exception
            long current_time = System.currentTimeMillis();
            int current_time_int = (int) (current_time / 1000.0);
            stmt = conn.prepareStatement(SQL_INSERT_RECEIVER);
            stmt.setInt(1, sender);
            stmt.setInt(2, receiver_id);
            stmt.setShort(3, context);
            stmt.setShort(4, priority);
            stmt.setInt(5, current_time_int);
            stmt.setString(6, message);
            result = stmt.executeQuery();
            if (!result.next())
                throw new CreateMessageException();
            long message_id = result.getLong(1);
            associateMessagesToQueues(queues, message_id, conn);
        } finally {
            if (stmt != null)
                stmt.close();
        }
    }

    /**
     * Create a message without specific receiver and put it in multiple queues
     * as specified in the input list.
     * 
     * @param sender
     *            id of the client that sends the message, assumed to be valid.
     * @param queues
     *            list of queues where to send the message.
     * @param context
     *            context of the message.
     * @param priority
     *            priority of the message, from 1 to 10.
     * @param message
     *            content of the message, assumed to be less than 2k characters.
     * @param conn
     *            database connection.
     * @throws SQLException
     *             if there is an unexpected error accessing the database.
     * @throws InexistentQueueException
     *             if one of the queue names in the list doesn't exist.
     * @throws CreateMessageException
     *             if the message was not created but no error was triggered.
     */
    public static void execute(int sender, Iterable<String> queues,
            short context, short priority, String message, Connection conn)
            throws SQLException, InexistentQueueException,
            CreateMessageException {
        PreparedStatement stmt = null;
        try {

            // Prepare the message insert and execute it, no expected exception
            long current_time = System.currentTimeMillis();
            int current_time_int = (int) (current_time / 1000.0);
            stmt = conn.prepareStatement(SQL_INSERT_NO_RECEIVER);
            stmt.setInt(1, sender);
            stmt.setShort(2, context);
            stmt.setShort(3, priority);
            stmt.setInt(4, current_time_int);
            stmt.setString(5, message);
            ResultSet result = stmt.executeQuery();
            if (!result.next())
                throw new CreateMessageException();
            long message_id = result.getLong(1);
            associateMessagesToQueues(queues, message_id, conn);
        } finally {
            if (stmt != null)
                stmt.close();
        }
    }

    /**
     * Create a message with a specific receiver and put it in a single queue.
     * 
     * @param sender
     *            id of the client that sends the message, assumed to be valid.
     * @param receiver
     *            username of the client that should receive the message, may
     *            not exist in the database.
     * @param queue
     *            queue where to send the message, it may not exist.
     * @param context
     *            context of the message.
     * @param priority
     *            priority of the message, from 1 to 10.
     * @param message
     *            content of the message, assumed to be less than 2k characters.
     * @param conn
     *            database connection.
     * @throws SQLException
     *             if there is an unexpected error accessing the database.
     * @throws InexistentQueueException
     *             if one of the queue names in the list doesn't exist.
     * @throws InexistentClientException
     *             if the receiver doesn't exist.
     * @throws CreateMessageException
     *             if the message was not created but no error was triggered.
     */
    public static void execute(int sender, String receiver, String queue,
            short context, short priority, String message, Connection conn)
            throws SQLException, InexistentQueueException,
            InexistentClientException, CreateMessageException {
        ArrayList<String> queues = new ArrayList<String>();
        queues.add(queue);
        execute(sender, receiver, queues, context, priority, message, conn);
    }

    /**
     * Create a message without specific receiver and put it in a single queue.
     * 
     * @param sender
     *            id of the client that sends the message, assumed to be valid.
     * @param queue
     *            queue where to send the message, it may not exist.
     * @param context
     *            context of the message.
     * @param priority
     *            priority of the message, from 1 to 10.
     * @param message
     *            content of the message, assumed to be less than 2k characters.
     * @param conn
     *            database connection.
     * @throws SQLException
     *             if there is an unexpected error accessing the database.
     * @throws InexistentQueueException
     *             if one of the queue names in the list doesn't exist.
     * @throws CreateMessageException
     *             if the message was not created but no error was triggered.
     */
    public static void execute(int sender, String queue, short context,
            short priority, String message, Connection conn)
            throws SQLException, InexistentQueueException,
            CreateMessageException {
        ArrayList<String> queues = new ArrayList<String>();
        queues.add(queue);
        execute(sender, queues, context, priority, message, conn);
    }

    /**
     * Creates records in the database that associate a message with one or more
     * queues, in the context of the messaging system this means putting a newly
     * created message in a queue.
     * 
     * @param queues
     *            list of queues where to put the message.
     * @param message_id
     *            id of the message to put in the queues.
     * @param conn
     *            database connection.
     * @throws SQLException
     *             if the database can't be accessed.
     * @throws InexistentQueueException
     *             if one of the queues in the list doesn't exist.
     */
    private static void associateMessagesToQueues(Iterable<String> queues,
            long message_id, Connection conn) throws SQLException,
            InexistentQueueException {
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(SQL_ASSOC_QUEUE);
            for (String queueName : queues) {
                stmt.setLong(1, message_id);
                stmt.setString(2, queueName);
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (BatchUpdateException ex) {
            SQLException batchEx = ex.getNextException();
            String sqlState = batchEx.getSQLState();
            if (sqlState.equals("23502"))
                throw new InexistentQueueException(batchEx.getMessage());
            else
                throw ex;
        } finally {
            if (stmt != null)
                stmt.close();
        }
    }
}
