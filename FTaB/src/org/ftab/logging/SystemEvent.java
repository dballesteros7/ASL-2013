package org.ftab.logging;

public enum SystemEvent {
	/**
	 * Event of a client attempting to connect to a server
	 */
	CLIENT_CONNECTION,
	
	/**
	 * Event of a client attempting to send a message
	 */
	SEND_MESSAGE,
	
	/**
	 * Event of the client attempting to create a queue
	 */
	QUEUE_CREATION,
	
	/**
	 * Event of the client attempting to delete a queue
	 */
	QUEUE_DELETION,
	
	/**
	 * Event of the client fetching queues waiting
	 */
	FETCH_WAITING_QUEUES,
	
	/**
	 * Event of the client retrieving a message
	 */
	RETRIEVE_MESSAGE
	
}
