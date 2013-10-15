package de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.algorithms.wave.tests;

import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.OTOperationDefinition;


/**
 *
 */
public class WaveTest2 {

    public static OTOperationDefinition.OTOperation<Character> serverSent = null;
    public static OTOperationDefinition.OTOperation<Character> sentOpA = null;
    public static OTOperationDefinition.OTOperation<Character> sentOpB = null;
    public static OTOperationDefinition.OTOperation<Character> sentOpC = null;

    /**
     * Test application entry point.
     * @param args Not used.
     */
    public static void main( String[] args ) {
        /*
        final OTSystemDefinition.InclusionTransformer<Character> transformer
                = new OTLinearTransformer<Character>( new LastWriterWinsConflictSolver<Character>( true ) );

        final UUID clientAUID = UUID.randomUUID();
        final OTLinearDataModel<Character> clientModelA = new OTLinearDataModel<Character>( Character.class );
        final WaveClientAlgorithm<Character> clientA =
                new WaveClientAlgorithm<Character>( clientAUID, clientModelA, transformer, new OTSystemDefinition.OperationSender<Character>() {
                    @Override
                    public void sendOperation( UUID clientUID, OTOperationDefinition.OTOperation<Character> op ) {
                        sentOpA = op;
                    }
                });

        final UUID clientBUID = UUID.randomUUID();
        final OTLinearDataModel<Character> clientModelB = new OTLinearDataModel<Character>( Character.class );
        final WaveClientAlgorithm<Character> clientB =
                new WaveClientAlgorithm<Character>( clientBUID, clientModelB, transformer, new OTSystemDefinition.OperationSender<Character>() {
                    @Override
                    public void sendOperation( UUID clientUID, OTOperationDefinition.OTOperation<Character> op ) {
                        sentOpB = op;
                    }
                });

        final UUID clientCUID = UUID.randomUUID();
        final OTLinearDataModel<Character> clientModelC = new OTLinearDataModel<Character>( Character.class );
        final WaveClientAlgorithm<Character> clientC =
                new WaveClientAlgorithm<Character>( clientCUID, clientModelC, transformer, new OTSystemDefinition.OperationSender<Character>() {
                    @Override
                    public void sendOperation( UUID clientUID, OTOperationDefinition.OTOperation<Character> op ) {
                        sentOpC = op;
                    }
                });

        final OTLinearDataModel<Character> serverModel = new OTLinearDataModel<Character>( Character.class );
        final WaveServerAlgorithm<Character> server =
                new WaveServerAlgorithm<Character>( serverModel, transformer, new OTSystemDefinition.OperationSender<Character>() {
                    @Override
                    public void sendOperation(UUID clientUID, OTOperationDefinition.OTOperation<Character> op) {
                        serverSent = op;
                    }
                }, null );

        server.addClient( clientAUID );
        server.addClient( clientBUID );
        server.addClient( clientCUID );

        clientA.generate( new OTLinearOperations.InsertSEOperation<Character>( 0, 'A' ) );
        clientA.generate( new OTLinearOperations.InsertSEOperation<Character>( 1, 'B' ) );
        clientA.generate( new OTLinearOperations.InsertSEOperation<Character>( 2, 'C' ) );

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


        clientA.generate( new OTLinearOperations.UpdateSEOperation<Character>( 1, 'X' ) );
        clientB.generate( new OTLinearOperations.UpdateSEOperation<Character>( 1, 'Y' ) );
        clientC.generate( new OTLinearOperations.UpdateSEOperation<Character>( 1, 'Z' ) );

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
        System.out.println();*/
    }
}
