package de.tuberlin.rcd.server.runtime;

import de.tuberlin.rcd.network.Transceiver;
import de.tuberlin.rcd.network.common.Event;
import de.tuberlin.rcd.network.common.IEventListener;
import de.tuberlin.rcd.protocol.runtimedefinition.IDataManager;
import de.tuberlin.rcd.protocol.runtimedefinition.TypeNameRegistry;

public class ServerFactory {

    /**
     * Class holding the context of the data sync server.
     */
    public static class ServerContext {

        /**
         * Constructor.
         * @param dataManager Reference to the server side data manager.
         * @param connectionManager Reference to the server side connection manager.
         * @param nameRegistry Reference to the server side name registry.
         */
        public ServerContext( IDataManager dataManager, ServerConnectionManager connectionManager, TypeNameRegistry nameRegistry) {
            this.dataManager = dataManager;
            this.connectionManager = connectionManager;
            this.nameRegistry = nameRegistry;
        }

        /** Reference to the server side data manager. */
        public final IDataManager dataManager;

        /** Reference to the server side connection manager. */
        public final ServerConnectionManager connectionManager;

        /** Reference to the server side name registry.
            Responsible for resolving type name between client and server. */
        public final TypeNameRegistry nameRegistry;
    }

    /**
     * Create a server context.
     * @param configuration The server configuration.
     * @return A initialized server context.
     */
    public ServerContext create( ServerConfiguration configuration ) {
        // sanity check.
        if( configuration == null )
            throw new NullPointerException();

        // Create the all server components.
        final ServerConnectionManager connectionManager = new ServerConnectionManager( configuration );
        final ServerDataManager dataManager = new ServerDataManager( connectionManager );
        final TypeNameRegistry nameRegistry = new TypeNameRegistry();
        final ServerCommandMapping msgCmdMapping = new ServerCommandMapping( connectionManager, dataManager, nameRegistry);

        // Use the event mechanism to embed dependencies on
        // the transceiver instances of the lower network layer.
        // Avoid cyclic dependencies between components. Only the
        // upper layer knows lower layer. software design principles!!
        connectionManager.addEventListener( "CM_INJECT_TRANSCEIVER_DEPENDENCIES", new IEventListener() {
            @Override
            public void handleEvent( Event event ) {
                final Transceiver transceiver = (Transceiver)event.data;
                transceiver.getReceiver().storeDependency( "connectionManager", connectionManager );
                transceiver.getReceiver().storeDependency( "msgCmdMapping", msgCmdMapping );
            }
        });

        return new ServerContext( dataManager, connectionManager, nameRegistry);
    }
}
