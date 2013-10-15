package de.tuberlin.rcd.protocol.types;


import de.tuberlin.rcd.network.Message;
import de.tuberlin.rcd.network.common.Event;
import de.tuberlin.rcd.network.common.IEventDispatcher;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.OTOperationDefinition;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.OTOperationHistory;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.OTSystemDefinition;

/**
 * The common interface for all replicated types. All replicated types are able to dispatch events
 * to notify the environment of state changing actions, e.g. the data type integrated a remote operation.
 * All events builds the event model of the data type. Inherited data types can extend
 * this basic event model to support fine granular actions.
 */
public interface IReplicatedType<T> extends IEventDispatcher {

    //-----------------------------------------------
    // The event model.
    //-----------------------------------------------

    /**
     * ReplicatedString event.
     */
    class ReplicatedTypeEvent extends Event {

        /**
         * Event interface.
         */
        public static final String RS_REMOTE_UPDATE = "rs_remote_update";

        public static final String RS_LOCAL_UPDATE = "rs_local_update";

        public static final String RS_ACKNOWLEDGE = "rs_acknowledge";

        public static final String RS_UPDATE = "rs_update";

        public static final String RS_FILL = "rs_fill";

        /**
         * Constructor.
         */
        public ReplicatedTypeEvent( String type, Object data ) {
            super( type, data );
        }
    }

    /**
     * Start the internal worker thread for remote operation integration.
     */
    public abstract void startProcessing();

    /**
     * Terminate internal worker thread.
     */
    public abstract void stopProcessing();

    /**
     * Enqueue a received remote operation.
     * @param msg The received operation.
     */
    public abstract void enqueueReceivedMsg( Message msg );

    /**
     * Process remote operations before application.
     * @param msg The received message.
     * @param op The remote operation.
     */
    public abstract void processRemoteOperation( Message msg, OTOperationDefinition.OTOperation<T> op );

    /**
     * Process local operations before submission.
     * @param op The remote operation.
     */
    public abstract void processLocalOperation( OTOperationDefinition.OTOperation<T> op );

    /**
     * Return the history of all executed operations.
     * @return The operation history.
     */
    public abstract OTOperationHistory<T> getHistory();

    /**
     * Create a cursor on a fixed position.
     * @param position Index of the data element the cursor points to.
     * @return A registered cursor.
     */
    public abstract OTSystemDefinition.OTCursor<T> createCursor( int position );
}
