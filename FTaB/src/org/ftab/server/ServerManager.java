/**
 * ServerManager.java
 * Created: Oct 12, 2013
 * Author: Diego Ballesteros (diegob)
 */
package org.ftab.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
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
import org.ftab.logging.server.ServerManagerLogRecord;

/**
 * The ServerManager class is the entry point of the messaging server and serves
 * as the dispatcher of incoming connections to worker threads that will service
 * their requests, it limits the maximum number of clients that can be serviced
 * based on configurable limits on the number of worker threads and clients per
 * thread. A ServerManager is created using the ServerFactory.
 */
public class ServerManager implements Runnable {
    /**
     * The class' logger
     */
    private final static Logger LOGGER = Logger.getLogger(ServerManager.class
            .getName());

    /**
     * An id used to distinguish servers on the same machine from one another
     */
    private static int uniqueServerID = 0;
    
    /**
     * Port where the server will listen for incoming connections
     */
    private final int listeningPort;

    /**
     * The name of the server
     */
    private final String serverName;
    
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
     * Indicates if the manager should continue executing.
     */
    private boolean keepRunning;

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
        String hostname = null;
        try {
        	hostname = InetAddress.getLocalHost().getCanonicalHostName();
		} catch (UnknownHostException e) {
			hostname = "unknownhost";
		} finally {
			synchronized (this) {
				serverName = String.format("server%d@%s:%d", uniqueServerID++, hostname, listeningPort);
			}
		}
        workers = new LinkedList<MessagingWorker>();
        keepRunning = true;
                
        LOGGER.log(new ServerManagerLogRecord(Level.CONFIG, this, 
        		String.format("Created server with the following settings:\n"
                        + "Number of workers: %d\nClients per worker: %d\n"
                        + "Listening port: %d\nServer ready!", nWorkerThreads,
                nClientsPerWorker, nListeningPort)));
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
     *             when there is an error closing the selector or server
     *             channel.
     */
    public void run() {
    	ServerManagerLogRecord record = new ServerManagerLogRecord(this, "Server starting.");
    	LOGGER.log(record);
    	
        Selector serverSelector = null;
        ServerSocketChannel ssc = null;
        try {
            serverSelector = Selector.open();
            ssc = ServerSocketChannel.open();
            ssc.configureBlocking(false);

            ServerSocket ss = ssc.socket();
            InetSocketAddress address = new InetSocketAddress(listeningPort);
            ss.bind(address);
            ssc.register(serverSelector, SelectionKey.OP_ACCEPT);
            
            record = new ServerManagerLogRecord(Level.CONFIG, this, 
            		String.format("Server socket created and registered, listening IP: [%s:%d].",
                            address.getHostName(), address.getPort()), record);
            LOGGER.log(record);
            
            while (!Thread.interrupted() && keepRunning) {
            	ServerManagerLogRecord innerRecord = new ServerManagerLogRecord(this, 
            			"Polling for selector events.", record);  
            	LOGGER.log(innerRecord);
            	
                serverSelector.select();
                Set<SelectionKey> selectedKeys = serverSelector.selectedKeys();
                Iterator<SelectionKey> it = selectedKeys.iterator();
                while (it.hasNext()) {
                    SelectionKey key = it.next();
                    it.remove();
                    if (key.isAcceptable()) {
                        // This selector only has one channel registered to it,
                        // so
                        // we can assume which one it is.
                        SocketChannel sc = ssc.accept();
                        sc.configureBlocking(false);
                        
                        ServerManagerLogRecord innerInnerRecord = 
                        		new ServerManagerLogRecord(this, "Received incoming connection "
                        		+ ServerLogger.parseSocketAddress(sc) + ".", innerRecord);
                        LOGGER.log(innerInnerRecord);
                        
                        // Check if the server is full or not, if it is then
                        // register the socket for writing so we can inform the
                        // client that we can't accept the connection. Otherwise
                        // delegate to the first available thread.
                        if (serverFull()) {
                        	LOGGER.log(new ServerManagerLogRecord(this, 
                        			"Registering connection for write, server is full",
                        			innerInnerRecord));
                        	
                            sc.register(serverSelector, SelectionKey.OP_WRITE);
                        } else {
                        	LOGGER.log(new ServerManagerLogRecord(this, 
                        			"Delegating connection to available worker.",
                        			innerInnerRecord));                            
                            
                            delegateSocketToWorker(sc);
                        }
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
                        
                        LOGGER.log(new ServerManagerLogRecord(this,
                        		"Refused connection from " + ServerLogger.parseSocketAddress(sc) + " because the server is full.",
                        		innerRecord));
                        key.cancel();
                        sc.close();
                    }
                }
            }
        } catch (IOException e) {
        	LOGGER.log(new ServerManagerLogRecord(this,
        			"There was an IO error while running the server, shutting it down.", record, e));
        } finally {
            if (ssc != null) {
                try {
                    ssc.close();
                } catch (IOException e) {
                	LOGGER.log(new ServerManagerLogRecord(this, 
                			"IO exception while closing server channel, it is unrecoverable.", record, e));
                }
            }
            if (serverSelector != null) {
                try {
                    serverSelector.close();
                } catch (IOException e) {
                	LOGGER.log(new ServerManagerLogRecord(this, 
                			"IO exception while closing the server selector, it is unrecoverable.", record, e));
                }
            }
        }
    }

    /**
     * Instruct the server manager to stop the execution. This doesn't take care
     * of cleaning up the resources, that must be done with the shutdown
     * directive.
     */
    public void stop() {
        keepRunning = false;
    }

    /**
     * Gracefully shutdown the manager, this implies going through all the
     * worker and stopping them, then closing the database connection pool. It
     * is assumed that the selector and server socket was closed at the end of
     * the start method.
     */
    public void shutdown() {
    	LOGGER.log(new ServerManagerLogRecord(Level.INFO, this, "Gracefully shutting down the server."));
    	
        for (MessagingWorker worker : workers) {
            worker.stopRunning();
            while (!worker.isShutdown());
        }
        dbConnectionDispatcher.closePool();
        threadPool.shutdown();
        
        LOGGER.log(new ServerManagerLogRecord(Level.INFO, this, "Ready to leave."));
        
        ServerLogger.closeLogger();
    }

    /**
     * Delegate an incoming socket channel to a worker, first creating all
     * possible workers until maxThreads is reached and then filling them up in
     * a round-robin fashion. This method assumes that adding a worker won't
     * take the server over its configured capacity.
     * 
     * @param sc
     *            SocketChannel to assign to a worker.
     */
    private void delegateSocketToWorker(SocketChannel sc) {
    	try {
        	        	
            // Check if we are not in the limit of workers, if so then create
            // one.
            if (workers.size() < maxThreads) {
                MessagingWorker newWorker = new MessagingWorker(
                        maxClientsPerWorker, dbConnectionDispatcher, this.getServerName());
                
                ServerManagerLogRecord record = new ServerManagerLogRecord(this, 
                		"Created new worker: " + newWorker.getIdentifier() + ".");
                LOGGER.log(record);
                
                newWorker.registerChannel(sc);
                
                LOGGER.log(new ServerManagerLogRecord(this,
                		String.format("Assigned connection from %s to worker %s.", 
                				ServerLogger.parseSocketAddress(sc), newWorker.getIdentifier()), record));
                
                threadPool.execute(newWorker);
                workers.add(newWorker);
            } else {
                // If not then find the worker with the least amount of users.
                MessagingWorker minWorker = null;
                int maxCapacity = 0;
                for (MessagingWorker worker : workers) {
                    int remainingCap = worker.remainingCapacity();
                    if (remainingCap > maxCapacity) {
                        minWorker = worker;
                        maxCapacity = remainingCap;
                    }
                }
                minWorker.registerChannel(sc);
                
                LOGGER.log(new ServerManagerLogRecord(this, 
                		String.format("Assigned connection from %s to worker %s.", 
                				ServerLogger.parseSocketAddress(sc), minWorker.getIdentifier())));
            }
        } catch (IOException e) {
        	LOGGER.log(new ServerManagerLogRecord(this, "There was an error creating a worker.", e));
        } catch (RejectedExecutionException e) {
        	LOGGER.log(new ServerManagerLogRecord(this, "Thread pool could not accept a worker.", e)); 
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
     * Retrieves the name of the server
     * @return A name associated with the server
     */
    public String getServerName() {
		return serverName;
	}
    
    
    /**
     * Entry point for the server. Build a server manager using the standard
     * factory and start it in a separate thread.
     * 
     * @param args
     *            command line arguments.
     */
    public static void main(String[] args) {
        if (args.length < 1)
            System.exit(1);
        final ServerManager manager = ServerFactory.buildManager(args[0]);
        final Thread targetThread = new Thread(manager);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                manager.stop();
                try {
                    targetThread.join(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                manager.shutdown();
            }
        });
        targetThread.start();
    }
}
