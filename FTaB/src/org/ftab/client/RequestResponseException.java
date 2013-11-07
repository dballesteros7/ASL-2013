package org.ftab.client;

import org.ftab.communication.responses.RequestResponse.Status;

@SuppressWarnings("serial")
public class RequestResponseException extends Exception {
	/**
	 * The error status associated with this exception
	 */
	private final Status errorStatus;	
	
	/**
	 * Creates an exception representing a failure state of a 
	 * request response. 
	 * @param status The associated Status value.
	 */
	public RequestResponseException(Status status) {
		this(null, status);
	}

	/**
	 * Creates an exception representing a failure state of a 
	 * request response.
	 * @param message The error message to be attached to the
	 * exception.
	 * @param status The associated Status value. 
	 */
	public RequestResponseException(String message, Status status) {
		super(message);
		errorStatus = status;
	}

	/**
	 * Gets the request response status associated with this exception.
	 * @return A Status enumerated value represented by this exception.
	 */
	public Status getErrorStatus() {
		return errorStatus;
	}
}
