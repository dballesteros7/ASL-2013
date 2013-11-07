/**
 * MessageReceivedRequest.java
 * Created: 15.10.2013
 * Author: Diego
 */
package org.ftab.communication.requests;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import org.ftab.communication.ProtocolMessage;

/**
 * Encapsulates the response from the client to the server signifying that the
 * returned message was retrieved. Also contains details needed by the server 
 * to then carrying out a removal of the message from the database. The layout
 * of the message is as follows:<br>
 * 
 * | 1 byte - MessageType as byte | 8 byte - message ID | 1 byte - delete message bool |<br>
 * | 2 bytes - count of n bytes in queue name | n bytes - the queue name|<br>
 * @author Jean-Pierre Smith
 *
 */
public class MessageReceivedRequest extends ProtocolMessage {
    /**
     * Boolean value indicating whether the message detailed within should
     * be deleted from the database upon this confirmation.
     */
	private boolean deleteMessage = false;
	
	/**
	 * The ID of the message that was received.
	 */
    private long messageID;
    
    /**
     * The name of the queue from which the message was retreived.
     */
    private String queueName = null;
	
    /**
     * Creates a new response that will indicate to the server that a message that was 
     * sent to the client was received, along with how to proceed with the message.
     * @param msgID The message ID of the message whose reception is being confirmed.
     * @param queue The queue name of the queue from which the message originated.
     * @param deleteMsg A flag indicating whether to delete (true) or not the message from
     * the database.
     */
	public MessageReceivedRequest(long msgID, String queue, boolean deleteMsg) {
		this.messageType = MessageType.MESSAGE_RECEIVED;
		
		this.messageID = msgID;
		this.queueName = queue;
		this.deleteMessage = deleteMsg;
	}
	
	/**
	 * Gets whether the message should be removed from the queue it was
	 * on or not.
	 * @return True to pop the message from the queue, false otherwise.
	 */
	public boolean isPop() {
		return deleteMessage;
	}
	
	/**
	 * Gets the message ID of the message being confirmed.
	 * @return The long value of the message ID.
	 */
    public long getMessageId() {
    	return messageID;
    }
    
    /**
     * Gets the name of the queue on which the message resides.
     * @return The name of the queue from which the message originated.
     */
    public String getQueue() {
    	return queueName;
    }
	
    
    @Override
	public ByteBuffer toBytes() {
    	byte[] nameInBytes = null;
    	try {
			nameInBytes = queueName.getBytes(CHARSET);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
    	
    	ByteBuffer buffer = ByteBuffer.allocate(11 + nameInBytes.length);
    	
    	// Push the message ID
    	buffer.putLong(messageID);
    	// Push 1 or 0 for the boolean value of whether to delete the msg
    	buffer.put((byte) (deleteMessage ? 1 : 0));
    	// Push a count of the bytes in the queue name
    	buffer.putShort((short) nameInBytes.length);
    	// Push the bytes for the queue name
    	buffer.put(nameInBytes);
    	
    	buffer.flip();
		return buffer;
	}
    
    /**
     * Converts a byte buffer into a MessageReceivedRequest object
     * @param body A byte array containing the body of the message without
     * any header or type information
     * @return The MessageReceivedRequest object corresponding to the byte array
     */
    public static MessageReceivedRequest fromBytes(ByteBuffer body) {
    	long msgID = body.getLong();
    	boolean delete = (body.get() == 0) ? false : true;
    	
    	byte[] arr = new byte[body.getShort()];
    	body.get(arr);
    	String name = null;
    	try {
			name = new String(arr, CHARSET);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
    	
    	return new MessageReceivedRequest(msgID, name, delete);
    }
}
