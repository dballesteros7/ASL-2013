package org.ftab.test.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.ftab.client.Client;
import org.ftab.communication.ProtocolMessage;
import org.ftab.communication.exceptions.InvalidHeaderException;
import org.ftab.communication.requests.ConnectionRequest;
import org.ftab.communication.requests.GetQueuesRequest;
import org.ftab.communication.requests.QueueModificationRequest;
import org.ftab.communication.requests.SendMessageRequest;
import org.ftab.communication.responses.GetQueuesResponse;
import org.ftab.communication.responses.RequestResponse;
import org.ftab.communication.responses.RequestResponse.Status;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests the methods used by the client
 * @author Jean-Pierre Smith
 *
 */
public class ClientTests_AlreadyConnected {
	/**
	 * The ServerSocketChannel listening for requests from clients
	 */
	private ServerSocketChannel serverChannel;
	
	/**
	 * The port number that the server socket is created on
	 */
	private int portNumber;
	
	/**
	 * An ExecutorService to support concurrent execution of the client's request
	 * while manually controlling it with the server.
	 */
	private ExecutorService service = Executors.newSingleThreadExecutor(); 
	
	/**
	 * The client being used for the tests
	 */
	private Client client;
	
	/**
	 * The server's socket channel to the client
	 */
	private SocketChannel channel; 
	
	/**
	 * Creates the server channel before each test is run
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {
		serverChannel = ServerSocketChannel.open();
		serverChannel.socket().bind(null, 0);
		portNumber = serverChannel.socket().getLocalPort();
		
		// Connect the client
		Future<Boolean> result = service.submit(new Runnable() {
			@Override
			public void run() {
				client = new Client("bob");
				try {
					client.Connect("localhost", portNumber);
				} catch (Exception e) {
					fail("Failure during setup...");
				}
			}
		}, Boolean.TRUE);
		
		channel = serverChannel.accept();
		
		// Get the connection request
		ConnectionRequest conRequest = (ConnectionRequest) this.getMessage(channel);
		
		this.sendMessage(new RequestResponse(Status.SUCCESS), channel);
		result.get();
	}

	/**
	 * Closes the server channel on completion of a test,
	 * resetting any connections that were present.
	 * @throws Exception
	 */
	@After
	public void tearDown() throws Exception {
		serverChannel.close();
		channel = null;
		client = null;
	}

	/**
	 * Tests that the client recognises successful and unsuccessful
	 * disconnection attempts
	 * @throws IOException
	 * @throws InvalidHeaderException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	@Test
	public void testDisconnection() throws IOException, InvalidHeaderException, InterruptedException, ExecutionException {
		Future<Boolean> result = service.submit(new Runnable() {
			@Override
			public void run() {
				try {
					assertFalse(client.Disconnect());
					assertTrue(client.Disconnect());
				} catch (Exception e){ 
					fail("Exception should not be thrown"); 
				}
			}
		}, Boolean.TRUE);
		
		// Get the connection request
		ConnectionRequest conRequest = (ConnectionRequest) this.getMessage(channel);
		assertEquals(conRequest.isConnection(), false);
		assertEquals(conRequest.getUsername(), null);
		
		this.sendMessage(new RequestResponse(Status.EXCEPTION), channel);
		
		// Get the connection request
		conRequest = (ConnectionRequest) this.getMessage(channel);
		assertEquals(conRequest.isConnection(), false);
		assertEquals(conRequest.getUsername(), null);

		this.sendMessage(new RequestResponse(Status.SUCCESS), channel);
		
		result.get();		
	}

	/**
	 * Tests whether clients can send messages over the connection properly as well as recognise 
	 * the various responses.
	 * @throws IOException
	 * @throws InvalidHeaderException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	@Test
	public void testSendingMessages() throws IOException, InvalidHeaderException, InterruptedException, ExecutionException {
		final String[] messages = { "Hello world!", "Hello world! Prepare to die edition...", 
				"This is bound to fail" };
		final byte[] priorities = { 5, 9, 1 };
		final int[] contexts = { 0, 1, 2 };
		final String[] receivers = { null, "mary", "mallot" };
		final String[][] queues = new String[][] { 
				{"queue 1", "queue 2", "queue 3", "queue 4" },
				{"queue 5"},
				{"queue 6", "queue 7" }			
		};
		
		Future<Boolean> result = service.submit(new Runnable() {
			@Override
			public void run() {
				try {
				assertTrue(client.SendMessage(messages[0], priorities[0], contexts[0], 
						queues[0]));
				assertTrue(client.SendMessage(messages[1], priorities[1], contexts[1], 
						receivers[1], queues[1]));
				assertFalse(client.SendMessage(messages[2], priorities[2], contexts[2], 
						receivers[2], queues[2]));
				} catch (Exception e) { fail("Exceptions should not be thrown."); }
			}
		}, Boolean.TRUE);
		
		// Get the send message request request
		SendMessageRequest request = (SendMessageRequest) this.getMessage(channel);
		assertEquals(messages[0], request.getMessage());
		assertEquals(priorities[0], request.getPriority());
		assertEquals(false, request.hasReceiver());
		List<String> queuesList = (List<String>) request.getQueueList();
		assertEquals(queues[0].length, queuesList.size());
		for (int i = 0; i < queuesList.size(); i++ ) assertEquals(queues[0][i], queuesList.get(i));
		
		this.sendMessage(new RequestResponse(Status.SUCCESS), channel);
		
		// Get the send message request request
		request = (SendMessageRequest) this.getMessage(channel);
		assertEquals(messages[1], request.getMessage());
		assertEquals(priorities[1], request.getPriority());
		assertEquals(receivers[1], request.getReceiver());
		queuesList = (List<String>) request.getQueueList();
		assertEquals(queues[1].length, queuesList.size());
		for (int i = 0; i < queuesList.size(); i++ ) assertEquals(queues[1][i], queuesList.get(i));
		
		this.sendMessage(new RequestResponse(Status.SUCCESS), channel);
		
		// Get the send message request request
		request = (SendMessageRequest) this.getMessage(channel);
		assertEquals(messages[2], request.getMessage());
		assertEquals(priorities[2], request.getPriority());
		assertEquals(receivers[2], request.getReceiver());
		queuesList = (List<String>) request.getQueueList();
		assertEquals(queues[2].length, queuesList.size());
		for (int i = 0; i < queuesList.size(); i++ ) assertEquals(queues[2][i], queuesList.get(i));

		this.sendMessage(new RequestResponse(Status.NO_CLIENT), channel);
		
		result.get();
	}
	
	/**
	 * Tests whether the client can send requests to modify queues and identify when it
	 * was not successful.
	 * @throws IOException
	 * @throws InvalidHeaderException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	@Test
	public void testModifyingQueues() throws IOException, InvalidHeaderException, InterruptedException, ExecutionException {
		final String[] names = { "queue 1", "queue 2", "queue 3", "queue 4", "queue 5", "queue 6" };
		final Status[] results = { Status.SUCCESS, Status.SUCCESS, Status.SUCCESS, Status.QUEUE_NOT_EMPTY, Status.QUEUE_EXISTS, 
				Status.QUEUE_NOT_EXISTS };
		
		Future<Boolean> result = service.submit(new Runnable() {
			@Override
			public void run() {
				for (int i = 0; i < names.length; i ++) {
					boolean result = i > 2 ? false : true; 
					
					if (i % 2 == 0) {
						try {
							assertEquals(result, client.CreateQueue(names[i]));
						} catch (Exception e) {
							fail("Exception was thrown");
						} 
					} else {
						try {
							assertEquals(result, client.DeleteQueue(names[i]));
						} catch (Exception e) {
							fail("Exception was thrown");
						}
					}
				}
			}
		}, Boolean.TRUE);
		
		QueueModificationRequest request;
		// Get the request
		for (int i = 0; i <names.length; i++) {
			request = (QueueModificationRequest) this.getMessage(channel);
			assertEquals(names[i], request.getQueueName());
			this.sendMessage(new RequestResponse(results[i]), channel);
		}
		
		
		result.get();
	}
	
	/** 
	 * Tests whether the client can send a request and identify different responses for
	 * returned queues.
	 * @throws IOException
	 * @throws InvalidHeaderException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	@Test
	public void testFetchingQueues() throws IOException, InvalidHeaderException, InterruptedException, ExecutionException {
		final String[][] results = new String[][] {
			new String[] { "queue 1", "queue 2" },
			new String[] { "queue 3" },
			null
		};
		
		Future<Boolean> result = service.submit(new Runnable() {
			@Override
			public void run() {
				for (int i = 0; i < results.length; i ++) {
					ArrayList<String> queues = null;
					try {
						queues = (ArrayList<String>) client.GetWaitingQueues();
					} catch (Exception e) {
						fail("Exception was thrown.");
					}
					
					if (i == 2) assertEquals(results[i], queues);
					else {
						assertEquals(results[i].length, queues.size());
						for (int j = 0; j < queues.size(); j++) {
							assertEquals(results[i][j], queues.get(j));
						}
					}
					
					
				}
			}
		}, Boolean.TRUE);
		
		// Get the request
		for (int i = 0; i < results.length; i++) {
			assertTrue(this.getMessage(channel) instanceof GetQueuesRequest);
			this.sendMessage(i == 2 
					? new RequestResponse(Status.EXCEPTION) 
					: new GetQueuesResponse(Arrays.asList(results[i])), channel);
		}		
		
		result.get();
	}
	
	
	
	
	/**
	 * Gets a message from a channel and parses it to a ProtocolMessage
	 * @param channel The channel from which to retreive the message
	 * @return The message that was sent over the channel
	 * @throws IOException
	 * @throws InvalidHeaderException
	 */
	private ProtocolMessage getMessage(SocketChannel channel) throws IOException, InvalidHeaderException {
		ByteBuffer buffer = ByteBuffer.allocate(ProtocolMessage.HEADER_SIZE);
		channel.read(buffer);
		buffer.flip();
		int size = ProtocolMessage.getBodySize(buffer);
		
		buffer = ByteBuffer.allocate(size);
		channel.read(buffer);
		buffer.flip();
		return ProtocolMessage.fromBytes(buffer);
	}
	
	/**
	 * Sends a message over the specified channel.
	 * @param msg The ProtocolMessage to be sent.
	 * @param channel
	 * @throws IOException
	 */
	private void sendMessage(ProtocolMessage msg, SocketChannel channel) throws IOException {
		channel.write(ProtocolMessage.toBytes(msg));
	}	
}
