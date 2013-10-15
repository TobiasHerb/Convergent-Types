package de.tuberlin.rcd.client.types.list;


import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.UUID;

import de.tuberlin.rcd.client.types.AbstractClientReplicatedType;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.OTSystemDefinition;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.algorithms.ClientOTConcurrencyControlFactory;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.algorithms.wave.WaveClientAlgorithm;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.datamodels.linearmodel.OTLinearCursor;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.datamodels.linearmodel.OTLinearDataModel;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.datamodels.linearmodel.OTLinearTransformer;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.datamodels.linearmodel.conflictmng.LastWriterWinsConflictSolver;
import de.tuberlin.rcd.protocol.runtimedefinition.IDataManager;
import de.tuberlin.rcd.protocol.types.AbstractReplicatedType;
import de.tuberlin.rcd.protocol.types.list.IReplicatedList;

/**
 * The client-side list implementation. The replicated list implement the java
 * <code>List</code> interface and can hence be used as a simple list in the client code.
 * @param <T> The type of the elements in the data model.
 */
public class ClientReplicatedList<T> extends AbstractClientReplicatedType<T>
        implements IReplicatedList<T> {

    /**
     * Log4J.
     */
    //private static final Logger LOGGER = Logger.getLogger( ClientReplicatedList.class );

    /**
     * Constructor.
     */
    public ClientReplicatedList(String name, UUID typeUID, final Class<T> typeInfo, IDataManager dataManager) {
        super( new ClientOTConcurrencyControlFactory<T>() {

            @Override
            public OTSystemDefinition.OTClientAlgorithm<T>
                injectControlAlgorithm( UUID clientUID,
                                        OTSystemDefinition.OTDataModel<T> dataModel,
                                        OTSystemDefinition.InclusionTransformer<T> transformer,
                                        OTSystemDefinition.OperationSender<T> sender ) {
                return new WaveClientAlgorithm<T>( clientUID, dataModel, transformer, sender );
            }

            @Override
            public OTSystemDefinition.InclusionTransformer<T> injectInclusionTransformer( final OTSystemDefinition.OTDataModel<T> dataModel ) {
                return new OTLinearTransformer<T>(
                        new LastWriterWinsConflictSolver<T>( true, dataModel )
                );
            }

            @Override
            public OTSystemDefinition.OTDataModel<T> injectDataModel() {
                return new OTLinearDataModel<T>( typeInfo );
            }

        }, name, typeUID, dataManager);

        cursor = (OTLinearCursor<T>) createCursor( 0 );
    }

    /** Internal cursor, for list modification methods. */
    private final OTLinearCursor<T> cursor;

    /**
     * Create a cursor on a fixed position.
     * @param position Index of the data element the cursor points to.
     * @return A registered cursor.
     */
    @Override
    public OTSystemDefinition.OTCursor<T> createCursor( final int position ) {
        // block the calling thread until the fill sequence is finished.
        //waitUntilFilled();
        final AbstractReplicatedType<T> self = this;
        return clientIntegrator.registerCursor( new OTSystemDefinition.OTCursor.OTCursorFactory<T>() {
            @Override
            public OTSystemDefinition.OTCursor<T> createCursor() {
                return new OTLinearCursor<T>( self, position );
            }
        });
    }

    //-----------------------------------------------
    // Modification methods.
    //-----------------------------------------------

    @Override
    public synchronized T set( int index, T element ) {
        // sanity check.
        if( element == null )
            throw new IllegalArgumentException();
        cursor.reset( index );
        return cursor.replaceElement( element );

        //2)
        // sanity check.
        /*if( element == null )
            throw new IllegalArgumentException();
        final OTLinearCursor<T> cursor = (OTLinearCursor<T>)createCursor( index );
        T ret = cursor.replaceElement( element );
        cursor.done();
        return ret;*/

        //1)
        /*final List<T> m = (List<T>)model.getData();
        // sanity check.
        if( index > m.size() || index < 0 )
            throw new IndexOutOfBoundsException();
        final OTOperationDefinition.OTOperation<T> updateOp
                = new OTLinearOperations.UpdateSEOperation<T>( index, element );
        final T displacedElement = m.get( index );
        processLocalOperation( updateOp );
        return displacedElement;*/
    }

    @Override
    public synchronized void add( int index, T element ) {
        // sanity check.
        if( element == null )
            throw new IllegalArgumentException();
        cursor.reset( index );
        cursor.insertElement( element );

        //2)
        // sanity check.
        /*if( element == null )
            throw new IllegalArgumentException();
        final OTLinearCursor<T> cursor = (OTLinearCursor<T>)createCursor( index );
        cursor.insertElement( element );
        cursor.done();*/

        //1)
        //if( element == null )
        //    throw new IllegalArgumentException();
        //cur.reset( index );
        //cur.insertElement( element );
        //cursor.done();

        /*final List<T> m = (List<T>)model.getData();
        // sanity check.
        if( index > m.size() || index < 0 )
            throw new IndexOutOfBoundsException();
        final OTOperationDefinition.OTOperation<T> insertOp
                = new OTLinearOperations.InsertSEOperation<T>( index, element );
        processLocalOperation( insertOp );*/
    }

    @Override
    public synchronized T remove( int index ) {
        cursor.reset( index );
        return cursor.deleteElement();

        //2)
        /*final OTLinearCursor<T> cursor = (OTLinearCursor<T>)createCursor( index );
        T ret = cursor.deleteElement();
        cursor.done();
        return ret;*/

        //1)
        /*final List<T> m = (List<T>)model.getData();
        // sanity check.
        if( index > m.size() || index < 0 )
            throw new IndexOutOfBoundsException();
        final OTOperationDefinition.OTOperation<T> deleteOp
                = new OTLinearOperations.DeleteSEOperation<T>( index );
        final T removedElement = m.get( index );
        processLocalOperation( deleteOp );
        return removedElement;*/
    }

    @Override
    public synchronized boolean add( T element ) {
        // sanity check.
        if( element == null )
            throw new IllegalArgumentException();
        cursor.moveToTail();
        cursor.insertElement( element );
        return true;

        //2)
        // sanity check.
        /*if( element == null )
            throw new IllegalArgumentException();
        final OTLinearCursor<T> cursor = (OTLinearCursor<T>)createCursor( 0 );
        cursor.moveToTail();
        final int size1 = ((List<T>)model.getData()).size();
        cursor.insertElement( element );
        cursor.done();
        return ( size1 < ((List<T>)model.getData()).size() );*/

        //1)
        /*final List<T> m = (List<T>)model.getData();
        final int index = m.size() - 1;
        // sanity check.
        if( index > m.size() || index < 0 )
            throw new IndexOutOfBoundsException();
        final OTOperationDefinition.OTOperation<T> insertOp
                = new OTLinearOperations.InsertSEOperation<T>( index, element );
        processLocalOperation( insertOp );
        final int index2 = m.size() - 1;
        return index != index2;*/
    }

    @Override
    public synchronized boolean remove( Object element ) {
        // sanity check.
        if( element == null )
            throw new IllegalArgumentException();
        final StableIterator<T> iterator = (StableIterator<T>) this.iterator();
        while( iterator.hasNext() ) {
            final T obj = iterator.next();
            if( obj == element || obj.equals( element ) ) {
                iterator.done();
                return true;
            }
        }
        iterator.done();
        return false;
    }

    @Override
    public synchronized boolean addAll( Collection<? extends T> elements ) {  // need a mutex?
        return addAll( 0, elements );
    }

    @Override
    public synchronized boolean addAll( int index, Collection<? extends T> elements ) {
        // sanity check.
        if( elements == null )
            throw new IllegalArgumentException();
            final StableListIterator<T> iterator = (StableListIterator<T>) this.listIterator( index );
        for( T element : elements ) {
            iterator.add( element );
        }
        iterator.done();
        return true;
    }

    @Override
    public synchronized boolean retainAll( Collection<?> elements ) {
        // sanity check.
        if( elements == null )
            throw new IllegalArgumentException();
        final StableListIterator<T> iterator = (StableListIterator<T>) this.listIterator();
        boolean result = false;
        while( iterator.hasNext() ) {
            final T obj = iterator.next();
            for( Object o : elements ) {
                if( !( obj == o || obj.equals( o ) ) ) {
                    iterator.remove();
                    result = true;
                }
            }
        }
        iterator.done();
        return result;
    }

    @Override
    public synchronized boolean removeAll( Collection<?> elements ) {
        // sanity check.
        if( elements == null )
            throw new IllegalArgumentException();
        final StableListIterator<T> iterator = (StableListIterator<T>) this.listIterator();
        boolean result = false;
        while( iterator.hasNext() ) {
            final T obj = iterator.next();
            for( Object o : elements ) {
                if( obj == o || obj.equals( o ) ) {
                    iterator.remove();
                    result = true;
                }
            }
        }
        iterator.done();
        return result;
    }

    @Override
    public synchronized void clear() {
        final StableIterator<T> iterator = (StableIterator<T>) this.iterator();
        while( iterator.hasNext() ) {
            iterator.remove();
        }
        iterator.done();
    }

    //-----------------------------------------------
    // Query methods.
    //-----------------------------------------------

    @Override
    public int size() {
        return ((List<?>)model.getData()).size();
    }

    @Override
    public boolean isEmpty() {
        return ((List<?>)model.getData()).isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return ((List<?>)model.getData()).contains( o );
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return ((List<?>)model).containsAll( c );
    }

    @Override
    public Object[] toArray() {
        return ((List<?>)model.getData()).toArray();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <G> G[] toArray( G[] a) {
        return ((List<T>)model.getData()).toArray(a);
    }

    @Override
    @SuppressWarnings("unchecked")
    public T get(int index) {
        return ((List<T>)model.getData()).get(index);
    }

    @Override
    public int indexOf(Object o) {
        return ((List<?>)model.getData()).indexOf( o );
    }

    @Override
    public int lastIndexOf(Object o) {
        return ((List<?>)model.getData()).lastIndexOf(o);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<T> subList(int fromIndex, int toIndex) {
        return ((List<T>)model.getData()).subList( fromIndex, toIndex );
    }

    //-----------------------------------------------
    // Iterator methods.
    //-----------------------------------------------

    @Override
    public Iterator<T> iterator() {
        // block the calling thread until the fill sequence is finished.
        waitUntilFilled();
        return new StableIterator<T>( this, -1 );
    }

    @Override
    public ListIterator<T> listIterator() {
        // block the calling thread until the fill sequence is finished.
        waitUntilFilled();
        return new StableListIterator<T>( this, -1 );
    }

    @Override
    public ListIterator<T> listIterator( int index ) {
        // block the calling thread until the fill sequence is finished.
        waitUntilFilled();
        return new StableListIterator<T>( this, index );
    }

    //-----------------------------------------------
    // toString.
    //-----------------------------------------------

    @Override
    public String toString() {
        return model.toString();
    }

    //-----------------------------------------------
    // Stable iterator implementation.
    //-----------------------------------------------

    /**
     * A stable iterator implementation that throws no <code>ConcurrentModificationException</code>.
     * The iterator can be used only once. If the iterator process spans over the whole list, the
     * iterator deregisters itself from the replicated list. If the iteration process is aborted before
     * reaching the end, a explicit <done>done</done> method call must be performed to avoid memory leaks.
     * @param <T> The type of the elements the iterator iterates over.
     */
    @SuppressWarnings("hiding")
	public class StableIterator<T> implements Iterator<T> {

        /**
         * The OTCursor works as abstract pointer to a element in the data model.
         * Its position is adapted according to local or remote inserts. The cursor provides
         * methods for inserting and deleting elements.
         */
        private final OTLinearCursor<T> cursor;

        /**
         * Constructor.
         * @param art The replicated on that the cursor works.
         * @param position The initial position of the cursor.
         */
        public StableIterator( AbstractReplicatedType<T> art,
                               int position ) {
            // sanity check.
            if( art == null )
                throw new IllegalArgumentException();

            cursor = (OTLinearCursor<T>) art.createCursor( position );
        }

        @Override
        public boolean hasNext() {
            // If the iterator has passed all elements, it automatically deregisters
            // from the replicated list.
            //if( !cursor.hasNext() ) {
                //cursor.done();
            //}
            return cursor.hasNext();
        }

        @Override
        public T next() {
            return cursor.moveForward();
        }

        @Override
        public void remove() {
            cursor.deleteElement();
        }

        public void done() {
            cursor.done();
        }
    }

    //-----------------------------------------------
    // Stable list iterator implementation.
    //-----------------------------------------------

    /**
     * A stable list iterator implementation that throws no <code>ConcurrentModificationException</code>.
     * The iterator can be used only once. To prevent a memory leak a explicit <code>done</code> method
     * must be performed.
     * (TODO: try to avoid explicit <code>done</code> method call after finished operation)
     * @param <T> The type of the elements in the data model.
     */
    @SuppressWarnings("hiding")
	public class StableListIterator<T> implements ListIterator<T> {

        /**
         * The OTCursor works as abstract pointer to a element in the data model.
         * Its position is adapted according to local or remote inserts. The cursor provides
         * methods for inserting and deleting elements.
         */
        private final OTLinearCursor<T> cursor;

        /**
         * Constructor.
         * @param art The replicated on that the cursor works.
         * @param position The initial position of the cursor.
         */
        public StableListIterator( AbstractReplicatedType<T> art,
                                   int position ) {
            // sanity check.
            if( art == null )
                throw new IllegalArgumentException();

            //cursor = new OTLinearCursor<T>( art, position );
            cursor = (OTLinearCursor<T>) art.createCursor( position );
        }

        @Override
        public boolean hasNext() {
            // If the iterator has passed all elements, it automatically deregisters
            // from the replicated list.
            if( !cursor.hasNext() ) {
                cursor.done();
            }
            return cursor.hasNext();
        }

        @Override
        public T next() {
            return cursor.moveForward();
        }

        @Override
        public boolean hasPrevious() {
            return cursor.hasPrevious();
        }

        @Override
        public T previous() {
            return cursor.moveBackward();
        }

        @Override
        public int nextIndex() {
            return cursor.getPosition() + 1;
        }

        @Override
        public int previousIndex() {
            return cursor.getPosition() - 1;
        }

        @Override
        public void remove() {
            cursor.deleteElement();
        }

        @Override
        public void set( T element ) {
            cursor.replaceElement( element );
        }

        @Override
        public void add( T element ) {
            cursor.insertElement( element );
        }

        public void done() {
            cursor.done();
        }
    }
}
