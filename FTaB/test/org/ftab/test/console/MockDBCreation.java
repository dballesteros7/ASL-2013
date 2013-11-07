package org.ftab.test.console;

import static org.junit.Assert.fail;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.ftab.database.Create;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.postgresql.ds.PGPoolingDataSource;

/**
 * Used to create a seperate persistent mock database from
 * the custum Java API
 * @author Jean-Pierre Smith
 *
 */
public class MockDBCreation {

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
       source.setDataSourceName("Mock DB Source");
       source.setServerName("localhost:5432"); // Test server
       source.setDatabaseName("MockDB"); // Test DB
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
   
   @Test
   public void doNothing() throws SQLException {
      
   }
   
   @After
   public void tearDown() throws SQLException {
	   if (source != null)
           source.close();
   }
}
