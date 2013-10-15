package de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.algorithms;

import java.util.UUID;

import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.OTSystemDefinition;

public interface ClientOTConcurrencyControlFactory<T> {

    public abstract OTSystemDefinition.InclusionTransformer<T> injectInclusionTransformer( OTSystemDefinition.OTDataModel<T> dataModel );

    public abstract OTSystemDefinition.OTClientAlgorithm<T>
            injectControlAlgorithm( UUID clientUID,
                                    OTSystemDefinition.OTDataModel<T> dataModel,
                                    OTSystemDefinition.InclusionTransformer<T> transformer,
                                    OTSystemDefinition.OperationSender<T> sender );

    public abstract OTSystemDefinition.OTDataModel<T> injectDataModel();
}