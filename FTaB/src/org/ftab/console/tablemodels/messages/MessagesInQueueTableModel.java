package org.ftab.console.tablemodels.messages;

import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;

import org.ftab.database.Message;
import org.ftab.database.message.GetAllMessages;

/**
 * Table model that displays only the messages belonging to a 
 * particular queue.
 * @author Jean-Pierre Smith
 *
 */
@SuppressWarnings("serial")
public class MessagesInQueueTableModel extends MessagesTableModel<Integer, String> {

	public MessagesInQueueTableModel() { }

	@Override
	protected Integer LoadData(String queueName, Connection conn) throws SQLException {
		int count = 0;
		
		ArrayList<Message> msgs = new GetAllMessages().execute(conn);
		for (Message msg : msgs) {
			if (queueName.equals(msg.getQueueName())) {
				count++;
				
				this.addRow(new Object[] {
					msg.getId(), msg.getQueueId(), msg.getQueueName(), msg.getSender(), msg.getReceiver(),
					msg.getContext(), msg.getPriority(), new Date(msg.getCreateTime() * 1000l), msg.getContent()
				});
			}
		}
		return Integer.valueOf(count);		
	}

}
