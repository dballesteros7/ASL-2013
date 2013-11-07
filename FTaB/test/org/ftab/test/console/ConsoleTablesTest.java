package org.ftab.test.console;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.ftab.console.tablemodels.clients.AllClientsTableModel;
import org.ftab.console.tablemodels.clients.structs.AllClientsStats;
import org.ftab.database.Create;
import org.ftab.database.Destroy;
import org.ftab.database.client.CreateClient;
import org.ftab.database.queue.CreateQueue;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.postgresql.ds.PGPoolingDataSource;

/**
 * Unit test for all the message related DAOs.
 * 
 * @see org.ftab.database.message
 */
public class ConsoleTablesTest {

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
        source.setServerName("localhost:5432"); // Test server
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
     * Tests that we can create messages with or without receiver and in one or
     * more queues. Also checks that we can retrieve all the messages in the
     * system with the get-all DAO.
     * 
     * @throws SQLException
     *             if there is an unexpected database error.
     */
    @Test
    public void testClientTableModel() throws SQLException {
        Connection conn = null;
        try {
            stuffDatabase(0, 2);
            conn = source.getConnection();
            conn.setAutoCommit(true);
            
            AllClientsTableModel model = new AllClientsTableModel();
            AllClientsStats ro = (AllClientsStats) model.Refresh(null, source);
            
            assertEquals(0, ro.getOnlineClients());
            assertEquals(2, ro.getTotalClients());
            assertEquals(2, model.getRowCount());
            assertEquals("Client#1", model.getValueAt(0, 1));
            assertEquals("Client#2", model.getValueAt(1, 1));
            
            new CreateClient().execute("Client#3", true, conn);
            
            ro = (AllClientsStats) model.Refresh(null, source);
            assertEquals(1, ro.getOnlineClients());
            assertEquals(3, ro.getTotalClients());
            assertEquals(3, model.getRowCount());
            assertEquals("Client#1", model.getValueAt(0, 1));
            assertEquals("Client#2", model.getValueAt(1, 1));
            assertEquals("Client#3", model.getValueAt(2, 1));
        } catch (Exception ex) {
            fail("Got an exception during the test");
        } finally {
            if (conn != null)
                conn.close();
        }
    }
}
