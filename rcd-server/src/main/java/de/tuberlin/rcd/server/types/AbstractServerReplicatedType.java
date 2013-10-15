package de.tuberlin.rcd.server.types;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;

import de.tuberlin.rcd.network.Message;
import de.tuberlin.rcd.network.MessageBuilder;
import de.tuberlin.rcd.network.Transceiver;
import de.tuberlin.rcd.network.common.Event;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.OTOperationDefinition;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.OTOperationHistory;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.OTSystemDefinition;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.algorithms.ServerOTConcurrencyControlFactory;
import de.tuberlin.rcd.protocol.message.MessageFormat;
import de.tuberlin.rcd.protocol.types.AbstractFillData;
import de.tuberlin.rcd.protocol.types.AbstractReplicatedType;
import de.tuberlin.rcd.server.runtime.ServerConnectionManager;
import de.tuberlin.rcd.server.runtime.ServerConnectionManagerException;
import de.tuberlin.rcd.server.runtime.ServerSender;

/**
 * Server side specialization of the AbstractReplicatedType.
 */
public abstract class AbstractServerReplicatedType<T> extends AbstractReplicatedType<T> {

    /**
     * Log4J.
     */
     private static final Logger LOGGER = Logger.getLogger( AbstractServerReplicatedType.class );

    /**
     * Constructor.
     */
    public AbstractServerReplicatedType( ServerOTConcurrencyControlFactory<T> factory, final String name, final UUID typeUID, final ServerConnectionManager connectionManager ) {
        super( name, typeUID );
        // sanity check.
        if( factory == null )
            throw new IllegalArgumentException();
        if( name == null )
            throw new IllegalArgumentException();
        if( typeUID == null )
            throw new IllegalArgumentException();
        if( connectionManager == null )
            throw new IllegalArgumentException();

        this.registeredClients = Collections.synchronizedSet( new HashSet<UUID>() );
        this.connectionManager = connectionManager;
        this.model = factory.injectDataModel();
        this.transformer = factory.injectInclusionTransformer( model );
        this.serverIntegrator = factory.injectControlAlgorithm( model, transformer, opSender, this );
    }

    /** Stores all registered client for this type. */
    protected final Set<UUID> registeredClients;

    /** Reference to the connection manager. */
    protected final ServerConnectionManager connectionManager;

    /** System specific */

    /** Encapsulate the network layer specific stuff. */
    protected final OTSystemDefinition.OperationSender<T> opSender = new OTSystemDefinition.OperationSender<T>() {
        @Override
        public void sendOperation( UUID clientUID, OTOperationDefinition.OTOperation<T> op ) {
            // substitute the operation in msg.
            final MessageBuilder builder;
            try {
                builder = connectionManager.getConnection( clientUID ).msgBuilder;
            } catch( ServerConnectionManagerException e ) {
                throw new IllegalStateException( e );
            }
            final Message newMsg = builder.begin( UUID.randomUUID() )
                    .addParameter( MessageFormat.MessageAttribute.MSG_ATTR_MSG_TYPE,
                            MessageFormat.MessageType.MSG_TYPE_OPERATION_FOR_REPLICATED_TYPE )
                    .addParameter( MessageFormat.MessageAttribute.MSG_ATTR_TYPE_NAME, name )
                    .addParameter(MessageFormat.MessageAttribute.MSG_ATTR_OPERATION, op )
                    .build( op.getMetaData().creator );
            try {
                final Transceiver transceiver = connectionManager.getTransceiver( clientUID );
                final ServerSender sender = (ServerSender)transceiver.getTransmitter();
                sender.enqueueMessage( newMsg );
            } catch( ServerConnectionManagerException e ) {
                throw new IllegalStateException( e );
            }
        }
    };

    /** Structure specific **/

    /** The data structures data model. */
    protected final OTSystemDefinition.OTDataModel<T> model;

    /** The effect inclusion transformer. */
    protected final OTSystemDefinition.InclusionTransformer<T> transformer;

    /** Algorithm specific **/

    /** The server-side integration control algorithm. */
    protected final OTSystemDefinition.OTServerAlgorithm<T> serverIntegrator;

    //private CountDownLatch fillLatch = null;

    //-----------------------------------------------
    // tests specific implementation.
    //-----------------------------------------------

    /**
     * Build a association between a data type and client.
     * @param clientUID The UID of the client.
     */
    public void registerClient( UUID clientUID ) {
        // sanity check.
        if( clientUID == null )
            throw new NullPointerException();
        if( !registeredClients.contains( clientUID ) ) {
            registeredClients.add(clientUID);
            dispatchEvent( new Event( "type_added_client", clientUID ) );
        } else {
            throw new IllegalStateException();
        }
    }

    /**
     * Check if a client is associated with the data type.
     * @param clientUID The UID of the client.
     * @return true, if the given client is registered.
     */
    public boolean isClientRegistered( UUID clientUID ) {
        // sanity check.
        if( clientUID == null )
            throw new NullPointerException();
        return registeredClients.contains( clientUID );
    }

    /**
     * Cut the link to a associated client.
     * @param clientUID The UID of the loosing client.
     */
    public void deregisterClient( UUID clientUID ) {
        // sanity check.
        if( clientUID == null )
            throw new NullPointerException();
        if( registeredClients.contains( clientUID ) ) {
            registeredClients.remove(clientUID);
            dispatchEvent( new Event( "type_removed_client", clientUID ) );
        } else {
            throw new IllegalStateException();
        }
    }

    /**
     * Fill the given client with the current state of the replicated object.
     * @param clientUID The UID of the client that gets filled.
     */
    public void fillClient( UUID clientUID ) {
        // sanity check.
        if( clientUID == null )
            throw new NullPointerException();

        // TODO: use cyclic barrier!
        // reset the latch.
        //fillLatch = new CountDownLatch( 1 );
        //try {
            //fillLatch.await();
        //} catch (InterruptedException e) {
        //    throw new IllegalStateException( e );
        //}

        // Acquire a lock in the fill phase. No incoming operation is processed until the new client is
        // registered and filled with current data model state. Without the lock it could be possible that
        // operation are processed and the new client gets no messages. The state would diverge from the beginning.
        lockRemoteOpProcessing(); // The experimental re-entrant lock of <code>AbstractReplicatedType</code>.

            final AbstractFillData fillData = fillData();
            final MessageBuilder builder;

            try {
                builder = connectionManager.getConnection(clientUID).msgBuilder;
            } catch( ServerConnectionManagerException e ) {
                throw new IllegalStateException( e );
            }

            final Message msg = builder.begin( UUID.randomUUID() )
                    .addParameter( MessageFormat.MessageAttribute.MSG_ATTR_MSG_TYPE, MessageFormat.MessageType.MSG_TYPE_FILL_REPLICATED_TYPE )
                    .addParameter( MessageFormat.MessageAttribute.MSG_ATTR_TYPE_NAME, name )
                    .addParameter( MessageFormat.MessageAttribute.MSG_ATTR_FILL_DATA, fillData )
                    .build( connectionManager.configuration.serverUID );

            try {
                final ServerSender sender = (ServerSender) connectionManager.getTransceiver(clientUID).getTransmitter();
                sender.enqueueMessage( msg );
            } catch( ServerConnectionManagerException e ) {
                throw new IllegalStateException( e );
            }

            registerClient( clientUID );
            LOGGER.info( "registered and filled client[uid =" + clientUID + " ] by type[name = " + name + "]" );

            // TODO: this is strange. Why do we need to wait here?
            try {
                Thread.sleep( 1000 );
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        unlockRemoteOpProcessing();
    }

    /**
     * Process remote operations before application.
     * @param msg The received message.
     * @param remoteOp The remote operation.
     */
    @Override
    public void processRemoteOperation( Message msg, OTOperationDefinition.OTOperation<T> remoteOp ) {
        LOGGER.info( "replicated type[name = " + name + "] is processing message[msg = " + msg + "]" );

        //
        // DO SOME TRANSFORMATION.
        //
        serverIntegrator.integrate( remoteOp );
    }

    /**
     * Process local operations before submission.
     *
     * @param op The remote operation.
     */
    @Override
    public void processLocalOperation(OTOperationDefinition.OTOperation<T> op) {
        throw new UnsupportedOperationException();
    }

    /**
     * Return the history of all executed operations.
     * @return The operation history.
     */
    @Override
    public OTOperationHistory<T> getHistory() {
        return serverIntegrator.getHistory();
    }

    /**
     * Return the current state of the replicated data for client filling.
     * @return Current state of the replicated data.
     */
    protected abstract AbstractFillData fillData();

    /**
     * Create a cursor on a fixed position.
     * @param position Index of the data element the cursor points to.
     * @return A registered cursor.
     */
    @Override
    public OTSystemDefinition.OTCursor<T> createCursor( int position ) {
        return null;
    }

    @Override
    public OTSystemDefinition.OTAlgorithmBase<T> getAlgorithmBase() {
        return this.serverIntegrator;
    }

    @Override
    public OTSystemDefinition.OTOperationGenerator<T> getOperationGenerator() {
        return null;
    }

    @Override
    public OTSystemDefinition.OTOperationIntegrator<T> getOperationIntegrator() {
        return this.serverIntegrator;
    }

    @Override
    public OTSystemDefinition.OTDataModel<T> getDataModel() {
        return this.model;
    }
}
