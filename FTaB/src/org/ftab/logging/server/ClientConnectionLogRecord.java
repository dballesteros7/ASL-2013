package org.ftab.logging.server;

import java.util.logging.Level;

import org.ftab.logging.SystemEvent;

/**
 * Log record for logs created by the client connection server class
 * @author Jean-Pierre Smith
 *
 */
@SuppressWarnings("serial")
public class ClientConnectionLogRecord extends ServerLogRecord {

	/**
	 * The client address of the client associated with the log
	 */
	private final String clientAddress;	
	
	/**
	 * Marks whether the retrieval was a success or not
	 */
	private boolean success;
	
	/**
	 * Creates a new log record with the specified level
	 * @param level The level to assign the log record
	 * @param clientAddress The address of the client 
	 * @param eventCategory The event type
	 * @param msg The associated message
	 */
	public ClientConnectionLogRecord(Level level, String clientAddress, 
			SystemEvent eventCategory, String msg) {
		super(level, clientAddress, eventCategory, msg);
		
		this.clientAddress = clientAddress;
		this.success = true;
	}

	/**
	 * Creates a new log record with the specified level and associated record
	 * @param level The level to assign the log record
	 * @param clientAddress The address of the client 
	 * @param eventCategory The event type
	 * @param msg The associated message
	 * @param associatedRec An associated record
	 */
	public ClientConnectionLogRecord(Level level, String clientAddress,
			SystemEvent eventCategory, String msg, ServerLogRecord associatedRec) {
		super(level, clientAddress, eventCategory, msg, associatedRec);
		
		this.clientAddress = clientAddress;
		this.success = true;
	}

	/**
	 * Creates a new log record with the level Level.FINE
	 * @param clientAddress The address of the client 
	 * @param eventCategory The event type
	 * @param msg The associated message
	 */
	public ClientConnectionLogRecord(String clientAddress, 
			SystemEvent eventCategory, String msg) {
		this(Level.FINE, clientAddress, eventCategory, msg);
	}

	/**
	 * Creates a new log record with the level Level.FINE and associated record
	 * @param clientAddress The address of the client 
	 * @param eventCategory The event type
	 * @param msg The associated message
	 * @param associatedRec An associated record
	 */
	public ClientConnectionLogRecord(String clientAddress,
			SystemEvent eventCategory, String msg, ServerLogRecord associatedRec) {
		this(Level.FINE, clientAddress, eventCategory, msg, associatedRec);
	}
	
	/**
	 * Creates a new log record with the level Level.SEVERE and throwable
	 * @param clientAddress The address of the client 
	 * @param eventCategory The event type
	 * @param msg The associated message
	 * @param e The exception associated with this record
	 */
	public ClientConnectionLogRecord(String clientAddress, 
			SystemEvent eventCategory, String msg, Throwable e) {
		this(Level.SEVERE, clientAddress, eventCategory, msg);
		
		this.setThrown(e);
		this.success = false;
	}

	/**
	 * Creates a new log record with the level Level.SEVERE and associated record and throwable
	 * @param clientAddress The address of the client 
	 * @param eventCategory The event type
	 * @param msg The associated message
	 * @param associatedRec An associated record
	 * @param e The exception associated with this record
	 */
	public ClientConnectionLogRecord(String clientAddress,
			SystemEvent eventCategory, String msg, ServerLogRecord associatedRec, Throwable e) {
		this(Level.SEVERE, clientAddress, eventCategory, msg, associatedRec);
		
		this.setThrown(e);
		this.success = false;
	}
	
	/**
	 * Gets the client address of the client associated with the log
	 * @return The client address
	 */
	public String getClientAddress() {
		return clientAddress;
	}
	
	/**
	 * Gets whether the operation was a success, default is true for exception 
	 * records and false otherwise <br>
	 * <br>
	 * WARNING: THIS MUST BE SET EXPLICITLY BEFORE EXPECTING IT TO BE ACCURATE
	 * @return True if the operation was considered a success, false otherwise
	 */
	public boolean isSuccess() {
		return success;
	}
	
	/**
	 * Sets whether this record encapsulates a successful operation
	 * @param success True for success, false otherwise
	 */
	public void setSuccess(boolean success) {
		this.success = success;
	}
}
