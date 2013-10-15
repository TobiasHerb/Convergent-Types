package de.tuberlin.rcd.client.tests.conflictclient;

import java.net.Socket;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;

import de.tuberlin.rcd.client.runtime.ClientFactory;
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
public class ConflictClientB {

    /**
     * Log4J.
     */
    private static final Logger LOGGER = Logger.getRootLogger();

    /**
     * Main.
     * @param args Not used.
     */
    @SuppressWarnings("unchecked")
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
            final IDataManager dataManager = context.dataManager;

            //-----------------------------------
            // initialize types.
            //-----------------------------------
            context.nameRegistry.insertMapping( "ReplicatedString", ClientReplicatedString.class.getName() );
            context.nameRegistry.insertMapping( "ReplicatedList", ClientReplicatedList.class.getName() );

            final IReplicatedType<Character> testList = dataManager.registerByReplicatedType( "testList",
                    new IReplicatedObjectFactory<Character>() {
                        @Override
                        public IReplicatedType<Character> instantiate( final String name, final UUID typeUID, final IDataManager dataManager ) {
                            return new ClientReplicatedList<Character>( name, typeUID, Character.class, dataManager );
                        }
                    } );

            //-----------------------------------
            // do some stuff.
            //-----------------------------------
            final List<Character> list =  (List<Character>)testList;

            Thread.sleep( 2000 );

            System.out.println( list.toString() );

        } catch( Throwable t ) {
            //LOGGER.error( "fatal error", t );
            t.printStackTrace();
        }
    }
}
