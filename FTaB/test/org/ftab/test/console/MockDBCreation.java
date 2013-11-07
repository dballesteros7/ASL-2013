package org.ftab.test.console;

import static org.junit.Assert.fail;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

import org.ftab.database.Create;
import org.ftab.database.Destroy;
import org.ftab.database.client.CreateClient;
import org.ftab.database.message.CreateMessage;
import org.ftab.database.queue.CreateQueue;
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

       Connection conn = null;
       try {
           conn = source.getConnection();
           conn.setAutoCommit(false);
           
           // Tear down what was there before
           Destroy d = new Destroy();
           d.execute(true, true, true, conn);
           
           // Recreate the schema
           Create c = new Create();
           c.execute(true, true, true, conn);
                     
           conn.commit();
       } catch (SQLException ex) {
           fail("Got an SQL exception while tearing down the test.");
       } finally {
           if (conn != null)
               conn.close();
       }
       
       try {
           stuffDatabase(5, 2);
           conn = source.getConnection();
           conn.setAutoCommit(false);
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
   
   @Test
   public void doNothing() throws SQLException {
      
   }
   
   @After
   public void tearDown() throws SQLException {
	   if (source != null)
           source.close();
   }
}
