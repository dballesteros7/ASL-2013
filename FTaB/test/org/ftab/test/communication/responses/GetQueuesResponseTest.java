/**
 * GetQueuesResponseTest.java
 * Created: 15.10.2013
 * Author: Diego
 */
package org.ftab.test.communication.responses;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.ByteBuffer;
import java.util.Set;
import java.util.TreeSet;

import org.ftab.communication.responses.GetQueuesResponse;
import org.junit.Test;

/**
 * Unit tests for the {@link GetQueuesResponse} class.
 */
public class GetQueuesResponseTest {

    /**
     * Check that we can serialize and de-serialize a {@link GetQueuesResponse}
     * object.
     */
    @Test
    public void testSerializing() {
        Set<String> uniqueQueues = new TreeSet<String>();
        uniqueQueues.add("OneQueue");
        uniqueQueues.add("ItsAMeQueue");
        uniqueQueues.add("anotherQueue");

        GetQueuesResponse gqr = new GetQueuesResponse(uniqueQueues);
        ByteBuffer serialized = gqr.toBytes();
        GetQueuesResponse echo = GetQueuesResponse.fromBytes(serialized);
        int size = 0;
        for (String queueName : echo.getQueues()) {
            assertTrue(uniqueQueues.contains(queueName));
            ++size;
        }
        assertEquals(size, uniqueQueues.size());
        uniqueQueues.add("Tricky" + GetQueuesResponse.SEPARATOR + "Queue" +
                GetQueuesResponse.SEPARATOR);
        gqr = new GetQueuesResponse(uniqueQueues);
        serialized = gqr.toBytes();
        echo = GetQueuesResponse.fromBytes(serialized);
        size = 0;
        for (String queueName : echo.getQueues()) {
            assertTrue(uniqueQueues.contains(queueName));
            ++size;
        }
        assertEquals(size, uniqueQueues.size());
    }

}
