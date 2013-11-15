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
     * @aslincludefields
     */
    public enum Status {
    	/**
         * Marks that the call generating this request was a success.
         */
        SUCCESS, 
        
        /**
         * Marks that the call generating this request failed for a unexpected
         * reason.
         */
        EXCEPTION, 
        
        /**
         * Marks that the connection call failed due to the server being
         * at full capcacity
         */
        FULL_SERVER, 
        
        /**
         * Marks that a call requesting queues with messages waiting resulted
         * in no queues.
         */
        NO_QUEUE, 
        
        /**
         * Marks that the call requesting to login a user failed due to the 
         * user already being online.
         */
        USER_ONLINE,
        
        /**
         * Marks that the call requesting the deletion of a queue failed due
         * to the queue not being empty.
         */
        QUEUE_NOT_EMPTY, 
        
        /**
         * Marks that in a call involving a client, the specified client does
         * not exist in the system.
         */
        NO_CLIENT, 
        
        /**
         * Marks that for a call to retrieve a message, no messages were found. 
         */
        NO_MESSAGE, 
        
        /**
         * Marks that the call to create a queue failed due to the queue already
         * existing.
         */
        QUEUE_EXISTS, 
        
        /**
         * Marks that in a call involving a queue, the specified queue does not
         * exist in the system. 
         */
        QUEUE_NOT_EXISTS
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
