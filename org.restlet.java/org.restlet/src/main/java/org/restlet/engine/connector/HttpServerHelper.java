/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.engine.connector;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.restlet.Server;
import org.restlet.data.Protocol;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * Internal HTTP server connector.
 * 
 * @author Jerome Louvel
 */
public class HttpServerHelper extends NetServerHelper {
	/** The underlying HTTP server. */
	private volatile HttpServer server;

	/**
	 * Constructor.
	 * 
	 * @param server The server to help.
	 */
	public HttpServerHelper(Server server) {
		super(server);
		getProtocols().add(Protocol.HTTP);
	}

	@Override
	public void start() throws Exception {
		// Use ephemeral port
		int port = Math.max(getHelped().getPort(), 0);
		if (getHelped().getAddress() != null) {
			// This call may throw UnknownHostException and otherwise always
			// returns an instance of INetAddress.
			// Note: textual representation of inet addresses are supported
			InetAddress iaddr = InetAddress.getByName(getHelped().getAddress());

			// Note: the backlog of 50 is the default
			setAddress(new InetSocketAddress(iaddr, port));
		} else {
			// Listens to any local IP address
			setAddress(new InetSocketAddress(port));
		}

		this.server = HttpServer.create(getAddress(), 0);
		server.createContext("/", new HttpHandler() {
			@Override
			public void handle(HttpExchange httpExchange) throws IOException {
				HttpServerHelper.this.handle(new HttpExchangeCall(getHelped(), httpExchange));
			}
		});
		// creates a default executor
		server.setExecutor(createThreadPool());
		server.start();

		setConfidential(false);
		setEphemeralPort(server.getAddress().getPort());
		super.start();
	}

	@Override
	public synchronized void stop() throws Exception {
		super.stop();
		this.server.stop(0);
	}
}
