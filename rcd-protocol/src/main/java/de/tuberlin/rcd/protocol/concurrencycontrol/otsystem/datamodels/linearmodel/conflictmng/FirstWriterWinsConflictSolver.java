package de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.datamodels.linearmodel.conflictmng;

import de.tuberlin.rcd.network.common.Pair;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.OTOperationDefinition;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.datamodels.linearmodel.OTLinearOperations;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.datamodels.linearmodel.OTLinearOperations.OperationHelper;

public class FirstWriterWinsConflictSolver<T> implements UpdateConflictSolver<T> {

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
                noOp.setMetaData( remoteOp.getMetaData() );
                // on the server-side the local operation wins always.
                return OperationHelper.buildPair( noOp, localOp );

            } else {

                final OTOperationDefinition.NoOperation<T> noOp
                        = new OTOperationDefinition.NoOperation<T>();
                noOp.setMetaData( localOp.getMetaData() );
                // on the client-side the remote operation wins always.
                return OperationHelper.buildPair( remoteOp, noOp );
            }

        } else if( remoteOp instanceof OTLinearOperations.UpdateSEOperation &&
                   localOp instanceof OTLinearOperations.UpdateEntityOperation ) {

            // TODO: implement it.

        } else if( remoteOp instanceof OTLinearOperations.UpdateEntityOperation &&
                   localOp instanceof OTLinearOperations.UpdateSEOperation ) {

            // TODO: implement it.

        } else if( remoteOp instanceof OTLinearOperations.UpdateEntityOperation &&
                   localOp instanceof OTLinearOperations.UpdateEntityOperation ) {

            // TODO: implement it.
        }

        throw new IllegalStateException();
    }
}
