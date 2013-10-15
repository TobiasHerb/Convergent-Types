package de.tuberlin.rcd.network;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Creates network messages. 
 * 
 * usage:
 * Message m = builder.addParameter( "xxx", X ) 
 * 					  .addParameter( "yyy", Y )
 * 					  .addParamteer( "zzz", Z )
 * 					  .build();
 * 
 * ATTENTION: The build process is thread safe!
 * 
 * @author Tobias Herb
 * 
 */
public final class MessageBuilder {

	/**
	 * Constructor.
	 */
	public MessageBuilder( boolean useIncrementingTimeStamp ) {
		this.dataTable = new HashMap<String,Serializable>();
		this.useIncrementingTimeStamp = useIncrementingTimeStamp;
		if( useIncrementingTimeStamp )
			this.timeStamp = 0;
		else 
			this.timeStamp = -1;
		messageUID = null;
	}
	
	/** The message UID of the to be created message. */
	private UUID messageUID;
	
	/** Stores the parameters of the to be created message. */
	private final Map<String,Serializable> dataTable;
	
	/** Stores the message timeStamp, if we don´t need one the default value is -1. */
	private int timeStamp;
	
	/** Flag that controls incrementing time stamps. */
	private final boolean useIncrementingTimeStamp;

    /** Lock for the building process from begin to build. */
    private final Lock builderLock = new ReentrantLock();

	/**
	 * Set the UID of the message.
	 * @param messageUID The UID of the messages.
	 */
	public MessageBuilder begin( UUID messageUID ) {
		// sanity check.
		if( messageUID == null )
			throw new IllegalArgumentException( "messageUID must not be null" );
        builderLock.lock();
        this.messageUID = messageUID;
		return this;
	}
	
	/**
	 * Add a parameter to message.
	 * @param name Name of the parameter.
	 * @param data The associated data.
	 */
	public MessageBuilder addParameter( String name, Serializable data ) {
		// sanity check.
		if( data == null ) {
            builderLock.unlock();
            throw new IllegalArgumentException( "data must not be null" );
        }
        if( dataTable.containsKey( name ) ) {
			builderLock.unlock();
            throw new IllegalArgumentException( "name " + name + " already exits" );
        }
		dataTable.put( name, data );
		return this;
	}
	
	/**
	 * Set the message time stamp. Is not recommended.
	 * @param value The message time stamp. 
	 */
	public MessageBuilder setTimeStamp( int value ) {
		if( useIncrementingTimeStamp ) {
			builderLock.unlock();
            throw new IllegalStateException( "message builder is responsible for time stamping" );
        }
        timeStamp = value;
		return this;
	}
	
	/**
	 * Constructs the message.
	 * @return The constructed message. 
	 */
	public Message build( UUID sourceUID ) {
		// Create a copy of the used data table. 
		// Don´ use the builders data table reference!
		Map<String,Serializable> msgDataTable = new HashMap<String,Serializable>( this.dataTable );
		Message msg = new Message( sourceUID, messageUID, msgDataTable, timeStamp );
		// Clear for the data table and time stamp for next usage.
		dataTable.clear();
		messageUID = null;
		if( useIncrementingTimeStamp )
			++timeStamp;
		else
			timeStamp = -1;
        builderLock.unlock();
		return msg;
	}
}
