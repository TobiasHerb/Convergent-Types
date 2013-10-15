package de.tuberlin.rcd.protocol.types.string;

import de.tuberlin.rcd.network.common.IEventDispatcher;

/**
 * Interface for a replicated string.
 */
public interface IReplicatedString extends CharSequence, IEventDispatcher {

    //-----------------------------------------------
    // ReplicatedString interface.
    //-----------------------------------------------

    /**
     * Insert a character at a given position.
     * @param pos The position to insert.
     * @param character The character to insert.
     */
    public abstract void insertChar( int pos, char character );

    /**
     * Delete a character at a given position.
     * @param pos The position to delete.
     */
    public abstract void deleteChar( int pos );
}