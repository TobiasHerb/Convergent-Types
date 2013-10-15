package de.tuberlin.rcd.client.runtime;

import org.apache.log4j.Logger;

import de.tuberlin.rcd.network.Message;
import de.tuberlin.rcd.network.Transceiver;
import de.tuberlin.rcd.protocol.message.MessageFormat;
import de.tuberlin.rcd.protocol.runtimedefinition.IMessageCommand;

/**
 * Client receiver thread of the server side transceiver instances.
 */
public final class ClientReceiver extends Transceiver.TransceiverThreadBase {

    /**
     * Log4J.
     */
    private static final Logger LOGGER = Logger.getLogger( ClientReceiver.class );

    /**
     * The MessageDispatcher is responsible for translating incoming messages
     * to commands and execute them.
     */
    private class MessageCommandDispatcher {

        /**
         * Constructor.
         */
        public MessageCommandDispatcher() {
            // extract the dependencies, from the server factory.
            msgCmdMapping = (ClientCommandMapping)getDependency( "MessageCmdMap" );
        }

        /** Reference to the message type command mapping. */
        final ClientCommandMapping msgCmdMapping;

        /**
         * Is called on incoming messages to delegate them to appropriate command handler.
         * @param msg The received network message.
         */
        public void dispatchMessage( Message msg ) {
            final MessageFormat.MessageType msgType = (MessageFormat.MessageType) msg.getParameter( "type" );
            final IMessageCommand msgCmd = msgCmdMapping.getMessageCommand(msgType);
            if( msgCmd != null ) {
                msgCmd.execute( msg );
            } else {
                throw new IllegalStateException( "Message type not supported" );
            }
        }
    }

    /**
     * Constructor.
     * @param transceiver The transceiver (thread controller).
     */
	public ClientReceiver( Transceiver transceiver ) {
		super( transceiver );
    }

    /** Dispatcher that is responsible for delegating incoming messages
        to associated command handler. */
    private MessageCommandDispatcher msgDispatcher;

	/* (non-Javadoc)
	 * @see com.radt.network.Transceiver.TransceiverThreadBase#initialize()
	 */
	@Override
	protected void initialize() throws Exception {
        msgDispatcher = new MessageCommandDispatcher();
    }

	/* (non-Javadoc)
	 * @see com.radt.network.Transceiver.TransceiverThreadBase#process()
	 */
	@Override
	protected void process() throws Exception {
        final Object receivedObj = connection.in.readObject();
        if( receivedObj instanceof Message ) {
            final Message msg = (Message)receivedObj;
            msgDispatcher.dispatchMessage( msg );
        } else {
            throw new IllegalStateException();
        }
    }

	/* (non-Javadoc)
	 * @see com.radt.network.Transceiver.TransceiverThreadBase#shutdown()
	 */
	@Override
	protected void shutdown() throws Exception {
	}
}
