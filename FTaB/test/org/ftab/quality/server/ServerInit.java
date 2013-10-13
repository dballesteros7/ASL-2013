/**
 * ServerInit.java
 * Created: Oct 16, 2013
 * Author: Diego Ballesteros (diegob)
 */
package org.ftab.quality.server;

import static org.junit.Assert.fail;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.ftab.database.Create;
import org.ftab.database.Destroy;
import org.ftab.server.DBConnectionDispatcher;

/**
 * Quality class that provides utilities to start a server. This includes
 * deploying a database schema and setting up connections.
 */
public class ServerInit {
    /**
     * Static method to create a database connection pool of the given size.
     * 
     * @param poolSize
     *            size of the pool.
     * @return connection pool object.
     * @throws SQLException
     *             if the connection can't be established.
     */
    public static DBConnectionDispatcher connectToDatabase(int poolSize)
            throws SQLException {
        // Setup a simple connection pool of size 10
        DBConnectionDispatcher source = new DBConnectionDispatcher();
        source.configureDatabaseConnectionPool("unittester", "likeineedone",
                "localhost", "unittestdiego", poolSize);

        return source;
    }

    /**
     * Static method to deploy a schema as defined in the database package.
     * 
     * @param dispatch
     *            connection pool provider.
     * @throws SQLException
     *             if the schema can't be deployed.
     */
    public static void setupSchema(DBConnectionDispatcher dispatch)
            throws SQLException {
        // Get a connection and create schema
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = dispatch.retrieveDatabaseConnection();
            conn.setAutoCommit(false);
            Create.execute(true, true, true, conn);
            conn.commit();
        } catch (SQLException ex) {
            if (conn != null)
                conn.rollback();
            ex.printStackTrace();
            fail("Got an SQL exception while setting up the test.");
        } finally {
            if (stmt != null)
                stmt.close();
            if (conn != null) {
                conn.close();
            }
        }
    }

    /**
     * Static method to destroy a schema as defined in the database package.
     * 
     * @param dispatch
     *            connection pool provider.
     * @throws SQLException
     *             if there is an error while dropping the schema.
     */
    public static void destroySchema(DBConnectionDispatcher dispatch)
            throws SQLException {
        Connection conn = null;
        try {
            conn = dispatch.retrieveDatabaseConnection();
            conn.setAutoCommit(false);
            Destroy.execute(true, true, true, conn);
            conn.commit();
        } catch (SQLException ex) {
            if (conn != null)
                conn.rollback();
            ex.printStackTrace();
            fail("Got an SQL exception while tearing down the test.");
        } finally {
            if (conn != null)
                conn.close();
        }
    }

}
