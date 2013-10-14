/**
 * GetQueuesResponse.java
 * Created: 15.10.2013
 * Author: Diego
 */
package org.ftab.communication.responses;

import org.ftab.communication.ProtocolMessage;

/**
 * 
 */
public abstract class GetQueuesResponse extends ProtocolMessage {
    public abstract Iterable<String> getQueues();
}
