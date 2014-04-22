package org.restlet.ext.apispark;

import org.restlet.data.Protocol;

public class Endpoint {

	/** The host's name. */
	private String host;

	/** The endpoint's port. */
	private int port;

	/** Protocol used for this endpoint. */
	private Protocol protocol;

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public Protocol getProtocol() {
		return protocol;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setProtocol(Protocol protocol) {
		this.protocol = protocol;
	}
}
