/**
 * RetrieveMessageResponse.java
 * Created: 15.10.2013
 * Author: Diego
 */
package org.ftab.communication.responses;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.regex.Pattern;

import org.ftab.communication.ProtocolMessage;
import org.ftab.communication.requests.RetrieveMessageRequest;
import org.ftab.communication.requests.SendMessageRequest.Context;
import org.ftab.database.Message;

/**
 * Protocol response with a requested message. It is the response to a
 * {@link RetrieveMessageRequest}.
 */
public class RetrieveMessageResponse extends ProtocolMessage {

    /**
     * Separator string for serialization.
     */
    public final static String SEPARATOR = ";";
    /**
     * Escape string for serialization.
     */
    public final static String ESCAPE_CHAR = "&";
    /**
     * Cache of the Context possible values.
     */
    private final static Context[] CONTEXT_CACHE = Context.values();

    /**
     * Message identifier.
     */
    private final long messageId;
    /**
     * Message content.
     */
    private final String message;
    /**
     * Message sender.
     */
    private final String sender;
    /**
     * Message intended receiver, can be null.
     */
    private final String receiver;
    /**
     * Message containing queue.
     */
    private final String queueName;
    /**
     * Message priority.
     */
    private final int priority;
    /**
     * Message context.
     */
    private final Context context;

    /**
     * Create a response with the given information.
     * 
     * @param nMessageid
     *            message id.
     * @param nMessage
     *            message content.
     * @param nSender
     *            message sender.
     * @param nReceiver
     *            message receiver, if any.
     * @param nQueueName
     *            message containing queue name.
     * @param nPriority
     *            message priority.
     * @param nContext
     *            message context.
     */
    public RetrieveMessageResponse(long nMessageid, String nMessage,
            String nSender, String nReceiver, String nQueueName, int nPriority,
            Context nContext) {
        messageId = nMessageid;
        message = nMessage;
        sender = nSender;
        receiver = nReceiver;
        queueName = nQueueName;
        priority = nPriority;
        context = nContext;
        messageType = MessageType.RETURNED_MESSAGES;
    }

    /**
     * Create a message response from a database Message object.
     * 
     * @param msg
     *            database representation of a message.
     */
    public RetrieveMessageResponse(Message msg) {
        messageId = msg.getId();
        message = msg.getContent();
        sender = msg.getSender();
        receiver = msg.getReceiver();
        queueName = msg.getQueueName();
        priority = msg.getPriority();
        context = CONTEXT_CACHE[msg.getContext()];
        messageType = MessageType.RETURNED_MESSAGES;
    }

    /**
     * Get the message id.
     * 
     * @return message id.
     */
    public long getMessageId() {
        return messageId;
    }

    /**
     * Get the message content.
     * 
     * @return message content.
     */
    public String getMessageContent() {
        return message;
    }

    /**
     * Get the message sender.
     * 
     * @return message sender.
     */
    public String getSender() {
        return sender;
    }

    /**
     * Get the message receiver.
     * 
     * @return message receiver.
     */
    public String getReceiver() {
        return receiver;
    }

    /**
     * Indicates if the message has a valid receiver, it encapsulates the
     * definition of a valid receiver.
     * 
     * @return true if the receiver field is not null, false otherwise.
     */
    public boolean hasReceiver() {
        return receiver != null;
    }

    /**
     * Get the name of the containing queue.
     * 
     * @return name of the containing queue.
     */
    public String getQueue() {
        return queueName;
    }

    /**
     * Get the message priority.
     * 
     * @return message priority.
     */
    public int getPriority() {
        return priority;
    }

    /**
     * Get the context value.
     * 
     * @return message context.
     */
    public Context getContext() {
        return context;
    }

    /**
     * Retrieve a response given a ByteBuffer, this is defined in a way such
     * that X == fromBytes(X.toBytes()) holds.
     * 
     * @param input
     *            ByteBuffer generated from a {@link RetrieveMessageResponse}
     *            instance with the toBytes method.
     * @return response object.
     */
    public static RetrieveMessageResponse fromBytes(ByteBuffer input) {
        Charset charset = Charset.forName(CHARSET);
        String decodedString = charset.decode(input).toString();
        String[] tokens = decodedString.split(
                "(?<!" + Pattern.quote(ESCAPE_CHAR) + ")"
                        + Pattern.quote(SEPARATOR), 7);
        String candidateReceiver = tokens[3].replace(ESCAPE_CHAR + SEPARATOR,
                SEPARATOR);
        RetrieveMessageResponse instance = new RetrieveMessageResponse(
                Long.parseLong(tokens[0]), tokens[1].replace(ESCAPE_CHAR
                        + SEPARATOR, SEPARATOR), tokens[2].replace(ESCAPE_CHAR
                        + SEPARATOR, SEPARATOR),
                candidateReceiver.equals("") ? null : candidateReceiver,
                tokens[4].replace(ESCAPE_CHAR + SEPARATOR, SEPARATOR),
                Integer.parseInt(tokens[5]), Context.valueOf(tokens[6]));
        return instance;
    }

    @Override
    public ByteBuffer toBytes() {
        String escapedMessage = message.replaceAll(SEPARATOR, ESCAPE_CHAR
                + SEPARATOR);
        String escapedReceiver = (hasReceiver() ? receiver : "").replaceAll(
                SEPARATOR, ESCAPE_CHAR + SEPARATOR);
        String escapedSender = sender.replaceAll(SEPARATOR, ESCAPE_CHAR
                + SEPARATOR);
        String escapedQueue = queueName.replaceAll(SEPARATOR, ESCAPE_CHAR
                + SEPARATOR);
        String serialized = messageId + SEPARATOR + escapedMessage + SEPARATOR
                + escapedSender + SEPARATOR + escapedReceiver + SEPARATOR
                + escapedQueue + SEPARATOR + priority + SEPARATOR
                + context.toString();
        Charset encoder = Charset.forName(CHARSET);
        ByteBuffer bb = encoder.encode(serialized);
        return bb;
    }

}
