package de.tuberlin.rcd.client.runtime;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import de.tuberlin.rcd.client.types.AbstractClientReplicatedType;
import de.tuberlin.rcd.network.Connection;
import de.tuberlin.rcd.network.Message;
import de.tuberlin.rcd.network.MessageBuilder;
import de.tuberlin.rcd.network.Transceiver;
import de.tuberlin.rcd.protocol.message.MessageFormat;
import de.tuberlin.rcd.protocol.runtimedefinition.IDataManager;
import de.tuberlin.rcd.protocol.runtimedefinition.IReplicatedObjectFactory;
import de.tuberlin.rcd.protocol.runtimedefinition.TypeNameRegistry;
import de.tuberlin.rcd.protocol.types.IReplicatedType;

//import org.apache.log4j.Logger;

/**
 * Client side implementation of the data manager.
 *
 * Dependencies:
 *  - TypeNameRegistry
 *  - MessageBuilder
 *  - Connection
 *  - Transceiver
 *
 */
public final class ClientDataManager implements IDataManager {

    /**
     * Log4J.
     */
    //private static final Logger LOGGER = Logger.getLogger( ClientDataManager.class );

    /**
     * Constructor.
     */
    public ClientDataManager(Connection connection, Transceiver transceiver,
                             TypeNameRegistry nameRegistry, MessageBuilder messageBuilder) {
        this.connection = connection;
        this.transceiver = transceiver;
        this.nameRegistry = nameRegistry;
        this.messageBuilder = messageBuilder;
    }

    public final Connection connection;

    public final Transceiver transceiver;

    public final TypeNameRegistry nameRegistry;

    public final MessageBuilder messageBuilder;

    /**
     * Store the replicated types.
     */
    private final Map<String,AbstractClientReplicatedType<?>> replicatedTypes
            = new HashMap<String, AbstractClientReplicatedType<?>>();

    /**
     * Create a new replicated type. Implemented in client and tests specialization.
     * @param name The name of the new replicated type.
     * @param clientUID Only used in the tests implementation.
     * @param clazz The class information about the replicated type.
     * @return The new created replicated type.
     */
    @SuppressWarnings("rawtypes")
	@Override
    public IReplicatedType<?> createReplicatedType( String name, UUID clientUID, Class<? extends IReplicatedType> clazz ) {
        // sanity check.
        if( name ==  null )
            throw new IllegalArgumentException();
        if( clazz == null )
            throw new IllegalArgumentException();

        final AbstractClientReplicatedType<?> type;
        try {
             type = (AbstractClientReplicatedType<?>) clazz.getConstructor( String.class, UUID.class, IDataManager.class )
                     .newInstance(name, UUID.randomUUID(), this);
        } catch( Exception e ) {
            throw new IllegalStateException( e );
        }

        if( type != null ) {
            replicatedTypes.put( name, type );
            type.startProcessing();
            final String globalTypeName = nameRegistry.getGlobalName(clazz.getName());

            final Message msgCreateType = messageBuilder.begin(UUID.randomUUID())
                    .addParameter( MessageFormat.MessageAttribute.MSG_ATTR_MSG_TYPE, MessageFormat.MessageType.MSG_TYPE_CREATE_REPLICATED_TYPE )
                    .addParameter( MessageFormat.MessageAttribute.MSG_ATTR_CLASS_NAME, globalTypeName )
                    .addParameter(MessageFormat.MessageAttribute.MSG_ATTR_TYPE_NAME, name)
                    .build(connection.getUID());

            final ClientSender sender = (ClientSender) transceiver.getTransmitter();
            sender.enqueueMessage( msgCreateType );
            type.fill( null );
            return type;
        } else {
            throw new IllegalStateException();
        }
    }

    /**
     * Create a new generic replicated type. Implemented in client and tests specialization.
     * @param name The name of the new replicated type.
     * @param factory Factory is responsible for concrete type instantiation.
     * @return The new created replicated type.
     */
    @Override
    public <G> IReplicatedType<G> createReplicatedType( String name, IReplicatedObjectFactory<G> factory ) {
        // sanity check.
        if( name ==  null )
            throw new IllegalArgumentException();
        if( factory == null )
            throw new IllegalArgumentException();

        final AbstractClientReplicatedType<G> type = (AbstractClientReplicatedType<G>) factory.instantiate( name, UUID.randomUUID(), this );
        if( type != null ) {
            replicatedTypes.put( name, type );

            type.startProcessing();
            final String globalTypeName = nameRegistry.getGlobalName(type.getClass().getName());

            final Message msgCreateType = messageBuilder.begin(UUID.randomUUID())
                    .addParameter( MessageFormat.MessageAttribute.MSG_ATTR_MSG_TYPE, MessageFormat.MessageType.MSG_TYPE_CREATE_REPLICATED_TYPE )
                    .addParameter( MessageFormat.MessageAttribute.MSG_ATTR_CLASS_NAME, globalTypeName )
                    .addParameter(MessageFormat.MessageAttribute.MSG_ATTR_TYPE_NAME, name)
                    .build(connection.getUID());

            final ClientSender sender = (ClientSender) transceiver.getTransmitter();
            sender.enqueueMessage( msgCreateType );
            type.fill( null );
            return type;
        } else {
            throw new IllegalStateException();
        }
    }

    /**
     * Client registers with a replicated data type.
     * Implemented in client specialization.
     * @param name The name of the replicated type.
     * @param clazz The class information about the replicated type.
     * @return The registered replicated type.
     */
    @SuppressWarnings("rawtypes")
	@Override
    public IReplicatedType<?> registerByReplicatedType( String name, Class<? extends IReplicatedType> clazz ) {
        // sanity check.
        if( name ==  null )
            throw new IllegalArgumentException();
        if( clazz == null )
            throw new IllegalArgumentException();

        final AbstractClientReplicatedType<?> type;
        try {
             type = (AbstractClientReplicatedType<?>) clazz.getConstructor( String.class, UUID.class, IDataManager.class )
                     .newInstance(name, UUID.randomUUID(), this);
        } catch( Exception e ) {
            throw new IllegalStateException( e );
        }
        if( type != null ) {
            replicatedTypes.put( name, type );
            type.startProcessing();
            final String globalTypeName = nameRegistry.getGlobalName(clazz.getName());

            final Message msgRegisterType = messageBuilder.begin(UUID.randomUUID())
                    .addParameter( MessageFormat.MessageAttribute.MSG_ATTR_MSG_TYPE, MessageFormat.MessageType.MSG_TYPE_REGISTER_BY_REPLICATED_TYPE )
                    .addParameter( MessageFormat.MessageAttribute.MSG_ATTR_CLASS_NAME, globalTypeName )
                    .addParameter( MessageFormat.MessageAttribute.MSG_ATTR_TYPE_NAME, name )
                    .build( connection.getUID() );

            final ClientSender sender = (ClientSender) transceiver.getTransmitter();
            sender.enqueueMessage( msgRegisterType );
            return type;
        } else {
            throw new IllegalStateException();
        }
    }

    /**
     * Client registers with a generic replicated data type.
     * @param name The name of the replicated type.
     * @param factory Factory is responsible for concrete type instantiation.
     * @return The registered replicated type.
     */
    public <G> IReplicatedType<G> registerByReplicatedType( String name, IReplicatedObjectFactory<G> factory ) {
        // sanity check.
        if( name ==  null )
            throw new IllegalArgumentException();
        if( factory == null )
            throw new IllegalArgumentException();

        final AbstractClientReplicatedType<G> type = (AbstractClientReplicatedType<G>) factory.instantiate( name, UUID.randomUUID(), this );
        if( type != null ) {
            replicatedTypes.put( name, type );
            type.startProcessing();
            final String globalTypeName = nameRegistry.getGlobalName(type.getClass().getName());

            final Message msgRegisterType = messageBuilder.begin(UUID.randomUUID())
                    .addParameter( MessageFormat.MessageAttribute.MSG_ATTR_MSG_TYPE, MessageFormat.MessageType.MSG_TYPE_REGISTER_BY_REPLICATED_TYPE )
                    .addParameter( MessageFormat.MessageAttribute.MSG_ATTR_CLASS_NAME, globalTypeName )
                    .addParameter( MessageFormat.MessageAttribute.MSG_ATTR_TYPE_NAME, name )
                    .build( connection.getUID() );

            final ClientSender sender = (ClientSender) transceiver.getTransmitter();
            sender.enqueueMessage( msgRegisterType );
            return type;
        } else {
            throw new IllegalStateException();
        }
    }

    /**
     * Remove/Delete the replicated data type.
     * @param name The name of the replicated type.
     */
    @Override
    public void removeReplicatedType( String name ) {
        // TODO: implement it.
    }

    /**
     * Test if the replicated data type exists.
     * @param name The name of the replicated type.
     * @return True if the data type exists.
     */
    @Override
    public boolean existsReplicatedType( String name ) {
        // sanity check.
        if( name == null )
            throw new IllegalArgumentException();
        return replicatedTypes.containsKey( name );
    }

    /**
     * Get the replicated data with the given name.
     * @param name The name of the replicated type.
     * @return The data type if exists else null should be returned.
     */
    @Override
    public IReplicatedType<?> getReplicatedType( String name ) {
        // sanity check.
        if( name == null )
            throw new IllegalArgumentException();
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
