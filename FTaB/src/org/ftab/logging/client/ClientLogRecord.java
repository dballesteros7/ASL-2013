package org.ftab.logging.client;

import java.util.logging.Level;

import org.ftab.client.Client;
import org.ftab.logging.SystemEvent;
import org.ftab.logging.SystemLogRecord;

/**
 * Parent class for log records from clients.<br>
 * <br>
 * To make filtering easier, please adhere to the following guidelines:<br>
 * &emsp;&bull; Expected errors such as full queues should be logged with a 
 * level of WARNING<br> 
 * &emsp;&bull; Errors that are not expected such as IOErrors and 
 * InvalidHeaderException should be logged with a level of SEVERE<br>
 * &emsp;&bull; Normal logs for tracing of events should be done with a log 
 * level of FINE<br>
 * 
 * @author Jean-Pierre
 */
@SuppressWarnings("serial")
abstract class ClientLogRecord extends SystemLogRecord {
	/**
	 * The username of the client
	 */
	private final String clientUsername;
	
	/**
	 * The string rep of the server
	 */
	private final String clientServerString;
			
	/**
	 * Creates a new log record from a client
	 * @param level The level of severity of the recorrd
	 * @param client The client that created the log record
	 * @param eventCategory The category for this event
	 * @param msg The message for the log record
	 * @param associatedLogRecord The record to be associated with this log record
	 */
	public ClientLogRecord(Level level, Client client, SystemEvent eventCategory, String msg, ClientLogRecord associatedLogRecord) {
		super(level, eventCategory, String.format("%s <-> %s | %s", client.getUsername(), client.getServer().toString(), msg), 
				associatedLogRecord);
		
		this.clientUsername = client.getUsername();
		this.clientServerString = client.getServer().toString();
	}
	
	/**
	 * Creates a new log record from a client
	 * @param level The level of severity of the recorrd
	 * @param client The client that created the log record
	 * @param eventCategory The category for this event
	 * @param msg The message for the log record
	 */
	public ClientLogRecord(Level level, Client client, SystemEvent eventCategory, String msg) {
		this(level, client, eventCategory, msg, null);
	}
		
	
	@Override
	public void setMessage(String message) {
		super.setMessage(String.format("%s <-> %s | %s", 
				clientUsername, clientServerString, message));
	}
	
	/**
	 * Gets the username of the client
	 * @return The username of the client
	 */
	public String getClientUsername() {
		return clientUsername;
	}
}
