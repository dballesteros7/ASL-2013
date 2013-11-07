package org.ftab.protocol;

/**
 * Abstract class that forms the basis for the protocol messages
 * passed between client and server.
 * @author Jean-Pierre Smith
 */
public abstract class ClientServerProtocol {
	/**
	 * Codes for use in the protocol messages.
	 */
	protected static final byte 
		MSG_START = (byte)0xAA,
		MSG_END = (byte)0xFF,
		CONNECT = (byte)0xF1,
		DISCONNECT = (byte)0xF2,
		CREATE_QUEUE = (byte)0xE1,
		DELETE_QUEUE = (byte)0xE2,
		SEND_MSG = (byte)0xD1,
		RET_MSG_Q = (byte)0xC1,
		RET_MSG_S = (byte)0xC2,
		RETURN_MSG = (byte)0xC3,
		RETURN_QUEUES_WITH_MSGS = (byte)0xB1;
	
	/**
	 * Converts the protocol message to a byte array according to the 
	 * protocol's format.
	 * @return This message as a byte array.
	 */
	public abstract byte[] toBytes();
}
