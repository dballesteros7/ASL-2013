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
import java.sql.Statement;
import java.util.ArrayList;

import org.ftab.database.Client;
import org.ftab.database.Create;
import org.ftab.database.Destroy;
import org.ftab.database.client.ChangeClientStatus;
import org.ftab.database.client.CreateClient;
import org.ftab.database.client.FetchClient;
import org.ftab.database.client.RetrieveClients;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.postgresql.ds.PGPoolingDataSource;

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
            c.execute(true, false, false, conn);
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
            d.execute(true, false, false, conn);
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
            FetchClient f = new FetchClient();
            CreateClient cc = new CreateClient();
            conn = source.getConnection();
            conn.setAutoCommit(false);

            // Create Bob and Alice
            cc.execute("Bob", true, conn);
            cc.execute("Alice", false, conn);

            // Retrieve them
            Client bob = f.execute("Bob", conn);
            Client alice = f.execute("Alice", conn);

            // Check data validity
            assertEquals(bob.getClientId(), 1);
            assertEquals(bob.getClientUsername(), "Bob");
            assertTrue(bob.isClientOnline());

            assertEquals(alice.getClientId(), 2);
            assertEquals(alice.getClientUsername(), "Alice");
            assertFalse(alice.isClientOnline());

            // Retrieve inexistent Larry
            Client larry = f.execute("Larry", conn);
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
            FetchClient fc = new FetchClient();
            CreateClient cc = new CreateClient();
            ChangeClientStatus ccs = new ChangeClientStatus();
            conn = source.getConnection();
            conn.setAutoCommit(false);

            // Create Bob and Alice
            cc.execute("Bob", false, conn);
            cc.execute("Alice", false, conn);

            // Change their status to connected by username and id
            ccs.execute("Bob", true, conn);
            ccs.execute(2, true, conn);

            // Retrieve them and check data validity
            Client bob = fc.execute("Bob", conn);
            Client alice = fc.execute("Alice", conn);
            assertTrue(bob.isClientOnline());
            assertTrue(alice.isClientOnline());

            // Disconnect Alice and check
            ccs.execute("Alice", false, conn);
            bob = fc.execute("Bob", conn);
            alice = fc.execute("Alice", conn);
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
            CreateClient cc = new CreateClient();
            conn = source.getConnection();
            conn.setAutoCommit(false);
            for (int i = 1; i <= n; i++) {
                cc.execute("Bob" + i, (i % 2 == 0), conn);
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
            RetrieveClients rc = new RetrieveClients();
            conn = source.getConnection();
            conn.setAutoCommit(false);

            // Retrieve all clients and check them one by one
            ArrayList<Client> result = rc.execute(conn);
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
