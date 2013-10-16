/**
 * ServerManager.java
 * Created: Oct 12, 2013
 * Author: Diego Ballesteros (diegob)
 */
package org.ftab.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ftab.communication.ProtocolMessage;
import org.ftab.communication.responses.RequestResponse;
import org.ftab.communication.responses.RequestResponse.Status;

/**
 * The ServerManager class is the entry point of the messaging server and serves
 * as the dispatcher of incoming connections to worker threads that will service
 * their requests, it limits the maximum number of clients that can be serviced
 * based on configurable limits on the number of worker threads and clients per
 * thread. A ServerManager is created using the ServerFactory.
 */
public class ServerManager {
    /**
     * The class' logger
     */
    private final static Logger LOGGER = Logger.getLogger(ServerManager.class
            .getName());

    /**
     * Port where the server will listen for incoming connections
     */
    private final int listeningPort;

    /**
     * List of current active workers. TODO: Currently the workers don't die,
     * correct that and implement a guard for the list so it can be modified by
     * the workers when the die.
     */
    private final LinkedList<MessagingWorker> workers;

    /**
     * Executor service which implements the thread pool to execute the workers.
     */
    private final ExecutorService threadPool;

    /**
     * Maximum number of threads in the pool.
     */
    private final int maxThreads;

    /**
     * Maximum number of clients that can be serviced by a MessagingWorker.
     */
    private final int maxClientsPerWorker;

    /**
     * Database connection pool.
     */
    private final DBConnectionDispatcher dbConnectionDispatcher;

    /**
     * Initializes a ServerManager instance, this constructor should not be
     * called directly. Instead the ServerFactory service should be used. The
     * ServerManager object is initialized with the given configuration
     * parameters and the thread pool and database connection providers are
     * initialized.
     * 
     * @param nWorkerThreads
     *            maximum number of worker threads.
     * @param nClientsPerWorker
     *            maximum number of clients per worker.
     * @param nListeningPort
     *            port to listen to.
     */
    public ServerManager(int nWorkerThreads, int nClientsPerWorker,
            int nListeningPort) {
        // 1. Create a thread pool with nWorkerThreads
        maxThreads = nWorkerThreads;
        maxClientsPerWorker = nClientsPerWorker;
        threadPool = Executors.newFixedThreadPool(maxThreads);

        // 2. Create a database connection pool
        dbConnectionDispatcher = new DBConnectionDispatcher();

        // 3. Configure other attributes
        listeningPort = nListeningPort;
        workers = new LinkedList<MessagingWorker>();
        LOGGER.config(String.format(
                "Created server with the following settings:\n"
                        + "Number of workers: %d\nClients per worker: %d\n"
                        + "Listening port: %d\nServer ready!", nWorkerThreads,
                nClientsPerWorker, nListeningPort));
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
        dbConnectionDispatcher.configureDatabaseConnectionPool(username,
                password, server, database, maxConnections);
    }

    /**
     * Main server method, open the connection and start listening in the
     * designated port. For each incoming connection, assign it to a worker if
     * the number of threads is not at the limit and any of them still have
     * capacity otherwise accept the socket and write out a message indicating
     * that the server is full before closing the connection.
     * 
     * @throws IOException
     */
    private void start() throws IOException {
        LOGGER.info("Server starting.");
        Selector serverSelector = Selector.open();
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);

        ServerSocket ss = ssc.socket();
        InetSocketAddress address = new InetSocketAddress(listeningPort);
        ss.bind(address);
        ssc.register(serverSelector, SelectionKey.OP_ACCEPT);
        LOGGER.config(String.format(
                "Server socket created and registered, listening IP: [%s:%d].",
                address.getHostName(), address.getPort()));
        while (true) {
            LOGGER.config("Polling for selector events.");
            int num = serverSelector.select(60000);
            if (num == 0) {
                LOGGER.config("No selector event occured in the last minute.");
                continue;
            }
            Set<SelectionKey> selectedKeys = serverSelector.selectedKeys();
            Iterator<SelectionKey> it = selectedKeys.iterator();
            while (it.hasNext()) {
                SelectionKey key = it.next();
                it.remove();
                if (key.isAcceptable()) {
                    // This selector only has one channel registered to it, so
                    // we can assume which one it is.
                    SocketChannel sc = ssc.accept();
                    sc.configureBlocking(false);
                    LOGGER.info("Received incoming connection ["
                            + sc.socket().getInetAddress().getHostName() + ":"
                            + sc.socket().getPort() + "].");
                    // Check if the server is full or not, if it is then
                    // register the socket for writing so we can inform the
                    // client that we can't accept the connection. Otherwise
                    // delegate to the first available thread.
                    if (serverFull()) {
                        LOGGER.config("Registering connection "
                                + "for write, server is full.");
                        sc.register(serverSelector, SelectionKey.OP_WRITE);
                    } else {
                        LOGGER.config("Delegating connection to available worker.");
                        delegateSocketToWorker(sc);
                    }
                    // Remove the processed key.
                    selectedKeys.remove(key);
                } else if (key.isWritable()) {
                    SocketChannel sc = (SocketChannel) key.channel();
                    RequestResponse response = new RequestResponse(
                            Status.FULL_SERVER);
                    ByteBuffer responseBuffer = ProtocolMessage
                            .toBytes(response);
                    // TODO: Make unblocking
                    while (responseBuffer.hasRemaining()) {
                        sc.write(responseBuffer);
                    }
                    LOGGER.info("Refused connection from ["
                            + sc.socket().getInetAddress().getHostName() + ":"
                            + sc.socket().getPort()
                            + "] because the server is full.");
                    key.cancel();
                    sc.close();
                    selectedKeys.remove(key);
                }
            }
        }
    }

    /**
     * Delegate an incoming socket channel to a worker, if all workers are full
     * then spawn a new one and assign the SocketChannel to it. This method
     * assumes that adding a worker won't take the server over its configured
     * capacity.
     * 
     * @param sc
     *            SocketChannel to assign to a worker.
     */
    private void delegateSocketToWorker(SocketChannel sc) {
        try {
            // Check if any of the existing workers is not full, if so then
            // register the channel to the first one with room for another
            // client.
            for (MessagingWorker worker : workers) {
                if (!worker.isFull()) {
                    worker.registerChannel(sc);
                    LOGGER.info("Assigned connection from ["
                            + sc.socket().getInetAddress().getHostName() + ":"
                            + sc.socket().getPort() + "] to worker "
                            + worker.getIdentifier() + ".");
                    return;
                }
            }
            // If no worker is empty, create a new worker since we know we are
            // still below max capacity in the server.
            MessagingWorker newWorker = new MessagingWorker(
                    maxClientsPerWorker, dbConnectionDispatcher);
            LOGGER.info("Created new worker: " + newWorker.getIdentifier()
                    + ".");
            newWorker.registerChannel(sc);
            LOGGER.info("Assigned connection from ["
                    + sc.socket().getInetAddress().getHostName() + ":"
                    + sc.socket().getPort() + "] to worker "
                    + newWorker.getIdentifier() + ".");
            threadPool.execute(newWorker);
            workers.add(newWorker);
        } catch (ClosedChannelException e) {
            LOGGER.severe("Channel was closed before assigning it.");
            LOGGER.log(Level.CONFIG, "", e);
        } catch (IOException e) {
            LOGGER.severe("There was an error creating a worker");
            LOGGER.log(Level.CONFIG, "", e);
        } catch (RejectedExecutionException e) {
            LOGGER.severe("Thread pool could not accept a worker.");
            LOGGER.log(Level.CONFIG, "", e);
        }
    }

    /**
     * Check the existing workers and determine if we can accept an incoming
     * connection. A connection can be accepted if the number of workers is less
     * than the configure maximum or at least one of the current workers has
     * less clients than the configured maximum.
     * 
     * @return true if the server is full, false otherwise.
     */
    private boolean serverFull() {
        if (workers.size() < maxThreads)
            return false;
        for (MessagingWorker worker : workers)
            if (!worker.isFull())
                return false;
        return true;
    }

    /**
     * Entry point for the server. Build a server manager using the standard
     * factory and start it.
     * 
     * @param args
     *            command line arguments.
     */
    public static void main(String[] args) {
        // TODO: Read the configuration file from args
        ServerManager manager = ServerFactory
                .buildManager("manuals/config-example.xml");
        try {
            manager.start();
        } catch (IOException e) {
            LOGGER.severe("There was an IO error while running the server, shutting it down.");
            LOGGER.log(Level.CONFIG, "", e);
        }
    }

}
