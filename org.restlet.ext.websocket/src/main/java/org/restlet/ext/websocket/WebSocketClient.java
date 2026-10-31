/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.websocket;

import io.netty.channel.EventLoopGroup;
import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;
import javax.net.ssl.SSLContext;
import org.restlet.Client;
import org.restlet.engine.netty.NettyClientHelper;
import org.restlet.ext.websocket.internal.NettyWebSocketConnector;

/**
 * Opens WebSocket client sessions ("ws" and "wss" URIs) over HTTP/1.1 upgrades. Each session uses
 * its own connection.
 *
 * <p>Configure the client before starting it. {@link #connect(String)} starts a stopped client,
 * like {@link org.restlet.Restlet#handle(org.restlet.Request)}. Stop or close the client to close
 * its sessions; it can be started again afterwards.
 *
 * <p>By default, the client creates its own I/O threads, released when it stops. To share the I/O
 * threads of the application instead, set a borrowed HTTP {@link Client} with {@link
 * #setClient(Client)}.
 */
public final class WebSocketClient implements AutoCloseable {

    private SSLContext sslContext;

    private Duration connectTimeout = Duration.ofSeconds(15);

    private Duration idleTimeout = Duration.ofSeconds(30);

    private int maxMessageSize = 65536;

    private int maxFrameSize;

    private boolean compression = true;

    private boolean hostnameVerification = true;

    private Client client;

    private NettyWebSocketConnector connector;

    /** Constructor using the default settings. */
    public WebSocketClient() {}

    /**
     * Returns the SSL context used for "wss" URIs.
     *
     * @return The SSL context, or null for the JVM default context.
     */
    public synchronized SSLContext getSslContext() {
        return sslContext;
    }

    /**
     * Sets the SSL context used for "wss" URIs. Defaults to the JVM default context.
     *
     * @param sslContext The SSL context.
     */
    public synchronized void setSslContext(SSLContext sslContext) {
        requireStopped();
        this.sslContext = sslContext;
    }

    /**
     * Returns the connect and handshake timeout.
     *
     * @return The timeout; zero for none.
     */
    public synchronized Duration getConnectTimeout() {
        return connectTimeout;
    }

    /**
     * Sets the connect and handshake timeout. Defaults to 15 seconds; zero for none.
     *
     * @param connectTimeout The timeout.
     */
    public synchronized void setConnectTimeout(Duration connectTimeout) {
        requireStopped();
        this.connectTimeout = requireNonNegative(connectTimeout, "connectTimeout");
    }

    /**
     * Returns the idle time after which the server is pinged.
     *
     * @return The idle timeout; zero when disabled.
     */
    public synchronized Duration getIdleTimeout() {
        return idleTimeout;
    }

    /**
     * Sets the idle time after which the server is pinged, the connection being closed if it stays
     * idle as long again. Defaults to 30 seconds; zero to disable.
     *
     * @param idleTimeout The idle timeout.
     */
    public synchronized void setIdleTimeout(Duration idleTimeout) {
        requireStopped();
        this.idleTimeout = requireNonNegative(idleTimeout, "idleTimeout");
    }

    /**
     * Returns the maximum size in bytes of a received message.
     *
     * @return The maximum message size.
     */
    public synchronized int getMaxMessageSize() {
        return maxMessageSize;
    }

    /**
     * Sets the maximum size in bytes of a received message. Defaults to 65536.
     *
     * @param maxMessageSize The maximum message size.
     */
    public synchronized void setMaxMessageSize(int maxMessageSize) {
        requireStopped();
        if (maxMessageSize <= 0) {
            throw new IllegalArgumentException("maxMessageSize must be positive");
        }
        this.maxMessageSize = maxMessageSize;
    }

    /**
     * Returns the maximum size in bytes of a received frame.
     *
     * @return The maximum frame size, the maximum message size when not set.
     */
    public synchronized int getMaxFrameSize() {
        return (maxFrameSize > 0) ? maxFrameSize : maxMessageSize;
    }

    /**
     * Sets the maximum size in bytes of a received frame. A message may be fragmented in several
     * frames. Defaults to the maximum message size, and is capped by it.
     *
     * @param maxFrameSize The maximum frame size.
     */
    public synchronized void setMaxFrameSize(int maxFrameSize) {
        requireStopped();
        if (maxFrameSize <= 0) {
            throw new IllegalArgumentException("maxFrameSize must be positive");
        }
        this.maxFrameSize = maxFrameSize;
    }

    /**
     * Indicates whether "permessage-deflate" compression (RFC 7692) is offered to the server.
     *
     * @return True if compression is offered.
     */
    public synchronized boolean isCompression() {
        return compression;
    }

    /**
     * Sets whether "permessage-deflate" compression (RFC 7692) is offered to the server. Defaults
     * to true; it is used only if the server accepts it.
     *
     * @param compression True to offer compression.
     */
    public synchronized void setCompression(boolean compression) {
        requireStopped();
        this.compression = compression;
    }

    /**
     * Indicates whether the server host name is verified for "wss" URIs.
     *
     * @return True if the host name is verified.
     */
    public synchronized boolean isHostnameVerification() {
        return hostnameVerification;
    }

    /**
     * Sets whether the server host name is verified for "wss" URIs. Defaults to true.
     *
     * @param hostnameVerification True to verify the host name.
     */
    public synchronized void setHostnameVerification(boolean hostnameVerification) {
        requireStopped();
        this.hostnameVerification = hostnameVerification;
    }

    /**
     * Returns the borrowed HTTP client whose I/O threads are shared.
     *
     * @return The borrowed client, or null if the WebSocket client creates its own I/O threads.
     */
    public synchronized Client getClient() {
        return client;
    }

    /**
     * Sets a borrowed HTTP client whose I/O threads are shared, instead of creating new ones. The
     * client must use the Netty HTTP connector of the core, have a context, and be started before
     * this WebSocket client. Its owner stops it, after this WebSocket client: stopping it closes
     * the sessions. Only the I/O threads are shared; the other settings, including TLS, are those
     * of this WebSocket client.
     *
     * @param client The borrowed client, or null to create new I/O threads.
     */
    public synchronized void setClient(Client client) {
        requireStopped();
        this.client = client;
    }

    /**
     * Starts the client, creating its I/O threads unless a borrowed client is set. Does nothing if
     * already started.
     *
     * @throws IOException If the client cannot be started.
     * @throws IllegalStateException If the borrowed client is not a started Netty HTTP client.
     */
    public synchronized void start() throws IOException {
        if (connector == null) {
            connector =
                    new NettyWebSocketConnector(
                            borrowedEventLoopGroup(),
                            sslContext,
                            connectTimeout.toMillis(),
                            idleTimeout.toMillis(),
                            maxMessageSize,
                            getMaxFrameSize(),
                            compression,
                            hostnameVerification,
                            Logger.getLogger(WebSocketClient.class.getName()));
        }
    }

    /**
     * Indicates whether the client is started.
     *
     * @return True if the client is started.
     */
    public synchronized boolean isStarted() {
        return connector != null;
    }

    /**
     * Stops the client, closing its sessions and releasing its own I/O threads, if any. Does
     * nothing if already stopped.
     */
    public synchronized void stop() {
        if (connector != null) {
            connector.close();
            connector = null;
        }
    }

    /**
     * Opens a session, blocking until the handshake completes. Starts the client if needed.
     *
     * @param uri The "ws" or "wss" URI.
     * @return The open session.
     * @throws IOException If the connection or the handshake fails.
     */
    public WebSocketSession connect(String uri) throws IOException {
        return connect(URI.create(uri), List.of(), Map.of());
    }

    /**
     * Opens a session, blocking until the handshake completes. Starts the client if needed.
     *
     * @param uri The "ws" or "wss" URI.
     * @param subprotocols The subprotocols to offer, in preference order.
     * @param headers Additional handshake request headers, for example "Authorization".
     * @return The open session.
     * @throws IOException If the connection or the handshake fails.
     */
    public WebSocketSession connect(URI uri, List<String> subprotocols, Map<String, String> headers)
            throws IOException {
        NettyWebSocketConnector current;
        synchronized (this) {
            start();
            current = connector;
        }
        return current.connect(
                Objects.requireNonNull(uri, "uri"),
                List.copyOf(subprotocols),
                new LinkedHashMap<>(headers));
    }

    /** Stops the client. */
    @Override
    public void close() {
        stop();
    }

    /**
     * Returns the event loop group of the borrowed client.
     *
     * @return The event loop group, or null if no client is borrowed.
     */
    private EventLoopGroup borrowedEventLoopGroup() {
        if (client == null) {
            return null;
        }
        Object helper =
                (client.getContext() == null)
                        ? null
                        : client.getContext().getAttributes().get("org.restlet.engine.helper");
        if (!(helper instanceof NettyClientHelper nettyHelper)) {
            throw new IllegalStateException(
                    "The borrowed client must have a context and use the Netty HTTP connector");
        }
        EventLoopGroup group = nettyHelper.getEventLoopGroup();
        if (group == null || group.isShuttingDown()) {
            throw new IllegalStateException(
                    "Start the borrowed client before the WebSocket client");
        }
        return group;
    }

    private void requireStopped() {
        if (connector != null) {
            throw new IllegalStateException(
                    "Stop the WebSocket client before changing its settings");
        }
    }

    private static Duration requireNonNegative(Duration value, String name) {
        Objects.requireNonNull(value, name);
        if (value.isNegative()) {
            throw new IllegalArgumentException(name + " cannot be negative");
        }
        return value;
    }
}
