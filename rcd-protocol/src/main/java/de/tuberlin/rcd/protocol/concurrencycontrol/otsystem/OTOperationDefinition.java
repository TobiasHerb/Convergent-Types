package de.tuberlin.rcd.protocol.concurrencycontrol.otsystem;

import java.io.Serializable;
import java.util.UUID;

import de.tuberlin.rcd.protocol.concurrencycontrol.common.VectorClock;

/**
 * The basic OT operation definition.
 */
public class OTOperationDefinition {

    /**
     * Operation metadata. Every operation is associated with metadata object, that
     * information about operation creator and further algorithm specific metadata.
     */
    public static abstract class OTOperationMetaData implements Serializable {

        /**
		 * 
		 */
		private static final long serialVersionUID = 7056878298237492758L;

		/**
         * Constructor.
         * @param creator The UID of the client that created the operation.
         */
        public OTOperationMetaData( UUID creator, VectorClock state ) {
            // sanity check.
            if( creator == null )
                throw new IllegalArgumentException();
            if( state == null )
                throw new IllegalArgumentException();

            this.creator = creator;
            this.state = new VectorClock( state );
        }

        /** UID of the client that created the operation. */
        final public UUID  creator;

        public final VectorClock state;

        /**
         * Return a string representation of the operation.
         */
        @Override
        public String toString() {
            return "[ClientUID: " + creator.toString() + "]";
        }
    }

    /**
     * The abstract std of all OT operations. Here is the association to
     * operation metadata defined.
     * @param <T> The type of the data elements in the data model.
     */
    public static abstract class OTOperation<T> implements Serializable {

        /**
		 * 
		 */
		private static final long serialVersionUID = -5215694069844903003L;
		/** Reference to the operation meta data. */
        private OTOperationMetaData metaData;

        /**
         * Set the metadata object.
         */
        public void setMetaData( OTOperationMetaData metaData ) {
            this.metaData = metaData;
        }

        /**
         * Get the metadata object.
         */
        public OTOperationMetaData getMetaData() {
            return metaData;
        }
    }

    /**
     * No operation. This operation has no effect on the data model.
     * @param <T> The type of the data elements in the data model.
     */
    public static class NoOperation<T> extends OTOperation<T> {

        /**
		 * 
		 */
		private static final long serialVersionUID = -7696136245908881298L;

		/**
         * Constructor.
         */
        public NoOperation() {}

        /**
         * Copy constructor.
         * @param op The source operation that gets cloned.
         */
        public NoOperation( final NoOperation<T> op ) {
            this.setMetaData( op.getMetaData() );
        }

        /**
         * Return a string representation of the operation.
         */
        @Override
        public String toString() {
            return "[nop()]";
        }
    }
}
