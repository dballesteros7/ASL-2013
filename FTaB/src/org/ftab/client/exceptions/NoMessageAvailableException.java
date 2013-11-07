package org.ftab.client.exceptions;

import org.ftab.communication.requests.RetrieveMessageRequest.Filter;

/**
 * An exception representing a lack of any message satisfying a particular condition.
 * @author Jean-Pierre Smith
 */
@SuppressWarnings("serial")
public class NoMessageAvailableException extends FTaBServerException {
	/**
	 * The value of the filter used to request the message.
	 */
	private final String value;
	
	/**
	 * The type of the filter being applied.
	 */
	private final Filter filterType;
	
	/**
	 * Creates a new exception signifying that no message was found satisfying the provided
	 * filter details.
	 * @param value The name of the queue or sender.
	 * @param filterType An enum indicating whether the value represents a queue or sender.
	 */
	public NoMessageAvailableException(String value, Filter filterType) {
		super(String.format("No messages were found for filter type %s of value %s.", 
				filterType.name(), value));
		
		this.value = value;
		this.filterType = filterType;
	}

	/**
	 * Creates a new exception signifying that no message was found.
	 */
	public NoMessageAvailableException() {
		super("No message was found to be returned.");
		
		this.value = null;
		this.filterType = null;
	}

	/**
	 * Gets the filter value that was used in requesting the message.
	 * @return The filter value or null if none was specified.
	 */
	public String getValue() {
		return value;
	}
	
	/**
	 * Gets the filter type being a queue or client.
	 * @return The filter type or null if none was specified.
	 */
	public Filter getFilterType() {
		return filterType;
	}

}
