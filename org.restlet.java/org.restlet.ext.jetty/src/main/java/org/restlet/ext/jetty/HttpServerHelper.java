/**
 * Copyright 2005-2024 Qlik
 * <p>
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * <p>
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.ext.jetty;

import org.eclipse.jetty.http2.server.HTTP2CServerConnectionFactory;
import org.eclipse.jetty.server.ConnectionFactory;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.restlet.Server;
import org.restlet.data.Protocol;

import java.util.Arrays;
import java.util.List;

/**
 * Jetty HTTP server connector.
 *
 * <table>
 * <caption>list of supported parameters</caption>
 * <tr>
 * <td>http.transport.mode</td>
 * <td>string</td>
 * <td>HTTP1_1</td>
 * <td>Supported protocol. Values: HTTP1_1 or HTTP2. The protocol HTTP 1.1 is always supported,
 * the support of HTTP2 (in clear text mode) is done thanks to upgrade from HTTP 1.1 protocol.</td>
 * </tr>
 * </table>
 *
 * @author Jerome Louvel
 * @author Tal Liron
 */
public class HttpServerHelper extends JettyServerHelper {

    /**
     * Constructor.
     *
     * @param server The server to help.
     */
    public HttpServerHelper(Server server) {
        super(server);
        getProtocols().add(Protocol.HTTP);
    }

    /**
     * Create and configure the Jetty HTTP connector
     *
     * @param configuration The HTTP configuration.
     */
    @Override
    protected ConnectionFactory[] createConnectionFactories(final HttpConfiguration configuration) {
        HttpTransportProtocol httpTransportProtocol = HttpTransportProtocol.fromName(getHttpTransportProtocol());

        return switch (httpTransportProtocol) {
            case HTTP1_1 -> new ConnectionFactory[]{new HttpConnectionFactory(configuration)};
            case HTTP2 -> new ConnectionFactory[]{
                    new HttpConnectionFactory(configuration), // still necessary to support protocol upgrade
                    new HTTP2CServerConnectionFactory(configuration)};
        };
    }

    @Override
    protected List<Connector> createConnectors(org.eclipse.jetty.server.Server server) {
        return List.of(createServerConnector(server, createHttpConfiguration()));
    }

    /**
     * Supported HTTP transport protocol. Defaults to HTTP1_1.
     *
     * @return Supported HTTP transport protocol.
     */
    public String getHttpTransportProtocol() {
        return getHelpedParameters().getFirstValue("http.transport.protocol", HttpTransportProtocol.HTTP1_1.name());
    }

    /**
     * Supported HTTP transport protocols.
     */
    private enum HttpTransportProtocol {
        HTTP1_1, HTTP2;

        static HttpTransportProtocol fromName(final String name) {
            try {
                return HttpTransportProtocol.valueOf(name);
            } catch (final IllegalArgumentException iae) {
                String supportedHttpTransportProtocols = Arrays.toString(HttpTransportProtocol.values());

                final String errorMessage = String.format("'%s' is not one of the supported values: %s",
                        name, supportedHttpTransportProtocols);

                throw new IllegalArgumentException(errorMessage);
            }
        }
    }

}
