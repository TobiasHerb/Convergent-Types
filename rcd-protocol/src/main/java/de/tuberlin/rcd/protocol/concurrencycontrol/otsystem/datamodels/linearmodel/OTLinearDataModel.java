package de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.datamodels.linearmodel;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import de.tuberlin.rcd.network.common.Pair;
import de.tuberlin.rcd.protocol.concurrencycontrol.common.VectorClock;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.OTOperationDefinition;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.OTSystemDefinition;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.datamodels.linearmodel.OTLinearOperations.DeleteSEOperation;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.datamodels.linearmodel.OTLinearOperations.InsertSEOperation;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.datamodels.linearmodel.OTLinearOperations.UpdateEntityOperation;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.datamodels.linearmodel.OTLinearOperations.UpdateSEOperation;

/**
 * Representing a data model where data elements can be accessed in linear address space.
 * @param <T> Type of the data elements stored in the linear data model.
 * TODO: remove (and verify that) the <code>synchronized</code> modifiers.
 */
public class OTLinearDataModel<T> implements OTSystemDefinition.OTDataModel<T> {

    /**
	 * 
	 */
	private static final long serialVersionUID = -1575449896240299725L;

	private static class ElementDeltaHistory {
        public ElementDeltaHistory() {
            this.deltaHistoryMap = new ArrayList<Pair<VectorClock,Map<String,Object>>>();
        }
        private List<Pair<VectorClock,Map<String,Object>>> deltaHistoryMap;
        public void addDelta( VectorClock vc, Map<String,Object> deltaMap ) {
            deltaHistoryMap.add( new Pair<VectorClock,Map<String, Object>>( vc, deltaMap ) );
        }
        //public Map<String,Object> getDelta( VectorClock vc ) {
        //    return deltaHistoryMap.get( vc );
        //}

        public ListIterator<Pair<VectorClock,Map<String, Object>>> getDeltas() {
            return deltaHistoryMap.listIterator( deltaHistoryMap.size() );
        }
    }

    /**
     * Constructor.
     */
    public OTLinearDataModel( Class<T> typeInfo ) {
        // The array list must be synchronized.
        this.data = Collections.synchronizedList( new ArrayList<Pair<T,ElementDeltaHistory>>() );

        //this.data = Collections.synchronizedList( new ArrayList<T>() );

        this.typeInfo = typeInfo;
    }

    /**
     * Copy constructor.
     */
    public OTLinearDataModel( OTLinearDataModel<T> model ) {
        this.data = Collections.synchronizedList( new ArrayList<Pair<T,ElementDeltaHistory>>( model.data ) );

        //this.data = Collections.synchronizedList( new ArrayList<T>( model.data ) );

        this.typeInfo = model.typeInfo;
    }

    /** A list data structure that store the data elements in linear address space. */
    private final List<Pair<T,ElementDeltaHistory>> data;

    //private final List<T> data;

    /** Type information of the stored data elements. */
    private final Class<T> typeInfo;

    /**
     * Apply a (linear) operation on the data model.
     * @param op The operation to execute.
     */
    @Override
    public synchronized void applyOperation( OTOperationDefinition.OTOperation<T> op ) {
        if( op instanceof OTOperationDefinition.NoOperation ) {
            // do nothing.
        } else if( op instanceof InsertSEOperation ) {
            final InsertSEOperation<T> insertOp = (InsertSEOperation<T>)op;
            // check if operation position is valid.
            if( insertOp.position > data.size() ) {
                throw new IllegalStateException( "op.pos: " + insertOp.position +
                        " > " + "data.size: " + data.size() );
            }

            // --------
            /*final T copiedElement;
            try {
                Class<?>  adaptedTypeInfo;
                if( typeInfo == Character.class ) {
                    adaptedTypeInfo = char.class;
                } else {
                    adaptedTypeInfo = typeInfo;
                }
                copiedElement = typeInfo.getConstructor( adaptedTypeInfo )
                        .newInstance( insertOp.insertedElement );
            } catch( Exception e ) {
                throw new IllegalStateException( e );
            }*/
            // --------


            data.add( insertOp.position, new Pair<T,ElementDeltaHistory>( insertOp.insertedElement, new ElementDeltaHistory() ) );
            //data.add( insertOp.position, new Pair<T,ElementDeltaHistory>( copiedElement, new ElementDeltaHistory() ) );
            //data.add( insertOp.position, insertOp.insertedElement );

        } else if( op instanceof DeleteSEOperation ) {
            final DeleteSEOperation<T> deleteOp = (DeleteSEOperation<T>)op;
            // check if operation position is valid.
            if( deleteOp.position >= data.size() ) {
                throw new IllegalStateException();
            }
            data.remove( deleteOp.position );

        } else if( op instanceof UpdateSEOperation ) {
            final UpdateSEOperation<T> updateOp = (UpdateSEOperation<T>)op;
            // check if operation position is valid.
            if( updateOp.position >= data.size() ) {
                throw new IllegalStateException();
            }

            // --------
            /*final T copiedElement;
            try {
                Class<?>  adaptedTypeInfo;
                if( typeInfo == Character.class ) {
                    adaptedTypeInfo = char.class;
                } else {
                    adaptedTypeInfo = typeInfo;
                }
                copiedElement = typeInfo.getConstructor( adaptedTypeInfo )
                        .newInstance( updateOp.updatedElement );
            } catch( Exception e ) {
                throw new IllegalStateException( e );
            }*/
            // --------

            data.set( updateOp.position, new Pair<T,ElementDeltaHistory>( updateOp.updatedElement, new ElementDeltaHistory() ) );
            //data.set( updateOp.position, new Pair<T,ElementDeltaHistory>( copiedElement, new ElementDeltaHistory() ) );
            //data.set( updateOp.position, copiedElement );
            //data.set( updateOp.position, updateOp.updatedElement );

        } else if( op instanceof UpdateEntityOperation ) {

            final UpdateEntityOperation<T> updateEntityOp = (UpdateEntityOperation<T>)op;
            if( updateEntityOp.entityClazz != typeInfo ) {
                throw new IllegalStateException();
            }

            Pair<T,ElementDeltaHistory> elementAndHistory = data.get( updateEntityOp.position );
            T element = elementAndHistory.getFirst();
            ElementDeltaHistory edh = elementAndHistory.getSecond();

            final Map<String,Object> rollbackDeltaMap = new HashMap<String, Object>();
            for( String propertyName : updateEntityOp.entityDelta.keySet() ) {
                try {
                    Field field = element.getClass().getField( propertyName );
                    try {
                        final Object value = field.get( element );
                        rollbackDeltaMap.put( propertyName, value );
                    } catch( IllegalAccessException e ) {
                        throw new IllegalStateException( e );
                    }
                } catch (NoSuchFieldException e) {
                    throw new IllegalStateException( e );
                }
            }

            for( String propertyName : updateEntityOp.entityDelta.keySet() ) {
                try {
                    Field field = element.getClass().getField( propertyName );
                    try {
                        field.set( element, updateEntityOp.entityDelta.get( propertyName ) );
                    } catch( IllegalAccessException e ) {
                        throw new IllegalStateException( e );
                    }
                } catch (NoSuchFieldException e) {
                    throw new IllegalStateException( e );
                }
            }

            if( op.getMetaData() == null ) {
                throw new IllegalArgumentException();
            }

            edh.addDelta( op.getMetaData().state, rollbackDeltaMap );
        }
    }

    public synchronized void rollBackDataElement( int position, VectorClock vc ) {
        Pair<T,ElementDeltaHistory> elementAndHistory = data.get( position );
        ElementDeltaHistory edh = elementAndHistory.getSecond();
        ListIterator<Pair<VectorClock,Map<String,Object>>> iter = edh.getDeltas();
        while( iter.hasPrevious() ) {
            Pair<VectorClock,Map<String,Object>> delta = iter.previous();
            if(  vc.get( 0 ) < delta.getFirst().get( 0 ) ) {
                final Map<String,Object> rollbackDeltaMap = delta.getSecond();

                //final UpdateEntityOperation<T> rollbackOp = new UpdateEntityOperation<T>( position, typeInfo, rollbackDeltaMap );
                //applyOperation( rollbackOp );

                for( String propertyName : rollbackDeltaMap.keySet() ) {
                    try {
                        Field field = elementAndHistory.getFirst().getClass().getField( propertyName );
                        try {
                            field.set( elementAndHistory.getFirst(), rollbackDeltaMap.get( propertyName ) );
                        } catch( IllegalAccessException e ) {
                            throw new IllegalStateException( e );
                        }
                    } catch (NoSuchFieldException e) {
                        throw new IllegalStateException( e );
                    }
                }

            }
        }
    }

    /**
     * Get direct access to the data.
     */
    @Override
    public synchronized Object getData() {
        return Collections.unmodifiableList( data );
    }

    //public List<T> getDataAsList() {
    //    return Collections.unmodifiableList( data );
    //}

    public T getDataElement( int pos ) {
        return data.get( pos ).getFirst();
    }

    /**
     * Fill the model at runtime with new content.
     * @param model The content.
     */
    @Override
    @SuppressWarnings("unchecked")
    public synchronized void fillModel( OTSystemDefinition.OTDataModel<T> model ) {
        // sanity check.
        if( model == null )
            throw new IllegalArgumentException();
        data.addAll( (List<Pair<T,ElementDeltaHistory>>)model.getData() );
    }

    /**
     * Build a string representation of the data model.
     * @return A string representing the model content.
     */
    @Override
    public synchronized String toString() {
        final StringBuilder strBuilder = new StringBuilder();
        for( Pair<T,ElementDeltaHistory> c : data ) {
            strBuilder.append(c.getFirst().toString());
        }
        return strBuilder.toString();
    }

    /**
     * Return the number of elements in the data model.
     */
    @Override
    public synchronized int size() {
        return data.size();
    }
}
