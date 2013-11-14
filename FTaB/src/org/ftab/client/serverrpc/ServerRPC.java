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
 * Represents a specific middlewae server and provides an interface of the 
 * calls that the client can make of the system
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
	 * Connects the provided client to the middleware server represented 
	 * by this instance.
	 * @param client The client to connect to this server.
	 * @throws FullServerException If the server is already at full capacity
	 * @throws AlreadyOnlineException If the client is already online in the
	 * system
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
	 * Disconnects the specified client from this server and
	 * the system.
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
	 * Ascertains whether there is a connection open with this server.
	 * @return True if there is a connection, false otherwise.
	 * @aslexclude
	 */
	public boolean isConnectionOpen() {
		return channelToServer.isConnected();
	}
	
	/**
	 * Pushes a message onto the system.
	 * @param message The message to be pushed onto the system.
	 * @throws QueueInexistentException If at least one of the queues specified in the 
	 * message does not exist in the system.
	 * @throws ClientInexistentException If the receiver specified in the message does not 
	 * exist in the system.
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
	 * Removes an empty queue from the system.	
	 * @param queueName The name of the queue to be deleted
	 * @throws IOException If an error occurred with the channel
	 * @throws InvalidHeaderException If the input data was somehow corrupted
	 * @throws QueueInexistentException If the queue to be deleted does not exist in the system
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
	 * Creates a new queue in the system with the specified name.
	 * @param queueName The name to be assigned to the newly created queue
	 * @throws QueueAEException If a queue with the specified name already exists 
	 * in the system
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
	 * Retrieves the names of the queues that have messages waiting the 
	 * specified client
	 * @param client The client for whom queues with waiting messages should be fetched
	 * @return An Iterable containing the names of the queues that have messages
	 * waiting
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
	 * Retrieves a message from the system according to the specified filter and
	 * ordering.
	 * @param value The name of the queue or sender to retrieve the message by.
	 * @param filter Whether to retrieve a message on a particular queue or sent by
	 * a particular sender
	 * @param order Whether to retrieve the message with the highest priority or 
	 * with the earliest time 
	 * @param delete <b>true</b> to delete the message on retrieval, <b>false</b> otherwise
	 * @return The retrieved message object or null if no messages were found that met the criteria.
	 * @throws IOException If an error occurred on the channel
	 * @throws QueueInexistentException If the filter specified was a queue but the specified name
	 * does not exist in the system as a queue
	 * @throws ClientInexistentException If the filter specified was a sender but the specified name
	 * does not exist in the system as a client.
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
