package de.tuberlin.rcd.client.runtime;

import java.util.EnumMap;
import java.util.Map;

import org.apache.log4j.Logger;

import de.tuberlin.rcd.client.types.AbstractClientReplicatedType;
import de.tuberlin.rcd.network.Message;
import de.tuberlin.rcd.protocol.message.MessageFormat;
import de.tuberlin.rcd.protocol.runtimedefinition.IDataManager;
import de.tuberlin.rcd.protocol.runtimedefinition.IMessageCommand;
import de.tuberlin.rcd.protocol.types.AbstractFillData;

/**
 *
 * Dependencies:
 *  - ClientDataManager
 */
public final class ClientCommandMapping {

    /**
     * Log4J.
     */
    private static final Logger LOGGER = Logger.getLogger( ClientCommandMapping.class );

    /**
     * Constructor.
     */
    public ClientCommandMapping(IDataManager dataManager) {
        // sanity check.
        if( dataManager == null )
            throw new IllegalArgumentException();
        this.dataManager = dataManager;
        msgCmdMap.put( MessageFormat.MessageType.MSG_TYPE_OPERATION_FOR_REPLICATED_TYPE, new OperationCommand() );
        msgCmdMap.put( MessageFormat.MessageType.MSG_TYPE_FILL_REPLICATED_TYPE, new FillCommand() );
    }

    /**
     *
     */
    private final Map<MessageFormat.MessageType,IMessageCommand> msgCmdMap
            = new EnumMap<MessageFormat.MessageType, IMessageCommand>( MessageFormat.MessageType.class );


    private final IDataManager dataManager;

    /**
     * Resolve the message type to a associated command implementation.
     * @param msgType The msg provided by the received message.
     * @return The associated command implementation
     */
    public IMessageCommand getMessageCommand( MessageFormat.MessageType msgType ) {
        return msgCmdMap.get( msgType );
    }

    /**
     *
     */
    private final class OperationCommand implements IMessageCommand {
        @Override
        public void execute( Message msg ) {
            final String typeName = msg.getParameter( MessageFormat.MessageAttribute.MSG_ATTR_TYPE_NAME ).toString();
            if( dataManager.existsReplicatedType( typeName ) ) {
                AbstractClientReplicatedType<?> type = (AbstractClientReplicatedType<?>) dataManager.getReplicatedType(typeName);
                if( type != null ) {
                    type.enqueueReceivedMsg( msg );
                } else {
                    // TODO: Error handling.
                    throw new IllegalStateException();
                }
            } else {
                // TODO: Error handling.
                throw new IllegalStateException();
            }
        }
    }

    /**
     *
     */
    private final class FillCommand implements IMessageCommand {
        @Override
        public void execute( Message msg ) {
            final String typeName = msg.getParameter( MessageFormat.MessageAttribute.MSG_ATTR_TYPE_NAME ).toString();
            if( dataManager.existsReplicatedType( typeName ) ) {
                AbstractClientReplicatedType<?> type = (AbstractClientReplicatedType<?>)dataManager.getReplicatedType( typeName );
                if( type != null ) {
                    final AbstractFillData data = (AbstractFillData)msg.getParameter( MessageFormat.MessageAttribute.MSG_ATTR_FILL_DATA );
                    if( data == null )
                        throw new NullPointerException();
                    type.fill( data );
                } else {
                    // TODO: Error handling.
                    throw new IllegalStateException();
                }
            } else {
                // TODO: Error handling.
                throw new IllegalStateException();
            }
        }
    }
}
