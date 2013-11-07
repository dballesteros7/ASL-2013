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
 * Protocol response with no detailed content. It can be of several types
 * depending on the outcome of the originating request.
 */
public class RequestResponse extends ProtocolMessage {

    /**
     * Separator string for serialization.
     */
    public final static String SEPARATOR = ";";

    /**
     * Set of possible status of the response.
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
     * Get the response status
     * 
     * @return response status.
     */
    public Status getStatus() {
        return responseStatus;
    }

    /**
     * Get the response description, if any.
     * 
     * @return response description.
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
