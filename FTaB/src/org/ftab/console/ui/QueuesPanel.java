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
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import org.ftab.console.tablemodels.messages.MessagesInQueueTableModel;
import org.ftab.console.tablemodels.queues.AllQueuesTableModel;
import org.ftab.console.tablemodels.queues.QueuesTableModel;
import org.ftab.console.ui.dialogs.ErrorPane;

@SuppressWarnings("serial")
public class QueuesPanel extends JPanel {
	private JTable tblMessagesInQueue;
	private JTable tblQueues;
	private JLabel lblTotalQueuesStats;
	private JLabel lblMsgsInStats;
	
	/**
	 * Create the panel.
	 */
	public QueuesPanel() {
		setLayout(new BorderLayout(0, 0));
		
		JSplitPane splitPane = new JSplitPane();
		splitPane.setResizeWeight(0.5);
		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		add(splitPane, BorderLayout.CENTER);
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		splitPane.setRightComponent(tabbedPane);
		
		JPanel panel = new JPanel();
		tabbedPane.addTab("Messages In Queue", null, panel, null);
		panel.setLayout(new BorderLayout(0, 0));
		
		JScrollPane scrollPane = new JScrollPane();
		panel.add(scrollPane, BorderLayout.CENTER);
		
		tblMessagesInQueue = new JTable();
		tblMessagesInQueue.setModel(new MessagesInQueueTableModel());
		scrollPane.setViewportView(tblMessagesInQueue);
		
		JPanel panel_4 = new JPanel();
		panel.add(panel_4, BorderLayout.NORTH);
		panel_4.setLayout(new BoxLayout(panel_4, BoxLayout.X_AXIS));
		
		JPanel panel_5 = new JPanel();
		panel_4.add(panel_5);
		panel_5.setLayout(new GridLayout(0, 2, 10, 2));
		
		JLabel lblNewLabel = new JLabel("Msgs In Queue:");
		lblNewLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		panel_5.add(lblNewLabel);
		
		lblMsgsInStats = new JLabel("0");
		panel_5.add(lblMsgsInStats);
		
		Component horizontalGlue_1 = Box.createHorizontalGlue();
		horizontalGlue_1.setMaximumSize(new Dimension(2032767, 0));
		panel_4.add(horizontalGlue_1);
		
		JPanel panel_7 = new JPanel();
		splitPane.setLeftComponent(panel_7);
		panel_7.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_1 = new JPanel();
		panel_7.add(panel_1, BorderLayout.NORTH);
		panel_1.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_3 = new JPanel();
		panel_1.add(panel_3);
		panel_3.setLayout(new BoxLayout(panel_3, BoxLayout.X_AXIS));
		
		JPanel panel_2 = new JPanel();
		panel_3.add(panel_2);
		panel_2.setLayout(new GridLayout(0, 2, 10, 2));
		
		JLabel lblTotalQueues = new JLabel("Total Queues:");
		panel_2.add(lblTotalQueues);
		
		lblTotalQueuesStats = new JLabel("0");
		panel_2.add(lblTotalQueuesStats);
		
		Component horizontalGlue = Box.createHorizontalGlue();
		horizontalGlue.setMaximumSize(new Dimension(2032767, 0));
		panel_3.add(horizontalGlue);
		
		JButton btnRefresh = new JButton("Refresh");
		btnRefresh.addActionListener(new RefreshQueuesTableListener());
		panel_1.add(btnRefresh, BorderLayout.EAST);
		
		JLabel lblQueueStats = new JLabel("Queue Statistics");
		lblQueueStats.setFont(lblQueueStats.getFont().deriveFont(lblQueueStats.getFont().getStyle() | Font.BOLD, lblQueueStats.getFont().getSize() + 2f));
		panel_1.add(lblQueueStats, BorderLayout.NORTH);
		
		JScrollPane scrollPane_1 = new JScrollPane();
		panel_7.add(scrollPane_1, BorderLayout.CENTER);
		
		tblQueues = new JTable();
		tblQueues.setModel(new AllQueuesTableModel());
		tblQueues.getSelectionModel().addListSelectionListener(new QueuesTableSelectionListener());
		scrollPane_1.setViewportView(tblQueues);

	}
	
	
	/**
	 * Listener for the refresh button of the queues table. 
	 * @author Jean-Pierre Smith
	 *
	 */
	private class RefreshQueuesTableListener implements ActionListener {
		
		/**
		 * On refresh, clears the dependent tables and updates the statistical
		 * information
		 */
		public void actionPerformed(ActionEvent arg0) {
			AllQueuesTableModel model = (AllQueuesTableModel)tblQueues.getModel();
			
			try {
				QueuesPanel.this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				
				int value = model.Refresh();
				
				lblTotalQueuesStats.setText(Integer.toString(value));
			} catch (SQLException e) {
				lblTotalQueuesStats.setText("0");
				
				ErrorPane.showErrorMessage(QueuesPanel.this,
						"An SQL exception occured while trying to refresh the queues table:",
						"SQL Exception", JOptionPane.ERROR_MESSAGE, e);
			} finally {
				// Clear the other tables
				((DefaultTableModel)tblMessagesInQueue.getModel()).setRowCount(0);
				lblMsgsInStats.setText("0");

				QueuesPanel.this.setCursor(Cursor.getDefaultCursor());
			}
		}
	}
	
	/**
	 * Listener for changing selection in the queues table.
	 * @author Jean-Pierre Smith
	 *
	 */
	private class QueuesTableSelectionListener implements ListSelectionListener {

		/**
		 * On selection changed, refresh the visible dependent
		 * table. 
		 */
		@Override
		public void valueChanged(ListSelectionEvent e) {
			if (!e.getValueIsAdjusting()) {
				int row = tblQueues.getSelectedRow();
				if (row != -1)
				{
					String queueName = (String) tblQueues.getValueAt(row, QueuesTableModel.QueueNameColIndex);
					
					Integer val;
					try {
						val = ((MessagesInQueueTableModel)tblMessagesInQueue.getModel()).Refresh(queueName);
						lblMsgsInStats.setText(val.toString());
					} catch (SQLException e1) {
						lblMsgsInStats.setText("0");
						
						ErrorPane.showErrorMessage(QueuesPanel.this, "An error occured while trying to refresh the messages in this queue:", 
								"SQL Exception", JOptionPane.ERROR_MESSAGE, e1);
					}					
				}
			}
		}
		
	}

}
