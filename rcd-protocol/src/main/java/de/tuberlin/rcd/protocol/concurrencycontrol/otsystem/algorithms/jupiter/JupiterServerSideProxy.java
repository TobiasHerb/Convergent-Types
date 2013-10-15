package de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.algorithms.jupiter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import de.tuberlin.rcd.network.common.Pair;
import de.tuberlin.rcd.protocol.concurrencycontrol.common.VectorClock;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.OTOperationDefinition;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.OTSystemDefinition;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.datamodels.linearmodel.OTLinearOperations;

public class JupiterServerSideProxy<T> {

    /**
     * Constructor.
     * @param clientUID The UID of the associated remote client.
     * @param transformer Operation (inclusion) transformer.
     * @param opSender The sending mechanism is encapsulated behind interface, for ensuring a loose coupling
     *                 between the OT-system and the underlying network layer.
     */
    public JupiterServerSideProxy( UUID clientUID,
                                   OTSystemDefinition.InclusionTransformer<T> transformer,
                                   OTSystemDefinition.OperationSender<T> opSender,
                                   VectorClock serverState ) {
        // sanity check.
        if( clientUID == null )
            throw new IllegalArgumentException();
        if( transformer == null )
            throw new IllegalArgumentException();
        if( opSender == null )
            throw new IllegalArgumentException();

        this.clientUID = clientUID;
        this.transformer = transformer;
        this.opSender = opSender;
        this.state = new VectorClock( serverState );
    }

    /** The UID of the client. */
    private final UUID clientUID;

    /** Operation (inclusion) transformer. */
    private final OTSystemDefinition.InclusionTransformer<T> transformer;

    /** The sending mechanism is encapsulated behind interface, for ensuring a loose coupling
        between the OT-system and the underlying network layer. */
    private final OTSystemDefinition.OperationSender<T> opSender;

    /** Store all local operations in a outgoing list. */
    private final List<OTOperationDefinition.OTOperation<T>> outgoingQueue
            = new ArrayList<OTOperationDefinition.OTOperation<T>>();

    /** Track the state-space the client pass through.
        The first represents the number of local operations
        and the second component the count of remote operations. */
    private final VectorClock state; // = new VectorClock( 0, 0 );

    /**
     * Hand-over of integrated client operation to distribute it
     * to the other connected tests.
     * @param localOp The already integrated operation.
     */
    public void proxyGenerate( OTOperationDefinition.OTOperation<T> localOp ) {
        // create a shallow copy for transmission to the associated client.
        final OTOperationDefinition.OTOperation<T> cpLocalOp
                = OTLinearOperations.OperationHelper.shallowCopyOperation( localOp );
        // Rewrite the metadata of the operation. Adapt it to the current proxy state.
        final JupiterOperationMetaData metaData = new JupiterOperationMetaData( localOp.getMetaData().creator, state );
        cpLocalOp.setMetaData( metaData );
        // Send it to the associated client.
        opSender.sendOperation( clientUID, cpLocalOp );
        // Store it for possible transformations.
        outgoingQueue.add( cpLocalOp );
        // increment the local count.
        state.inc( 0 );
    }

    /**
     * Integrate a (remote) client operation to current server (proxy) states.
     * @param remoteOp The received client operation.
     * @return The server side adapted version of the operation.
     */
    public OTOperationDefinition.OTOperation<T>
        proxyIntegrate( OTOperationDefinition.OTOperation<T> remoteOp ) {
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

        // transformOperation against updated local buffer.
        for( int i = 0; i < outgoingQueue.size(); ++i ) {
            final OTOperationDefinition.OTOperation<T> localBufferedOp = outgoingQueue.get( i );
            final Pair<OTOperationDefinition.OTOperation<T>,OTOperationDefinition.OTOperation<T>> r2 =
                    transformer.transformOperation( true, remoteOp, localBufferedOp );
            // substitute...
            remoteOp = r2.getFirst();
            outgoingQueue.set( i, r2.getSecond() );
        }
        state.inc( 1 );
        return remoteOp;
    }

    /**
     * Get the UID of the UID this proxy instance manages.
     */
    public UUID getClientUID() {
        return clientUID;
    }

    /**
     * Return the string representation of the proxy state space.
     */
    @Override
    public String toString() {
        return "[ClientUID: " + clientUID.toString() + ", State: " + state +"]";
    }
}
