package de.tuberlin.rcd.protocol.runtimedefinition;

import java.util.HashMap;
import java.util.Map;

/**
 * Client and Server uses this class for full qualified name resolution.
 * With the help of this class can the client and the server map between the client
 * and server side type names.
 */
public final class TypeNameRegistry {

    /**
     * Constructor.
     */
    public TypeNameRegistry() {
        this.globalToSpecificMap = new HashMap<String, String>();
        this.specificToGlobalMap = new HashMap<String, String>();
    }

    /** Map the global name to the full qualified implementation specific name. */
    private final Map<String,String> globalToSpecificMap;

    /** The reverse mapping. */
    private final Map<String,String> specificToGlobalMap;

    /**
     * Add a replicated type name mapping.
     * @param globalName The global name of a replicated type for client and tests side.
     * @param specificName The full qualified implementation specific name of the replicated type.
     */
    public void insertMapping( String globalName, String specificName ) {
        // sanity check.
        if( globalName == null )
            throw new NullPointerException();
        if( specificName == null )
            throw new NullPointerException();
        if( globalToSpecificMap.containsKey( globalName ) )
            throw new IllegalStateException();
        //if( specificToGlobalMap.containsKey( specificName ) )
        //    throw new IllegalStateException();

        //try {
            //final Class<?> clazz = Class.forName( specificName );
            // TODO:
            //if( !clazz.isAssignableFrom( IReplicatedType.class ) ) {
            //    throw new IllegalStateException();
            //}
        //} catch( ClassNotFoundException e ) {
        //    throw new IllegalStateException( e );
        //}

        globalToSpecificMap.put( globalName, specificName );
        specificToGlobalMap.put( specificName, globalName );
    }

    /**
     * Return the full qualified specific name of the global name.
     * @param globalName The name used at both sides of unique identification.
     * @return The specific name of the global name.
     */
    public String getSpecificName( String globalName ) {
        // sanity check.
        if( globalName == null )
            throw new NullPointerException();
        final String specificName = globalToSpecificMap.get( globalName );
        if( specificName != null )
            return specificName;
        else
            throw new IllegalStateException();
    }

    /**
     * Return the global name.
     * @param specificName The specific name of the global name.
     * @return The name used at both sides of unique identification.
     */
    public String getGlobalName( String specificName ) {
        // sanity check.
        if( specificName == null )
            throw new NullPointerException();
        final String globalName = specificToGlobalMap.get( specificName );
        if( globalName != null )
            return globalName;
        else
            throw new IllegalStateException();
    }
}
