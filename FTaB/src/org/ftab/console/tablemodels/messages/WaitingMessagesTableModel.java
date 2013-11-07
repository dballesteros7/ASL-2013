package org.ftab.console.tablemodels.messages;

import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;

import org.ftab.database.Message;
import org.ftab.database.message.GetAllMessages;

/**
 * Table model that fetches and stores messages that are destined for a particular
 * client are current queued and waiting
 * @author Jean-Pierre Smith
 *
 */
@SuppressWarnings("serial")
public class WaitingMessagesTableModel extends MessagesTableModel<Integer, String> {

	public WaitingMessagesTableModel() { }

	/**
	 * @param The destined receiver for the messages
	 * @return The number of such messages waiting for the receiver
	 */
	@Override
	protected Integer LoadData(String receiver, Connection conn) throws SQLException {
		int count = 0;

		ArrayList<Message> msgs = new GetAllMessages().execute(conn);
		for (Message msg : msgs) {
			if (receiver.equals(msg.getReceiver())) {
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
