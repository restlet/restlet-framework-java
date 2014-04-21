package org.restlet.ext.apispark;

import org.restlet.data.Protocol;

public class Endpoint {

	/**
	 * Protocol used for this endpoint
	 */
	private Protocol protocol;
	
	/**
	 * Address of the host
	 */
	private String host;
	
	/**
	 * Port used for this endpoint
	 */
	private Integer port;
	
	public Protocol getProtocol() {
		return protocol;
	}
	public void setProtocol(Protocol protocol) {
		this.protocol = protocol;
	}
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public Integer getPort() {
		return port;
	}
	public void setPort(Integer port) {
		this.port = port;
	}
}
