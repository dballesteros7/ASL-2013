/**
 * NonExistingQueueException.java
 * Created: Oct 10, 2013
 * Author: Diego Ballesteros (diegob)
 */
package org.ftab.database.exceptions;

/**
 * Exception class that identifies scenarios where a non-existing queue was
 * referenced, e.g. when trying to delete a queue that is not present in the
 * database.
 */
public class InexistentQueueException extends Exception {

    /**
     * Serial version UID for serializable classes.
     */
    private static final long serialVersionUID = 5410975611826301103L;

    /**
     * Creates an exception with the given detail message.
     * 
     * @param message
     *            details about the exception.
     */
    public InexistentQueueException(String message) {
        super(message);
    }

}
