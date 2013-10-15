package de.tuberlin.rcd.client.tests.conflictclient;

import java.net.Socket;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;

import de.tuberlin.rcd.client.runtime.ClientFactory;
import de.tuberlin.rcd.client.types.list.ClientReplicatedList;
import de.tuberlin.rcd.client.types.string.ClientReplicatedString;

/**
 * Entry point for data synchronization sample client.
 *
 * @author Tobias Herb
 *
 */
public class ConflictClientA {

    /**
     * Log4J.
     */
    private static final Logger LOGGER = Logger.getRootLogger();

    /**
     * Main.
     * @param args Not used.
     */
    public static void main(String[] args) {

        //-----------------------------------
        // initialize log4j.
        //-----------------------------------
        final SimpleLayout layout = new SimpleLayout();
        final ConsoleAppender consoleAppender = new ConsoleAppender( layout );
        LOGGER.addAppender( consoleAppender );
        LOGGER.setLevel( Level.WARN );

        try {
            //-----------------------------------
            // initialize tests components.
            //-----------------------------------
            final Socket socket = new Socket( "localhost", 2832 );
            final ClientFactory factory = new ClientFactory();
            final ClientFactory.ClientContext context = factory.create( socket );
            //final IDataManager dataManager = context.dataManager;

            //-----------------------------------
            // initialize types.
            //-----------------------------------
            context.nameRegistry.insertMapping( "ReplicatedString", ClientReplicatedString.class.getName() );
            context.nameRegistry.insertMapping( "ReplicatedList", ClientReplicatedList.class.getName() );

            /*final IReplicatedType<Character> testList = dataManager.createReplicatedType( "testList",
                    new IReplicatedObjectFactory<Character>() {
                        @Override
                        public IReplicatedType<Character> instantiate( final String name, final UUID typeUID, final IDataManager dataManager ) {
                            return new ClientReplicatedList<Character>( name, typeUID, Character.class, dataManager );
                        }
                    } );*/

            //-----------------------------------
            // do some stuff.
            //-----------------------------------
            /*final List<Character> list =  (List<Character>)testList;
            final AbstractClientReplicatedType<Character> type = (AbstractClientReplicatedType<Character>)testList;

            list.add( 'a' );
            list.add( 'b' );
            list.add( 'c' );
            list.add( 'd' );
            list.add( 'e' );
            list.add( 'f' );
            list.add( 'g' );

            //type.setSingleStepMode( true );

            System.in.read();

            list.set( 1, 'B' );
            type.explicitSend();*/

        } catch( Throwable t ) {
            //LOGGER.error( "fatal error", t );
            t.printStackTrace();
        }
    }
}
