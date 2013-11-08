package org.ftab.logging.client;

import java.util.logging.Level;

import org.ftab.client.Client;
import org.ftab.logging.SystemEvent;

/**
 * Log record for attempts to fetch waiting queues
 * @author Jean-Pierre
 *
 */
@SuppressWarnings("serial")
public class WaitingQueuesLogRecord extends ClientLogRecord {
	/**
	 * Flag indicating whether the record is the start or end of an attempt
	 */
	private final boolean isAttemptStart;
	
	/**
	 * Creates a new record for the start of an attempt to fetch waiting queues
	 * @param client The client attempting to delete the queue
	 */
	public WaitingQueuesLogRecord(Client client) {
		super(Level.FINE, client, SystemEvent.FETCH_WAITING_QUEUES, "Attempting to fetch waiting queues.");
		
		this.isAttemptStart = true;
	}
	
	/**
	 * Creates a new record for the end of a successful fetch
	 * @param client The client that requested the fetch
	 * @param startRecord The record logging the start of the attempt
	 */
	public WaitingQueuesLogRecord(Client client, Iterable<String> queues, WaitingQueuesLogRecord startRecord) {
		super(Level.FINE, client, SystemEvent.FETCH_WAITING_QUEUES, "", startRecord); 
		
		/* Count the number of queues returned */
		int count = 0;
		for (@SuppressWarnings("unused") String name : queues) count++; 
		
		this.setMessage(String.format("Successfully received the names of %d queues after %d milliseconds.", 
				count, this.getChainElapsedTime()));
		
		this.isAttemptStart = false;
	}

	/**
	 * Creates a new record for the unsuccessful fetch
	 * @param client The client that attempted to fetch the names of the queues
	 * @param thrown The exception thrown on the attempt
	 * @param startRecord The record logging the start of the attempt
	 */
	public WaitingQueuesLogRecord(Client client, Throwable thrown, WaitingQueuesLogRecord startRecord) {
		super(Level.SEVERE, client, SystemEvent.FETCH_WAITING_QUEUES, "", startRecord);				
				
		this.setMessage(String.format("The fetch failed after %d milliseconds, reason: %s", 
				this.getChainElapsedTime(), thrown.getMessage()));
				
		this.isAttemptStart = false;
	}
	
	/**
	 * Gets whether the record represents the start of an attempt
	 * @return True if it is a start, false otherwise
	 */
	public boolean isAttemptStart() {
		return isAttemptStart;
	}
}
