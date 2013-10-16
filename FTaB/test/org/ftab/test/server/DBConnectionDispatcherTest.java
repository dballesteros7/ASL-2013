/**
 * DBConnectionDispatcher.java
 * Created: 15.10.2013
 * Author: Diego
 */
package org.ftab.test.server;

import static org.junit.Assert.fail;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.ftab.quality.server.ServerInit;
import org.ftab.server.DBConnectionDispatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for the DBConnection dispatcher.
 */
public class DBConnectionDispatcherTest {

    /**
     * Thread pool for the tests.
     */
    private ExecutorService threadPool;
    /**
     * Object under test.
     */
    private DBConnectionDispatcher dispatcher;

    /**
     * Number of worker threads in the test.
     */
    private int numberOfWorkers;

    /**
     * Create a fixed thread pool for testing purposes and configure the
     * database connection.
     * 
     * @throws java.lang.Exception
     *             in case of any errors.
     */
    @Before
    public void setUp() throws Exception {
        numberOfWorkers = 100;
        threadPool = Executors.newFixedThreadPool(numberOfWorkers);
        dispatcher = ServerInit.connectToDatabase(numberOfWorkers);
    }

    /**
     * Close the database connection pool after the tests.
     * 
     * @throws java.lang.Exception
     *             in case of any errors.
     */
    @After
    public void tearDown() throws Exception {
        if (dispatcher != null)
            dispatcher.closePool();
    }

    /**
     * Check that we can concurrently requests connection without errors, simple
     * sanity check on the pooling instance.
     */
    @Test
    public void testRetrieveDatabaseConnection() {
        // Implement a simple worker inside the test
        class Worker implements Runnable {
            private DBConnectionDispatcher dispatcher;
            private volatile boolean keepRunning;

            public Worker(DBConnectionDispatcher nDispatcher) {
                dispatcher = nDispatcher;
                keepRunning = true;
            }

            @Override
            public void run() {
                while (keepRunning) {
                    // Get a connection, hold it for a random number of seconds,
                    // release and wait another random number of seconds.
                    Connection conn = null;
                    try {
                        conn = dispatcher.retrieveDatabaseConnection();
                        Random randomizer = new Random(Thread.currentThread()
                                .getId());
                        try {
                            Thread.sleep(Math.abs(randomizer.nextLong()) % 10000);
                        } catch (InterruptedException e) {
                            fail("No exceptions should happen during the sleep.");
                        }
                    } catch (SQLException e1) {
                        e1.printStackTrace();
                        fail("Failed to retrieve a connection.");
                        break;
                    } finally {
                        if (conn != null)
                            try {
                                conn.close();
                            } catch (SQLException e) {
                                fail("Failed to close a connection.");
                                break;
                            }
                    }
                }
            }
        }
        LinkedList<Worker> workers = new LinkedList<Worker>();
        for (int i = 0; i < numberOfWorkers; ++i) {
            Worker myWorker = new Worker(dispatcher);
            threadPool.execute(myWorker);
            workers.add(myWorker);
        }
        for (Worker worker : workers) {
            worker.keepRunning = false;
        }
        threadPool.shutdown();
        try {
            threadPool.awaitTermination(60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            fail("Couldn't wait for thread pool to terminate.");
        }
    }
}
