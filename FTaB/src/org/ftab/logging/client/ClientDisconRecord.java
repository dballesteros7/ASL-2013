package org.ftab.logging.client;

import java.util.logging.Level;

import org.ftab.client.Client;
import org.ftab.logging.SystemEvent;

/**
 * Log record for disconnection attempts
 * @author Jean-Pierre
 *
 */
@SuppressWarnings("serial")
public class ClientDisconRecord extends ClientLogRecord {
	/**
	 * Flag indicating whether the record is the start or end of a disconnection attempt
	 */
	private final boolean isDisconnectionStart;
	
	/**
	 * Creates a new record for the start of a disconnection attempt
	 * @param client The client that is attempting to disconnect
	 */
	public ClientDisconRecord(Client client) {
		super(Level.FINE, client, SystemEvent.CLIENT_CONNECTION, "Attempting to disconnect.");
		
		this.isDisconnectionStart = true;
	}
	
	/**
	 * Creates a new record for the end of a successful disconnection attempt
	 * @param client The client that attempted to disconnect
	 * @param disconStart The record for the start of the disconnection attempt
	 */
	public ClientDisconRecord(Client client, ClientDisconRecord disconStart) {
		super(Level.FINE, client, SystemEvent.CLIENT_CONNECTION, "", disconStart); 
		
		this.setMessage(String.format("Disconnected after %d milliseconds.", 
				this.getChainElapsedTime()));
		
		this.isDisconnectionStart = false;
	}

	/**
	 * Creates a new record for the unsuccessful end of a disconnection attempt
	 * @param client The client that attempted to disconnect
	 * @param thrown The exception thrown on the disconnection attempt
	 * @param disconStart The record for the start of the disconnection attempt
	 */
	public ClientDisconRecord(Client client, Throwable thrown, ClientDisconRecord disconStart) {
		super(Level.SEVERE, client, SystemEvent.CLIENT_CONNECTION, "", disconStart);				
		
		this.setMessage(String.format("The disconnection failed after %d milliseconds, reason: %s", 
				this.getChainElapsedTime(), thrown.getMessage()));
				
		this.isDisconnectionStart = false;
	}
	
	/**
	 * Gets whether the record represents the start or attempt of a 
	 * disconnection
	 * @return True if it is a start, false otherwise
	 */
	public boolean isDisconnectionStart() {
		return isDisconnectionStart;
	}
}
