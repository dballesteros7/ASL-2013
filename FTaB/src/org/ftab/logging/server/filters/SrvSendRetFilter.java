package org.ftab.logging.server.filters;

import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.ftab.logging.SystemEvent;
import org.ftab.logging.server.ClientConnectionLogRecord;

public class SrvSendRetFilter implements Filter {
	@Override
	public boolean isLoggable(LogRecord record) {
		if (record instanceof ClientConnectionLogRecord) {
			ClientConnectionLogRecord cRecord = (ClientConnectionLogRecord)record;
			
			// Narrow down to send and retrieve if there was a message to retrieve
			if ((cRecord.getEventCategory() == SystemEvent.RETRIEVE_MESSAGE && cRecord.isSuccess()) ||
					cRecord.getEventCategory() == SystemEvent.SEND_MESSAGE) {
								
				// Records to be logged were marked with info
				if (cRecord.getLevel() == Level.INFO) {
					return true;
				}
			}
		}
		
		return false;
	}

}
