/**
 * ProtocolMessage.java
 * Created: 14.10.2013
 * Author: Diego
 */
package org.ftab.communication;

import java.nio.ByteBuffer;

import org.ftab.communication.exceptions.InvalidHeaderException;
import org.ftab.communication.requests.ConnectionRequest;
import org.ftab.communication.requests.GetQueuesRequest;
import org.ftab.communication.requests.QueueModificationRequest;
import org.ftab.communication.requests.RetrieveMessageRequest;
import org.ftab.communication.requests.SendMessageRequest;
import org.ftab.communication.responses.GetQueuesResponse;
import org.ftab.communication.responses.RequestResponse;
import org.ftab.communication.responses.RetrieveMessageResponse;

/**
 * Base class for messages between the client and server. All client-server messages
 * extend this class. Handles the conversion from bytes to an instance of one of its 
 * subclasses. The format of the ProtocolMessage is: <br>
 * 
 * || __HEADER__				|| __MESSAGE_BODY__ 				||<br>
 * || START_MESSAGE | BodySize	|| MessageType	| Message Contents	||<br>
 * || 4 bytes		| 4 bytes	|| 1 byte		| BodySize - 1 bytes||
 */
public abstract class ProtocolMessage {
	/**
	 * Enumeration defining the type of messages that a {@link org.ftab.communication.ProtocolMessage#ProtocolMessage ProtocolMessage} 
	 * can be.
	 * @author Jean-Pierre Smith
	 */
	public enum MessageType {
        CONNECTION_REQUEST(CON_REQ), QUEUE_MODIFICATION(QUE_MOD), SEND_MESSAGE(SEND_MSG), RETRIEVE_MESSAGE(RETR_MSG), 
        RETRIEVE_QUEUES(RETR_QUE), REQUEST_RESPONSE(REQ_RESP), RETURNED_MESSAGES(RETU_MSG), 
        RETURNED_QUEUE(RETU_QUE); // MESSAGE_RECEIVED(MSG_REC), 
        
        /**
         * The associated byte value for an enum constant
         */
        private byte byteValue;
        
        /**
         * Creates the enum type with a specified mapping to a byte value.
         * @param mapping The byte value corresponding to the enum
         */
        MessageType(byte mapping) {
        	byteValue = mapping;
        }
        
        /**
         * Converts the enumeration constant to a byte value.
         * @return The byte value associated with the enumeration.
         */
        public byte getByteValue() {
        	return byteValue;
        }
        
        /**
         * Converts a byte value to an enumeration constant
         * @param b The byte value to convert
         * @return The enumerated constant corresponding to the supplied byte value.
         */
        public static MessageType fromByte(byte b) {
        	for(MessageType mType : MessageType.values()) {
        		if(b == mType.getByteValue()) {
        			return mType;
        		}
        	}
        	throw new IllegalArgumentException("That byte value has no defined MessageType.");
        }        
    };

    /**
     * Byte values to be sent across the link corresponding to various message types.
     */
    private static final byte CON_REQ = 0, QUE_MOD = 1, SEND_MSG = 2, RETR_MSG = 3, 
    		RETR_QUE = 5, REQ_RESP = 6, RETU_MSG = 7, RETU_QUE = 8; // MSG_REC = 4, 
    
    /**
     * The character set name to be used for all string encodings
     */
    public final static String CHARSET = "UTF-8";
    
	/**
	 * The number of bytes present in the header.<br>
	 */
    public final static int HEADER_SIZE = 8;
    
    /**
     * The byte sequence that marks the beginning of a message.
     */
    public final static byte[] START_MESSAGE = { -86, 86, -86, 86 };

    /**
     * The {@link #MessageType MessageType} enumeration corresponding to the type of this message.
     */
    protected MessageType messageType;
    
    /**
     * Returns the message type of this message.
     * @return The {@link org.ftab.communication.ProtocolMessage.MessageType#MessageType MessageType} 
     * enumeration corresponding to this message.
     */
    public MessageType getMessageType() {
    	return messageType;
    }
    
    /**
     * Returns the size of the body to be read based on the information in the
     * header segment.
     * @param header A sequence of {@link #HEADER_SIZE HEADER_SIZE} bytes beginning with the
     * byte array defined by {@link #START_MESSAGE START_MESSAGE}.
     * @return The number of bytes to be read to construct the corresponding message.
     * @throws InvalidHeaderException If the byte array passed does not begin with the byte array
     * designated by {@link #START_MESSAGE START_MESSAGE} or the array is not 
     * {@link #HEADER_SIZE HEADER_SIZE} in length. 
     */
    public static int getBodySize(ByteBuffer header) throws InvalidHeaderException{
        // Check that the header has the correct format
    	if (header.remaining() != HEADER_SIZE) {
    		throw new InvalidHeaderException("The supplied header is not of the correct size.");
    	}
    	else
    	{
    		// Check for equality between the first section of the header array and the START_MESSAGE
    		for (int i = 0; i < START_MESSAGE.length; i++) {
    			if (header.get() != START_MESSAGE[i]) {
    				throw new InvalidHeaderException("The supplied header is not of the correct size.");
    			}
    		}
    	}
    	
    	return header.getInt();
    }

    /**
     * Places a protocol message into a byte buffer.
     * @param message The message to be placed into a byte buffer.
     * @return The byte buffer containing the protocol message with its limit set to the end 
     * of the message and its position set to zero.
     */
    public static ByteBuffer toBytes(ProtocolMessage message) {
    	ByteBuffer bodyBuffer;
    	
    	switch(message.getMessageType()){
    	case SEND_MESSAGE:
			bodyBuffer = ((SendMessageRequest)message).toBytes();
			break;
    	case CONNECTION_REQUEST:
    		bodyBuffer = ((ConnectionRequest)message).toBytes();
    		break;
    	case RETRIEVE_QUEUES:
    		bodyBuffer = ((GetQueuesRequest)message).toBytes();
    		break;
//    	case MESSAGE_RECEIVED:
//    		bodyBuffer = ((MessageReceivedRequest)message).toBytes();
//    		break;
		case QUEUE_MODIFICATION:
			bodyBuffer = ((QueueModificationRequest)message).toBytes();
			break;
		case REQUEST_RESPONSE:
			bodyBuffer = ((RequestResponse)message).toBytes();
			break;
		case RETRIEVE_MESSAGE:
			bodyBuffer = ((RetrieveMessageRequest)message).toBytes();
			break;
		case RETURNED_MESSAGES:
			bodyBuffer = ((RetrieveMessageResponse)message).toBytes();
			break;
		case RETURNED_QUEUE:
			bodyBuffer = ((GetQueuesResponse)message).toBytes();
			break;
		default:
			throw new UnsupportedOperationException("That message type is not supported.");    	
    	}
    	
    	ByteBuffer messageBuffer = ByteBuffer.allocate(bodyBuffer.remaining() + HEADER_SIZE + 1);
    	
    	// Put the header
    	messageBuffer.put(START_MESSAGE).putInt(bodyBuffer.remaining() + 1);
    	
    	// Put the message type
    	messageBuffer.put(message.getMessageType().getByteValue());    	
    	
    	// Write out the contents from the previous buffer
    	messageBuffer.put(bodyBuffer);
    	
    	// Set up the buffer for being read
    	messageBuffer.flip();
    	
    	return messageBuffer;
    }
    
    /**
     * Converts a message into a byte buffer, less the header information.
     * @return A ByteBuffer containing this message with its limit set to the end
     * of the message and its position set to zero.
     */
    public abstract ByteBuffer toBytes();
     
    
    /**
     * Retrieves a message from a byte buffer.
     * @param input  The byte buffer to be read for the message.
     * @return The message corresponding to the contents in the buffer.
     */
    public static ProtocolMessage fromBytes(ByteBuffer input){
        byte mtype = input.get();
                
        switch (MessageType.fromByte(mtype)) {
        case SEND_MESSAGE:
			return SendMessageRequest.fromBytes(input);
        case CONNECTION_REQUEST:
        	return ConnectionRequest.fromBytes(input);
        case RETRIEVE_QUEUES:
        	return GetQueuesRequest.fromBytes(input);
//        case MESSAGE_RECEIVED:
//        	return MessageReceivedRequest.fromBytes(input);
        case QUEUE_MODIFICATION:
        	return QueueModificationRequest.fromBytes(input);
		case REQUEST_RESPONSE:
			return RequestResponse.fromBytes(input);
		case RETRIEVE_MESSAGE:
			return RetrieveMessageRequest.fromBytes(input);
		case RETURNED_MESSAGES:
			return RetrieveMessageResponse.fromBytes(input);
		case RETURNED_QUEUE:
			return GetQueuesResponse.fromBytes(input);
		default:
			throw new UnsupportedOperationException("That message type is not supported.");
        }
    }
}
