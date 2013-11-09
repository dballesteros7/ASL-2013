/**
 * ClientConnectionTest.java
 * Created: Oct 16, 2013
 * Author: Diego Ballesteros (diegob)
 */
package org.ftab.test.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

import org.ftab.communication.ProtocolMessage;
import org.ftab.communication.ProtocolMessage.MessageType;
import org.ftab.communication.exceptions.InvalidHeaderException;
import org.ftab.communication.requests.ConnectionRequest;
import org.ftab.communication.requests.GetQueuesRequest;
import org.ftab.communication.requests.QueueModificationRequest;
import org.ftab.communication.requests.RetrieveMessageRequest;
import org.ftab.communication.requests.SendMessageRequest;
import org.ftab.communication.responses.GetQueuesResponse;
import org.ftab.communication.responses.RequestResponse;
import org.ftab.communication.responses.RequestResponse.Status;
import org.ftab.communication.responses.RetrieveMessageResponse;
import org.ftab.database.Client;
import org.ftab.database.Message;
import org.ftab.database.Queue;
import org.ftab.database.client.RetrieveClients;
import org.ftab.database.message.GetAllMessages;
import org.ftab.database.queue.RetrieveQueues;
import org.ftab.pubenums.Filter;
import org.ftab.pubenums.Order;
import org.ftab.quality.server.ServerInit;
import org.ftab.server.ClientConnection;
import org.ftab.server.ClientConnection.WriteStatus;
import org.ftab.server.DBConnectionDispatcher;
import org.ftab.server.exceptions.RemoteSocketClosedException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 */
public class ClientConnectionTest {

	private DBConnectionDispatcher dispatcher;

	private ServerSocketChannel ssc;
	private SocketChannel listeningSocket;
	private SocketChannel clientSocket;

	private Connection conn;

	/**
	 * Sets up the environment for the test, it creates a database connection
	 * pool and the necessary sockets.
	 * 
	 * @throws SQLException
	 *             in case of errors setting up the database.
	 * @throws IOException
	 *             in case of errors configuring the sockets.
	 */
	@Before
	public void setUp() throws SQLException, IOException {
		dispatcher = ServerInit.connectToDatabase(10);
		ServerInit.setupSchema(dispatcher);

		// Setup a server socket and a client mocker
		ssc = ServerSocketChannel.open();
		ServerSocket ss = ssc.socket();
		InetSocketAddress address = new InetSocketAddress("localhost", 5000);
		ss.bind(address);
		ssc.configureBlocking(true);

		clientSocket = SocketChannel.open();
		clientSocket.configureBlocking(true);
		clientSocket.connect(address);

		listeningSocket = ssc.accept();
		listeningSocket.configureBlocking(false);
	}

	/**
	 * Tear down by destroying the database schema and closing sockets and
	 * connections.
	 * 
	 * @throws SQLException
	 *             in case of errors destroying the schema.
	 * @throws IOException
	 *             if there are problems closing the sockets.
	 */
	@After
	public void tearDown() throws SQLException, IOException {
		if (conn != null)
			conn.close();
		if (dispatcher != null) {
			ServerInit.destroySchema(dispatcher);
			dispatcher.closePool();
		}
		if (listeningSocket != null)
			listeningSocket.close();
		if (clientSocket != null)
			clientSocket.close();
		if (ssc != null)
			ssc.close();
	}

	/**
	 * Uses a mocker client to make typical requests to test the
	 * {@link ClientConnection} class.
	 * 
	 * @throws IOException
	 *             in case of an error writing in the sockets.
	 * @throws RemoteSocketClosedException
	 *             in case of an unexpectedly closed socket.
	 * @throws SQLException
	 *             in case of error during the database queries.
	 * @throws InvalidHeaderException
	 * @throws InterruptedException
	 */
	@Test
	public void testFullFlow() throws IOException, RemoteSocketClosedException,
			SQLException, InvalidHeaderException, InterruptedException {
		// Create the request processor.
		ClientConnection m = new ClientConnection(dispatcher);
		// Keep a database connection handy to check the status of the DB.
		conn = dispatcher.retrieveDatabaseConnection();

		// First lets try to connect client Mario, blocking until it is done.
		ConnectionRequest x = new ConnectionRequest("Mario", true);
		clientSocket.write(ProtocolMessage.toBytes(x));
		while (!m.processRead(listeningSocket))
			;

		// Now lets check that the client was created
		ArrayList<Client> clients = RetrieveClients.execute(conn);
		for (Client client : clients) {
			assertEquals(client.getClientUsername(), "Mario");
		}
		assertEquals(clients.size(), 1);

		// Let mario create several queues and delete some of them.
		String[] queues = { "HighPrio", "Temp", "Spam" };
		ArrayList<String> queueList = new ArrayList<String>();
		for (String queueName : queues) {
			QueueModificationRequest y = new QueueModificationRequest(
					queueName, false);
			clientSocket.write(ProtocolMessage.toBytes(y));
			while (!m.processRead(listeningSocket))
				;
			queueList.add(queueName);
		}
		ArrayList<Queue> queuesInDB = RetrieveQueues.execute(conn);
		for (Queue queue : queuesInDB) {
			assertTrue(queueList.contains(queue.getName()));
		}
		assertEquals(queuesInDB.size(), queues.length);
		clientSocket.write(ProtocolMessage
				.toBytes(new QueueModificationRequest(queues[1], true)));
		while (!m.processRead(listeningSocket))
			;
		queuesInDB = RetrieveQueues.execute(conn);
		for (Queue queue : queuesInDB) {
			assertTrue(queueList.contains(queue.getName()));
		}
		assertEquals(queuesInDB.size(), queues.length - 1);
		queueList.remove(queues[1]);

		// Now let Mario send some messages, since he is alone he will also send
		// auto-messages. Poor guy.
		clientSocket.write(ProtocolMessage.toBytes(new SendMessageRequest(
				"Hey I am a message", (byte) 10, 0, queueList)));
		while (!m.processRead(listeningSocket))
			;
		clientSocket.write(ProtocolMessage.toBytes(new SendMessageRequest(
				"It feels lonely in here", (byte) 8, 0, queueList, "Mario")));
		while (!m.processRead(listeningSocket))
			;
		clientSocket.write(ProtocolMessage.toBytes(new SendMessageRequest(
				"Hey I am a request", (byte) 5, 1, queueList)));
		while (!m.processRead(listeningSocket))
			;
		Thread.sleep(1000);
		clientSocket.write(ProtocolMessage.toBytes(new SendMessageRequest(
				"Hey I am a response", (byte) 2, 2, queueList, "Mario")));
		while (!m.processRead(listeningSocket))
			;
		ArrayList<Message> messages = GetAllMessages.execute(conn);
		assertEquals(messages.size(), 4 * queueList.size());
		int[] idList = { 0, 0, 0, 0 };
		for (Message message : messages) {
			idList[(int) message.getId() - 1]++;
			assertEquals(message.getSender(), "Mario");
			assertTrue(queueList.contains(message.getQueueName()));
			switch (message.getPriority()) {
			case 10:
				assertEquals(message.getContent(), "Hey I am a message");
				assertEquals(message.getContext(), 0);
				assertEquals(message.getReceiver(), null);
				break;
			case 8:
				assertEquals(message.getContent(), "It feels lonely in here");
				assertEquals(message.getContext(), 0);
				assertEquals(message.getReceiver(), "Mario");
				break;
			case 5:
				assertEquals(message.getContent(), "Hey I am a request");
				assertEquals(message.getContext(), 1);
				assertEquals(message.getReceiver(), null);
				break;
			case 2:
				assertEquals(message.getContent(), "Hey I am a response");
				assertEquals(message.getContext(), 2);
				assertEquals(message.getReceiver(), "Mario");
				break;
			default:
				fail("Found wrong message.");
				break;
			}
		}
		for (int i : idList) {
			assertEquals(i, 2);
		}
		// Now Mario will ask for queues with messages for him, we'll check the
		// result later
		clientSocket.write(ProtocolMessage.toBytes(new GetQueuesRequest()));
		while (!m.processRead(listeningSocket))
			;

		// Mario now peeks for the top priority message and the newest one in
		// each queue.
		clientSocket.write(ProtocolMessage.toBytes(new RetrieveMessageRequest(
				"HighPrio", Filter.QUEUE, Order.PRIORITY, true)));
		while (!m.processRead(listeningSocket))
			;
		clientSocket.write(ProtocolMessage.toBytes(new RetrieveMessageRequest(
				"Spam", Filter.QUEUE, Order.TIMESTAMP, true)));
		while (!m.processRead(listeningSocket))
			;
		clientSocket.write(ProtocolMessage.toBytes(new RetrieveMessageRequest(
				"Mario", Filter.SENDER, Order.PRIORITY, false)));
		while (!m.processRead(listeningSocket))
			;
		clientSocket.write(ProtocolMessage.toBytes(new RetrieveMessageRequest(
				"Alice", Filter.SENDER, Order.TIMESTAMP, false)));
		while (!m.processRead(listeningSocket))
			;

		// Now let's clear the write buffer in the processor.
		while (m.processWrite(listeningSocket) != WriteStatus.IDLE)
			;

		// We expect 14 responses, 9 of them are OK responses, 2 of them are
		// failures, 2 are messages and 1 is a list of queues.
		long[] messagesToDelete = new long[3];
		String[] fromQueues = new String[3];
		int j = 0;
		for (int i = 0; i < 14; i++) {
			ByteBuffer headerBuffer = ByteBuffer
					.allocate(ProtocolMessage.HEADER_SIZE);
			while (headerBuffer.hasRemaining())
				clientSocket.read(headerBuffer);
			headerBuffer.flip();
			int bodySize = ProtocolMessage.getBodySize(headerBuffer);
			ByteBuffer bodyBuffer = ByteBuffer.allocate(bodySize);
			while (bodyBuffer.hasRemaining())
				clientSocket.read(bodyBuffer);
			bodyBuffer.flip();
			ProtocolMessage response = ProtocolMessage.fromBytes(bodyBuffer);
			switch (response.getMessageType()) {
			case REQUEST_RESPONSE:
				RequestResponse rr = (RequestResponse) response;
				if (i < 9) {
					assertEquals(rr.getStatus(), Status.SUCCESS);
				} else {
					assertEquals(rr.getStatus(), Status.NO_MESSAGE);
				}
				break;
			case RETURNED_QUEUE:
				GetQueuesResponse gqr = (GetQueuesResponse) response;
				int size = 0;
				for (String queueName : gqr.getQueues()) {
					++size;
					assertTrue(queueList.contains(queueName));
				}
				assertEquals(size, 2);
				break;
			case RETURNED_MESSAGES:
				RetrieveMessageResponse rmr = (RetrieveMessageResponse) response;
				switch (rmr.getPriority()) {
				case 10:
					assertEquals(rmr.getSender(), "Mario");
					assertEquals(rmr.getMessageContent(), "Hey I am a message");
					assertEquals(rmr.getContext(), 0);
					assertEquals(rmr.getReceiver(), null);
					break;
				case 2:
					assertEquals(rmr.getSender(), "Mario");
					assertEquals(rmr.getMessageContent(), "Hey I am a response");
					assertEquals(rmr.getContext(), 2);
					assertEquals(rmr.getReceiver(), "Mario");
					break;
				default:
					fail("Unexpected message retrieved");
					break;
				}
				messagesToDelete[j] = rmr.getMessageId();
				fromQueues[j] = rmr.getQueue();
				++j;
				break;
			default:
				fail("Unexpected response " + response.getMessageType() + ".");
				break;
			}
		}

		clientSocket.write(ProtocolMessage.toBytes(new ConnectionRequest("",
				false)));
		while (!m.processRead(listeningSocket))
			;

		// Check the database for the result on messages and the client status
		messages = GetAllMessages.execute(conn);
		assertEquals(messages.size(), 4 * queueList.size() - 2);
		clients = RetrieveClients.execute(conn);
		for (Client client : clients) {
			assertEquals(client.getClientUsername(), "Mario");
			assertFalse(client.isClientOnline());
		}
		assertEquals(clients.size(), 1);

		// The writing out msut result in a disconnect flag
		while (m.processWrite(listeningSocket) != WriteStatus.DISCONNECT)
			;

		// Check that we get 1 response to the disconnection
		ByteBuffer headerBuffer = ByteBuffer
				.allocate(ProtocolMessage.HEADER_SIZE);
		while (headerBuffer.hasRemaining())
			clientSocket.read(headerBuffer);
		headerBuffer.flip();
		int bodySize = ProtocolMessage.getBodySize(headerBuffer);
		ByteBuffer bodyBuffer = ByteBuffer.allocate(bodySize);
		while (bodyBuffer.hasRemaining())
			clientSocket.read(bodyBuffer);
		bodyBuffer.flip();
		ProtocolMessage response = ProtocolMessage.fromBytes(bodyBuffer);
		assertEquals(response.getMessageType(), MessageType.REQUEST_RESPONSE);
		assertEquals(((RequestResponse) response).getStatus(), Status.SUCCESS);
	}
}
