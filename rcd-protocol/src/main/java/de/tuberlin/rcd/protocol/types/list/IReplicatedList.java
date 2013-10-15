package de.tuberlin.rcd.protocol.types.list;


import java.util.List;

import de.tuberlin.rcd.network.common.IEventDispatcher;

/**
 * The replicated List. This interface inherits from javas <code>List</code> interface and additionally
 * the <code>IEventDispatcher</code> interface for notifying the surrounding environment.
 */
public interface IReplicatedList<T> extends List<T>, IEventDispatcher {
}
