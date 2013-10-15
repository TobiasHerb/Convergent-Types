package de.tuberlin.rcd.protocol.runtimedefinition;

import java.util.Collection;
import java.util.UUID;

import de.tuberlin.rcd.protocol.types.IReplicatedType;

/**
 * Interface for client and tests side data manager.
 * Both client and server have data manager that manages the replicated types.
 * This interface specifies their common functionality.
 */
public interface IDataManager {

    /**
     * Create a new replicated type. Implemented in client and tests specialization.
     * @param name The name of the new replicated type.
     * @param clientUID Only used in the tests implementation.
     * @param clazz The class information about the replicated type.
     * @return A instance of the replicated type.
     */
	@SuppressWarnings("rawtypes") 
    public abstract IReplicatedType<?> createReplicatedType( String name, UUID clientUID, Class<? extends IReplicatedType> clazz );

    /**
     * Create a new generic replicated type. Implemented in client and tests specialization.
     * @param name The name of the new replicated type.
     * @param factory The factory is responsible for creating a local instance of the generic type.
     * @return A instance of the replicated type.
     */
    public abstract <G> IReplicatedType<G> createReplicatedType( String name, IReplicatedObjectFactory<G> factory );

    /**
     * Client registers with a replicated data type. (Only client side)
     * Implemented in client specialization.
     * @param name The name of the replicated type.
     * @param clazz The class information about the replicated type.
     * @return A instance of the replicated type.
     */
    @SuppressWarnings("rawtypes") 
    public abstract IReplicatedType<?> registerByReplicatedType( String name, Class<? extends IReplicatedType> clazz );

    /**
     * Client registers with a replicated generic data type. (Only client side)
     * @param name The name of the new replicated type.
     * @param factory The factory is responsible for creating a local instance of the generic type.
     * @return A instance of the replicated type.
     */
    public abstract <G> IReplicatedType<G> registerByReplicatedType( String name, IReplicatedObjectFactory<G> factory );

    /**
     * Remove/Delete the replicated data type.
     * @param name The name of the replicated type.
     */
    public abstract void removeReplicatedType( String name );

    /**
     * Test if the replicated data type exists.
     * @param name The name of the replicated type.
     * @return True if the data type exists.
     */
    public abstract boolean existsReplicatedType( String name );

    /**
     * Get the replicated data with the given name.
     * @param name The name of the replicated type.
     * @return The data type if exists else null should be returned.
     */
    public abstract IReplicatedType<?> getReplicatedType( String name );

    /**
     * Return the collection of all active replicated data types.
     * @return A collection of all replicated data types.
     */
    @SuppressWarnings("rawtypes")
	public abstract Collection<? extends IReplicatedType> getAllReplicatedTypes();
}
