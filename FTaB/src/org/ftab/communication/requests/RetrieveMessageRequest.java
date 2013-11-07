/**
 * RetrieveMessage.java
 * Created: 15.10.2013
 * Author: Diego
 */
package org.ftab.communication.requests;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import org.ftab.communication.ProtocolMessage;
import org.ftab.pubenums.Filter;
import org.ftab.pubenums.Order;

/**
 * Class that encapsulates a request from the client to the server
 * to retreive a paticular message, either from a queue or by a sender. The
 * format for messages is:
 * <br><br>
 * | Filter - 1 byte | Order - 1 byte | Pop Message? - 1 byte | # of bytes in name - 2 bytes | name - n bytes | 
 * 
 * @author Jean-Pierre Smith
 *
 */
public class RetrieveMessageRequest extends ProtocolMessage {
    /**
     * The criteria by which to search for a message
     */
    private final Filter filterType;
    
    /**
     * The criteria by which to select which of the messages get returned.
     */
    private final Order orderedBy;
    
    /**
     * The name of the sender or queue from which to search for a message.
     */
    private final String value;
    
    /**
     * Flag indicating whether the message should be deleted after it is pulled.
     */
    private final boolean popMessage;
    
    /**
     * Creates a new request for a message.
     * @param name The name of the queue or sender from which to retreive the message.
     * @param type An enum of type Filter which indicates whether to retreive the message
     * from a particular queue or a particular sender
     * @param order An enum of type Order which indicates whether to retreive the earliest message
     * or the message with the highest priority.
     * @param andDelete A value signifying whether to delete the message after retrieval
     */
    public RetrieveMessageRequest(String name, Filter type, Order order, boolean andDelete) {
    	this.messageType = MessageType.RETRIEVE_MESSAGE;
    	
    	this.value = name;
    	this.filterType = type;
    	this.orderedBy = order;
    	this.popMessage = andDelete;
    }
     
    /**
     * Gets a value indicating whether this request is for a message from a queue or sender
     * @return A value of Filter.QUEUE to get a message from a queue and Filter.SENDER to
     * get the message sent by a particular sender.
     */
    public Filter getFilterType() {
    	return filterType;
    }
    
    /**
     * Gets a value indicating whether to retrieve the message based on priority or the message
     * with the earliest time.
     * @return A value of Order.Priority to retreive the message by priority or Order.TIMESTAMP to 
     * retreive the message by time stamp.
     */
    public Order getOrderBy() {
    	return orderedBy;
    }
    
    /**
     * A string that represents the queue or sender from which to retreive the message.
     * @return A string value that either represents a queue or sender's name.
     */
    public String getFilterValue() {
    	return value;
    }

    /**
     * Whether to pop the message after retrieving it.
     * @return True to pop the message from the queue, false otherwise.
     */
    public boolean isPopMessage() {
		return popMessage;
	}
    
    @Override
	public ByteBuffer toBytes() {
    	byte[] nameInBytes = null;
    	try {
			nameInBytes = value.getBytes(CHARSET);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
    	
    	final ByteBuffer buffer = ByteBuffer.allocate(5 + nameInBytes.length);
    	
    	// Put the filter data into the buffer
    	buffer.put(filterType.getByteValue());
    	// Put the order by type
    	buffer.put(orderedBy.getByteValue());
    	// Put the value whether to pop the message
    	buffer.put((byte) (popMessage ? 1 : 0));
    	// Put the # of bytes in the name
    	buffer.putShort((short) nameInBytes.length);
    	// Put the name in bytes
    	buffer.put(nameInBytes);
    	
    	buffer.flip();
    	return buffer;
	}
    
    /**
     * Converts a byte buffer into a RetrieveMessageRequest object
     * @param body A byte array containing the body of the message without
     * any header or type information
     * @return The RetrieveMessageRequest object corresponding to the byte array
     */
    public static RetrieveMessageRequest fromBytes(ByteBuffer body) {
    	final Filter filter = Filter.fromByte(body.get());
    	final Order order = Order.fromByte(body.get());
    	final boolean pop = body.get() == 0 ? false : true;
    	
    	byte[] arr = new byte[body.getShort()];
    	body.get(arr);
    	String name = null;
    	try {
			name = new String(arr, CHARSET);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
    	
    	return new RetrieveMessageRequest(name, filter, order, pop);
    }
}
