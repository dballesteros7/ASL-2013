/**
 * ConnectionRequest.java
 * Created: 14.10.2013
 * Author: Diego
 */
package org.ftab.communication.requests;

import org.ftab.communication.ProtocolMessage;

/**
 * 
 */
public abstract class ConnectionRequest extends ProtocolMessage {
    public abstract String getUsername();
    public abstract boolean isConnection();
}
