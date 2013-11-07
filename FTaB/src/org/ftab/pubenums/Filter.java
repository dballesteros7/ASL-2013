package org.ftab.pubenums;

/**
 * Enum type to signify whther the request corresponds to
 * filtering by a particular queue or a particular sender. 
 * @author Jean-Pierre Smith
 */
public enum Filter {
	/**
	 * Enum value for a filter corresponding to a particular queue
	 */
    QUEUE((byte)0, "Get message from a paritcular queue.", "By Queue"), 
    
    /**
	 * Enum value for a filter corresponding to a particular sender
	 */
    SENDER((byte)1, "Get message a particular sender", "By Sender");
    
    /**
     * The associated byte value for an enum constant
     */
    private byte byteValue;
    
    /**
     * Description of the enum
     */
    private String description;

	private String title;
    
    /**
     * Creates the enum type with a specified mapping to a byte value.
     * @param mapping The byte value corresponding to the enum
     * @param description The description of the enum
     */
    Filter(byte mapping, String description, String title) {
    	byteValue = mapping;
    	this.description = description;
		this.title = title;
    }
    
    /**
     * Converts the enumeration constant to a byte value.
     * @return The byte value associated with the enumeration.
     */
    public byte getByteValue() {
    	return byteValue;
    }
    
    /**
     * Gets a textual representation of the enum
     * @return A description for this enum constant.
     */
    public String getDescription() {
		return description;
	}
    
    /**
     * Gets the title for this enum
     * @return A title for the enum
     */
    public String getTitle() {
		return title;
	}
    
    /**
     * Converts a byte value to an enumeration constant
     * @param b The byte value to convert
     * @return The enumerated constant corresponding to the supplied byte value.
     */
    public static Filter fromByte(byte b) {
    	for(Filter filter : Filter.values()) {
    		if(b == filter.getByteValue()) {
    			return filter;
    		}
    	}
    	throw new IllegalArgumentException("That byte value has no defined Filter.");
    }        
};
