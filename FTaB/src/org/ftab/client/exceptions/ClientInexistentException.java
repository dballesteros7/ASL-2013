package org.ftab.client.exceptions;

/**
 * The exception that is thrown by the server when a username specified 
 * as an argument in a server call does not exist in the system.
 * @author Jean-Pierre Smith
 * @aslexcludemethods
 */
@SuppressWarnings("serial")
public class ClientInexistentException extends FTaBServerException {
		
	/**
	 * The username of the client that does not exist in the system.
	 */
	private final String username;
	
	
	/**
	 * Creates a new exception specifying that the provided username of the client
	 * does not exist in the system
	 * @param clientName The name of the client that threw the exception.
	 */
	public ClientInexistentException(String clientName) {
		super(String.format("The client - %s does not exist in the system.", clientName));
		
		this.username = clientName;
	}

	/**
	 * Creates a new exception specifying that a requested client
	 * does not exist in the system
	 */
	public ClientInexistentException() {
		super("That client does not exist in the system.");
		
		this.username = null;
	}

	/**
	 * Gets the username of the client that prompted the exception.
	 * @return The name of the client or null if it was not specified.
	 * @aslexclude
	 */
	public String getUsername() {
		return username;
	}

}
