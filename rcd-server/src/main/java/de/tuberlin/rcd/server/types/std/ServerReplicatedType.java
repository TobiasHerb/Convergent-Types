package de.tuberlin.rcd.server.types.std;

import java.util.UUID;

import de.tuberlin.rcd.network.common.IEventDispatcher;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.OTSystemDefinition;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.algorithms.ServerOTConcurrencyControlFactory;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.algorithms.filldata.OperationBasedFillData;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.algorithms.wave.WaveServerAlgorithm;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.datamodels.linearmodel.OTLinearDataModel;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.datamodels.linearmodel.OTLinearTransformer;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.datamodels.linearmodel.conflictmng.LastWriterWinsConflictSolver;
import de.tuberlin.rcd.protocol.types.AbstractFillData;
import de.tuberlin.rcd.server.runtime.ServerConnectionManager;
import de.tuberlin.rcd.server.types.AbstractServerReplicatedType;

public class ServerReplicatedType extends AbstractServerReplicatedType<Object> {

    /**
     * Log4J.
     */
    //private static final Logger LOGGER = Logger.getLogger( ServerReplicatedType.class );

    /**
     * Constructor.
     *
     * @param name    The name of the type.
     * @param typeUID The UID of the type.
     */
    public ServerReplicatedType(String name, UUID typeUID, ServerConnectionManager connectionManager) {
        super( new ServerOTConcurrencyControlFactory<Object>() {

            @Override
            public OTSystemDefinition.OTServerAlgorithm<Object>
                injectControlAlgorithm( OTSystemDefinition.OTDataModel<Object> dataModel,
                                        OTSystemDefinition.InclusionTransformer<Object> transformer,
                                        OTSystemDefinition.OperationSender<Object> sender,
                                        IEventDispatcher dispatcher ) {

                return new WaveServerAlgorithm<Object>( dataModel, transformer, sender, dispatcher );
            }

            @Override
            public OTSystemDefinition.InclusionTransformer<Object> injectInclusionTransformer( OTSystemDefinition.OTDataModel<Object> dataModel ) {
                return new OTLinearTransformer<Object>(
                        new LastWriterWinsConflictSolver<Object>( true, dataModel )
                );
            }

            @Override
            public OTSystemDefinition.OTDataModel<Object> injectDataModel() {
                return new OTLinearDataModel<Object>( Object.class );
            }

        }, name, typeUID, connectionManager );
    }

    /**
     *
     * @return
     */
    @Override
    protected AbstractFillData fillData() {
        //return new StateBasedFillData<Object>( model,
        //       ((WaveServerAlgorithm)serverIntegrator).getRevision() );
        return new OperationBasedFillData<Object>( serverIntegrator.getHistory(),
                                                       serverIntegrator.getState() );
    }
}
