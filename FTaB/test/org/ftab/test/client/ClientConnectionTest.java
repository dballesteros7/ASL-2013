package org.ftab.test.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.ftab.client.Client;
import org.ftab.communication.ProtocolMessage;
import org.ftab.communication.exceptions.InvalidHeaderException;
import org.ftab.communication.requests.ConnectionRequest;
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
public class ClientConnectionTest {
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
	 * Creates the server channel before each test is run
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {
		serverChannel = ServerSocketChannel.open();
		serverChannel.socket().bind(null, 0);
		portNumber = serverChannel.socket().getLocalPort();
	}

	/**
	 * Closes the server channel on completion of a test,
	 * resetting any connections that were present.
	 * @throws Exception
	 */
	@After
	public void tearDown() throws Exception {
		serverChannel.close();
	}

	/**
	 * Tests successfully creating a connection with a server
	 * @throws IOException
	 * @throws InvalidHeaderException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	@Test
	public void testSuccessfulConnection() throws IOException, InvalidHeaderException, InterruptedException, ExecutionException {
		Future<Boolean> result = service.submit(new Runnable() {
			@Override
			public void run() {
				final Client client = new Client("bob");
				assertTrue(client.Connect("localhost", portNumber));
			}
		}, Boolean.TRUE);
		
		SocketChannel channel = serverChannel.accept();
		
		// Get the cnnection request
		ConnectionRequest conRequest = (ConnectionRequest) this.getMessage(channel);
		assertEquals(conRequest.isConnection(), true);
		assertEquals(conRequest.getUsername(), "bob");
		
		this.sendMessage(new RequestResponse(Status.SUCCESS), channel);
		result.get();		
	}

	/**
	 * Tests that the client will recognise reponses that are not successful completions
	 * @throws IOException
	 * @throws InvalidHeaderException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	@Test
	public void testUnSuccessfulConnection() throws IOException, InvalidHeaderException, InterruptedException, ExecutionException {
		Future<Boolean> result = service.submit(new Runnable() {
			@Override
			public void run() {
				final Client client = new Client("bob");
				assertFalse(client.Connect("localhost", portNumber));
			}
		}, Boolean.FALSE);
		
		SocketChannel channel = serverChannel.accept();
		
		// Get the cnnection request
		ConnectionRequest conRequest = (ConnectionRequest) this.getMessage(channel);
		assertEquals(conRequest.isConnection(), true);
		assertEquals(conRequest.getUsername(), "bob");
		
		this.sendMessage(new RequestResponse(Status.FULL_SERVER), channel);
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
