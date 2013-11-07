package org.ftab.client.serverrpc;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;

import org.ftab.client.Client;
import org.ftab.client.Message;
import org.ftab.client.exceptions.AlreadyOnlineException;
import org.ftab.client.exceptions.ClientInexistentException;
import org.ftab.client.exceptions.FullServerException;
import org.ftab.client.exceptions.QueueInexistentException;
import org.ftab.client.exceptions.QueueNotEmptyException;
import org.ftab.client.exceptions.UnexpectedResponseException;
import org.ftab.client.exceptions.UnspecifiedErrorException;
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
import org.ftab.communication.responses.RetrieveMessageResponse;
import org.ftab.client.exceptions.QueueAEException;
import org.ftab.pubenums.Filter;
import org.ftab.pubenums.Order;

/**
 * Connects the client to the rest of the system by encapsulating and 
 * making available the interface of the server to the client.
 * @author Jean-Pierre Smith
 */
public class ServerRPC {
	/**
	 * The channel to the server
	 */
	private SocketChannel channelToServer;
	
	/**
	 * The address of the server
	 */
	private InetSocketAddress address;
	
	/**
	 * Creates a new server on the provided address.
	 * @param serverIPv4 The IPv4 address of the server or the
	 * server's hostname
	 * @param port The port the server is listening on
	 */
	public ServerRPC(String serverIPv4, int port) {
		this.address = new InetSocketAddress(serverIPv4, port);
	}

	/**
	 * Connects the client to the server represented by this instance
	 * @param client The client to connect
	 * @throws FullServerException If the server is full
	 * @throws AlreadyOnlineException If the client is already online
	 * @throws UnspecifiedErrorException If the server encounter an error but refuses to pass details
	 * @throws IOException If an error occurred with the channel
	 * @throws InvalidHeaderException If the input data was somehow corrupted
	 */
	public void Connect(Client client) 
			throws FullServerException, AlreadyOnlineException, UnspecifiedErrorException, IOException, InvalidHeaderException {
		// Attempt to close any existing channels
		if (channelToServer != null) {
			try {
				channelToServer.close();
			} catch (IOException e) { 
				e.printStackTrace();
			} finally {
				channelToServer = null;
			}
		}

		// Create the new socket channel
		channelToServer = SocketChannel.open(address);
					
		// Attempt to write the request to the channel
		this.sendMessage(new ConnectionRequest(client.getUsername(), true));
					
		RequestResponse msg = (RequestResponse) this.getResponse();
					
		switch (msg.getStatus()) {
		case SUCCESS:
			break;			
		case FULL_SERVER:
			throw new FullServerException(address.getHostName(), address.getPort());
		case USER_ONLINE:
			throw new AlreadyOnlineException(client.getUsername());
		case EXCEPTION:
			throw new UnspecifiedErrorException();
		default:
			throw new UnexpectedResponseException();
		}						
	}
	
	/**
	 * Disconnects the provided client from this server
	 * @param client The client to be disconnected
	 * @throws UnspecifiedErrorException If the server encounter an error but refuses to pass details
	 * @throws IOException If an error occurred with the channel
	 * @throws InvalidHeaderException If the input data was somehow corrupted
	 */
	public void Disconnect(Client client) 
			throws UnspecifiedErrorException, IOException, InvalidHeaderException {
		// Attempt to log out from the server
		this.sendMessage(new ConnectionRequest(client.getUsername(), false));
		RequestResponse response = (RequestResponse) this.getResponse();
		
		switch (response.getStatus()) {
		case SUCCESS:
			break;
		case EXCEPTION:
			throw new UnspecifiedErrorException();
		default:
			throw new UnexpectedResponseException();
		}
	}
	
	/**
	 * Tells whether or not there is a connection to this server.
	 * @return True if there is a connection, false otherwise.
	 */
	public boolean isConnectionOpen() {
		return channelToServer.isConnected();
	}
	
	/**
	 * Pushes a message onto one or more queues
	 * @param message The message to be pushed
	 * @throws QueueInexistentException If at least one of the queues does not exist
	 * @throws ClientInexistentException If the receiver does not exist
	 * @throws UnspecifiedErrorException If the server encounter an error but refuses to pass details
	 * @throws IOException If an error occurred with the channel
	 * @throws InvalidHeaderException If the input data was somehow corrupted
	 */
	public void PushMessage(Message message) 
			throws QueueInexistentException, ClientInexistentException, UnspecifiedErrorException, IOException, InvalidHeaderException {
		
		this.sendMessage(new SendMessageRequest(message.getContent(), message.getPriority(),
				message.getContext(), message.getQueues(), message.getReceiver()));
				
		RequestResponse msg = (RequestResponse) this.getResponse();
		
		switch(msg.getStatus()) {
		case SUCCESS:
			break;
		case NO_CLIENT:
			throw new ClientInexistentException(message.getReceiver());
		case QUEUE_NOT_EXISTS:
			throw new QueueInexistentException();			
		case EXCEPTION:
			throw new UnspecifiedErrorException();
		default:
			throw new UnexpectedResponseException();
		}
	}
	
	/**
	 * Deletes a queue from the system.	
	 * @param queueName The name of the queue to delete
	 * @throws IOException If an error occurred with the channel
	 * @throws InvalidHeaderException If the input data was somehow corrupted
	 * @throws QueueInexistentException If the queue to be deleted does not exist
	 * @throws QueueNotEmptyException If the queue to be deleted is not empty
	 * @throws UnspecifiedErrorException If the server encounter an error but refuses to pass details
	 */
	public void DeleteQueue(String queueName) 
			throws IOException, InvalidHeaderException, QueueInexistentException, QueueNotEmptyException, UnspecifiedErrorException {
		this.sendMessage(new QueueModificationRequest(queueName, true));
		
		RequestResponse msg = (RequestResponse) this.getResponse();
		
		switch(msg.getStatus()) {
		case SUCCESS:
			break;
		case QUEUE_NOT_EXISTS:
			throw new QueueInexistentException(queueName);
		case QUEUE_NOT_EMPTY:			
			throw new QueueNotEmptyException(queueName);
		case EXCEPTION:
			throw new UnspecifiedErrorException();
		default:
			throw new UnexpectedResponseException();
		}	
	}
	
	/**
	 * Creates a queue in the system
	 * @param queueName The name of the queue to be created
	 * @throws QueueAEException If the queue already exists
	 * @throws IOException If an error occurred on the channel
	 * @throws InvalidHeaderException If the response was somehow corrupted
	 * @throws UnspecifiedErrorException If the server encounter an error but refuses to pass details
	 */
	public void CreateQueue(String queueName) 
			throws QueueAEException, IOException, InvalidHeaderException, UnspecifiedErrorException {
		this.sendMessage(new QueueModificationRequest(queueName, false));
		
		RequestResponse msg = (RequestResponse) this.getResponse();
		
		switch(msg.getStatus()) {
		case SUCCESS:
			break;
		case QUEUE_EXISTS:
			throw new QueueAEException(queueName);
		case EXCEPTION:
			throw new UnspecifiedErrorException();
		default:
			throw new UnexpectedResponseException();
		}		
	}
	
	/**
	 * Gets the queues that have messages waiting for a particular client.
	 * @param client The client for whom queues should be fetched.
	 * @return An iterable containing the names of the queues that have messages
	 * waiting or an empty iterable if there are no queues.
	 * @throws UnspecifiedErrorException If the server encounter an error but refuses to pass details
	 * @throws IOException If an error occurred on the channel
	 * @throws InvalidHeaderException If the response was somehow corrupted
	 */
	public Iterable<String> GetWaitingQueues(Client client) 
			throws UnspecifiedErrorException, IOException, InvalidHeaderException {
		this.sendMessage(new GetQueuesRequest());
		
		ProtocolMessage message = this.getResponse();
		
		if (message.getMessageType() == MessageType.RETURNED_QUEUE) {
			return ((GetQueuesResponse)message).getQueues();
		} else {
			RequestResponse failureResponse = (RequestResponse) message;
			
			switch (failureResponse.getStatus()) {
			case NO_QUEUE:
				return new ArrayList<String>();
			case EXCEPTION:
				throw new UnspecifiedErrorException();
			default:
				throw new UnexpectedResponseException();
			}
		}		
	}
	
	/**
	 * Retrieves a message from the system
	 * @param value The name of the queue or sender to retrieve the message by.
	 * @param filter Whether to retrieve the message by queue or sender
	 * @param order Whether to retrieve the message by priority or time 
	 * @param delete True to delete the message, false otherwise
	 * @return The retrieved message object or null if no messages were found that met the criteria.
	 * @throws IOException If an error occurred on the channel
	 * @throws QueueInexistentException If the filter specified was a queue and it does not exist
	 * @throws ClientInexistentException If the filter specified was a sender and it does not exist
	 * @throws UnspecifiedErrorException If the server encounter an error but refuses to pass details
	 * @throws InvalidHeaderException If the response was somehow corrupted
	 */
	public Message RetrieveMessage(String value, Filter filter, Order order, boolean delete) 
			throws IOException, QueueInexistentException, ClientInexistentException, UnspecifiedErrorException, InvalidHeaderException {
		this.sendMessage(new RetrieveMessageRequest(value, filter, order, delete));

		ProtocolMessage message = this.getResponse();

		if (message.getMessageType() == MessageType.REQUEST_RESPONSE) {
			RequestResponse rqMessage = (RequestResponse) message;
			
			switch (rqMessage.getStatus()) {
			case NO_MESSAGE:
				return null;
			case QUEUE_NOT_EXISTS:
				if (filter == Filter.QUEUE) throw new QueueInexistentException(value);
				else throw new UnexpectedResponseException();
			case NO_CLIENT:
				if (filter == Filter.SENDER) throw new ClientInexistentException(value);
				else throw new UnexpectedResponseException(); 
			case EXCEPTION:
				throw new UnspecifiedErrorException();
			default:
				throw new UnexpectedResponseException();
			}
			
		} else {
			RetrieveMessageResponse response = (RetrieveMessageResponse) message;

			return new Message(response.getMessageId(), response.getContext(), (byte)response.getPriority(),
					response.getMessageContent(), response.getSender(), response.getReceiver(), response.getQueue());
		}
	}
			
	/**
	 * Sends a byte message over the socket-channel to the remote
	 * server.
	 * @param message The ProtocolMessage to be sent.
	 * @throws IOException If an IOException occurs while writing to the
	 * socket.
	 */
	private void sendMessage(ProtocolMessage message) throws IOException {
	    ByteBuffer buffer = ProtocolMessage.toBytes(message);
	    while(buffer.hasRemaining())
	        channelToServer.write(buffer);
	}
	
	/**
	 * Reads a response from the server over the socket and converts it to a ProtocolMessage.
	 * @return A protocol message representing the response from the server.
	 * @throws IOException If an IOException occurs while reading from the channel.
	 * @throws InvalidHeaderException If the header read had an invalid format
	 */
	private ProtocolMessage getResponse() throws IOException, InvalidHeaderException {
		// Attempt read the header information from the channel
		ByteBuffer buffer = ByteBuffer.allocate(ProtocolMessage.HEADER_SIZE);
		while(buffer.hasRemaining()) {
		    channelToServer.read(buffer);
		}
		buffer.flip();

		// Read the body and convert it into a message
		int bodySize = ProtocolMessage.getBodySize(buffer);
		buffer = ByteBuffer.allocate(bodySize);
		while(buffer.hasRemaining()) {
		    channelToServer.read(buffer);
		}
		buffer.flip();
		return ProtocolMessage.fromBytes(buffer);
	}
	
	@Override
	public String toString() {
		return String.format("Server at %s:%d", address.getHostName(), address.getPort());
	}
}
