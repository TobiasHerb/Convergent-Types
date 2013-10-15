package de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.algorithms.wave;

import java.util.UUID;

import de.tuberlin.rcd.protocol.concurrencycontrol.common.VectorClock;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.OTOperationDefinition.OTOperationMetaData;

/**
 *
 */
public class WaveOperationMetaData extends OTOperationMetaData {

    /**
	 * 
	 */
	private static final long serialVersionUID = -4316782563942665194L;

	/**
     * Constructor.
     * @param client UID of the sending client.
     * @param state State of the sending component.
     */
    public WaveOperationMetaData( UUID client, VectorClock state ) {
        super( client, state );
        //this.state = new VectorClock( state );
    }

    /**
     * State of the sending component.
     * In case of the Wave algorithm its just the server revision.
     */
    //public final VectorClock state;

    /**
     * Return a string representation of the meta data.
     */
    @Override
    public String toString() {
        return "[ClientUID: " + creator.toString() + ", State: " + state +"]";
    }
}
