package de.tuberlin.rcd.network.common;


/**
 * The IEventListener interface declares the handler function.
 * @author Tobias Herb
 *
 */
public interface IEventListener {

    /**
     * Declares the handler method for dispatched events.
     */
    void handleEvent( Event event );
}
