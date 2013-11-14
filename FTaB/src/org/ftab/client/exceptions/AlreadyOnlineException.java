package org.ftab.client.exceptions;

/**
 * The exception that is thrown by the server when a client that is 
 * currently recorded as being online attempts to log in.
 * @author Jean-Pierre Smith
 * @aslexcludemethods
 */
@SuppressWarnings("serial")
public class AlreadyOnlineException extends FTaBServerException {
	/**
	 * The username of the client that attempted to log in.
	 */
	private final String username;
	
	/**
	 * Creates a new exception that a client is already online.
	 * @param username The name of the client that attempted to log in.
	 */
	public AlreadyOnlineException(String username) {
		super(String.format("Client %s is already logged in.", username));
		
		this.username = username;
	}
	
	/**
	 * Creates a new exception that a client is already online.
	 */
	public AlreadyOnlineException() {
		super("The client is already logged in.");
		
		this.username = null;
	}

	/**
	 * Gets the username of the client attempting to login but 
	 * @return The username of the client if it was set. Null otherwise.
	 * @aslexclude
	 */
	public String getUsername() {
		return username;
	}

}
