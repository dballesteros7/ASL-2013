/**
 * ServerManagerTest.java
 * Created: 17.10.2013
 * Author: Diego
 */
package org.ftab.test.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

import org.ftab.client.Client;
import org.ftab.communication.requests.RetrieveMessageRequest.Order;
import org.ftab.communication.requests.SendMessageRequest.Context;
import org.ftab.database.Message;
import org.ftab.database.Queue;
import org.ftab.database.client.RetrieveClients;
import org.ftab.database.message.GetAllMessages;
import org.ftab.database.queue.RetrieveQueues;
import org.ftab.quality.server.ServerInit;
import org.ftab.server.DBConnectionDispatcher;
import org.ftab.server.ServerFactory;
import org.ftab.server.ServerManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Comprehensive unit test suite for the server which provides an integration
 * test of the whole system.
 */
public class ServerManagerTest {

    /**
     * Alice client.
     */
    private Client alice;
    /**
     * Bob client.
     */
    private Client bob;
    /**
     * Server manager instance.
     */
    private ServerManager manager;
    /**
     * Thread where the server will run.
     */
    private Thread serverThread;
    /**
     * DB Connection pool.
     */
    private DBConnectionDispatcher dispatch;

    /**
     * Creates the database schema and starts a server process.
     * 
     * @throws SQLException
     *             if there is a problem setting up the database.
     * @throws InterruptedException
     *             if we get interrupted while waiting for the server to start.
     */
    @Before
    public void setUp() throws SQLException, InterruptedException {
        dispatch = ServerInit.connectToDatabase(10);
        ServerInit.setupSchema(dispatch);
        alice = new Client("Alice");
        bob = new Client("Bob");
        manager = ServerFactory.buildManager("./config/config-unittest.xml");
        serverThread = new Thread(manager);
        serverThread.start();
        Thread.sleep(1000);
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
        if(serverThread != null){
            serverThread.interrupt();
            manager.stop();
            serverThread.join();
            manager.shutdown();
        }
        if(dispatch != null){
            ServerInit.destroySchema(dispatch);
            dispatch.closePool();
        }
    }

    @Test
    public void testBroadcast() throws SQLException {
        Connection conn;
        assertTrue(alice.Connect("localhost", 34582));
        assertTrue(alice.CreateQueue("Alice's Vault"));
        ArrayList<String> queues = new ArrayList<String>();
        queues.add("Alice's Vault");
        for (int i = 0; i < 100; ++i) {
            alice.SendMessage("This is test message No. " + i,
                    (byte) ((i % 10) + 1), Context.NONE, queues);
        }
        assertTrue(alice.CreateQueue("Alice's Rant Place"));
        queues.add("Alice's Rant Place");
        for (int i = 0; i < 100; ++i) {
            alice.SendMessage("This is angry test message No. " + i,
                    (byte) ((i % 10) + 1), Context.NONE, queues);
        }
        conn = dispatch.retrieveDatabaseConnection();
        ArrayList<Queue> queuesInDB = RetrieveQueues.execute(conn);
        conn.close();
        assertEquals(queuesInDB.size(), 2);
        conn = dispatch.retrieveDatabaseConnection();
        ArrayList<Message> messagesInDB = GetAllMessages.execute(conn);
        conn.close();
        assertEquals(messagesInDB.size(), 300);
        conn = dispatch.retrieveDatabaseConnection();
        ArrayList<org.ftab.database.Client> clientsInDB = RetrieveClients
                .execute(conn);
        conn.close();
        assertEquals(clientsInDB.size(), 1);
        for (org.ftab.database.Client client : clientsInDB) {
            assertTrue(client.isClientOnline());
        }
        alice.Disconnect();
        conn = dispatch.retrieveDatabaseConnection();
        clientsInDB = RetrieveClients.execute(conn);
        conn.close();
        assertEquals(clientsInDB.size(), 1);
        for (org.ftab.database.Client client : clientsInDB) {
            assertFalse(client.isClientOnline());
        }
    }

    @Test
    public void testPingPong() throws SQLException {
        Connection conn;
        assertTrue(alice.Connect("localhost", 34582));
        assertTrue(bob.Connect("localhost", 34582));
        assertTrue(alice.CreateQueue("Alice's Inbox"));
        assertTrue(bob.CreateQueue("Bob's Inbox"));
        ArrayList<String> bobInbox = new ArrayList<String>();
        bobInbox.add("Bob's Inbox");
        ArrayList<String> aliceInbox = new ArrayList<String>();
        aliceInbox.add("Alice's Inbox");

        for (int i = 0; i < 500; ++i) {
            assertTrue(alice.SendMessage("The ping-pong ball",
                    (byte) (i % 10 + 1), Context.REQUEST, bobInbox));
            org.ftab.client.Message msg = bob.ViewMessageFromSender("Alice",
                    true);
            assertEquals(msg.getContent(), "The ping-pong ball");
            assertEquals(msg.getContext(), Context.REQUEST);
            assertEquals(msg.getQueueName(), "Bob's Inbox");
            assertTrue(bob.SendMessage("The ping-pong ball",
                    (byte) (i % 10 + 1), Context.RESPONSE, aliceInbox));
            msg = alice.ViewMessageFromQueue("Alice's Inbox", false,
                    Order.TIMESTAMP);
            assertEquals(msg.getContent(), "The ping-pong ball");
            assertEquals(msg.getContext(), Context.RESPONSE);
            assertEquals(msg.getSender(), "Bob");
        }

        conn = dispatch.retrieveDatabaseConnection();
        ArrayList<Queue> queuesInDB = RetrieveQueues.execute(conn);
        conn.close();
        assertEquals(queuesInDB.size(), 2);
        conn = dispatch.retrieveDatabaseConnection();
        ArrayList<Message> messagesInDB = GetAllMessages.execute(conn);
        conn.close();
        assertEquals(messagesInDB.size(), 500);
        for (Message message : messagesInDB) {
            assertEquals(message.getQueueName(), "Alice's Inbox");
        }
        conn = dispatch.retrieveDatabaseConnection();
        ArrayList<org.ftab.database.Client> clientsInDB = RetrieveClients
                .execute(conn);
        conn.close();
        assertEquals(clientsInDB.size(), 2);
        for (org.ftab.database.Client client : clientsInDB) {
            assertTrue(client.isClientOnline());
        }
        alice.Disconnect();
        bob.Disconnect();
        conn = dispatch.retrieveDatabaseConnection();
        clientsInDB = RetrieveClients.execute(conn);
        conn.close();
        assertEquals(clientsInDB.size(), 2);
        for (org.ftab.database.Client client : clientsInDB) {
            assertFalse(client.isClientOnline());
        }
    }

}
