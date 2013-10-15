package de.tuberlin.rcd.server.runtime;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;

import de.tuberlin.rcd.network.Connection;
import de.tuberlin.rcd.network.Transceiver;
import de.tuberlin.rcd.network.TransceiverFactory;
import de.tuberlin.rcd.network.common.Event;
import de.tuberlin.rcd.network.common.EventDispatcher;
import de.tuberlin.rcd.network.common.Pair;

/**
 * Central component responsible for managing all client connections.
 *
 * @author Tobias Herb
 *
 * TODO: We could use a ConncurrentHashMap for connectionMap for allowing concurrent accesses.
 */
public final class ServerConnectionManager extends EventDispatcher {

    /**
     * Log4J.
     */
    private static final Logger LOGGER = Logger.getLogger( ServerConnectionManager.class );

    /**
     * Event that is dispatched by client
     */
    public static class ConnectionChangedEvent extends Event {

        /**
         * Event types.
         */
        public static final String EVENT_TYPE_CLIENT_ADDED   = "event_type_client_added";
        public static final String EVENT_TYPE_CLIENT_REMOVED = "event_type_client_removed";

        /**
         * Constructor.
         * @param type The type of that event.
         * @param clientUID The client UID.
         */
        public ConnectionChangedEvent( String type, UUID clientUID ) {
            super( type, clientUID );
        }
    }

	/**
	 * Constructor.
	 */
	public ServerConnectionManager(ServerConfiguration configuration) {
		// sanity check.
        if( configuration == null )
            throw new NullPointerException();

        this.configuration = configuration;
        this.connectionMap = new HashMap<UUID,Pair<Connection,Transceiver>>();
		this.factory = new TransceiverFactory( ServerReceiver.class, ServerSender.class );
	}

	/** A map that stores the client UID and the associated connection. */
	private final Map<UUID,Pair<Connection,Transceiver>> connectionMap;
	
	/** The factory is responsible to create a transceiver
	    for the associated connection. */
	private final TransceiverFactory factory;

    /** The tests configuration. */
    public final ServerConfiguration configuration;

	/**
	 * Create and open a client connection and add it to the connection manager.
	 * @param socket The client socket object.
	 * @throws ServerConnectionManagerException
	 */
	public void openConnection( Socket socket, boolean startTransceiver ) throws ServerConnectionManagerException {
		// sanity check.
		if( socket == null )
			throw new NullPointerException();
		
		try {
            // create a dedicated connection handler.
			final Connection connection = new Connection( socket );

            try {
                // Generate a UID for this connection and send it to the client.
                // This behaviour is hardwired, because we want that the client uses
                // the same UID as the tests for further communication.
                UUID clientUID = UUID.randomUUID();
                connection.out.writeObject( clientUID );
                connection.setUID( clientUID );
                LOGGER.info( "uid transmitted to the client [uid = " + connection.getUID() + "]" );
            } catch( Exception e ) {
                connection.close();
                throw new ServerConnectionManagerException( "could not send UID to the client", e );
            }

            // monitor for concurrent managers accesses.
            final ServerConnectionManager managerMutex = this;

            // The transceiver is responsible for managing the life time of the sender and receiver thread for that connection.
            // If the connection collapses, the transceiver stopProcessing the threads and internally cleans up everything.
            // After starting and closing of the transceiver, callback handler for external (the layer above) notification are called.
            // ATTENTION: The handling of the add and remove events is done in different threads!

            final Transceiver transceiver = factory.create( connection,
                /**--------------------------------------------*
                 * starting callback handler.
                 *---------------------------------------------*/
                new Runnable() {
                    @Override
                    public void run() {
                        // serialize access to ServerConnectionManager internals.
                        synchronized( managerMutex ) {
                            // dispatch a client removed event for the external environment.
                            managerMutex.dispatchEvent(new ConnectionChangedEvent(ConnectionChangedEvent.EVENT_TYPE_CLIENT_ADDED, connection.getUID()));
                        }
                    }
                }
                ,
                /**--------------------------------------------*
                 * closing callback handler.
                 *---------------------------------------------*/
                new Runnable() {
                    // Do the housekeeping for the connection manager and close the connection.
                    // The callback handler is called either by the sender or by the receiver thread of the transceiver.
                    @Override
                    public void run() {
                        // serialize access to ServerConnectionManager internals in the case of concurrent closing callback invocations.
                        synchronized( managerMutex ) {
                            if( connectionMap.containsKey( connection.getUID() ) ) {
                                // dispatch a client removed event for the external environment.
                                managerMutex.dispatchEvent(new ConnectionChangedEvent(ConnectionChangedEvent.EVENT_TYPE_CLIENT_REMOVED, connection.getUID()));
                                connectionMap.remove( connection.getUID() );
                            } else {
                                throw new IllegalStateException( "client entry not found" );
                            }
                            try {
                                connection.close();
                            } catch( Exception e ) {
                                throw new IllegalStateException( "could not close connection" );
                            }
                        }
                     }
			    } );
			
			if( transceiver != null ) {
                connectionMap.put( connection.getUID(),
            		               new Pair<Connection,Transceiver>( connection, transceiver ) );

				if( startTransceiver ) {
                    // before we start the transceiver for this connection, we must
                    // inject dependencies for receiver and sender.
                    // (the best way to ensure loose coupling!)
                    this.dispatchEvent( new Event( "CM_INJECT_TRANSCEIVER_DEPENDENCIES", transceiver ) );
					// startProcessing sender and receiver thread for connection.
					transceiver.start();
				}
			} else {
				connection.close();
				throw new ServerConnectionManagerException( "transceiver object could not be created" );
			}
			
		} catch( IOException e ) {
			throw new ServerConnectionManagerException( "connection object could not be created" );
		}
	}
	
	/**
	 * Return the connection object specified by the uid.
	 * @param uid The UID of the client connection.
	 * @return The connection object associated with the UID.
	 * @throws ServerConnectionManagerException If the connection could not be found.
	 */
	public Connection getConnection( UUID uid ) throws ServerConnectionManagerException {
		// sanity check.
		if( uid == null )
			throw new NullPointerException();
		
		final Pair<Connection,Transceiver> connectionAndTransceiver = connectionMap.get( uid );
		if( connectionAndTransceiver.getFirst() == null )
			throw new ServerConnectionManagerException( "connection manager could not find connection" );
		return connectionAndTransceiver.getFirst();
	}
	
	/**
	 * Return the transceiver object specified by the uid.
	 * @param uid The UID of the client transceiver.
	 * @return The transceiver object associated with the UID.
	 * @throws ServerConnectionManagerException
	 */
	public Transceiver getTransceiver( UUID uid ) throws ServerConnectionManagerException {
		// sanity check.
		if( uid == null )
			throw new NullPointerException();
		
		final Pair<Connection,Transceiver> connectionAndTransceiver = connectionMap.get( uid );
		if( connectionAndTransceiver.getSecond() == null )
			throw new ServerConnectionManagerException( "connection manager could not find transceiver" );
		return connectionAndTransceiver.getSecond();
	}
	
	/**
	 * Close the client connection and remove it from the connection manager.
	 * @param uid The UID which specifies the established connection.
	 * @throws ServerConnectionManagerException If the connection could not be found.
	 * 									  If the connection could not be closed.
	 */
	public synchronized void closeConnection( UUID uid ) throws ServerConnectionManagerException {
		// sanity check.
		if( uid == null )
			throw new NullPointerException();
		
		if( connectionMap.containsKey( uid ) ) {
			final Pair<Connection,Transceiver> connectionAndTransceiver = connectionMap.remove( uid );
			final Transceiver transceiver = connectionAndTransceiver.getSecond();
			if( transceiver.isActive() ) {
				transceiver.stop();
			} else {
				try {
					connectionAndTransceiver.getFirst().close();
				} catch (IOException e) {
					throw new ServerConnectionManagerException( "connection object could not be created" );
				}
			}
		} else {
			throw new ServerConnectionManagerException( "connection manager could not find connection" );
		}
	}

    /**
     * Test if connection to the given client UID exists.
     * @param clientUID The UID of the client.
     * @return true, if the client is connected.
     */
    public boolean existsConnection( UUID clientUID ) {
        // sanity check.
        if( clientUID == null )
            throw new NullPointerException();
        return connectionMap.containsKey( clientUID );
    }
}
