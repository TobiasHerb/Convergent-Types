package de.tuberlin.rcd.network;

import org.apache.log4j.Logger;

import java.io.EOFException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 
 * @author Tobias Herb
 *
 * TODO: use thread pools to reduce thread creation effort!
 */
public final class Transceiver {

    /**
     * Log4J.
     */
    private static final Logger LOGGER = Logger.getLogger( Transceiver.class );

	/**
	 * The TransceiverThreadBase class is abstract and defines the life cycle
	 * of the sender/receiver processing. The superior transceiver component
     * is responsible for creation and termination of the threads. The sender/
     * receiver implementation is derived from this std.
	 * 
	 * @author Tobias Herb
	 *
	 */
	public static abstract class TransceiverThreadBase implements Runnable {
		
		/**
		 * Constructor.
		 * @param transceiver The connection context this thread is processing.
		 */
		public TransceiverThreadBase( Transceiver transceiver ) {
			// sanity check.
			if( transceiver == null )
				throw new NullPointerException();
			
			this.transceiver = transceiver;
			this.connection = transceiver.getConnection(); 
		    this.finished = new AtomicBoolean( false );
        }
		
		/** That associated transceiver component. */
		protected final Transceiver transceiver;
		
		/** The connection that is processed. */
		protected final Connection 	connection;
		
		/** Flag that indicates stopped processing. */
		private final AtomicBoolean finished;

        /** Storing dependencies for the implementer. */
        private final Map<String,Object> dependencyMap = new HashMap<String, Object>();

		/**
		 * Threads entry point.
		 */
		@Override
		public void run() {
			try {
				// initialize the thread.
				initialize();
                LOGGER.info( "transceiver thread " + this.getClass().getSimpleName() + " initialized [uid = " + connection.getUID() + "]" );
				while( transceiver.isActive() ) {
					// process the thread.
					process();
				}

			} catch( Exception e ) {
                if( e instanceof EOFException ) {
                    LOGGER.info( "connection context collapsed [uid = " + connection.getUID() + "]" );
                }
                if( transceiver.isActive() && !( e instanceof EOFException ) ) {
                    // TODO: change this
                    throw new IllegalStateException( e );
                }

			} finally {
				try {
					// shutdown the thread.
					shutdown();
                    LOGGER.info( "transceiver thread " + this.getClass().getSimpleName() + " shutdown [uid = " + connection.getUID() + "]" );

				} catch( Exception e ) {
                    // TODO: change this
                    throw new IllegalStateException( e );

                } finally {
                    // must be placed before quitProcessing to prevent a deadlock!
                    // stop is synchronized and internally polls until both threads
                    // reach the finished = true condition.
					finished.set( true );
					// stop threads.
					transceiver.stop();
				}				
			}
		}
		
		/**
		 * Initialize the thread and is called before entering the processing loop.
		 * Here gets thread specific resources allocated. 
		 * @throws Exception 
		 */
		protected abstract void initialize() throws Exception;
		
		/**
		 * Process the current connection and is called every processing loop iteration.
		 * ATTENTION: We use the classic blocking socket connection.
		 * @throws Exception
		 */
		protected abstract void process() throws Exception;
		
		/**
		 * Clean up after connection processing. Free the thread specific resources.
		 * @throws Exception
		 */
		protected abstract void shutdown() throws Exception;
	
		/**
		 * Check if the processing is finished.
		 */
		public boolean isFinished() {
			return finished.get();
		}

        protected Object getDependency( String name ) {
            return dependencyMap.get( name );
        }

        public void storeDependency( String name, Object dependency ) {
            // sanity check.
            if( dependency == null )
                throw new IllegalArgumentException();
            dependencyMap.put( name, dependency );
        }
	}
	
	/**
	 * Constructor.
	 * @param connection The client connection.
	 */
	public Transceiver( Connection connection,
                        Runnable startingCallback,
						Runnable closingCallback ) {
		// sanity check.
		if( connection == null )
			throw new NullPointerException();

		this.connection  = connection;
        this.startingCallback = startingCallback;
		this.closingCallback = closingCallback;
        this.active = new AtomicBoolean( false );
	}
	
	/** The client connection that is processed by the transceiver. */
	private final Connection connection;
	
	/** Flag that indicates if the sender and receiver thread of the
	    transceiver is running. */
	private final AtomicBoolean active;

	/** Contains the code for the sender thread. */
	private TransceiverThreadBase transmitter = null;
	
	/** Contains the code for the receiver thread. */
	private TransceiverThreadBase receiver = null;

    /** Store the reference to the sender thread container. */
    private Thread transmitterThread = null;

    /** Store the reference to the receiver thread container. */
    private Thread receiverThread = null;

    /** Callback handler that is executed after starting sender and receiver threads. */
    private final Runnable startingCallback;

	/** Callback handler that is executed after shutting down the transceiver component. */
	private final Runnable closingCallback;

	/**
	 * 
	 * @param transmitter
	 */
	public void setTransmitter( TransceiverThreadBase transmitter ) {
		if( transmitter == null )
			throw new NullPointerException();
		this.transmitter = transmitter; 
	}
	
	/**
	 * 
	 * @param receiver
	 */
	public void setReceiver( TransceiverThreadBase receiver ) {
		if( receiver == null )
			throw new NullPointerException();
		this.receiver = receiver; 
	}	
	
	/**
	 * Returns the associated connection for this transceiver.
	 * @return The associated connection.
	 */
	public Connection getConnection() {
		return connection;
	}
	
	/**
	 * Indicates if the transceiver threads are running or not.
	 */
	public boolean isActive() {
		return active.get();
	}
	
	/**
	 * Start the processing of sender and receiver thread
	 */
	public void start() {
        // sanity check.
		if( !active.get() && transmitterThread == null && receiverThread == null ) {
			// set the condition for entering the processing loop.
            active.set( true );

            // create thread containers and start their execution.
            ( transmitterThread = new Thread( transmitter ) ).start();
			( receiverThread = new Thread( receiver ) ).start();

            // starting callback handler is called after starting sender and receiver thread.
            // be careful: executed in the ConnectionListener thread.
            if( startingCallback != null ) {
                startingCallback.run();
            }

            LOGGER.info( "transceiver threads started [uid = " + connection.getUID() + "]" );
		}
	}
	
	/**
	 * Stops processing of sender and receiver thread. 
	 */
	public synchronized void stop() {
		// sanity check.
        if( active.get() && transmitterThread != null && receiverThread != null ) {
            // set the condition for exiting the processing loop
            active.set( false );

            // this style allows multiple blocking instructions in the sender/receiver processing implementation.
            // interrupt transmitter and receiver thread for releasing their locks, if they hold one.
            // active polling, and if necessary interrupting, until life cycles of both threads reach
            // the 'thread.finished = true' condition.
            while( !receiver.isFinished() ) {
                if( receiverThread.getState() == Thread.State.WAITING ||
                    receiverThread.getState() == Thread.State.BLOCKED ) {
                    receiverThread.interrupt();
                }
            }

            while( !transmitter.isFinished() ) {
                if( transmitterThread.getState() == Thread.State.WAITING ||
                    transmitterThread.getState() == Thread.State.BLOCKED ) {
                    transmitterThread.interrupt();
                }
            }

            // notification of the external environment.
			if( closingCallback != null ) {
				closingCallback.run();
			}

            LOGGER.info( "transceiver threads terminated [uid = " + connection.getUID() + "]" );
		}
	}

    /**
     *
     * @return
     */
    public TransceiverThreadBase getTransmitter() {
        return transmitter;
    }

    /**
     *
     * @return
     */
    public TransceiverThreadBase getReceiver() {
        return receiver;
    }
}