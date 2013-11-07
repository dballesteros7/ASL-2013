package org.ftab.console.tablemodels.messages;

import java.sql.Date;

import org.ftab.console.tablemodels.FTaBTableModel;

/**
 * Provides a model for a messages table. Override the LoadData
 * method to provide differing functionality for which rows are
 * displayed.
 * 
 * The table structure is:
 * Message ID - long, Queue ID - long, Queue Name - String, Sender - String, Receiver - String, 
 * Context - short, Priority - short, Create Time - Data, Content - String		
 *
 * @author Jean-Pierre Smith
 *
 * @param <S> The return type of the refresh method
 * @param <T> The type of the parameter passed to the refresh method
 */
@SuppressWarnings("serial")
public abstract class MessagesTableModel<S, T> extends FTaBTableModel<S, T> {
	public static final int CreateTimeColIndex = 7;
	
	public MessagesTableModel() {
		super(new String[] {"Message ID", "Queue ID", "Queue Name", "Sender", 
				"Receiver", "Context", "Priority", "Create Time", "Content" }, 
				new Class[] {Long.class, Long.class, String.class, String.class, String.class,
					Short.class, Short.class, Date.class, String.class});
	}
}
