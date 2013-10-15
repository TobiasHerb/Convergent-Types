package de.tuberlin.rcd.client.runtime;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

import de.tuberlin.rcd.network.Message;
import de.tuberlin.rcd.network.Transceiver;

/**
 * Server sender thread of the server side transceiver instances.
 */
public final class ClientSender extends Transceiver.TransceiverThreadBase {

    /**
     * Log4J.
     */
    private static final Logger LOGGER = Logger.getLogger( ClientSender.class );

    /**
     * Constructor.
     * @param transceiver Reference the controlling transceiver component.
     */
	public ClientSender(Transceiver transceiver) {
		super( transceiver );
	}

    /** Queue containing all messages that are send to the associated client. */
	private final BlockingQueue<Message> msgQueue = new LinkedBlockingQueue<Message>();
	
	/* (non-Javadoc)
	 * @see com.radt.network.Transceiver.TransceiverThreadBase#initialize()
	 */
	@Override
	protected void initialize() throws Exception {
	}

	/* (non-Javadoc)
	 * @see com.radt.network.Transceiver.TransceiverThreadBase#process()
	 */
	@Override
	protected void process() throws Exception {
		final Message msg = msgQueue.take();
        connection.out.writeObject( msg );
	}

	/* (non-Javadoc)
	 * @see com.radt.network.Transceiver.TransceiverThreadBase#shutdown()
	 */
	@Override
	protected void shutdown() throws Exception {
	}

    /**
     * Enqueue a message in the queue.
     * @param msg The message to send.
     */
    public synchronized void enqueueMessage( Message msg ) {
        // sanity check.
        if( msg == null )
            throw new NullPointerException();
        msgQueue.add( msg );
    }
}
