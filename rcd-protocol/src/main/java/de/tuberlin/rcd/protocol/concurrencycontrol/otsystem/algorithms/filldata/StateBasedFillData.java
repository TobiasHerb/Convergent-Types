package de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.algorithms.filldata;

import de.tuberlin.rcd.protocol.concurrencycontrol.common.VectorClock;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.OTSystemDefinition;
import de.tuberlin.rcd.protocol.types.AbstractFillData;

public class StateBasedFillData<T> extends AbstractFillData {

    /**
	 * 
	 */
	private static final long serialVersionUID = -6434971845329247052L;

	/**
     * Constructor.
     * @param model The serialized server data model.
     */
    public StateBasedFillData(OTSystemDefinition.OTDataModel<T> model,
                              VectorClock state) {
        // sanity check.
        if( model == null )
            throw new IllegalArgumentException();

        this.model = model;
        this.state = new VectorClock( state );
    }

    /** History (in execution order) of the server side. */
    public final OTSystemDefinition.OTDataModel<T> model;

    /** Current server state */
    public final VectorClock state;
}
