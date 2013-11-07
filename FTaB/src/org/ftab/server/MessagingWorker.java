/**
 * MessagingWorker.java
 * Created: Oct 13, 2013
 * Author: Diego Ballesteros (diegob)
 */
package org.ftab.server;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ftab.server.ClientConnection.WriteStatus;
import org.ftab.server.exceptions.RemoteSocketClosedException;

/**
 * Worker process that process multiple socket channels for read and write
 * operations.
 */
public class MessagingWorker implements Runnable {
    /**
     * The class' logger
     */
    private final static Logger LOGGER = Logger.getLogger(MessagingWorker.class
            .getName());

    /**
     * Selector for demuxing the channels.
     */
    private final Selector selector;
    /**
     * Lock objector the selector.
     */
    private final Object guardlock;
    /**
     * Indicates the max number of keys in the selector.
     */
    private int capacity;
    /**
     * Unique ientifier for the worker.
     */
    private final String identifier;
    /**
     * Database connection pool.
     */
    private final DBConnectionDispatcher dispatcher;
    /**
     * Indicates if the worker should keep running.
     */
    private volatile boolean keepRunning;
    /**
     * Indicates if the worker is running.
     */
    private volatile boolean active;

    /**
     * Max time that a channel is allowed to be inactive (i.e. no read/write
     * event has been selected), in seconds.
     */
    private final long clientTimeout;

    /**
     * Create a worker with the given capacity.
     * 
     * @param nCapacity
     *            initial capacity of the worker.
     * @param nDispatcher
     *            database connection pool.
     * @throws IOException
     *             If there is a problem opening the selector.
     */
    public MessagingWorker(int nCapacity, DBConnectionDispatcher nDispatcher)
            throws IOException {
        selector = Selector.open();
        capacity = nCapacity;
        guardlock = new Object();
        identifier = UUID.randomUUID().toString();
        dispatcher = nDispatcher;
        active = false;
        keepRunning = true;
        clientTimeout = 3600;
    }

    /**
     * Indicates if the worker is at full capacity.
     * 
     * @return true if the worker is at full capacity, false otherwise.
     */
    public boolean isFull() {
        synchronized (guardlock) {
            selector.wakeup();
            return selector.keys().size() >= capacity;
        }
    }

    /**
     * Indicates if the worker is shutdown.
     * 
     * @return true if the worker is not running, false otherwise.
     */
    public boolean isShutdown() {
        return !active;
    }

    /**
     * Change the keep running directive of the worker to false.
     */
    public void stopRunning() {
        keepRunning = false;
    }

    /**
     * Get the thread's unique identifier.
     * 
     * @return thread's identifier.
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Dynamically increase the capacity of the worker.
     * 
     * @param increase
     *            value by which the capacity will be increased.
     */
    public void increaseCapacity(int increase) {
        capacity += increase;
    }

    /**
     * Thread-safe registration of a channel in the worker's selector.
     * 
     * @param channel
     *            socket channel to register.
     */
    public void registerChannel(SocketChannel channel) {
        synchronized (guardlock) {
            selector.wakeup();
            SelectionKey key;
            try {
                key = channel.register(selector, SelectionKey.OP_READ);
                key.attach(new ClientConnection(dispatcher));
            } catch (ClosedChannelException e) {
                LOGGER.severe("Couldn't register new channel in worker " +
                        identifier + " because it was already closed.");
                LOGGER.log(Level.SEVERE, "", e);
            }
        }
    }

    /**
     * Thread-safe cancellation of a channel in the worker's selector.
     * 
     * @param key
     *            key to de-register, it must belong to the Selector's key-set.
     */
    private void deregisterKey(SelectionKey key) {
        synchronized (guardlock) {
            selector.wakeup();
            key.cancel();
            LOGGER.info("Canceled key for connection from " +
                    ServerLogger.parseSocketAddress((SocketChannel) key
                            .channel()) + ".");
        }
    }

    /**
     * Thread-safe method to indicate that a key is interested in writing as
     * well as reading.
     * 
     * @param key
     *            key that is interested in writing, it must belong to the
     *            Selector's key-set.
     */
    private void announceWriteNeed(SelectionKey key) {
        synchronized (guardlock) {
            selector.wakeup();
            key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
            LOGGER.info("Channel connected to " +
                    ServerLogger.parseSocketAddress((SocketChannel) key
                            .channel()) + " is now interested in writing.");
        }
    }

    /**
     * Thread-safe method to indicate that the key is only interested in reading
     * from the channel.
     * 
     * @param key
     *            key that must be changed to read-only mode, it must belong to
     *            the Selector's key-set.
     */
    private void readOnlyMode(SelectionKey key) {
        synchronized (guardlock) {
            selector.wakeup();
            key.interestOps(SelectionKey.OP_READ);
            LOGGER.info("Channel connected to " +
                    ServerLogger.parseSocketAddress((SocketChannel) key
                            .channel()) + " is now only interested in reading.");
        }
    }

    /**
     * Runnable interface method, the worker selects the registered channels and
     * processes the ready events sequentially according to their read/write
     * operation.
     */
    @Override
    public void run() {
        active = true;
        while (keepRunning && !Thread.interrupted()) {
            synchronized (guardlock) {
                // Wait for the lock on the selector key-set before continuing
            }
            try {
                int eventCount = selector.select(clientTimeout);
                if (eventCount == 0) {
                    // TODO: Check for timed out clients and reap them.
                    continue;
                }
                Iterator<SelectionKey> it = selector.selectedKeys().iterator();
                while (it.hasNext()) {
                    SelectionKey key = it.next();
                    it.remove();
                    if (key.isReadable()) {
                        processReadReady(key);
                    } else if (key.isWritable()) {
                        processWriteReady(key);
                    }
                }
            } catch (IOException e) {
                LOGGER.severe("Failed to select anything in worker " +
                        identifier + ".");
                LOGGER.log(Level.SEVERE, "", e);
            }
        }
        closeWorker();
        active = false;
    }

    /**
     * Close the worker after receiving a shutdown order. This expects that no
     * other thread is touching the selector or the channels.
     */
    private void closeWorker() {
        LOGGER.info("Shutting down worker " + identifier + ".");
        try {
            for (SelectionKey key : selector.keys()) {
                SocketChannel sc = (SocketChannel) key.channel();
                sc.close();
            }
            selector.close();
        } catch (IOException e) {
            LOGGER.severe("Failed to close channels and selector in worker " +
                    identifier + ".");
            LOGGER.log(Level.SEVERE, "", e);
        }
    }

    /**
     * Method that processes a read-ready key, it calls the attached framework
     * object and passes the socket for extraction of the available bytes. If
     * the framework object requests writing after the reading then this is done
     * in a thread-safe manner.
     * 
     * @param key
     *            read-ready key to process.
     */
    private void processReadReady(SelectionKey key) {
        SocketChannel sc = (SocketChannel) key.channel();
        ClientConnection cc = (ClientConnection) key.attachment();
        try {
            boolean needWrite = false;
            try {
                LOGGER.config("Processing read event from " +
                        ServerLogger.parseSocketAddress(sc) + ".");
                needWrite = cc.processRead(sc);
            } catch (RemoteSocketClosedException e) {
                sc.close();
                deregisterKey(key);
            }
            if (needWrite && ((key.interestOps() & SelectionKey.OP_WRITE) == 0))
                announceWriteNeed(key);
        } catch (IOException e) {
            LOGGER.severe("Failed to process a read event from " +
                    ServerLogger.parseSocketAddress(sc) + ".");
            LOGGER.log(Level.SEVERE, "", e);
        }
    }

    /**
     * Method that processes a write-ready key, it calls the attached framework
     * object and passes the socket for writing any queued bytes. If the
     * framework object requests disconnection or read-only mode after the write
     * operation then this is done in a thread-safe manner.
     * 
     * @param key
     *            write-ready key to process.
     */
    private void processWriteReady(SelectionKey key) {
        SocketChannel sc = (SocketChannel) key.channel();
        ClientConnection cc = (ClientConnection) key.attachment();
        try {
            LOGGER.config("Processing write event from " +
                    ServerLogger.parseSocketAddress(sc) + ".");
            WriteStatus ccs = cc.processWrite(sc);
            switch (ccs) {
            case IDLE:
                readOnlyMode(key);
                break;
            case WRITING:
                break;
            case DISCONNECT:
                sc.close();
                deregisterKey(key);
                break;
            }
        } catch (IOException e) {
            LOGGER.severe("Failed to process a write event from " +
                    ServerLogger.parseSocketAddress(sc) + ".");
            LOGGER.log(Level.SEVERE, "", e);
        }
    }
}
