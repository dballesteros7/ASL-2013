package org.ftab.logging.client.formatters;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

import org.ftab.logging.client.ClientLogRecord;

/**
 * Outputs the details of a client record in the format
 * client_username event_type start_time duration
 * @author Jean-Pierre Smith
 *
 */
public class ClientDetailRTFormatter extends Formatter {

	@Override
	public String format(LogRecord record) {
		if (record instanceof ClientLogRecord) {
			ClientLogRecord cRecord = (ClientLogRecord)record;
		
			if (cRecord != cRecord.getFirstRecord()) {
				return String.format("%s %s %d %d %s", 
						cRecord.getClientUsername(),
						cRecord.getEventCategory().name(),
						cRecord.getFirstRecord().getMillis(),
						cRecord.getChainElapsedTime(),
						System.getProperty("line.separator"));
			}
		}
		
		return "";
	}

}
