/**
 * SendMessageRequest.java
 * Created: 15.10.2013
 * Author: Diego
 */
package org.ftab.communication.requests;

import org.ftab.communication.ProtocolMessage;

/**
 * 
 */
public abstract class SendMessageRequest extends ProtocolMessage {
    public enum Context {
        REQUEST,
        RESPONSE,
        NONE
    };
    public abstract Iterable<String> getQueueList();
    public abstract String getReceiver();
    public abstract String getMessage();
    public abstract int getPriority();
}
