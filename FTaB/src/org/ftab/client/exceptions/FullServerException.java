package org.ftab.client.exceptions;

/**
 * Creates an exception that encapsulates a refusal of the server
 * to accept a connection due to reaching its capacity.
 * @author Jean-Pierre Smith
 *
 */
@SuppressWarnings("serial")
public class FullServerException extends FTaBServerException {
	/**
	 * The port that the server is listening on
	 */
	private final int serverPort;
	/**
	 * The hostname or address of the server
	 */
	private final String serverName;
	
	/**
	 * Creates a new FullServerException
	 * @param serverName The host name of the server 
	 * @param port The port number of the server 
	 */
	public FullServerException(String serverName, int port) { 
		super(String.format("The server at %s:%d is full and will not accept any more connections.", 
				serverName, port));
		
		this.serverPort = port;
		this.serverName = serverName;
	}
	
	/**
	 * Gets the name of the server that refused the connection.
	 * @return Returns the hostname, or the String form of the address if it doesn't 
	 * have a hostname (it was created using a literal)
	 */
	public String getServerName() {
		return serverName;
	}
	
	/**
	 * Gets the port that the server is listening on.
	 * @return The server's port.
	 */
	public int getServerPort() {
		return serverPort;
	}
}
