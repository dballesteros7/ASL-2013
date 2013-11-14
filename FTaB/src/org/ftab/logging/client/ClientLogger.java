package org.ftab.logging.client;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Filter;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ftab.client.Client;

public class ClientLogger {

	/**
	 * Sets the logging level for the clients
	 * @param level THe logging level to set the logger to
	 */
	public static void setLevel(Level level) {
		final Logger logger = Logger.getLogger(Client.class.getName());
		logger.setLevel(level);
	}

	/**
	 * Adds a log stream that outputs client log data to the specified file
	 * @param fileName The name of the file to output the log data
	 * @param filter The filter to determine what is written to the log file, can be null
	 * @param formatter The formatter to determine the output format
	 * @throws SecurityException If there are IO problems opening the files
	 * @throws IOException If a secuirty manager exits and the caller does not have 
	 * adequate permission
	 */
	public static void addLogStream(String fileName, Filter filter, Formatter formatter) 
			throws SecurityException, IOException {
		final Logger logger = Logger.getLogger(Client.class.getName());
		final FileHandler handler = new FileHandler(fileName, 1024 * 1024 * 512, 1000);
		
		if (filter != null) handler.setFilter(filter);
		handler.setFormatter(formatter);
		
		logger.addHandler(handler);
	}
	
}
