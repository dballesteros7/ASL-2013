package org.ftab.logging;

import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * The parent class for log records in the FTaB system
 * @author Jean-Pierre
 */
@SuppressWarnings("serial")
public abstract class SystemLogRecord extends LogRecord {
	/**
	 * The category of the event
	 */
	private SystemEvent eventCategory;
	
	/**
	 * Creates a new log record for the system
	 * @param level The level of severity of the log
	 * @param eventCategory The category that the event falls within in the system
	 * @param msg The message describing the event
	 */
	public SystemLogRecord(Level level, SystemEvent eventCategory, String msg) {
		super(level, msg);
		this.eventCategory = eventCategory;
	}
	
	/**
	 * Returns the event category associated with the system log record
	 * @return The associated event category.
	 */
	public SystemEvent getEventCategory() {
		return eventCategory;
	}	
}
