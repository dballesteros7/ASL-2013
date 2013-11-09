package org.ftab.logging.server;

import java.util.logging.Level;

import org.ftab.logging.SystemEvent;
import org.ftab.logging.SystemLogRecord;
import org.ftab.server.ServerLogger;

/**
 * Base class for server logs
 * @author Jean-Pierre Smith
 *
 */
@SuppressWarnings("serial")
public class ServerLogRecord extends SystemLogRecord {
	
	/**
	 * The tag of the unit logging associated with this record
	 */
	private final String unitTag;
	
	private final static String logMessageFormat = "%s - %s | %s";
	
	/**
	 * Creates a new server log record
	 * @param level The level associated with the record
	 * @param unitTag An identifier for the entity logging the messgae, worker thead, servermanager, etc
	 * @param eventCategory The category of the event
	 * @param msg The message to be logged
	 */
	public ServerLogRecord(Level level, String unitTag, SystemEvent eventCategory, 
			String msg) {
		this(level, unitTag, eventCategory, msg, null);
	}
	
	/**
	 * Creates a new server log record
	 * @param level The level associated with the record
	 * @param unitTag An identifier for the entity logging the messgae, worker thead, servermanager, etc
	 * @param eventCategory The category of the event
	 * @param msg The message to be logged
	 * @param associatedRec A server log record associated with this record
	 */
	public ServerLogRecord(Level level, String unitTag, SystemEvent eventCategory, 
			String msg, ServerLogRecord associatedRec) {
		super(level, eventCategory, "", associatedRec);
		
		this.unitTag = unitTag;
		
		this.setMessage(msg);
	}
	
	@Override
	public void setMessage(String message) {
		super.setMessage(String.format(logMessageFormat, 
				ServerLogger.calcDate(this.getMillis()), 
				unitTag, message));
	}
	
	/**
	 * Gets the tag of the unit
	 * @return The tag of the unit associated with this record
	 */
	public String getUnitTag() {
		return unitTag;
	}
}
