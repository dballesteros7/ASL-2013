package org.ftab.client.shell;

import java.io.IOException;
import java.util.Scanner;

import static org.ftab.client.shell.ClientShell.MENU_ITEM_FORMAT_STRING;
import static org.ftab.client.shell.ClientShell.SHELL_TITLE;
import static org.ftab.client.shell.ClientShell.isReturnToMainMenu;

import org.ftab.client.Client;
import org.ftab.client.exceptions.QueueInexistentException;
import org.ftab.client.exceptions.QueueNotEmptyException;
import org.ftab.client.exceptions.UnspecifiedErrorException;
import org.ftab.client.shell.ClientShell.Options;
import org.ftab.communication.exceptions.InvalidHeaderException;
import org.ftab.client.exceptions.QueueAEException;

/**
 * Class for the shell to manage the operations on queues
 * @author Jean-Pierre
 *
 */
final class QueuesOptionShell {
	/**
	 * The client that the shell is being run for
	 */
	private static Client client;
	
	/**
	 * A handle on the input scanner being used
	 */
	private static Scanner in;
	
	/**
	 * Handles console operations for queues
	 * @param client The client that the shell is being displayed for
	 * @param scanner The scanner over the input console
	 * @throws InvalidHeaderException If the response was corrupted somehow
	 * @throws IOException If there was an error on the underlying channel
	 * @throws UnspecifiedErrorException If the server threw an error without specifying 
	 * the reason.
	 */
	static void RunQueuesOption(Client client, Scanner scanner) 
			throws UnspecifiedErrorException, IOException, InvalidHeaderException {
		QueuesOptionShell.in = scanner;
		QueuesOptionShell.client = client;
		
		System.out.println(String.format("\n%s > %s\n\n", SHELL_TITLE, Options.QUEUES.getTitle()));
		
		try {
			final boolean isCreate = getIsCreateFromConsole();
			final String queueName = getQueueNameFromConsole(isCreate);
			
			if (isCreate) {
				QueuesOptionShell.client.CreateQueue(queueName);
			} else {
				QueuesOptionShell.client.DeleteQueue(queueName);
			}
			
			System.out.println("The operation was completed successfully.");
		} catch (CancelOperationException e) {
			return;
		} catch (QueueAEException e) {
			System.out.println(
					String.format("The operation was not completed for the following reason:\n%s", 
							e.getMessage()));
		} catch (QueueInexistentException e) {
			System.out.println(
					String.format("The operation was not completed for the following reason:\n%s", 
							e.getMessage()));
		} catch (QueueNotEmptyException e) {
			System.out.println(
					String.format("The operation was not completed for the following reason:\n%s", 
							e.getMessage()));
		}		
	}
	
	/**
	 * Gets whether the operation is to create or delete a queue
	 * @return True to create a new queue, false otherwise
	 * @throws CancelOperationException If the user decided to cancel the operation
	 */
	private static boolean getIsCreateFromConsole() throws CancelOperationException {
		String[] options = { "Create a new queue in the system.", "Delete an existing queue from the system."};
		final StringBuilder builder = new StringBuilder("Please enter the number of the option to continue:\n");
		
		// Prints each line in the main menu
		for (int i = 0; i < options.length; i++) {
			builder.append(String.format(MENU_ITEM_FORMAT_STRING, i + 1, options[i]));
		}
		
		System.out.println(builder.toString());
		
		// Get the response
		boolean validInput;
		int result = -1;
		
		do {
			validInput = true;
						
			System.out.print("Selection: ");
			
			try {
				String input = in.nextLine();

				// Check if the input signifies a return to the main menu
				if (isReturnToMainMenu(input)) { 
					throw new CancelOperationException();
				}
				
				result = Integer.parseInt(input);
				
				if (result < 1 || result > options.length) {
					throw new NumberFormatException("Input outside of range.");
				}	
			} catch (NumberFormatException ex) {
				System.out.println("Invalid entry, please re-enter your selection.");
				validInput = false;
			}			
		} while (!validInput);
		
		return result == 1 ? true : false;
	}
	
	/**
	 * 
	 * @param isCreate Whether the queue is being created or deleted
	 * @return The name of the queue
	 * @throws CancelOperationException I the
	 */
	private static String getQueueNameFromConsole(boolean isCreate) throws CancelOperationException {
		if (isCreate) {
			System.out.print("Enter the name of the queue to create: ");
		} else {
			System.out.print("Enter the name of the queue to delete: ");
		}
					
		String result = in.nextLine().trim();
		
		// Check if the input signifies a return to the main menu
		if (isReturnToMainMenu(result)) { 
			throw new CancelOperationException();
		}
		
		System.out.println();
		
		return result;
	}
}