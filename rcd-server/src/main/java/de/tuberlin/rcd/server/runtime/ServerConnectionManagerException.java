package de.tuberlin.rcd.server.runtime;

/**
 * ServerConnectionManager specific exceptions.
 * @author Tobias Herb
 *
 */
@SuppressWarnings("serial")
public class ServerConnectionManagerException extends Exception {

	/**
	 * Constructor.
	 * @param msg A error message.
	 */
	public ServerConnectionManagerException(String msg) {
		super( msg ); 
	} 
	
	/**
	 * 
	 * @param msg A error message.
 	 * @param t
	 */
	public ServerConnectionManagerException(String msg, Throwable t) {
		super( msg, t ); 
	} 	
}
