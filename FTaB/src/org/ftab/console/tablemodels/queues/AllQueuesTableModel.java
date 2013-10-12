package org.ftab.console.tablemodels.queues;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

import org.ftab.database.Queue;
import org.ftab.database.queue.RetrieveQueues;

/**
 * Table model that fetches and displays all the queues in the database
 * @author Jean-Pierre Smith
 *
 */
@SuppressWarnings("serial")
public class AllQueuesTableModel extends QueuesTableModel<Integer, Object> {

	public AllQueuesTableModel() { }

	@Override
	protected Integer LoadData(Object arg, Connection conn) throws SQLException {
		int count = 0;
		
		ArrayList<Queue> queues = new RetrieveQueues().execute(conn);
		for (Queue queue : queues) {
			count++;

			this.addRow(new Object[] {
				queue.getName(), queue.getMessageCount()
			});

		}
		return Integer.valueOf(count);
	}

}
