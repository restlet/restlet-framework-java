/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.netty;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpRequest;
import java.util.Map;
import java.util.logging.Logger;
import org.restlet.data.Parameter;
import org.restlet.util.Series;

/**
 * Connection handed to a {@link NettyConnectionUpgrade}. It exposes Netty types: extensions using
 * it must require the Netty modules themselves.
 */
@SuppressWarnings("exports")
public final class NettyUpgradeContext {

    private final Channel channel;

    private final HttpRequest request;

    private final boolean confidential;

    private final NettyServerHelper helper;

    NettyUpgradeContext(
            Channel channel, HttpRequest request, boolean confidential, NettyServerHelper helper) {
        this.channel = channel;
        this.request = request;
        this.confidential = confidential;
        this.helper = helper;
    }

    /**
     * Returns the HTTP/1.1 connection. Its pipeline still holds the HTTP server codec and the idle
     * state handler, if any; the Restlet HTTP handlers were removed. Reading is on demand
     * (auto-read is off).
     *
     * @return The connection.
     */
    public Channel getChannel() {
        return channel;
    }

    /**
     * Returns the head of the upgrade request.
     *
     * @return The upgrade request head.
     */
    public HttpRequest getRequest() {
        return request;
    }

    /**
     * Indicates whether the connection is secured with TLS.
     *
     * @return True if the connection is secured with TLS.
     */
    public boolean isConfidential() {
        return confidential;
    }

    /**
     * Returns the parameters of the server connector.
     *
     * @return The parameters of the server connector.
     */
    public Series<Parameter> getParameters() {
        return helper.getHelpedParameters();
    }

    /**
     * Returns the attributes of the server connector, shared by all its connections, for example to
     * keep per-server state.
     *
     * @return The attributes of the server connector.
     */
    public Map<String, Object> getAttributes() {
        return helper.getAttributes();
    }

    /**
     * Returns the connection idle timeout of the server connector.
     *
     * @return The idle timeout in milliseconds, 0 for none.
     */
    public long getIdleTimeout() {
        return helper.getConnectorIdleTimeout();
    }

    /**
     * Returns the logger of the server connector.
     *
     * @return The logger.
     */
    public Logger getLogger() {
        return helper.getLogger();
    }
}
