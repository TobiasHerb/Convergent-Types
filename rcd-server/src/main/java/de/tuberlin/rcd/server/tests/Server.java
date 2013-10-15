package de.tuberlin.rcd.server.tests;

import java.util.UUID;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;

import de.tuberlin.rcd.server.runtime.ServerConfiguration;
import de.tuberlin.rcd.server.runtime.ServerConnectionListener;
import de.tuberlin.rcd.server.runtime.ServerFactory;
import de.tuberlin.rcd.server.runtime.ServerFactory.ServerContext;
import de.tuberlin.rcd.server.types.list.ServerReplicatedList;
import de.tuberlin.rcd.server.types.string.ServerReplicatedString;

/**
 * Entry point of the data synchronization tests.
 * 
 * @author Tobias Herb
 *
 */
public final class Server {

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
        LOGGER.setLevel( Level.INFO );

        //-----------------------------------
        // initialize tests components.
        //-----------------------------------
        final ServerFactory factory = new ServerFactory();
        final ServerContext context = factory.create(
                new ServerConfiguration( UUID.randomUUID(), 2832, 1000, "localhost" )
            );
        LOGGER.info( "tests started" );

        //-----------------------------------
        // initialize types.
        //-----------------------------------
        context.nameRegistry.insertMapping( "ReplicatedString", ServerReplicatedString.class.getName() );
        context.nameRegistry.insertMapping( "ReplicatedList", ServerReplicatedList.class.getName() );

        //-----------------------------------
        // execute connection listener.
        //-----------------------------------
		try {
			new ServerConnectionListener( context.connectionManager ).startListener();
		} catch( Throwable t ) {
			LOGGER.error( "fatal error", t );
		}
	}
}