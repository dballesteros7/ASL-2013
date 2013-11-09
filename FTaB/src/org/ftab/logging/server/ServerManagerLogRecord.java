package org.ftab.logging.server;

import java.util.logging.Level;

import org.ftab.logging.SystemEvent;
import org.ftab.server.ServerManager;

/**
 * Creates a new log record for a server manager
 * @author Jean-Pierre Smith
 *
 */
@SuppressWarnings("serial")
public class ServerManagerLogRecord extends ServerLogRecord {

	/**
	 * Creates a new log record for an event occurring in the server manager with 
	 * level Level.FINE
	 * @param manager The manager associated with the log record
	 * @param msg The message to be logged
	 */
	public ServerManagerLogRecord(ServerManager manager, String msg) {
		this(Level.FINE, manager, msg);
	}
	
	/**
	 * Creates a new log record for an event occurring in the server manager 
	 * @param level The level to create the log record with
	 * @param manager The manager associated with the log record
	 * @param msg The message to be logged
	 */
	public ServerManagerLogRecord(Level level, ServerManager manager, String msg) {
		super(level, manager.getServerName(), SystemEvent.SYSTEM_GENERIC, msg);
	}

	/**
	 * Creates a new log record for an event occurring in the server manager with 
	 * level Level.FINE
	 * @param manager The manager associated with the log record
	 * @param msg The message to be logged
	 * @param associatedRec An associated log record
	 */
	public ServerManagerLogRecord(ServerManager manager, String msg, 
			ServerLogRecord associatedRec) {
		this(Level.FINE, manager, msg, associatedRec);
	}
	
	/**
	 * Creates a new log record for an event occurring in the server manager
	 * @param level The level to create the record with
	 * @param manager The manager associated with the log record
	 * @param msg The message to be logged
	 * @param associatedRec An associated log record
	 */
	public ServerManagerLogRecord(Level level, ServerManager manager, String msg, 
			ServerLogRecord associatedRec) {
		super(level, manager.getServerName(), SystemEvent.SYSTEM_GENERIC, msg, associatedRec);
	}
	
	/**
	 * Creates a new severe log record for an event occurring in the server manager with 
	 * level Level.SEVERE
	 * @param manager The manager associated with the log record
	 * @param msg The message to be logged
	 * @param associatedRec An associated log record; may be null
	 * @param A thrown error
	 */
	public ServerManagerLogRecord(ServerManager manager, String msg, 
			ServerLogRecord associatedRec, Throwable thrown) {
		super(Level.SEVERE, manager.getServerName(), SystemEvent.SYSTEM_GENERIC, msg, associatedRec);
		
		this.setThrown(thrown);
	}
	
	/**
	 * Creates a new severe log record for an event occurring in the server manager with 
	 * level Level.SEVERE
	 * @param manager The manager associated with the log record
	 * @param msg The message to be logged
	 * @param A thrown error
	 */
	public ServerManagerLogRecord(ServerManager manager, String msg, Throwable thrown) {
		super(Level.SEVERE, manager.getServerName(), SystemEvent.SYSTEM_GENERIC, msg);
		
		this.setThrown(thrown);
	}
	
}
