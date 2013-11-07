package org.ftab.console.ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import org.ftab.console.ui.MgmtConsole;

@SuppressWarnings("serial")
public class ConnectionDetailsDialog extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private JTextField tfServerName;
	private JTextField tfDatabaseName;
	private JTextField tfUsername;
	private JPasswordField passwordField;

	/**
	 * Create the dialog.
	 */
	public ConnectionDetailsDialog() {
		setTitle("Connection Settings");
		setModalityType(ModalityType.DOCUMENT_MODAL);
		setBounds(100, 100, 325, 204);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.X_AXIS));
		{
			Component horizontalGlue = Box.createHorizontalGlue();
			contentPanel.add(horizontalGlue);
		}
		{
			JPanel panel = new JPanel();
			contentPanel.add(panel);
			panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
			{
				Component verticalGlue = Box.createVerticalGlue();
				panel.add(verticalGlue);
			}
			{
				JPanel gridPanel = new JPanel();
				panel.add(gridPanel);
				gridPanel.setLayout(new GridLayout(0, 2, 7, 2));
				{
					JLabel label = new JLabel("Server Name:");
					label.setHorizontalAlignment(SwingConstants.RIGHT);
					gridPanel.add(label);
				}
				{
					tfServerName = new JTextField();
					tfServerName.setText("dryad01.ethz.ch");
					tfServerName.setColumns(15);
					gridPanel.add(tfServerName);
				}
				{
					JLabel label = new JLabel("Database Name:");
					label.setHorizontalAlignment(SwingConstants.RIGHT);
					gridPanel.add(label);
				}
				{
					tfDatabaseName = new JTextField();
					tfDatabaseName.setText("test1");
					tfDatabaseName.setColumns(15);
					gridPanel.add(tfDatabaseName);
				}
				{
					JLabel label = new JLabel("User Name:");
					label.setHorizontalAlignment(SwingConstants.RIGHT);
					gridPanel.add(label);
				}
				{
					tfUsername = new JTextField();
					tfUsername.setText("user25");
					tfUsername.setColumns(15);
					gridPanel.add(tfUsername);
				}
				{
					JLabel label = new JLabel("Password:");
					label.setHorizontalAlignment(SwingConstants.RIGHT);
					gridPanel.add(label);
				}
				{
					passwordField = new JPasswordField();
					gridPanel.add(passwordField);
				}
			}
			{
				Component verticalGlue = Box.createVerticalGlue();
				panel.add(verticalGlue);
			}
		}
		{
			Component horizontalGlue = Box.createHorizontalGlue();
			contentPanel.add(horizontalGlue);
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						MgmtConsole.SetConnectionDetails(tfDatabaseName.getText(), tfServerName.getText(), 
								tfUsername.getText(), new String(passwordField.getPassword()));
						
						ConnectionDetailsDialog.this.dispose();
					}
				});
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {					
					@Override
					public void actionPerformed(ActionEvent e) {
						ConnectionDetailsDialog.this.dispose();				
					}
				});
				buttonPane.add(cancelButton);
			}
		}
	}

}
