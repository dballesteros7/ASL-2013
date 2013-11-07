package org.ftab.client.exceptions;

/**
 * An exception that is thrown by the server when it does not want to specify
 * what went wrong.
 * @author Jean-Pierre Smith
 */
@SuppressWarnings("serial")
public class UnspecifiedErrorException extends FTaBServerException {

	/**
	 * Creates a new exception with the generic message.
	 */
	public UnspecifiedErrorException() {
		super("The request failed due to an unkown reason.");
	}
}
