package org.ftab.console.tablemodels.messages;

import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;

import org.ftab.database.Message;
import org.ftab.database.message.GetAllMessages;

/**
 * Table model that displays only messages that were sent by a specific client.
 * @author Jean-Pierre Smith
 *
 */
@SuppressWarnings("serial")
public class SentMessagesTableModel extends MessagesTableModel<Integer, String> {

	public SentMessagesTableModel() { }

	/**
	 * @param sender The sender of the messages.
	 * @return The number of messages sent by the provided sender.
	 */
	@Override
	protected Integer LoadData(String sender, Connection conn) throws SQLException {
		int count = 0;
		
		ArrayList<Message> msgs = new GetAllMessages().execute(conn);
		for (Message msg : msgs) {
			if (msg.getSender().equals(sender)) {
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
