package org.ftab.logging.client.formatters;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * Formatter that just outputs the record message
 * @author Jean-Pierre Smith
 *
 */
public class MessageOnlyFormatter extends Formatter {
	@Override
	public String format(LogRecord record) {
		return record.getMessage() + System.getProperty("line.separator");
	}

}
