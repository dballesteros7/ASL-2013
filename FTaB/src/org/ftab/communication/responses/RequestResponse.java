/**
 * RequestResponse.java
 * Created: 15.10.2013
 * Author: Diego
 */
package org.ftab.communication.responses;

import org.ftab.communication.ProtocolMessage;

/**
 * 
 */
public abstract class RequestResponse extends ProtocolMessage {
    public enum Status {
        SUCCESS,
        EXCEPTION,
        FULL_SERVER,
        NO_QUEUE,
        USER_ONLINE,
        QUEUE_NOT_EMPTY,
        NO_CLIENT,
        NO_MESSAGE
    };
    
    public abstract Status getStatus();
    public abstract String getDescription();
}
