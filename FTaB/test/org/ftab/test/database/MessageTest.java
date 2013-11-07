/**
 * Message.java
 * Created: Oct 10, 2013
 * Author: Diego Ballesteros (diegob)
 */
package org.ftab.test.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.ftab.database.Create;
import org.ftab.database.Destroy;
import org.ftab.database.client.CreateClient;
import org.ftab.database.message.CreateMessage;
import org.ftab.database.message.GetAllMessages;
import org.ftab.database.queue.CreateQueue;
import org.ftab.server.Message;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.postgresql.ds.PGPoolingDataSource;

/**
 * 
 */
public class MessageTest {

    /**
     * Connection pool for the database
     */
    PGPoolingDataSource source;

    /**
     * Setups the database connection pool and creates necessary tables and
     * records. Assumes an empty DB.
     * 
     * @throws SQLException
     *             if some database operation fails
     */
    @Before
    public void setUp() throws SQLException {
        // Setup a simple connection pool of size 10
        source = new PGPoolingDataSource();
        source.setDataSourceName("Test data source");
        source.setServerName("dryad01.ethz.ch"); // Test server
        source.setDatabaseName("test1"); // Test DB
        source.setUser("user25"); // Group user
        source.setPassword("dbaccess25"); // OMG! It's a password in the code!!
                                          // It's ok we are protected files
        source.setMaxConnections(10); // Try a maximum of 10 pooled connections

        // Get a connection and create schema
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = source.getConnection();
            conn.setAutoCommit(false);
            Create c = new Create();
            c.execute(true, true, true, conn);
            conn.commit();
        } catch (SQLException ex) {
            fail("Got an SQL exception while setting up the test.");
        } finally {
            if (stmt != null)
                stmt.close();
            if (conn != null) {
                conn.rollback();
                conn.close();
            }
        }
    }

    /**
     * Drops the created tables from the database.
     * 
     * @throws Exception
     *             if anything goes wrong in the tear down.
     */
    @After
    public void tearDown() throws SQLException {
        Connection conn = null;
        try {
            conn = source.getConnection();
            conn.setAutoCommit(false);
            Destroy d = new Destroy();
            d.execute(true, true, true, conn);
            conn.commit();
        } catch (SQLException ex) {
            fail("Got an SQL exception while tearing down the test.");
        } finally {
            if (conn != null)
                conn.close();
            if (source != null)
                source.close();
        }
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
            conn = source.getConnection();
            conn.setAutoCommit(false);
            CreateClient cc = new CreateClient();
            CreateQueue cq = new CreateQueue();
            for (int i = 1; i <= nQueues; i++) {
                cq.execute("Queue#" + i, conn);
            }
            for (int i = 1; i <= nClients; i++) {
                cc.execute("Client#" + i, false, conn);
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
     * Tests that we can retrieve messages with or without receiver and in one
     * or more queues.
     * 
     * @throws SQLException
     *             if there is an unexpected database error.
     */
    @Test
    public void testCreationSuccess() throws SQLException {
        Connection conn = null;
        try {
            conn = source.getConnection();
            conn.setAutoCommit(false);
            stuffDatabase(5, 2);
            ArrayList<String> evenQueues = new ArrayList<String>();
            ArrayList<String> oddQueues = new ArrayList<String>();

            for (int i = 1; i < 6; i++) {
                if (i % 2 == 0)
                    evenQueues.add("Queue#" + i);
                else
                    oddQueues.add("Queue#" + i);
            }

            CreateMessage cm = new CreateMessage();
            cm.execute(1, "Queue#1", (short) 0, (short) 10, "Test msg 1", conn);
            cm.execute(1, evenQueues, (short) 0, (short) 9, "Test msg 2", conn);
            cm.execute(1, oddQueues, (short) 0, (short) 8, "Test msg 3", conn);
            cm.execute(2, "Queue#5", (short) 1, (short) 7, "Test msg 4", conn);
            cm.execute(2, evenQueues, (short) 1, (short) 6, "Test msg 5", conn);
            cm.execute(2, oddQueues, (short) 1, (short) 5, "Test msg 6", conn);
            cm.execute(1, "Client#2", "Queue#1", (short) 0, (short) 4,
                    "Test msg 7", conn);
            cm.execute(2, "Client#1", oddQueues, (short) 1, (short) 3,
                    "Test msg 8", conn);

            GetAllMessages gam = new GetAllMessages();
            ArrayList<Message> allMessages = gam.execute(conn);
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
            ex.printStackTrace();
            if (conn != null)
                conn.rollback();
            fail("Got an exception during the test");
        } finally {
            if (conn != null)
                conn.close();
        }
    }
}
