/**
 * QueueTest.java
 * Created: Oct 11, 2013
 * Author: Diego Ballesteros (diegob)
 */
package org.ftab.test.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

import org.ftab.database.Queue;
import org.ftab.database.client.CreateClient;
import org.ftab.database.exceptions.QueueAlreadyExistsException;
import org.ftab.database.exceptions.QueueNotEmptyException;
import org.ftab.database.message.CreateMessage;
import org.ftab.database.queue.CreateQueue;
import org.ftab.database.queue.DeleteQueue;
import org.ftab.database.queue.GetQueuesWithMessages;
import org.ftab.database.queue.RetrieveQueues;
import org.ftab.quality.server.ServerInit;
import org.ftab.server.DBConnectionDispatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for all queue related DAOs.
 * 
 * @see org.ftab.database.queue
 */
public class QueueTest {

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
    public void tearDown() throws Exception {
        ServerInit.destroySchema(source);
        if (source != null)
            source.closePool();
    }

    /**
     * Tests that we can create queues and retrieve them from the database, also
     * check that a proper exception is thrown when creating duplicate queues.
     * 
     * @throws SQLException
     *             if there is a problem accessing the database.
     */
    @Test
    public void testCreateQueue() throws SQLException {
        Connection conn = null;
        try {
            conn = source.retrieveDatabaseConnection();
            conn.setAutoCommit(false);
            // Create several queues and check that we can retrieve them all
            CreateQueue.execute("AwesomeQueue", conn);
            CreateQueue.execute("NotSoAwesomeQueue", conn);
            CreateQueue.execute("OkQueue", conn);
            CreateQueue.execute("SomewhatBadQueue", conn);
            CreateQueue.execute("HorribleQueue", conn);
            CreateQueue.execute("DoNotUseQueue", conn);
            ArrayList<Queue> result = RetrieveQueues.execute(conn);
            assertEquals(result.size(), 6);
            for (Queue queue : result) {
                assertTrue(queue.getName().endsWith("Queue"));
                assertEquals(queue.getMessageCount(), 0);
            }
            // The queue is so awesome we want to create it again, it must fail.
            CreateQueue.execute("AwesomeQueue", conn);
            fail("Creating an existing queue didn't fail as expected.");
        } catch (QueueAlreadyExistsException qaeex) {
            // Success
            conn.rollback();
        } catch (Exception ex) {
            if (conn != null)
                conn.rollback();
        } finally {
            if (conn != null)
                conn.close();
        }
    }

    /**
     * Test that we can delete queues and we restrict deletion to empty queues.
     * 
     * @throws SQLException
     *             if there was an error accessing the database.
     */
    @Test
    public void testDeleteQueue() throws SQLException {
        Connection conn = null;
        try {
            conn = source.retrieveDatabaseConnection();
            conn.setAutoCommit(false);
            // Create several queues and check that we can retrieve them all
            CreateQueue.execute("AwesomeQueue", conn);
            CreateQueue.execute("NotSoAwesomeQueue", conn);
            CreateQueue.execute("OkQueue", conn);
            CreateQueue.execute("SomewhatBadQueue", conn);
            CreateQueue.execute("HorribleQueue", conn);
            CreateQueue.execute("DoNotUseQueue", conn);

            ArrayList<Queue> result = RetrieveQueues.execute(conn);
            assertEquals(result.size(), 6);

            // Check that we can delete empty queues
            DeleteQueue.execute("AwesomeQueue", conn);
            DeleteQueue.execute("SomewhatBadQueue", conn);
            result = RetrieveQueues.execute(conn);
            assertEquals(result.size(), 4);

            // Check that trying to delete an empty queue raises an exception.
            CreateClient.execute("Bob", false, conn);
            CreateMessage.execute(1, "OkQueue", (short) 0, (short) 10,
                    "Blocking MSG", conn);
            DeleteQueue.execute("OkQueue", conn);
            fail("Deleting a non-empty queue didn't throw an exception.");
        } catch (QueueNotEmptyException qnee) {
            // Success
            conn.rollback();
        } catch (Exception ex) {
            if (conn != null)
                conn.rollback();
        } finally {
            if (conn != null)
                conn.close();
        }
    }

    /**
     * Check that a client can query for queues where there are messages
     * addressed specifically to him.
     * 
     * @throws SQLException
     *             if there is an error accessing the database.
     */
    @Test
    public void testQueuesWithWaitingMessages() throws SQLException {
        Connection conn = null;
        try {
            conn = source.retrieveDatabaseConnection();
            conn.setAutoCommit(false);
            // Create several queues and check that we can retrieve them all
            CreateQueue.execute("AwesomeQueue", conn);
            CreateQueue.execute("NotSoAwesomeQueue", conn);
            CreateQueue.execute("OkQueue", conn);
            CreateQueue.execute("SomewhatBadQueue", conn);
            CreateQueue.execute("HorribleQueue", conn);
            CreateQueue.execute("DoNotUseQueue", conn);

            // Create a few messages from Bob, Alice and Larry.
            int bobId = CreateClient.execute("Bob", false, conn);
            int aliceId = CreateClient.execute("Alice", false, conn);
            int larryId = CreateClient.execute("Larry", false, conn);

            Set<String> aliceQueues = new TreeSet<String>();
            aliceQueues.add("AwesomeQueue");
            aliceQueues.add("OkQueue");
            Set<String> bobQueues = new TreeSet<String>();
            bobQueues.add("HorribleQueue");
            bobQueues.add("OkQueue");
            bobQueues.add("DoNotUseQueue");

            CreateMessage.execute(bobId, "Alice", aliceQueues, (short) 0,
                    (short) 10, "Blocking MSG", conn);
            CreateMessage.execute(aliceId, "Bob", bobQueues, (short) 0,
                    (short) 10, "Blocking MSG", conn);
            CreateMessage.execute(aliceId, "NotSoAwesomeQueue", (short) 0,
                    (short) 10, "Blocking MSG", conn);

            ArrayList<String> resultBob = GetQueuesWithMessages.execute(bobId,
                    conn);
            ArrayList<String> resultAlice = GetQueuesWithMessages.execute(
                    aliceId, conn);
            ArrayList<String> resultLarry = GetQueuesWithMessages.execute(
                    larryId, conn);
            assertEquals(resultBob.size(), bobQueues.size());
            assertEquals(resultAlice.size(), aliceQueues.size());
            assertEquals(resultLarry.size(), 0);

            for (String queue : resultBob) {
                assertTrue(bobQueues.contains(queue));
            }
            for (String queue : resultAlice) {
                assertTrue(aliceQueues.contains(queue));
            }
        } catch (Exception ex) {
            if (conn != null)
                conn.rollback();
        } finally {
            if (conn != null)
                conn.close();
        }
    }
}
