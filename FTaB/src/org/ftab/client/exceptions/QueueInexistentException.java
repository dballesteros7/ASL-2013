package org.ftab.client.exceptions;

/**
 * The exception that is thrown by the server when a queue name
 * specified in a call does not exist in the system.
 * @author Jean-Pierre Smith
 * @aslexcludemethods
 */
@SuppressWarnings("serial")
public class QueueInexistentException extends FTaBServerException {
	/**
	 * The name of the queue that was requested but does not exist
	 */
	private final String queueName;
	
	/**
	 * Creates a new exception that the specified queue does not exist.
	 * @param queueName The name of the queue that does not exist.
	 */
	public QueueInexistentException(String queueName) {
		super(String.format("The queue %s does not exist.", queueName));
		
		this.queueName = queueName;
	}
	
	/**
	 * Creates a new exception that a requested queue does not exist.
	 */
	public QueueInexistentException() {
		super("The queue that was specified does not exist");
		
		this.queueName = null;
	}
	
	/**
	 * Gets the name of the queue that does not exist. 
	 * @return The name of the queue if it was set or null
	 * if the error was thrown without specifying the name of
	 * the queue.
	 * @aslexclude
	 */
	public String getQueueName() {
		return queueName;
	}
}
