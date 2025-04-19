/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
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
 * <td>Supported protocol. Values: HTTP1_1 or HTTP2. The protocol HTTP 1.1 is always supported, the support of HTTP 2 is
 * done thanks to upgrade from HTTP 1.1 protocol.</td>
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
        final String httpTransportProtocolAsString = getHttpTransportProtocol();

        return switch (httpTransportProtocolAsString) {
            case "HTTP1_1" -> new ConnectionFactory[] { new HttpConnectionFactory(configuration) };
            case "HTTP2" -> new ConnectionFactory[] {
                    new HttpConnectionFactory(configuration), // still necessary to support protocol upgrade
                    new HTTP2CServerConnectionFactory(configuration) };
            default -> {
                final String errorMessage = String.format("'%s' is not one of the supported value: [HTTP1_1, HTTP2]",
                        httpTransportProtocolAsString);
                throw new IllegalArgumentException(errorMessage);
            }
        };
    }

    @Override
    protected List<Connector> createConnectors(org.eclipse.jetty.server.Server server) {
        return List.of(createTcpConnector(server));
    }

    /**
     * Supported HTTP transport protocol. Defaults to HTTP1_1.
     *
     * @return Supported HTTP transport protocol.
     */
    public String getHttpTransportProtocol() {
        return getHelpedParameters().getFirstValue("http.transport.protocol", "HTTP1_1");
    }

}
