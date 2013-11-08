package org.ftab.logging.client;

import java.util.logging.Level;

import org.ftab.client.Client;
import org.ftab.client.Message;
import org.ftab.client.exceptions.ClientInexistentException;
import org.ftab.client.exceptions.QueueInexistentException;
import org.ftab.logging.SystemEvent;
import org.ftab.pubenums.Filter;
import org.ftab.pubenums.Order;

/**
 * Log record for message retrieval attempts
 * @author Jean-Pierre
 *
 */
@SuppressWarnings("serial")
public class GetMessageLogRecord extends ClientLogRecord {
	/**
	 * Flag indicating whether the record is the start or end of an attempt
	 */
	private final boolean isAttemptStart;
	
	/**
	 * The sender name or queue name being filtered by
	 */
	private final String value;
	
	/**
	 * The type of the filter being used
	 */
	private final Filter filterBy;
	
	/**
	 * The type of prioritizing being used
	 */
	private final Order orderBy;
	
	/**
	 * Flag indicating whether the message was to be deleted after retrieval
	 */
	private final boolean isDelete;
	
	/**
	 * The message that was retrieved
	 */
	private final Message retrievedMessage;
	
	/**
	 * Creates a new log record for the attempt to retrieve a message
	 * @param client The client attempting to retrieve the message
	 * @param filter The filter being used to retrieve the message
	 * @param value The value of the filter being used
	 * @param order The order being prioritised
	 * @param andDelete Whether to delete the message afterwards
	 */
	public GetMessageLogRecord(Client client, Filter filter, String value, Order order, boolean andDelete) {
		super(Level.FINE, client, SystemEvent.RETRIEVE_MESSAGE, 
				String.format("Attempting to %s a message by %s:%s when sorted by %s.", 
						andDelete ? "POP" : "PEEK AT", filter.name(), value, order.name())); 
				
		this.value = value;
		this.filterBy = filter;
		this.orderBy = order;
		this.isDelete = andDelete;
		retrievedMessage = null;
		
		this.isAttemptStart = true;
	}
	
	/**
	 * Creates a new record to log the successful retrieval of a message
	 * @param client The client that retrieved the message
	 * @param msg The message that was retrieved
	 * @param filter The filter the attempt used
	 * @param value The value of the filter that was being used
	 * @param order The order that was being prioritized
	 * @param andDelete Whether the message was to be deleted after being retrieved
	 * @param requestStart The record that logged the start of the attempt
	 */
	public GetMessageLogRecord(Client client, Message msg, Filter filter, String value, Order order, 
			boolean andDelete, GetMessageLogRecord requestStart) {
		super(Level.FINE, client, SystemEvent.RETRIEVE_MESSAGE, "", requestStart); 
		
		this.setMessage(String.format("%s successfully %s after %d milliseconds.", 
				msg != null ? msg.getSummary() : "There was no message to be", andDelete ? "POPPED" : "PEEKED", this.getChainElapsedTime()));
		
		this.value = value;
		this.filterBy = filter;
		this.orderBy = order;
		this.isDelete = andDelete;
		this.retrievedMessage = msg;
		
		this.isAttemptStart = false;
	}

	/**
	 * Creates a new record to log the unsuccessful retrieval of a message
	 * @param client The client that attempted to retrieve the message
	 * @param filter The filter the attempt was using
	 * @param value The value of the filter that was being used
	 * @param order The order that was being prioritized
	 * @param andDelete Whether the message was to be deleted after being retrieved
	 * @param thrown The exception that was thrown in the attempt
	 * @param requestStart The record that logged the start of the attempt
	 */
	public GetMessageLogRecord(Client client, Filter filter, String value, Order order, boolean andDelete, 
			Throwable thrown, GetMessageLogRecord requestStart) {
		super(Level.WARNING, client, SystemEvent.RETRIEVE_MESSAGE, "", requestStart);				
		
		/*
		 * If the exception thrown was not one of the expected errors then
		 * mark this log as severe
		 */
		if (thrown.getClass() != QueueInexistentException.class && 
				thrown.getClass() != ClientInexistentException.class) {
			this.setLevel(Level.SEVERE);
		}
		
		this.setMessage(String.format("The retrieval of the message failed after %d milliseconds, reason: %s", 
				this.getChainElapsedTime(), thrown.getMessage()));
				
		this.value = value;
		this.filterBy = filter;
		this.orderBy = order;
		this.isDelete = andDelete;
		this.retrievedMessage = null;
		
		this.isAttemptStart = false;
	}
	
	/**
	 * Gets whether the record represents the start of an attempt
	 * @return True if it is a start, false otherwise
	 */
	public boolean isAttemptStart() {
		return isAttemptStart;
	}
	
	/**
	 * Gets the filter that wasused to retrieve the message 
	 * @return The filter that was used
	 */
	public Filter getFilterBy() {
		return filterBy;
	}

	/**
	 * Gets the order on which to retrieve the message
	 * @return The order on which to retrieve the message
	 */
	public Order getOrderBy() {
		return orderBy;
	}
	
	/**
	 * Gets the value of the filter
	 * @return The value used for the filter
	 */
	public String getValue() {
		return value;
	}
	
	/**
	 * Gets the message that was retrieved
	 * @return The message that was retrieved
	 */
	public Message getRetrievedMessage() {
		return retrievedMessage;
	}
	
	/**
	 * Gets whether the message was to be deleted afterwards
	 * @return True if the message was to be deleted, false otherwise
	 */
	public boolean isDelete() {
		return isDelete;
	}
}
