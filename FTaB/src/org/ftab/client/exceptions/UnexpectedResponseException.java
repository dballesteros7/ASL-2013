package org.ftab.client.exceptions;

/**
 * A runtime exception thrown when the server returns a message that
 * was not accounted for.
 * @author Jean-Pierre Smith
 */
@SuppressWarnings("serial")
public class UnexpectedResponseException extends RuntimeException {
	/**
	 * Creates a new exception with the default message.s
	 */
	public UnexpectedResponseException() {
		super("The server returned an unexpected response.");
	}
}
