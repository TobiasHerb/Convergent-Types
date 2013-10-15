package de.tuberlin.rcd.server.types.list;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.UUID;

import org.apache.log4j.Logger;

import de.tuberlin.rcd.network.common.IEventDispatcher;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.OTSystemDefinition;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.algorithms.ServerOTConcurrencyControlFactory;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.algorithms.filldata.OperationBasedFillData;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.algorithms.wave.WaveServerAlgorithm;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.datamodels.linearmodel.OTLinearDataModel;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.datamodels.linearmodel.OTLinearTransformer;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.datamodels.linearmodel.conflictmng.LastWriterWinsConflictSolver;
import de.tuberlin.rcd.protocol.types.AbstractFillData;
import de.tuberlin.rcd.protocol.types.list.IReplicatedList;
import de.tuberlin.rcd.server.runtime.ServerConnectionManager;
import de.tuberlin.rcd.server.types.AbstractServerReplicatedType;

public class ServerReplicatedList<T> extends AbstractServerReplicatedType<T>
        implements IReplicatedList<T> {

    /**
     * Log4J.
     */
    private static final Logger LOGGER = Logger.getLogger( ServerReplicatedList.class );

    /**
     * Constructor.
     *
     * @param name The name of the type.
     * @param typeUID The UID of the type.
     */
    public ServerReplicatedList( String name, UUID typeUID, final Class<T> typeInfo, ServerConnectionManager connectionManager ) {
        super( new ServerOTConcurrencyControlFactory<T>() {

            @Override
            public OTSystemDefinition.OTServerAlgorithm<T>
                injectControlAlgorithm( OTSystemDefinition.OTDataModel<T> dataModel,
                                        OTSystemDefinition.InclusionTransformer<T> transformer,
                                        OTSystemDefinition.OperationSender<T> sender,
                                        IEventDispatcher dispatcher )  {

                return new WaveServerAlgorithm<T>( dataModel, transformer, sender, dispatcher );
            }

            @Override
            public OTSystemDefinition.InclusionTransformer<T>
                injectInclusionTransformer( OTSystemDefinition.OTDataModel<T> dataModel ) {
                return new OTLinearTransformer<T>(
                        new LastWriterWinsConflictSolver<T>( true, dataModel )
                );
            }

            @Override
            public OTSystemDefinition.OTDataModel<T> injectDataModel() {
                return new OTLinearDataModel<T>( typeInfo );
            }

        }, name, typeUID, connectionManager );
    }

    //-----------------------------------------------
    // IReplicatedType implementation.
    //-----------------------------------------------

    /**
     * Return the current state of the replicated data for client filling.
     *
     * @return Current state of the replicated data.
     */
    @Override
    public AbstractFillData fillData() {
        //return new StateBasedFillData<T>( model,
        //        ((WaveServerAlgorithm)serverIntegrator).getRevision() );
        return new OperationBasedFillData<T>( serverIntegrator.getHistory(),
                                                  serverIntegrator.getState() );
    }

    //-----------------------------------------------
    // List implementation.
    //-----------------------------------------------

    //-----------------------------------------------
    // Modification methods.
    //-----------------------------------------------

    @Override
    public boolean add(T t) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public T set(int index, T element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(int index, T element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public T remove(int index) {
        throw new UnsupportedOperationException();
    }

    //-----------------------------------------------
    // Query methods.
    //-----------------------------------------------

    @Override
    public int size() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isEmpty() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean contains(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object[] toArray() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <G> G[] toArray( G[] a) {
        throw new UnsupportedOperationException();
    }

    @Override
    public T get(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int indexOf(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int lastIndexOf(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException();
    }

    //-----------------------------------------------
    // Iterator methods.
    //-----------------------------------------------

    @Override
    public Iterator<T> iterator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ListIterator<T> listIterator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        throw new UnsupportedOperationException();
    }
}
