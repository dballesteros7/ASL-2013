/**
* QueueAlreadyExistsException.java
* Created: Oct 10, 2013
* Author: Diego Ballesteros (diegob)
*/
package org.ftab.database.exceptions;

import java.sql.SQLException;

/**
 * Exception class that identifies the scenario where a queue already exists
 * in the database and someone attempted to create it.
 */
public class QueueAlreadyExistsException extends DatabaseException {
	
	/**
	 * Serial version UID for serializable classes.
	 */
	private static final long serialVersionUID = -395127328755879553L;

	/**
	 * Constructor that just implements the constructor of the abstract parent.
	 * @see DatabaseException
	 * @param originalEx original SQLException object.
	 */
	public QueueAlreadyExistsException(SQLException originalEx) {
		super(originalEx);
	}
}
