/**
 * Copyright 2005-2024 Qlik
 * <p>
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * <p>
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.ext.jetty;

import org.eclipse.jetty.alpn.server.ALPNServerConnectionFactory;
import org.eclipse.jetty.http2.server.HTTP2ServerConnectionFactory;
import org.eclipse.jetty.server.AbstractConnectionFactory;
import org.eclipse.jetty.server.ConnectionFactory;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.restlet.Server;
import org.restlet.data.Protocol;
import org.restlet.engine.ssl.DefaultSslContextFactory;
import org.restlet.ext.jetty.internal.RestletSslContextFactoryServer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

/**
 * Jetty HTTPS server connector. Here is the list of additional parameters that
 * are supported. They should be set in the Server's context before it is
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
 * SSL context setting</td>
 * </tr>
 * <tr>
 * <td>http.transport.protocols</td>
 * <td>string</td>
 * <td>http1</td>
 * <td>Coma separated and sorted list of supported protocols. Values: http1,
 * http2, http3.</td>
 * </tr>
 * </table>
 * For the default SSL parameters see the Javadocs of the
 * {@link DefaultSslContextFactory} class.
 *
 * @see <a href=
 *      "https://jetty.org/docs/jetty/12/operations-guide/keystore/index.html">How
 *      to configure SSL for Jetty</a>
 * @author Jerome Louvel
 * @author Tal Liron
 */
public class HttpsServerHelper extends JettyServerHelper {

	/**
	 * Constructor.
	 *
	 * @param server The server to help.
	 */
	public HttpsServerHelper(Server server) {
		super(server);
		getProtocols().add(Protocol.HTTPS);
	}

	@Override
	protected ConnectionFactory[] createConnectionFactories(HttpConfiguration configuration) {
		ConnectionFactory[] result;

		try {
			final List<ConnectionFactory> connectionFactories = new ArrayList<>();

			for (String httpTransportProtocolAsString : getHttpTransportProtocols()) {
				switch (httpTransportProtocolAsString) {
				case "http1":
					connectionFactories.add(new HttpConnectionFactory(configuration));
					break;
				case "http2":
					connectionFactories.add(new ALPNServerConnectionFactory());
					connectionFactories.add(new HTTP2ServerConnectionFactory(configuration));
					break;
				default:
					final String errorMessage = String.format("'%s' is not one of the supported value: [http1, http2]",
							httpTransportProtocolAsString);
					throw new IllegalArgumentException(errorMessage);
				}
			}

			SslContextFactory.Server sslContextFactory = new RestletSslContextFactoryServer(
					org.restlet.engine.ssl.SslUtils.getSslContextFactory(this));

			result = AbstractConnectionFactory.getFactories(sslContextFactory,
					connectionFactories.toArray(new ConnectionFactory[0]));
		} catch (RuntimeException e) {
			getLogger().log(Level.WARNING, "Unable to create the Jetty SSL context factory", e);
			throw e;
		} catch (Exception e) {
			getLogger().log(Level.WARNING, "Unable to create the Jetty SSL context factory", e);
			throw new RuntimeException(e);
		}

		return result;
	}

	/**
	 * Supported HTTP transport protocol. Defaults to http1.
	 *
	 * @return Supported HTTP transport protocol.
	 */
	public List<String> getHttpTransportProtocols() {
		String httpTransportProtocolsAsString = getHelpedParameters().getFirstValue("http.transport.protocols",
				"http1");
		return Arrays.stream(httpTransportProtocolsAsString.split(",")).toList();
	}
}
