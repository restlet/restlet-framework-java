/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.data;

/**
 * Server specific data related to a call.
 * 
 * @author Jerome Louvel
 */
public final class ServerInfo {
	/** Indicates if the server accepts range requests for a resource. */
	private volatile boolean acceptingRanges;

	/** The IP address. */
	private volatile String address;

	/** The agent name. */
	private volatile String agent;

	/** The port number. */
	private volatile int port;

	/**
	 * Constructor.
	 */
	public ServerInfo() {
		this.address = null;
		this.agent = null;
		this.port = -1;
		this.acceptingRanges = false;
	}

	/**
	 * Returns the IP address.
	 * 
	 * @return The IP address.
	 */
	public String getAddress() {
		return this.address;
	}

	/**
	 * Returns the agent name (ex: "Restlet-Framework/2.0"). Note that when used
	 * with HTTP connectors, this property maps to the "Server" header.
	 * 
	 * @return The agent name.
	 */
	public String getAgent() {
		return this.agent;
	}

	/**
	 * Returns the port number which received the call. If no port is specified, -1
	 * is returned.
	 * 
	 * @return The port number which received the call.
	 */
	public int getPort() {
		return this.port;
	}

	/**
	 * Return true if the server accepts range requests for a resource, with the
	 * "byte" range unit. Note that when used with HTTP connectors, this property
	 * maps to the "Accept-Ranges" header.
	 * 
	 * @return True if the server accepts range requests for a resource.
	 */
	public boolean isAcceptingRanges() {
		return acceptingRanges;
	}

	/**
	 * Indicates if the server accepts range requests for a resource, with the
	 * "byte" range unit. Note that when used with HTTP connectors, this property
	 * maps to the "Accept-Ranges" header.
	 * 
	 * @param acceptingRanges True if the server accepts range requests for a
	 *                        resource.
	 */
	public void setAcceptingRanges(boolean acceptingRanges) {
		this.acceptingRanges = acceptingRanges;
	}

	/**
	 * Sets the IP address which received the call.
	 * 
	 * @param address The IP address which received the call.
	 */
	public void setAddress(String address) {
		this.address = address;
	}

	/**
	 * Sets the agent name (ex: "Restlet-Framework/2.0"). Note that when used with
	 * HTTP connectors, this property maps to the "Server" header.
	 * 
	 * @param agent The agent name.
	 */
	public void setAgent(String agent) {
		this.agent = agent;
	}

	/**
	 * Sets the port number which received the call.
	 * 
	 * @param port The port number which received the call.
	 */
	public void setPort(int port) {
		this.port = port;
	}

}
