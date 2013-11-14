package org.ftab.client.exceptions;

/**
 * The exception that is thrown when a queue in the system already has 
 * the name specified to be used to create a new queue.
 * @author Jean-Pierre Smith
 * @aslexcludemethods
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
	 * Gets the queue name that already exists in the system 
	 * @return The name of the queue or null if it already exists.
	 * @aslexclude
	 */
	public String getQueueName() {
		return queueName;
	}

}
