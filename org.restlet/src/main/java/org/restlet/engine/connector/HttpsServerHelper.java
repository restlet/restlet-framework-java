/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.connector;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import org.eclipse.jetty.alpn.server.ALPNServerConnectionFactory;
import org.eclipse.jetty.http2.server.HTTP2ServerConnectionFactory;
import org.eclipse.jetty.http3.server.HTTP3ServerConnectionFactory;
import org.eclipse.jetty.quic.server.QuicServerConnector;
import org.eclipse.jetty.quic.server.ServerQuicConfiguration;
import org.eclipse.jetty.server.AbstractConnectionFactory;
import org.eclipse.jetty.server.ConnectionFactory;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.restlet.Server;
import org.restlet.data.Protocol;
import org.restlet.engine.security.RestletSslContextFactoryServer;
import org.restlet.engine.ssl.DefaultSslContextFactory;
import org.restlet.engine.ssl.SslUtils;

/**
 * Jetty HTTPS server connector. Here is the list of additional parameters that are supported. They
 * should be set in the Server's context before it is started:
 *
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
 * <td>Directory where are exported trusted certificates, required for HTTP3 support. There is no default value to let
 * you configure a secured enough directory.</td>
 * </tr>
 * </table>
 *
 * For the default SSL parameters see the Javadocs of the {@link DefaultSslContextFactory} class.
 *
 * @see <a href= "https://jetty.org/docs/jetty/12/operations-guide/keystore/index.html">Configure
 *     SSL for Jetty</a>
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
    protected List<Connector> createConnectors(org.eclipse.jetty.server.Server server) {
        final List<Connector> result = new ArrayList<>();

        final List<HttpTransportProtocol> httpTransportProtocols =
                getHttpTransportProtocols().stream().map(HttpTransportProtocol::fromName).toList();

        if (httpTransportProtocols.stream().anyMatch(HttpTransportProtocol::isTcpProtocol)) {
            HttpConfiguration configuration = createHttpConfiguration();
            ServerConnector connector = createServerConnector(server, configuration);
            result.add(connector);
        } else if (httpTransportProtocols.contains(HttpTransportProtocol.HTTP3)) {
            ServerQuicConfiguration configuration =
                    createQuicConfiguration(getQuicServerSslContextFactory());
            QuicServerConnector connector = createQuicServerConnector(server, configuration);
            result.add(connector);
        }

        return result;
    }

    @Override
    protected ConnectionFactory[] createConnectionFactories(final HttpConfiguration configuration) {
        final List<ConnectionFactory> connectionFactories = new ArrayList<>();

        final List<HttpTransportProtocol> tcpBasedTransportProtocols =
                getHttpTransportProtocols().stream()
                        .map(HttpTransportProtocol::fromName)
                        .filter(HttpTransportProtocol::isTcpProtocol)
                        .toList();

        for (HttpTransportProtocol tcpBasedTransportProtocol : tcpBasedTransportProtocols) {
            final List<ConnectionFactory> protocolConnectionFactories =
                    switch (tcpBasedTransportProtocol) {
                        case HTTP1_1 -> List.of(new HttpConnectionFactory(configuration));
                        case HTTP2 ->
                                List.of(
                                        new ALPNServerConnectionFactory(),
                                        new HTTP2ServerConnectionFactory(configuration));
                        default -> {
                            String supportedHttpTransportProtocols =
                                    tcpBasedTransportProtocols.toString();
                            final String errorMessage =
                                    String.format(
                                            "'%s' is not one of the supported values: %s",
                                            tcpBasedTransportProtocol,
                                            supportedHttpTransportProtocols);
                            throw new IllegalArgumentException(errorMessage);
                        }
                    };
            connectionFactories.addAll(protocolConnectionFactories);
        }

        SslContextFactory.Server sslContextFactory = getServerSslContextFactory();

        return AbstractConnectionFactory.getFactories(
                sslContextFactory, connectionFactories.toArray(new ConnectionFactory[0]));
    }

    private QuicServerConnector createQuicServerConnector(
            org.eclipse.jetty.server.Server server, ServerQuicConfiguration configuration) {
        QuicServerConnector connector =
                new QuicServerConnector(
                        server, configuration, new HTTP3ServerConnectionFactory(configuration));
        final String address = getHelped().getAddress();
        if (address != null) {
            connector.setHost(address);
        }
        connector.setPort(getHelped().getPort());
        connector.setIdleTimeout(getConnectorIdleTimeout());
        connector.setShutdownIdleTimeout(getShutdownTimeout());
        return connector;
    }

    /**
     * Supported HTTP transport protocols. Defaults to HTTP1_1.
     *
     * @return Supported HTTP transport protocols.
     */
    public List<String> getHttpTransportProtocols() {
        String httpTransportProtocolsAsString =
                getHelpedParameters()
                        .getFirstValue(
                                "http.transport.protocols", HttpTransportProtocol.HTTP1_1.name());
        return Arrays.stream(httpTransportProtocolsAsString.split(","))
                .map(String::trim)
                .distinct()
                .toList();
    }

    /**
     * Directory where are extracted the supported certificates.
     *
     * @return Directory where are extracted the supported certificates.
     */
    public String getHttp3PemWorkDir() {
        return getHelpedParameters().getFirstValue("http3.pem.workdir");
    }

    private Path getHttp3PemWorkDirectoryPath() {
        return Optional.ofNullable(getHttp3PemWorkDir()).map(Path::of).orElse(null);
    }

    private SslContextFactory.Server getServerSslContextFactory() {
        try {
            return new RestletSslContextFactoryServer(SslUtils.getSslContextFactory(this));
        } catch (Exception e) {
            getLogger().log(Level.WARNING, "Unable to create the Jetty SSL context factory", e);
            throw new IllegalArgumentException("Unable to create the Jetty SSL context factory", e);
        }
    }

    private SslContextFactory.Server getQuicServerSslContextFactory() {
        SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();

        sslContextFactory.setKeyStorePassword(
                getHelpedParameters()
                        .getFirstValue(
                                "keyStorePassword",
                                true,
                                System.getProperty("javax.net.ssl.keyStorePassword", "")));
        sslContextFactory.setKeyStorePath(
                getHelpedParameters()
                        .getFirstValue(
                                "keyStorePath",
                                true,
                                System.getProperty("javax.net.ssl.keyStore")));
        sslContextFactory.setKeyStoreType(
                getHelpedParameters()
                        .getFirstValue(
                                "keyStoreType",
                                true,
                                System.getProperty("javax.net.ssl.keyStoreType")));
        sslContextFactory.setProtocol(getHelpedParameters().getFirstValue("protocol", true, "TLS"));
        sslContextFactory.setSecureRandomAlgorithm(
                getHelpedParameters().getFirstValue("secureRandomAlgorithm", true));
        sslContextFactory.setTrustStorePassword(
                getHelpedParameters()
                        .getFirstValue(
                                "trustStorePassword",
                                true,
                                System.getProperty("javax.net.ssl.trustStorePassword")));
        sslContextFactory.setTrustStorePath(
                getHelpedParameters()
                        .getFirstValue(
                                "trustStorePath",
                                true,
                                System.getProperty("javax.net.ssl.trustStore")));
        sslContextFactory.setTrustStoreType(
                getHelpedParameters()
                        .getFirstValue(
                                "trustStoreType",
                                true,
                                System.getProperty("javax.net.ssl.trustStoreType")));

        return sslContextFactory;
    }

    private ServerQuicConfiguration createQuicConfiguration(
            SslContextFactory.Server sslContextFactory) {
        Path pemWorkDirectory = getHttp3PemWorkDirectoryPath();
        ServerQuicConfiguration configuration =
                new ServerQuicConfiguration(sslContextFactory, pemWorkDirectory);
        configuration.setOutputBufferSize(getHttpOutputBufferSize());
        return configuration;
    }

    /** Supported HTTP transport protocols. */
    private enum HttpTransportProtocol {
        HTTP1_1(true),
        HTTP2(true),
        HTTP3(false);

        private final boolean tcpProtocol;

        static HttpTransportProtocol fromName(final String name) {
            try {
                return HttpTransportProtocol.valueOf(name);
            } catch (final IllegalArgumentException iae) {
                String supportedHttpTransportProtocols =
                        Arrays.toString(HttpTransportProtocol.values());

                final String errorMessage =
                        String.format(
                                "'%s' is not one of the supported values: %s",
                                name, supportedHttpTransportProtocols);

                throw new IllegalArgumentException(errorMessage);
            }
        }

        HttpTransportProtocol(boolean tcpProtocol) {
            this.tcpProtocol = tcpProtocol;
        }

        public boolean isTcpProtocol() {
            return tcpProtocol;
        }
    }
}
