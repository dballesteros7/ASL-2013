package org.ftab.console.tablemodels.clients;

import org.ftab.console.tablemodels.FTaBTableModel;

/**
 * Provides a model for a client table. Override the LoadData
 * method to provide differing functionality for which rows are
 * displayed.
 * 
 * The table structure is:
 * Client ID - Integer, Username - String, Online Status - Boolean
 * 
 * @author Jean-Pierre Smith
 *
 * @param <S> The return type of the refresh method.
 * @param <T> The type of the parameter to be passed to the refresh method.
 */
@SuppressWarnings("serial")
public abstract class ClientsTableModel<S, T> extends FTaBTableModel<S, T> {
	public static final int IDIndex = 0, UsernameIndex = 1, StatusIndex = 2;	
	
	/**
	 * Creates a new instance of the ClientsTableModel 
	 */
	public ClientsTableModel() {
		super(new String[] {"Client ID", "Client Username", "Online"}, 
				new Class[] { Integer.class, String.class, Boolean.class });
	}
}
