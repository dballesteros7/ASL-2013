/**
 * Message.java
 * Created: Oct 10, 2013
 * Author: Diego Ballesteros (diegob)
 */
package org.ftab.database;

/**
 * Class representing a message in the system with information extracted from
 * the database, suitable for encapsulating results from queries on the message
 * table.
 */
public class Message {

    /**
     * Id of the message in the database.
     */
    private final long id;

    /**
     * Id of the queue associated with this message, note that in the database
     * the message can be associated to many queues but the current object
     * refers to only one of those records.
     */
    private final long queueId;

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
    private final int context;

    /**
     * Priority of the message.
     */
    private final short priority;

    /**
     * Time where the message was created in the database in seconds since
     * epoch.
     */
    private final int createTime;

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
     * @param nCreateTime
     *            time when the message was created.
     * @param nQueueName
     *            name of the queue where the message resides.
     * @param nQueueId
     *            id of the queue where the message resides.
     * @param nReceiver
     *            client that is supposed to receive the message.
     */
    public Message(long nId, int nContext, short nPrio, String nContent,
            String nSender, int nCreateTime, String nQueueName, long nQueueId,
            String nReceiver) {
        id = nId;
        content = nContent;
        context = nContext;
        priority = nPrio;
        sender = nSender;
        createTime = nCreateTime;
        queueName = nQueueName;
        queueId = nQueueId;
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
    public int getContext() {
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
     * Getter for the create time field.
     * 
     * @return message's creation timestamp.
     */
    public int getCreateTime() {
        return createTime;
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
     * Getter for the queue id.
     * 
     * @return message's queue id.
     */
    public long getQueueId() {
        return queueId;
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
