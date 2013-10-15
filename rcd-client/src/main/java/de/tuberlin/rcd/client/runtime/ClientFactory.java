package de.tuberlin.rcd.client.runtime;

import java.io.IOException;
import java.net.Socket;
import java.util.UUID;

import de.tuberlin.rcd.network.Connection;
import de.tuberlin.rcd.network.MessageBuilder;
import de.tuberlin.rcd.network.Transceiver;
import de.tuberlin.rcd.network.TransceiverFactory;
import de.tuberlin.rcd.protocol.runtimedefinition.IDataManager;
import de.tuberlin.rcd.protocol.runtimedefinition.TypeNameRegistry;
import de.tuberlin.rcd.protocol.types.IReplicatedType;

/**
 * Create the client context.
 */
public final class ClientFactory {

    public static final class ClientContext {

        /**
         * Constructor.
         * @param dataManager Reference to the server side data manager.
         * @param transceiver Reference to the client side transceiver component.
         * @param nameRegistry Reference to the server side name registry.
         */
        public ClientContext( IDataManager dataManager, Transceiver transceiver, TypeNameRegistry nameRegistry) {
            this.dataManager = dataManager;
            this.transceiver = transceiver;
            this.nameRegistry = nameRegistry;
        }

        /** Reference to the client side data manager. */
        public final IDataManager dataManager;

        /** Reference to the client side transceiver component. */
        public final Transceiver transceiver;

        /** Reference to the client side name registry.
            Responsible for resolving type name between client and server. */
        public final TypeNameRegistry nameRegistry;
    }

    /** Reference to the client side data manager. */
    private IDataManager dataManager;


    public ClientContext create( Socket socket ) throws IOException {
        final Connection connection = new Connection( socket );
        final UUID clientUID;
        try {
            // The first thing after establishing the connection
            // is to receive the client UID from the tests.
            clientUID = (UUID) connection.in.readObject();
            connection.setUID( clientUID );
        } catch( Exception e ) {
            throw new IllegalStateException( e );
        }
        final TransceiverFactory factory = new TransceiverFactory( ClientReceiver.class, ClientSender.class );
        final Transceiver transceiver = factory.create( connection,
                new Runnable() {
                    @Override
                    public void run() {
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // TODO: be careful for concurrency issues.
                            for( IReplicatedType<?> type : dataManager.getAllReplicatedTypes() ) {
                                type.stopProcessing();
                            }
                            connection.close();
                        } catch( IOException e ) {
                            throw new IllegalStateException( e );
                        }
                    }
                }
        );

        final MessageBuilder messageBuilder = connection.msgBuilder;
        final TypeNameRegistry nameRegistry = new TypeNameRegistry();
        dataManager = new ClientDataManager( connection, transceiver, nameRegistry, messageBuilder );
        final ClientCommandMapping msgCmdMapping = new ClientCommandMapping( dataManager );
        // inject dependencies for transmitter and receiver.
        transceiver.getReceiver().storeDependency("MessageCmdMap", msgCmdMapping);
        //transceiver.getReceiver().storeDependency("DataManager", dataManager );
        // start connection.
        transceiver.start();
        return new ClientContext( dataManager, transceiver, nameRegistry);
    }
}
