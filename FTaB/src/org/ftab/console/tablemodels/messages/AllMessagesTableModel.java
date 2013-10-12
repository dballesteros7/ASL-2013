package org.ftab.console.tablemodels.messages;

import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;

import org.ftab.database.Message;
import org.ftab.database.message.GetAllMessages;

/**
 * Table model that displays all the messages in the message
 * table.
 * @author Jean-Pierre Smith
 *
 */
@SuppressWarnings("serial")
public class AllMessagesTableModel extends MessagesTableModel<Integer, Object> {

	public AllMessagesTableModel() { }

	@Override
	protected Integer LoadData(Object arg, Connection conn) throws SQLException {
		int count = 0;
		
		ArrayList<Message> msgs = new GetAllMessages().execute(conn);
		for (Message msg : msgs) {
			count++;

			this.addRow(new Object[] {
				msg.getId(), msg.getQueueId(), msg.getQueueName(), msg.getSender(), msg.getReceiver(),
				msg.getContext(), msg.getPriority(), new Date(msg.getCreateTime() * 1000l), msg.getContent()
			});

		}
		return Integer.valueOf(count);
	}	
}
