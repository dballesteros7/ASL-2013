/**
 * ClientTest.java
 * Created: Oct 10, 2013
 * Author: Diego Ballesteros (diegob)
 */
package org.ftab.test.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

import org.ftab.database.Client;
import org.ftab.database.client.ChangeClientStatus;
import org.ftab.database.client.CreateClient;
import org.ftab.database.client.FetchClient;
import org.ftab.database.client.RetrieveClients;
import org.ftab.quality.server.ServerInit;
import org.ftab.server.DBConnectionDispatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for all the client related DAOs.
 * 
 * @see org.ftab.database.client
 * 
 */
public class ClientTest {

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
     * Tests that we can insert and retrieve clients into the database without
     * errors.
     * 
     * @throws SQLException
     *             if the database can't be accessed.
     */
    @Test
    public void testInsertionRetrieval() throws SQLException {
        Connection conn = null;
        try {
            conn = source.retrieveDatabaseConnection();
            conn.setAutoCommit(false);

            // Create Bob and Alice
            CreateClient.execute("Bob", true, conn);
            CreateClient.execute("Alice", false, conn);

            // Retrieve them
            Client bob = FetchClient.execute("Bob", conn);
            Client alice = FetchClient.execute("Alice", conn);

            // Check data validity
            assertEquals(bob.getClientId(), 1);
            assertEquals(bob.getClientUsername(), "Bob");
            assertTrue(bob.isClientOnline());

            assertEquals(alice.getClientId(), 2);
            assertEquals(alice.getClientUsername(), "Alice");
            assertFalse(alice.isClientOnline());

            // Retrieve inexistent Larry
            Client larry = FetchClient.execute("Larry", conn);
            assertNull(larry);

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
     * Tests that we can change the online status of a client.
     * 
     * @throws SQLException
     *             if the database can't be accessed.
     */
    @Test
    public void testConnectionStatus() throws SQLException {
        Connection conn = null;
        try {
            conn = source.retrieveDatabaseConnection();
            conn.setAutoCommit(false);

            // Create Bob and Alice
            CreateClient.execute("Bob", false, conn);
            CreateClient.execute("Alice", false, conn);

            // Change their status to connected by username and id
            ChangeClientStatus.execute("Bob", true, conn);
            ChangeClientStatus.execute(2, true, conn);

            // Retrieve them and check data validity
            Client bob = FetchClient.execute("Bob", conn);
            Client alice = FetchClient.execute("Alice", conn);
            assertTrue(bob.isClientOnline());
            assertTrue(alice.isClientOnline());

            // Disconnect Alice and check
            ChangeClientStatus.execute("Alice", false, conn);
            bob = FetchClient.execute("Bob", conn);
            alice = FetchClient.execute("Alice", conn);
            assertTrue(bob.isClientOnline());
            assertFalse(alice.isClientOnline());

            conn.commit();
        } catch (SQLException ex) {
            if (conn != null)
                conn.rollback();
            fail("Got an exception during the test");
        } finally {
            if (conn != null)
                conn.close();
        }
    }

    /**
     * Stuff database with n "Bob" clients, half of them online.
     * 
     * @throws SQLException
     *             if the database can't be stuffed.
     */
    private void stuffDatabase(int n) throws SQLException {
        Connection conn = null;
        try {
            conn = source.retrieveDatabaseConnection();
            conn.setAutoCommit(false);
            for (int i = 1; i <= n; i++) {
                CreateClient.execute("Bob" + i, (i % 2 == 0), conn);
            }
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
     * Test that we can retrieve all clients in the database.
     * 
     * @throws SQLException
     *             if the database can't be accessed.
     */
    @Test
    public void testClientRetrieval() throws SQLException {
        Connection conn = null;
        try {
            // Stuff 1000 Bobs
            stuffDatabase(1000);
            conn = source.retrieveDatabaseConnection();
            conn.setAutoCommit(false);

            // Retrieve all clients and check them one by one
            ArrayList<Client> result = RetrieveClients.execute(conn);
            for (Client userClient : result) {
                int id = userClient.getClientId();
                assertEquals("Bob" + id, userClient.getClientUsername());
                assertEquals(id % 2 == 0, userClient.isClientOnline());
            }
            // Check that we received all 1000 clients
            assertEquals(result.size(), 1000);
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
}
