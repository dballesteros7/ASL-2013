package org.ftab.client.exceptions;

/**
 * Exception that is thrown when a queue was attempted to be deleted
 * but is not empty of messages.
 * @author Jean-Pierre Smith
 */
@SuppressWarnings("serial")
public class QueueNotEmptyException extends FTaBServerException {
	
	/**
	 * The name of the queue which was attempted to be deleted
	 */
	private final String queueName;
	
	/**
	 * Creates a new exception specifying that the named queue could not be deleted
	 * because it is not empty
	 * @param queueName The name of the queue which was attempted to be deleted
	 */
	public QueueNotEmptyException(String queueName) {
		super(String.format("The queue %s can not be deleted because it is not empty.", queueName));
		
		this.queueName = queueName;
	}
	
	/**
	 * Creates a new exception specifying that a queue could not be deleted
	 * because it is not empty
	 */
	public QueueNotEmptyException() {
		super("The queue can not be deleted because it is not empty.");
		
		this.queueName = null;
	}

	/**
	 * Gets the name of the queue that was attempted to be deleted.
	 * @return The name of the queue or null if it was not specified.
	 */
	public String getQueueName() {
		return queueName;
	}

}
