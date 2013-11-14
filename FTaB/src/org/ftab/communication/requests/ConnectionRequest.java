/**
 * ConnectionRequest.java
 * Created: 14.10.2013
 * Author: Diego
 */
package org.ftab.communication.requests;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import org.ftab.communication.ProtocolMessage;

/**
 * Encapsulates a client's connection or disconnection request.
 */
public class ConnectionRequest extends ProtocolMessage {
    /**
     * The username of the client making this reqeust
     */
	private String username = null;
	
	/**
	 * Whether the request is a connection or disconnection
	 */
	private boolean isConnection = false;
    
	/**
	 * Creates a new connection or disconnection request for the client with the supplied 
	 * username.
	 * @param username The username for this client requesting the connection. This
	 * parameter is not required if it is a disconnection.
	 * @param connect True for a connection request, false for a disconnection request.
	 */
    public ConnectionRequest(String username, boolean connect) {
    	this.messageType = MessageType.CONNECTION_REQUEST;
    	
    	if (connect) {
    		this.username = username;
    		isConnection = true;
    	}
    }
    
	/**
     * Gets the username of the client requesting the connection.
     * @return The username of the client requesting a connection or 
     * null if the request is a disconnection.
     */
	public String getUsername() {
		return username;
	}
	
	/**
	 * Gets whether this connection request corresponds to a connection
	 * or a disconnection of the client from the server.
	 * @return <b>true</b> if this request encapsulates a connection request, <b>false</b>
	 * if this request is a disconnection request.
	 */
    public boolean isConnection() {
    	return isConnection;
    }
            
	@Override
	public ByteBuffer toBytes() {
		ByteBuffer buffer;
		
		if (!isConnection) {
			buffer = ByteBuffer.allocate(1);
			
			buffer.put((byte) 0);			
		} else {
			byte[] usernameBytes = null;
			
			try {
				usernameBytes = username.getBytes(CHARSET);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			
			buffer = ByteBuffer.allocate(usernameBytes.length + 3);
			
			// Push the connection boolean onto the buffer
			buffer.put((byte) 1);
			// Push the number of bytes for the username
			buffer.putShort((short) usernameBytes.length);
			// Push the username onto the buffer
			buffer.put(usernameBytes);
		}
		
		buffer.flip();
		return buffer;
	}
	
	/**
	 * Converts a byte buffer to a {@link #ConnectionRequest ConnectionRequest} object.
	 * @param body A ByteBuffer with the position pointing to the first byte in the body
	 * of the message.
	 * @return The {@link #ConnectionRequest ConnectionRequest} object represented
	 * by the byte buffer.
	 * @aslexclude
	 */
	public static ConnectionRequest fromBytes(ByteBuffer body) {
		final boolean connect = (body.get() == 0) ? false : true;
		String uname = null;
		
		if (connect) {
			// Get the number of bytes to read for the username
			short count = body.getShort();
			
			// Read the username into a byte array
			byte[] unameBytes = new byte[count];
			body.get(unameBytes);
			try {
				uname = new String(unameBytes, CHARSET);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		
		return new ConnectionRequest(uname, connect);
	}
}
