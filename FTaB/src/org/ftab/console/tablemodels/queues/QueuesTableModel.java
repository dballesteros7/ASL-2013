package org.ftab.console.tablemodels.queues;

import org.ftab.console.tablemodels.FTaBTableModel;

/**
 * Abstract table model for representing queue data.
 * @author Jean-Pierre Smith
 *
 * @param <S> The return type of the refresh methods
 * @param <T> The parameter type of the refresh methods.
 */
@SuppressWarnings("serial")
public abstract class QueuesTableModel<S, T> extends FTaBTableModel<S, T> {
	public static final int QueueNameColIndex = 0;
	
	public QueuesTableModel() {
		super(new String[] {"Queue Name", "# of Messages"}, 
				new Class[] { String.class, Long.class });
	}
}
