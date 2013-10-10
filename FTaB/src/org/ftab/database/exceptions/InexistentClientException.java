/**
 * InexistentClientException.java
 * Created: Oct 10, 2013
 * Author: Diego Ballesteros (diegob)
 */
package org.ftab.database.exceptions;

/**
 * Exception class that identifies scenarios where a inexistent client is
 * referenced in a database operation, e.g. when trying to create a message with
 * a receiver that doesn't exist.
 */
public class InexistentClientException extends Exception {

    /**
     * Serial version UID for serializable classes.
     */
    private static final long serialVersionUID = -7982328056744540113L;

    /**
     * Create an exception with the given details.
     * 
     * @param message
     *            details about the exception.
     */
    public InexistentClientException(String message) {
        super(message);
    }

}
