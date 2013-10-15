/**
 *
 */
package de.tuberlin.rcd.client.tests.consoleclient;

import java.net.Socket;
import java.util.Random;
import java.util.UUID;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;

import de.tuberlin.rcd.client.runtime.ClientFactory;
import de.tuberlin.rcd.client.runtime.ClientFactory.ClientContext;
import de.tuberlin.rcd.client.types.list.ClientReplicatedList;
import de.tuberlin.rcd.client.types.string.ClientReplicatedString;
import de.tuberlin.rcd.protocol.runtimedefinition.IDataManager;
import de.tuberlin.rcd.protocol.runtimedefinition.IReplicatedObjectFactory;
import de.tuberlin.rcd.protocol.types.IReplicatedType;

/**
 * Entry point for data synchronization sample client.
 *
 * @author Tobias Herb
 *
 */
public final class ClientA {

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
        LOGGER.setLevel( Level.OFF );

        try {
            //-----------------------------------
            // initialize tests components.
            //-----------------------------------
            final Socket socket = new Socket( "localhost", 2832 );
            final ClientFactory factory = new ClientFactory();
            final ClientContext context = factory.create( socket );
            final IDataManager dataManager = context.dataManager;

            //-----------------------------------
            // initialize types.
            //-----------------------------------
            context.nameRegistry.insertMapping( "ReplicatedString", ClientReplicatedString.class.getName() );
            context.nameRegistry.insertMapping( "ReplicatedList", ClientReplicatedList.class.getName() );

            //final ClientReplicatedString testB = (ClientReplicatedString) dataManager.createReplicatedType( "testB", null, ClientReplicatedString.class );

            final ClientReplicatedList<Character> testList = (ClientReplicatedList<Character>) dataManager.createReplicatedType( "testList",
                    new IReplicatedObjectFactory<Character>() {
                        @Override
                        public IReplicatedType<Character> instantiate( final String name, final UUID typeUID, final IDataManager dataManager ) {
                            return new ClientReplicatedList<Character>( name, typeUID, Character.class, dataManager );
                        }
                    } );

            //----------------------------------------
            // (1) ReplicatedString test
            //----------------------------------------

            /*Random rand1 = new Random();
            int i = 0;
            while( i < 10000 ) {
                int pos = testB.length() < 2 ? 0 : rand1.nextInt( testB.length() - 1 );
                //if( rand1.nextInt( 200 ) < 5 && testB.length() > 5 )
                //    testB.deleteChar( pos );
                //else
                    testB.insertChar( pos, 'A' ); //(char)( (i % 26) + 'A' ) );
                Thread.sleep( rand1.nextInt( 10 ) );
                ++i;
            }
            System.out.println( "FINISH" );
            System.in.read();
            System.out.println( testB.toString() );*/

            //----------------------------------------
            // (2) ReplicatedList test
            //----------------------------------------

            /*Random rand1 = new Random();
            int i = 0;
            while( i < 10000 ) {
                int pos = testList.size() < 2 ? 0 : rand1.nextInt( testList.size() - 1 );
                if( rand1.nextInt( 200 ) < 5 && testList.size() > 5 )
                    testList.remove( pos );
                else
                    testList.add( pos, (char)( (i % 26) + 'a' ) );
                Thread.sleep( rand1.nextInt( 10 ) );
                ++i;
            }
            System.out.println( "FINISH" );
            System.in.read();
            System.out.println( testList.toString() );*/

            //----------------------------------------
            // (3) ReplicatedList test
            //----------------------------------------

            Random rand1 = new Random();
            int i = 0;
            while( i < 10000 ) {
                int pos = testList.size() < 2 ? 0 : rand1.nextInt( testList.size() - 1 );
                testList.add( pos, (char)( (i % 26) + 'a' ) );
                Thread.sleep( 10 );
                ++i;
            }
            System.out.println( "FINISH" );
            System.in.read();
            System.out.println( testList.toString() );

            //----------------------------------------
            // (4) ReplicatedString & ReplicatedList test
            //----------------------------------------

            /*Random rand1 = new Random();
            int i = 0;
            while( i < 10000 ) {

                int pos1 = testB.length() < 2 ? 0 : rand1.nextInt( testB.length() - 1 );
                if( rand1.nextInt( 200 ) < 5 && testB.length() > 5 )
                    testB.deleteChar( pos1 );
                else
                    testB.insertChar( pos1, (char)( (i % 26) + 'A' ) );

                int pos2 = testList.size() < 2 ? 0 : rand1.nextInt( testList.size() - 1 );
                if( rand1.nextInt( 200 ) < 5 && testList.size() > 5 )
                    testList.remove( pos2 );
                else
                    testList.add( pos2, (char)( (i % 26) + 'a' ) );

                Thread.sleep( rand1.nextInt( 15 ) );
                ++i;
            }
            System.out.println( "FINISH" );
            System.in.read();
            System.out.println( testB.toString() );
            System.in.read();
            System.out.println( testList.toString() );*/

        } catch( Throwable t ) {
            //LOGGER.error( "fatal error", t );
            t.printStackTrace();
        }
    }
}
