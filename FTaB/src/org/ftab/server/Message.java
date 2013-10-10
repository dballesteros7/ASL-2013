/**
 * Message.java
 * Created: Oct 10, 2013
 * Author: Diego Ballesteros (diegob)
 */
package org.ftab.server;

/**
 * 
 */
public class Message {

    private final long id;

    private final short context;

    private final short priority;

    private final String content;

    private final String sender;

    private final int createTime;

    private final String queueName;

    private final long queueId;

    private final String receiver;

    public Message(long nId, short nContext, short nPrio, String nContent,
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
     * @return the id
     */
    public long getId() {
        return id;
    }

    /**
     * @return the context
     */
    public short getContext() {
        return context;
    }

    /**
     * @return the priority
     */
    public short getPriority() {
        return priority;
    }

    /**
     * @return the content
     */
    public String getContent() {
        return content;
    }

    /**
     * @return the sender
     */
    public String getSender() {
        return sender;
    }

    /**
     * @return the createTime
     */
    public int getCreateTime() {
        return createTime;
    }

    /**
     * @return the queueName
     */
    public String getQueueName() {
        return queueName;
    }

    /**
     * @return the queueId
     */
    public long getQueueId() {
        return queueId;
    }

    /**
     * @return the receiver
     */
    public String getReceiver() {
        return receiver;
    }

}
