package org.ftab.client.exceptions;

/**
 * An exception that is thrown when a queue is attempted to be created but
 * already exists.
 * @author Jean-Pierre Smith
 */
@SuppressWarnings("serial")
public class QueueAEException extends FTaBServerException {
	/**
	 * The name of the queue that already exists
	 */
	private final String queueName;
	
	/**
	 * Creates a new exception for the specified queue.
	 * @param queueName The name of the queue that could not be created due to already
	 * existing.
	 */
	public QueueAEException(String queueName) {
		super(String.format("Could not create the queue - %s because it already exists.", 
				queueName));
		
		this.queueName = queueName;
	}
	
	/**
	 * Creates a new exception specifying that the queue already exists.
	 */
	public QueueAEException() {
		super("Could not create the queue because it already exists.");
		
		this.queueName = null;
	}
	
	/**
	 * Gets the name of the queue which already exists 
	 * @return The name of the queue or null if it already exists.
	 */
	public String getQueueName() {
		return queueName;
	}

}
