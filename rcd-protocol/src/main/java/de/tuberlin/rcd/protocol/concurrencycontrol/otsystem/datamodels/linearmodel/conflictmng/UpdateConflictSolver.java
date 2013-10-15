package de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.datamodels.linearmodel.conflictmng;

import de.tuberlin.rcd.network.common.Pair;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.OTOperationDefinition;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.datamodels.linearmodel.OTLinearOperations;

public interface UpdateConflictSolver<T> {

    public abstract Pair<OTOperationDefinition.OTOperation<T>, OTOperationDefinition.OTOperation<T>>
                            solveUpdateConflict( boolean isServer,
                                                 OTLinearOperations.UpdateOperationBase<T> remote,
                                                 OTLinearOperations.UpdateOperationBase<T> local );
}
