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
	private String clientUsername;
	
	/**
	 * The time elapsed since the period passed as a constructor argument 
	 */
	private long elapsedTime;
	
	/**
	 * Creates a new log record from a client
	 * @param level The level of severity of the recorrd
	 * @param client The client that created the log record
	 * @param millis A reference time for this event, from which its elapsed time will be calculated.
	 * @param eventCategory The category for this event
	 * @param msg The message for the log record
	 */
	public ClientLogRecord(Level level, Client client, long millis, SystemEvent eventCategory, String msg) {
		super(level, eventCategory, String.format("%s <-> %s | %s", client.getUsername(), client.getServer().toString(), msg));
		
		this.clientUsername = client.getUsername();
		
		/* Log the time elapsed since a related event */
		if (millis < 0) {
			elapsedTime = 0;
		} else {
			elapsedTime = this.getMillis() - millis;
		}
	}
	
	/**
	 * Creates a new log record from a client
	 * @param level The level of severity of the recorrd
	 * @param client The client that created the log record
	 * @param eventCategory The category for this event
	 * @param msg The message for the log record
	 */
	public ClientLogRecord(Level level, Client client, SystemEvent eventCategory, String msg) {
		this(level, client, -1l, eventCategory, msg);
	}
		
	/**
	 * Gets the username of the client
	 * @return The username of the client
	 */
	public String getClientUsername() {
		return clientUsername;
	}
	
	/**
	 * Gets the time elapsed since another related event.
	 * @return The time between this event and the time passed during
	 * construction.
	 */
	public long getElapsedTime() {
		return elapsedTime;
	}
}
