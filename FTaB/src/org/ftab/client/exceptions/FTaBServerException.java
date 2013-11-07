package org.ftab.client.exceptions;

/**
 * General class for an exception thrown by the server.
 * @author Jean-Pierre Smith
 *
 */
@SuppressWarnings("serial")
public abstract class FTaBServerException extends Exception {

	public FTaBServerException() {	}

	public FTaBServerException(String message) {
		super(message);
	}

	public FTaBServerException(Throwable cause) {
		super(cause);
	}

	public FTaBServerException(String message, Throwable cause) {
		super(message, cause);
	}

}
