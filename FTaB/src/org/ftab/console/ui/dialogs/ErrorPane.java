package org.ftab.console.ui.dialogs;

import java.awt.Component;

import javax.swing.JOptionPane;

/**
 * Convenience class for creating wrapped-text messages
 * @author Jean-Pierre Smith
 */
public class ErrorPane {
	private ErrorPane() { }
	
	/**
	 * Shows a JOptionPane dialog with  wrapped text.
	 * @param parentComponent determines the Frame in which the dialog is displayed; 
	 * if null, or if the parentComponent has no Frame, a default Frame is used
	 * @param message the Object to display
	 * @param title the title string for the dialog
	 * @param messageType the type of message to be displayed: ERROR_MESSAGE, 
	 * INFORMATION_MESSAGE, WARNING_MESSAGE, QUESTION_MESSAGE, or PLAIN_MESSAGE 
	 * @param e The exception whose message is to be displayed.
	 */
	public static void showErrorMessage(Component parentComponent, String message,
			String title, int messageType, Exception e)
	{
		StringBuilder builder = new StringBuilder();
		builder.append("<html><body><p style='width: 300px;'>");
		builder.append(message);
		builder.append("<br><br>");
		builder.append(e.getMessage());
		builder.append("<br>");
		builder.append("</p></body></html>");
		
		JOptionPane.showMessageDialog(parentComponent, builder.toString(), title, messageType);		
	}
}