package de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.algorithms.wave.tests;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.OTOperationDefinition;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.OTSystemDefinition;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.algorithms.wave.WaveClientAlgorithm;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.algorithms.wave.WaveServerAlgorithm;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.datamodels.linearmodel.OTLinearDataModel;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.datamodels.linearmodel.OTLinearOperations;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.datamodels.linearmodel.OTLinearTransformer;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.datamodels.linearmodel.conflictmng.LastWriterWinsConflictSolver;

/**
 *
 */
public class WaveTest3 {


    public static class Entity {

        public Entity( String stringData, Integer integerData, Boolean booleanData ) {
            this.stringData = stringData;
            this.integerData = integerData;
            this.booleanData = booleanData;
        }

        public Entity( Entity copy ) {
            this.stringData  = new String( copy.stringData );
            this.integerData = new Integer( copy.integerData );
            this.booleanData = new Boolean( copy.booleanData );
        }

        public String stringData;
        public Integer integerData;
        public Boolean booleanData;

        @Override
        public String toString() {
            return "(" + stringData + "," + integerData + "," + booleanData + ")";
        }
    }

    public static OTOperationDefinition.OTOperation<Entity> serverSent = null;
    public static OTOperationDefinition.OTOperation<Entity> sentOpA = null;
    public static OTOperationDefinition.OTOperation<Entity> sentOpB = null;
    public static OTOperationDefinition.OTOperation<Entity> sentOpC = null;

    /**
     * Test application entry point.
     * @param args Not used.
     */
    public static void main( String[] args ) {

        final UUID clientAUID = UUID.randomUUID();
        final OTLinearDataModel<Entity> clientModelA = new OTLinearDataModel<Entity>( Entity.class );
        final OTSystemDefinition.InclusionTransformer<Entity> transformerA
                = new OTLinearTransformer<Entity>( new LastWriterWinsConflictSolver<Entity>( true, clientModelA ) );
        final WaveClientAlgorithm<Entity> clientA =
                new WaveClientAlgorithm<Entity>( clientAUID, clientModelA, transformerA, new OTSystemDefinition.OperationSender<Entity>() {
                    @Override
                    public void sendOperation( UUID clientUID, OTOperationDefinition.OTOperation<Entity> op ) {
                        sentOpA = OTLinearOperations.OperationHelper.shallowCopyOperation( op );
                    }
                });

        final UUID clientBUID = UUID.randomUUID();
        final OTLinearDataModel<Entity> clientModelB = new OTLinearDataModel<Entity>( Entity.class );
        final OTSystemDefinition.InclusionTransformer<Entity> transformerB
                = new OTLinearTransformer<Entity>( new LastWriterWinsConflictSolver<Entity>( true, clientModelB ) );
        final WaveClientAlgorithm<Entity> clientB =
                new WaveClientAlgorithm<Entity>( clientBUID, clientModelB, transformerB, new OTSystemDefinition.OperationSender<Entity>() {
                    @Override
                    public void sendOperation( UUID clientUID, OTOperationDefinition.OTOperation<Entity> op ) {
                        sentOpB = OTLinearOperations.OperationHelper.shallowCopyOperation( op );
                    }
                });

        final UUID clientCUID = UUID.randomUUID();
        final OTLinearDataModel<Entity> clientModelC = new OTLinearDataModel<Entity>( Entity.class );
        final OTSystemDefinition.InclusionTransformer<Entity> transformerC
                = new OTLinearTransformer<Entity>( new LastWriterWinsConflictSolver<Entity>( true, clientModelC ) );
        final WaveClientAlgorithm<Entity> clientC =
                new WaveClientAlgorithm<Entity>( clientCUID, clientModelC, transformerC, new OTSystemDefinition.OperationSender<Entity>() {
                    @Override
                    public void sendOperation( UUID clientUID, OTOperationDefinition.OTOperation<Entity> op ) {
                        sentOpC = OTLinearOperations.OperationHelper.shallowCopyOperation( op );
                    }
                });

        final OTLinearDataModel<Entity> serverModel = new OTLinearDataModel<Entity>( Entity.class );
        final OTSystemDefinition.InclusionTransformer<Entity> transformerS
                = new OTLinearTransformer<Entity>( new LastWriterWinsConflictSolver<Entity>( true, serverModel ) );
        final WaveServerAlgorithm<Entity> server =
                new WaveServerAlgorithm<Entity>( serverModel, transformerS, new OTSystemDefinition.OperationSender<Entity>() {
                    @Override
                    public void sendOperation(UUID clientUID, OTOperationDefinition.OTOperation<Entity> op) {
                        serverSent = OTLinearOperations.OperationHelper.shallowCopyOperation( op );
                    }
                }, null );

        server.addClient( clientAUID );
        server.addClient( clientBUID );
        server.addClient( clientCUID );

        clientA.generate( new OTLinearOperations.InsertSEOperation<Entity>( 0, new Entity( "A", 1, false ) ) );
        clientA.generate( new OTLinearOperations.InsertSEOperation<Entity>( 1, new Entity( "B", 2, false ) ) );
        clientA.generate( new OTLinearOperations.InsertSEOperation<Entity>( 2, new Entity( "C", 3, false ) ) );

        System.out.println( "1)" );
        System.out.println( "ClientA: " + clientModelA.toString() );
        System.out.println( "ClientB: " + clientModelB.toString() );
        System.out.println( "ClientC: " + clientModelC.toString() );
        System.out.println();

        // 2)
        server.integrate( sentOpA );
        clientA.integrate( serverSent );
        clientB.integrate( serverSent );
        clientC.integrate( serverSent );

        System.out.println( "2)" );
        System.out.println( "ClientA: " + clientModelA.toString() );
        System.out.println( "ClientB: " + clientModelB.toString() );
        System.out.println( "ClientC: " + clientModelC.toString() );
        System.out.println();

        // 3)
        server.integrate( sentOpA );
        clientA.integrate( serverSent );
        clientB.integrate( serverSent );
        clientC.integrate( serverSent );

        System.out.println( "3)" );
        System.out.println( "ClientA: " + clientModelA.toString() );
        System.out.println( "ClientB: " + clientModelB.toString() );
        System.out.println( "ClientC: " + clientModelC.toString() );
        System.out.println();

        // 4)
        server.integrate( sentOpA );
        clientA.integrate( serverSent );
        clientB.integrate( serverSent );
        clientC.integrate( serverSent );

        System.out.println( "4)" );
        System.out.println( "ClientA: " + clientModelA.toString() );
        System.out.println( "ClientB: " + clientModelB.toString() );
        System.out.println( "ClientC: " + clientModelC.toString() );
        System.out.println();

        final Map<String,Object> deltaMap1 = new HashMap<String,Object>();
        deltaMap1.put( "stringData", "Q" );
        deltaMap1.put( "integerData", 9 );

        clientA.generate( new OTLinearOperations.UpdateEntityOperation<Entity>( 1, Entity.class, deltaMap1 ) );

        final Map<String,Object> deltaMap2 = new HashMap<String, Object>();
        deltaMap2.put( "booleanData", true );

        clientB.generate( new OTLinearOperations.UpdateEntityOperation<Entity>( 1, Entity.class, deltaMap2 ) );

        final Map<String,Object> deltaMap3 = new HashMap<String,Object>();
        //deltaMap3.put( "integerData", 9 );
        deltaMap3.put( "stringData", "W" );
        deltaMap3.put( "booleanData", false );

        clientC.generate( new OTLinearOperations.UpdateEntityOperation<Entity>( 1, Entity.class, deltaMap3 ) );

        // 5)
        server.integrate( sentOpA );
        clientA.integrate( serverSent );
        clientB.integrate( serverSent );
        clientC.integrate( serverSent );

        System.out.println( "5)" );
        System.out.println( "ClientA: " + clientModelA.toString() );
        System.out.println( "ClientB: " + clientModelB.toString() );
        System.out.println( "ClientC: " + clientModelC.toString() );
        System.out.println();

        // 6)
        server.integrate( sentOpB );
        clientA.integrate( serverSent );
        clientB.integrate( serverSent );
        clientC.integrate( serverSent );

        System.out.println( "6)" );
        System.out.println( "ClientA: " + clientModelA.toString() );
        System.out.println( "ClientB: " + clientModelB.toString() );
        System.out.println( "ClientC: " + clientModelC.toString() );
        System.out.println();

        // 7)
        server.integrate( sentOpC );
        clientA.integrate( serverSent );
        clientB.integrate( serverSent );
        clientC.integrate( serverSent );

        System.out.println( "7)" );
        System.out.println( "ClientA: " + clientModelA.toString() );
        System.out.println( "ClientB: " + clientModelB.toString() );
        System.out.println( "ClientC: " + clientModelC.toString() );
        System.out.println();
    }
}
