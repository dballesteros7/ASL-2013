package org.ftab.console.ui;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.sql.Connection;
import java.sql.SQLException;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JTabbedPane;

import org.ftab.console.ui.dialogs.ConnectionDetailsDialog;
import org.postgresql.ds.PGPoolingDataSource;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class MgmtConsole  {

	/**
     * Connection pool for the database
     */
    private static PGPoolingDataSource source = new PGPoolingDataSource();
    
    /**
     * Connection to be used 
     */
    
	private JFrame frmManagementConsole;
	

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Runtime.getRuntime().addShutdownHook(new Thread() {
						@Override
						public void run() {
							if (source != null)
								source.close();
						}
					});
					
					MgmtConsole window = new MgmtConsole();
					window.frmManagementConsole.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public MgmtConsole() {
		initialize();
		
		this.displayConnectionSettingsDialog();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmManagementConsole = new JFrame();
		frmManagementConsole.setTitle("Management Console");
		frmManagementConsole.setBounds(100, 100, 633, 468);
		frmManagementConsole.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		frmManagementConsole.getContentPane().add(tabbedPane, BorderLayout.CENTER);
		
		tabbedPane.addTab("Clients", new ClientsPanel());
		tabbedPane.addTab("Queues", new QueuesPanel());
		tabbedPane.addTab("Messages", new MessagesPanel());
		
		JMenuBar menuBar = new JMenuBar();
		frmManagementConsole.setJMenuBar(menuBar);
		
		JMenu mnSettings = new JMenu("Settings");
		menuBar.add(mnSettings);
		
		JMenuItem mntmConnectionSettings = new JMenuItem("Connection Settings...");
		mntmConnectionSettings.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				displayConnectionSettingsDialog();
			}
		});
		mnSettings.add(mntmConnectionSettings);
	}
	
	/**
	 * Display the dialog to change the settings used for connecting to the database.
	 */
	private void displayConnectionSettingsDialog() {
		ConnectionDetailsDialog dialog = new ConnectionDetailsDialog();
		dialog.setVisible(true);
	}
	
	public static void SetConnectionDetails(String dbName, String srvName, String username, String password ) {
		source.close();
		
		source = new PGPoolingDataSource();
		source.setDataSourceName("MGMT Console Data Source");
		
		source.setServerName(srvName);
		source.setDatabaseName(dbName);
		source.setUser(username);
		source.setPassword(password);
	}
	
	public static Connection GetSourceConnection() throws SQLException {
		Connection con = source.getConnection();
		con.setReadOnly(true);
		return con;
	}
}
