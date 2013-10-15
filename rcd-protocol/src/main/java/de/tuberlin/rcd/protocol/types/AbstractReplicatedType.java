package de.tuberlin.rcd.protocol.types;

import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import de.tuberlin.rcd.network.Message;
import de.tuberlin.rcd.network.common.EventDispatcher;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.OTOperationDefinition;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.OTSystemDefinition;
import de.tuberlin.rcd.protocol.message.MessageFormat;

/**
 * This abstract class forms a common code base that share client and server implementations.
 */
public abstract class AbstractReplicatedType<T> extends EventDispatcher implements IReplicatedType<T> {

    /**
     * Log4J.
     */
     private static final Logger LOGGER = Logger.getLogger( AbstractReplicatedType.class );

    /**
     *
     */
    public static enum TypeState {
        TYPE_STATE_UNINITILAIZED,   // client state after creation.
        TYPE_STATE_OPERATIVE,       // normal operation mode.
        TYPE_STATE_LOCKED,          // type is not allowed to send operations and can only receive.
        TYPE_STATE_EXCLUSIVE_WRITE  // type receives no more remote operations and has exclusive write privilege.
    }

    /**
     * Internal remote processing thread. Every replicated data type (client and server) contains
     * a thread, that is responsible for integrating remote operations. The lifecycle of this internal
     * thread is controlled by the <code>startProcessing</code> and <code>stopProcessing</code> methods
     * of
     */
    private class ProcessingCode implements Runnable {

        /** This flag indicates if the processing loop is running. */
        final AtomicBoolean running = new AtomicBoolean( false );

        /** This flag indicates if the processing loop is finished. */
        final AtomicBoolean finished = new AtomicBoolean( false );

        @Override
        @SuppressWarnings("unchecked")
        public void run() {
            while( running.get() ) {
                try {

                    //------------------------ BEGIN EXPERIMENTAL ------------------------
                    /*
                    if( receiveLatch != null ) {
                        try {
                        receiveLatch.await();
                        } catch( Exception e ) {
                            e.printStackTrace();
                        }
                        if( singleStepMode ) {
                            receiveLatch = new CountDownLatch( 1 );
                        } else {
                            receiveLatch = null;
                        }
                    }
                    */
                    //------------------------ END EXPERIMENTAL ------------------------

                    final Message msg = receiverQueue.take();
                    if( msg.getParameter( MessageFormat.MessageAttribute.MSG_ATTR_MSG_TYPE )
                            == MessageFormat.MessageType.MSG_TYPE_OPERATION_FOR_REPLICATED_TYPE  ) {
                        final OTOperationDefinition.OTOperation<T> remoteOp =
                                (OTOperationDefinition.OTOperation<T>) msg.getParameter( MessageFormat.MessageAttribute.MSG_ATTR_OPERATION );

                        // the lock is used for the fill-phase after connection and local locking.
                        lockRemoteOpProcessing();
                            processRemoteOperation( msg, remoteOp );
                        unlockRemoteOpProcessing();

                    } else {
                        throw new IllegalStateException();
                    }
                } catch( InterruptedException e ) {
                    // Do nothing.
                }
            }
            finished.set( true );
        }
    }

    //------------------------ BEGIN EXPERIMENTAL ------------------------
    /*
    protected boolean singleStepMode = false;

    private CountDownLatch receiveLatch = null;

    public void setSingleStepMode( boolean singleStepMode ) {
        this.singleStepMode = singleStepMode;
        receiveLatch = new CountDownLatch( 1 );
    }

    public void  explicitReceive() {
        receiveLatch.countDown();
    }
    */
    //------------------------ END EXPERIMENTAL ------------------------

    /**
     * Constructor.
     * @param name The name of the replicated type.
     * @param typeUID Every type is assigned a unique id.
     */
    public AbstractReplicatedType( String name, UUID typeUID ) {
        // sanity check.
        if( name == null )
            throw new NullPointerException();
        if( typeUID == null )
            throw new NullPointerException();

        this.name = name;
        this.typeUID = typeUID;
        this.receiverQueue = new LinkedBlockingQueue<Message>();
        this.processingCode = new ProcessingCode();
        this.processingThread = new Thread( processingCode );
        this.processingThread.setName( "Thread( " + name + " )" );
    }

    /** A queue that stores all received remote operations.*/
    protected final BlockingQueue<Message> receiverQueue;

    /** UID of the replicated type. */
    public final UUID typeUID;

    /** Name of the replicated type. */
    public final String name;

    /** Thread container for the remote operation processing. */
    protected final Thread processingThread;

    /** Code for integration thread. */
    protected final ProcessingCode processingCode;

    /** Lock the remote processing thread.
        The lock is used for the fill-phase after connection
        and local remote operation locking. */
    protected final Lock lock = new ReentrantLock();

    /** This flag represents the current state of the replicated type.
        It controls sending (locked) and receiving (exclusive write) activity of the type. **/
    protected TypeState typeState = null;

    protected void setTypeState( TypeState state ) {
        typeState = state;
    }

    public TypeState getTypeState() {
        return typeState;
    }

    /**
     * Enqueue a received remote operation.
     * @param msg The received operation.
     */
    @Override
    public void enqueueReceivedMsg( Message msg ) {
        // sanity check.
        if( msg == null )
            throw new NullPointerException();

        receiverQueue.add( msg );
    }

    /**
     * Start the internal worker thread for remote operation reception and submission.
     */
    @Override
    public void startProcessing() {
        // sanity check.
        if( processingThread.isAlive() || processingCode.running.get() )
            throw new IllegalStateException();

        processingCode.running.set( true );
        processingThread.start();
        LOGGER.info( "replicated type is started [name = " + name + "]" );
    }

    /**
     * Terminate internal worker thread.
     */
    @Override
    public void stopProcessing() {
        // sanity check.
        if( !processingCode.running.get() )
            throw new IllegalStateException();

        processingCode.running.set( false );
        while( !processingCode.finished.get() ) {
            if( processingThread.getState() == Thread.State.WAITING ||
                processingThread.getState() == Thread.State.BLOCKED ) {
                processingThread.interrupt();
            }
        }
        LOGGER.info( "replicated type is terminated [name = " + name + "]" );
    }

    /**
     * Lock the remote processing thread.
     */
    public void lockRemoteOpProcessing() {
        lock.lock();
    }

    /**
     * Unlock the remote processing thread.
     */
    public void unlockRemoteOpProcessing() {
        lock.unlock();
    }

    public abstract OTSystemDefinition.OTAlgorithmBase<T> getAlgorithmBase();

    public abstract OTSystemDefinition.OTOperationGenerator<T> getOperationGenerator();

    public abstract OTSystemDefinition.OTOperationIntegrator<T> getOperationIntegrator();

    public abstract OTSystemDefinition.OTDataModel<T> getDataModel();
}
