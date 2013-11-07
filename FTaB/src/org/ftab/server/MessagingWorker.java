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

import org.ftab.server.ClientConnection.WriteStatus;
import org.ftab.server.exceptions.RemoteSocketClosedException;

/**
 * 
 */
public class MessagingWorker implements Runnable {

    private final Selector selector;
    private final int capacity;
    private final String identifier;
    private final Object guardlock;
    private final DBConnectionDispatcher dispatcher;

    public MessagingWorker(int nCapacity, DBConnectionDispatcher nDispatcher)
            throws IOException {
        selector = Selector.open();
        capacity = nCapacity;
        guardlock = new Object();
        identifier = UUID.randomUUID().toString();
        dispatcher = nDispatcher;
    }

    public void registerChannel(SocketChannel channel)
            throws ClosedChannelException {
        synchronized (guardlock) {
            selector.wakeup();
            SelectionKey key = channel.register(selector, SelectionKey.OP_READ);
            key.attach(new ClientConnection(dispatcher));
        }
    }

    public void deregisterKey(SelectionKey key) {
        synchronized (guardlock) {
            selector.wakeup();
            key.cancel();
        }
    }

    public void announceWriteNeed(SelectionKey key) {
        synchronized (guardlock) {
            selector.wakeup();
            key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        }
    }

    public void readOnlyMode(SelectionKey key) {
        synchronized (guardlock) {
            selector.wakeup();
            key.interestOps(SelectionKey.OP_READ);
        }
    }

    public boolean isFull() {
        synchronized (guardlock) {
            selector.wakeup();
            return selector.keys().size() >= capacity;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        // TODO Auto-generated method stub
        while (!Thread.interrupted()) {
            synchronized (guardlock) {
                // Wait for the lock on the selector key-set before continuing
            }
            try {
                int eventCount = selector.select(3600000);
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
            } catch (IOException ioex) {

            }
        }
    }

    private void processReadReady(SelectionKey key) throws IOException {
        SocketChannel sc = (SocketChannel) key.channel();
        ClientConnection cc = (ClientConnection) key.attachment();
        boolean needWrite = false;
        try {
            needWrite = cc.processRead(sc);
        } catch (RemoteSocketClosedException e) {
            deregisterKey(key);
            sc.close();
        }
        if (needWrite && ((key.interestOps() & SelectionKey.OP_WRITE) == 0))
            announceWriteNeed(key);
    }

    private void processWriteReady(SelectionKey key) throws IOException {
        SocketChannel sc = (SocketChannel) key.channel();
        ClientConnection cc = (ClientConnection) key.attachment();
        WriteStatus ccs = cc.processWrite(sc);
        switch (ccs) {
        case IDLE:
            readOnlyMode(key);
            break;
        case WRITING:
            break;
        case DISCONNECT:
            deregisterKey(key);
            sc.close();
            break;
        }

    }

    public String getIdentifier() {
        return identifier;
    }
}
