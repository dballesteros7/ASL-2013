package org.ftab.console.ui;

import java.awt.EventQueue;

import javax.swing.JFrame;
import java.awt.BorderLayout;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.LineBorder;
import java.awt.Color;
import java.awt.Font;
import java.awt.FlowLayout;
import javax.swing.JLabel;
import javax.swing.JTextField;
import java.awt.Component;
import javax.swing.Box;
import javax.swing.border.EmptyBorder;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.ListSelectionModel;
import javax.swing.JToggleButton;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import javax.swing.JList;
import javax.swing.AbstractListModel;
import javax.swing.border.CompoundBorder;
import javax.swing.JButton;

public class ConsoleUI {

	private JFrame mainFrame;
	private JTextField filterTextField;
	private JTable dataTable;
	private final ButtonGroup displayBtnGroup = new ButtonGroup();

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ConsoleUI window = new ConsoleUI();
					window.mainFrame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public ConsoleUI() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		mainFrame = new JFrame();
		mainFrame.setTitle("Management Console");
		mainFrame.setBounds(100, 100, 628, 540);
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.getContentPane().setLayout(new BoxLayout(mainFrame.getContentPane(), BoxLayout.X_AXIS));
		
		JPanel backgroundPanel = new JPanel();
		mainFrame.getContentPane().add(backgroundPanel);
		backgroundPanel.setLayout(new BorderLayout(0, 0));
		
		JPanel categoryPanel = new JPanel();
		categoryPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		backgroundPanel.add(categoryPanel, BorderLayout.WEST);
		categoryPanel.setLayout(new BorderLayout(0, 0));
		
		JPanel filterPanel = new JPanel();
		filterPanel.setBorder(new EmptyBorder(5, 0, 0, 0));
		categoryPanel.add(filterPanel, BorderLayout.SOUTH);
		filterPanel.setLayout(new BoxLayout(filterPanel, BoxLayout.X_AXIS));
		
		JLabel lblFilter = new JLabel("Filter:");
		filterPanel.add(lblFilter);
		
		Component horizontalStrut = Box.createHorizontalStrut(10);
		filterPanel.add(horizontalStrut);
		
		filterTextField = new JTextField();
		filterPanel.add(filterTextField);
		filterTextField.setColumns(10);
		
		JPanel leftListPanel = new JPanel();
		categoryPanel.add(leftListPanel, BorderLayout.CENTER);
		leftListPanel.setLayout(new BorderLayout(0, 0));
		
		JList itemList = new JList();
		itemList.setBorder(new CompoundBorder(new LineBorder(new Color(0, 0, 0), 1, true), new EmptyBorder(5, 5, 5, 5)));
		itemList.setModel(new AbstractListModel() {
			String[] values = new String[] {"Test", "test2"};
			public int getSize() {
				return values.length;
			}
			public Object getElementAt(int index) {
				return values[index];
			}
		});
		itemList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		leftListPanel.add(itemList);
		
		JPanel radioBtnPanel = new JPanel();
		categoryPanel.add(radioBtnPanel, BorderLayout.NORTH);
		
		JRadioButton clientsRadioBtn = new JRadioButton("Clients");
		clientsRadioBtn.setSelected(true);
		displayBtnGroup.add(clientsRadioBtn);
		radioBtnPanel.add(clientsRadioBtn);
		
		JRadioButton queuesRadioBtn = new JRadioButton("Queues");
		displayBtnGroup.add(queuesRadioBtn);
		radioBtnPanel.add(queuesRadioBtn);
		
		JPanel displayPanel = new JPanel();
		displayPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		backgroundPanel.add(displayPanel, BorderLayout.CENTER);
		displayPanel.setLayout(new BorderLayout(0, 0));
		
		JButton refreshBtn = new JButton("Refresh");
		displayPanel.add(refreshBtn, BorderLayout.NORTH);
		
		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(10, 0, 0, 0));
		displayPanel.add(panel, BorderLayout.CENTER);
		panel.setLayout(new BorderLayout(0, 0));
		
		dataTable = new JTable();
		panel.add(dataTable);
		dataTable.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		dataTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		dataTable.setFillsViewportHeight(true);
		dataTable.setModel(new DefaultTableModel(
			new Object[][] {
			},
			new String[] {
			}
		));
	}

}
