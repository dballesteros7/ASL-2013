/**
 * RetrieveMessageResponse.java
 * Created: 15.10.2013
 * Author: Diego
 */
package org.ftab.communication.responses;

import org.ftab.communication.ProtocolMessage;
import org.ftab.communication.requests.SendMessageRequest.Context;

/**
 * 
 */
public abstract class RetrieveMessageResponse extends ProtocolMessage {
    public abstract long getMessageId();
    public abstract String getMessageContent();
    public abstract String getSender();
    public abstract String getReceiver();
    public abstract String getQueue();
    public abstract int getPriority();
    public abstract Context getContext();
}
