package org.ftab.client;

import java.io.IOException;
import java.util.logging.Logger;

import org.ftab.client.exceptions.AlreadyOnlineException;
import org.ftab.client.exceptions.ClientInexistentException;
import org.ftab.client.exceptions.FullServerException;
import org.ftab.client.exceptions.QueueInexistentException;
import org.ftab.client.exceptions.QueueNotEmptyException;
import org.ftab.client.exceptions.UnspecifiedErrorException;
import org.ftab.client.serverrpc.ServerRPC;
import org.ftab.communication.exceptions.InvalidHeaderException;
import org.ftab.communication.requests.SendMessageRequest.Context;
import org.ftab.database.exceptions.QueueAlreadyExistsException;
import org.ftab.logging.client.ClientConnectionRecord;
import org.ftab.logging.client.ClientDisconRecord;
import org.ftab.logging.client.GetMessageLogRecord;
import org.ftab.logging.client.QueueCreateLogRecord;
import org.ftab.logging.client.QueueDeleteLogRecord;
import org.ftab.logging.client.SendMsgLogRecord;
import org.ftab.logging.client.WaitingQueuesLogRecord;
import org.ftab.pubenums.Filter;
import org.ftab.pubenums.Order;

/**
 * The api for making calls as a client
 * @author Jean-Pierre Smith
 *
 */
public class Client {
	/**
	 * Boolean indicating whether errors should be suppressed.
	 */
	private boolean suppressingErrors = true;	
	
	/**
	 * The sting that represents the user's login information
	 */
	private final String username;
	
	/**
	 * The remote server being communicated with by this client
	 */
	private ServerRPC server;
	
	/**
	 * The logger used by the client
	 */
	private static final Logger LOGGER = Logger.getLogger(Client.class.getName());
		
	/**
	 * Creates a new instance of a client with the specified name
	 * @param clientName 
	 */
	public Client(String clientName) {
		this.username = clientName;
		this.suppressingErrors = true;
	}
	
	/**
	 * Gets the username assigned to this client at creation.
	 * @return The assigned username
	 */
	public String getUsername() {
		return username;
	}
	
	/**
	 * Gets whether errors are being suppressed.
	 * @return True if errors will not be thrown by the methods, false otherwise
	 */
	public boolean isSuppressingErrors() {
		return suppressingErrors;
	}
	
	/**
	 * Sets whether exceptions should be suppressed by the client
	 * @param suppressingErrors True to suppress exceptions from being thrown, false otherwise.
	 */
	public void setSuppressingErrors(boolean suppressingErrors) {
		this.suppressingErrors = suppressingErrors;
	}
	
	/**
	 * Ascertains whether the client is connected to 
	 * a server or not
	 * @return True if the client is connected, false otherwise.
	 */
	public boolean isConnected() {
		if (server == null) return false;
		else return server.isConnectionOpen();
	}
	
	/**
	 * Connects this client to a server.
	 * @param serverIPv4 The string representation or hostname of the server
	 * to connect to.
	 * @param port The port to connect to.
	 * @return True if the connection was successful, false otherwise
	 * @throws FullServerException If the server is full
	 * @throws AlreadyOnlineException If the client is already online
	 * @throws UnspecifiedErrorException If the server encounter an error but refuses to pass details
	 * @throws IOException If an error occurred with the channel
	 * @throws InvalidHeaderException If the input data was somehow corrupted
	 */
	public boolean Connect(String serverIPv4, int port) 
			throws IOException, UnspecifiedErrorException, InvalidHeaderException, FullServerException, AlreadyOnlineException {
		// Create the server
		this.server = new ServerRPC(serverIPv4, port);		
		
		// Log the start of the attempt
		final ClientConnectionRecord record = new ClientConnectionRecord(this);
		LOGGER.log(record);
				
		try {
			server.Connect(this);

			LOGGER.log(new ClientConnectionRecord(this, record.getMillis()));
			return true;
		} catch (IOException e) {
			LOGGER.log(new ClientConnectionRecord(this, e, record.getMillis()));
			if (!suppressingErrors) throw e;
		} catch (InvalidHeaderException e) {
			LOGGER.log(new ClientConnectionRecord(this, e, record.getMillis()));
			if (!suppressingErrors) throw e;
		} catch (FullServerException e) {
			LOGGER.log(new ClientConnectionRecord(this, e, record.getMillis()));
			if (!suppressingErrors) throw e;
		} catch (AlreadyOnlineException e) {
			LOGGER.log(new ClientConnectionRecord(this, e, record.getMillis()));
			if (!suppressingErrors) throw e;
		} catch (UnspecifiedErrorException e) {
			LOGGER.log(new ClientConnectionRecord(this, e, record.getMillis()));
			if (!suppressingErrors) throw e;
		}
		
		return false;
	}
	
	/**
	 * Disconnects the client from a remote server and closes the channel
	 * between the client and the server
	 * @return True if the disconnection was successful, false otherwise.
	 * @throws UnspecifiedErrorException If the server encounter an error but refuses to pass details
	 * @throws IOException If an error occurred with the channel
	 * @throws InvalidHeaderException If the input data was somehow corrupted
	 */
	public boolean Disconnect() throws InvalidHeaderException, IOException, UnspecifiedErrorException {
		// Log the start of the attempt
		final ClientDisconRecord record = new ClientDisconRecord(this);
		LOGGER.log(record);
		
		try {
			server.Disconnect(this);
		
			LOGGER.log(new ClientDisconRecord(this, record.getMillis()));
			return true;
		} catch (IOException e) {
			LOGGER.log(new ClientDisconRecord(this, e, record.getMillis()));
			if (!suppressingErrors) throw e;
		} catch (InvalidHeaderException e) {
			LOGGER.log(new ClientDisconRecord(this, e, record.getMillis()));
			if (!suppressingErrors) throw e;
			
			// Assume the attempt was successful
			return true;
		} catch (UnspecifiedErrorException e) {
			LOGGER.log(new ClientDisconRecord(this, e, record.getMillis()));
			if (!suppressingErrors) throw e;
		}
		
		return false;
	}

	/**
	 * Sends a message from this client to one or more queues with the possibility
	 * of a designated receiver.
	 * @param message The string message to be sent.
	 * @param priority The priority of the message
	 * @param context The context of the message
	 * @param receiver The designated receiver of the message
	 * @param queues The queue or queues to which to send the messages
	 * @return True if the message is sent successfully, false otherwise
	 * @throws QueueInexistentException If at least one of the queues does not exist
	 * @throws ClientInexistentException If the receiver does not exist
	 * @throws UnspecifiedErrorException If the server encounter an error but refuses to pass details
	 * @throws IOException If an error occurred with the channel
	 * @throws InvalidHeaderException If the input data was somehow corrupted
	 */
	public boolean SendMessage(String message, byte priority, Context context, String receiver, String... queues) 
			throws QueueInexistentException, UnspecifiedErrorException, ClientInexistentException, IOException, InvalidHeaderException {
		
		final Message msg = new Message(context, priority, message, this.getUsername(), receiver, queues);
		
		/* Log the start of the attempt */
		final SendMsgLogRecord record = new SendMsgLogRecord(this, msg);
		LOGGER.log(record);
		
		try {
			server.PushMessage(msg);
			
			LOGGER.log(new SendMsgLogRecord(this, msg, record.getMillis()));
			return true;
		
			/* Log the failed attempt */
		} catch (QueueInexistentException e) {
			LOGGER.log(new SendMsgLogRecord(this, msg, e, record.getMillis()));
			if (!suppressingErrors) throw e;
		} catch (ClientInexistentException e) {
			LOGGER.log(new SendMsgLogRecord(this, msg, e, record.getMillis()));
			if (!suppressingErrors) throw e;
		} catch (UnspecifiedErrorException e) {
			LOGGER.log(new SendMsgLogRecord(this, msg, e, record.getMillis()));
			if (!suppressingErrors) throw e;
		} catch (IOException e) {
			LOGGER.log(new SendMsgLogRecord(this, msg, e, record.getMillis()));
			if (!suppressingErrors) throw e;
		} catch (InvalidHeaderException e) {
			LOGGER.log(new SendMsgLogRecord(this, msg, e, record.getMillis()));
			if (!suppressingErrors) throw e;
		}
		
		return false;
	}
	
	/**
	 * Sends a message from this client to one or more queues without a designated 
	 * receiver.
	 * @param message The string message to be sent.
	 * @param priority The priority of the message
	 * @param context The context of the message
	 * @param queues The queue or queues to which to send the messages
	 * @return True if the message is sent successfully, false otherwise
	 * @throws QueueInexistentException If at least one of the queues does not exist
	 * @throws ClientInexistentException If the receiver does not exist
	 * @throws UnspecifiedErrorException If the server encounter an error but refuses to pass details
	 * @throws IOException If an error occurred with the channel
	 * @throws InvalidHeaderException If the input data was somehow corrupted
	 */
	public boolean SendMessage(String message, byte priority, Context context, String... queues) 
			throws QueueInexistentException, UnspecifiedErrorException, ClientInexistentException, IOException, InvalidHeaderException {
		return this.SendMessage(message, priority, context, null, queues);
	}
	
	/**
	 * Requests that a named queue be deleted from the system
	 * @param queueName The name of the queue to be deleted.
	 * @return True if the queue was deleted, false otherwise.
	 * @throws IOException If an error occurred with the channel
	 * @throws InvalidHeaderException If the input data was somehow corrupted
	 * @throws QueueInexistentException If the queue to be deleted does not exist
	 * @throws QueueNotEmptyException If the queue to be deleted is not empty
	 * @throws UnspecifiedErrorException If the server encounter an error but refuses to pass details
	 */
	public boolean DeleteQueue(String queueName) 
			throws InvalidHeaderException, QueueInexistentException, QueueNotEmptyException, 
			UnspecifiedErrorException, IOException {
		final QueueDeleteLogRecord record = new QueueDeleteLogRecord(this, queueName);
		LOGGER.log(record);
		
		try {
			server.DeleteQueue(queueName);
			
			LOGGER.log(new QueueDeleteLogRecord(this, queueName, record.getMillis()));
			return true;
		} catch (QueueInexistentException e) {
			LOGGER.log(new QueueDeleteLogRecord(this, queueName, e, record.getMillis()));
			if (!suppressingErrors) throw e;
		} catch (QueueNotEmptyException e) {
			LOGGER.log(new QueueDeleteLogRecord(this, queueName, e, record.getMillis()));
			if (!suppressingErrors) throw e;
		} catch (UnspecifiedErrorException e) {
			LOGGER.log(new QueueDeleteLogRecord(this, queueName, e, record.getMillis()));
			if (!suppressingErrors) throw e;
		} catch (IOException e) {
			LOGGER.log(new QueueDeleteLogRecord(this, queueName, e, record.getMillis()));
			if (!suppressingErrors) throw e;
		} catch (InvalidHeaderException e) {
			LOGGER.log(new QueueDeleteLogRecord(this, queueName, e, record.getMillis()));
			if (!suppressingErrors) throw e;
		}
		return false;
	}
	
	/**
	 * Requests that a queue be created in the the system with the
	 * specified name.
	 * @param queueName The name of the queue to create
	 * @return True if the queue was created successfully, false otherwise.
	 * @throws QueueAlreadyExistsException If the queue already exists
	 * @throws IOException If an error occurred on the channel
	 * @throws InvalidHeaderException If the response was somehow corrupted
	 * @throws UnspecifiedErrorException If the server encounter an error but refuses to pass details
	 */
	public boolean CreateQueue(String queueName) 
			throws UnspecifiedErrorException, QueueAlreadyExistsException, IOException, InvalidHeaderException {
		final QueueCreateLogRecord record = new QueueCreateLogRecord(this, queueName);
		LOGGER.log(record);
		
		try {
			server.CreateQueue(queueName);
			
			LOGGER.log(new QueueCreateLogRecord(this, queueName, record.getMillis()));
			return true;
		} catch (UnspecifiedErrorException e) {
			LOGGER.log(new QueueCreateLogRecord(this, queueName, e, record.getMillis()));
			if (!suppressingErrors) throw e;
		} catch (QueueAlreadyExistsException e) {
			LOGGER.log(new QueueCreateLogRecord(this, queueName, e, record.getMillis()));
			if (!suppressingErrors) throw e;
		} catch (IOException e) {
			LOGGER.log(new QueueCreateLogRecord(this, queueName, e, record.getMillis()));
			if (!suppressingErrors) throw e;
		} catch (InvalidHeaderException e) {
			LOGGER.log(new QueueCreateLogRecord(this, queueName, e, record.getMillis()));
			if (!suppressingErrors) throw e;
		}
		return false;
	}

	/**
	 * Gets the queues that have messages waiting for the client.
	 * @return An Iterable containing the names of the queues with messages waiting
	 * for the client, or null if an error occurred or if there are no queues.
	 * @throws UnspecifiedErrorException If the server encounter an error but refuses to pass details
	 * @throws IOException If an error occurred on the channel
	 * @throws InvalidHeaderException If the response was somehow corrupted
	 */
	public Iterable<String> GetWaitingQueues() 
			throws UnspecifiedErrorException, IOException, InvalidHeaderException {
		final WaitingQueuesLogRecord record = new WaitingQueuesLogRecord(this);
		LOGGER.log(record);
		
		try {
			Iterable<String> result = server.GetWaitingQueues(this);
			
			LOGGER.log(new WaitingQueuesLogRecord(this, result, record.getMillis()));
			return result;
		} catch (UnspecifiedErrorException e) {
			LOGGER.log(new WaitingQueuesLogRecord(this, e, record.getMillis()));
			if (!suppressingErrors) throw e;
		} catch (IOException e) {
			LOGGER.log(new WaitingQueuesLogRecord(this, e, record.getMillis()));
			if (!suppressingErrors) throw e;
		} catch (InvalidHeaderException e) {
			LOGGER.log(new WaitingQueuesLogRecord(this, e, record.getMillis()));
			if (!suppressingErrors) throw e;
		}
		
		return null;
	}
	
	/**
	 * Retrieves a message from a queue
	 * @param queueName The name of the queue from which to retrieve the message
	 * @param andRemove Whether to delete the message after retrieving it.
	 * @return The message with the highest priority in that queue
	 * @throws IOException If an error occurred on the channel
	 * @throws QueueInexistentException If the filter specified was a queue and it does not exist
	 * @throws UnspecifiedErrorException If the server encounter an error but refuses to pass details
	 * @throws InvalidHeaderException If the response was somehow corrupted
	 */
	public Message ViewMessageFromQueue(String queueName, boolean andRemove) 
			throws QueueInexistentException, UnspecifiedErrorException, InvalidHeaderException, IOException {
		return ViewMessageFromQueue(queueName, andRemove, Order.PRIORITY);
	}
	
	/**
	 * Retrieves a message from a queue
	 * @param queueName The name of the queue from which to retrieve the message
	 * @param andRemove Whether to delete the message after retrieving it.
	 * @param orderedBy A value indicating how to determine which message to return. 
	 * @return The message that was selected 
	 * @throws IOException If an error occurred on the channel
	 * @throws QueueInexistentException If the filter specified was a queue and it does not exist
	 * @throws UnspecifiedErrorException If the server encounter an error but refuses to pass details
	 * @throws InvalidHeaderException If the response was somehow corrupted
	 */
	public Message ViewMessageFromQueue(String queueName, boolean andRemove, Order orderedBy) 
			throws InvalidHeaderException, IOException, QueueInexistentException, UnspecifiedErrorException {
		final GetMessageLogRecord record = new GetMessageLogRecord(this, Filter.QUEUE, queueName, orderedBy, andRemove);
		LOGGER.log(record);
		
		try {
			Message result = server.RetrieveMessage(queueName, Filter.QUEUE, orderedBy, andRemove);
			
			LOGGER.log(new GetMessageLogRecord(this, result, Filter.QUEUE, queueName, 
					orderedBy, andRemove, record.getMillis()));
			return result;
		} catch (QueueInexistentException e) {
			LOGGER.log(new GetMessageLogRecord(this, Filter.QUEUE, queueName, orderedBy, 
					andRemove, e, record.getMillis()));
			if (!suppressingErrors) throw e;
		} catch (ClientInexistentException e) {
			// Should never reach here
			e.printStackTrace();
		} catch (UnspecifiedErrorException e) {
			LOGGER.log(new GetMessageLogRecord(this, Filter.QUEUE, queueName, orderedBy, 
					andRemove, e, record.getMillis()));
			if (!suppressingErrors) throw e;
		} catch (IOException e) {
			LOGGER.log(new GetMessageLogRecord(this, Filter.QUEUE, queueName, orderedBy, 
					andRemove, e, record.getMillis()));
			if (!suppressingErrors) throw e;
		} catch (InvalidHeaderException e) {
			LOGGER.log(new GetMessageLogRecord(this, Filter.QUEUE, queueName, orderedBy, 
					andRemove, e, record.getMillis()));
			if (!suppressingErrors) throw e;
		}
		
		return null;
	}
	
	/**
	 * Retrieves a message to be viewed from a particular sender.
	 * @param senderName The name of the sender
	 * @param andRemove Whether to delete the message after retrieving it.
	 * @return The message with the highest priority from the sender or null if there is no message
	 * @throws IOException If an error occurred on the channel
	 * @throws ClientInexistentException If the filter specified was a sender and it does not exist
	 * @throws UnspecifiedErrorException If the server encounter an error but refuses to pass details
	 * @throws InvalidHeaderException If the response was somehow corrupted
	 */
	public Message ViewMessageFromSender(String senderName, boolean andRemove) 
			throws ClientInexistentException, UnspecifiedErrorException, IOException, InvalidHeaderException {
		return ViewMessageFromSender(senderName, andRemove, Order.PRIORITY);
	}
	
	/**
	 * Retrieves a message to be viewed from a particular sender.
	 * @param senderName The name of the sender
	 * @param andRemove Whether to delete the message after retrieving it.
	 * @param orderedBy A value indicating how to determine which message to return.
	 * @return The message that was selected or null if no message was retrieved
	 * @throws IOException If an error occurred on the channel
	 * @throws ClientInexistentException If the filter specified was a sender and it does not exist
	 * @throws UnspecifiedErrorException If the server encounter an error but refuses to pass details
	 * @throws InvalidHeaderException If the response was somehow corrupted
	 */
	public Message ViewMessageFromSender(String senderName, boolean andRemove, Order orderedBy) 
			throws ClientInexistentException, UnspecifiedErrorException, IOException, InvalidHeaderException {
				
		final GetMessageLogRecord record = new GetMessageLogRecord(this, Filter.SENDER, senderName, orderedBy, andRemove);
		LOGGER.log(record);
		
		try {
			Message result = server.RetrieveMessage(senderName, Filter.SENDER, orderedBy, andRemove);

			LOGGER.log(new GetMessageLogRecord(this, result, Filter.SENDER, senderName, 
					orderedBy, andRemove, record.getMillis()));
			return result;
		} catch (QueueInexistentException e) {
			// Should never reach here
			e.printStackTrace();
		} catch (ClientInexistentException e) {
			LOGGER.log(new GetMessageLogRecord(this, Filter.SENDER, senderName, orderedBy, 
					andRemove, e, record.getMillis()));
			if (!suppressingErrors) throw e;
		} catch (UnspecifiedErrorException e) {
			LOGGER.log(new GetMessageLogRecord(this, Filter.SENDER, senderName, orderedBy, 
					andRemove, e, record.getMillis()));
			if (!suppressingErrors) throw e;
		} catch (IOException e) {
			LOGGER.log(new GetMessageLogRecord(this, Filter.SENDER, senderName, orderedBy, 
					andRemove, e, record.getMillis()));
			if (!suppressingErrors) throw e;
		} catch (InvalidHeaderException e) {
			LOGGER.log(new GetMessageLogRecord(this, Filter.SENDER, senderName, orderedBy, 
					andRemove, e, record.getMillis()));
			if (!suppressingErrors) throw e;
		}

		return null;
	}
	
	/**
	 * Gets the server that the client is interacting with
	 * @return The server that the client is interacting with
	 */
	public ServerRPC getServer() {
		return server;
	}
}
