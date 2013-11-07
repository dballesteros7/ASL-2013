/**
* NonExistingQueueException.java
* Created: Oct 10, 2013
* Author: Diego Ballesteros (diegob)
*/
package org.ftab.database.exceptions;

import java.sql.SQLException;

/**
 * Exception class that identifies scenarios where a non-existing queue was
 * referenced, e.g. when trying to delete a queue that is not present in the
 * database.
 */
public class NonExistingQueueException extends DatabaseException {

	/**
	 * Serial version UID for serializable classes.
	 */
	private static final long serialVersionUID = 5410975611826301103L;

	/**
	 * Constructor that just implements the constructor of the abstract parent.
	 * @see DatabaseException
	 * @param originalEx original SQLException object.
	 */
	public NonExistingQueueException(SQLException originalEx) {
		super(originalEx);
	}

}
