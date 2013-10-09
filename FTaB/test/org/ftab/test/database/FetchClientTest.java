package org.ftab.test.database;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ftab.database.client.CreateClient;
import org.ftab.database.client.FetchClient;
import org.ftab.database.Create;
import org.ftab.database.Destroy;
import org.ftab.server.UserClient;

import java.sql.*;

import org.postgresql.ds.PGPoolingDataSource;

/**
 * Unit test for the DAO that retrieves clients from the databas.e
 * @author Diego Ballesteros (diegob)
 *
 */
public class FetchClientTest {

	/**
	 * Connection pool for the database
	 */
	PGPoolingDataSource source;

	/**
	 * Setups the database connection pool and creates
	 * necessary tables and records. Assumes an empty DB.
	 * @throws Exception if some database operation fails
	 */
	@Before
	public void setUp() throws Exception {
		// Setup a simple connection pool of size 10
		source = new PGPoolingDataSource();
		source.setDataSourceName("Test data source");
		source.setServerName("dryad01.ethz.ch"); // Test server
		source.setDatabaseName("test1"); // Test DB
		source.setUser("user25"); // Group user
		source.setPassword("dbaccess25"); // OMG! It's a password in the code!!
										  // It's ok we are protected files
		source.setMaxConnections(10); // Try a maximum of 10 pooled connections
		
		// Get a connection and create initial records
		Connection conn = null;
		Statement stmt = null;
		try{
			conn = source.getConnection();
			conn.setAutoCommit(false);

			Create c = new Create();
			CreateClient cc = new CreateClient();

			c.execute(true, false, false, conn);
			cc.execute("Bob", true, conn);
			cc.execute("Alice", false, conn);

			conn.commit();
		} catch (SQLException ex){
			fail("Got an SQL exception while setting up the test.");
		} finally {
			if(stmt != null)
				stmt.close();
			if(conn != null){
				conn.rollback();
				conn.close();
			}
		}
	}

	/**
	 * Drops the created tables from the database.
	 * @throws Exception if anything goes wrong in the tear down.
	 */
	@After
	public void tearDown() throws Exception {
		Connection conn = null;
		try{
			conn = source.getConnection();
			conn.setAutoCommit(false);
			Destroy d = new Destroy();
			d.execute(true, false, false, conn);
			conn.commit();
		} catch (SQLException ex){
			fail("Got an SQL exception while tearing down the test.");
		} finally {
			if(conn != null)
				conn.close();
			if(source != null)
				source.close();
		}
	}

	/**
	 * Tests that we can retrieve the information of client that exists in the
	 * database.
	 * @throws SQLException if the database can not be accessed.
	 */
	@Test
	public void testExisting() throws SQLException{
		Connection conn = null;
		try{
			FetchClient x = new FetchClient();
			conn = source.getConnection();
			conn.setAutoCommit(false);

			UserClient bob = x.execute("Bob", conn);
			UserClient alice= x.execute("Alice", conn);

			assertEquals(bob.getClientId(), 1);
			assertEquals(bob.getClientUsername(), "Bob");
			assertEquals(bob.isClientOnline(), true);

			assertEquals(alice.getClientId(), 2);
			assertEquals(alice.getClientUsername(), "Alice");
			assertEquals(alice.isClientOnline(), false);
			
			conn.commit();
		} catch(Exception ex){
			if(conn != null)
				conn.rollback();
			fail("Got an exception during the test");
		} finally {
			if(conn != null)
				conn.close();
		}
	}

	/**
	 * Tests that we correctly fail to retrieve the information of an inexistent
	 * client in the database.
	 * @throws SQLException if the database can not be accessed.
	 */
	@Test
	public void testNonExisting() throws SQLException{
		Connection conn = null;
		try{
			FetchClient x = new FetchClient();
			conn = source.getConnection();
			conn.setAutoCommit(false);

			UserClient larry = x.execute("Larry", conn);
			assertNull(larry);
			conn.commit();
		} catch(Exception ex){
			if(conn != null)
				conn.rollback();
			fail("Got an exception during the test");
		} finally {
			if(conn != null)
				conn.close();
		}
	}
}
