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
import org.eclipse.jetty.http3.server.HTTP3ServerConnectionFactory;
import org.eclipse.jetty.quic.server.QuicServerConnector;
import org.eclipse.jetty.quic.server.ServerQuicConfiguration;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.restlet.Server;
import org.restlet.data.Protocol;
import org.restlet.engine.ssl.DefaultSslContextFactory;
import org.restlet.ext.jetty.internal.RestletSslContextFactoryServer;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

/**
 * Jetty HTTPS server connector. Here is the list of additional parameters that are supported. They should be set in the
 * Server's context before it is started:
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
 * <td>Let you specify a {@link SslContextFactory} qualified class name as a parameter, or an instance as an attribute
 * for a more complete and flexible SSL context setting</td>
 * </tr>
 * <tr>
 * <td>http.transport.protocols</td>
 * <td>string</td>
 * <td>HTTP1_1</td>
 * <td>Comma separated and sorted list of supported protocols. Available values: HTTP1_1, HTTP2, HTTP3.</td>
 * </tr>
 * <tr>
 * <td>http3.pem.workdir</td>
 * <td>string</td>
 * <td>No default value</td>
 * <td>Directory where are exported trusted certificates, required for HTTP3 support. There is no default value to let you configure a secured enough directory.</td>
 * </tr>
 * </table>
 * For the default SSL parameters see the Javadocs of the {@link DefaultSslContextFactory} class.
 *
 * @see <a href= "https://jetty.org/docs/jetty/12/operations-guide/keystore/index.html">Configure SSL for Jetty</a>
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
    protected ConnectionFactory[] createConnectionFactories(final HttpConfiguration configuration) {
        ConnectionFactory[] result;

        final List<ConnectionFactory> connectionFactories = new ArrayList<>();

        final List<String> httpTransportProtocols = new ArrayList<>(getHttpTransportProtocols());
        httpTransportProtocols.remove("HTTP3");

        for (String httpTransportProtocolAsString : httpTransportProtocols) {
            connectionFactories.addAll(createConnectionFactories(configuration, httpTransportProtocolAsString));
        }

        SslContextFactory.Server sslContextFactory = getServerSslContextFactory();

        result = AbstractConnectionFactory.getFactories(sslContextFactory,
                connectionFactories.toArray(new ConnectionFactory[0]));

        return result;
    }

    @Override
    protected List<Connector> createConnectors(org.eclipse.jetty.server.Server server) {
        final List<Connector> result = new ArrayList<>();

        final List<String> httpTransportProtocols = getHttpTransportProtocols();

        Optional<String> unknownProtocol = httpTransportProtocols.stream()
                .filter(httpTransportProtocol -> List.of("HTTP3", "HTTP2", "HTTP1.1").contains(httpTransportProtocol))
                .findAny();
        if (unknownProtocol.isPresent()) {
            final String errorMessage = String.format(
                    "'%s' is not one of the supported value: [HTTP1_1, HTTP2, HTTP3]", unknownProtocol.get());
            throw new IllegalArgumentException(errorMessage);
        }

        if (httpTransportProtocols.contains("HTTP3")) {
            SslContextFactory.Server sslContextFactory = getServerSslContextFactory();
            ServerQuicConfiguration configuration = new ServerQuicConfiguration(sslContextFactory, Path.of(getHttp3PemWorkDir()));
            configuration.setOutputBufferSize(getHttpOutputBufferSize());

            QuicServerConnector connector = new QuicServerConnector(server, configuration, new HTTP3ServerConnectionFactory(configuration));
            final String address = getHelped().getAddress();
            if (address != null) {
                connector.setHost(address);
            }
            connector.setPort(getHelped().getPort());
            connector.setIdleTimeout(getConnectorIdleTimeout());
            connector.setShutdownIdleTimeout(getShutdownTimeout());

            result.add(connector);
        } else if (httpTransportProtocols.contains("HTTP1_1") || httpTransportProtocols.contains("HTTP2")) {
            result.add(createTcpConnector(server));
        }

        return result;
    }

    /**
     * Creates new internal Jetty connection factories.
     *
     * @param configuration The HTTP configuration.
     * @param protocol The connection factory's protocol name.
     * @return New internal Jetty connection factories.
     */
    private List<ConnectionFactory> createConnectionFactories(final HttpConfiguration configuration, final String protocol) {
        return switch (protocol) {
            case "HTTP1_1" -> List.of(new HttpConnectionFactory(configuration));
            case "HTTP2" -> List.of(new ALPNServerConnectionFactory(), new HTTP2ServerConnectionFactory(configuration));
            default -> {
                final String errorMessage = String.format(
                        "'%s' is not one of the supported value: [HTTP1_1, HTTP2]", protocol);
                throw new IllegalArgumentException(errorMessage);
            }
        };
    };

    /**
     * Supported HTTP transport protocol. Defaults to http1.
     *
     * @return Supported HTTP transport protocol.
     */
    public List<String> getHttpTransportProtocols() {
        String httpTransportProtocolsAsString = getHelpedParameters().getFirstValue("http.transport.protocols",
                "HTTP1_1");
        return Arrays.stream(httpTransportProtocolsAsString.split(","))
                .map(String::trim)
                .toList();
    }

    /**
     * Directory where are extracted the supported certificates.
     * @return Directory where are extracted the supported certificates.
     */
    public String getHttp3PemWorkDir() {
        return getHelpedParameters().getFirstValue("http3.pem.workdir");
    }

    private SslContextFactory.Server getServerSslContextFactory() {
        try {
            return new RestletSslContextFactoryServer(
                    org.restlet.engine.ssl.SslUtils.getSslContextFactory(this));
        } catch (RuntimeException e) {
            getLogger().log(Level.WARNING, "Unable to create the Jetty SSL context factory", e);
            throw e;
        } catch (Exception e) {
            getLogger().log(Level.WARNING, "Unable to create the Jetty SSL context factory", e);
            throw new RuntimeException(e);
        }
    }

}
