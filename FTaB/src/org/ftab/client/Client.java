package org.ftab.client;

import java.io.IOException;
import java.util.logging.Logger;

import org.ftab.client.exceptions.AlreadyOnlineException;
import org.ftab.client.exceptions.ClientInexistentException;
import org.ftab.client.exceptions.FullServerException;
import org.ftab.client.exceptions.QueueAEException;
import org.ftab.client.exceptions.QueueInexistentException;
import org.ftab.client.exceptions.QueueNotEmptyException;
import org.ftab.client.exceptions.UnexpectedResponseException;
import org.ftab.client.exceptions.UnspecifiedErrorException;
import org.ftab.client.serverrpc.ServerRPC;
import org.ftab.communication.exceptions.InvalidHeaderException;
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
 * Clients represent entities that communicate through the messaging system
 * by sending and receiving messages to and from queues. 
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
	 * @param clientName The name to assign this client.
	 */
	public Client(String clientName) {
		this.username = clientName;
		this.suppressingErrors = true;
	}
	
	/**
	 * Gets the username assigned to this client at creation.
	 * @return The assigned username
	 * @aslexclude
	 */
	public String getUsername() {
		return username;
	}
	
	/**
	 * Gets whether errors are being suppressed.
	 * @return True if errors will not be thrown by the methods, false otherwise
	 * @aslexclude
	 */
	public boolean isSuppressingErrors() {
		return suppressingErrors;
	}
	
	/**
	 * Sets whether exceptions should be suppressed by the client
	 * @param suppressingErrors True to suppress exceptions from being thrown, false otherwise.
	 * @aslexclude
	 */
	public void setSuppressingErrors(boolean suppressingErrors) {
		this.suppressingErrors = suppressingErrors;
	}
	
	/**
	 * Ascertains whether the client is connected to 
	 * a server or not
	 * @return True if the client is connected, false otherwise.
	 * @aslexclude
	 */
	public boolean isConnected() {
		if (server == null) return false;
		else return server.isConnectionOpen();
	}
	
	/**
	 * Initiates a connection from this client to a middleware server.
	 * @param serverIPv4 The IPv4 string representation or fully qualified domain name of 
	 * the server the machine the middleware resides on.
	 * @param port The port on the remote machine that the middleware is listening on.
	 * @return <b>true</b> if the connection was successful, <b>false</b> otherwise
	 * @throws FullServerException If the server is not accepting any more connections 
	 * due to full capacity.
	 * @throws AlreadyOnlineException If the client is marked in the system as being online.
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

			LOGGER.log(new ClientConnectionRecord(this, record));
			return true;
		} catch (IOException | InvalidHeaderException | FullServerException | 
				 AlreadyOnlineException | UnspecifiedErrorException e) {
			LOGGER.log(new ClientConnectionRecord(this, e, record));
			if (!suppressingErrors) throw e;
		} 
		
		return false;
	}
	
	/**
	 * Disconnects the client from the server it is connected to.
	 * @return <b>true</b> if the disconnection was successful, <b>false</b> otherwise.
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
		
			LOGGER.log(new ClientDisconRecord(this, record));
			return true;
		} catch (IOException | UnspecifiedErrorException e) {
			LOGGER.log(new ClientDisconRecord(this, e, record));
			if (!suppressingErrors) throw e;
		} catch (InvalidHeaderException e) {
			LOGGER.log(new ClientDisconRecord(this, e, record));
			if (!suppressingErrors) throw e;
			
			// Assume the attempt was successful
			return true;
		}
		
		return false;
	}

	/**
	 * Sends a message from this client to one or more queues with a designated 
	 * receiver for the message. 
	 * @param message The text of the message to be sent.
	 * @param priority The priority of the message, from 1 to 10, with 1 being the
	 * lowest and 10 the highest.
	 * @param context The context of the message
	 * @param receiver The username of the designated receiver of the message
	 * @param queues The names of the queue or queues to which to send the message
	 * @return <b>true</b> if the message was sent successfully, <b>false</b> otherwise
	 * @throws QueueInexistentException If one or more of the queues specified does not
	 * exist in the system
	 * @throws ClientInexistentException If the specified receiver does not exist in the
	 * system
	 * @throws UnspecifiedErrorException If the server encounter an error but refuses to pass details
	 * @throws IOException If an error occurred with the channel
	 * @throws InvalidHeaderException If the input data was somehow corrupted
	 */
	public boolean SendMessage(String message, byte priority, int context, String receiver, String... queues) 
			throws QueueInexistentException, UnspecifiedErrorException, ClientInexistentException, IOException, InvalidHeaderException {
		
		final Message msg = new Message(context, priority, message, this.getUsername(), receiver, queues);
		
		/* Log the start of the attempt */
		final SendMsgLogRecord record = new SendMsgLogRecord(this, msg);
		LOGGER.log(record);
		
		try {
			server.PushMessage(msg);
			
			LOGGER.log(new SendMsgLogRecord(this, msg, record));
			return true;
		
			/* Log the failed attempt */
		} catch (QueueInexistentException | ClientInexistentException | UnspecifiedErrorException 
				| IOException | InvalidHeaderException e) {
			LOGGER.log(new SendMsgLogRecord(this, msg, e, record));
			if (!suppressingErrors) throw e;
		} 
		
		return false;
	}
	
	/**
	 * Sends a message from this client to one or more queues.
	 * @param message The text of the message to be sent.
	 * @param priority The priority of the message, from 1 to 10, with 1 being the
	 * lowest and 10 the highest.
	 * @param context The context of the message
	 * @param queues The names of the queue or queues to which to send the message
	 * @return <b>true</b> if the message was sent successfully, <b>false</b> otherwise
	 * @throws QueueInexistentException If one or more of the queues specified does not
	 * exist in the system
	 * @throws UnspecifiedErrorException If the server encounter an error but refuses to pass details
	 * @throws IOException If an error occurred with the channel
	 * @throws InvalidHeaderException If the input data was somehow corrupted
	 */
	public boolean SendMessage(String message, byte priority, int context, String... queues) 
			throws QueueInexistentException, UnspecifiedErrorException, IOException, InvalidHeaderException {
		try {
			return this.SendMessage(message, priority, context, null, queues);
		} catch (ClientInexistentException e) {
			throw new UnexpectedResponseException();
		}
	}
	
	/**
	 * Requests that the named queue be deleted from the queues in the system.
	 * @param queueName The name of the queue to be deleted.
	 * @return <b>true</b> if the queue was deleted, <b>false</b> otherwise.
	 * @throws IOException If an error occurred with the channel
	 * @throws InvalidHeaderException If the input data was somehow corrupted
	 * @throws QueueInexistentException If the queue to be deleted does not exist in the system.
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
			
			LOGGER.log(new QueueDeleteLogRecord(this, queueName, record));
			return true;
		} catch (QueueInexistentException | QueueNotEmptyException | UnspecifiedErrorException 
				| IOException | InvalidHeaderException e) {
			LOGGER.log(new QueueDeleteLogRecord(this, queueName, e, record));
			if (!suppressingErrors) throw e;
		} 
		return false;
	}
	
	/**
	 * Requests that a new queue be created in the the system with the specified name.
	 * @param queueName The name to assign the newly created queue.
	 * @return <b>true</b> if the queue was created successfully, <b>false</b> otherwise.
	 * @throws QueueAEException If a queue with the name specified already exists in the
	 * system
	 * @throws IOException If an error occurred on the channel
	 * @throws InvalidHeaderException If the response was somehow corrupted
	 * @throws UnspecifiedErrorException If the server encounter an error but refuses to pass details
	 */
	public boolean CreateQueue(String queueName) 
			throws UnspecifiedErrorException, QueueAEException, IOException, InvalidHeaderException {
		final QueueCreateLogRecord record = new QueueCreateLogRecord(this, queueName);
		LOGGER.log(record);
		
		try {
			server.CreateQueue(queueName);
			
			LOGGER.log(new QueueCreateLogRecord(this, queueName, record));
			return true;
		} catch (UnspecifiedErrorException | QueueAEException | IOException | InvalidHeaderException e) {
			LOGGER.log(new QueueCreateLogRecord(this, queueName, e, record));
			if (!suppressingErrors) throw e;
		} 
		return false;
	}

	/**
	 * Gets the names of the queues that have messages waiting for this client.
	 * @return An Iterable containing the names of the queues with messages waiting
	 * for this client, or null if there are no such queues or the operation did not
	 * complete successfully.
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
			
			LOGGER.log(new WaitingQueuesLogRecord(this, result, record));
			return result;
		} catch (UnspecifiedErrorException | IOException | InvalidHeaderException e) {
			LOGGER.log(new WaitingQueuesLogRecord(this, e, record));
			if (!suppressingErrors) throw e;
		}
		
		return null;
	}
	
	/**
	 * Retrieves the message with the highest priority from a queue and optionally 
	 * removes the message from the queue.
	 * @param queueName The name of the queue from which to retrieve the message
	 * @param andRemove <b>true</b> to delete the message after retrieving it, 
	 * <b>false</b> to leave the message on the queue.
	 * @return The message with the highest priority in the specified queue
	 * @throws IOException If an error occurred on the channel
	 * @throws QueueInexistentException If the specified was queue does not exist in
	 * the system.
	 * @throws UnspecifiedErrorException If the server encounter an error but refuses to pass details
	 * @throws InvalidHeaderException If the response was somehow corrupted
	 */
	public Message ViewMessageFromQueue(String queueName, boolean andRemove) 
			throws QueueInexistentException, UnspecifiedErrorException, InvalidHeaderException, IOException {
		return ViewMessageFromQueue(queueName, andRemove, Order.PRIORITY);
	}
	
	/**
	 * Retrieves a message from a queue and optionally removes the message from 
	 * that queue.
	 * @param queueName The name of the queue from which to retrieve the message
	 * @param andRemove <b>true</b> to delete the message after retrieving it, 
	 * <b>false</b> to leave the message on the queue.
	 * @param orderedBy An enumerated value indicating whether to retrieve the
	 * message with the earliest time-stamp or with the highest priority. 
	 * @return A message from the specified queue retrieved by either highest priority
	 * or earliest time-stamp. 
	 * @throws IOException If an error occurred on the channel
	 * @throws QueueInexistentException If the specified was queue does not exist in
	 * the system.
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
					orderedBy, andRemove, record));
			return result;
		} catch (QueueInexistentException e) {
			LOGGER.log(new GetMessageLogRecord(this, Filter.QUEUE, queueName, orderedBy, 
					andRemove, e, record));
			if (!suppressingErrors) throw e;
		} catch (ClientInexistentException e) {
			// Should never reach here
			e.printStackTrace();
		} catch (UnspecifiedErrorException | IOException | InvalidHeaderException e) {
			LOGGER.log(new GetMessageLogRecord(this, Filter.QUEUE, queueName, orderedBy, 
					andRemove, e, record));
			if (!suppressingErrors) throw e;
		} 
		
		return null;
	}
	
	/**
	 * Retrieves the message with the highest priority from a particular sender and optionally
	 * removes the message from the queue it was found on.
	 * @param senderName The name of the sender from whom the message is to be.
	 * @param andRemove <b>true</b> to delete the message after retrieving it, 
	 * <b>false</b> to leave the message on the queue.
	 * @return The message with the highest priority from the sender or null if there was 
	 * no message available from that sender.
	 * @throws IOException If an error occurred on the channel
	 * @throws ClientInexistentException If the specified sender does not exist in 
	 * the system
	 * @throws UnspecifiedErrorException If the server encounter an error but refuses to pass details
	 * @throws InvalidHeaderException If the response was somehow corrupted
	 */
	public Message ViewMessageFromSender(String senderName, boolean andRemove) 
			throws ClientInexistentException, UnspecifiedErrorException, IOException, InvalidHeaderException {
		return ViewMessageFromSender(senderName, andRemove, Order.PRIORITY);
	}
	
	/**
	 * Retrieves a message from a particular sender and optionally removes the 
	 * message from the queue it was found on.
	 * @param senderName The name of the sender from whom the message is to be
	 * @param andRemove <b>true</b> to delete the message after retrieving it, 
	 * <b>false</b> to leave the message on the queue.
	 * @param orderedBy An enumerated value indicating whether to retrieve the
	 * message with the earliest time-stamp or with the highest priority. 
	 * @return The message that was selected or null if there was no message available
	 * from the sender.
	 * @throws IOException If an error occurred on the channel
	 * @throws ClientInexistentException If the specified sender does not exist in the
	 * system.
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
					orderedBy, andRemove, record));
			return result;
		} catch (QueueInexistentException e) {
			// Should never reach here
			e.printStackTrace();
		} catch (ClientInexistentException | UnspecifiedErrorException | IOException | InvalidHeaderException e) {
			LOGGER.log(new GetMessageLogRecord(this, Filter.SENDER, senderName, orderedBy, 
					andRemove, e, record));
			if (!suppressingErrors) throw e;
		} 

		return null;
	}
	
	/**
	 * Gets the server that the client is interacting with
	 * @return The server that the client is interacting with
	 * @aslexclude
	 */
	public ServerRPC getServer() {
		return server;
	}
}
