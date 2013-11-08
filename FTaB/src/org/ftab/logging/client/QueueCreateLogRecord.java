package org.ftab.logging.client;

import java.util.logging.Level;

import org.ftab.client.Client;
import org.ftab.client.exceptions.QueueAEException;
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
	 * @param startRecord The record logging the start of the attempt
	 */
	public QueueCreateLogRecord(Client client, String queueName, QueueCreateLogRecord startRecord) {
		super(Level.FINE, client, SystemEvent.QUEUE_CREATION, "", startRecord); 
		
		this.setMessage(String.format("Queue %s successfully created after %d milliseconds.", 
				queueName, this.getChainElapsedTime()));
		
		this.queueName = queueName;
		this.isAttemptStart = false;
	}

	/**
	 * Creates a new record for the unsuccessful queue creation
	 * @param client The client that attempted to create the queue
	 * @param queueName The name of the queue that was to be created
	 * @param thrown The exception thrown on the attempt
	 * @param startRecord The record logging the start of the attempt
	 */
	public QueueCreateLogRecord(Client client, String queueName, Throwable thrown, QueueCreateLogRecord startRecord) {
		super(Level.WARNING, client, SystemEvent.QUEUE_CREATION, "", startRecord);				
		
		/*
		 * If the exception thrown was not one of the expected errors then
		 * mark this log as severe
		 */
		if (thrown.getClass() != QueueAEException.class) {
			this.setLevel(Level.SEVERE);
		}
		
		this.setMessage(String.format("The creation of queue %s failed after %d milliseconds, reason: %s", 
				queueName, this.getChainElapsedTime(), thrown.getMessage()));
				
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
