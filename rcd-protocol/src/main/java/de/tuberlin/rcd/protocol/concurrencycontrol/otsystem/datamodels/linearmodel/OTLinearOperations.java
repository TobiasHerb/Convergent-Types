package de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.datamodels.linearmodel;

import java.util.HashMap;
import java.util.Map;

import de.tuberlin.rcd.network.common.Pair;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.OTOperationDefinition;

/**
 * Defines the operations for the linear data model.
 */
public class OTLinearOperations {

    /**
     * Abstract operation, that owns a positional parameter to address a data element in a data model.
     * All operations that operate on a linear data model must be derived from this class.
     * @param <T> The type of the data element.
     */
    public static abstract class PositionalOperation<T> extends OTOperationDefinition.OTOperation<T> {

        /**
		 * 
		 */
		private static final long serialVersionUID = 6390469211935368526L;

		/**
         * Constructor.
         * @param position The index of the data element in data model
         *                 that is affected by the operation.
         */
        public PositionalOperation( int position ) {
            this.position = position;
        }

        /** The index of the data element in data model that is affected by the operation. */
        public final int position;
    }

    /**
     * Delete a element from a data model.
     * @param <T> The type of the data element.
     */
    public static class DeleteSEOperation<T> extends PositionalOperation<T> {

        /**
		 * 
		 */
		private static final long serialVersionUID = -2416878068767597708L;

		/**
         * Constructor.
         * @param position The index of the data element that gets deleted.
         */
        public DeleteSEOperation( int position ) {
            super( position );
        }

        /**
         * Copy constructor.
         * @param op The source operation that gets cloned.
         */
        public DeleteSEOperation( final DeleteSEOperation<T> op ) {
            this( op.position );
            this.setMetaData( op.getMetaData() );
        }

        /**
         * Return a string representation of the operation.
         */
        @Override
        public String toString() {
            return "[del( " + position + " )]";
        }
    }

    /**
     * Inserts a element in a data model.
     * @param <T> The type of the data element.
     */
    public static class InsertSEOperation<T> extends PositionalOperation<T> {

        /**
		 * 
		 */
		private static final long serialVersionUID = -5403396402399733069L;

		/**
         * Constructor.
         * @param position The index of the data element that gets inserted.
         * @param insertedElement The element that gets inserted.
         */
        public InsertSEOperation( int position, T insertedElement ) {
            super( position );
            // sanity check.
            if( insertedElement == null )
                throw new IllegalArgumentException();

            this.insertedElement = insertedElement;
        }

        /**
         * Copy constructor.
         * @param op The source operation that gets cloned.
         */
        public InsertSEOperation( final InsertSEOperation<T> op ) {
            this( op.position, op.insertedElement );
            this.setMetaData( op.getMetaData() );
        }

        /** The new data element that gets inserted. */
        public final T insertedElement;

        /**
         * Return a string representation of the operation.
         */
        @Override
        public String toString() {
            return "[ins( " + position + ", " + insertedElement + " )]";
        }
    }

    /**
     * The std class for update operations. This std class is introduced to reduce the amount of code
     * for handling the update operations in the linear transformer class.
     * @param <T> The type of the data element.
     */
    public static abstract class UpdateOperationBase<T> extends PositionalOperation<T> {

        /**
		 * 
		 */
		private static final long serialVersionUID = -8420967032157124891L;

		/**
         * Constructor.
         * @param position The index of the data element in data model
         *                 that is affected by the operation.
         */
        public UpdateOperationBase( int position) {
            super(position);
        }
    }

    /**
     * Updates a element in a data model.
     * @param <T> The type of the data element.
     */
    public static class UpdateSEOperation<T> extends UpdateOperationBase<T> {

        /**
		 * 
		 */
		private static final long serialVersionUID = 3600771639876725258L;

		/**
         * Constructor.
         * @param position The index of the data element that gets updated.
         * @param updatedElement The element that gets updated.
         */
        public UpdateSEOperation( int position, T updatedElement ) {
            super( position );
            this.updatedElement = updatedElement;
        }

        /**
         * Copy constructor.
         * @param op The source operation that gets cloned.
         */
        public UpdateSEOperation( final UpdateSEOperation<T> op ) {
            this( op.position, op.updatedElement );
            this.setMetaData( op.getMetaData() );
        }

        /** The new data element that gets updated. */
        public final T updatedElement;

        /**
         * Return a string representation of the operation.
         */
        @Override
        public String toString() {
            return "[upd( " + position + ", " + updatedElement + " )]";
        }
    }

    /**
     * Update a complex entity element in a data model.
     * @param <T> The type of the data element.
     */
    public static class UpdateEntityOperation<T> extends UpdateOperationBase<T> {

        /**
		 * 
		 */
		private static final long serialVersionUID = 1505739643517045631L;

		/**
         * Constructor.
         * @param position The index of the data element that gets updated.
         * @param entityClazz The type information of the data element that gets updated.
         * @param entityDelta A map that stores the property name and the new value for the data element.
         */
        public UpdateEntityOperation( int position, Class<T> entityClazz, Map<String,Object> entityDelta ) {
            super( position );
            // sanity check.
            if( entityClazz == null )
                throw new IllegalArgumentException();
            if( entityDelta == null )
                throw new IllegalArgumentException();

            this.entityClazz = entityClazz;
            this.entityDelta = new HashMap<String,Object>( entityDelta );
            verifyDeltaProperties();
        }

        /**
         * Copy constructor.
         * @param op The source operation that gets cloned.
         */
        public UpdateEntityOperation( final UpdateEntityOperation<T> op ) {
            this( op.position, op.entityClazz, op.entityDelta );
            this.setMetaData( op.getMetaData() );
        }

        /** The type information of the data element that gets updated. */
        public final Class<T>  entityClazz;

        /** The map that stores the property name and the new value for the data element. */
        public final Map<String,Object> entityDelta;

        /**
         * Return a string representation of the operation.
         */
        @Override
        public String toString() {
            final StringBuilder strBuilder = new StringBuilder();
            for( String fieldName : entityDelta.keySet() ) {
                strBuilder.append( "( " )
                .append( fieldName )
                .append( ", " )
                .append( entityDelta.get( fieldName ) )
                .append( " )" );
            }
            return "[upd( " + position + ", " + strBuilder.toString() + " )]";
        }

        /**
         * Check if all properties defined in the delta map
         * exists in the entity type that gets updated.
         */
        private void verifyDeltaProperties() {
            for( String property : entityDelta.keySet() ) {
                try {
                    // if the field does not exist a exception is thrown.
                    entityClazz.getField( property );
                } catch( NoSuchFieldException e ) {
                    throw new IllegalStateException( e );
                }
            }
        }
    }

    /**
     * Helper class.
     */
    public static final class OperationHelper {

        // prohibit instantiation.
        private OperationHelper() {}

        /**
         * Change the positional index of a operation. This method creates a operation with adapted position.
         * @param sourceOp The source operation that must adapted.
         * @param newPosition The position of the operation.
         * @param <T> The type of the data element.
         * @return The adapted operation.
         */
        public static <T> PositionalOperation<T> adaptOperation( PositionalOperation<T> sourceOp, int newPosition ) {
            // sanity check.
            if( sourceOp == null )
                throw new IllegalArgumentException();

            if( sourceOp instanceof InsertSEOperation ) {
                final InsertSEOperation<T> insertOp = (InsertSEOperation<T>)sourceOp;
                final InsertSEOperation<T> newInsertOp = new InsertSEOperation<T>( newPosition, insertOp.insertedElement );
                newInsertOp.setMetaData( insertOp.getMetaData() );
                return newInsertOp;

            } else if( sourceOp instanceof DeleteSEOperation ) {
                final DeleteSEOperation<T> deleteOp = (DeleteSEOperation<T>)sourceOp;
                final DeleteSEOperation<T> newDeleteOp = new DeleteSEOperation<T>( newPosition );
                newDeleteOp.setMetaData( deleteOp.getMetaData() );
                return newDeleteOp;

            } else if( sourceOp instanceof UpdateSEOperation ) {
                final UpdateSEOperation<T> updateOp = (UpdateSEOperation<T>)sourceOp;
                final UpdateSEOperation<T> newUpdateOp = new UpdateSEOperation<T>( newPosition, updateOp.updatedElement );
                newUpdateOp.setMetaData( updateOp.getMetaData() );
                return newUpdateOp;
            }

            throw new IllegalStateException();
        }

        /**
         * Helper Function, that just builds a <code>Pair</code> object.
         * @param remoteOp The remote operation.
         * @param localOp The local Operation.
         * @param <T> The type of the data elements stored in the data model.
         * @return A <code>Pair</code> instance, where first element is the remote operation
         *         and the second  is the local operation.
         */
        public static <T> Pair<OTOperationDefinition.OTOperation<T>, OTOperationDefinition.OTOperation<T>>
                buildPair(OTOperationDefinition.OTOperation<T> remoteOp, OTOperationDefinition.OTOperation<T> localOp) {
            return new Pair<OTOperationDefinition.OTOperation<T>, OTOperationDefinition.OTOperation<T>>( remoteOp, localOp );
        }

        /**
         * Shallow copy of linear operations + nop. The metadata and other stuff is NOT duplicated.
         * @param op The operation that is duplicated.
         * @param <T> The type of the data elements stored in the data model.
         * @return A shallow copy of the operation.
         */
        public static <T> OTOperationDefinition.OTOperation<T> shallowCopyOperation( OTOperationDefinition.OTOperation<T> op ) {
            // sanity check.
            if( op == null )
                throw new IllegalArgumentException();

            if( op instanceof OTOperationDefinition.NoOperation ) {
                return new OTOperationDefinition.NoOperation<T>( (OTOperationDefinition.NoOperation<T>)op );
            } else if( op instanceof DeleteSEOperation ) {
                return new DeleteSEOperation<T>( (DeleteSEOperation<T>)op );
            } else if( op instanceof InsertSEOperation ) {
                return new InsertSEOperation<T>( (InsertSEOperation<T>)op );
            } else if( op instanceof UpdateSEOperation ) {
                return new UpdateSEOperation<T>( (UpdateSEOperation<T>)op );
            } else if( op instanceof UpdateEntityOperation ) {
                return new UpdateEntityOperation<T>( (UpdateEntityOperation<T>)op );
            }

            throw new IllegalStateException();
        }
    }
}
