/**
 * GetQueuesRequest.java
 * Created: 15.10.2013
 * Author: Diego
 */
package org.ftab.communication.requests;

import java.nio.ByteBuffer;

import org.ftab.communication.ProtocolMessage;

/**
 * Encapsulates a request for the queues containing messages for
 * the calling client.<br>
 * As a simple request, the structure for this call is just:<br>
 * | 1 byte - MessageType as byte |
 */
public class GetQueuesRequest extends ProtocolMessage {
	/**
	 * Creates a request for queues, intializing the message type identifying
	 * this protocol message.
	 */
	public GetQueuesRequest() {
		this.messageType = MessageType.RETRIEVE_QUEUES;
	}
	
	@Override
	public ByteBuffer toBytes() {
		ByteBuffer buffer = ByteBuffer.allocate(0);
		
		return buffer;
	}
	
	/**
	 * Returns an object of this type from an empty byte array.
	 * @param body The ByteBuffer containing the data for this class.
	 * @return A standard GetQueuesRequest object.
	 */
	public static GetQueuesRequest fromBytes(ByteBuffer body) {
		return new GetQueuesRequest();
	}
}
