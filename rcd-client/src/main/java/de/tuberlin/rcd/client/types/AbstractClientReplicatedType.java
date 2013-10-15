package de.tuberlin.rcd.client.types;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import org.apache.log4j.Logger;

import de.tuberlin.rcd.client.runtime.ClientDataManager;
import de.tuberlin.rcd.client.runtime.ClientSender;
import de.tuberlin.rcd.network.Message;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.OTOperationDefinition;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.OTOperationHistory;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.OTSystemDefinition;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.algorithms.ClientOTConcurrencyControlFactory;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.algorithms.filldata.OperationBasedFillData;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.algorithms.filldata.StateBasedFillData;
import de.tuberlin.rcd.protocol.message.MessageFormat;
import de.tuberlin.rcd.protocol.runtimedefinition.IDataManager;
import de.tuberlin.rcd.protocol.types.AbstractFillData;
import de.tuberlin.rcd.protocol.types.AbstractReplicatedType;

public abstract class AbstractClientReplicatedType<T> extends AbstractReplicatedType<T> {

    /**
     * Log4J.
     */
     private static final Logger LOGGER = Logger.getLogger( AbstractClientReplicatedType.class );

    /**
     * Constructor.
     */
    public AbstractClientReplicatedType( ClientOTConcurrencyControlFactory<T> factory, String name, UUID typeUID, final IDataManager dataManager ) {
        super( name, typeUID );
        // sanity check.
        if( dataManager == null )
            throw new IllegalArgumentException();

        this.dataManager = (ClientDataManager)dataManager;

        model = factory.injectDataModel();
        transformer = factory.injectInclusionTransformer( model );
        clientIntegrator = factory.injectControlAlgorithm( this.dataManager.connection.getUID(), model, transformer, opSender );
    }

    //--------------------------------------------------------------------

    /** EXPERIMENTAL */
    private final List<OTOperationDefinition.OTOperation<T>> sendingQueue
            = new ArrayList<OTOperationDefinition.OTOperation<T>>();

    public void explicitSend() {
        if( sendingQueue.size() > 0 ) {
            OTOperationDefinition.OTOperation<T> op = sendingQueue.remove( 0 );
            final ClientSender sender = (ClientSender)
                    this.dataManager.transceiver.getTransmitter();
            sender.enqueueMessage( convertOperationToMessage( op ) );
        }
    }

    //--------------------------------------------------------------------

    /** Reference to the data manager. */
    protected final ClientDataManager dataManager;

    /** System specific */

    /** Encapsulate the network specific stuff. */
    protected final OTSystemDefinition.OperationSender<T> opSender = new OTSystemDefinition.OperationSender<T>() {
        @Override
        public void sendOperation(UUID clientUID, OTOperationDefinition.OTOperation<T> op) {
            //if( singleStepMode ) {
            //    sendingQueue.add( op );
            //} else {
                final ClientSender sender = (ClientSender)
                        dataManager.transceiver.getTransmitter();
                sender.enqueueMessage( convertOperationToMessage( op ) );
            //}
        }
    };

    /** The data structures data model. */
    protected final OTSystemDefinition.OTDataModel<T> model;

    /** The effect inclusion transformer. */
    protected final OTSystemDefinition.InclusionTransformer<T> transformer;

    /** The client-side integration control algorithm. */
    protected final OTSystemDefinition.OTClientAlgorithm<T> clientIntegrator;

    /**
     * Stop (barrier) local operations until the data type is initialized. In case of creation is internally
     * <code>type.fill( null )</code> in the data manager <code>createReplicatedType</code> called. In case
     * of registration is the fill method in the <code>FillCommand</code> at the <code>ClientCommandMapping</code>
     * called.
     */
    protected final CountDownLatch fillLatch = new CountDownLatch( 1 );

    /**
     * Convert a operation in a network message.
     * @param op The operation to submit.
     * @return A network message.
     */
    protected Message convertOperationToMessage( OTOperationDefinition.OTOperation<T> op ) {
        return dataManager.messageBuilder.begin(UUID.randomUUID())
                .addParameter( MessageFormat.MessageAttribute.MSG_ATTR_MSG_TYPE,
                        MessageFormat.MessageType.MSG_TYPE_OPERATION_FOR_REPLICATED_TYPE )
                .addParameter( MessageFormat.MessageAttribute.MSG_ATTR_TYPE_NAME, name )
                .addParameter(MessageFormat.MessageAttribute.MSG_ATTR_OPERATION, op)
                .build(dataManager.connection.getUID());
    }

    /**
     * Process remote operations before application.
     * @param msg The received message.
     * @param op The remote operation.
     */
    @Override
    public void processRemoteOperation( Message msg, OTOperationDefinition.OTOperation<T> op ) {
        if( msg.getSourceUID().equals(dataManager.connection.getUID()) ) {
            LOGGER.info( "received acknowledge for message [UID = " + msg.getMessageUID() + "]" );
            dispatchEvent( new ReplicatedTypeEvent( ReplicatedTypeEvent.RS_ACKNOWLEDGE, op ) );
        }
        //
        // DO THE TRANSFORMATION.
        //
        OTOperationDefinition.OTOperation<T> adaptedOp = clientIntegrator.integrate( op );
        if( !msg.getSourceUID().equals( dataManager.connection.getUID() ) ) {
            LOGGER.info( "apply " + op + " to replicated type[name = " + name + "]" );
            dispatchEvent( new ReplicatedTypeEvent( ReplicatedTypeEvent.RS_REMOTE_UPDATE, adaptedOp ) );
        }
        dispatchEvent( new ReplicatedTypeEvent( ReplicatedTypeEvent.RS_UPDATE, adaptedOp ) );
    }

    /**
     * Process local operations before submission.
     * @param op The remote operation.
     */
    @Override
    public void processLocalOperation( OTOperationDefinition.OTOperation<T> op ) {

        // block the calling thread until the fill sequence is finished.
        waitUntilFilled();

        clientIntegrator.generate(op);
        dispatchEvent( new ReplicatedTypeEvent( ReplicatedTypeEvent.RS_LOCAL_UPDATE, op ) );
        dispatchEvent( new ReplicatedTypeEvent( ReplicatedTypeEvent.RS_UPDATE, op ) );
    }

    @Override
    public OTOperationHistory<T> getHistory() {
        return clientIntegrator.getHistory();
    }

    /**
     * Fill the client side data model after registration.
     * @param fillData The received fill data.
     */
    @SuppressWarnings("unchecked")
    public void fill( AbstractFillData fillData ) {
        if( fillData != null ) {
            if( fillData instanceof StateBasedFillData) {
                final StateBasedFillData<T> data = (StateBasedFillData<T>)fillData;
                model.fillModel( data.model );
                clientIntegrator.setState( data.state );
            } else if( fillData instanceof OperationBasedFillData) {
                final OperationBasedFillData<T> data = (OperationBasedFillData<T>)fillData;
                for ( Iterator<OTOperationDefinition.OTOperation<T>> iterator =
                              data.history.getHistoryIterator(); iterator.hasNext(); ) {
                    final OTOperationDefinition.OTOperation<T> op = iterator.next();
                    model.applyOperation( op );
                }
                clientIntegrator.setState( data.state );
                clientIntegrator.fillHistory( data.history );
            }
        }
        dispatchEvent( new ReplicatedTypeEvent( ReplicatedTypeEvent.RS_FILL, model ) );
        this.fillLatch.countDown();
    }

    /**
     * Internal method that is used for API functions that clients threads call.
     * The calling thread must be blocked until the fill sequence is finished.
     */
    protected void waitUntilFilled() {
        try {
            this.fillLatch.await();
        } catch( InterruptedException e ) {
            throw new IllegalStateException( e );
        }
    }

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
        return this.clientIntegrator;
    }

    @Override
    public OTSystemDefinition.OTOperationGenerator<T> getOperationGenerator() {
        return this.clientIntegrator;
    }

    @Override
    public OTSystemDefinition.OTOperationIntegrator<T> getOperationIntegrator() {
        return this.clientIntegrator;
    }

    @Override
    public OTSystemDefinition.OTDataModel<T> getDataModel() {
        return this.model;
    }
}