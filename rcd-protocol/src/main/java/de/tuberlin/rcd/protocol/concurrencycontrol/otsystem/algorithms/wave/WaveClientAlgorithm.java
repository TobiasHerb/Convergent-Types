package de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.algorithms.wave;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;

import de.tuberlin.rcd.network.common.Pair;
import de.tuberlin.rcd.protocol.concurrencycontrol.common.VectorClock;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.OTOperationDefinition.OTOperation;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.OTOperationHistory;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.OTSystemDefinition;

/**
 * The wave client integration control algorithm.
 * @param <T> The type of the data model elements.
 */
public class WaveClientAlgorithm<T> implements OTSystemDefinition.OTClientAlgorithm<T> {

    /**
     * Log4J.
     */
    private static final Logger LOGGER = Logger.getLogger( WaveClientAlgorithm.class );

    /**
     * Constructor.
     * @param clientUID The UID of the client.
     * @param dataModel The associated data model.
     * @param transformer The client side inclusion transformer.
     * @param opSender The encapsulated sender mechanism.
     */
    public WaveClientAlgorithm( UUID clientUID,
                                OTSystemDefinition.OTDataModel<T> dataModel,
                                OTSystemDefinition.InclusionTransformer<T> transformer,
                                OTSystemDefinition.OperationSender<T> opSender ) {
        // sanity check.
        if( clientUID == null )
            throw new IllegalArgumentException();
        if( dataModel == null )
            throw new IllegalArgumentException();
        if( transformer == null )
            throw new IllegalArgumentException();
        if( opSender == null )
            throw new IllegalArgumentException();

        this.clientUID = clientUID;
        this.dataModel = dataModel;
        this.transformer = transformer;
        this.opSender = opSender;
    }

    /** The operation that is on its way to the server. */
    private OTOperation<T> inFlightOperation = null;

    /** The current server revision on the client.
        The count of received server operations. */
    //private int revision = 0;
    private final VectorClock state = new VectorClock( 1 );

    /** The UID of the client. */
    private final UUID clientUID;

    /** Reference to the associated data model. */
    private final OTSystemDefinition.OTDataModel<T> dataModel;

    /** Reference to the operation transformer. */
    private final OTSystemDefinition.InclusionTransformer<T> transformer;

    /** The buffer that stores the local operations. All incoming operations gets
        transformed against this buffer. */
    private final List<OTOperation<T>> localOperationBuffer = new ArrayList<OTOperation<T>>();

    /** The sending mechanism is encapsulated behind interface, for ensuring a loose coupling
        between the OT-system and the underlying network layer. */
    private final OTSystemDefinition.OperationSender<T> opSender;

    /** The history of applied operations on the client side. */
    private OTOperationHistory<T> history = new OTOperationHistory<T>();

    /** Store the registered cursors, that gets position adapted by local or remote operations. */
    private final Set<OTSystemDefinition.OTCursor<T>> cursors
            = Collections.synchronizedSet( new HashSet<OTSystemDefinition.OTCursor<T>>() );

    /** TODO: do we need this mutex? */
    //private final Object mutex = new Object();

    /**
     * Executes local operations and transmits them to the server.
     * @param localOp The local Operations.
     */
    @Override
    public synchronized void generate( OTOperation<T> localOp ) {
        // sanity check.
        if( localOp == null )
            throw new IllegalArgumentException();

        //synchronized( mutex ) {
            LOGGER.info("generate local operation " + localOp);
            // if no operation is in-flight state or in the local operation buffer, we can
            // send it to the tests.
            if( inFlightOperation == null && localOperationBuffer.size() == 0 ) {
                inFlightOperation = localOp;
                final WaveOperationMetaData metaData = new WaveOperationMetaData( clientUID, state );
                inFlightOperation.setMetaData( metaData );
                LOGGER.info("send next operation " + inFlightOperation.toString());
                opSender.sendOperation( null, inFlightOperation );
            } else {
                // if operation is in-flight, then add the local operation to the buffer.
                if( inFlightOperation != null ) {
                    localOperationBuffer.add( localOp );
                }
            }
            // apply local operation.

            // ---
            final WaveOperationMetaData metaData = new WaveOperationMetaData( clientUID, state );
            localOp.setMetaData( metaData );
            // ---

            dataModel.applyOperation( localOp );

            history.pushOperation( localOp );
            // adapt registered cursors.
            for( OTSystemDefinition.OTCursor<T> cursor : cursors ) {
                transformer.transformCursor( cursor, localOp );
            }
        //}
    }

    /**
     * Integrate a remote (server) operation into the client state.
     * @param remoteOp The operation received from the server.
     * @return The client-side adapted version of the received server operation.
     */
    @Override
    public synchronized OTOperation<T> integrate( OTOperation<T> remoteOp ) {
        // sanity check.
        if( remoteOp == null )
            throw new IllegalArgumentException();

        //synchronized( mutex ) {
            // move in state space along tests axis.
            state.inc( 0 );
            // if we receive an acknowledge, drop the old in-flight, and take
            // the first element in our buffer as the new one.
            if( remoteOp.getMetaData().creator.equals( clientUID ) ) {
                LOGGER.info("receive acknowledge " + remoteOp.toString());
                // something goes wrong, if we receive an
                // acknowledge and have no in-flight.
                if( inFlightOperation == null )
                    throw new IllegalStateException();
                // determine the new in-flight, if possible.
                if( localOperationBuffer.size() > 0 ) {
                    inFlightOperation = localOperationBuffer.remove( 0 );
                    // setup correct metadata for operation.
                    final WaveOperationMetaData metaData = new WaveOperationMetaData( clientUID, state );
                    inFlightOperation.setMetaData( metaData );
                    LOGGER.info("send next operation " + inFlightOperation.toString());
                    opSender.sendOperation( null, inFlightOperation );
                } else {
                    inFlightOperation = null;
                }
            } else {
                // transformOperation incoming operation.
                if( inFlightOperation != null ) {
                    LOGGER.info("integrate operation " + remoteOp.toString() + " from " + remoteOp.getMetaData());

                    //final WaveOperationMetaData metaData = new WaveOperationMetaData( clientUID, state );
                    //inFlightOperation.setMetaData( metaData );

                    // transformOperation incoming operation first against "in-flight" operation.
                    final Pair<OTOperation<T>,OTOperation<T>> r1 =
                            transformer.transformOperation(false, remoteOp, inFlightOperation);
                    LOGGER.info("transformOperation remote operation " + remoteOp.toString() + " against server operation "
                            + inFlightOperation + " => " + "(remote: " + r1.getFirst() + ", local: " + r1.getSecond() + ") - in-flight");
                    // substitute...
                    remoteOp = r1.getFirst();
                    inFlightOperation = r1.getSecond();
                    // transformOperation against local buffer.
                    for( int i = 0; i < localOperationBuffer.size(); ++i ) {
                        final OTOperation<T> localBufferedOp = localOperationBuffer.get( i );
                        final Pair<OTOperation<T>,OTOperation<T>> r2 =
                                transformer.transformOperation(false, remoteOp, localBufferedOp);

                        LOGGER.info("transformOperation remote operation " + remoteOp.toString() + " against server operation "
                                + localBufferedOp + " => " + "(remote: " + r2.getFirst() + ", local: " + r2.getSecond() + ") - local-buffer");
                        // substitute...
                        remoteOp = r2.getFirst();
                        localOperationBuffer.set( i, r2.getSecond() );
                    }
                } else {
                    if( localOperationBuffer.size() > 0 ) {
                        throw new IllegalStateException();
                    }
                }
                LOGGER.info("apply remote operation " + remoteOp.toString());

                // apply adapted remote operation.

                // ---
                //final WaveOperationMetaData metaData = new WaveOperationMetaData( clientUID, state );
                //remoteOp.setMetaData( metaData );
                // ---

                dataModel.applyOperation( remoteOp );

                // adapt registered cursors.
                for( OTSystemDefinition.OTCursor<T> cursor : cursors ) {
                    transformer.transformCursor( cursor, remoteOp );
                }

                history.pushOperation( remoteOp );

            }
        //}
        return remoteOp;
    }

    /**
     * Rewrite the local history with a new one.
     * @param history A external (server-side) history.
     */
    public void fillHistory( OTOperationHistory<T> history ) {
        this.history = new OTOperationHistory<T>( history );
    }

    /**
     * Get the current server-side operation history.
     */
    public OTOperationHistory<T> getHistory() {
        return new OTOperationHistory<T>( history );
    }

    @Override
    public void setState( VectorClock state ) {
        this.state.copy( state );
    }

    /**
     * Create a cursor and make register at the algorithm to make
     * it aware of local and remote operations.
     * @param cursorFactory The factory that is responsible for
     *                      creating the cursor instance.
     */
    @Override
    public synchronized OTSystemDefinition.OTCursor<T> registerCursor( OTSystemDefinition.OTCursor.OTCursorFactory<T> cursorFactory ) {
        // sanity check.
        if( cursorFactory == null )
            throw new IllegalArgumentException();
        final OTSystemDefinition.OTCursor<T> cursor = cursorFactory.createCursor();
        if( !cursors.contains( cursor ) ) {
            cursors.add( cursor );
            return cursor;
        } else
            throw new IllegalStateException();
    }

    /**
     * Deregister a registered cursor from the client algorithm.
     * @param cursor The cursor that is deregistered.
     */
    @Override
    public synchronized void deregisterCursor( OTSystemDefinition.OTCursor<T> cursor ) {
        // sanity check.
        if( cursor == null )
            throw new IllegalArgumentException();
        if( cursors.contains( cursor ) ) {
            cursors.remove( cursor );
        } else
            throw new IllegalStateException();
    }
}
