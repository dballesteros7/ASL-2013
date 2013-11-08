package org.ftab.logging.client;

import java.util.logging.Level;

import org.ftab.client.Client;
import org.ftab.client.Message;
import org.ftab.client.exceptions.ClientInexistentException;
import org.ftab.client.exceptions.QueueInexistentException;
import org.ftab.logging.SystemEvent;

/**
 * Log record for message sending attempts
 * @author Jean-Pierre
 *
 */
@SuppressWarnings("serial")
public class SendMsgLogRecord extends ClientLogRecord {
	/**
	 * Flag indicating whether the record is the start or end of an attempt
	 */
	private final boolean isAttemptStart;
	
	/**
	 * The message that was being sent
	 */
	private final Message sentMessage;
	
	/**
	 * Creates a new record for the start of a message sending attempt
	 * @param client The client that is attempting to send the message
	 * @param msg The message that is being sent
	 */
	public SendMsgLogRecord(Client client, Message msg) {
		super(Level.FINE, client, SystemEvent.SEND_MESSAGE, 
				String.format("Attempting to send %s.", msg.getSummary()));
		
		this.sentMessage = msg;
		this.isAttemptStart = true;
	}
	
	/**
	 * Creates a new record for the end of a successful send message attempt
	 * @param client The client that attempted to send the message
	 * @param startRecord The record logging the start of the attempt
	 */
	public SendMsgLogRecord(Client client, Message msg, SendMsgLogRecord startRecord) {
		super(Level.FINE, client, SystemEvent.SEND_MESSAGE, "", startRecord); 
		
		this.setMessage(String.format("Message successfully sent after %d milliseconds.", 
				this.getChainElapsedTime()));
		
		this.sentMessage = msg;
		this.isAttemptStart = false;
	}

	/**
	 * Creates a new record for the unsuccessful send message attempt
	 * @param client The client that attempted to send the message
	 * @param thrown The exception thrown on the attempt
	 * @param startRecord The record logging the start of the attempt
	 */
	public SendMsgLogRecord(Client client, Message msg, Throwable thrown, SendMsgLogRecord startRecord) {
		super(Level.WARNING, client, SystemEvent.SEND_MESSAGE, "", startRecord);				
		
		/*
		 * If the exception thrown was not one of the expected errors then
		 * mark this log as severe
		 */
		if (thrown.getClass() != ClientInexistentException.class && 
				thrown.getClass() != QueueInexistentException.class) {
			this.setLevel(Level.SEVERE);
		}
		
		this.setMessage(String.format("The send message attempt failed after %d milliseconds, reason: %s", 
				this.getChainElapsedTime(), thrown.getMessage()));
				
		this.sentMessage = msg;
		this.isAttemptStart = false;
	}
	
	/**
	 * Gets whether the record represents the start of an attempt
	 * @return True if it is a start, false otherwise
	 */
	public boolean isAttemptStart() {
		return isAttemptStart;
	}
	
	/**
	 * Gets the message that was attempting to be sent
	 * @return The message object that was sent
	 */
	public Message getSentMessage() {
		return sentMessage;
	}
	
}
