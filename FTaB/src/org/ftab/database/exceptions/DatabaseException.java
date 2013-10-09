/**
* DatabaseException.java
* Created: Oct 10, 2013
* Author: Diego Ballesteros (diegob)
*/
package org.ftab.database.exceptions;

import java.sql.SQLException;

/**
 * Abstract class for database related exceptions, these exceptions contain
 * the original SQLException object that was triggered in the database, if any.
 */
public abstract class DatabaseException extends Exception {

	/**
	 * Serial version UID for serializable classes.
	 */
	private static final long serialVersionUID = -2881649733606594127L;

	/**
	 * Original SQLException object
	 */
	private final SQLException sqlEx;
	
	/**
	 * Creates an exception object with the original SQL exception
	 * that triggered it, note that this can be null if
	 * there was no SQLException.
	 * @param originalEx original SQLException object.
	 */
	public DatabaseException(SQLException originalEx){
		super();
		sqlEx = originalEx;
	}

	/**
	 * Get the SQLException from the database
	 * @return the original SQLException
	 */
	public SQLException getSqlEx() {
		return sqlEx;
	}
	
}
