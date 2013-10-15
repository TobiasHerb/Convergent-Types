package de.tuberlin.rcd.protocol.runtimedefinition;


import java.util.UUID;

import de.tuberlin.rcd.protocol.types.IReplicatedType;

/**
 * Generic replicated data types can not be created by the reflection api, because of javas type erasure.
 * (The type information of the parametric type gets lost after the compilation process.) This factory is a
 * workaround to support the creation of generic types. It is passed to data managers methods
 * <code>createReplicatedType</code> or <code>registerByReplicatedType</code>.
 * @param <G> The type of the data model elements.
 */
public interface IReplicatedObjectFactory<G> {

    /**
     * This method is internally called by the <code>createReplicatedType</code> or
     * <code>registerByReplicatedType</code> methods of the data manager api.
     * @param name The name of the replicated (generic) data type.
     * @param typeUID The unique ID of the replicated data type.
     * @param dataManager A reference to the data manager.
     * @return A reference to a the new created instance.
     */
    public abstract IReplicatedType<G> instantiate( final String name, final UUID typeUID, final IDataManager dataManager );

}
