package org.ftab.logging.client;

import java.util.logging.Level;

import org.ftab.client.Client;
import org.ftab.client.exceptions.QueueAEException;
import org.ftab.client.exceptions.QueueInexistentException;
import org.ftab.logging.SystemEvent;

/**
 * Log record for queue deletion attempts
 * @author Jean-Pierre
 *
 */
@SuppressWarnings("serial")
public class QueueDeleteLogRecord extends ClientLogRecord {
	/**
	 * Flag indicating whether the record is the start or end of an attempt
	 */
	private final boolean isAttemptStart;
	
	/**
	 * The name of the queue that was being modified
	 */
	private final String queueName;
	
	/**
	 * Creates a new record for the start of an attempt to delete a queue
	 * @param client The client attempting to delete the queue
	 * @param queueName The name of the queue being deleted
	 */
	public QueueDeleteLogRecord(Client client, String queueName) {
		super(Level.FINE, client, SystemEvent.QUEUE_DELETION, 
				String.format("Attempting to delete queue %s.", queueName));
		
		this.queueName = queueName;
		this.isAttemptStart = true;
	}
	
	/**
	 * Creates a new record for the end of a successful queue deletion
	 * @param client The client that attempted to delete the queue
	 * @param queueName The name of the queue that was deleted
	 * @param millis The time since the start of the attempt
	 */
	public QueueDeleteLogRecord(Client client, String queueName, long millis) {
		super(Level.FINE, client, millis, SystemEvent.QUEUE_DELETION, ""); 
		
		this.setMessage(String.format("Queue %s successfully deleted after %d milliseconds.", 
				queueName, this.getElapsedTime()));
		
		this.queueName = queueName;
		this.isAttemptStart = false;
	}

	/**
	 * Creates a new record for the unsuccessful queue deletion
	 * @param client The client that attempted to delete the queue
	 * @param queueName The name of the queue that was to be deleted
	 * @param thrown The exception thrown on the attempt
	 * @param millis The time since the start of the attempt
	 */
	public QueueDeleteLogRecord(Client client, String queueName, Throwable thrown, long millis) {
		super(Level.WARNING, client, millis, SystemEvent.QUEUE_DELETION, "");				
		
		/*
		 * If the exception thrown was not one of the expected errors then
		 * mark this log as severe
		 */
		if (thrown.getClass() != QueueInexistentException.class && 
				thrown.getClass() != QueueAEException.class) {
			this.setLevel(Level.SEVERE);
		}
		
		this.setMessage(String.format("The deletion of queue %s failed after %d milliseconds, reason: %s", 
				queueName, this.getElapsedTime(), thrown.getMessage()));
				
		this.queueName = queueName;
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
	 * Gets the name of the queue that was being created
	 * @return The name of the queue
	 */
	public String getQueueName() {
		return queueName;
	}
	
}
