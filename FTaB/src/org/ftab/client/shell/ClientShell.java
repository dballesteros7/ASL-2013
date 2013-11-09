package org.ftab.client.shell;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ftab.client.Client;
import org.ftab.client.exceptions.AlreadyOnlineException;
import org.ftab.client.exceptions.ClientInexistentException;
import org.ftab.client.exceptions.FullServerException;
import org.ftab.client.exceptions.UnspecifiedErrorException;
import org.ftab.communication.exceptions.InvalidHeaderException;
import org.ftab.logging.client.ClientLogger;
import org.ftab.logging.formatters.MessageOnlyFormatter;

/**
 * Shell interface for the client
 * @author Jean-Pierre Smith
 */
public final class ClientShell {
	/**
	 * The possible actions to carry out in the interface
	 * @author Jean-Pierre Smith
	 */
	protected enum Options {
		/**
		 * Connect to a server
		 */
		CONNECT("Connect to a remote server.", "Connect"), 
		
		/**
		 * Disconnect from the server
		 */
		DISCONNECT("Disconnect from the server.", "Disconnect"),
		
		/**
		 * Exits the terminal
		 */
		EXIT("Close this terminal.", "Exit"),
		
		/**
		 * Handles messages
		 */
		MESSAGES("Send and receive messages.", "Messages"),
		
		/**
		 * Handles queues
		 */
		QUEUES("Create and delete queues.", "Queues");	
		
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
		private Options(String description, String title) {
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
	 * The client being run
	 */
	private static Client client;
	
	/**
	 * The input scanner
	 */
	private static final Scanner in = new Scanner(System.in);
	
	/**
	 * The list of the items to be displayed for the main menu, initialized with 
	 * the default menu items.
	 */
	private static final ArrayList<Options> menuOptions = new ArrayList<Options>(
			Arrays.asList(new Options[] { Options.CONNECT, Options.EXIT })); 
	
	/**
	 * The formatted string that lists a menu item, requires a 
	 * decimal then string.
	 */
	protected static final String MENU_ITEM_FORMAT_STRING = "\t%d. %s\n";
		
	/**
	 * The name of the shell for use in the path description
	 */
	protected static String SHELL_TITLE = "Client Shell"; 
		
	/**
	 * Creates and runs a client
	 * @param args arg[0] contains 'true' or 'false' to denote whether to run with errors supressed
	 * @throws InvalidHeaderException 
	 * @throws IOException 
	 * @throws UnspecifiedErrorException 
	 * @throws ClientInexistentException  
	 */
	public static void main(String[] args) 
			throws UnspecifiedErrorException, IOException, InvalidHeaderException {		
		// Attach the shutdown hook to do any necessary cleanup
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				in.close();
			}
		});
		
		// Setup the logger
		ClientLogger.setLevel(Level.FINEST);
		ClientLogger.addLogStream("C:\\logs\\clientlog%g.log", null, new MessageOnlyFormatter());
		
		client = new Client(getUsernameFromConsole());
		client.setSuppressingErrors(args.length > 0 ? Boolean.parseBoolean(args[0]) : false);
		
		while (true) {
			switch (getMainMenuSelection()) {
			case CONNECT:
				connectOption();
				break;
			case DISCONNECT:
				disconnectOption();
				break;
			case EXIT:
				exitOption();
				break;
			case MESSAGES:
				MessagesOptionShell.RunMessagesOption(client, in);
				break;
			case QUEUES:
				QueuesOptionShell.RunQueuesOption(client, in);
				break;				
			}
		}
	}
	
	/**
	 * Responsible for the connection command line function.
	 * @throws UnspecifiedErrorException If the server threw an error but refused to specify what.
	 * @throws FullServerException If the server is too full
	 * @throws AlreadyOnlineException If the client is already online
	 * @throws IOException If an IO error happened on the channel
	 * @throws InvalidHeaderException If the response packet was somehow corrupted.
	 */
	private static void connectOption() 
			throws UnspecifiedErrorException, IOException, InvalidHeaderException {
		final Pattern pattern = Pattern.compile("(.+):(\\d+)");
		
		System.out.println(String.format("\n%s > %s >\n" , SHELL_TITLE, Options.CONNECT.getTitle()));
		
		boolean validInput;
		Matcher input = null;;
		
		do {
			
			System.out.print("Server details as 'hostname:port': ");
			
			String readString = in.nextLine();
			
			// Check if the input signifies a return to the main menu
			if (isReturnToMainMenu(readString)) { 
				return;
			}
			
			input = pattern.matcher(readString);
			validInput = input.lookingAt();
			
			if (!validInput) {
				System.out.println("Invalid entry, please re-enter the details.");
			}			
		} while (!validInput);
		
		System.out.println();
		
		boolean result = false;
		
		try {
			result = client.Connect(input.group(1), Integer.parseInt(input.group(2)));
		} catch (NumberFormatException e) { } catch (FullServerException e) {
			System.out.println(
					String.format("The client could not connect for the following reason:\n%s", 
							e.getMessage()));
		} catch (AlreadyOnlineException e) {
			System.out.println(
					String.format("The client could not connect for the following reason:\n%s", 
							e.getMessage()));
		}
		
		if (result) {
			System.out.println("The connection was successful.");
			
			menuOptions.clear();
			
			menuOptions.add(Options.MESSAGES);
			menuOptions.add(Options.QUEUES);
			menuOptions.add(Options.DISCONNECT);
		}
		else System.out.println("The connection was not successful.");
	}
	
	/**
	 * Responsible for the disconnect command line function.
	 * @throws UnspecifiedErrorException If the server threw an error but refused to specify what.
	 * @throws InvalidHeaderException If the response packet was somehow corrupted.
	 * @throws IOException If an IO error happened on the channel
	 */
	private static void disconnectOption() 
			throws UnspecifiedErrorException, InvalidHeaderException, IOException {
		System.out.println(String.format("\n%s > %s >\n" , SHELL_TITLE, Options.DISCONNECT.getTitle()));
		
		boolean result = client.Disconnect();
		
		if (result) {
			System.out.println("Successfully disconnected from the server.");
			
			menuOptions.clear();
			menuOptions.add(Options.CONNECT);
			menuOptions.add(Options.EXIT);
		} 
		else System.out.println("Was unable to disconnect from the server.");
	}
	
	/**
	 * Responsible for closing the terminal
	 */
	private static void exitOption() {
		System.out.println(String.format("\n%s > %s >\n" , SHELL_TITLE, Options.EXIT.getTitle()));
		
		System.out.println("Exiting the terminal...");
		
		System.exit(0);
	}
	
	/**
	 * Checks whether an input signifies a return to the main menu.
	 * @param readInput The input that was read
	 * @return True if the calling method should return to the main menu, false otherwise.
	 */
	protected static boolean isReturnToMainMenu(Object readInput) {
		if (readInput.toString().equals("")) {
			System.out.println("Returning to main menu...");
			return true;
		}
		
		return false;
	}
	
	/**
	 * Gets an option selection for the main menu
	 * @param in The scanner to be used to fetch the input
	 * @return An Options enum corresponding to the read input
	 */
	private static Options getMainMenuSelection() {
		printMainMenu();
		
		boolean validInput;
		int input = 0;
		
		do {
			validInput = true;
						
			System.out.print("Selection: ");
			
			try {
				input = in.nextInt();
				
				if (input < 1 || input > menuOptions.size()) {
					throw new InputMismatchException("Input outside of range.");
				}	
			} catch (InputMismatchException ex) {
				System.out.println("Invalid entry, please re-enter your selection.");
				validInput = false;
			}			
		} while (!validInput);
		
		// Clean up the input stream
		in.nextLine();
		
		return menuOptions.get(input - 1);
	}
	
	/**
	 * Prints the main menu
	 */
	private static void printMainMenu() {
		StringBuilder builder = new StringBuilder(String.format("\n%s >\n\n", SHELL_TITLE));
		builder.append("Please enter the number of the option to continue:\n");
		
		// Prints each line in the main menu
		for (int i = 0; i < menuOptions.size(); i++) {
			builder.append(String.format(MENU_ITEM_FORMAT_STRING, i + 1, menuOptions.get(i).getDescription()));
		}
		
		System.out.println(builder.toString());
	}

	/**
	 * Gets the username to be used from the console
	 * @return The username of the client
	 */
	private static String getUsernameFromConsole() {
		final StringBuilder builder = new StringBuilder(String.format("\n%s >\n\n", SHELL_TITLE));
		
		builder.append("Usename: ");
		
		System.out.print(builder.toString());
		
		String username;
		do {
			username = in.nextLine();
		} while (username.equals(""));
		
		SHELL_TITLE = String.format(SHELL_TITLE + " [%s]", username);
		
		return username;
	}
}
