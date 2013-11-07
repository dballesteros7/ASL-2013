package org.ftab.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import org.ftab.communication.ProtocolMessage;
import org.ftab.communication.ProtocolMessage.MessageType;
import org.ftab.communication.exceptions.InvalidHeaderException;
import org.ftab.communication.requests.ConnectionRequest;
import org.ftab.communication.requests.GetQueuesRequest;
import org.ftab.communication.requests.QueueModificationRequest;
import org.ftab.communication.requests.RetrieveMessageRequest;
import org.ftab.communication.requests.RetrieveMessageRequest.Filter;
import org.ftab.communication.requests.RetrieveMessageRequest.Order;
import org.ftab.communication.requests.SendMessageRequest;
import org.ftab.communication.requests.SendMessageRequest.Context;
import org.ftab.communication.responses.GetQueuesResponse;
import org.ftab.communication.responses.RequestResponse;
import org.ftab.communication.responses.RetrieveMessageResponse;

/**
 * The api for making calls as a client
 * @author Jean-Pierre Smith
 *
 */
public class Client {
	/**
	 * Socket channel to be used for connections to the server
	 */
	private SocketChannel channelToServer;
	
	/**
	 * The sting that represents the user's login information
	 */
	//TODO: Make a getter and return to private
	public final String username;
	
	/**
	 * Creates a new instance of a client with the specified name
	 * @param clientName 
	 */
	public Client(String clientName) {
		this.username = clientName;
		
		// TODO: Log the successful creation
	}
	
	/**
	 * Connects this client to a server.
	 * @param serverIPv4 The string representation or hostname of the server
	 * to connect to.
	 * @param port The port to connect to.
	 * @return True if the connection was successful, false otherwise
	 */
	public boolean Connect(String serverIPv4, int port) {
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

		try {
			// Create the new socket channel
			channelToServer = SocketChannel.open(new InetSocketAddress(serverIPv4, port));
			
			// Attempt to write the request to the channel
			this.sendMessage(new ConnectionRequest(this.username, true));
			
			RequestResponse msg = (RequestResponse) this.getResponse();
			
			switch (msg.getStatus()) {
			case SUCCESS:
				// TODO: Log the success
				return true;
			default:
				// TODO: Log the failure using the status
				return false;
			}			
		} catch (IOException e) {
			// TODO: Log the failed connection attempt

			e.printStackTrace();
			return false;
		} catch (InvalidHeaderException e) {
			e.printStackTrace();
			return false;
		}			
	}
	
	/**
	 * Disconnects the client from a remote server and closes the channel
	 * between the client and the server
	 * @return True if the disconnection was successful, false otherwise.
	 */
	public boolean Disconnect() {
		try {
			// Attempt to log out from the server
			this.sendMessage(new ConnectionRequest(this.username, false));
			RequestResponse response = (RequestResponse) this.getResponse();
			
			switch(response.getStatus()) {
			case SUCCESS:
				// TODO: Log the disconnection success
				return true;
			default:
				// TODO: Log the disconnection failure
				return false;
			}
			
		} catch (IOException e) {
			// TODO: Log the failed disconnection attempt
			e.printStackTrace();
			return false;
		} catch (InvalidHeaderException e) {
			// TODO: Assuming success?
			// TODO: Log the disconnection success
			e.printStackTrace();
			return true;
		}		
	}

	/**
	 * Sends a message from this client to one or more queues with the possibility
	 * of a designated receiver.
	 * @param message The string message to be sent.
	 * @param priority The priority of the message
	 * @param context The context of the message
	 * @param queues The queue or queues to which to send the messages
	 * @param receiver The designated receiver of the message
	 * @return True if the message is sent successfully, false otherwise
	 */
	public boolean SendMessage(String message, byte priority, Context context, Iterable<String> queues, String receiver) {
		try {
			this.sendMessage(new SendMessageRequest(message, priority, context, queues, receiver));
			
			RequestResponse msg = (RequestResponse) this.getResponse();
			
			switch(msg.getStatus()) {
			case SUCCESS:
				// TODO: Log the success
				return true;
			default:
				// TODO: Log the failure using the status
				return false;
			}			
			
		} catch (IOException e) {
			// TODO Log the IO Exception
			e.printStackTrace();
			return false;
		} catch (InvalidHeaderException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Sends a message from this client to one or more queues without a desingated 
	 * receiver.
	 * @param message The string message to be sent.
	 * @param priority The priority of the message
	 * @param context The context of the message
	 * @param queues The queue or queues to which to send the messages
	 * @return True if the message is sent successfully, false otherwise
	 */
	public boolean SendMessage(String message, byte priority, Context context, Iterable<String> queues) {
		return this.SendMessage(message, priority, context, queues, null);
	}
	
	/**
	 * Requests that a named queue be deleted from the system
	 * @param queueName The name of the queue to be deleted.
	 * @return True if the queue was deleted, false otherwise.
	 */
	public boolean DeleteQueue(String queueName) {
		try {
			this.sendMessage(new QueueModificationRequest(queueName, true));
		
			RequestResponse msg = (RequestResponse) this.getResponse();
			
			switch(msg.getStatus()) {
			case SUCCESS:
				// TODO: Log the success
				return true;
			default:
				// TODO: Log the failure using the status
				return false;
			}
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (InvalidHeaderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Requests that a queue be created in the the system with the
	 * specified name.
	 * @param queueName The name of the queue to create
	 * @return True if the queue was created successfully, false otherwise.
	 */
	public boolean CreateQueue(String queueName) {
		try {
			this.sendMessage(new QueueModificationRequest(queueName, false));
		
			RequestResponse msg = (RequestResponse) this.getResponse();
			
			switch(msg.getStatus()) {
			case SUCCESS:
				// TODO: Log the success
				return true;
			default:
				// TODO: Log the failure using the status
				return false;
			}
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (InvalidHeaderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Gets the queues that have messages waiting for the client.
	 * @return An Iterable containing the names of the queues with messages waiting
	 * for the client, or null if an error occurred or if there are no queues.
	 */
	public Iterable<String> GetWaitingQueues() {
		try {
			this.sendMessage(new GetQueuesRequest());
			
			ProtocolMessage message = this.getResponse();
			
			if (message.getMessageType() == MessageType.RETURNED_QUEUE) {
				return ((GetQueuesResponse)message).getQueues();
			} else {
				RequestResponse failureResponse = (RequestResponse) message;
				switch (failureResponse.getStatus()) {
				case NO_QUEUE:
					// TODO: Log the reason that there were no queues to fetch
					return null;
				default:
					// TODO: Log the reason for the failure
					return null;
				}
			}			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (InvalidHeaderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
	}
	
	
	/**
	 * Retrieves a message from a queue
	 * @param queueName The name of the queue from which to retrieve the message
	 * @param andRemove Whether to delete the message after retrieving it.
	 * @return The message with the highest priority in that queue
	 */
	public Message ViewMessageFromQueue(String queueName, boolean andRemove) {
		return ViewMessageFromQueue(queueName, andRemove, Order.PRIORITY);
	}
	
	/**
	 * Retrieves a message from a queue
	 * @param queueName The name of the queue from which to retrieve the message
	 * @param andRemove Whether to delete the message after retrieving it.
	 * @param orderedBy A value indicating how to determine which message to return. 
	 * @return The message that was selected 
	 */
	public Message ViewMessageFromQueue(String queueName, boolean andRemove, Order orderedBy) {
		try {
			return this.retrieveMessage(queueName, Filter.QUEUE, orderedBy, andRemove);
		} catch (RequestResponseException e) {
			// TODO Log the message appropriately
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Log the message appropriately
			e.printStackTrace();
		} catch (InvalidHeaderException e) {
			// TODO Log the message appropriately
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Retrieves a message to be viewed from a particular sender.
	 * @param senderName The name of the sender
	 * @param andRemove Whether to delete the message after retrieving it.
	 * @return The message with the highest priority from the sender.
	 */
	public Message ViewMessageFromSender(String senderName, boolean andRemove) {
		return ViewMessageFromSender(senderName, andRemove, Order.PRIORITY);
	}
	
	/**
	 * Retrieves a message to be viewed from a particular sender.
	 * @param senderName The name of the sender
	 * @param andRemove Whether to delete the message after retrieving it.
	 * @param orderedBy A value indicating how to determine which message to return.
	 * @return The message that was selected.
	 */
	public Message ViewMessageFromSender(String senderName, boolean andRemove, Order orderedBy) {
		try {
			return this.retrieveMessage(senderName, Filter.SENDER, orderedBy, andRemove);
		} catch (RequestResponseException e) {
			// TODO Log the message appropriately
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Log the message appropriately
			e.printStackTrace();
		} catch (InvalidHeaderException e) {
			// TODO Log the message appropriately
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Handles retrieval of messages from the server
	 * @param name The value to filter by
	 * @param filter The type of filter to apply, sender or queue
	 * @param order The order in which to return the details
	 * @param delete Whether to delete the message or not
	 * @return A message object containing the details of the message
	 * @throws RequestResponseException If the request was not carried out successfully
	 * as deemed by the server
	 * @throws IOException If an IOException occurred while using the channel
	 * @throws InvalidHeaderException If the data was corrupted in transit and was thus
	 * rendered indecipherable.
	 */
	private Message retrieveMessage(String value, Filter filter, Order order, boolean delete) throws RequestResponseException, 
																										IOException, InvalidHeaderException {
		this.sendMessage(new RetrieveMessageRequest(value, filter, order, delete));

		ProtocolMessage message = this.getResponse();

		if (message.getMessageType() == MessageType.REQUEST_RESPONSE) {
			RequestResponse rqMessage = (RequestResponse) message;
			throw new RequestResponseException(rqMessage.getDescription(), rqMessage.getStatus());				
		} else {
			RetrieveMessageResponse response = (RetrieveMessageResponse) message;

			return new Message(response.getMessageId(), response.getContext(), (byte)response.getPriority(),
					response.getMessageContent(), response.getSender(), response.getQueue(), response.getReceiver());
		}
	}
		
	/**
	 * Sends a message over this client's socket-channel to the remote
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
	 * Reads a response from the server over the socket channel used by this client
	 * and converts it to a ProtocolMessage.
	 * @return A protocol message representing the response from the server.
	 * @throws IOException If an IOException occurs while reading from the channel.
	 * @throws InvalidHeaderException If the header read had an invalid format
	 */
	private ProtocolMessage getResponse() throws IOException, InvalidHeaderException {
		// Attempt read the header information from the channel
		ByteBuffer buffer = ByteBuffer.allocate(ProtocolMessage.HEADER_SIZE);
		while(buffer.hasRemaining())
		    channelToServer.read(buffer);
		buffer.flip();

		// Read the body and convert it into a message
		int bodySize = ProtocolMessage.getBodySize(buffer);
		buffer = ByteBuffer.allocate(bodySize);
		while(buffer.hasRemaining())
		    channelToServer.read(buffer);
		buffer.flip();
		return ProtocolMessage.fromBytes(buffer);
	}	
}
