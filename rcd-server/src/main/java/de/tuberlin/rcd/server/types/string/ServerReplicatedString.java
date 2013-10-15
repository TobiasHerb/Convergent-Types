package de.tuberlin.rcd.server.types.string;

import java.util.List;
import java.util.UUID;

import de.tuberlin.rcd.network.common.IEventDispatcher;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.OTSystemDefinition;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.algorithms.ServerOTConcurrencyControlFactory;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.algorithms.filldata.StateBasedFillData;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.algorithms.wave.WaveServerAlgorithm;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.datamodels.linearmodel.OTLinearDataModel;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.datamodels.linearmodel.OTLinearTransformer;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.datamodels.linearmodel.conflictmng.LastWriterWinsConflictSolver;
import de.tuberlin.rcd.protocol.types.AbstractFillData;
import de.tuberlin.rcd.protocol.types.string.IReplicatedString;
import de.tuberlin.rcd.server.runtime.ServerConnectionManager;
import de.tuberlin.rcd.server.types.AbstractServerReplicatedType;

/**
 * A tests implementation of the tests replicated type.
 */
public final class ServerReplicatedString extends AbstractServerReplicatedType<Character>
        implements IReplicatedString {

    /**
     * Log4J.
     */
    //private static final Logger LOGGER = Logger.getLogger( ServerReplicatedString.class );

    /**
     * Constructor.
     *
     * @param name    The name of the type.
     * @param typeUID The UID of the type.
     */
    public ServerReplicatedString( String name, UUID typeUID, ServerConnectionManager connectionManager) {
        super( new ServerOTConcurrencyControlFactory<Character>() {

            @Override
            public OTSystemDefinition.OTServerAlgorithm<Character>
                injectControlAlgorithm( OTSystemDefinition.OTDataModel<Character> dataModel,
                                       OTSystemDefinition.InclusionTransformer<Character> transformer,
                                       OTSystemDefinition.OperationSender<Character> sender,
                                       IEventDispatcher base ) {

                return new WaveServerAlgorithm<Character>( dataModel, transformer, sender, base );
            }

            @Override
            public OTSystemDefinition.InclusionTransformer<Character> injectInclusionTransformer( OTSystemDefinition.OTDataModel<Character> dataModel ) {
                return new OTLinearTransformer<Character>(
                        new LastWriterWinsConflictSolver<Character>( true, dataModel )
                );
            }

            @Override
            public OTSystemDefinition.OTDataModel<Character> injectDataModel() {
                return new OTLinearDataModel<Character>( Character.class );
            }

        }, name, typeUID, connectionManager );
    }

    /**
     * Return the current state of the replicated data for client filling.
     *
     * @return Current state of the replicated data.
     */
    @Override
    public AbstractFillData fillData() {
        return new StateBasedFillData<Character>( model, serverIntegrator.getState() );
    }

    //-----------------------------------------------
    // IReplicatedString implementation.
    //-----------------------------------------------

    /**
     * Insert a character at a given position.
     *
     * @param pos       The position to insert.
     * @param character The character to insert.
     */
    @Override
    public void insertChar( int pos, char character ) {
        throw new UnsupportedOperationException();
    }

    /**
     * Delete a character at a given position.
     *
     * @param pos The position to delete.
     */
    @Override
    public void deleteChar( int pos ) {
        throw new UnsupportedOperationException();
    }

    //-----------------------------------------------
    // CharSequence implementation.
    //-----------------------------------------------

    @Override
    public int length() {
        throw new UnsupportedOperationException();
    }

    @Override
    public char charAt( int i ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public CharSequence subSequence( int i, int i1 ) {
        throw new UnsupportedOperationException();
    }

    @Override
    @SuppressWarnings("unchecked")
    public String toString() {
        //synchronized( modelMutex ) {
            final List<Character> m = (List<Character>)model.getData();
            final StringBuilder builder = new StringBuilder( m.size() );
            for( Character ch : m ) {
                builder.append( ch );
            }
            return builder.toString();
        //}
    }
}
