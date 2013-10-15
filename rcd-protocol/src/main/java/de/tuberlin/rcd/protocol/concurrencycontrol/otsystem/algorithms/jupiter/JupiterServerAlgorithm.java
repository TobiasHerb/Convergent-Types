package de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.algorithms.jupiter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import de.tuberlin.rcd.network.common.Event;
import de.tuberlin.rcd.network.common.IEventDispatcher;
import de.tuberlin.rcd.network.common.IEventListener;
import de.tuberlin.rcd.protocol.concurrencycontrol.common.VectorClock;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.OTOperationDefinition;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.OTOperationHistory;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.OTSystemDefinition;

public class JupiterServerAlgorithm<T> implements OTSystemDefinition.OTServerAlgorithm<T> {

    /**
     * Constructor.
     */
    public JupiterServerAlgorithm( OTSystemDefinition.OTDataModel<T> dataModel,
                                   OTSystemDefinition.InclusionTransformer<T> _transformer,
                                   OTSystemDefinition.OperationSender<T> _sender,
                                   IEventDispatcher base) {
        // sanity check.
        if( dataModel == null )
            throw new IllegalArgumentException();
        if( _transformer == null )
            throw new IllegalArgumentException();
        //if( _sender ==  null )
        //    throw new IllegalArgumentException();

        this.dataModel = dataModel;
        this.transformer = _transformer;
        this.sender = _sender;

        if( base != null ) {
            base.addEventListener( "type_added_client", new IEventListener() {
                @Override
                public void handleEvent( Event event ) {
                    final UUID clientUID = (UUID)event.data;
                    proxyMap.put( clientUID,
                            new JupiterServerSideProxy<T>( clientUID, transformer, sender,
                                    new VectorClock( history.getHistorySize(), 0 ) )
                    );
                }
            } );

            base.addEventListener( "type_removed_client", new IEventListener() {
                @Override
                public void handleEvent( Event event ) {
                    final UUID clientUID = (UUID)event.data;
                    proxyMap.remove( clientUID );
                }
            } );
        }
    }

    /** List storing the proxies for the registered tests. */
    private final Map<UUID,JupiterServerSideProxy<T>> proxyMap = new HashMap<UUID,JupiterServerSideProxy<T>>();

    /** Server-side data model. */
    private final OTSystemDefinition.OTDataModel<T> dataModel;

    /** Operation (inclusion) transformer. */
    private final OTSystemDefinition.InclusionTransformer<T> transformer;

    /** Operation history. */
    private final OTOperationHistory<T> history = new OTOperationHistory<T>();

    /** The sending mechanism is encapsulated behind interface, for ensuring a loose coupling
        between the OT-system and the underlying network layer. */
    private final OTSystemDefinition.OperationSender<T> sender;

    /**
     * Adapt a incoming operation by transforming it against all local
     * concurrent (independent) operations.
     * @param remoteOp The remote operation.
     */
    @Override
    public OTOperationDefinition.OTOperation<T> integrate( OTOperationDefinition.OTOperation<T> remoteOp ) {
        // delegate the incoming remote operation to the associated proxy.
        final JupiterServerSideProxy<T> proxy =  proxyMap.get( remoteOp.getMetaData().creator );
        // if no proxy exists to the incoming operation
        // then something went wrong.
        if( proxy == null ) {
            throw new IllegalStateException();
        }
        // integrate the client operation into server state.
        OTOperationDefinition.OTOperation<T> adaptedOp = proxy.proxyIntegrate( remoteOp );
        // distribute it to the client proxies.
        for( JupiterServerSideProxy<T> p : proxyMap.values() ) {
            if( !p.getClientUID().equals( proxy.getClientUID() ) ) {
                p.proxyGenerate( adaptedOp );
            }
        }
        // apply the operation to data model and add it to history.
        dataModel.applyOperation( adaptedOp );
        history.pushOperation( adaptedOp );
        return adaptedOp;
    }

    /**
     * Get the current server-side operation history.
     */
    @Override
    public OTOperationHistory<T> getHistory() {
        return history;
    }

    @Override
    public VectorClock getState() {
        return new VectorClock( 0, history.getHistorySize() );
    }

    @Override
    public OTSystemDefinition.OTCursor<T> registerCursor( OTSystemDefinition.OTCursor.OTCursorFactory<T> cursorFactory ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deregisterCursor( OTSystemDefinition.OTCursor<T> cursor ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return dataModel.toString();
    }

    // -------------------- Only for tests purposes --------------------

    public String getProxyState( UUID clientUID ) {
        return proxyMap.get( clientUID ).toString();
    }

    public void addClientProxy( UUID clientUID, OTSystemDefinition.OperationSender<T> sender ) {
        // sanity check.
        if( clientUID == null )
            throw new IllegalArgumentException();
        JupiterServerSideProxy<T> proxy = new JupiterServerSideProxy<T>( clientUID, transformer, sender, new VectorClock( 0, 0 ) );
        proxyMap.put( clientUID, proxy );
    }
}
