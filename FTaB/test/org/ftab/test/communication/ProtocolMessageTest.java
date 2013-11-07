package org.ftab.test.communication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

import org.ftab.communication.ProtocolMessage;
import org.ftab.communication.ProtocolMessage.MessageType;
import org.ftab.communication.exceptions.InvalidHeaderException;
import org.ftab.communication.requests.ConnectionRequest;
import org.ftab.communication.requests.GetQueuesRequest;
import org.ftab.communication.requests.MessageReceivedRequest;
import org.ftab.communication.requests.QueueModificationRequest;
import org.ftab.communication.requests.RetrieveMessageRequest;
import org.ftab.communication.requests.RetrieveMessageRequest.Filter;
import org.ftab.communication.requests.RetrieveMessageRequest.Order;
import org.ftab.communication.requests.SendMessageRequest;
import org.ftab.communication.requests.SendMessageRequest.Context;
import org.junit.Test;

/**
 * Unit tests for methods and functions of the {@link org.ftab.communication.ProtocolMessage#ProtocolMessage ProtocolMessage} 
 * class.
 * @author Jean-Pierre Smith
 *
 */
public class ProtocolMessageTest {

	/**
	 * Tests that the {@link org.ftab.communication.exceptions.InvalidHeaderException#InvalidHeaderException 
	 * InvalidHeaderException} is thrown when the array has an invalid size.  
	 * @throws Exception
	 */
	@Test(expected=InvalidHeaderException.class)
	public void testInvalidLengthGetBodySize() throws Exception{
		byte[] invalidLength = { -86, 86, -86, 86, 45, 72 };
		
		ProtocolMessage.getBodySize(ByteBuffer.wrap(invalidLength));
	}

	/**
	 * Tests that the {@link org.ftab.communication.exceptions.InvalidHeaderException#InvalidHeaderException 
	 * InvalidHeaderException} is thrown when the array does not have the starting sequence.  
	 * @throws Exception
	 */
	@Test(expected=InvalidHeaderException.class)
	public void testStartingSequenceGetBodySize() throws Exception{
		byte[] invalidStart = { 17, -86, 86, -86, 86, 45, 72, -86 };
		
		ProtocolMessage.getBodySize(ByteBuffer.wrap(invalidStart));
	}
	
	/**
	 * Tests that the length returned is the correct length
	 */
	@Test
	public void testGetBodySizeReturn() {
		ByteBuffer buffer = ByteBuffer.wrap(new byte[] { -86, 86, -86, 86,  0, 0, 0, 0});
		
		// Set the buffer up for reading
		buffer.position(0);
		
		try {
			int value = ProtocolMessage.getBodySize(buffer);
			assertEquals(0, value);
			
			buffer.position(ProtocolMessage.HEADER_SIZE - ProtocolMessage.START_MESSAGE.length);
			buffer.putInt(95);
			buffer.flip();	
			
			value = ProtocolMessage.getBodySize(buffer);
			assertEquals(95, value);
			
			buffer.position(ProtocolMessage.HEADER_SIZE - ProtocolMessage.START_MESSAGE.length);
			buffer.putInt(10234);
			buffer.flip();
			
			value = ProtocolMessage.getBodySize(buffer);
			assertEquals(10234, value);
			
			buffer.position(ProtocolMessage.HEADER_SIZE - ProtocolMessage.START_MESSAGE.length);
			buffer.putInt(2147483647);
			buffer.flip();
			
			value = ProtocolMessage.getBodySize(buffer);
			assertEquals(2147483647, value);
			
		} catch (InvalidHeaderException e) {
			fail("Exception was thrown on correct input.");
		}
	}
	
	/**
	 * Tests whether Connection requests can be properly serialised and deserialised
	 */
	@Test
	public void testConnectionRequestToFromBytes() {
		// Disconnection tests
		final ConnectionRequest disRequest = new ConnectionRequest(null, false);
		final ConnectionRequest disRequest2 = new ConnectionRequest("jim", false);
		ConnectionRequest transRequest;
		
		transRequest = (ConnectionRequest) ProtocolMessage.fromBytes((ByteBuffer) 
				ProtocolMessage.toBytes(disRequest).position(ProtocolMessage.HEADER_SIZE));
		
		assertEquals(disRequest.getUsername(), transRequest.getUsername());
		assertEquals(disRequest.isConnection(), transRequest.isConnection());
		
		transRequest = (ConnectionRequest) ProtocolMessage.fromBytes((ByteBuffer) 
				ProtocolMessage.toBytes(disRequest2).position(ProtocolMessage.HEADER_SIZE));
		
		assertEquals(disRequest2.getUsername(), transRequest.getUsername());
		assertEquals(disRequest2.isConnection(), transRequest.isConnection());
	}
	
	/**
	 * Tests whether Queue requests can be properly serialised and deserialised
	 */
	@Test
	public void testQueueRequestToFromBytes() {
		final GetQueuesRequest req = new GetQueuesRequest();
		GetQueuesRequest transRequest;
		
		transRequest = (GetQueuesRequest) ProtocolMessage.fromBytes((ByteBuffer) 
				ProtocolMessage.toBytes(req).position(ProtocolMessage.HEADER_SIZE));
		
		assertEquals(MessageType.RETRIEVE_QUEUES, transRequest.getMessageType());		
	}
	
	/**
	 * Tests whether Queue requests can be properly serialised and deserialised
	 */
	@Test
	public void testMessageReceivedRequestToFromBytes() {
		final MessageReceivedRequest req = new MessageReceivedRequest(9238108l, "hellokity", false);
		final MessageReceivedRequest req2 = new MessageReceivedRequest(9238108l, "hellokity", true);
		MessageReceivedRequest result;
		
		result = (MessageReceivedRequest) ProtocolMessage.fromBytes((ByteBuffer) 
				ProtocolMessage.toBytes(req).position(ProtocolMessage.HEADER_SIZE));
		
		assertEquals(req.getMessageId(), result.getMessageId());
		assertEquals(req.getQueue(), result.getQueue());
		assertEquals(req.isPop(), result.isPop());
		
		result = (MessageReceivedRequest) ProtocolMessage.fromBytes((ByteBuffer) 
				ProtocolMessage.toBytes(req2).position(ProtocolMessage.HEADER_SIZE));
		
		assertEquals(req2.getMessageId(), result.getMessageId());
		assertEquals(req2.getQueue(), result.getQueue());
		assertEquals(req2.isPop(), result.isPop());
	}
	
	/**
	 * Tests whether queue modification requeuests can be properly serialised and
	 * deserialised
	 */
	@Test
	public void testQueueModificationRequestToFromBytes() {
		final QueueModificationRequest request = new QueueModificationRequest("queue name", true);
		final QueueModificationRequest request2 = new QueueModificationRequest("queue name two", false);
		
		QueueModificationRequest result;
		
		result = (QueueModificationRequest) ProtocolMessage.fromBytes((ByteBuffer) 
				ProtocolMessage.toBytes(request).position(ProtocolMessage.HEADER_SIZE));
		
		assertEquals(request.isDelete(), result.isDelete());
		assertEquals(request.getQueueName(), result.getQueueName());
		
		result = (QueueModificationRequest) ProtocolMessage.fromBytes((ByteBuffer) 
				ProtocolMessage.toBytes(request2).position(ProtocolMessage.HEADER_SIZE));
		
		assertEquals(request2.isDelete(), result.isDelete());
		assertEquals(request2.getQueueName(), result.getQueueName());		
	}
	
	/**
	 * Tests whether retreive message requests can be properly serialised and deserialised
	 */
	@Test
	public void testRetrieveMessageRequestToFromBytes() {
		for (String name : new String[] { "name1", "name 2", "name 3" }) {
			for (Filter f : Filter.values()) {
				for (Order o : Order.values()) {
					RetrieveMessageRequest request = new RetrieveMessageRequest(name, f, o);
					
					RetrieveMessageRequest result = (RetrieveMessageRequest) ProtocolMessage.fromBytes((ByteBuffer) 
							ProtocolMessage.toBytes(request).position(ProtocolMessage.HEADER_SIZE));
					
					assertEquals(request.getFilterValue(), result.getFilterValue());
					assertEquals(request.getFilterType(), result.getFilterType());
					assertEquals(request.getOrderBy(), result.getOrderBy());
				}
			}
		}		
	}
	
	/**
	 * Tests whether send message requests can be properly serialised and deserialised
	 */
	@Test
	public void testSendMessageRequestToFromBytes() {
		for (Context context : Context.values()) {
			SendMessageRequest request1 = new SendMessageRequest("A random array of characters...", (byte)5, context,
					Arrays.asList("queue 1", "queue 2", "pipe queue 3", "piped turnover 4"));
			SendMessageRequest request2 = new SendMessageRequest("A random array...", (byte)3, context,
					Arrays.asList("pipe queue 3", "piped turnover 4"), "bob");
			SendMessageRequest request3 = new SendMessageRequest("", (byte)10, context,
					Arrays.asList("queue 1", "queue 2", "pipe queue 3", "piped turnover 4"), "piepd piper");
			
			SendMessageRequest result;
			
			result = (SendMessageRequest) ProtocolMessage.fromBytes((ByteBuffer) 
					ProtocolMessage.toBytes(request1).position(ProtocolMessage.HEADER_SIZE));
			
			assertEquals(request1.getMessage(), result.getMessage());
			assertEquals(request1.getPriority(), result.getPriority());
			assertEquals(request1.getContext(), result.getContext());
			assertEquals(request1.getReceiver(), result.getReceiver());
			assertEquals(request1.hasReceiver(), result.hasReceiver());
			
			List<String> reqList = (List<String>) request1.getQueueList();
			List<String> resultList = (List<String>) result.getQueueList();
			
			assertEquals(reqList.size(), resultList.size());
			for (int i = 0; i < reqList.size(); i++) {
				assertEquals(reqList.get(i), resultList.get(i));
			}
			
			
			result = (SendMessageRequest) ProtocolMessage.fromBytes((ByteBuffer) 
					ProtocolMessage.toBytes(request2).position(ProtocolMessage.HEADER_SIZE));
			
			assertEquals(request2.getMessage(), result.getMessage());
			assertEquals(request2.getPriority(), result.getPriority());
			assertEquals(request2.getContext(), result.getContext());
			assertEquals(request2.getReceiver(), result.getReceiver());
			assertEquals(request2.hasReceiver(), result.hasReceiver());
			
			reqList = (List<String>) request2.getQueueList();
			resultList = (List<String>) result.getQueueList();
			
			assertEquals(reqList.size(), resultList.size());
			for (int i = 0; i < reqList.size(); i++) {
				assertEquals(reqList.get(i), resultList.get(i));
			}
			
			
			result = (SendMessageRequest) ProtocolMessage.fromBytes((ByteBuffer) 
					ProtocolMessage.toBytes(request3).position(ProtocolMessage.HEADER_SIZE));
			
			assertEquals(request3.getMessage(), result.getMessage());
			assertEquals(request3.getPriority(), result.getPriority());
			assertEquals(request3.getContext(), result.getContext());
			assertEquals(request3.getReceiver(), result.getReceiver());
			assertEquals(request3.hasReceiver(), result.hasReceiver());
			
			reqList = (List<String>) request1.getQueueList();
			resultList = (List<String>) result.getQueueList();
			
			assertEquals(reqList.size(), resultList.size());
			for (int i = 0; i < reqList.size(); i++) {
				assertEquals(reqList.get(i), resultList.get(i));
			}
			
			
		}		
	}
}


