package de.tuberlin.rcd.network;

import de.tuberlin.rcd.network.Transceiver.TransceiverThreadBase;

/**
 * @author Tobias Herb
 *
 */
public final class TransceiverFactory {

	/**
	 * Constructor.
	 * @param receiverClass
	 * @param senderClass
	 */
	public TransceiverFactory( Class<? extends Transceiver.TransceiverThreadBase> receiverClass,
							   Class<? extends Transceiver.TransceiverThreadBase> senderClass ) {
		// sanity check.
		if( receiverClass == null )
			throw new NullPointerException();
		if( senderClass == null )
			throw new NullPointerException();
		
		this.receiverClass = receiverClass;
		this.senderClass = senderClass;
	}
	
	/**
	 * 
	 */
	private Class<? extends Transceiver.TransceiverThreadBase> receiverClass;
	
	/**
	 * 
	 */
	private Class<? extends Transceiver.TransceiverThreadBase> senderClass;
	
	/**
	 * 
	 * @param connection
	 * @param closingCallback
	 * @return
	 */
	public Transceiver create( Connection connection, Runnable startingCallback, Runnable closingCallback ) {
		// sanity check.
		if( connection == null ) 
			throw new NullPointerException();

		Transceiver transceiver = null;
		try {
			transceiver = new Transceiver( connection, startingCallback, closingCallback );
            TransceiverThreadBase sender = senderClass.getConstructor( Transceiver.class ).
                    newInstance( transceiver );
			TransceiverThreadBase receiver = receiverClass.getConstructor( Transceiver.class ).
                    newInstance( transceiver );
			transceiver.setTransmitter( sender );
			transceiver.setReceiver( receiver );
		} catch( Exception e ) {
			e.printStackTrace();
			return null;
		} 
		
		return transceiver;
	}
}
