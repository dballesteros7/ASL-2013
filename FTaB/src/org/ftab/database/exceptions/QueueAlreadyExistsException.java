/**
 * QueueAlreadyExistsException.java
 * Created: Oct 10, 2013
 * Author: Diego Ballesteros (diegob)
 */
package org.ftab.database.exceptions;

/**
 * Exception class that identifies the scenario where a queue already exists in
 * the database and someone attempted to create it.
 */
public class QueueAlreadyExistsException extends Exception {

    /**
     * Serial version UID for serializable classes.
     */
    private static final long serialVersionUID = -395127328755879553L;

    /**
     * Creates an exception with the given detail message.
     * 
     * @param message
     *            details about the exception.
     */
    public QueueAlreadyExistsException(String message) {
        super(message);
    }

}
