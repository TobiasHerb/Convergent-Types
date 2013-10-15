package de.tuberlin.rcd.server.runtime;

import java.util.UUID;

/**
 * Encapsulates the tests configuration.
 * @author Tobias Herb
 *
 */
public final class ServerConfiguration {

	/**
	 * Constructor.
	 */
	public ServerConfiguration(UUID serverUID, int port, int backlog, String host) {
		this.serverUID = serverUID;
        this.port 	 = port;
		this.backlog = backlog;
		this.host 	 = host;
	}
	
	/** The port number, or <code>0</code> to use any free port. */
	private final int port;
	
	/** The maximum length of the backlog queue. */
	private final int backlog;
	
	/** The host name. */
	private final String host;

    /** Server UID. */
    public final UUID serverUID;

	/**
	 *	Return the port number.
	 */
	public int getPort() {
		return port;
	}
	
	/**
	 * Return the maximum length of the backlog queue.
	 */
	public int getBacklog() {
		return backlog;
	}
	
	/**
	 * Return the host name. 
	 */
	public String getHost() {
		return host;
	}
}
