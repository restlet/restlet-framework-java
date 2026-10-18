/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.connector;

import java.util.Arrays;
import java.util.List;
import org.restlet.Server;
import org.restlet.data.Protocol;
import org.restlet.engine.netty.NettyServerHelper;
import org.restlet.engine.ssl.DefaultSslContextFactory;
import org.restlet.engine.ssl.SslContextFactory;

/**
 * Netty HTTPS server connector. Here is the list of additional parameters that are supported. See
 * {@link NettyServerHelper} for the common ones.
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
 * <td>Let you specify a {@link SslContextFactory} qualified class name as a parameter, or an
 * instance as an attribute for a more complete and flexible SSL context setting</td>
 * </tr>
 * <tr>
 * <td>http.transport.protocols</td>
 * <td>String</td>
 * <td>HTTP1_1</td>
 * <td>Comma separated and ordered list of supported protocols. Available values: HTTP1_1, HTTP2
 * (both negotiated with ALPN on TCP) and HTTP3. HTTP3 is <b>experimental</b>: it is served over
 * QUIC on the UDP port having the same number as the TCP port, advertised to clients with the
 * "Alt-Svc" response header, and requires the optional {@code io.netty:netty-codec-http3}
 * dependency with its native QUIC library. It must be combined with HTTP1_1 or HTTP2. See the
 * limits in {@code org.restlet.engine.netty.Http3Support}.</td>
 * </tr>
 * </table>
 *
 * For the default SSL parameters see the Javadocs of the {@link DefaultSslContextFactory} class.
 *
 * @author Jerome Louvel
 */
public class HttpsServerHelper extends NettyServerHelper {

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
    protected void validateConfiguration() {
        getApplicationProtocols();
    }

    @Override
    protected List<String> getApplicationProtocols() {
        return getHttpTransportProtocols().stream()
                .map(HttpTransportProtocol::fromName)
                .map(
                        p ->
                                switch (p) {
                                    case HTTP1_1 -> HTTP_1_1;
                                    case HTTP2 -> HTTP_2;
                                    case HTTP3 -> HTTP_3;
                                })
                .toList();
    }

    @Override
    protected boolean isConfidential() {
        return true;
    }

    /**
     * Supported HTTP transport protocols. Defaults to HTTP1_1.
     *
     * @return Supported HTTP transport protocols.
     */
    public List<String> getHttpTransportProtocols() {
        String value =
                getHelpedParameters()
                        .getFirstValue(
                                "http.transport.protocols", HttpTransportProtocol.HTTP1_1.name());
        return Arrays.stream(value.split(",", -1)).map(String::trim).distinct().toList();
    }

    /** Supported HTTP transport protocols. */
    private enum HttpTransportProtocol {
        HTTP1_1,
        HTTP2,
        HTTP3;

        static HttpTransportProtocol fromName(final String name) {
            try {
                return HttpTransportProtocol.valueOf(name);
            } catch (final IllegalArgumentException | NullPointerException _) {
                throw new IllegalArgumentException(
                        String.format(
                                "'%s' is not one of the supported values: %s",
                                name, Arrays.toString(HttpTransportProtocol.values())));
            }
        }
    }
}
