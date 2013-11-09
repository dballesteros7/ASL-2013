package org.ftab.logging.client.filters;

import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.ftab.logging.SystemEvent;
import org.ftab.logging.client.ClientLogRecord;

/**
 * Filters a log messages to only allow SEND or Retrieve non-error log messages
 * @author Jean-Pierre Smith
 *
 */
public class SendRetrieveFilter implements Filter {
	@Override
	public boolean isLoggable(LogRecord record) {
		if (record instanceof ClientLogRecord) {
			ClientLogRecord cRecord = (ClientLogRecord)record;
			
			// Narrow down to only send and retrieve log records
			if (cRecord.getEventCategory() == SystemEvent.SEND_MESSAGE 
					|| cRecord.getEventCategory() == SystemEvent.RETRIEVE_MESSAGE) {
				
				// Narrow down to only non-error log messages
				if (cRecord.getLevel() == Level.FINE) {
					
					// Narrow down to only those that are not starting records
					if (cRecord.getAssociatedRecord() != null) {
						return true;
					}
				}
			}			
		}
		return false;
	}

}
