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
import java.sql.Statement;
import java.util.ArrayList;

import org.ftab.database.Create;
import org.ftab.database.Destroy;
import org.ftab.database.Queue;
import org.ftab.database.client.CreateClient;
import org.ftab.database.exceptions.QueueAlreadyExistsException;
import org.ftab.database.exceptions.QueueNotEmptyException;
import org.ftab.database.message.CreateMessage;
import org.ftab.database.queue.CreateQueue;
import org.ftab.database.queue.DeleteQueue;
import org.ftab.database.queue.RetrieveQueues;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.postgresql.ds.PGPoolingDataSource;

/**
 * Unit tests for all queue related DAOs.
 * 
 * @see org.ftab.database.queue
 */
public class QueueTest {

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
    public void tearDown() throws Exception {
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
            conn = source.getConnection();
            conn.setAutoCommit(false);
            CreateQueue cq = new CreateQueue();
            // Create several queues and check that we can retrieve them all
            cq.execute("AwesomeQueue", conn);
            cq.execute("NotSoAwesomeQueue", conn);
            cq.execute("OkQueue", conn);
            cq.execute("SomewhatBadQueue", conn);
            cq.execute("HorribleQueue", conn);
            cq.execute("DoNotUseQueue", conn);
            RetrieveQueues rq = new RetrieveQueues();
            ArrayList<Queue> result = rq.execute(conn);
            assertEquals(result.size(), 6);
            for (Queue queue : result) {
                assertTrue(queue.getName().endsWith("Queue"));
                assertEquals(queue.getMessageCount(), 0);
            }
            // The queue is so awesome we want to create it again, it must fail.
            cq.execute("AwesomeQueue", conn);
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
            conn = source.getConnection();
            conn.setAutoCommit(false);
            CreateQueue cq = new CreateQueue();
            // Create several queues and check that we can retrieve them all
            cq.execute("AwesomeQueue", conn);
            cq.execute("NotSoAwesomeQueue", conn);
            cq.execute("OkQueue", conn);
            cq.execute("SomewhatBadQueue", conn);
            cq.execute("HorribleQueue", conn);
            cq.execute("DoNotUseQueue", conn);

            RetrieveQueues rq = new RetrieveQueues();
            ArrayList<Queue> result = rq.execute(conn);
            assertEquals(result.size(), 6);

            // Check that we can delete empty queues
            DeleteQueue dq = new DeleteQueue();
            dq.execute("AwesomeQueue", conn);
            dq.execute("SomewhatBadQueue", conn);
            result = rq.execute(conn);
            assertEquals(result.size(), 4);

            // Check that trying to delete an empty queue raises an exception.
            CreateMessage cm = new CreateMessage();
            CreateClient cc = new CreateClient();
            cc.execute("Bob", false, conn);
            cm.execute(1, "OkQueue", (short) 0, (short) 10, "Blocking MSG",
                    conn);
            dq.execute("OkQueue", conn);
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

}
