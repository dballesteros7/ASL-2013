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
	 * A record associated with this log record
	 */
	private SystemLogRecord associatedRecord;
	
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
	 * Creates a new log record for the system
	 * @param level The level of severity of the log
	 * @param eventCategory The category that the event falls within in the system
	 * @param msg The message describing the event
	 * @param associatedRec A log record associated with this record
	 */
	public SystemLogRecord(Level level, SystemEvent eventCategory, String msg, SystemLogRecord associatedRec) {
		super(level, msg);
		this.eventCategory = eventCategory;
		this.associatedRecord = associatedRec;
	}
	
	
	/**
	 * Returns the event category associated with the system log record
	 * @return The associated event category.
	 */
	public SystemEvent getEventCategory() {
		return eventCategory;
	}	
	
	/**
	 * Gets the log record associated with this record or null if none was
	 * set
	 * @return An associated log record
	 */
	public SystemLogRecord getAssociatedRecord() {
		return associatedRecord;
	}
	
	/**
	 * Sets the SystemLogRecord associated with this log record.
	 * @param associatedRecord The record to be set as this record's
	 * associated record.
	 */
	public void setAssociatedRecord(SystemLogRecord associatedRecord) {
		this.associatedRecord = associatedRecord;
	}
	
	/**
	 * Gets the time elapsed since the start of the chain of records created 
	 * @return The time elapsed.
	 */
	public long getChainElapsedTime() {
		if (this.getAssociatedRecord() == null || this.getAssociatedRecord().getMillis() < 0) {
			return 0;
		} else {
			return this.getMillis() 
					- this.getAssociatedRecord().getMillis() 
					+ this.getAssociatedRecord().getChainElapsedTime();
		}
	}
}
