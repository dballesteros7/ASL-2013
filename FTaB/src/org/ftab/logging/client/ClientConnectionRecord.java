package org.ftab.logging.client;

import java.util.logging.Level;

import org.ftab.client.Client;
import org.ftab.client.exceptions.AlreadyOnlineException;
import org.ftab.client.exceptions.FullServerException;
import org.ftab.logging.SystemEvent;

/**
 * Log record for connection attempts
 * @author Jean-Pierre
 *
 */
@SuppressWarnings("serial")
public class ClientConnectionRecord extends ClientLogRecord {
	/**
	 * Flag indicating whether the record is the start or end of a connection attempt
	 */
	private final boolean isConnectionStart;
	
	/**
	 * Creates a new record for the start of a connection attempt
	 * @param client The client that is attempting to connect
	 */
	public ClientConnectionRecord(Client client) {
		super(Level.FINE, client, SystemEvent.CLIENT_CONNECTION, "Attempting to connect to server.");
		
		this.isConnectionStart = true;
	}
	
	/**
	 * Creates a new record for the end of a successful connection attempt
	 * @param client The client that attempted to connect
	 * @param startRecord The record of the the start of the connection attempt
	 */
	public ClientConnectionRecord(Client client, ClientConnectionRecord startRecord) {
		super(Level.FINE, client, SystemEvent.CLIENT_CONNECTION, "", startRecord); 
		
		this.setMessage(String.format("Connected after %d milliseconds.", 
				this.getChainElapsedTime()));
		
		this.isConnectionStart = false;
	}

	/**
	 * Creates a new record for the unsuccessful end of a connection attempt
	 * @param client The client that attempted to connect
	 * @param thrown The exception thrown on the connection attempt
	 * @param startRecord The record of the the start of the connection attempt
	 */
	public ClientConnectionRecord(Client client, Throwable thrown, ClientConnectionRecord startRecord) {
		super(Level.WARNING, client, SystemEvent.CLIENT_CONNECTION, "", startRecord);				
		
		/*
		 * If the exception thrown was not one of the expected errors then
		 * mark this log as severe
		 */
		if (thrown.getClass() != AlreadyOnlineException.class && 
				thrown.getClass() != FullServerException.class) {
			this.setLevel(Level.SEVERE);
		}
		
		this.setMessage(String.format("The connection attempt failed after %d milliseconds, reason: %s", 
				this.getChainElapsedTime(), thrown.getMessage()));
				
		this.isConnectionStart = false;
	}
	
	/**
	 * Gets whether the record represents the start or attempt for a 
	 * connection
	 * @return True if it is a start, false otherwise
	 */
	public boolean isConnectionStart() {
		return isConnectionStart;
	}
}
