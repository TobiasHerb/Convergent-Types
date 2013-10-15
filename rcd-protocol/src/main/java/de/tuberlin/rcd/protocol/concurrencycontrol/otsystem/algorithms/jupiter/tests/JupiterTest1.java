package de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.algorithms.jupiter.tests;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.OTOperationDefinition;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.OTSystemDefinition;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.algorithms.jupiter.JupiterClientAlgorithm;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.algorithms.jupiter.JupiterServerAlgorithm;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.datamodels.linearmodel.OTLinearDataModel;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.datamodels.linearmodel.OTLinearOperations;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.datamodels.linearmodel.OTLinearTransformer;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.datamodels.linearmodel.conflictmng.FirstWriterWinsConflictSolver;

public class JupiterTest1 {

    public static final List<OTOperationDefinition.OTOperation<Character>> sentOpAQueue
            = new ArrayList<OTOperationDefinition.OTOperation<Character>>();

    public static final List<OTOperationDefinition.OTOperation<Character>> sentOpBQueue
            = new ArrayList<OTOperationDefinition.OTOperation<Character>>();

    public static final List<OTOperationDefinition.OTOperation<Character>> sentOpCQueue
            = new ArrayList<OTOperationDefinition.OTOperation<Character>>();

    public static final List<OTOperationDefinition.OTOperation<Character>> serverSentOpAQueue
            = new ArrayList<OTOperationDefinition.OTOperation<Character>>();

    public static final List<OTOperationDefinition.OTOperation<Character>> serverSentOpBQueue
            = new ArrayList<OTOperationDefinition.OTOperation<Character>>();

    public static final List<OTOperationDefinition.OTOperation<Character>> serverSentOpCQueue
            = new ArrayList<OTOperationDefinition.OTOperation<Character>>();

    public static void main( String[] args ) {

        final OTSystemDefinition.InclusionTransformer<Character> transformer
                = new OTLinearTransformer<Character>( new FirstWriterWinsConflictSolver<Character>() );

        final UUID clientAID = UUID.fromString( "6dc4a6cf-c90b-4b6b-b134-f69740e24247" );
        System.out.println( "ClientA ID: " + clientAID );
        final OTLinearDataModel<Character> clientModelA = new OTLinearDataModel<Character>( Character.class );
        final JupiterClientAlgorithm<Character> clientA = new JupiterClientAlgorithm<Character>( clientAID, clientModelA, transformer, new OTSystemDefinition.OperationSender<Character>() {
            @Override
            public void sendOperation( UUID clientUID, OTOperationDefinition.OTOperation<Character> op ) {
                sentOpAQueue.add( op );
            }
        } );

        final UUID clientBID = UUID.fromString( "ea5c0e12-c1a6-40e7-930c-93ce5d1ff0ca" );
        System.out.println( "ClientB ID: " + clientBID );
        final OTLinearDataModel<Character> clientModelB = new OTLinearDataModel<Character>( Character.class );
        final JupiterClientAlgorithm<Character> clientB = new JupiterClientAlgorithm<Character>( clientBID, clientModelB, transformer, new OTSystemDefinition.OperationSender<Character>() {
            @Override
            public void sendOperation( UUID clientUID, OTOperationDefinition.OTOperation<Character> op ) {
                sentOpBQueue.add( op );
            }
        } );

        final UUID clientCID = UUID.fromString( "f518b3de-f829-48c2-97ac-5914018994c8" );
        System.out.println( "ClientC ID: " + clientCID );
        final OTLinearDataModel<Character> clientModelC = new OTLinearDataModel<Character>( Character.class );
        final JupiterClientAlgorithm<Character> clientC = new JupiterClientAlgorithm<Character>( clientCID, clientModelC, transformer, new OTSystemDefinition.OperationSender<Character>() {
            @Override
            public void sendOperation( UUID clientUID, OTOperationDefinition.OTOperation<Character> op ) {
                sentOpCQueue.add( op );
            }
        } );

        System.out.println();

        final OTLinearDataModel<Character> serverModel = new OTLinearDataModel<Character>( Character.class );
        final JupiterServerAlgorithm<Character> server = new JupiterServerAlgorithm<Character>( serverModel, transformer, null, null );

        server.addClientProxy( clientAID, new OTSystemDefinition.OperationSender<Character>() {
            @Override
            public void sendOperation( UUID clientUID, OTOperationDefinition.OTOperation<Character> op ) {
                serverSentOpAQueue.add( op );
            }
        });

        server.addClientProxy( clientBID, new OTSystemDefinition.OperationSender<Character>() {
            @Override
            public void sendOperation( UUID clientUID, OTOperationDefinition.OTOperation<Character> op ) {
                serverSentOpBQueue.add( op );
            }
        });

        server.addClientProxy( clientCID, new OTSystemDefinition.OperationSender<Character>() {
            @Override
            public void sendOperation( UUID clientUID, OTOperationDefinition.OTOperation<Character> op ) {
                serverSentOpCQueue.add( op );
            }
        });

        clientA.generate( new OTLinearOperations.InsertSEOperation<Character>( 0, 'A' ) );
        clientA.generate( new OTLinearOperations.InsertSEOperation<Character>( 1, 'B' ) );
        clientA.generate( new OTLinearOperations.InsertSEOperation<Character>( 2, 'C' ) );

        clientB.generate( new OTLinearOperations.InsertSEOperation<Character>( 0, 'X' ) );

        System.out.println( "1)" );
        System.out.println( "ClientA: " + clientModelA.toString() + " | " + clientA.toString() );
        System.out.println( "Client Proxy A State: " + server.getProxyState( clientAID ) );
        System.out.println( "ClientB: " + clientModelB.toString() + " | " + clientB.toString() );
        System.out.println( "Client Proxy B State: " + server.getProxyState( clientBID ) );
        System.out.println( "ClientC: " + clientModelC.toString() + " | " + clientC.toString() );
        System.out.println( "Client Proxy C State: " + server.getProxyState( clientCID ) );
        System.out.println( "Server: " + serverModel.toString() );
        System.out.println();

        server.integrate( sentOpAQueue.remove( 0 ) );
        clientB.integrate( serverSentOpBQueue.remove( 0 ) );
        clientC.integrate( serverSentOpCQueue.remove( 0 ) );

        System.out.println( "2)" );
        System.out.println( "ClientA: " + clientModelA.toString() + " | " + clientA.toString() );
        System.out.println( "Client Proxy A State: " + server.getProxyState( clientAID ) );
        System.out.println( "ClientB: " + clientModelB.toString() + " | " + clientB.toString() );
        System.out.println( "Client Proxy B State: " + server.getProxyState( clientBID ) );
        System.out.println( "ClientC: " + clientModelC.toString() + " | " + clientC.toString() );
        System.out.println( "Client Proxy C State: " + server.getProxyState( clientCID ) );
        System.out.println( "Server: " + serverModel.toString() );
        System.out.println();

        server.integrate( sentOpAQueue.remove( 0 ) );
        clientB.integrate( serverSentOpBQueue.remove( 0 ) );
        clientC.integrate( serverSentOpCQueue.remove( 0 ) );

        System.out.println( "3)" );
        System.out.println( "ClientA: " + clientModelA.toString() + " | " + clientA.toString() );
        System.out.println( "Client Proxy A State: " + server.getProxyState( clientAID ) );
        System.out.println( "ClientB: " + clientModelB.toString() + " | " + clientB.toString() );
        System.out.println( "Client Proxy B State: " + server.getProxyState( clientBID ) );
        System.out.println( "ClientC: " + clientModelC.toString() + " | " + clientC.toString() );
        System.out.println( "Client Proxy C State: " + server.getProxyState( clientCID ) );
        System.out.println( "Server: " + serverModel.toString() );
        System.out.println();

        clientB.generate( new OTLinearOperations.InsertSEOperation<Character>( 1, 'Y' ) );

        System.out.println( "4)" );
        System.out.println( "ClientA: " + clientModelA.toString() + " | " + clientA.toString() );
        System.out.println( "Client Proxy A State: " + server.getProxyState( clientAID ) );
        System.out.println( "ClientB: " + clientModelB.toString() + " | " + clientB.toString() );
        System.out.println( "Client Proxy B State: " + server.getProxyState( clientBID ) );
        System.out.println( "ClientC: " + clientModelC.toString() + " | " + clientC.toString() );
        System.out.println( "Client Proxy C State: " + server.getProxyState( clientCID ) );
        System.out.println( "Server: " + serverModel.toString() );
        System.out.println();

        server.integrate( sentOpBQueue.remove( 0 ) );               // break...
        clientA.integrate( serverSentOpAQueue.remove( 0 ) );        // break...
        clientC.integrate( serverSentOpCQueue.remove( 0 ) );

        System.out.println( "5)" );
        System.out.println( "ClientA: " + clientModelA.toString() + " | " + clientA.toString() );
        System.out.println( "Client Proxy A State: " + server.getProxyState( clientAID ) );
        System.out.println( "ClientB: " + clientModelB.toString() + " | " + clientB.toString() );
        System.out.println( "Client Proxy B State: " + server.getProxyState( clientBID ) );
        System.out.println( "ClientC: " + clientModelC.toString() + " | " + clientC.toString() );
        System.out.println( "Client Proxy C State: " + server.getProxyState( clientCID ) );
        System.out.println( "Server: " + serverModel.toString() );
        System.out.println();

        clientC.generate( new OTLinearOperations.InsertSEOperation<Character>( 1, 'Z' ) );

        server.integrate( sentOpAQueue.remove( 0 ) );
        clientB.integrate( serverSentOpBQueue.remove( 0 ) );
        clientC.integrate( serverSentOpCQueue.remove( 0 ) );

        System.out.println( "6)" );
        System.out.println( "ClientA: " + clientModelA.toString() + " | " + clientA.toString() );
        System.out.println( "Client Proxy A State: " + server.getProxyState( clientAID ) );
        System.out.println( "ClientB: " + clientModelB.toString() + " | " + clientB.toString() );
        System.out.println( "Client Proxy B State: " + server.getProxyState( clientBID ) );
        System.out.println( "ClientC: " + clientModelC.toString() + " | " + clientC.toString() );
        System.out.println( "Client Proxy C State: " + server.getProxyState( clientCID ) );
        System.out.println( "Server: " + serverModel.toString() );
        System.out.println();

        server.integrate( sentOpBQueue.remove( 0 ) );
        clientA.integrate( serverSentOpAQueue.remove( 0 ) );
        clientC.integrate( serverSentOpCQueue.remove( 0 ) );

        System.out.println( "7)" );
        System.out.println( "ClientA: " + clientModelA.toString() + " | " + clientA.toString() );
        System.out.println( "Client Proxy A State: " + server.getProxyState( clientAID ) );
        System.out.println( "ClientB: " + clientModelB.toString() + " | " + clientB.toString() );
        System.out.println( "Client Proxy B State: " + server.getProxyState( clientBID ) );
        System.out.println( "ClientC: " + clientModelC.toString() + " | " + clientC.toString() );
        System.out.println( "Client Proxy C State: " + server.getProxyState( clientCID ) );
        System.out.println( "Server: " + serverModel.toString() );
        System.out.println();

        server.integrate( sentOpCQueue.remove( 0 ) );
        clientA.integrate( serverSentOpAQueue.remove( 0 ) );
        clientB.integrate( serverSentOpBQueue.remove( 0 ) );

        System.out.println( "8)" );
        System.out.println( "ClientA: " + clientModelA.toString() + " | " + clientA.toString() );
        System.out.println( "Client Proxy A State: " + server.getProxyState( clientAID ) );
        System.out.println( "ClientB: " + clientModelB.toString() + " | " + clientB.toString() );
        System.out.println( "Client Proxy B State: " + server.getProxyState( clientBID ) );
        System.out.println( "ClientC: " + clientModelC.toString() + " | " + clientC.toString() );
        System.out.println( "Client Proxy C State: " + server.getProxyState( clientCID ) );
        System.out.println( "Server: " + serverModel.toString() );
        System.out.println();
    }
}
