/**
 * Message.java
 * Created: Oct 10, 2013
 * Author: Diego Ballesteros (diegob)
 */
package org.ftab.test.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

import org.ftab.database.Message;
import org.ftab.database.client.CreateClient;
import org.ftab.database.exceptions.InexistentClientException;
import org.ftab.database.exceptions.InexistentQueueException;
import org.ftab.database.message.CreateMessage;
import org.ftab.database.message.DeleteMessage;
import org.ftab.database.message.GetAllMessages;
import org.ftab.database.message.RetrieveMessage;
import org.ftab.database.queue.CreateQueue;
import org.ftab.quality.server.ServerInit;
import org.ftab.server.DBConnectionDispatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for all the message related DAOs.
 * 
 * @see org.ftab.database.message
 */
public class MessageTest {

    /**
     * Connection pool for the database
     */
    private DBConnectionDispatcher source;

    /**
     * Setups the database connection pool and creates necessary tables and
     * records. Assumes an empty DB.
     * 
     * @throws SQLException
     *             if some database operation fails
     */
    @Before
    public void setUp() throws SQLException {
        source = ServerInit.connectToDatabase(10);
        ServerInit.setupSchema(source);
    }

    /**
     * Drops the created tables from the database.
     * 
     * @throws Exception
     *             if anything goes wrong in the tear down.
     */
    @After
    public void tearDown() throws SQLException {
        ServerInit.destroySchema(source);
        if (source != null)
            source.closePool();
    }

    /**
     * Creates queues and clients in the database for use in tests, it creates
     * nQueues queues and nClients clients. Queues are named from "Queue#1" to
     * "Queue#nQueues" and clients are named from "Bob#1" to "Bob#nClients".
     * 
     * @param nQueues
     *            number of queues to create.
     * @param nClients
     *            number of clients to create.
     * @throws SQLException
     *             if there is an unexpected error accessing the database.
     */
    private void stuffDatabase(int nQueues, int nClients) throws SQLException {
        Connection conn = null;
        try {
            conn = source.retrieveDatabaseConnection();
            conn.setAutoCommit(false);
            for (int i = 1; i <= nQueues; i++) {
                CreateQueue.execute("Queue#" + i, conn);
            }
            for (int i = 1; i <= nClients; i++) {
                CreateClient.execute("Client#" + i, false, conn);
            }
            conn.commit();
        } catch (Exception ex) {
            if (conn != null)
                conn.rollback();
            fail("Couldn't insert records in the database");
        } finally {
            if (conn != null)
                conn.close();
        }
    }

    /**
     * Tests that we can create messages with or without receiver and in one or
     * more queues. Also checks that we can retrieve all the messages in the
     * system with the get-all DAO.
     * 
     * @throws SQLException
     *             if there is an unexpected database error.
     */
    @Test
    public void testCreationSuccess() throws SQLException {
        Connection conn = null;
        try {
            stuffDatabase(5, 2);
            conn = source.retrieveDatabaseConnection();
            conn.setAutoCommit(false);
            ArrayList<String> evenQueues = new ArrayList<String>();
            ArrayList<String> oddQueues = new ArrayList<String>();

            for (int i = 1; i < 6; i++) {
                if (i % 2 == 0)
                    evenQueues.add("Queue#" + i);
                else
                    oddQueues.add("Queue#" + i);
            }

            CreateMessage.execute(1, "Queue#1", (short) 0, (short) 10,
                    "Test msg 1", conn);
            CreateMessage.execute(1, evenQueues, (short) 0, (short) 9,
                    "Test msg 2", conn);
            CreateMessage.execute(1, oddQueues, (short) 0, (short) 8,
                    "Test msg 3", conn);
            CreateMessage.execute(2, "Queue#5", (short) 1, (short) 7,
                    "Test msg 4", conn);
            CreateMessage.execute(2, evenQueues, (short) 1, (short) 6,
                    "Test msg 5", conn);
            CreateMessage.execute(2, oddQueues, (short) 1, (short) 5,
                    "Test msg 6", conn);
            CreateMessage.execute(1, "Client#2", "Queue#1", (short) 0,
                    (short) 4, "Test msg 7", conn);
            CreateMessage.execute(2, "Client#1", oddQueues, (short) 1,
                    (short) 3, "Test msg 8", conn);

            ArrayList<Message> allMessages = GetAllMessages.execute(conn);
            assertEquals(allMessages.get(0).getContent(), "Test msg 1");
            assertEquals(allMessages.get(1).getContent(), "Test msg 2");
            assertEquals(allMessages.get(3).getContent(), "Test msg 3");
            assertEquals(allMessages.get(6).getContent(), "Test msg 4");
            assertEquals(allMessages.get(7).getContent(), "Test msg 5");
            assertEquals(allMessages.get(9).getContent(), "Test msg 6");
            assertEquals(allMessages.get(12).getContent(), "Test msg 7");
            assertEquals(allMessages.get(13).getContent(), "Test msg 8");

            int[] queueCounts = new int[5];
            for (Message msg : allMessages) {
                queueCounts[Character.getNumericValue(msg.getQueueName()
                        .charAt(6)) - 1]++;
            }
            assertEquals(queueCounts[0], 5);
            assertEquals(queueCounts[1], 2);
            assertEquals(queueCounts[2], 3);
            assertEquals(queueCounts[3], 2);
            assertEquals(queueCounts[4], 4);
            conn.commit();
        } catch (Exception ex) {
            if (conn != null)
                conn.rollback();
            fail("Got an exception during the test");
        } finally {
            if (conn != null)
                conn.close();
        }
    }

    /**
     * Tests that creating a message with a non-valid receiver triggers the
     * appropriate exception.
     * 
     * @throws SQLException
     *             if there is an unexpected error accessing the database.
     */
    @Test
    public void testCreationFailureNoReceiver() throws SQLException {
        Connection conn = null;
        try {
            stuffDatabase(5, 2);
            conn = source.retrieveDatabaseConnection();
            conn.setAutoCommit(false);
            CreateMessage.execute(1, "Client#3", "Queue#1", (short) 0,
                    (short) 10, "FAIL", conn);
            fail("Creating a message with inexistent receiver didn't result in an exception");
            conn.commit();
        } catch (InexistentClientException icex) {
            // Success
            conn.rollback();
        } catch (Exception ex) {
            if (conn != null)
                conn.rollback();
            fail("We failed with an unexpected exception.");
        } finally {
            if (conn != null)
                conn.close();
        }
    }

    /**
     * Tests that creating a message with an inexistent queue triggers the
     * appropriate exception.
     * 
     * @throws SQLException
     *             if there is an unexpected error accessing the database.
     */
    @Test
    public void testCreationFailureNoQueue() throws SQLException {
        Connection conn = null;
        try {
            stuffDatabase(5, 2);
            conn = source.retrieveDatabaseConnection();
            conn.setAutoCommit(false);
            ArrayList<String> queues = new ArrayList<String>();
            queues.add("Queue#1");
            queues.add("Queue#2");
            queues.add("Queue#-1");
            queues.add("Queue#4");
            CreateMessage.execute(1, "Client#2", queues, (short) 0, (short) 10,
                    "FAIL", conn);
            fail("Creating a message with inexistent queue didn't result in an exception");
            conn.commit();
        } catch (InexistentQueueException iqex) {
            // Success
            conn.rollback();
        } catch (Exception ex) {
            if (conn != null)
                conn.rollback();
            fail("We failed with an unexpected exception.");
        } finally {
            if (conn != null)
                conn.close();
        }
    }

    /**
     * Tests that we can delete messages in queues from the database and when
     * the message is not in any other queue then it will be delete from the
     * message table.
     * 
     * @throws SQLException
     *             if the deletion can't be executed.
     */
    @Test
    public void testDeleteMessages() throws SQLException {
        Connection conn = null;
        try {
            stuffDatabase(2, 2);
            conn = source.retrieveDatabaseConnection();
            conn.setAutoCommit(false);
            ArrayList<String> queues = new ArrayList<String>();
            queues.add("Queue#1");
            queues.add("Queue#2");
            CreateMessage.execute(1, queues, (short) 0, (short) 1,
                    "Disposable", conn);
            DeleteMessage.execute(1, "Queue#1", conn);
            ArrayList<Message> result = GetAllMessages.execute(conn);
            assertEquals(result.size(), 1);
            assertEquals(result.get(0).getQueueId(), 2);
            assertEquals(result.get(0).getContent(), "Disposable");
            DeleteMessage.execute(1, "Queue#2", conn);
            result = GetAllMessages.execute(conn);
            assertEquals(result.size(), 0);
            conn.commit();
        } catch (Exception ex) {
            if (conn != null)
                conn.rollback();
        } finally {
            if (conn != null)
                conn.close();
        }
    }

    /**
     * Tests that we can peek at the messages in a queue according to different
     * criteria and the result is sorted by either priority or timestamp.
     * 
     * @throws SQLException
     *             if the queries can't be executed.
     */
    @Test
    public void testRetrieveMessages() throws SQLException {
        Connection conn = null;
        try {
            stuffDatabase(5, 2);
            conn = source.retrieveDatabaseConnection();
            conn.setAutoCommit(false);

            // Test access control, checking that client 1 can't read
            // messages addressed for client 2.
            CreateMessage.execute(1, "Client#2", "Queue#1", (short) 0,
                    (short) 5, "Not for you", conn);
            Message result = RetrieveMessage.execute(1, "Queue#1", true, true,
                    conn);
            assertNull(result);
            result = RetrieveMessage.execute(2, "Queue#1", true, true, conn);
            assertNotNull(result);

            // The following queries are queue based.
            // Check retrieval by priority first
            Thread.sleep(1000); // Timestamp precision is seconds
            CreateMessage.execute(1, "Queue#1", (short) 0, (short) 5,
                    "High prio", conn);
            Thread.sleep(1000);
            CreateMessage.execute(1, "Queue#1", (short) 0, (short) 4,
                    "New low prio", conn);

            result = RetrieveMessage.execute(2, "Queue#1", true, true, conn);
            assertEquals(result.getContent(), "High prio");

            // Check retrieval by timestamp first
            result = RetrieveMessage.execute(2, "Queue#1", false, true, conn);
            assertEquals(result.getContent(), "New low prio");

            // Check retrieval by sender
            result = RetrieveMessage.execute(2, "Client#1", true, false, conn);
            assertEquals(result.getContent(), "High prio");
            result = RetrieveMessage.execute(2, "Client#2", false, false, conn);
            assertNull(result);
            conn.commit();
        } catch (Exception ex) {
            if (conn != null)
                conn.rollback();
            fail("Got an exception while testing message retrieval.");
        } finally {
            if (conn != null)
                conn.close();
        }
    }
}
