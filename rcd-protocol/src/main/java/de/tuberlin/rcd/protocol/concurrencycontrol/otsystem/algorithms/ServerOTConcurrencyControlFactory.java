package de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.algorithms;


import de.tuberlin.rcd.network.common.IEventDispatcher;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.OTSystemDefinition;

public interface ServerOTConcurrencyControlFactory<T> {

    public abstract OTSystemDefinition.OTServerAlgorithm<T>
            injectControlAlgorithm( OTSystemDefinition.OTDataModel<T> dataModel,
                                    OTSystemDefinition.InclusionTransformer<T> transformer,
                                    OTSystemDefinition.OperationSender<T> sender,
                                    IEventDispatcher base );

    public abstract OTSystemDefinition.InclusionTransformer<T> injectInclusionTransformer( OTSystemDefinition.OTDataModel<T> dataModel );

    public abstract OTSystemDefinition.OTDataModel<T> injectDataModel();
}
