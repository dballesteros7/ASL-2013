/**
 * MessageReceivedRequest.java
 * Created: 15.10.2013
 * Author: Diego
 */
package org.ftab.communication.requests;

import org.ftab.communication.ProtocolMessage;

/**
 * 
 */
public abstract class MessageReceivedRequest extends ProtocolMessage {
    public abstract boolean isPop();
    public abstract long getMessageId();
    public abstract String getQueue();
}
