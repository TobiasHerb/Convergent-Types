package de.tuberlin.rcd.server.runtime;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

/**
 * Entry point for client communication.
 * @author Tobias Herb
 *
 */
public final class ServerConnectionListener {

    /**
     * Log4J.
     */
    private static final Logger LOGGER = Logger.getLogger( ServerConnectionListener.class );

	/**
	 * Constructor.
	 * @param connectionManager The connection manager.
	 * @throws IOException IO error when opening the socket.
	 */
	public ServerConnectionListener( ServerConnectionManager connectionManager) throws IOException {
		// sanity check.
		if( connectionManager == null )
			throw new IllegalArgumentException();
        this.connectionManager = connectionManager;
        this.configuration = connectionManager.configuration;
		this.socket = new ServerSocket( configuration.getPort(), configuration.getBacklog() );
	    this.running = new AtomicBoolean( true );
    }

    /** Server port configuration. */
    private final ServerConfiguration configuration;

	/** Server socket, that accept client requests. */
	private final ServerSocket socket;

	/** Flag that controls tests thread life. */
	private final AtomicBoolean running;
	
	/** Manager that hold all connections. */
	private final ServerConnectionManager connectionManager;

    /** Thread container for listener thread. */
    private Thread listenerThread = null;

	/**
	 * Create a tests thread which accepts initial client requests,
	 * build <code>Connection</code> objects and add them to 
	 * the connection manager. 
	 */
	public void startListener() {
		if( listenerThread == null ) {
            ( listenerThread = new Thread( new Runnable() {
                @Override
                public void run() {
                    try {
                        while( running.get() ) {
                            Socket clientSocket = socket.accept();
                            LOGGER.info( "socket connection established" );
                            // accepted client socket and create connection context.
                            connectionManager.openConnection( clientSocket, true );
                        }
                    } catch( Exception e ) {
                        throw new IllegalStateException( e );
                    } finally {
                        try {
                            socket.close();
                        } catch( IOException e ) {
                            throw new IllegalStateException( e );
                        }
                    }
                }
            }
            ) ).start();
            LOGGER.info( "connection listener started [port = " + configuration.getPort() + "]" );
        }
    }

    /**
     * Stop connection listener thread.
     */
    public void stop() {
        running.set( false );
        if( listenerThread.getState() == Thread.State.WAITING ||
            listenerThread.getState() == Thread.State.BLOCKED ) {
            listenerThread.interrupt();
        }
        LOGGER.info( "connection listener stopped" );
    }
}
