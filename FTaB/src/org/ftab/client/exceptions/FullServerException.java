package org.ftab.client.exceptions;

/**
 * The exception that is thrown when the server is refusing to accept 
 * a connection due to being at its full capacity.
 * @author Jean-Pierre Smith
 * @aslexcludemethods
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
	 * @aslexclude
	 */
	public String getServerName() {
		return serverName;
	}
	
	/**
	 * Gets the port that the server is listening on.
	 * @return The server's port.
	 * @aslexclude
	 */
	public int getServerPort() {
		return serverPort;
	}
}
