/**
 * RetrieveMessageResponseTest.java
 * Created: 15.10.2013
 * Author: Diego
 */
package org.ftab.test.communication.responses;

import static org.junit.Assert.assertEquals;

import java.nio.ByteBuffer;

import org.ftab.communication.requests.SendMessageRequest.Context;
import org.ftab.communication.responses.RetrieveMessageResponse;
import org.ftab.database.Message;
import org.junit.Test;

/**
 * Unit tests for the {@link RetrieveMessageResponse} class.
 */
public class RetrieveMessageResponseTest {

    /**
     * Check that we can serialize and de-serialize a
     * {@link RetrieveMessageResponse} object.
     */
    @Test
    public void testSerializing() {
        // With receiver
        Message msg = new Message(Long.MAX_VALUE, (short) 0, (short) 10,
                "This is an easy message, Hi Mom!", "NationalBroadcaster",
                (int) System.currentTimeMillis() / 1000, "QueueTest", 1,
                "ConcernedCitizen");
        RetrieveMessageResponse rmr = new RetrieveMessageResponse(msg);
        ByteBuffer serialized = rmr.toBytes();
        RetrieveMessageResponse echo = RetrieveMessageResponse
                .fromBytes(serialized);
        compareMessages(rmr, echo);

        // With null receiver
        msg = new Message(Long.MAX_VALUE, (short) 0, (short) 10,
                "This is an easy message, Hi Mom!", "NationalBroadcaster",
                (int) System.currentTimeMillis() / 1000, "QueueTest", 1, null);
        rmr = new RetrieveMessageResponse(msg);
        serialized = rmr.toBytes();
        echo = RetrieveMessageResponse.fromBytes(serialized);
        compareMessages(rmr, echo);

        // With tricky strings
        msg = new Message(Long.MAX_VALUE, (short) 0, (short) 10,
                "This is a tricky " + RetrieveMessageResponse.SEPARATOR
                        + "  message, Hi Mom!", "NationalBroadcaster",
                (int) System.currentTimeMillis() / 1000,
                RetrieveMessageResponse.SEPARATOR + "QueueTest"
                        + RetrieveMessageResponse.SEPARATOR, 1, "Citizen");
        rmr = new RetrieveMessageResponse(msg);
        serialized = rmr.toBytes();
        echo = RetrieveMessageResponse.fromBytes(serialized);
        compareMessages(rmr, echo);

        // With tricky strings
        msg = new Message(Long.MAX_VALUE, (short) 0, (short) 10,
                "This is a tricky " + RetrieveMessageResponse.SEPARATOR
                        + "  message, Hi Mom!", "NationalBroadcaster",
                (int) System.currentTimeMillis() / 1000,
                RetrieveMessageResponse.SEPARATOR + "QueueTest"
                        + RetrieveMessageResponse.SEPARATOR, 1, null);
        rmr = new RetrieveMessageResponse(msg);
        serialized = rmr.toBytes();
        echo = RetrieveMessageResponse.fromBytes(serialized);
        compareMessages(rmr, echo);
    }

    /**
     * Check that we can build responses and retrieve the message information
     * from it.
     */
    @Test
    public void testConstructor() {
        RetrieveMessageResponse rmr = new RetrieveMessageResponse(
                Long.MIN_VALUE, "Hey, I am a message", "I sent it",
                "I receive it", "To this queue", 10, Context.RESPONSE);
        assertEquals(rmr.getMessageId(), Long.MIN_VALUE);
        assertEquals(rmr.getMessageContent(), "Hey, I am a message");
        assertEquals(rmr.getSender(), "I sent it");
        assertEquals(rmr.getReceiver(), "I receive it");
        assertEquals(rmr.getQueue(), "To this queue");
        assertEquals(rmr.getPriority(), 10);
        assertEquals(rmr.getContext(), Context.RESPONSE);
    }

    /**
     * Compare two messages contained in different responses by all the relevant
     * attributes.
     * 
     * @param a
     *            one response.
     * @param b
     *            another response.
     */
    private void compareMessages(RetrieveMessageResponse a,
            RetrieveMessageResponse b) {
        assertEquals(a.getMessageId(), b.getMessageId());
        assertEquals(a.getMessageContent(), b.getMessageContent());
        assertEquals(a.getSender(), b.getSender());
        assertEquals(a.getReceiver(), b.getReceiver());
        assertEquals(a.hasReceiver(), b.hasReceiver());
        assertEquals(a.getQueue(), b.getQueue());
        assertEquals(a.getPriority(), b.getPriority());
        assertEquals(a.getContext(), b.getContext());
    }
}
