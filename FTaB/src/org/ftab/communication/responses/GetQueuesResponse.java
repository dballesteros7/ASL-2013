/**
 * GetQueuesResponse.java
 * Created: 15.10.2013
 * Author: Diego
 */
package org.ftab.communication.responses;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.ftab.communication.ProtocolMessage;
import org.ftab.communication.requests.GetQueuesRequest;

/**
 * Protocol response after a {@link GetQueuesRequest}.
 */
public class GetQueuesResponse extends ProtocolMessage {

    /**
     * Separator string for serialization.
     */
    public static final String SEPARATOR = ";";
    /**
     * Escaping string for serialization.
     */
    public static final String ESCAPE_CHAR = "&";

    /**
     * List of queue names with messages.
     */
    private final List<String> queues;

    /**
     * Build a response given a container with queue names.
     * 
     * @param nQueues
     *            {@link Iterable} collection with the queue names.
     */
    public GetQueuesResponse(Iterable<String> nQueues) {
        queues = new ArrayList<String>();
        for (String queueName : nQueues) {
            queues.add(queueName);
        }
        messageType = MessageType.RETURNED_QUEUE;
    }

    /**
     * Get an {@link Iterable} container with the queue names.
     * 
     * @return collection of queue names.
     */
    public Iterable<String> getQueues() {
        return queues;
    }

    /**
     * Retrieve a response given a ByteBuffer, this is defined in a way such
     * that X == fromBytes(X.toBytes()) holds.
     * 
     * @param input
     *            ByteBuffer generated from a {@link GetQueuesResponse} instance
     *            with the toBytes method.
     * @return response object.
     */
    public static GetQueuesResponse fromBytes(ByteBuffer input) {
        Charset charset = Charset.forName(CHARSET);
        String decodedString = charset.decode(input).toString();
        String[] tokens = decodedString.split("(?<!"
                + Pattern.quote(ESCAPE_CHAR) + ")" + Pattern.quote(SEPARATOR));
        ArrayList<String> queueNames = new ArrayList<String>();
        for (String queueName : tokens) {
            queueNames.add(queueName.replaceAll(ESCAPE_CHAR + SEPARATOR,
                    SEPARATOR));
        }
        GetQueuesResponse instance = new GetQueuesResponse(queueNames);
        return instance;
    }

    @Override
    public ByteBuffer toBytes() {
        String serializedList = "";
        for (String queueName : queues) {
            String escaped = queueName.replaceAll(SEPARATOR, ESCAPE_CHAR
                    + SEPARATOR);
            serializedList += escaped;
            serializedList += SEPARATOR;
        }
        serializedList = serializedList.substring(0, serializedList.length()
                - SEPARATOR.length());
        Charset encoder = Charset.forName(CHARSET);
        ByteBuffer bb = encoder.encode(serializedList);
        return bb;
    }

}
