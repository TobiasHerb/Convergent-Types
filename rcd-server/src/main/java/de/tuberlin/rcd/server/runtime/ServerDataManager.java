package de.tuberlin.rcd.server.runtime;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;

import de.tuberlin.rcd.network.common.Event;
import de.tuberlin.rcd.network.common.IEventListener;
import de.tuberlin.rcd.protocol.runtimedefinition.IDataManager;
import de.tuberlin.rcd.protocol.runtimedefinition.IReplicatedObjectFactory;
import de.tuberlin.rcd.protocol.types.IReplicatedType;
import de.tuberlin.rcd.server.types.AbstractServerReplicatedType;

/**
 *
 * @author Tobias Herb
 * ATTENTION: This class must be synchronized.
 * Try to use only non-blocking concurrent datastructures
 * to avoid heavy synchronization overhead.
 */
public final class ServerDataManager implements IDataManager {

    /**
     * Log4J.
     */
    private static final Logger LOGGER = Logger.getLogger( ServerDataManager.class );

	/**
	 * Constructor.
	 */
	public ServerDataManager(ServerConnectionManager connectionManager) {
	    // sanity check.
        if( connectionManager == null )
            throw new NullPointerException();

        this.connectionManager = connectionManager;
        registerByConnectionManager();
    }

    /** Reference to the ServerConnectionManager. */
    private final ServerConnectionManager connectionManager;

    /** A mapping between the given names and the references to the replicated types. */
    private final Map<String,AbstractServerReplicatedType<?>> replicatedTypes
            = new HashMap<String,AbstractServerReplicatedType<?>>();

    /**
     * Install listeners for ServerConnectionManager events.
     */
    private void registerByConnectionManager() {
        // monitor for concurrent managers accesses.
        final ServerDataManager manager = this;
        // ATTENTION: The handling of the events is done in different threads!
        // install handler for client add events.
        connectionManager.addEventListener( ServerConnectionManager.ConnectionChangedEvent.EVENT_TYPE_CLIENT_ADDED,
            new IEventListener() {
                @Override
                public void handleEvent( Event e ) {
                    final UUID clientUID = (UUID)e.data;
                    // serialize access to data manager internals.
                    synchronized( manager ) {
                        LOGGER.info( "client add notification [uid = " + clientUID.toString() + "]" );
                    }
                }
            }
        );
        // install handler for client remove events.
        connectionManager.addEventListener( ServerConnectionManager.ConnectionChangedEvent.EVENT_TYPE_CLIENT_REMOVED,
            new IEventListener() {
                @Override
                public void handleEvent( Event e ) {
                    final UUID clientUID = (UUID)e.data;
                    // serialize access to data manager internals.
                    synchronized( manager ) {
                        LOGGER.info( "client remove notification [uid = " + clientUID.toString() + "]" );
                        //
                        // Remove the client of all types, where it is registered.
                        // TODO: ensure correct concurrency behaviour.
                        //
                        for( AbstractServerReplicatedType<?> type : replicatedTypes.values() ) {
                            if( type.isClientRegistered( clientUID ) ) {
                                type.deregisterClient(clientUID);
                            }
                        }
                    }
                }
            }
        );
    }

    /**
     * Create a new replicated type. Implemented in client and tests specialization.
     * @param name The name of the new replicated type.
     * @param clientUID Only used in the tests implementation.
     * @param clazz The class information about the replicated type.
     * @return The new created replicated type.
     */
    public synchronized IReplicatedType<?> createReplicatedType( String name, UUID clientUID, @SuppressWarnings("rawtypes") Class<? extends IReplicatedType> clazz ) {
        // sanity check.
        if( name ==  null )
            throw new NullPointerException();
        if( clazz == null )
            throw new NullPointerException();
        final AbstractServerReplicatedType<?> type;
        try {
             Constructor<?> constructors[] = clazz.getConstructors();
             if( constructors.length ==  1 ) {
                 Constructor<?> constructor = constructors[0];
                 // two constructor signatures of replicated types are supported.
                 if( constructor.getParameterTypes().length == 4 ) {
                     type = (AbstractServerReplicatedType<?>) clazz.getConstructor( String.class, UUID.class, Class.class, ServerConnectionManager.class )
                             .newInstance( name, UUID.randomUUID(), Object.class, connectionManager );
                 } else if( constructor.getParameterTypes().length == 3 ) {
                     type = (AbstractServerReplicatedType<?>) clazz.getConstructor( String.class, UUID.class, ServerConnectionManager.class )
                             .newInstance( name, UUID.randomUUID(), connectionManager );
                 } else {
                     throw new IllegalStateException();
                 }
             } else {
                 throw  new IllegalStateException();
             }

            if( type != null ) {
                replicatedTypes.put( name, type );
                // clientUID is null, its a tests side created type.
                if( clientUID != null ) {
                    type.registerClient( clientUID );
                }
                type.startProcessing();
                return type;
            } else {
                throw new IllegalStateException();
            }
        } catch( Exception e ) {
            throw new IllegalStateException( e );
        }
    }

    public <G> IReplicatedType<G> createReplicatedType( String name, IReplicatedObjectFactory<G> factory ) {
        return null;
    }

    /**
     * Client registers with a replicated data type.
     * Implemented in client specialization.
     * @param name The name of the replicated type.
     * @param clazz The class information about the replicated type.
     * @return The registered replicated type.
     */
    @Override
    public IReplicatedType<?> registerByReplicatedType( String name, @SuppressWarnings("rawtypes") Class<? extends IReplicatedType> clazz ) {
        throw new UnsupportedOperationException();
    }

    public <G> IReplicatedType<G> registerByReplicatedType( String name, IReplicatedObjectFactory<G> factory ) {
        throw new UnsupportedOperationException();
    }

    /**
     * Remove/Delete the replicated data type.
     * @param name The name of the replicated type.
     */
    @Override
    public synchronized void removeReplicatedType( String name ) {
        // TODO: implement it.
    }

    /**
     * Test if the replicated data type exists.
     * @param name The name of the replicated type.
     * @return True if the data type exists.
     */
    @Override
    public synchronized boolean existsReplicatedType( String name ) {
        // sanity check.
        if( name == null )
            throw new NullPointerException();
        return replicatedTypes.containsKey( name );
    }

    /**
     * Get the replicated data with the given name.
     * @param name The name of the replicated type.
     * @return The data type if exists else null should be returned.
     */
    @Override
    public synchronized IReplicatedType<?> getReplicatedType( String name ) {
        // sanity check.
        if( name == null )
            throw new NullPointerException();
        return replicatedTypes.get( name );
    }

    /**
     * Return the collection of all active replicated data types.
     * @return A collection of all replicated data types.
     */
    @SuppressWarnings("rawtypes")
	@Override
    public Collection<? extends IReplicatedType> getAllReplicatedTypes() {
        return replicatedTypes.values();
    }
}
