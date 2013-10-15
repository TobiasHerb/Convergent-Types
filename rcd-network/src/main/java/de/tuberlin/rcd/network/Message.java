package de.tuberlin.rcd.network;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

/**
 * Data container for network messages. All data which is send over the network  
 * is packed into this container.
 * 
 * @author Tobias Herb
 *
 */
public class Message implements Serializable {

	/**
	 * Constants.
	 */
	private static final long serialVersionUID = -4042668435910805429L;

	/**
	 * Constructor.
	 * @param sourceUID The UID of the creator of the message.
	 */
	public Message( UUID sourceUID, UUID messageUID, Map<String,Serializable> dataTable, int timeStamp ) {
		// sanity check.
		if( sourceUID == null )
			throw new NullPointerException();
		//if( messageUID == null )
		//	throw new IllegalArgumentException( "messageUID must not be null" );
		if( dataTable == null )
			throw new NullPointerException();
		
		this.sourceUID = sourceUID;
		this.messageUID = messageUID;
		this.dataTable = dataTable;
		// Set to default value. Which means no time stamp is used.
		this.timeStamp = timeStamp;
	}
	
	/** The UID of the creator of the message. */
	private final UUID sourceUID;
	
	/** The UID of the message. */
	private final UUID messageUID;
	
	/** The map that stores all parameters of the message. */
	private final Map<String,Serializable> dataTable;
	
	/** A simple scalar time stamp which can be used to enforce causal reception.
	    This field is only used if we use a communication channel with no FIFO semantic. */
	private final Integer timeStamp;
	
	/**
	 * Returns the UID of the creator.
	 * @return The UID of the creator.
	 */
	public UUID getSourceUID() {
		return sourceUID;
	}
	
	/**
	 * Returns the UID of the message.
	 * @return The UID of the message.
	 */
	public UUID getMessageUID() {
		return messageUID;
	}

	/**
	 * Return the data table that stores the application dependent parameters.
	 * @return The data table.
	 */
	public Map<String,Serializable> getDataTable() {
		return Collections.unmodifiableMap( this.dataTable );
	}
	
	/**
	 * Return a property stored in the dataTable with the associated name.
	 * @return The data table.
	 */
	public Serializable getParameter( String name ) {
		return dataTable.get( name );
	}

	/**
	 * Get time stamp.
	 * @return Time stamp value. 
	 */
	public int getTimeStamp() {
		return this.timeStamp;
	}
	
	/**
	 * Create a string representation of that message.
	 */
	public String toString() {
		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append( "Message { \n  SOURCE UID: " + this.sourceUID 
								   + "\n  MESSAGE UID: " + this.messageUID 
								   + "\n  TS: " + this.timeStamp 
								   + "\n  DATA {" );
		for( Map.Entry<String,Serializable> entry : this.dataTable.entrySet() ) {
			strBuilder.append( "\n    ['" + entry.getKey() + "'," + entry.getValue() + "]" );
		}
		strBuilder.append( "\n  }\n}" );
		return strBuilder.toString();
	}
}