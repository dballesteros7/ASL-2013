/**
 * DBConnectionDispatcher.java
 * Created: Oct 15, 2013
 * Author: Diego Ballesteros (diegob)
 */
package org.ftab.server;

import java.sql.Connection;
import java.sql.SQLException;

import org.postgresql.ds.PGPoolingDataSource;

/**
 * 
 */
public class DBConnectionDispatcher {
    private final PGPoolingDataSource dbConnectionPool;

    public DBConnectionDispatcher() {
        dbConnectionPool = new PGPoolingDataSource();
    }

    /**
     * Configure the database connection pool settings.
     * 
     * @param username
     *            database username.
     * @param password
     *            password for the user.
     * @param server
     *            server url.
     * @param database
     *            database name.
     * @param maxConnections
     *            maximum number of pooled connections.
     */
    public void configureDatabaseConnectionPool(String username,
            String password, String server, String database, int maxConnections) {
        dbConnectionPool.setUser(username);
        dbConnectionPool.setPassword(password);
        dbConnectionPool.setServerName(server);
        dbConnectionPool.setDatabaseName(database);
        dbConnectionPool.setMaxConnections(maxConnections);
    }

    /**
     * Retrieve a database connection from the pool. The connection comes with
     * autoCommit disabled.
     */
    public Connection retrieveDatabaseConnection() throws SQLException {
        Connection conn = dbConnectionPool.getConnection();
        conn.setAutoCommit(false);
        return conn;
    }

    /**
     * Close all connections.
     */
    public void closePool() {
        dbConnectionPool.close();
    }
}
