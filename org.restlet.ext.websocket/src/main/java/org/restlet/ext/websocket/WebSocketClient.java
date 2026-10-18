/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.websocket;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;
import javax.net.ssl.SSLContext;
import org.restlet.ext.websocket.internal.NettyWebSocketConnector;

/**
 * Opens WebSocket client sessions ("ws" and "wss" URIs) over HTTP/1.1 upgrades. Each session uses
 * its own connection. Close the client to release its I/O threads.
 */
public final class WebSocketClient implements AutoCloseable {

    private final NettyWebSocketConnector connector;

    private WebSocketClient(Builder builder) throws IOException {
        this.connector =
                new NettyWebSocketConnector(
                        builder.sslContext,
                        builder.connectTimeout.toMillis(),
                        builder.idleTimeout.toMillis(),
                        builder.maxMessageSize,
                        (builder.maxFrameSize > 0) ? builder.maxFrameSize : builder.maxMessageSize,
                        builder.compression,
                        builder.hostnameVerification,
                        Logger.getLogger(WebSocketClient.class.getName()));
    }

    /**
     * Creates a client with the default settings.
     *
     * @return The client.
     * @throws IOException If the client cannot be created.
     */
    public static WebSocketClient create() throws IOException {
        return builder().build();
    }

    /**
     * Returns a builder.
     *
     * @return The builder.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Opens a session, blocking until the handshake completes.
     *
     * @param uri The "ws" or "wss" URI.
     * @return The open session.
     * @throws IOException If the connection or the handshake fails.
     */
    public WebSocketSession connect(String uri) throws IOException {
        return connect(URI.create(uri), List.of(), Map.of());
    }

    /**
     * Opens a session, blocking until the handshake completes.
     *
     * @param uri The "ws" or "wss" URI.
     * @param subprotocols The subprotocols to offer, in preference order.
     * @param headers Additional handshake request headers, for example "Authorization".
     * @return The open session.
     * @throws IOException If the connection or the handshake fails.
     */
    public WebSocketSession connect(URI uri, List<String> subprotocols, Map<String, String> headers)
            throws IOException {
        return connector.connect(
                Objects.requireNonNull(uri, "uri"),
                List.copyOf(subprotocols),
                new LinkedHashMap<>(headers));
    }

    @Override
    public void close() {
        connector.close();
    }

    /** Builds a {@link WebSocketClient}. */
    public static final class Builder {

        private SSLContext sslContext;

        private Duration connectTimeout = Duration.ofSeconds(15);

        private Duration idleTimeout = Duration.ofSeconds(30);

        private int maxMessageSize = 65536;

        private int maxFrameSize;

        private boolean compression = true;

        private boolean hostnameVerification = true;

        private Builder() {}

        /**
         * Sets the SSL context used for "wss" URIs. Defaults to the JVM default context.
         *
         * @param sslContext The SSL context.
         * @return This builder.
         */
        public Builder sslContext(SSLContext sslContext) {
            this.sslContext = sslContext;
            return this;
        }

        /**
         * Sets the connect and handshake timeout. Defaults to 15 seconds; zero for none.
         *
         * @param connectTimeout The timeout.
         * @return This builder.
         */
        public Builder connectTimeout(Duration connectTimeout) {
            this.connectTimeout = requireNonNegative(connectTimeout, "connectTimeout");
            return this;
        }

        /**
         * Sets the idle time after which the server is pinged, the connection being closed if it
         * stays idle as long again. Defaults to 30 seconds; zero to disable.
         *
         * @param idleTimeout The idle timeout.
         * @return This builder.
         */
        public Builder idleTimeout(Duration idleTimeout) {
            this.idleTimeout = requireNonNegative(idleTimeout, "idleTimeout");
            return this;
        }

        /**
         * Sets the maximum size in bytes of a received message. Defaults to 65536.
         *
         * @param maxMessageSize The maximum message size.
         * @return This builder.
         */
        public Builder maxMessageSize(int maxMessageSize) {
            if (maxMessageSize <= 0) {
                throw new IllegalArgumentException("maxMessageSize must be positive");
            }
            this.maxMessageSize = maxMessageSize;
            return this;
        }

        /**
         * Sets the maximum size in bytes of a received frame. A message may be fragmented in
         * several frames. Defaults to the maximum message size, and is capped by it.
         *
         * @param maxFrameSize The maximum frame size.
         * @return This builder.
         */
        public Builder maxFrameSize(int maxFrameSize) {
            if (maxFrameSize <= 0) {
                throw new IllegalArgumentException("maxFrameSize must be positive");
            }
            this.maxFrameSize = maxFrameSize;
            return this;
        }

        /**
         * Sets whether "permessage-deflate" compression (RFC 7692) is offered to the server.
         * Defaults to true; it is used only if the server accepts it.
         *
         * @param compression True to offer compression.
         * @return This builder.
         */
        public Builder compression(boolean compression) {
            this.compression = compression;
            return this;
        }

        /**
         * Sets whether the server host name is verified for "wss" URIs. Defaults to true.
         *
         * @param hostnameVerification True to verify the host name.
         * @return This builder.
         */
        public Builder hostnameVerification(boolean hostnameVerification) {
            this.hostnameVerification = hostnameVerification;
            return this;
        }

        /**
         * Builds the client.
         *
         * @return The client.
         * @throws IOException If the client cannot be created.
         */
        public WebSocketClient build() throws IOException {
            return new WebSocketClient(this);
        }

        private static Duration requireNonNegative(Duration value, String name) {
            Objects.requireNonNull(value, name);
            if (value.isNegative()) {
                throw new IllegalArgumentException(name + " cannot be negative");
            }
            return value;
        }
    }
}
