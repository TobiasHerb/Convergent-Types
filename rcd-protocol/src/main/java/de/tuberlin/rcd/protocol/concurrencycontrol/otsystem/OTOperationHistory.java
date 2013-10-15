package de.tuberlin.rcd.protocol.concurrencycontrol.otsystem;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class OTOperationHistory<T> implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 794905876329104586L;

	/**
     * Constructor.
     */
    public OTOperationHistory() {
    }

    /**
     * Copy constructor.
     * @param copyHistory The operation history that gets cloned.
     */
    public OTOperationHistory( OTOperationHistory<T> copyHistory ) {
        // sanity check.
        if( copyHistory == null )
            throw new IllegalArgumentException();

        history.addAll( copyHistory.history );
    }

    /** The list storing the operations in execution order. */
    final List<OTOperationDefinition.OTOperation<T>> history =
            new ArrayList<OTOperationDefinition.OTOperation<T>>();

    /**
     * Add a operation to the history.
     * @param op Operation that is pushed to the history end.
     */
    public void pushOperation( OTOperationDefinition.OTOperation<T> op ) {
        history.add( op );
    }

    /**
     * Return all independent operations to a given revision.
     * @param revision The revision of a given operation.
     * @return All concurrent (= independent) operations to the given revision.
     */
    public List<OTOperationDefinition.OTOperation<T>> getConcurrentOperations( int revision ) {
        return Collections.unmodifiableList(
                history.subList( revision, history.size() ) );
    }

    /**
     * Return size of the history.
     * @return Size of the history.
     */
    public int getHistorySize() {
        return history.size();
    }

    /**
     * Get an history iterator.
     * @return History iterator.
     */
    public Iterator<OTOperationDefinition.OTOperation<T>> getHistoryIterator() {
        return history.iterator();
    }
}
