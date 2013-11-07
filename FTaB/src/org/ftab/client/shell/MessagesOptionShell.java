package org.ftab.client.shell;

import static org.ftab.client.shell.ClientShell.MENU_ITEM_FORMAT_STRING;
import static org.ftab.client.shell.ClientShell.SHELL_TITLE;
import static org.ftab.client.shell.ClientShell.isReturnToMainMenu;

import java.io.IOException;
import java.util.Scanner;

import org.ftab.client.Client;
import org.ftab.client.Message;
import org.ftab.client.exceptions.ClientInexistentException;
import org.ftab.client.exceptions.QueueInexistentException;
import org.ftab.client.exceptions.UnspecifiedErrorException;
import org.ftab.client.shell.ClientShell.Options;
import org.ftab.communication.exceptions.InvalidHeaderException;
import org.ftab.communication.requests.SendMessageRequest.Context;
import org.ftab.pubenums.Filter;
import org.ftab.pubenums.Order;

/**
 * Encapsulates the methods responsible for carrying out the message related options
 * @author Jean-Pierre
 */
final class MessagesOptionShell {
	/**
	 * The client that the shell is being run for
	 */
	private static Client client;
	
	/**
	 * A handle on the input scanner being used
	 */
	private static Scanner in;	
	
	/**
	 * Displays the shell menu for the message options and handles the selection		
	 * @param client The client that the shell is being displayed for
	 * @param scanner The scanner over the input console
	 * @throws InvalidHeaderException If the response was corrupted somehow
	 * @throws IOException If there was an error on the underlying channel
	 * @throws UnspecifiedErrorException If the server threw an error without specifying 
	 * the reason.
	 */ 
	static void RunMessagesOption(Client client, Scanner scanner) 
			throws UnspecifiedErrorException, IOException, InvalidHeaderException {
		MessagesOptionShell.in = scanner;
		MessagesOptionShell.client = client;
		
		StringBuilder builder = new StringBuilder(String.format("\n%s > %s\n\n", SHELL_TITLE, Options.MESSAGES.getTitle()));
		
		builder.append("Please enter the number of the option to continue:\n");
		
		MessageOptions[] options = MessageOptions.values();
		
		// Prints each line in the main menu
		for (int i = 0; i < options.length; i++) {
			builder.append(String.format(MENU_ITEM_FORMAT_STRING, i + 1, options[i].getDescription()));
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
					return;
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

		// Call the appropriate choice
		switch(options[result - 1]) {
		case SEND:
			SendMessageShell.sendMessageOption();
			break;
		case GET_QUEUES:
			getQueuesOption();
			break;
		case RETRIEVE:
			RetrieveMessageShell.retrieveMessageOption();
			break;
		}		
	}
	
	/**
	 * Displays the console output for a the retrieval of the message queues
	 * @throws InvalidHeaderException If the response was corrupted somehow
	 * @throws IOException If there was an error on the underlying channel
	 * @throws UnspecifiedErrorException If the server threw an error without specifying the reason.
	 */
	private static void getQueuesOption() 
			throws UnspecifiedErrorException, IOException, InvalidHeaderException {
		System.out.println(String.format("\n%s > %s > %s\n\n", 
				SHELL_TITLE, Options.MESSAGES.getTitle(), MessageOptions.GET_QUEUES.getTitle()));
		
		final Iterable<String> queues = client.GetWaitingQueues();		
		
		final StringBuilder builder = new StringBuilder("You have messages waiting in the following queues:\n");
		final int emptyLength = builder.length();
		for (String queue : queues) {
			builder.append(String.format("\t\u2022  %s\n", queue));
		}
		
		if (emptyLength == builder.length()) {
			System.out.println("You have no messages waiting in any queues.");
		} else {
			System.out.println(builder.toString());
		}
	}
	
	/**
	 * Enumeration for the options related to messages
	 * @author Jean-Pierre
	 *
	 */
	private enum MessageOptions {
		/**
		 * Sends a message to a another username
		 */
		SEND("Send a message to one or more queues.", "Send"), 
		
		/**
		 * Gets a message from the system
		 */
		RETRIEVE("Retrieve a message from the system.", "Retrieve"),
		
		/**
		 * Gets queues with messages waiting
		 */
		GET_QUEUES("Get the queues with messages waiting.", "Waiting Messages");
		
		/**
		 * The description of the enum value
		 */
		private final String description;
		
		/**
		 * The title to be used when this option is selected
		 */
		private final String title;
		
		/**
		 * Creates a new Options enum member with a specified string 
		 * description.
		 * @param description The description of the enumerated value
		 */
		private MessageOptions(String description, String title) {
			this.description = description;
			this.title = title;
		}
		
		/**
		 * Returns the description of an enumerated value.
		 * @return The description for the enumerated value
		 */
		public String getDescription() {
			return description;
		}
		
		/**
		 * Gets the title associated with this enum value
		 * @return The title to be used when this option is selected.
		 */
		public String getTitle() {
			return title;
		}
	}
	
	/**
	 * Contains the methods and displays for the shell to 
	 * send messages to remote clients
	 * @author Jean-Pierre
	 *
	 */
	private static final class SendMessageShell {
		/**
		 * Carries out the retrieval of the required information for the send command, and
		 * the send command itself
		 * @throws QueueInexistentException If at least one of the queues does not exist
		 * @throws ClientInexistentException If the receiver does not exist
		 * @throws UnspecifiedErrorException If the server encounter an error but refuses to pass details
		 * @throws IOException If an error occurred with the channel
		 * @throws InvalidHeaderException If the input data was somehow corrupted
		 */
		private static void sendMessageOption() 
				throws UnspecifiedErrorException, IOException, InvalidHeaderException {
			
			System.out.println(String.format("\n%s > %s >\n" , ClientShell.SHELL_TITLE, 
					Options.MESSAGES.getTitle(), MessageOptions.SEND));
			
			String[] queues;
			String receiver, content;
			byte priority;
			Context context;
			try {
				// Get the queues
				queues = getQueuesToSendTo();
				
				// Get the receiver
				receiver = getReceiverFromConsole();
				
				// Get the message Context
				context = getContextFromConsole();
				
				// Get the content
				content = getMessageContentFromConsole();
				
				// Get the message priority
				priority = getPriorityFromConsole();
			} catch (CancelOperationException e) {
				return;
			}
			
			try {
				if (receiver == null) {
					client.SendMessage(content, priority, context, queues);
				} else {
					client.SendMessage(content, priority, context, receiver, queues);
				}
				
				System.out.println("Message sent successfully.");
			} catch (QueueInexistentException e) {
				System.out.println(
						String.format("The message was not sent for the following reason:\n%s", 
								e.getMessage()));
			} catch (ClientInexistentException e) {
				System.out.println(
						String.format("The message was not sent for the following reason:\n%s", 
								e.getMessage()));
			}
		}
		
		/**
		 * Gets the context of the message from the console
		 * @return The context for the message
		 * @throws CancelOperationException If the operation was cancelled
		 */
		private static Context getContextFromConsole() throws CancelOperationException {
			StringBuilder builder = new StringBuilder("Please select the number of a message context from the list below:\n");
			
			Context[] values = Context.values();
			
			// Prints each line in the main menu
			for (int i = 0; i < values.length; i++) {
				builder.append(String.format(ClientShell.MENU_ITEM_FORMAT_STRING, i + 1, values[i].toString()));
			}
			
			System.out.println(builder.toString());
			
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
					
					if (result < 1 || result > values.length) {
						throw new NumberFormatException("Input outside of range.");
					}	
				} catch (NumberFormatException ex) {
					System.out.println("Invalid entry, please re-enter your selection.");
					validInput = false;
				}			
			} while (!validInput);
			
			System.out.println();
			
			return values[result - 1];			
		}
		
		/**
		 * Gets the priority of the message
		 * @return the priority of the message
		 * @throws CancelOperationException If the operation was cancelled
		 */
		private static byte getPriorityFromConsole() throws CancelOperationException {
			boolean validInput;
			byte result = -1;
			
			do {
				validInput = true;
							
				System.out.print("Please enter the message priority [1-10, 10 highest]: ");
				
				try {
					String input = in.nextLine();

					// Check if the input signifies a return to the main menu
					if (isReturnToMainMenu(input)) { 
						throw new CancelOperationException();
					}
					
					result = Byte.parseByte(input);
					
					if (result < 1 || result > 10) {
						throw new NumberFormatException("Input outside of range.");
					}	
				} catch (NumberFormatException ex) {
					System.out.println("Invalid entry, please re-enter your selection.");
					validInput = false;
				}			
			} while (!validInput);
						
			return result;
		}
			
		/**
		 * Retrieves the queues to which the message should be sent.
		 * @return An array containing the names of the queues
		 * @throws CancelOperationException If the operation was cancelled
		 */
		private static String[] getQueuesToSendTo() throws CancelOperationException {
			boolean validInput;
			String[] result;
			
			do {
				System.out.println("Enter the queues to send the message to, seperated by the character '|':");
				
				String readString = in.nextLine().trim();
				
				// Check if the input signifies a return to the main menu
				if (isReturnToMainMenu(readString)) { 
					throw new CancelOperationException();
				}
				
				result = readString.split("\\|");  
				
				validInput = result.length > 0;
				
				if (!validInput) {
					System.out.println("Invalid entry, please re-enter the details.");
				} else {
					for (int i = 0; i < result.length; i++) result[i] = result[i].trim();
				}
				
				
				
			} while (!validInput);
			
			System.out.println();
			return result;			
		}
		
		/**
		 * Gets the receiver of the message
		 * @return The name of the receiver or null if no receiver is to be used
		 * @throws CancelOperationException If the operation was cancelled
		 */
		private static String getReceiverFromConsole() throws CancelOperationException {
			System.out.print("Enter the username of the receiver, or '-' for no receiver: ");
			
			String result = in.nextLine().trim();
			
			// Check if the input signifies a return to the main menu
			if (isReturnToMainMenu(result)) { 
				throw new CancelOperationException();
			}
			
			System.out.println();
			
			if (result.equals("-")) {
				return null;
			} else {
				return result;
			}
		}

		/**
		 * Gets the content of the message 
		 * @return The message content.
		 * @throws CancelOperationException
		 */
		private static String getMessageContentFromConsole() throws CancelOperationException {
			System.out.println("Enter the content of the message: ");
			
			String result = in.nextLine();
			
			// Check if the input signifies a return to the main menu
			if (isReturnToMainMenu(result)) { 
				throw new CancelOperationException();
			}
			
			System.out.println();
			
			return result;
		}
	}

	/**
	 * Contains the methods and displays for the shell to retrieve messages
	 * @author Jean-Pierre
	 */
	private static final class RetrieveMessageShell {
		private static void retrieveMessageOption() throws UnspecifiedErrorException, IOException, InvalidHeaderException { 
			System.out.println(String.format("\n%s > %s > %s >\n" , ClientShell.SHELL_TITLE, 
					Options.MESSAGES.getTitle(), MessageOptions.RETRIEVE));
			
			try {
				final Filter filter = getFilterFromConsole();
				
				System.out.println(String.format("\n%s > %s > %s > %s >\n" , ClientShell.SHELL_TITLE, 
						Options.MESSAGES.getTitle(), MessageOptions.RETRIEVE, filter.getTitle()));
				
				final String value = getFilterValueFromConsole(filter);
				final Order order = getOrderFromConsole();
				final boolean delete = getDeleteDecisionFromConsole();
				
				Message message = null;
				
				switch(filter) {
				case QUEUE:
					message = client.ViewMessageFromQueue(value, delete, order);
					break;				
				case SENDER:
					message = client.ViewMessageFromSender(value, delete, order);
					break;
				}
				
				if (message == null) System.out.println("There was no message to be retrieved.");
				else System.out.println(message.toString());
			} catch (CancelOperationException e) {
				return;
			} catch (QueueInexistentException e) {
				System.out.println(
						String.format("The message was not sent for the following reason:\n%s", 
								e.getMessage()));
			} catch (ClientInexistentException e) {
				System.out.println(
						String.format("The message was not sent for the following reason:\n%s", 
								e.getMessage()));
			}
		}
		
		/**
		 * Gets a filter to be used to retrieve a message
		 * @return The selected filter
		 * @throws CancelOperationException If the user cancelled the operation
		 */
		private static Filter getFilterFromConsole() throws CancelOperationException {
			StringBuilder builder = new StringBuilder("Please select an option to continue:\n");
			
			Filter[] values = Filter.values();
			
			// Prints each line in the main menu
			for (int i = 0; i < values.length; i++) {
				builder.append(String.format(MENU_ITEM_FORMAT_STRING, i + 1, values[i].getDescription()));
			}
			
			System.out.println(builder.toString());
			
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
					
					if (result < 1 || result > values.length) {
						throw new NumberFormatException("Input outside of range.");
					}	
				} catch (NumberFormatException ex) {
					System.out.println("Invalid entry, please re-enter your selection.");
					validInput = false;
				}			
			} while (!validInput);
			
			System.out.println();
			
			return values[result - 1];
		}
		
		/**
		 * Gets the corresponding value of the filter from the console
		 * @param filter The filter for which the value is being sought
		 * @return The value of the filter
		 * @throws CancelOperationException If the user cancels the operation
		 */
		private static String getFilterValueFromConsole(Filter filter) throws CancelOperationException {
			switch (filter) {
			case QUEUE:
				System.out.print("Enter the name of the queue from which to fetch the message: ");
				break;
			case SENDER:
				System.out.print("Enter the username of the sender: ");
				break;
			}
						
			String result = in.nextLine().trim();
			
			// Check if the input signifies a return to the main menu
			if (isReturnToMainMenu(result)) { 
				throw new CancelOperationException();
			}
			
			System.out.println();
			
			return result;
		}
	
		/**
		 * Gets the order by which to fetch the message
		 * @return The order by which to fetch the message
		 * @throws CancelOperationException If the user cancels the operation
		 */
		private static Order getOrderFromConsole() throws CancelOperationException {
			boolean validInput;
			char[] result = null;
			
			do {
				validInput = true;
							
				System.out.print("Retrieve earliest message instead of highest priority message [Y/N]? ");
				
				try {
					String input = in.nextLine().trim();

					System.out.println();
					
					// Check if the input signifies a return to the main menu
					if (isReturnToMainMenu(input)) { 
						throw new CancelOperationException();
					}
					
					result = input.toLowerCase().toCharArray();
					
					if (result.length == 0 || (result[0] != 'y' && result[0] != 'n')) {
						throw new IllegalArgumentException();
					}	
				} catch (IllegalArgumentException ex) {
					System.out.println("Invalid entry, please re-enter your selection.");
					validInput = false;
				}			
			} while (!validInput);
						
			return result[0] == 'y' ? Order.TIMESTAMP : Order.PRIORITY;
		}

		/**
		 * Gets whether to pop or peek at the message
		 * @return True if to delete the message afterwards, false otherwise
		 * @throws CancelOperationException If the user cancels the operation
		 */
		private static boolean getDeleteDecisionFromConsole() throws CancelOperationException {
			boolean validInput;
			char[] result = null;
			
			do {
				validInput = true;
							
				System.out.print("Delete the message after retrieving it [Y/N]? ");
				
				try {
					String input = in.nextLine().trim();

					System.out.println();
					
					// Check if the input signifies a return to the main menu
					if (isReturnToMainMenu(input)) { 
						throw new CancelOperationException();
					}
					
					result = input.toLowerCase().toCharArray();
					
					if (result.length == 0 || (result[0] != 'y' && result[0] != 'n')) {
						throw new IllegalArgumentException();
					}	
				} catch (IllegalArgumentException ex) {
					System.out.println("Invalid entry, please re-enter your selection.");
					validInput = false;
				}			
			} while (!validInput);
						
			return result[0] == 'y' ? true : false;
		}
	}
}