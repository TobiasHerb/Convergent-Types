package de.tuberlin.rcd.network;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.UUID;

/**
 * Encapsulates a client connection.
 * 
 * @author Tobias Herb
 *
 */
public final class Connection {

    /**
     * Log4J.
     */
    private static final Logger LOGGER = Logger.getLogger( Connection.class );

	/**
	 * Constructor.
	 * @param socket Endpoint for client communication.
	 * @throws IOException If an I/O error occurs while reading or 
	 * 		   writing stream header.
	 */
	public Connection( Socket socket ) throws IOException {
		// sanity check. 
		if( socket == null )
			throw new NullPointerException();

        this.socket = socket;
        // be careful this order is important here, otherwise we generate a deadlock.
        this.out = new ObjectOutputStream( socket.getOutputStream() );
        this.in = new ObjectInputStream( socket.getInputStream() );
        this.msgBuilder = new MessageBuilder( false );
        LOGGER.info( "connection context created [uid = " + clientUID + "]" );
    }
	
	/** Endpoint for client communication. */
	public final Socket socket;
	
	/** Channel for receiving objects. */
	public final ObjectInputStream in;
	
	/** Channel for transmitting objects. */
	public final ObjectOutputStream out;
	
	/** Unique identifier for the connected client. */
	private UUID clientUID = null;

    /** Every connection gets its own message builder. */
    public final MessageBuilder msgBuilder;

    /**
     * Set the UID of the connection. Is the UID already set a IllegalStateException is thrown.
     * @param uid The UID of the connection.
     */
    public void setUID( UUID uid ) {
        // sanity check.
        if( uid == null )
            throw new NullPointerException();

        if( clientUID == null ) {
            clientUID = uid;
        } else {
            throw new IllegalStateException();
        }
        LOGGER.info( "connection context completed [uid = " + clientUID + "]" );
    }

    /**
     * Return the UID of the connection.
     * @return The UID of the connection.
     */
    public UUID getUID() {
        // sanity check.
        if( clientUID == null )
            throw new IllegalStateException();
        else
            return clientUID;
    }

	/**
	 * Closes the client connection.
	 * @throws IOException If an I/O error occurs while closing the connection.
	 */
	public void close() throws IOException {
		if( out == null || in == null || socket == null )
			throw new IllegalStateException();
		out.close();
		in.close();
		socket.close();
        LOGGER.info( "connection context closed [uid = " + clientUID + "]" );
	}
	
    /**
     * Converts this client connection to a <code>String</code>.
     *
     * @return  a string representation of this client connection.
     */
	public String toString() {
		return "Connection {/n/t SOCKET:" 
						+ socket.toString() 
						+ "/n/tUID:" + ( clientUID == null ? null : clientUID.toString() )
						+ "/n}";
	}
}
