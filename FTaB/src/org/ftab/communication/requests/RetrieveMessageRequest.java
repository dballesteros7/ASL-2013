/**
 * RetrieveMessage.java
 * Created: 15.10.2013
 * Author: Diego
 */
package org.ftab.communication.requests;

import org.ftab.communication.ProtocolMessage;

/**
 * 
 */
public abstract class RetrieveMessageRequest extends ProtocolMessage {
    public enum Filter {
        QUEUE,
        SENDER
    };
    
    public enum Order {
        PRIORITY,
        TIMESTAMP
    };
    
    public abstract Filter getFilterType();
    public abstract Order getOrderBy();
    public abstract String getFilterValue();
}
