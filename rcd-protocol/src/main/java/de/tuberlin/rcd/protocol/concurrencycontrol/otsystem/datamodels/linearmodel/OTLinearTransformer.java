package de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.datamodels.linearmodel;

import de.tuberlin.rcd.network.common.Pair;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.OTOperationDefinition.NoOperation;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.OTOperationDefinition.OTOperation;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.OTSystemDefinition;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.datamodels.linearmodel.OTLinearOperations.DeleteSEOperation;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.datamodels.linearmodel.OTLinearOperations.InsertSEOperation;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.datamodels.linearmodel.OTLinearOperations.OperationHelper;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.datamodels.linearmodel.OTLinearOperations.UpdateEntityOperation;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.datamodels.linearmodel.OTLinearOperations.UpdateOperationBase;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.datamodels.linearmodel.OTLinearOperations.UpdateSEOperation;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.datamodels.linearmodel.conflictmng.UpdateConflictSolver;

/**
 * This interfaces defines all transformation functions that include the effect
 * of (concurrent/independent) operations.
 */
public final class OTLinearTransformer<T> implements OTSystemDefinition.InclusionTransformer<T> {

    /**
     * Constructor.
     * @param conflictSolver A instance of a dedicated conflict solver for update-update conflicts.
     */
    public OTLinearTransformer( UpdateConflictSolver<T> conflictSolver ) {
        // sanity check.
        if( conflictSolver == null )
            throw new IllegalArgumentException();

        this.conflictSolver = conflictSolver;
    }

    /** Reference to the conflict solver, that allows to embed user defined semantics
        for resolving update-update conflicts. */
    private final UpdateConflictSolver<T> conflictSolver;

    /**
     * The heart of the OT system. This functions transforms a remote operation against a local operation
     * and produces a pair of corrected remote and local operation. The triggering of the transformation
     * function is done in a concrete transformation control algorithm.
     * @param isServer Flag that indicates if transformation is done on the server or the client side.
     *                 For insert-insert conflicts its a defined rule that the server operation always wins.
     * @param remoteOp The remote operation that needs to be transformed against the local operation.
     * @param localOp The local operations against the server operation is transformed.
     * @return A pair of corrected (adapted) remote and local operations.
     */
    @Override
    public Pair<OTOperation<T>, OTOperation<T>>
        transformOperation( boolean isServer, OTOperation<T> remoteOp, OTOperation<T> localOp ) {
        // sanity check.
        if( remoteOp == null )
            throw new IllegalArgumentException();
        if( localOp == null )
            throw new IllegalArgumentException();

        //-------------------------------------
        // remote: insert vs. local: insert
        //-------------------------------------
        if( remoteOp instanceof InsertSEOperation &&
            localOp instanceof InsertSEOperation ) {

            final InsertSEOperation<T> remoteInsert = (InsertSEOperation<T>)remoteOp;
            final InsertSEOperation<T> localInsert  = (InsertSEOperation<T>)localOp;

            if( remoteInsert.position < localInsert.position )
                return OperationHelper.buildPair(remoteInsert, OperationHelper.adaptOperation(localInsert, localInsert.position + 1));
            if( remoteInsert.position > localInsert.position )
                return OperationHelper.buildPair(OperationHelper.adaptOperation(remoteInsert, remoteInsert.position + 1), localInsert);

            if( remoteInsert.position == localInsert.position ) {
                // defined rule: tests operation wins always the insert conflict.
                if( isServer ) {
                    return OperationHelper.buildPair(OperationHelper.adaptOperation(remoteInsert, remoteInsert.position + 1), localInsert);
                } else {
                    return OperationHelper.buildPair(remoteInsert, OperationHelper.adaptOperation(localInsert, localInsert.position + 1));
                }
            }

        //-------------------------------------
        // remote: insert vs. local: update
        //-------------------------------------
        } else if( remoteOp instanceof InsertSEOperation &&
                   localOp instanceof UpdateOperationBase ) {

            final InsertSEOperation<T> remoteInsert = (InsertSEOperation<T>)remoteOp;
            final UpdateSEOperation<T> localUpdate  = (UpdateSEOperation<T>)localOp;

            if( remoteInsert.position < localUpdate.position )
                return OperationHelper.buildPair(remoteInsert, OperationHelper.adaptOperation(localUpdate, localUpdate.position + 1));
            if( remoteInsert.position > localUpdate.position )
                return OperationHelper.buildPair(remoteInsert, localUpdate);
            if( remoteInsert.position == localUpdate.position )
                return OperationHelper.buildPair(remoteInsert, OperationHelper.adaptOperation(localUpdate, localUpdate.position + 1));

        //-------------------------------------
        // remote: insert vs. local: delete
        //-------------------------------------
        } else if( remoteOp instanceof InsertSEOperation &&
                   localOp instanceof DeleteSEOperation ) {

            final InsertSEOperation<T> remoteInsert = (InsertSEOperation<T>)remoteOp;
            final DeleteSEOperation<T> localDelete  = (DeleteSEOperation<T>)localOp;

            if( remoteInsert.position < localDelete.position )
                return OperationHelper.buildPair(remoteInsert, OperationHelper.adaptOperation(localDelete, localDelete.position + 1));
            if( remoteInsert.position > localDelete.position )
                return OperationHelper.buildPair(OperationHelper.adaptOperation(remoteInsert, remoteInsert.position - 1), localDelete);
            if( remoteInsert.position == localDelete.position )
                return OperationHelper.buildPair(remoteInsert, OperationHelper.adaptOperation(localDelete, localDelete.position + 1));

        //-------------------------------------
        // remote: update vs. local: insert
        //-------------------------------------
        } else if( remoteOp instanceof UpdateOperationBase &&
                   localOp instanceof InsertSEOperation ) {

            final UpdateSEOperation<T> remoteUpdate = (UpdateSEOperation<T>)remoteOp;
            final InsertSEOperation<T> localInsert  = (InsertSEOperation<T>)localOp;

            if( remoteUpdate.position < localInsert.position )
                return OperationHelper.buildPair(remoteUpdate, localInsert);
            if( remoteUpdate.position > localInsert.position )
                return OperationHelper.buildPair(OperationHelper.adaptOperation(remoteUpdate, remoteUpdate.position + 1), localInsert);
            if( remoteUpdate.position == localInsert.position )
                return OperationHelper.buildPair(OperationHelper.adaptOperation(remoteUpdate, remoteUpdate.position + 1), localInsert);

        //-------------------------------------------------
        // remote: update std vs. local: update std
        //-------------------------------------------------
        } else if( remoteOp instanceof UpdateOperationBase &&
                   localOp instanceof UpdateOperationBase ) {

            //-------------------------------------------------
            // remote: replace vs. local: replace
            //-------------------------------------------------
            if( remoteOp instanceof UpdateSEOperation &&
                localOp instanceof UpdateSEOperation ) {

                final UpdateSEOperation<T> remoteUpdate = (UpdateSEOperation<T>)remoteOp;
                final UpdateSEOperation<T> localUpdate  = (UpdateSEOperation<T>)localOp;

                if( remoteUpdate.position != localUpdate.position )
                    return OperationHelper.buildPair( remoteUpdate, localUpdate );
                else {
                    // Call UpdateConflictSolver.
                    return conflictSolver.solveUpdateConflict( isServer, remoteUpdate, localUpdate );
                }

            //-------------------------------------------------
            // remote: replace vs. local: update entity
            //-------------------------------------------------
            } else if( remoteOp instanceof UpdateSEOperation &&
                       localOp instanceof UpdateEntityOperation ) {

                final UpdateSEOperation<T> remoteUpdate = (UpdateSEOperation<T>)remoteOp;
                final UpdateEntityOperation<T> localUpdateEntity  = (UpdateEntityOperation<T>)localOp;

                if( remoteUpdate.position != localUpdateEntity.position )
                    return OperationHelper.buildPair( remoteUpdate, localUpdateEntity );
                else {
                    // Call UpdateConflictSolver.
                    return conflictSolver.solveUpdateConflict( isServer, remoteUpdate, localUpdateEntity );
                }

            //-------------------------------------------------
            // remote: update entity vs. local: replace
            //-------------------------------------------------
            } else if( remoteOp instanceof UpdateEntityOperation &&
                    localOp instanceof UpdateSEOperation ) {

                final UpdateEntityOperation<T> remoteUpdateEntity = (UpdateEntityOperation<T>)remoteOp;
                final UpdateSEOperation<T> localUpdate  = (UpdateSEOperation<T>)localOp;

                if( remoteUpdateEntity.position != localUpdate.position )
                    return OperationHelper.buildPair( remoteUpdateEntity, localUpdate );
                else {
                    // Call UpdateConflictSolver.
                    return conflictSolver.solveUpdateConflict( isServer, remoteUpdateEntity, localUpdate );
                }

            //-------------------------------------------------
            // remote: update entity vs. local: update entity
            //-------------------------------------------------
            } else if( remoteOp instanceof UpdateEntityOperation &&
                    localOp instanceof UpdateEntityOperation ) {

                final UpdateEntityOperation<T> remoteUpdateEntity = (UpdateEntityOperation<T>)remoteOp;
                final UpdateEntityOperation<T> localUpdateEntity  = (UpdateEntityOperation<T>)localOp;

                if( remoteUpdateEntity.position != localUpdateEntity.position )
                    return OperationHelper.buildPair( remoteUpdateEntity, localUpdateEntity );
                else {
                    // Call UpdateConflictSolver.
                    return conflictSolver.solveUpdateConflict( isServer, remoteUpdateEntity, localUpdateEntity );
                }
            }

        //-------------------------------------
        // remote: update vs. local: delete
        //-------------------------------------
        } else if( remoteOp instanceof UpdateOperationBase &&
                   localOp instanceof DeleteSEOperation ) {

            final UpdateSEOperation<T> remoteUpdate = (UpdateSEOperation<T>)remoteOp;
            final DeleteSEOperation<T> localDelete  = (DeleteSEOperation<T>)localOp;

            if( remoteUpdate.position < localDelete.position )
                return OperationHelper.buildPair(remoteUpdate, localDelete);
            if( remoteUpdate.position > localDelete.position )
                return OperationHelper.buildPair(OperationHelper.adaptOperation(remoteUpdate, remoteUpdate.position - 1), localDelete);
            if( remoteUpdate.position == localDelete.position )
                return OperationHelper.buildPair(new NoOperation<T>(), localDelete);

        //-------------------------------------
        // remote: delete vs. local: insert
        //-------------------------------------
        } else if( remoteOp instanceof DeleteSEOperation &&
                   localOp instanceof InsertSEOperation ) {

            final DeleteSEOperation<T> remoteDelete = (DeleteSEOperation<T>)remoteOp;
            final InsertSEOperation<T> localInsert = (InsertSEOperation<T>)localOp;

            if( remoteDelete.position < localInsert.position )
                return OperationHelper.buildPair(remoteDelete, OperationHelper.adaptOperation(localInsert, localInsert.position - 1));
            if( remoteDelete.position > localInsert.position )
                return OperationHelper.buildPair(OperationHelper.adaptOperation(remoteDelete, remoteDelete.position + 1), localInsert);
            if( remoteDelete.position == localInsert.position )
                return OperationHelper.buildPair(OperationHelper.adaptOperation(remoteDelete, remoteDelete.position + 1), localInsert);

        //-------------------------------------
        // remote: delete vs. local: update
        //-------------------------------------
        } else if( remoteOp instanceof DeleteSEOperation &&
                   localOp instanceof UpdateOperationBase ) {

            final DeleteSEOperation<T> remoteDelete = (DeleteSEOperation<T>)remoteOp;
            final UpdateSEOperation<T> localUpdate = (UpdateSEOperation<T>)localOp;

            if( remoteDelete.position < localUpdate.position )
                return OperationHelper.buildPair(remoteDelete, OperationHelper.adaptOperation(localUpdate, localUpdate.position - 1));
            if( remoteDelete.position > localUpdate.position )
                return OperationHelper.buildPair(remoteDelete, localUpdate);
            if( remoteDelete.position == localUpdate.position )
                return OperationHelper.buildPair(remoteDelete, new NoOperation<T>());

        //-------------------------------------
        // remote: delete vs. local: delete
        //-------------------------------------
        } else if( remoteOp instanceof DeleteSEOperation &&
                   localOp instanceof DeleteSEOperation ) {

            final DeleteSEOperation<T> remoteDelete = (DeleteSEOperation<T>)remoteOp;
            final DeleteSEOperation<T> localDelete = (DeleteSEOperation<T>)localOp;

            if( remoteDelete.position < localDelete.position )
                return OperationHelper.buildPair(remoteDelete, OperationHelper.adaptOperation(localDelete, localDelete.position - 1));
            if( remoteDelete.position > localDelete.position )
                return OperationHelper.buildPair(OperationHelper.adaptOperation(remoteDelete, remoteDelete.position - 1), localDelete);
            if( remoteDelete.position == localDelete.position )
                return OperationHelper.buildPair(new NoOperation<T>(), new NoOperation<T>());
        }

        //-------------------------------------
        // no-operation transformation
        //-------------------------------------
        if( remoteOp instanceof NoOperation || localOp instanceof NoOperation ) {
            return OperationHelper.buildPair(remoteOp, localOp);
        }

        // if no case is accepted, something went wrong.
        throw new IllegalStateException( "( localOp: " + localOp.toString() + ", remoteOp: " + remoteOp.toString() + " )" );
    }

    /**
     * Adapt a cursor according to local or remote operations.
     * @param cursor The cursor that is adapted.
     * @param op The local or remote operation against the cursor is transformed.
     */
    public void transformCursor( OTSystemDefinition.OTCursor<T> cursor, OTOperation<T> op ) {
        // sanity check.
        if( cursor == null )
            throw new IllegalArgumentException();
        if( !( cursor instanceof OTLinearCursor ) )
            throw new IllegalStateException();
        if( op == null )
            throw new IllegalArgumentException();

        final OTLinearCursor<T> linearCursor = (OTLinearCursor<T>)cursor;

        if( op instanceof InsertSEOperation ) {
            final InsertSEOperation<T> insertOp = (InsertSEOperation<T>)op;

            if( insertOp.position <= linearCursor.getPosition() )
                linearCursor.moveForward();

        } else if( op instanceof DeleteSEOperation ) {
            final DeleteSEOperation<T> deleteOp = (DeleteSEOperation<T>)op;

            if( deleteOp.position < linearCursor.getPosition() )
                linearCursor.moveBackward();

            if( deleteOp.position == linearCursor.getPosition() )
                linearCursor.setCursorState( OTSystemDefinition.OTCursor.CursorState.CURSOR_DELETED );

        } else if( op instanceof UpdateSEOperation ) {
            // nothing happens.
            linearCursor.setCursorState( OTSystemDefinition.OTCursor.CursorState.CURSOR_UPDATED );
        }
    }
}

