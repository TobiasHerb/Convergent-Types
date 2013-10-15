package de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.algorithms.jupiter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;

import de.tuberlin.rcd.network.common.Pair;
import de.tuberlin.rcd.protocol.concurrencycontrol.common.VectorClock;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.OTOperationDefinition;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.OTOperationHistory;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.OTSystemDefinition;

public class JupiterClientAlgorithm<T> implements OTSystemDefinition.OTClientAlgorithm<T> {

    /**
     * Log4J.
     */
    private static final Logger LOGGER = Logger.getLogger( JupiterClientAlgorithm.class );

    /**
     * Constructor.
     * @param clientUID The UID of the client.
     * @param dataModel The associated data model.
     * @param transformer The client side inclusion transformer.
     * @param opSender The encapsulated sender mechanism.
     */
    public JupiterClientAlgorithm( UUID clientUID,
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
        //this.numLocalOps = 0;
        //this.numRemoteOps = 0;
    }

    /** The UID of the client. */
    private final UUID clientUID;

    /** Server-side data model. */
    private final OTSystemDefinition.OTDataModel<T> dataModel;

    /** Operation (inclusion) transformer. */
    private final OTSystemDefinition.InclusionTransformer<T> transformer;

    /** Operation history. */
    private OTOperationHistory<T> history = new OTOperationHistory<T>();

    /** The sending mechanism is encapsulated behind interface, for ensuring a loose coupling
     between the OT-system and the underlying network layer. */
    private final OTSystemDefinition.OperationSender<T> opSender;

    /** Store all local operations in a outgoing list. */
    private final List<OTOperationDefinition.OTOperation<T>> outgoingQueue
            = new ArrayList<OTOperationDefinition.OTOperation<T>>();

    /** Store the registered cursors, that gets position adapted by local or remote operations. */
    private final Set<OTSystemDefinition.OTCursor<T>> cursors = new HashSet<OTSystemDefinition.OTCursor<T>>();

    /** Track the state-space the client pass through.
        The first represents the number of local operations
        and the second component the count of remote operations. */
    private final VectorClock state = new VectorClock( 0, 0 );

    /**
     * Executes local operations and transmits them to the server.
     * @param localOp The local Operations.
     */
    @Override
    public synchronized void generate( OTOperationDefinition.OTOperation<T> localOp ) {
        // apply the operation to data model and add it to history.
        dataModel.applyOperation( localOp );
        history.pushOperation( localOp );
        // Rewrite the metadata of the operation. Adapt it to the current proxy state.
        final JupiterOperationMetaData metaData = new JupiterOperationMetaData( clientUID, state );
        localOp.setMetaData( metaData );
        // Send it to the associated client.
        opSender.sendOperation( null, localOp );
        // Store it for possible transformations.
        outgoingQueue.add( localOp );
        // increment the local count.
        state.inc( 0 );
    }

    /**
     * Integrate a remote (server) operation into the client state.
     * @param remoteOp The operation received from the server.
     * @return The client-side adapted version of the received server operation.
     */
    @Override
    public synchronized OTOperationDefinition.OTOperation<T> integrate( OTOperationDefinition.OTOperation<T> remoteOp ) {
        // extract metadata.
        final JupiterOperationMetaData remoteMetaData
                = (JupiterOperationMetaData) remoteOp.getMetaData();
        // list of local operations against the client operation must not be transformed.
        final List<OTOperationDefinition.OTOperation<T>> opsToDelete
                = new ArrayList<OTOperationDefinition.OTOperation<T>>();
        // remove operations that the client already integrated.
        for( OTOperationDefinition.OTOperation<T> op : outgoingQueue ) {
            final JupiterOperationMetaData localMetaData = (JupiterOperationMetaData) op.getMetaData();
            if( localMetaData.state.get( 0 ) < remoteMetaData.state.get( 1 ) )
                opsToDelete.add( op );
        }
        outgoingQueue.removeAll( opsToDelete );

        // transformOperation against the updated local buffer.
        for( int i = 0; i < outgoingQueue.size(); ++i ) {
            final OTOperationDefinition.OTOperation<T> localBufferedOp = outgoingQueue.get( i );
            final Pair<OTOperationDefinition.OTOperation<T>,OTOperationDefinition.OTOperation<T>> r2 =
                    transformer.transformOperation( false, remoteOp, localBufferedOp );
            // substitute...
            remoteOp = r2.getFirst();
            outgoingQueue.set( i, r2.getSecond() );
        }

        // apply the operation to data model and add it to history.
        dataModel.applyOperation( remoteOp );
        history.pushOperation( remoteOp );

        // adapt registered cursors.
        for( OTSystemDefinition.OTCursor<T> cursor : cursors ) {
            transformer.transformCursor( cursor, remoteOp );
        }

        state.inc( 1 );
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
    @Override
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

    /**
     * Return the string representation of client state space.
     */
    @Override
    public String toString() {
        return "[ClientUID: " + clientUID.toString() + ", State: " + state +"]";
    }
}
