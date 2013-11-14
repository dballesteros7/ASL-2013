/**
 * QueueModificationRequest.java
 * Created: 15.10.2013
 * Author: Diego
 */
package org.ftab.communication.requests;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import org.ftab.communication.ProtocolMessage;


/**
 * Encapsulates a request to create or delete a queue in the system.
 * @author Jean-Pierre Smith
 */
public class QueueModificationRequest extends ProtocolMessage {
    /**
     * The name of the queue to be created or deleted
     */
	final private String queueName;
	
	/**
	 * A value indicating whether this request is to delete the named
	 * queue or create the named queue.
	 */
	final private boolean deleteRequest;
	
	/**
	 * Creates a new QueueModificationRequest to create or delete
	 * a named queue.
	 * @param queue The queue to modify.
	 * @param delete True to delete the queue named, false to create it.
	 */
	public QueueModificationRequest(String queue, boolean delete) {
		this.messageType = MessageType.QUEUE_MODIFICATION;
		
		queueName = queue;
		deleteRequest = delete;
	}
	
	/**
	 * Gets the queue name of the queue on which the action is
	 * being performed.
	 * @return The name of the queue to be deleted or to assign the
	 * created queue. 
	 */
	public String getQueueName() {
		return queueName;
	}

	/**
	 * Gets a boolean value indicating whether this request is to create or
	 * delete the named queue.
	 * @return <b>true</b> to delete the named queue, <b>false</b> to create a
	 * queue with the supplied name.
	 */
    public boolean isDelete() {
    	return deleteRequest;
    }

	@Override
	public ByteBuffer toBytes() {
		byte[] nameInBytes = null;
    	try {
			nameInBytes = queueName.getBytes(CHARSET);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
    	
    	final ByteBuffer buffer = ByteBuffer.allocate(3 + nameInBytes.length);
    	
    	// Push 1 or 0 for the boolean value of whether to delete or create the queue
    	buffer.put((byte) (deleteRequest ? 1 : 0));
    	
    	// Push a count of the bytes in the queue name
    	buffer.putShort((short) nameInBytes.length);
    	
    	// Push the bytes for the queue name
    	buffer.put(nameInBytes);
    	
    	buffer.flip();
		return buffer;		
	}
	
	/**
     * Converts a byte array into a QueueModificationRequest object
     * @param body A byte array containing the body of the message without
     * any header or type information
     * @return The QueueModificiationRequest object corresponding to the byte array
     * @aslexclude
     */
	public static QueueModificationRequest fromBytes(ByteBuffer body) {
		final boolean delete = body.get() == 1 ? true : false;
		
		byte[] arr = new byte[body.getShort()];
		body.get(arr);
		String name = null;
		try {
			name = new String(arr, CHARSET);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		return new QueueModificationRequest(name, delete);
	}
}
