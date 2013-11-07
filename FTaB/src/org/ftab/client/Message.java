/**
 * Message.java
 * Created: Oct 10, 2013
 * Author: Diego Ballesteros (diegob)
 */
package org.ftab.client;

import org.ftab.communication.requests.SendMessageRequest.Context;

/**
 * Encapsulates a message pulled from a single queue
 */
public class Message {

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
    private final String queueName;

    /**
     * Context of the message (i.e. None, request, response).
     */
    private final Context context;

    /**
     * Priority of the message.
     */
    private final short priority;

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
     * @param nQueueName
     *            name of the queue where the message resides.
     * @param nReceiver
     *            client that is supposed to receive the message.
     */
    public Message(long nId, Context nContext, short nPrio, String nContent,
            String nSender, String nQueueName, String nReceiver) {
        id = nId;
        content = nContent;
        context = nContext;
        priority = nPrio;
        sender = nSender;
        queueName = nQueueName;
        receiver = nReceiver;
    }

    /**
     * Getter for the id field.
     * 
     * @return message's id.
     */
    public long getId() {
        return id;
    }

    /**
     * Getter for the context field.
     * 
     * @return message's context.
     */
    public Context getContext() {
        return context;
    }

    /**
     * Getter for the priority field.
     * 
     * @return message's priority.
     */
    public short getPriority() {
        return priority;
    }

    /**
     * Getter for the content field.
     * 
     * @return message's content.
     */
    public String getContent() {
        return content;
    }

    /**
     * Getter for the sender field.
     * 
     * @return message's sender.
     */
    public String getSender() {
        return sender;
    }

    /**
     * Getter for the queue name.
     * 
     * @return message's queue name.
     */
    public String getQueueName() {
        return queueName;
    }

    /**
     * Getter for the receiver field.
     * 
     * @return message's receiver.
     */
    public String getReceiver() {
        return receiver;
    }

}
