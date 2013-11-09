package org.ftab.logging.server.formatters;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

import org.ftab.logging.server.ClientConnectionLogRecord;

/**
 * Outputs the details of a client connection record in the format
 * client_address event_type start_time duration
 * @author Jean-Pierre Smith
 *
 */
public class ServerCCDetailRTFormatter extends Formatter {

	@Override
	public String format(LogRecord record) {
		if (record instanceof ClientConnectionLogRecord) {
			ClientConnectionLogRecord cRecord = (ClientConnectionLogRecord)record;
			
			if (cRecord != cRecord.getFirstRecord()) {
				return String.format("%s %s %d %d %s", 
						cRecord.getClientAddress(),
						cRecord.getEventCategory().name(),
						cRecord.getFirstRecord().getMillis(),
						cRecord.getChainElapsedTime(),
						System.getProperty("line.separator"));
			}			
		}
		
		return "";
	}
}
