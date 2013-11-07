/**
 * MessageQueue.java
 * Created: Oct 10, 2013
 * Author: Diego Ballesteros (diegob)
 */
package org.ftab.server;

/**
 * Class that represents a queue in the system and includes minimal information
 * about it, namely the unique name and the current number of messages in it.
 */
public class Queue {

    /**
     * Name of the queue.
     */
    private final String name;
    /**
     * Number of messages in the queue.
     */
    private final long messageCount;

    /**
     * Creates a MessageQueue object with the given name and message count.
     * 
     * @param name
     *            name of the queue.
     * @param messageCount
     *            current number of messages in the queue.
     */
    public Queue(final String nName, final long nMessageCount) {
        name = nName;
        messageCount = nMessageCount;
    }

    /**
     * Retrieve the name of the queue.
     * 
     * @return the queue name.
     */
    public String getName() {
        return name;
    }

    /**
     * Retrieve the number of messages in the queue.
     * 
     * @return the message count.
     */
    public long getMessageCount() {
        return messageCount;
    }

}
