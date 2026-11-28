/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.mcp;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.List;
import org.restlet.Client;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Protocol;
import org.restlet.engine.connector.ClientHelper;
import org.restlet.ext.mcp.internal.McpLegacyEdge;
import org.restlet.ext.mcp.internal.McpStdioChannel;

/**
 * Explicit MCP STDIO connector helper. Its owner supplies an unstarted StreamTransport in the
 * connector context under STREAM_TRANSPORT_ATTRIBUTE and stops the connector after the {@link
 * McpClientAdapter} using it. No executable is inferred from the stdio URI and no system stream is
 * installed by default.
 *
 * <p>The connector frames the stream, one JSON-RPC message per line, for one {@link
 * McpClientAdapter} at a time, which speaks MCP {@link McpProtocol#VERSION}, {@link
 * McpProtocol#MODERN_VERSION} or, with {@link McpProtocol#AUTO}, probes with {@code
 * server/discover} first and falls back to a legacy {@code initialize} on the same stream. The
 * first client to use the connector fixes its message limit. Stopping the connector ends the
 * stream.
 */
public final class StdioClientHelper extends ClientHelper {
    static final String TRANSPORT_ATTRIBUTE = "org.restlet.ext.mcp.clientTransport";
    private McpStdioChannel channel;

    public StdioClientHelper(Client client) {
        super(client);
        getProtocols().add(Protocol.STDIO);
    }

    @Override
    public synchronized void start() {
        Object value = getContext().getAttributes().get(McpProtocol.STREAM_TRANSPORT_ATTRIBUTE);
        if (!(value instanceof org.restlet.util.StreamTransport stream)) {
            throw new IllegalStateException(
                    "MCP STDIO requires an explicitly configured StreamTransport");
        }
        channel = new McpStdioChannel(stream);
        getContext().getAttributes().put(TRANSPORT_ATTRIBUTE, this);
    }

    /**
     * The framing of this connector's stream, which the client adapter using it attaches to.
     *
     * @return The channel.
     */
    synchronized McpStdioChannel channel() {
        if (channel == null) {
            throw new IllegalStateException("Start the MCP STDIO client connector first");
        }
        return channel;
    }

    /**
     * Starts a legacy client on this connector's stream, with an MCP client's limits, offering
     * {@link McpProtocol#VERSION}.
     */
    McpLegacyEdge.Client bind(int maxMessageBytes, int maxCalls, Duration timeout) {
        return McpLegacyEdge.stdio(
                channel(), List.of(McpProtocol.VERSION), maxMessageBytes, maxCalls, timeout);
    }

    @Override
    public synchronized void stop() {
        try {
            if (channel != null) {
                channel.close(Duration.ofSeconds(30));
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            getContext().getAttributes().remove(TRANSPORT_ATTRIBUTE, this);
        }
    }

    @Override
    public void handle(Request request, Response response) {
        throw new UnsupportedOperationException("Use McpClientAdapter on this helper's transport");
    }
}
