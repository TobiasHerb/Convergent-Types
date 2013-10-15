package de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.algorithms.wave;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;

import de.tuberlin.rcd.network.common.Event;
import de.tuberlin.rcd.network.common.IEventDispatcher;
import de.tuberlin.rcd.network.common.IEventListener;
import de.tuberlin.rcd.network.common.Pair;
import de.tuberlin.rcd.protocol.concurrencycontrol.common.VectorClock;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.OTOperationDefinition.OTOperation;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.OTOperationHistory;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.OTSystemDefinition;

/**
 *
 * @param <T>
 */
public class WaveServerAlgorithm<T> implements OTSystemDefinition.OTServerAlgorithm<T> {

    /**
     * Log4J.
     */
    private static final Logger LOGGER = Logger.getLogger( WaveServerAlgorithm.class );

    /**
     * Constructor.
     */
    public WaveServerAlgorithm( OTSystemDefinition.OTDataModel<T> dataModel,
                                OTSystemDefinition.InclusionTransformer<T> transformer,
                                OTSystemDefinition.OperationSender<T> sender,
                                IEventDispatcher base ) {
        // sanity check.
        if( dataModel == null )
            throw new IllegalArgumentException();
        if( transformer == null )
            throw new IllegalArgumentException();
        if( sender ==  null )
            throw new IllegalArgumentException();

        this.dataModel = dataModel;
        this.transformer = transformer;
        this.sender = sender;

        if( base !=  null ) {
            base.addEventListener( "type_added_client", new IEventListener() {
                @Override
                public void handleEvent( Event event ) {
                    final UUID clientUID = (UUID)event.data;
                    clients.add( clientUID );
                }
            } );

            base.addEventListener( "type_removed_client", new IEventListener() {
                @Override
                public void handleEvent( Event event ) {
                    final UUID clientUID = (UUID)event.data;
                    clients.remove( clientUID );
                }
            } );
        }
    }

    /** Set is storing all registered tests. */
    private final Set<UUID> clients = Collections.synchronizedSet( new HashSet<UUID>() );

    /** The sending mechanism is encapsulated behind interface, for ensuring a loose coupling
        between the OT-system and the underlying network layer. */
    private final OTSystemDefinition.OperationSender<T> sender;

    /** Server-side data model. */
    private final OTSystemDefinition.OTDataModel<T> dataModel;

    /** Operation (inclusion) transformer. */
    private final OTSystemDefinition.InclusionTransformer<T> transformer;

    /** Operation history. */
    private final OTOperationHistory<T> history = new OTOperationHistory<T>();

    /**
     * Adapt a incoming operation by transforming it against all local
     * concurrent (independent) operations.
     * @param remoteOp The remote operation.
     */
    @Override
    public OTOperation<T> integrate( OTOperation<T> remoteOp ) {
        // sanity check.
        if( remoteOp == null )
            throw new IllegalArgumentException();

        LOGGER.info("integrate operation " + remoteOp.toString() + " from " + remoteOp.getMetaData());

        if( history.getHistorySize() > 0 ) {
            final WaveOperationMetaData metaData = (WaveOperationMetaData)remoteOp.getMetaData();
            List<OTOperation<T>> concurrentOps =
                    history.getConcurrentOperations( metaData.state.get( 0 ) );

            for( OTOperation<T> serverOp : concurrentOps ) {
                Pair<OTOperation<T>,OTOperation<T>> result =
                        transformer.transformOperation(true, remoteOp, serverOp);

                LOGGER.info("transformOperation remote operation " + remoteOp.toString() + " against server operation "
                        + serverOp + " => " + result.getFirst());

                remoteOp = result.getFirst();
            }
        }

        LOGGER.info("apply remote operation " + remoteOp.toString());

        dataModel.applyOperation( remoteOp );
        history.pushOperation( remoteOp );

        // broadcast to all tests.
        for( UUID clientUID : clients ) {
            sender.sendOperation( clientUID, remoteOp );
        }

        return remoteOp;
    }

    /**
     * Get the current server-side operation history.
     */
    @Override
    public OTOperationHistory<T> getHistory() {
        return history;
    }

    /**
     * Get the current server state.
     */
    @Override
    public VectorClock getState() {
        return new VectorClock( new Integer[]{ history.getHistorySize() } );
    }

    /**
     *
     * @param cursorFactory
     */
    @Override
    public OTSystemDefinition.OTCursor<T> registerCursor( OTSystemDefinition.OTCursor.OTCursorFactory<T> cursorFactory ) {
        throw new UnsupportedOperationException();
    }

    /**
     *
     * @param cursor
     */
    @Override
    public void deregisterCursor( OTSystemDefinition.OTCursor<T> cursor ) {
        throw new UnsupportedOperationException();
    }

    // -------------------- Only for tests purposes --------------------

    public void addClient( UUID clientUID ) {
        clients.add( clientUID );
    }
}
