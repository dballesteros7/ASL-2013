package org.ftab.logging.client;

import java.util.logging.Level;

import org.ftab.client.Client;
import org.ftab.client.exceptions.QueueAlreadyExistsException;
import org.ftab.logging.SystemEvent;

/**
 * Log record for queue creation attempts
 * @author Jean-Pierre
 *
 */
@SuppressWarnings("serial")
public class QueueCreateLogRecord extends ClientLogRecord {
	/**
	 * Flag indicating whether the record is the start or end of an attempt
	 */
	private final boolean isAttemptStart;
	
	/**
	 * The name of the queue that was being modified
	 */
	private final String queueName;
	
	/**
	 * Creates a new record for the start of an attempt to create a queue
	 * @param client The client attempting to create the queue
	 * @param queueName The name of the queue being created
	 */
	public QueueCreateLogRecord(Client client, String queueName) {
		super(Level.FINE, client, SystemEvent.QUEUE_CREATION, 
				String.format("Attempting to create queue %s.", queueName));
		
		this.queueName = queueName;
		this.isAttemptStart = true;
	}
	
	/**
	 * Creates a new record for the end of a successful queue creation
	 * @param client The client that attempted to create the queue
	 * @param queueName The name of the queue that was created
	 * @param millis The time since the start of the attempt
	 */
	public QueueCreateLogRecord(Client client, String queueName, long millis) {
		super(Level.FINE, client, millis, SystemEvent.QUEUE_CREATION, ""); 
		
		this.setMessage(String.format("Queue %s successfully created after %d milliseconds.", 
				queueName, this.getElapsedTime()));
		
		this.queueName = queueName;
		this.isAttemptStart = false;
	}

	/**
	 * Creates a new record for the unsuccessful queue creation
	 * @param client The client that attempted to create the queue
	 * @param queueName The name of the queue that was to be created
	 * @param thrown The exception thrown on the attempt
	 * @param millis The time since the start of the attempt
	 */
	public QueueCreateLogRecord(Client client, String queueName, Throwable thrown, long millis) {
		super(Level.WARNING, client, millis, SystemEvent.QUEUE_CREATION, "");				
		
		/*
		 * If the exception thrown was not one of the expected errors then
		 * mark this log as severe
		 */
		if (thrown.getClass() != QueueAlreadyExistsException.class) {
			this.setLevel(Level.SEVERE);
		}
		
		this.setMessage(String.format("The creation of queue %s failed after %d milliseconds, reason: %s", 
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
