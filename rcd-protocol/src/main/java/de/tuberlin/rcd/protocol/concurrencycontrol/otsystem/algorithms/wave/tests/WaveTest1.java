package de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.algorithms.wave.tests;

import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.OTOperationDefinition;


/**
 *
 */
public class WaveTest1 {

    public static OTOperationDefinition.OTOperation<Character> serverSent = null;
    public static OTOperationDefinition.OTOperation<Character> sentOpA = null;
    public static OTOperationDefinition.OTOperation<Character> sentOpB = null;

    /**
     * Test application entry point.
     * @param args Not used.
     */
    public static void main( String[] args ) {
        /*
        final OTSystemDefinition.InclusionTransformer<Character> transformer
                = new OTLinearTransformer<Character>( new FirstWriterWinsConflictSolver<Character>() );
        final OTLinearDataModel<Character> clientModelA = new OTLinearDataModel<Character>( Character.class );

        final UUID clientAUID = UUID.randomUUID();
        final WaveClientAlgorithm<Character> clientA =
                new WaveClientAlgorithm<Character>( clientAUID, clientModelA, transformer, new OTSystemDefinition.OperationSender<Character>() {
                    @Override
                    public void sendOperation( UUID clientUID, OTOperationDefinition.OTOperation<Character> op ) {
                        sentOpA = op;
                    }
                });

        final OTLinearDataModel<Character> clientModelB = new OTLinearDataModel<Character>( Character.class );

        final UUID clientBUID = UUID.randomUUID();
        final WaveClientAlgorithm<Character> clientB =
                new WaveClientAlgorithm<Character>( clientBUID, clientModelB, transformer, new OTSystemDefinition.OperationSender<Character>() {
                    @Override
                    public void sendOperation( UUID clientUID, OTOperationDefinition.OTOperation<Character> op ) {
                        sentOpB = op;
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

        clientA.generate( new InsertSEOperation<Character>( 0, 'A' ) );
        clientA.generate( new InsertSEOperation<Character>( 1, 'B' ) );
        clientA.generate( new InsertSEOperation<Character>( 2, 'C' ) );

        clientB.generate( new InsertSEOperation<Character>( 0, '1' ) );
        clientB.generate( new InsertSEOperation<Character>( 1, '2' ) );

        System.out.println( "1)" );
        System.out.println( "ClientA: " + clientModelA.toString() );
        System.out.println( "ClientB: " + clientModelB.toString() );
        System.out.println();

        // 2)
        server.integrate( sentOpB );
        clientA.integrate( serverSent );
        clientB.integrate( serverSent );

        System.out.println( "2)" );
        System.out.println( "ClientA: " + clientModelA.toString() );
        System.out.println( "ClientB: " + clientModelB.toString() );
        System.out.println();

        // 3)
        server.integrate( sentOpB );
        clientA.integrate( serverSent );
        clientB.integrate( serverSent );

        System.out.println( "3)" );
        System.out.println( "ClientA: " + clientModelA.toString() );
        System.out.println( "ClientB: " + clientModelB.toString() );
        System.out.println();

        // 4)
        server.integrate( sentOpA );
        clientA.integrate( serverSent );
        clientB.integrate( serverSent );

        System.out.println( "4)" );
        System.out.println( "ClientA: " + clientModelA.toString() );
        System.out.println( "ClientB: " + clientModelB.toString() );
        System.out.println();

        // 5)
        clientB.generate( new InsertSEOperation( 3, 'X' ) );
        //clientB.generate( new OTOperationDefinition.DeleteSEOperation<Character>( 0 ) );

        System.out.println( "5)" );
        System.out.println( "ClientA: " + clientModelA.toString() );
        System.out.println( "ClientB: " + clientModelB.toString() );
        System.out.println();

        // 6)
        clientA.generate( new InsertSEOperation( 5, 'Y' ) );
        //clientA.generate( new OTOperationDefinition.DeleteSEOperation<Character>( 0 ) );

        System.out.println( "6)" );
        System.out.println( "ClientA: " + clientModelA.toString() );
        System.out.println( "ClientB: " + clientModelB.toString() );
        System.out.println();

        // 7)
        server.integrate( sentOpA );
        clientA.integrate( serverSent );
        clientB.integrate( serverSent );

        System.out.println( "7)" );
        System.out.println( "ClientA: " + clientModelA.toString() );
        System.out.println( "ClientB: " + clientModelB.toString() );
        System.out.println();

        // 8)
        server.integrate( sentOpB );
        clientA.integrate( serverSent );
        clientB.integrate( serverSent );

        System.out.println( "8)" );
        System.out.println( "ClientA: " + clientModelA.toString() );
        System.out.println( "ClientB: " + clientModelB.toString() );
        System.out.println();

        // 9)
        server.integrate( sentOpA );
        clientA.integrate( serverSent );
        clientB.integrate( serverSent );

        System.out.println( "9)" );
        System.out.println( "ClientA: " + clientModelA.toString() );
        System.out.println( "ClientB: " + clientModelB.toString() );
        System.out.println();

        // 10)
        server.integrate( sentOpA );
        clientA.integrate( serverSent );
        clientB.integrate( serverSent );

        System.out.println( "10)" );
        System.out.println( "ClientA: " + clientModelA.toString() );
        System.out.println( "ClientB: " + clientModelB.toString() );
        System.out.println();   */
    }
}
