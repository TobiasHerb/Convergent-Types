package de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.algorithms.jupiter;

import java.util.UUID;

import de.tuberlin.rcd.protocol.concurrencycontrol.common.VectorClock;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.OTOperationDefinition;

public class JupiterOperationMetaData extends OTOperationDefinition.OTOperationMetaData  {

    /**
	 * 
	 */
	private static final long serialVersionUID = 2687625992037748814L;

	/**
     * Constructor.
     * @param client UID of the sending client.
     * @param state State of the sending component.
     */
    public JupiterOperationMetaData( UUID client, VectorClock state ) {
        super( client, state );
        //this.state = new VectorClock( state );
    }

    /**
     * State of the sending component.
     * In case of the Jupiter algorithm its the count of
     * local and remote operations.
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
