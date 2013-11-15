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
 * Base class for all messages sent from the ServerRPC to the remote server. 
 * This class also handles the the conversion from bytes to an instance of one
 * of its subclasses. Supported subclasses are:<br>
 * &emsp;&bull; org.ftab.requests.ConnectionRequest<br>
 * &emsp;&bull; org.ftab.requests.GetQueuesRequest<br>
 * &emsp;&bull; org.ftab.requests.QueueModificationRequest<br>
 * &emsp;&bull; org.ftab.requests.RetrieveMessageRequest<br>
 * &emsp;&bull; org.ftab.requests.SendMessageRequest<br>
 * &emsp;&bull; org.ftab.responses.GetQueuesResponse<br>
 * &emsp;&bull; org.ftab.responses.RequestResponse<br>
 * &emsp;&bull; org.ftab.responses.RetrieveMessageResponse
 */
public abstract class ProtocolMessage {
	/**
	 * Enumeration defining the type of messages that a ProtocolMessage 
	 * can be.
	 * @author Jean-Pierre Smith
	 * @aslincludefields
	 */
	public enum MessageType {
		/** 
		 * Marks that the message body contains a message requesting a 
		 * connection or disconnection.
		 */
        CONNECTION_REQUEST(CON_REQ), 
        
        /**
         * Marks that the message body contains a message requesting a
         * queue modification action.
         */
        QUEUE_MODIFICATION(QUE_MOD), 
        
        /**
         * Marks that the message body contains a message requesting to
         * send a message.
         */
        SEND_MESSAGE(SEND_MSG), 
        
        /**
         * Marks that the message body contains a message requesting to
         * retrieve a message.
         */
        RETRIEVE_MESSAGE(RETR_MSG),
        
        /**
         * Marks that the message body contains a message requesting to
         * retrieve queues with messages waiting.
         */
        RETRIEVE_QUEUES(RETR_QUE), 
        
        /**
         * Marks that the message body contains a generic request response.
         */
        REQUEST_RESPONSE(REQ_RESP), 
        
        /**
         * Marks that the message body contains a the
         * requested message.
         */
        RETURNED_MESSAGES(RETU_MSG),
        
        /**
         * Marks that the message body contains the requested list of
         * queues.
         */
        RETURNED_QUEUE(RETU_QUE);  
        
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
         * Converts the enumeration constant to its byte value.
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
     * @return The MessageType enumerated value corresponding to this message.
     */
    public MessageType getMessageType() {
    	return messageType;
    }
    
    /**
     * Returns the size of the body to be read based on the information in the
     * header segment.
     * @param header A sequence of HEADER_SIZE bytes beginning with the
     * byte array defined by START_MESSAGE.
     * @return The number of bytes to be read to construct the corresponding message.
     * @throws InvalidHeaderException If the byte array passed does not begin with the byte array
     * designated by START_MESSAGE or the array is not HEADER_SIZE in length. 
     */
    public static int getBodySize(ByteBuffer header) throws InvalidHeaderException{
        // Check that the header has the correct format
    	if (header.remaining() != HEADER_SIZE) {
    		throw new InvalidHeaderException("The supplied header is not of the correct size. "
    		        + "Expected: " + HEADER_SIZE + " but got " + header.remaining());
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
     * Performs serialization on the supplied protocol message.
     * @param message The ProtocolMessage or subclass to be serialized.
     * @return A ByteBuffer containing the protocol message with its limit set to the end 
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
     * Serializes this ProtocolMessage excluding its header information.
     * @return A ByteBuffer containing this message with its limit set to the end
     * of the message and its position set to zero.
     */
    public abstract ByteBuffer toBytes();
     
    
    /**
     * Parses a ByteBuffer to its containing ProtocolMessage.
     * @param input  The ByteBuffer to be read for the message.
     * @return The ProtocolMessage corresponding to the contents in the buffer.
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
