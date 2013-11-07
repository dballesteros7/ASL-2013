/**
 * ServerLogger.java
 * Created: Oct 13, 2013
 * Author: Diego Ballesteros (diegob)
 */
package org.ftab.server;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.ftab.server.exceptions.ConfigurationErrorException;

/**
 * Logger for the server, it implements its own simple formatter that cuts down
 * the clutter of the default java formatter.
 */
public class ServerLogger extends Formatter {

    /**
     * Custom log levels for the application, these are the possible values for
     * the XML configuration file.
     */
    private static enum LOGGING_LEVELS {
        DEBUG, INFO, WARNING
    };

    /**
     * This utility method allows the server to easily format IP address from a
     * SocketChannel object.
     * 
     * @param sc
     *            socket channel to parse.
     * @return ip address and port associated to the socket channel.
     */
    public static String parseSocketAddress(SocketChannel sc) {
        return "[" + sc.socket().getInetAddress().getHostName() + ":" +
                sc.socket().getPort() + "]";
    }

    /**
     * Setup the root logger to use this formatter and log to the file in the
     * desired path.
     * 
     * @param logLevel
     *            minimum level of logging.
     * @param logOutput
     *            path to the file where the log should be stored.
     * @throws IOException
     *             If there is a problem opening the log file.
     * @throws ConfigurationErrorException
     *             if the logging level is not correct.
     */
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
        FileHandler log = new FileHandler(logOutput, 1024 * 1024 * 1024, 10);

        // Create a simple formatter and add the handler to the global logger
        ServerLogger logFormatter = new ServerLogger();
        log.setFormatter(logFormatter);
        for (Handler handler : logger.getHandlers()) {
            logger.removeHandler(handler);
        }
        logger.addHandler(log);
    }

    /**
     * Close the handlers in the global logger.
     */
    public static void closeLogger() {
        // Get the global logger to close it
        Logger logger = Logger.getLogger("");
        for (Handler handler : logger.getHandlers()) {
            handler.close();
            logger.removeHandler(handler);
        }
    }

    @Override
    public String format(LogRecord record) {
        StringBuilder sb = new StringBuilder(1000);
        Level recordLevel = record.getLevel();
        sb.append(calcDate(record.getMillis()));
        sb.append(" ");
        sb.append(String.format("%-32s", record.getLoggerName()));
        sb.append(" ");
        if (recordLevel.intValue() >= Level.WARNING.intValue())
            sb.append("ERROR: ");
        else if (recordLevel.intValue() >= Level.INFO.intValue())
            sb.append("INFO: ");
        else
            sb.append("DEBUG: ");
        if (record.getThrown() != null) {
            sb.append(record.getThrown().getMessage());
        } else {
            sb.append(record.getMessage());
        }
        sb.append("\n");
        return sb.toString();
    }

    /**
     * Utility method to parse miliseconds since epoch to a readable date.
     * 
     * @param millisecs
     *            miliseconds since epoch.
     * @return readable string for the data represented by milisecs.
     */
    private String calcDate(long millisecs) {
        SimpleDateFormat date_format = new SimpleDateFormat(
                "MM-dd HH:mm:ss.SSS");
        Date resultdate = new Date(millisecs);
        return date_format.format(resultdate);
    }

}
