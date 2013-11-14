/**
 * RequestResponse.java
 * Created: 15.10.2013
 * Author: Diego
 */
package org.ftab.communication.responses;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import org.ftab.communication.ProtocolMessage;

/**
 * Encapsulates a general response message with flags for operation success or failure.
 * Also contains any details of the failure such as system specific exceptions.
 */
public class RequestResponse extends ProtocolMessage {

    /**
     * Separator string for serialization.
     */
    public final static String SEPARATOR = ";";

    /**
     * Enum documenting the possible statuses a RequestResponse message can contain. Included
     * are statuses for success, exception, full server, no applicable queues, user already online, 
     * queue not empty, no client, no message, queue already exits, queue does not exist. 
     */
    public enum Status {
        SUCCESS, EXCEPTION, FULL_SERVER, NO_QUEUE, USER_ONLINE,
        QUEUE_NOT_EMPTY, NO_CLIENT, NO_MESSAGE, QUEUE_EXISTS, QUEUE_NOT_EXISTS
    };

    /**
     * Status of the response.
     */
    private final Status responseStatus;
    /**
     * Additional description for the response.
     */
    private final String responseDescription;

    /**
     * Build a response with no additional description.
     * 
     * @param nStatus
     *            status of the response.
     */
    public RequestResponse(Status nStatus) {
        responseStatus = nStatus;
        responseDescription = "";
        messageType = MessageType.REQUEST_RESPONSE;
    }

    /**
     * Build a response with additional information.
     * 
     * @param nStatus
     *            status of the response.
     * @param description
     *            additional description information.
     */
    public RequestResponse(Status nStatus, String description) {
        responseStatus = nStatus;
        responseDescription = description;
        messageType = MessageType.REQUEST_RESPONSE;
    }

    /**
     * Gets the enumerated value pertaining to the status of
     * the response.
     * @return The response status.
     */
    public Status getStatus() {
        return responseStatus;
    }

    /**
     * Gets the description of the response, if any.
     * 
     * @return The response description.
     */
    public String getDescription() {
        return responseDescription;
    }

    /**
     * Retrieve a response given a ByteBuffer, this is defined in a way such
     * that X == fromBytes(X.toBytes()) holds.
     * 
     * @param input
     *            ByteBuffer generated from a {@link RequestResponse} instance
     *            with the toBytes method.
     * @return response object.
     * @aslexclude
     */
    public static RequestResponse fromBytes(ByteBuffer input) {
        Charset charset = Charset.forName(CHARSET);
        String decodedString = charset.decode(input).toString();
        String[] tokens = decodedString.split(SEPARATOR, 2);
        return new RequestResponse(Status.valueOf(tokens[0]), tokens[1]);
    }

    @Override
    public ByteBuffer toBytes() {
        Charset charset = Charset.forName(CHARSET);
        ByteBuffer bb = charset.encode(getStatus().toString() + SEPARATOR
                + getDescription());
        return bb;
    }

}
