package de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.datamodels.linearmodel;

import java.util.Map;

import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.OTOperationDefinition;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.OTSystemDefinition;
import de.tuberlin.rcd.protocol.types.AbstractReplicatedType;

public class OTLinearCursor<T> extends OTSystemDefinition.OTCursor<T> {

    /**
     * Constructor.
     * @param art The replicated on that the cursor works.
     * @param position The initial position of the cursor.
     */
    public OTLinearCursor( AbstractReplicatedType<T> art,
                           int position ) {
        // sanity check.
        if( art == null )
            throw new IllegalArgumentException();

        this.type = art;
        //this.operationGenerator = art.getOperationGenerator();
        this.dataModel = (OTLinearDataModel<T>)art.getDataModel();
        this.position = position;
    }

    private final AbstractReplicatedType<T> type;

    //private final OTSystemDefinition.OTOperationGenerator<T> operationGenerator;

    private final OTLinearDataModel<T> dataModel;

    private int position;

    public T moveToTail() {
        position = dataModel.size(); // == 0 ? 0 : dataModel.size() - 1;
        if( dataModel.size() > 0 ) {
            return dataModel.getDataElement( position - 1 );
        } else {
            return null;
        }
    }

    public T moveToHead() {
        position = 0;
        if( dataModel.size() > 0 ) {
            return dataModel.getDataElement( position );
        } else {
            return null;
        }
    }

    public T moveForward() {
        ++position;
        if( position > dataModel.size() - 1 )
            return null;
        else
            return dataModel.getDataElement( position );
    }

    public T moveBackward() {
        --position;
        if( position < 0 )
            return null;
        else
            return dataModel.getDataElement( position );
    }

    public int getPosition() {
        return position;
    }

    @Override
    public T getElement() {
        return dataModel.getDataElement( position );
    }

    @Override
    public void done() {
        type.getAlgorithmBase().deregisterCursor( this );
    }

    public boolean hasNext() {
        return dataModel.size() > 0 &&  position < dataModel.size() - 1;
    }

    public boolean hasPrevious() {
        return dataModel.size() > 0 && position > 0;
    }

    public T replaceElement( T element ) {
        // sanity check.
        if( element == null )
            throw new IllegalArgumentException();
        final OTOperationDefinition.OTOperation<T> replaceOp =
                new OTLinearOperations.UpdateSEOperation<T>( position, element );
        final T result = getElement();
        //operationGenerator.generate( replaceOp );
        type.processLocalOperation( replaceOp );
        //type.dispatchEvent( new IReplicatedType.ReplicatedTypeEvent( IReplicatedType.ReplicatedTypeEvent.RS_LOCAL_UPDATE, replaceOp ) );
        //type.dispatchEvent( new IReplicatedType.ReplicatedTypeEvent( IReplicatedType.ReplicatedTypeEvent.RS_UPDATE, replaceOp ) );
        return result;
    }

    public void updateElement( Map<String,Object> entityDelta ) {
        // sanity check.
        if( entityDelta == null )
            throw new IllegalArgumentException();
        // TODO: implement it.
        throw new UnsupportedOperationException();
    }

    public T deleteElement() {
        final OTOperationDefinition.OTOperation<T> deleteOp =
                new OTLinearOperations.DeleteSEOperation<T>( position );
        final T result = getElement();
        //operationGenerator.generate( deleteOp );
        type.processLocalOperation( deleteOp );
        //type.dispatchEvent( new IReplicatedType.ReplicatedTypeEvent( IReplicatedType.ReplicatedTypeEvent.RS_LOCAL_UPDATE, deleteOp ) );
        //type.dispatchEvent( new IReplicatedType.ReplicatedTypeEvent( IReplicatedType.ReplicatedTypeEvent.RS_UPDATE, deleteOp ) );
        return result;
    }

    public void insertElement( T element ) {
        // sanity check.
        if( element == null )
            throw new IllegalArgumentException();
        final OTOperationDefinition.OTOperation<T> insertOp =
                new OTLinearOperations.InsertSEOperation<T>( position, element );
        //operationGenerator.generate( insertOp );
        type.processLocalOperation( insertOp );
        //type.dispatchEvent( new IReplicatedType.ReplicatedTypeEvent( IReplicatedType.ReplicatedTypeEvent.RS_LOCAL_UPDATE, insertOp ) );
        //type.dispatchEvent( new IReplicatedType.ReplicatedTypeEvent( IReplicatedType.ReplicatedTypeEvent.RS_UPDATE, insertOp ) );
    }

    public void reset( int position ) {
        this.position = position;
    }
}
