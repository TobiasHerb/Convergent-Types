package de.tuberlin.rcd.protocol.concurrencycontrol.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Vector clocks is an algorithm for generating a partial ordering of events in a
 * distributed system and detecting causality violations. Just as in Lamport timestamps,
 * interprocess messages contain the state of the sending process's logical state. A vector
 * state of a system of N processes is an array/vector of N logical clocks, one state per
 * process; a local "smallest possible values" copy of the global state-array is kept in each
 * process, with the following rules for state updates:
 * Initially all clocks are zero:
 *
 * (1) Each time a process experiences an internal event, it increments its own
 *     logical state in the vector by one.
 * (2) Each time a process prepares to send a message, it increments its own logical
 *     state in the vector by one and then sends its entire vector along with the
 *     message being sent.
 * (3) Each time a process receives a message, it increments its own logical state
 *     in the vector by one and updates each element in its vector by taking the maximum
 *     of the value in its own vector state and the value in the vector in the received
 *     message (for every element).
 */
public class VectorClock implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 6973036324174883251L;

	/**
     * Constructor.
     * @param N The initial number of components.
     */
    public VectorClock( int N ) {
        // sanity check.
        if( N < 1 )
            throw new IllegalArgumentException();
        components = new ArrayList<Integer>();
        for( int i = 0; i < N; ++i )
            components.add( 0 );
    }

    /**
     * Constructor.
     * @param components The components.
     */
    public VectorClock( Integer... components ) {
        // sanity check.
        if( components == null )
            throw new IllegalArgumentException();
        this.components = Arrays.asList( components );
    }

    /**
     * Copy-constructor.
     * @param vc The reference of the instance that is copied.
     */
    public VectorClock( final VectorClock vc ) {
        // sanity check.
        if( vc == null )
            throw new IllegalArgumentException();
        components = new ArrayList<Integer>( vc.components );
    }

    /** Store an integer value, for each source of concurrency (process).
        Implemented as list because of the varying count of processes over time. */
    final List<Integer> components;

    /**
     * Increment a component of the vector state.
     * @param index The index of the component that gets incremented.
     */
    public void inc( int index ) {
        components.set( index, ( components.get( index ) + 1 ) );
    }

    /**
     * Set the value of a component.
     * @param index The index of the component.
     * @param value The new value.
     */
    public void set( int index, int value ) {
        components.set( index, value );
    }

    /**
     * Return the value of a component.
     */
    public int get( int index ) {
        return components.get( index );
    }

    /**
     * Copy the content between the vector clocks.
     */
    public void copy( VectorClock vc ) {
        // sanity check.
        if( this.components.size() != vc.components.size() )
            throw new IllegalArgumentException();
        for( int i = 0; i < components.size(); ++i )
            components.set( i, vc.get( i ) );
    }

    /**
     * Return a string representation of the vector clock.
     */
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append( "[" );
        for( int i = 0; i < components.size(); ++i )
            sb.append( "(" )
              .append( i )
              .append( ":" )
              .append( components.get( i ) )
              .append( ")" )
              .append( "," );
        return sb.deleteCharAt( sb.length() - 1 )
                 .append( "]" )
                 .toString();
    }
}
