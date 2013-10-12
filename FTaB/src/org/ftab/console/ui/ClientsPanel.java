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
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import org.ftab.console.tablemodels.ClientsTableModel;
import org.ftab.console.tablemodels.ClientsTableModel.CTReturnObject;
import org.ftab.console.tablemodels.FTaBTableModel;
import org.ftab.console.ui.dialogs.ErrorPane;

public class ClientsPanel extends JPanel {
	private JTable clientsTable;
	
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
		lblClientStatistics.setFont(lblClientStatistics.getFont().deriveFont(lblClientStatistics.getFont().getStyle() | Font.BOLD, lblClientStatistics.getFont().getSize() + 3f));
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
		
		final JLabel lblTotalClients = new JLabel("0");
		statsPanel.add(lblTotalClients);
		
		JLabel onlineLbl = new JLabel("Online:");
		onlineLbl.setHorizontalAlignment(SwingConstants.RIGHT);
		statsPanel.add(onlineLbl);
		
		final JLabel lblOnlineClients = new JLabel("0");
		statsPanel.add(lblOnlineClients);
		
		JButton button = new JButton("Refresh");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				FTaBTableModel model = (FTaBTableModel)clientsTable.getModel();
				
				try {
					ClientsPanel.this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					
					CTReturnObject ro = (CTReturnObject)model.Refresh();
					
					lblOnlineClients.setText(Integer.toString(ro.getOnlineClients()));
					lblTotalClients.setText(Integer.toString(ro.getTotalClients()));
					
				} catch (SQLException e) {
					ErrorPane.showErrorMessage(ClientsPanel.this,
							"An SQL exception occured while trying to refresh the client table:",
							"SQL Exception", JOptionPane.ERROR_MESSAGE, e);
				} finally {
					ClientsPanel.this.setCursor(Cursor.getDefaultCursor());
				}
			}
		});
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
		clientsTable.setModel(new ClientsTableModel());
		
		clientsTable.getColumnModel().getColumn(0).setPreferredWidth(60);
		clientsTable.getColumnModel().getColumn(0).setMinWidth(60);
		clientsTable.getColumnModel().getColumn(1).setPreferredWidth(180);
		clientsTable.getColumnModel().getColumn(1).setMinWidth(120);
		clientsTable.getColumnModel().getColumn(2).setPreferredWidth(50);
		clientsTable.getColumnModel().getColumn(2).setMinWidth(50);
		clientsScrollPane.setViewportView(clientsTable);
	}
}
