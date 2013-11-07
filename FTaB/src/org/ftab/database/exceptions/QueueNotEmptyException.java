/**
 * QueueNotEmptyException.java
 * Created: Oct 10, 2013
 * Author: Diego Ballesteros (diegob)
 */
package org.ftab.database.exceptions;

/**
 * Exception that identifies an scenario where a client attempted to delete a
 * non-empty queue.
 */
public class QueueNotEmptyException extends Exception {

    /**
     * Serial version UID for serializable classes.
     */
    private static final long serialVersionUID = -1094886992938943908L;

    /**
     * Creates an exception with the given detail message.
     * 
     * @param message
     *            details about the exception.
     */
    public QueueNotEmptyException(String message) {
        super(message);
    }

}
