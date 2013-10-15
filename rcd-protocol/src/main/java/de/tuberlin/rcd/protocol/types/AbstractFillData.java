package de.tuberlin.rcd.protocol.types;

import java.io.Serializable;

/**
 * The std class of transmitted fill data. If a client registers itself by a replicated type,
 * the client-side instance of the replicated type must be filled with the state information from
 * the server (they must become initially synchronous in state). The framework supports two types
 * of fill data:
 *      (1) The state based fill data. This is the serialized data model state
 *          that is transmitted to the server.
 *      (2) The operation based fill data. The complete operation history is transmitted to the client
 *          and there applied to the local replicated instance of that type. The advantage of the operation
 *          based style is that it allows a replay function. The disadvantage is the increased size
 *          compared to the state based fill data.
 */
public abstract class AbstractFillData implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8478034513319401021L;
}
