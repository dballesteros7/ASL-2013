/**
 * RequestResponseTest.java
 * Created: 15.10.2013
 * Author: Diego
 */
package org.ftab.test.communication.responses;

import static org.junit.Assert.assertEquals;

import java.nio.ByteBuffer;

import org.ftab.communication.responses.RequestResponse;
import org.ftab.communication.responses.RequestResponse.Status;
import org.junit.Test;

/**
 * Unit tests for the {@link RequestResponse} class.
 */
public class RequestResponseTest {

    /**
     * Check that we can serialize and de-serialize a {@link RequestResponse}
     * object.
     */
    @Test
    public void testSerializing() {
        RequestResponse rr = new RequestResponse(Status.EXCEPTION,
                "This is a dummy exception!");
        ByteBuffer serialized = rr.toBytes();
        RequestResponse echo = RequestResponse.fromBytes(serialized);
        assertEquals(rr.getMessageType(), echo.getMessageType());
        assertEquals(rr.getDescription(), echo.getDescription());
        assertEquals(rr.getStatus(), echo.getStatus());

        rr = new RequestResponse(Status.FULL_SERVER,
                "This is a tricky exception with " + RequestResponse.SEPARATOR +
                        " string.");
        serialized = rr.toBytes();
        echo = RequestResponse.fromBytes(serialized);
        assertEquals(rr.getMessageType(), echo.getMessageType());
        assertEquals(rr.getDescription(), echo.getDescription());
        assertEquals(rr.getStatus(), echo.getStatus());
    }

    /**
     * Check that we can use all the class constructors and later retrieve the
     * arguments through the public getters.
     */
    @Test
    public void testBuildRequestResponse() {
        for (Status possibleStatus : Status.values()) {
            RequestResponse rr = new RequestResponse(possibleStatus);
            assertEquals(rr.getStatus(), possibleStatus);
            assertEquals(rr.getDescription(), "");
            rr = new RequestResponse(possibleStatus,
                    "This is a description for: " + possibleStatus.toString());
            assertEquals(rr.getStatus(), possibleStatus);
            assertEquals(rr.getDescription(), "This is a description for: " +
                    possibleStatus.toString());
        }

    }

}
