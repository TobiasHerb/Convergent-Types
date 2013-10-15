package de.tuberlin.rcd.protocol.concurrencycontrol.otsystem;

import java.io.Serializable;
import java.util.UUID;

import de.tuberlin.rcd.network.common.Pair;
import de.tuberlin.rcd.protocol.concurrencycontrol.common.VectorClock;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.OTOperationDefinition.OTOperation;

/**
 * This class contains all interfaces and abstract classes to define the
 * abstract operational transformation system.
 */
public class OTSystemDefinition {

    /**
     * Defines interface of all data models the OT system is working on.
     * @param <T> The type of the elements in the data model.
     */
    public static interface OTDataModel<T> extends Serializable  {

        /**
         * Factory for creating data elements.
         * @param <T> The type of the data element.
         */
        //public static interface OTDataModelElementFactory<T> {
        //    public abstract T createDataElement();
        //}

        /**
         * Execute a operation on the data model to change the state.
         * @param op The operation that is applied.
         */
        public abstract void applyOperation( OTOperation<T> op );

        /**
         * Get a complete data model in native data structure representation.
         */
        public abstract Object getData();

        /**
         * Fill the data model with a state a other data model.
         */
        public abstract void fillModel( OTDataModel<T> model );

        /**
         * Get the number of data elements in the data model.
         */
        public abstract int size();
    }

    /**
     * The interface for remote operation integration.
     * @param <T> The type of the elements in the data model.
     */
    public static interface OTOperationIntegrator<T> {

        /**
         * Adapt a incoming operation by transforming it against all local
         * concurrent (independent) operations.
         * @param remoteOp The remote operation.
         */
        public abstract OTOperation<T> integrate( final OTOperation<T> remoteOp );
    }

    /**
     * The interface for local operation generation.
     * @param <T> The type of the elements in the data model.
     */
    public static interface OTOperationGenerator<T> {

        /**
         * Executes local operations and transmits them to the server.
         * @param localOp The local Operations.
         */
        public abstract void generate( final OTOperation<T> localOp );
    }

    /**
     * The base interface for client and server algorithm.
     * @param <T> The type of the elements in the data model.
     */
    public static interface OTAlgorithmBase<T> {

        /**
         * Create and register a cursor.
         * @param cursorFactory The factory responsible for creating a cursor instance.
         * @return A registered cursor.
         */
        public OTCursor<T> registerCursor( OTSystemDefinition.OTCursor.OTCursorFactory<T> cursorFactory );

        /**
         * Deregister an cursor object.
         * @param cursor The cursor instance.
         */
        public void deregisterCursor( OTCursor<T> cursor );

        /**
         * Return the history of all executed operations.
         */
        public OTOperationHistory<T> getHistory();
    }

    /**
     * The interface for server-side OT integration algorithm.
     * @param <T> The type of the elements in the data model.
     */
    public static interface OTServerAlgorithm<T> extends OTAlgorithmBase<T>,
            OTOperationIntegrator<T> {

        /**
         * Return the server space state/revision.
         */
        public VectorClock getState();
    }

    /**
     * The sending mechanism is encapsulated behind interface, for ensuring a loose coupling
     * between the OT-system and the underlying network layer.
     * @param <T> The type of the elements in the data model.
     */
    public static interface OperationSender<T> {

        /**
         * Encapsulation of the sending mechanism.
         * @param clientUID The UID of the receiver.
         *                  (In the case that the client sends to the server, the UID is null.)
         * @param op The operation to send.
         */
        public abstract void sendOperation( UUID clientUID, OTOperation<T> op );
    }

    /**
     * The interface for client-side OT integration algorithm.
     * @param <T> The type of the elements in the data model.
     */
    public static interface OTClientAlgorithm<T> extends OTAlgorithmBase<T>,
            OTOperationGenerator<T>, OTOperationIntegrator<T> {

        /**
         * Load the server side operation history.
         * @param history History of all executed operations.
         */
        public void fillHistory( OTOperationHistory<T> history );

        /**
         * Return the history of all executed operations.
         */
   //     public OTOperationHistory<T> getHistory();

        /**
         * Set the state space.
         * @param state State space.
         */
        public void setState( VectorClock state );
    }

    /**
     * This interfaces defines all transformation functions that include the effect
     * of (concurrent/independent) operations.
     */
    public static interface InclusionTransformer<T> {

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
        public abstract Pair<OTOperation<T>, OTOperation<T>>
            transformOperation( boolean isServer, OTOperation<T> remoteOp, OTOperation<T> localOp );

        /**
         * Adapt a cursor according to local or remote operations.
         * @param cursor The cursor that is adapted.
         * @param op The local or remote operation against the cursor is transformed.
         */
        public abstract void transformCursor( OTCursor<T> cursor, OTOperation<T> op );
    }

    /**
     * The OTCursor is kind of abstract pointer to a element in the data model. If local or
     * remote operation is executed, the OTCursor adapts its position according to these operations.
     * On the basis of the OTCursor can stable iterators for data structures like lists be implemented.
     */
    public static abstract class OTCursor<T> {

        /**
         * The state of the cursor. Local or remote operations can delete or update
         * elements the cursor points to.
         */
        public enum CursorState {
            CURSOR_VALID,       // The cursor is valid.
            CURSOR_DELETED,     // The element the cursor points to is deleted.
            CURSOR_UPDATED      // The element the cursor points to is updated.
        }

        /**
         * Factory for creating generic OT cursors.
         * @param <R> The type of data elements the cursor points to.
         */
        public static interface OTCursorFactory<R> {

            public abstract OTCursor<R> createCursor();
        }

        /**
         * Constructor.
         * At beginning the cursor is always valid.
         */
        public OTCursor() {
            this.state = CursorState.CURSOR_VALID;
        }

        /** The state of the cursor. */
        protected CursorState state;

        /**
         * Get the data element the cursor points to.
         */
        public abstract T getElement();

        /**
         * Is called if the cursor is no longer needed.
         */
        public abstract void done();

        /**
         * Get the cursor state.
         */
        public CursorState getCursorState() { return state; }

        /**
         * Set the cursor state.
         */
        public void setCursorState( CursorState state ) { this.state = state; }
    }
}
