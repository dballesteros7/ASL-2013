/**
 * ServerLogger.java
 * Created: Oct 13, 2013
 * Author: Diego Ballesteros (diegob)
 */
package org.ftab.server;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.ftab.server.exceptions.ConfigurationErrorException;

/**
 * 
 */
public class ServerLogger {

    private static enum LOGGING_LEVELS {
        DEBUG, INFO, WARNING
    };

    static private FileHandler log;
    static private SimpleFormatter logFormatter;

    public static String parseSocketAddress(SocketChannel sc) {
        return "[" + sc.socket().getInetAddress().getHostName() + ":"
                + sc.socket().getPort() + "]";
    }

    static public void setup(String logLevel, String logOutput)
            throws IOException, ConfigurationErrorException {

        // Get the global logger to configure it
        Logger logger = Logger.getLogger("");

        // Set the logging level
        switch (LOGGING_LEVELS.valueOf(logLevel)) {
        case DEBUG:
            logger.setLevel(Level.CONFIG);
            break;
        case INFO:
            logger.setLevel(Level.INFO);
            break;
        case WARNING:
            logger.setLevel(Level.WARNING);
            break;
        default:
            throw new ConfigurationErrorException(
                    "The log level is not a valid value, accepted values are : DEBUG, INFO, WARNING");
        }

        // Create a fileHandler pointing to the given file, the path is expected
        // to be a pattern for log rotation.
        log = new FileHandler(logOutput, 1024 * 1024 * 1024, 10);

        // Create a simple formatter and add the handler to the global logger
        logFormatter = new SimpleFormatter();
        log.setFormatter(logFormatter);
        logger.addHandler(log);
    }

}
