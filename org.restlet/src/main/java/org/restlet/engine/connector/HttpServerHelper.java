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

/**
 * Netty HTTP server connector. Here is the list of additional parameters that are supported. See
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
 * <td>http.transport.protocol</td>
 * <td>String</td>
 * <td>HTTP1_1</td>
 * <td>Supported protocol. Values: HTTP1_1 or HTTP2. HTTP/1.1 is always supported. With HTTP2,
 * cleartext HTTP/2 (h2c) is also accepted, through the HTTP/1.1 "Upgrade" mechanism or with prior
 * knowledge.</td>
 * </tr>
 * </table>
 *
 * @author Jerome Louvel
 */
public class HttpServerHelper extends NettyServerHelper {

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
    protected void validateConfiguration() {
        HttpTransportProtocol.fromName(getHttpTransportProtocol());
    }

    @Override
    protected List<String> getApplicationProtocols() {
        return switch (HttpTransportProtocol.fromName(getHttpTransportProtocol())) {
            case HTTP1_1 -> List.of(HTTP_1_1);
            case HTTP2 -> List.of(HTTP_1_1, HTTP_2);
        };
    }

    @Override
    protected boolean isConfidential() {
        return false;
    }

    /**
     * Supported HTTP transport protocol. Defaults to HTTP1_1.
     *
     * @return Supported HTTP transport protocol.
     */
    public String getHttpTransportProtocol() {
        return getHelpedParameters()
                .getFirstValue("http.transport.protocol", HttpTransportProtocol.HTTP1_1.name());
    }

    /** Supported HTTP transport protocols. */
    private enum HttpTransportProtocol {
        HTTP1_1,
        HTTP2;

        static HttpTransportProtocol fromName(final String name) {
            try {
                return HttpTransportProtocol.valueOf(name);
            } catch (final IllegalArgumentException | NullPointerException e) {
                throw new IllegalArgumentException(
                        String.format(
                                "'%s' is not one of the supported values: %s",
                                name, Arrays.toString(HttpTransportProtocol.values())));
            }
        }
    }
}
