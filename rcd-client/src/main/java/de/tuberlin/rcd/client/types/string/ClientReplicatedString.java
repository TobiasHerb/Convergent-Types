package de.tuberlin.rcd.client.types.string;

import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;

import de.tuberlin.rcd.client.types.AbstractClientReplicatedType;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.OTOperationDefinition;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.OTSystemDefinition;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.algorithms.ClientOTConcurrencyControlFactory;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.algorithms.wave.WaveClientAlgorithm;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.datamodels.linearmodel.OTLinearCursor;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.datamodels.linearmodel.OTLinearDataModel;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.datamodels.linearmodel.OTLinearOperations;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.datamodels.linearmodel.OTLinearTransformer;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.datamodels.linearmodel.conflictmng.LastWriterWinsConflictSolver;
import de.tuberlin.rcd.protocol.runtimedefinition.IDataManager;
import de.tuberlin.rcd.protocol.types.AbstractReplicatedType;
import de.tuberlin.rcd.protocol.types.string.IReplicatedString;

/**
 * A tests implementation of the client replicated type.
 */
public final class ClientReplicatedString extends AbstractClientReplicatedType<Character>
        implements IReplicatedString {

    /**
     * Log4J.
     */
    private static final Logger LOGGER = Logger.getLogger( ClientReplicatedString.class );

    /**
     * Constructor.
     */
    public ClientReplicatedString( String name, UUID typeUID, IDataManager dataManager ) {
        super( new ClientOTConcurrencyControlFactory<Character>() {

            @Override
            public OTSystemDefinition.OTClientAlgorithm<Character>
                injectControlAlgorithm( UUID clientUID,
                                        OTSystemDefinition.OTDataModel<Character> dataModel,
                                        OTSystemDefinition.InclusionTransformer<Character> transformer,
                                        OTSystemDefinition.OperationSender<Character> sender ) {
                return new WaveClientAlgorithm<Character>( clientUID, dataModel, transformer, sender );
            }

            @Override
            public OTSystemDefinition.InclusionTransformer<Character> injectInclusionTransformer( final OTSystemDefinition.OTDataModel<Character> dataModel ) {
                return new OTLinearTransformer<Character>(
                        new LastWriterWinsConflictSolver<Character>( true, dataModel )
                );
            }

            @Override
            public OTSystemDefinition.OTDataModel<Character> injectDataModel() {
                return new OTLinearDataModel<Character>( Character.class );
            }

        }, name, typeUID, dataManager );
    }

    /**
     * Create a cursor on a fixed position.
     * @param position Index of the data element the cursor points to.
     * @return A registered cursor.
     */
    @Override
    public OTSystemDefinition.OTCursor<Character> createCursor( final int position ) {
        final AbstractReplicatedType<Character> self = this;
        return clientIntegrator.registerCursor( new OTSystemDefinition.OTCursor.OTCursorFactory<Character>() {
            @Override
            public OTSystemDefinition.OTCursor<Character> createCursor() {
                return new OTLinearCursor<Character>( self, position );
            }
        });
    }

    //-----------------------------------------------
    // IReplicatedString implementation.
    //-----------------------------------------------

    /**
     * Insert a character at a given position.
     * @param pos The position to insert.
     * @param character The character to insert.
     */
    @Override
    @SuppressWarnings("unchecked")
    public void insertChar( int pos, char character ) {
        final List<Character> m = (List<Character>)model.getData();
        // sanity check.
        if( pos > m.size() || pos < 0 )
            throw new IndexOutOfBoundsException();

        final OTOperationDefinition.OTOperation<Character> insertOp
                = new OTLinearOperations.InsertSEOperation<Character>( pos, character );

        processLocalOperation( insertOp );
    }

    /**
     * Delete a character at a given position.
     * @param pos The position to delete.
     */
    @Override
    @SuppressWarnings("unchecked")
    public void deleteChar( int pos ) {
        final List<Character> m = (List<Character>)model.getData();
        // sanity check.
        if( pos > m.size() || pos < 0 )
            throw new IndexOutOfBoundsException();

        final OTOperationDefinition.OTOperation<Character> deleteOp
                = new OTLinearOperations.DeleteSEOperation<Character>( pos );

        processLocalOperation( deleteOp );
    }

    //-----------------------------------------------
    // CharSequence implementation.
    //-----------------------------------------------

    @Override
    @SuppressWarnings("unchecked")
    public int length() {
        return ((List<Character>)model.getData()).size();
    }

    @Override
    @SuppressWarnings("unchecked")
    public char charAt( int i ) {
        return ((List<Character>)model).get( i );
    }

    @Override
    public CharSequence subSequence( int i, int i1 ) {
        throw new UnsupportedOperationException();
    }

    @Override
    @SuppressWarnings("unchecked")
    public String toString() {
        final List<Character> m = (List<Character>)model.getData();
        StringBuilder builder = new StringBuilder( m.size() );
        for( Character ch : m ) {
            builder.append( ch );
        }
        return builder.toString();
    }
}
