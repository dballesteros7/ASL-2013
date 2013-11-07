package org.ftab.pubenums;

/**
 * Enum type to signify whether to return the message by highest
 * priority or earliest time.
 * @author Jean-Pierre Smith
 *
 */
public enum Order {
    /**
     * Enum value corresponding to choosing a message by its priority
     */
	PRIORITY((byte)0), 
	
	/**
     * Enum value corresponding to choosing a message by its time
     */
	TIMESTAMP((byte)1);
    
    /**
     * The associated byte value for an enum constant
     */
    private byte byteValue;
    
    /**
     * Creates the enum type with a specified mapping to a byte value.
     * @param mapping The byte value corresponding to the enum
     */
    Order(byte mapping) {
    	byteValue = mapping;
    }
    
    /**
     * Converts the enumeration constant to a byte value.
     * @return The byte value associated with the enumeration.
     */
    public byte getByteValue() {
    	return byteValue;
    }
    
    /**
     * Converts a byte value to an enumeration constant
     * @param b The byte value to convert
     * @return The enumerated constant corresponding to the supplied byte value.
     */
    public static Order fromByte(byte b) {
    	for(Order order : Order.values()) {
    		if(b == order.getByteValue()) {
    			return order;
    		}
    	}
    	throw new IllegalArgumentException("That byte value has no defined Order.");
    }    
};
