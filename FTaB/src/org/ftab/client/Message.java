/**
 * Message.java
 * Created: Oct 10, 2013
 * Author: Diego Ballesteros (diegob)
 */
package org.ftab.client;

import java.util.ArrayList;


/**
 * Encapsulates a message that can be passed from client to server
 * or vice-versa. 
 */
public class Message {
	/**
	 * Constant representing the ID for a message with no ID.
	 */
	private static final int NO_ID = -1;
	
    /**
     * Id of the message in the database.
     */
    private final long id;

    /**
     * Username of the client that sent this message.
     */
    private final String sender;

    /**
     * Username of the client that is meant to receive this message, it may be
     * null.
     */
    private final String receiver;

    /**
     * Name of the queue from which this message was retrieved.
     */
    private final ArrayList<String> queues = new ArrayList<String>();

    /**
     * Context of the message
     */
    private final int context;

    /**
     * Priority of the message.
     */
    private final byte priority;

    /**
     * Body of the message.
     */
    private final String content;

    /**
     * Creates a message object with the given information, all parameters
     * except receiver are expected to be not-null and reference valid
     * information in the database.
     * 
     * @param nId
     *            id of the message.
     * @param nContext
     *            context of the message.
     * @param nPrio
     *            priority of the message.
     * @param nContent
     *            content of the message.
     * @param nSender
     *            client that sent the message.
     * @param nReceiver
     *            client that is supposed to receive the message.
     * @param nQueues
     *            the name of the queue or queues on which the message resided or is to be sent to
     */
    public Message(long nId, int nContext, byte nPrio, String nContent,
            String nSender, String nReceiver, String... nQueues) {
        id = nId;
        content = nContent;
        context = nContext;
        priority = nPrio;
        sender = nSender;
        for (String q : nQueues) this.queues.add(q);
        receiver = nReceiver;
    }

    /**
     * Creates a message object with the given information, all parameters
     * except receiver are expected to be not-null and reference valid
     * information in the database.
     * 
     * @param nContext
     *            context of the message.
     * @param nPrio
     *            priority of the message.
     * @param nContent
     *            content of the message.
     * @param nSender
     *            client that sent the message.
     * @param nReceiver
     *            client that is supposed to receive the message.
     * @param nQueues
     *            the name of the queue or queues on which the message resided or is to be sent to
     */
    public Message(int nContext, byte nPrio, String nContent,
            String nSender, String nReceiver, String... nQueues) {
    	id = Message.NO_ID;
        content = nContent;
        context = nContext;
        priority = nPrio;
        sender = nSender;
        for (String q : nQueues) this.queues.add(q);
        receiver = nReceiver;
    }
    
    /**
     * Gets the identification number of the message in
     * the system. 
     * @return The message's identification number or or <b>-1</b> 
     * if it was not set
     */
    public long getId() {
        return id;
    }

    /**
     * Gets the context of this message, where <b>0</b> represents
     * no context. 
     * @return The message's context.
     */
    public int getContext() {
        return context;
    }

    /**
     * Gets the priority of this message.
     * @return The message's priority from 1 to 10, 10 being
     * the highest
     */
    public byte getPriority() {
        return priority;
    }

    /**
     * Gets the content of this message.
     * @return This message's content.
     */
    public String getContent() {
        return content;
    }

    /**
     * Gets the username of the sender of this message.
     * @return The username of this message's sender.
     */
    public String getSender() {
        return sender;
    }

    /**
     * Gets the names of the queues this message either is to be 
     * sent to or the name of the queue it was retrieved from.
     * @return A list of queue names.
     */
    public Iterable<String> getQueues() {
		return queues;
	}

    /**
     * Gets the designated receiver of this message.
     * @return This message's receiver or null if none was
     * specified.
     */
    public String getReceiver() {
        return receiver;
    }

    @Override
    public String toString() {
    	StringBuilder stringRep = new StringBuilder();
    	
    	if (this.getId() != Message.NO_ID) {
    		stringRep.append(String.format("ID: %d \t", this.getId()));
    	}
    	
    	if (this.getReceiver() != null) {
    		stringRep.append(String.format("Sender: %s \t Receiver: %s \n", this.getSender(), this.getReceiver()));
    	} else {
    		stringRep.append(String.format("Sender: %s \n", this.getSender()));
    	}
    	
    	stringRep.append(String.format("Priority: %d \t Context: %d \n", this.getPriority(), this.getContext()));
    	
    	stringRep.append(String.format("Queues: %s \n", this.queues.toString()));
    	
    	stringRep.append(String.format("\n%s", this.getContent()));
    			
    	return stringRep.toString();
    }
    
    /**
     * Gets a summary of the details of the message
     * @return A string summarising the contents of this messages
     * @aslexclude
     */
    public String getSummary() {
    	return String.format("Message from %s to %s on %d queues with priority %d, context %d and %d characters",
    			this.getSender(), (this.getReceiver() == null) ? "no one" : this.getReceiver(), this.queues.size(),
    					this.getPriority(), this.getContext(), this.getContent().length());
    }
}
