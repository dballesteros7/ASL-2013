package org.ftab.communication.requests;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

import org.ftab.communication.ProtocolMessage;

/**
 * Represents a request from to send a message to a queue or group of queues.
 * @author Jean-Pierre Smith
 */
public class SendMessageRequest extends ProtocolMessage {
    /**
     * The designated receiver of the message.
     */
    final private String receiver;
    
    /**
     * The content of the message.
     */
    final private String messageContent;
    
    /**
     * The priority of the message, 1 - 10
     */
    final private byte messagePriority;
    
    /**
     * The context of the message
     */
    final private int messageContext;
    
    /**
     * An ArrayList containing the names of all the queues that the message is 
     * to be sent to.
     */
    final private ArrayList<String> deliveryQueues = new ArrayList<String>();
    
    /**
     * Constructs a new message sending request to send a message to a series of queues 
     * with a particular receiver.
     * @param message The body of the message to be sent, max 2000 characters.
     * @param priority The priority of the message 1 - 10
     * @param context The context of the message
     * @param queues Queues that the message are to be sent to.
     * @param receiver The designated receiver.
     */
    public SendMessageRequest(String message, byte priority, int context, 
    		Iterable<String> queues, String receiver) {
    	this.messageType = MessageType.SEND_MESSAGE;
    	
    	this.messageContent = message;
    	this.messagePriority = priority;
    	this.messageContext = context;
    	this.receiver = receiver;
    	for (String s : queues) deliveryQueues.add(s);
    }
    
    /**
     * Constructs a new message sending request to send a message to a series of queues
     * with no designated receiver.
     * @param message The body of the message to be sent, max 2000 characters.
     * @param priority The priority of the message 1 - 10
     * @param context The context of the message
     * @param queues Queues that the message are to be sent to.
     */
    public SendMessageRequest(String message, byte priority, int context,
    		Iterable<String> queues) {
    	this(message, priority, context, queues, null);
    }
    
    /**
     * Gets the designated receiver of the message
     * @return The name of the receiver of the message or null if the 
     * receiver was not specified.
     */
    public String getReceiver() {
    	return receiver;
    }
    
    /**
     * Gets a boolean value that indicates whether a receiver was specified
     * for the message.
     * @return <b>true</b> if the message has a receiver, <b>false</b> otherwise
     */
    public boolean hasReceiver() {
    	return (receiver != null);
    }
    
    /**
     * Retrieves the content of the message to be sent
     * @return A string containing the content of the message.
     */
    public String getMessage() {
    	return messageContent;
    }
    
    /**
     * Gets the priority of this message.
     * @return A value from 1 to 10, 10 being the highest, indicating
     * the priority of the message.
     */
    public byte getPriority() {
    	return messagePriority;
    }
    
    /**
     * Gets the context of the message
     * @return An integer value indicating the context of the message.
     */
    public int getContext() {
    	return messageContext;
    }
    
    /**
     * Gets the queues that the message should be sent to.
     * @return An iterable containing the names of the queues to put the
     * message upon.
     */
    public Iterable<String> getQueueList() {
    	return deliveryQueues;
    }
    
   
    /**
     * Special character that delimits the names of the queues 
     */
    private static final char delim = '|';
    
	@Override
	public ByteBuffer toBytes() {
		// Convert the queue names to bytes
		final StringBuilder builder = new StringBuilder();
		for (String str : deliveryQueues) { 
			builder.append(str);
			builder.append(delim);
		}
		final byte[] queuesInBytes = builder.toString().getBytes();
		
		// Convert the receiver name to bytes
		final byte[] receiverInBytes = (receiver != null)
				? receiver.getBytes() : new byte[0];
				
		// Convert the message content to bytes
		final byte[] messageInBytes = messageContent.getBytes();
		
		final ByteBuffer buffer = ByteBuffer.allocate(queuesInBytes.length + receiverInBytes.length +
				messageInBytes.length + 11);
		
		// Put the priority into the buffer
		buffer.put(messagePriority);
		// Put the context into the buffer
		buffer.putInt(messageContext);
		
		// Put the number of bytes in the queue names and the queue names
		buffer.putShort((short) queuesInBytes.length);
		buffer.put(queuesInBytes);
		
		// Put the number of bytes in the content and the content
		buffer.putShort((short) messageInBytes.length);
		buffer.put(messageInBytes);
		
		// Put the number of bytes in the receiver and the receiver
		buffer.putShort((short) receiverInBytes.length);
		buffer.put(receiverInBytes);
		
		buffer.flip();
		return buffer;		
	}
	
	/**
     * Converts a byte buffer into a SendMessageRequest object
     * @param body A byte array containing the body of the message without
     * any header or type information
     * @return The SendMessageRequest object corresponding to the byte array
     * @aslexclude
     */
	public static SendMessageRequest fromBytes(ByteBuffer body) {
		final byte prio = body.get();
		final int cont = body.getInt();
		
		// Get the queue names
		byte[] tempArray = new byte[body.getShort()];
		body.get(tempArray);
		String tempString = null;
		try {
			tempString = new String(tempArray, CHARSET);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		String[] queueArray = tempString.split("\\" + delim);
		
		// Get the content
		tempArray = new byte[body.getShort()];
		body.get(tempArray);
		String message = null;
		try {
			message = new String(tempArray, CHARSET);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		// Get the receiver
		tempArray = new byte[body.getShort()];
		String receiverString = null;
		if (tempArray.length != 0) {
			body.get(tempArray);
			try {
				receiverString = new String(tempArray, CHARSET);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return new SendMessageRequest(message, prio, cont, 
				Arrays.asList(queueArray), receiverString);
	}
}
