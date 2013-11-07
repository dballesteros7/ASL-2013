package org.ftab.console.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;

import org.ftab.console.tablemodels.DateCellRenderer;
import org.ftab.console.tablemodels.messages.AllMessagesTableModel;
import org.ftab.console.tablemodels.messages.MessagesTableModel;
import org.ftab.console.ui.dialogs.ErrorPane;

@SuppressWarnings("serial")
public class MessagesPanel extends JPanel {
	private JTable tblAllMessages;
	private JLabel lblTotalMsgsStats;

	/**
	 * Create the panel.
	 */
	public MessagesPanel() {
		setLayout(new BorderLayout(0, 0));
		
		JScrollPane scrollPane = new JScrollPane();
		add(scrollPane);
		
		tblAllMessages = new JTable();
		tblAllMessages.setModel(new AllMessagesTableModel());
		tblAllMessages.getColumnModel().getColumn(MessagesTableModel.CreateTimeColIndex)
			.setCellRenderer(new DateCellRenderer());
		scrollPane.setViewportView(tblAllMessages);
		
		JPanel panel = new JPanel();
		add(panel, BorderLayout.NORTH);
		panel.setLayout(new BorderLayout(0, 0));
		
		JButton btnRefresh = new JButton("Refresh");
		btnRefresh.addActionListener(new RefreshMessagesTableListener());
		panel.add(btnRefresh, BorderLayout.EAST);
		
		JLabel lblMsgStats = new JLabel("Message Statistics:");
		panel.add(lblMsgStats, BorderLayout.NORTH);
		lblMsgStats.setFont(lblMsgStats.getFont().deriveFont(lblMsgStats.getFont().getStyle() | Font.BOLD, lblMsgStats.getFont().getSize() + 2f));
		
		JPanel panel_1 = new JPanel();
		panel.add(panel_1, BorderLayout.CENTER);
		panel_1.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_2 = new JPanel();
		panel_1.add(panel_2, BorderLayout.CENTER);
		panel_2.setLayout(new BoxLayout(panel_2, BoxLayout.X_AXIS));
		
		JPanel pnlStatsDetails = new JPanel();
		panel_2.add(pnlStatsDetails);
		pnlStatsDetails.setLayout(new GridLayout(0, 2, 10, 2));
		
		JLabel lblTotalMsgs = new JLabel("Total Msgs:");
		lblTotalMsgs.setHorizontalAlignment(SwingConstants.RIGHT);
		pnlStatsDetails.add(lblTotalMsgs);
		
		lblTotalMsgsStats = new JLabel("0");
		pnlStatsDetails.add(lblTotalMsgsStats);
		
		Component horizontalGlue = Box.createHorizontalGlue();
		horizontalGlue.setMaximumSize(new Dimension(2032767, 0));
		panel_2.add(horizontalGlue);

	}
	
	/**
	 * Listener for the refresh button of the clients table. 
	 * @author Jean-Pierre Smith
	 *
	 */
	private class RefreshMessagesTableListener implements ActionListener {
		
		/**
		 * On refresh, clears the dependent tables and updates the statistical
		 * information
		 */
		public void actionPerformed(ActionEvent arg0) {			
			try {
				MessagesPanel.this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				
				int val = ((AllMessagesTableModel)tblAllMessages.getModel()).Refresh();
				
				lblTotalMsgsStats.setText(Integer.toString(val));
				
			} catch (SQLException e) {
				ErrorPane.showErrorMessage(MessagesPanel.this,
						"An SQL exception occured while trying to refresh the client table:",
						"SQL Exception", JOptionPane.ERROR_MESSAGE, e);
				lblTotalMsgsStats.setText("0");
			} finally {
				MessagesPanel.this.setCursor(Cursor.getDefaultCursor());
			}
		}
	}
}


