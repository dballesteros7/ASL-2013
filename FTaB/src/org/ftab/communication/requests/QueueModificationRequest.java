/**
 * QueueModificationRequest.java
 * Created: 15.10.2013
 * Author: Diego
 */
package org.ftab.communication.requests;

import org.ftab.communication.ProtocolMessage;

/**
 * 
 */
public abstract class QueueModificationRequest extends ProtocolMessage {
    public abstract String getQueueName();

    public abstract boolean isDelete();
}
