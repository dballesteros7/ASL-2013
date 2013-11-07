package org.ftab.client.exceptions;

/**
 * An exception that is thrown when the client requests a set of queues that
 * there are no queues for.
 * @author Jean-Pierre Smith
 */
@SuppressWarnings("serial")
public class NoSuchQueuesException extends FTaBServerException {
	
	/**
	 * Create a new execption specifying that there are no queues meeting
	 * the request.
	 */
	public NoSuchQueuesException() {
		super("There are no queues that have messages waiting.");
	}

}
