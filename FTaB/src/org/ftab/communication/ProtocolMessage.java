/**
 * ProtocolMessage.java
 * Created: 14.10.2013
 * Author: Diego
 */
package org.ftab.communication;

import org.ftab.communication.exceptions.InvalidHeaderException;

/**
 * 
 */
public abstract class ProtocolMessage {

    public final static int HEADER_SIZE = 8;
    public final static byte[] START_MESSAGE = { -86, 86, -86, 86 };
    
    public enum MessageType {
        CONNECTION_REQUEST, QUEUE_MODIFICATION, SEND_MESSAGE, RETRIEVE_MESSAGE, MESSAGE_RECEIVED, RETRIEVE_QUEUES, REQUEST_RESPONSE, RETURNED_MESSAGES, RETURNED_QUEUE
    };

    public abstract byte[] toBytes();

    public static ProtocolMessage fromBytes(byte[] input){
        return null;
    }
    
    public static int getBodySize(byte[] header) throws InvalidHeaderException{
        return 0;
    }

    public abstract MessageType getMessageType();
}
