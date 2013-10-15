package de.tuberlin.rcd.server.runtime;


import java.util.EnumMap;
import java.util.Map;

import org.apache.log4j.Logger;

import de.tuberlin.rcd.network.Message;
import de.tuberlin.rcd.protocol.message.MessageFormat;
import de.tuberlin.rcd.protocol.runtimedefinition.IMessageCommand;
import de.tuberlin.rcd.protocol.runtimedefinition.TypeNameRegistry;
import de.tuberlin.rcd.protocol.types.IReplicatedType;
import de.tuberlin.rcd.server.types.AbstractServerReplicatedType;

/**
 *
 */
public final class ServerCommandMapping {

    /**
     * Log4J.
     */
    private static final Logger LOGGER = Logger.getLogger( ServerCommandMapping.class );

    /**
     * Constructor.
     */
    public ServerCommandMapping(ServerConnectionManager connectionManager, ServerDataManager dataManager, TypeNameRegistry nameRegistry) {
        // sanity check.
        if( connectionManager == null )
            throw new IllegalArgumentException();
        if( dataManager == null )
            throw new IllegalArgumentException();
        if( nameRegistry == null )
            throw new IllegalArgumentException();

        //this.connectionManager = connectionManager;
        this.dataManager = dataManager;
        this.nameRegistry = nameRegistry;
        //this.msgBuilder = new MessageBuilder( connectionManager.configuration.serverUID, false );

        // message - command mapping.
        msgCmdMap.put( MessageFormat.MessageType.MSG_TYPE_REGISTER_BY_REPLICATED_TYPE, new RegisterCommand() );
        msgCmdMap.put( MessageFormat.MessageType.MSG_TYPE_OPERATION_FOR_REPLICATED_TYPE, new OperationCommand() );
        msgCmdMap.put( MessageFormat.MessageType.MSG_TYPE_CREATE_REPLICATED_TYPE, new CreateCommand() );
    }

    /** Defines a mapping of a message type to a handler. */
    private final Map<MessageFormat.MessageType,IMessageCommand> msgCmdMap
            = new EnumMap<MessageFormat.MessageType, IMessageCommand>( MessageFormat.MessageType.class );

    /** Server connection manager. */
    //private final ServerConnectionManager connectionManager;

    /** Server-side data manager. */
    private final ServerDataManager dataManager;

    /** Resolves client type names to server type names. */
    private final TypeNameRegistry nameRegistry;

    /**
     * Resolves a message type to a command.
     * @param msgType Type of the message.
     * @return The associated command.
     */
    public IMessageCommand getMessageCommand( MessageFormat.MessageType msgType ) {
        return msgCmdMap.get( msgType );
    }

    /**
     *
     */
    private final class RegisterCommand implements IMessageCommand {
        @Override
        public void execute( Message msg ) {
            final String typeName = msg.getParameter( MessageFormat.MessageAttribute.MSG_ATTR_TYPE_NAME ).toString();
            if( dataManager.existsReplicatedType( typeName ) ) {
                final AbstractServerReplicatedType<?> type = (AbstractServerReplicatedType<?>)dataManager.getReplicatedType( typeName );
                final String clazzName = msg.getParameter( MessageFormat.MessageAttribute.MSG_ATTR_CLASS_NAME ).toString();
                final String specificTypeName = nameRegistry.getSpecificName( clazzName );
                if( !type.getClass().getName().equals( specificTypeName ) ) {
                    // TODO: Error handling.
                    throw new IllegalStateException();
                }
                if( !type.isClientRegistered( msg.getSourceUID() ) ) {
                    // Fill client side structure.
                    // This is a critical section, because if we fill a new registered client structure,
                    // we must not process and update new received operations for this structure until the
                    // fill process is finished else the new registered client could diverge in state!.

                    type.fillClient( msg.getSourceUID() );

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
    private final class OperationCommand implements IMessageCommand {
        @Override
        public void execute( Message msg ) {
            final String typeName = msg.getParameter( MessageFormat.MessageAttribute.MSG_ATTR_TYPE_NAME ).toString();
            if( dataManager.existsReplicatedType( typeName ) ) {
                final AbstractServerReplicatedType<?> type = (AbstractServerReplicatedType<?>) dataManager.getReplicatedType( typeName );
                if( type.isClientRegistered( msg.getSourceUID() ) ) {
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
    private final class CreateCommand implements IMessageCommand {
        @Override
        public void execute( Message msg ) {
            final String typeName = msg.getParameter( MessageFormat.MessageAttribute.MSG_ATTR_TYPE_NAME ).toString();
            if( !dataManager.existsReplicatedType( typeName ) ) {
                final String clazzName = msg.getParameter( MessageFormat.MessageAttribute.MSG_ATTR_CLASS_NAME ).toString();
                try {
                    final String specificTypeName = nameRegistry.getSpecificName( clazzName );
                    final Class<?> clazz = Class.forName( specificTypeName );
                    //if( clazz.isAssignableFrom( IReplicatedType.class ) ) {
                        @SuppressWarnings("rawtypes")
						final Class<? extends IReplicatedType> replicatedTypeClazz = clazz.asSubclass( IReplicatedType.class );
                        dataManager.createReplicatedType( typeName, msg.getSourceUID(), replicatedTypeClazz );
                    //} else {
                        // TODO: Error handling.
                    //    throw new IllegalStateException();
                    //}
                } catch( ClassNotFoundException e ) {
                    // TODO: Error handling.
                    throw new IllegalStateException( e );
                }
            } else {

                //throw new IllegalStateException();

                // type name already used.
                IMessageCommand cmd = msgCmdMap.get( MessageFormat.MessageType.MSG_TYPE_REGISTER_BY_REPLICATED_TYPE );
                cmd.execute( msg );
            }
        }
    }
}
