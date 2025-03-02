/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.engine.connector;

import com.sun.net.httpserver.*;
import org.restlet.Server;
import org.restlet.data.Protocol;
import org.restlet.engine.ssl.DefaultSslContextFactory;
import org.restlet.engine.ssl.SslContextFactory;
import org.restlet.engine.ssl.SslUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * Internal HTTPS server connector. Here is the list of additional parameters
 * that are supported. They should be set in the Server's context before it is
 * started:
 * <table>
 * <caption>list of supported parameters</caption>
 * <tr>
 * <th>Parameter name</th>
 * <th>Value type</th>
 * <th>Default value</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>sslContextFactory</td>
 * <td>String</td>
 * <td>org.restlet.engine.ssl.DefaultSslContextFactory</td>
 * <td>Let you specify a {@link SslContextFactory} qualified class name as a
 * parameter, or an instance as an attribute for a more complete and flexible
 * SSL context setting.</td>
 * </tr>
 * </table>
 * For the default SSL parameters see the Javadocs of the
 * {@link DefaultSslContextFactory} class.
 * 
 * @author Jerome Louvel
 * @deprecated Will be removed in the next 2.7/3.0 release.
 */
@Deprecated
public class HttpsServerHelper extends NetServerHelper {
	/** The underlying HTTPS server. */
	private volatile HttpsServer server;

	/**
	 * Constructor.
	 * 
	 * @param server The server to help.
	 */
	public HttpsServerHelper(Server server) {
		super(server);
		getProtocols().add(Protocol.HTTPS);
	}

	/** Starts the Restlet. */
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

		// Complete initialization
		this.server = HttpsServer.create(getAddress(), 0);

		// Initialize the SSL context
		SslContextFactory sslContextFactory = SslUtils.getSslContextFactory(this);
		SSLContext sslContext = sslContextFactory.createSslContext();
		final SSLParameters sslParams = sslContext.getDefaultSSLParameters();
		server.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
			public void configure(HttpsParameters params) {
				params.setSSLParameters(sslParams);
			}
		});

		server.createContext("/", new HttpHandler() {
			@Override
			public void handle(HttpExchange httpExchange) throws IOException {
				HttpsServerHelper.this.handle(new HttpExchangeCall(getHelped(), httpExchange, true));
			}
		});
		// creates a default executor
		server.setExecutor(createThreadPool());
		server.start();

		setConfidential(true);
		setEphemeralPort(server.getAddress().getPort());
		super.start();
	}

	@Override
	public synchronized void stop() throws Exception {
		super.stop();
		this.server.stop(0);
	}

}
