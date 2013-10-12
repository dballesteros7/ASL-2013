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
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import org.ftab.console.tablemodels.DateCellRenderer;
import org.ftab.console.tablemodels.clients.AllClientsTableModel;
import org.ftab.console.tablemodels.clients.ClientsTableModel;
import org.ftab.console.tablemodels.clients.structs.AllClientsStats;
import org.ftab.console.tablemodels.messages.MessagesTableModel;
import org.ftab.console.tablemodels.messages.SentMessagesTableModel;
import org.ftab.console.tablemodels.messages.WaitingMessagesTableModel;
import org.ftab.console.ui.dialogs.ErrorPane;

@SuppressWarnings("serial")
public class ClientsPanel extends JPanel {
	private JTable clientsTable;
	private JTable tblClientMsgsSent;
	private JTable tblClientsMsgsWaiting;
	
	private JLabel lblTotalClients = new JLabel("0");
	private JLabel lblOnlineClients = new JLabel("0");
	private JLabel lblWaitStats;
	private JLabel lblSentStats;
	
	/**
	 * Create the panel.
	 */
	public ClientsPanel() {
		setPreferredSize(new Dimension(600, 480));
		setLayout(new BorderLayout(0, 0));
		
		JPanel statisticsPanel = new JPanel();
		statisticsPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		add(statisticsPanel, BorderLayout.NORTH);
		statisticsPanel.setLayout(new BorderLayout(0, 0));
		
		JLabel lblClientStatistics = new JLabel("Client Statistics");
		lblClientStatistics.setFont(lblClientStatistics.getFont().deriveFont(lblClientStatistics.getFont().getStyle() | Font.BOLD, lblClientStatistics.getFont().getSize() + 2f));
		statisticsPanel.add(lblClientStatistics, BorderLayout.NORTH);
		
		JPanel panel = new JPanel();
		statisticsPanel.add(panel, BorderLayout.CENTER);
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		
		JPanel statsPanel = new JPanel();
		panel.add(statsPanel);
		statsPanel.setLayout(new GridLayout(0, 2, 10, 2));
		
		JLabel totalLabel = new JLabel("In system:");
		totalLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		statsPanel.add(totalLabel);
		
		lblTotalClients = new JLabel("0");
		statsPanel.add(lblTotalClients);
		
		JLabel onlineLbl = new JLabel("Online:");
		onlineLbl.setHorizontalAlignment(SwingConstants.RIGHT);
		statsPanel.add(onlineLbl);
		
		lblOnlineClients = new JLabel("0");
		statsPanel.add(lblOnlineClients);
		
		JButton button = new JButton("Refresh");
		button.addActionListener(new RefreshClientsTableListener());
		statisticsPanel.add(button, BorderLayout.EAST);
		
		Component horizontalGlue = Box.createHorizontalGlue();
		horizontalGlue.setMaximumSize(new Dimension(9962774, 0));
		panel.add(horizontalGlue);
		
		JSplitPane splitPane = new JSplitPane();
		splitPane.setResizeWeight(0.5);
		add(splitPane, BorderLayout.CENTER);
		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		
		JScrollPane clientsScrollPane = new JScrollPane();
		splitPane.setLeftComponent(clientsScrollPane);
		
		clientsTable = new JTable();
		clientsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		clientsTable.getSelectionModel().addListSelectionListener(new ClientsTableSelectionListener());
		clientsTable.setModel(new AllClientsTableModel());
		
		clientsTable.getColumnModel().getColumn(0).setPreferredWidth(60);
		clientsTable.getColumnModel().getColumn(0).setMinWidth(60);
		clientsTable.getColumnModel().getColumn(1).setPreferredWidth(180);
		clientsTable.getColumnModel().getColumn(1).setMinWidth(120);
		clientsTable.getColumnModel().getColumn(2).setPreferredWidth(50);
		clientsTable.getColumnModel().getColumn(2).setMinWidth(50);
		clientsScrollPane.setViewportView(clientsTable);
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		splitPane.setRightComponent(tabbedPane);
		
		JPanel pnlMessagesSent = new JPanel();
		tabbedPane.addTab("Messages (Sent)", null, pnlMessagesSent, null);
		pnlMessagesSent.setLayout(new BorderLayout(0, 0));
		
		JScrollPane sPaneMsgsSent = new JScrollPane();
		pnlMessagesSent.add(sPaneMsgsSent, BorderLayout.CENTER);
		
		tblClientMsgsSent = new JTable();
		tblClientMsgsSent.setModel(new SentMessagesTableModel());
		tblClientMsgsSent.getColumnModel().getColumn(MessagesTableModel.CreateTimeColIndex)
			.setCellRenderer(new DateCellRenderer());
		sPaneMsgsSent.setViewportView(tblClientMsgsSent);
		
		JPanel pnlSentStatsEncl = new JPanel();
		pnlMessagesSent.add(pnlSentStatsEncl, BorderLayout.NORTH);
		pnlSentStatsEncl.setLayout(new BoxLayout(pnlSentStatsEncl, BoxLayout.X_AXIS));
		
		JPanel pnlSentStats = new JPanel();
		pnlSentStatsEncl.add(pnlSentStats);
		pnlSentStats.setLayout(new GridLayout(0, 2, 10, 2));
		
		JLabel lblTotalSent = new JLabel("Total sent & unrec.:");
		lblTotalSent.setHorizontalAlignment(SwingConstants.RIGHT);
		pnlSentStats.add(lblTotalSent);
		
		lblSentStats = new JLabel("0");
		pnlSentStats.add(lblSentStats);
		
		Component horizontalGlue_1 = Box.createHorizontalGlue();
		horizontalGlue_1.setMaximumSize(new Dimension(2032767, 0));
		pnlSentStatsEncl.add(horizontalGlue_1);
		
		JPanel pnlMessagesWaiting = new JPanel();
		tabbedPane.addTab("Messages (Waiting)", null, pnlMessagesWaiting, null);
		pnlMessagesWaiting.setLayout(new BorderLayout(0, 0));
		
		JPanel pnlWaitStatsEncl = new JPanel();
		pnlMessagesWaiting.add(pnlWaitStatsEncl, BorderLayout.NORTH);
		pnlWaitStatsEncl.setLayout(new BoxLayout(pnlWaitStatsEncl, BoxLayout.X_AXIS));
		
		JPanel pnlWaitStats = new JPanel();
		pnlWaitStatsEncl.add(pnlWaitStats);
		pnlWaitStats.setLayout(new GridLayout(0, 2, 10, 2));
		
		JLabel lblTotalWaiting = new JLabel("Total waiting:");
		pnlWaitStats.add(lblTotalWaiting);
		
		lblWaitStats = new JLabel("0");
		pnlWaitStats.add(lblWaitStats);
		
		Component horizontalGlue_2 = Box.createHorizontalGlue();
		horizontalGlue_2.setMaximumSize(new Dimension(2032767, 0));
		pnlWaitStatsEncl.add(horizontalGlue_2);
		
		JScrollPane sPaneMsgsWaiting = new JScrollPane();
		pnlMessagesWaiting.add(sPaneMsgsWaiting, BorderLayout.CENTER);
		
		tblClientsMsgsWaiting = new JTable();
		tblClientsMsgsWaiting.setModel(new WaitingMessagesTableModel());
		tblClientsMsgsWaiting.getColumnModel().getColumn(MessagesTableModel.CreateTimeColIndex)
			.setCellRenderer(new DateCellRenderer());
		sPaneMsgsWaiting.setViewportView(tblClientsMsgsWaiting);
	}
	
	/**
	 * Listener for the refresh button of the clients table. 
	 * @author Jean-Pierre Smith
	 *
	 */
	private class RefreshClientsTableListener implements ActionListener {
		
		/**
		 * On refresh, clears the dependent tables and updates the statistical
		 * information
		 */
		public void actionPerformed(ActionEvent arg0) {
			AllClientsTableModel model = (AllClientsTableModel)clientsTable.getModel();
			
			try {
				ClientsPanel.this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				
				AllClientsStats ro = model.Refresh();
				
				lblOnlineClients.setText(Integer.toString(ro.getOnlineClients()));
				lblTotalClients.setText(Integer.toString(ro.getTotalClients()));
				
			} catch (SQLException e) {
				lblOnlineClients.setText("0");
				lblTotalClients.setText("0");
				
				ErrorPane.showErrorMessage(ClientsPanel.this,
						"An SQL exception occured while trying to refresh the client table:",
						"SQL Exception", JOptionPane.ERROR_MESSAGE, e);
			} finally {
				// Clear the other tables
				((DefaultTableModel)tblClientMsgsSent.getModel()).setRowCount(0);
				lblSentStats.setText("0");
				((DefaultTableModel)tblClientsMsgsWaiting.getModel()).setRowCount(0);
				lblSentStats.setText("0");
				
				ClientsPanel.this.setCursor(Cursor.getDefaultCursor());
			}
		}
	}
	
	/**
	 * Listener for changing selection in the clients table.
	 * @author Jean-Pierre Smith
	 *
	 */
	private class ClientsTableSelectionListener implements ListSelectionListener {

		/**
		 * On selection changed, refresh the visible dependent
		 * table. 
		 */
		@Override
		public void valueChanged(ListSelectionEvent e) {
			if (!e.getValueIsAdjusting()) {
				int row = clientsTable.getSelectedRow();
				if (row != -1)
				{
					String clientUName = (String) clientsTable.getValueAt(row, ClientsTableModel.UsernameIndex);
					
					Integer val;
					try {
						val = ((SentMessagesTableModel)tblClientMsgsSent.getModel()).Refresh(clientUName);
						lblSentStats.setText(val.toString());
					} catch (SQLException e1) {
						ErrorPane.showErrorMessage(ClientsPanel.this, "An error occured while trying to refresh the messages sent by this client:", 
								"SQL Exception", JOptionPane.ERROR_MESSAGE, e1);
					}
					
					try {
						val = ((WaitingMessagesTableModel)tblClientsMsgsWaiting.getModel()).Refresh(clientUName);
						lblWaitStats.setText(val.toString());
					} catch (SQLException e1) {
						ErrorPane.showErrorMessage(ClientsPanel.this, "An error occured while trying to refresh the messages waiting for this client:", 
								"SQL Exception", JOptionPane.ERROR_MESSAGE, e1);
					}					
				}
			}
		}
	}
}
