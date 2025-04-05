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
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.restlet.Server;
import org.restlet.data.Protocol;

/**
 * Jetty HTTP server connector.
 *
 * <table>
 * <caption>list of supported parameters</caption>
 * <tr>
 * <td>http.transport.mode</td>
 * <td>string</td>
 * <td>http1</td>
 * <td>Supported protocol. Values: http1 or http2. The protocol HTTP 1.1 is always supported, the support of HTTP 2 is done thanks to upgrade from HTTP 1.1 protocol.</td>
 * </tr>
 * </table>
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
    protected ConnectionFactory[] createConnectionFactories(
            final HttpConfiguration configuration) {
        final ConnectionFactory[] result;

        final String httpTransportProtocolAsString = getHttpTransportProtocol();
        result = switch (httpTransportProtocolAsString) {
            case "http1" -> new ConnectionFactory[] { new HttpConnectionFactory(configuration) };
            case "http2" -> new ConnectionFactory[] {
                                new HttpConnectionFactory(configuration), // still necessary to support protocol upgrade
                                new HTTP2CServerConnectionFactory(configuration)
                            };
            default -> {
                final String errorMessage = String.format("'%s' is not one of the supported value: [http1, http2]", httpTransportProtocolAsString);
                throw new IllegalArgumentException(errorMessage);
            }
        };

        return result;
    }

    /**
     * Supported HTTP transport protocol. Defaults to http1.
     *
     * @return Supported HTTP transport protocol.
     */
    public String getHttpTransportProtocol() {
        return getHelpedParameters().getFirstValue("http.transport.protocol", "http1");
    }

}
