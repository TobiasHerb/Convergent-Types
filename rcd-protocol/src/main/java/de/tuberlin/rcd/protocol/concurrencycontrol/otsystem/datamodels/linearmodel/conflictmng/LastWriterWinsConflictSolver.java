package de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.datamodels.linearmodel.conflictmng;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import de.tuberlin.rcd.network.common.Pair;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.OTOperationDefinition;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.OTSystemDefinition;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.datamodels.linearmodel.OTLinearOperations;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.datamodels.linearmodel.OTLinearOperations.OperationHelper;

public class LastWriterWinsConflictSolver<T> implements UpdateConflictSolver<T> {


    public LastWriterWinsConflictSolver( boolean allowDeltaMerging, OTSystemDefinition.OTDataModel<T> dataModel ) {
        //this.allowDeltaMerging = allowDeltaMerging;
        //this.dataModel = (OTLinearDataModel<T>) dataModel;
    }

    //private final boolean allowDeltaMerging;

    //private final OTLinearDataModel<T> dataModel;

    @Override
    public Pair<OTOperationDefinition.OTOperation<T>, OTOperationDefinition.OTOperation<T>>
            solveUpdateConflict( boolean isServer,
                                 OTLinearOperations.UpdateOperationBase<T> remoteOp,
                                 OTLinearOperations.UpdateOperationBase<T> localOp ) {

        if( remoteOp instanceof OTLinearOperations.UpdateSEOperation &&
            localOp instanceof OTLinearOperations.UpdateSEOperation ) {

            if( isServer ) {

                final OTOperationDefinition.NoOperation<T> noOp
                        = new OTOperationDefinition.NoOperation<T>();
                noOp.setMetaData( localOp.getMetaData() );
                // on the server-side the remote operation wins always.
                return OperationHelper.buildPair( remoteOp, noOp );

            } else {

                final OTOperationDefinition.NoOperation<T> noOp
                        = new OTOperationDefinition.NoOperation<T>();
                noOp.setMetaData( remoteOp.getMetaData() );
                // on the client-side the local operation wins always.
                return OperationHelper.buildPair( noOp, localOp );
            }

        } else if( remoteOp instanceof OTLinearOperations.UpdateSEOperation &&
                localOp instanceof OTLinearOperations.UpdateEntityOperation ) {

            //
            // The replace operation wins always.
            //

            final OTOperationDefinition.NoOperation<T> noOp
                    = new OTOperationDefinition.NoOperation<T>();
            noOp.setMetaData( localOp.getMetaData() );
            // on the client-side the local operation wins always.
            return OperationHelper.buildPair( remoteOp, noOp );

        } else if( remoteOp instanceof OTLinearOperations.UpdateEntityOperation &&
                localOp instanceof OTLinearOperations.UpdateSEOperation ) {

            //
            // The replace operation wins always.
            //

            final OTOperationDefinition.NoOperation<T> noOp
                    = new OTOperationDefinition.NoOperation<T>();
            noOp.setMetaData( remoteOp.getMetaData() );
            // on the client-side the local operation wins always.
            return OperationHelper.buildPair( noOp, localOp );

        } else if( remoteOp instanceof OTLinearOperations.UpdateEntityOperation &&
                   localOp instanceof OTLinearOperations.UpdateEntityOperation ) {

            //
            // First try to merge both delta, if they conflict the last writer wins.
            //

            /*final OTLinearOperations.UpdateEntityOperation<T> updateEntityRemote
                    = (OTLinearOperations.UpdateEntityOperation<T>) remoteOp;

            final OTLinearOperations.UpdateEntityOperation<T> updateEntityLocal
                    = (OTLinearOperations.UpdateEntityOperation<T>) localOp;

            boolean deltasDisjoint = true;

            for( String propertyName : updateEntityLocal.entityDelta.keySet() ) {
                if( updateEntityRemote.entityDelta.containsKey( propertyName ) ) {
                    if( !updateEntityRemote.entityDelta.get( propertyName ).equals( updateEntityLocal.entityDelta.get( propertyName ) ) ) {
                        deltasDisjoint = false;
                    }
                }
            }*/

            // both changes can be applied to the data element.
            //if( deltasDisjoint && allowDeltaMerging ) {

                /*final Map<String,Object> mergedDeltaMap = new HashMap<String,Object>( updateEntityLocal.entityDelta );
                mergedDeltaMap.putAll( updateEntityRemote.entityDelta );

                final OTLinearOperations.UpdateEntityOperation<T> newUpdateEntityRemote
                        = new OTLinearOperations.UpdateEntityOperation<T>( updateEntityRemote.position, updateEntityRemote.entityClazz, mergedDeltaMap );

                newUpdateEntityRemote.setMetaData( updateEntityRemote.getMetaData() );

                final OTLinearOperations.UpdateEntityOperation<T> newUpdateEntityLocal
                        = new OTLinearOperations.UpdateEntityOperation<T>( updateEntityLocal.position, updateEntityLocal.entityClazz, mergedDeltaMap );

                newUpdateEntityLocal.setMetaData( updateEntityLocal.getMetaData() );*/

                //return OperationHelper.buildPair( updateEntityRemote, updateEntityLocal );

            //} else {
                // the last update wins against all, the deltas are not merged.

                //dataModel.rollBackDataElement( updateEntityRemote.position, remoteOp.getMetaData().state );

                final OTLinearOperations.UpdateEntityOperation<T> updateEntityRemote
                        = (OTLinearOperations.UpdateEntityOperation<T>) remoteOp;

                final OTLinearOperations.UpdateEntityOperation<T> updateEntityLocal
                        = (OTLinearOperations.UpdateEntityOperation<T>) localOp;


                if( isServer ) {

                    Map<String,Object> deltaDelta;
                    boolean deltasDisjoint = Collections.disjoint( updateEntityRemote.entityDelta.keySet(), updateEntityLocal.entityDelta.keySet() );
                    if( !deltasDisjoint ) {
                        deltaDelta = new HashMap<String, Object>();
                        for( String propertyName : updateEntityLocal.entityDelta.keySet() ) {
                            if( !updateEntityRemote.entityDelta.containsKey( propertyName ) ) {
                                deltaDelta.put( propertyName, updateEntityLocal.entityDelta.get( propertyName ) );
                            }
                        }
                    } else {
                        deltaDelta  = new HashMap<String, Object>( updateEntityLocal.entityDelta );
                    }

                    final OTLinearOperations.UpdateEntityOperation<T> newUpdateEntityLocal
                            = new OTLinearOperations.UpdateEntityOperation<T>( updateEntityLocal.position, updateEntityLocal.entityClazz, deltaDelta );
                    newUpdateEntityLocal.setMetaData( updateEntityLocal.getMetaData() );

                    //final OTOperationDefinition.NoOperation<T> noOp
                    //        = new OTOperationDefinition.NoOperation<T>();
                    //noOp.setMetaData( localOp.getMetaData() );
                    // on the server-side the remote operation wins always.


                    return OperationHelper.buildPair( remoteOp, newUpdateEntityLocal );
                } else {


                    Map<String,Object> deltaDelta;
                    boolean deltasDisjoint = Collections.disjoint( updateEntityRemote.entityDelta.keySet(), updateEntityLocal.entityDelta.keySet() );
                    if( !deltasDisjoint ) {
                        deltaDelta = new HashMap<String, Object>();
                        for( String propertyName : updateEntityRemote.entityDelta.keySet() ) {
                            if( !updateEntityLocal.entityDelta.containsKey( propertyName ) ) {
                                deltaDelta.put( propertyName, updateEntityRemote.entityDelta.get( propertyName ) );
                            }
                        }
                    } else {
                        deltaDelta  = new HashMap<String, Object>( updateEntityRemote.entityDelta );
                    }

                    final OTLinearOperations.UpdateEntityOperation<T> newUpdateEntityRemote
                            = new OTLinearOperations.UpdateEntityOperation<T>( updateEntityRemote.position, updateEntityRemote.entityClazz, deltaDelta );
                    newUpdateEntityRemote.setMetaData( updateEntityRemote.getMetaData() );

                    //final OTOperationDefinition.NoOperation<T> noOp
                    //        = new OTOperationDefinition.NoOperation<T>();
                    //noOp.setMetaData( remoteOp.getMetaData() );
                    // on the client-side the local operation wins always.

                    return OperationHelper.buildPair( newUpdateEntityRemote, localOp );
                }
            //}
        }

        throw new IllegalStateException();
    }
}
