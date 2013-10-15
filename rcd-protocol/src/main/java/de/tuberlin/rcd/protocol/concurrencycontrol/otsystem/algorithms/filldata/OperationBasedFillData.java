package de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.algorithms.filldata;

import de.tuberlin.rcd.protocol.concurrencycontrol.common.VectorClock;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.OTOperationHistory;
import de.tuberlin.rcd.protocol.types.AbstractFillData;

public class OperationBasedFillData<T> extends AbstractFillData {

    /**
	 * 
	 */
	private static final long serialVersionUID = 588876511111332122L;

	/**
     * Constructor.
     * @param history The operation history.
     */
    public OperationBasedFillData(OTOperationHistory<T> history,
                                  VectorClock state) {
        // sanity check.
        if( history == null )
            throw new IllegalArgumentException();

        this.history = history;
        this.state = new VectorClock( state );
    }

    /** History (in execution order) of the server side. */
    public final OTOperationHistory<T> history;

    /** Current server state */
    public final VectorClock state;
}
