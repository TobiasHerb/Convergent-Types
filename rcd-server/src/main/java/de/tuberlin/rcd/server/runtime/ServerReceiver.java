package de.tuberlin.rcd.server.runtime;

import java.util.UUID;

import org.apache.log4j.Logger;

import de.tuberlin.rcd.network.Message;
import de.tuberlin.rcd.network.Transceiver;
import de.tuberlin.rcd.protocol.message.MessageFormat;
import de.tuberlin.rcd.protocol.runtimedefinition.IMessageCommand;

/**
 * Server receiver thread of the server side transceiver instances.
 */
public final class ServerReceiver extends Transceiver.TransceiverThreadBase {

    /**
     * Log4J.
     */
    private static final Logger LOGGER = Logger.getLogger( ServerReceiver.class );

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
            connectionManager = (ServerConnectionManager)getDependency( "connectionManager" );
            msgCmdMapping = (ServerCommandMapping)getDependency( "msgCmdMapping" );
        }

        /** Reference to the server connection manager. */
        final ServerConnectionManager connectionManager;

        /** Reference to the message type command mapping. */
        final ServerCommandMapping msgCmdMapping;

        /**
         * Is called on incoming messages to delegate them to appropriate command handler.
         * @param msg The received network message.
         */
        public void dispatchMessage( Message msg ) {
            final MessageFormat.MessageType msgType = (MessageFormat.MessageType)msg.getParameter( "type" );
            LOGGER.info( "received message from client [UID = " + msg.getSourceUID() + "] of [type = " + msgType + "]" );
            // sanity check.
            final UUID clientUID = msg.getSourceUID();
            if( !connectionManager.existsConnection(clientUID) ) {
                throw new IllegalStateException( "Client is not registered" );
            }
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
	public ServerReceiver(Transceiver transceiver) {
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
        // Creation of the message dispatcher can only be done in the regular lifecycle,
        // of the transceiver thread, because of the dependency resolution in the
        // constructor the message command dispatcher.
        msgDispatcher = new MessageCommandDispatcher();
    }

	/* (non-Javadoc)
	 * @see com.radt.network.Transceiver.TransceiverThreadBase#process()
	 */
	@Override
	protected void process() throws Exception {
		Object receivedObj = connection.in.readObject();
	    if( receivedObj instanceof Message ) {
            Message msg = (Message)receivedObj;
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
