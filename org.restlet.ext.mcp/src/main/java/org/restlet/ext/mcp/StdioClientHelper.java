/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.mcp;

import io.modelcontextprotocol.spec.McpClientTransport;
import java.time.Duration;
import org.restlet.Client;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Protocol;
import org.restlet.engine.connector.ClientHelper;
import org.restlet.ext.mcp.internal.StdioMcpClientTransport;

/**
 * Explicit MCP STDIO connector helper. Its owner supplies an unstarted StreamTransport in the
 * connector context under STREAM_TRANSPORT_ATTRIBUTE and stops the connector after its SDK client.
 * No executable is inferred from the stdio URI and no system stream is installed by default.
 */
public final class StdioClientHelper extends ClientHelper {
    static final String TRANSPORT_ATTRIBUTE = "org.restlet.ext.mcp.clientTransport";
    private StdioMcpClientTransport transport;

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
        transport =
                new StdioMcpClientTransport(
                        stream,
                        McpProtocol.DEFAULT_MAX_MESSAGE_BYTES,
                        McpProtocol.DEFAULT_MAX_CONCURRENT_CALLS,
                        Duration.ofSeconds(30));
        getContext().getAttributes().put(TRANSPORT_ATTRIBUTE, this);
    }

    /** Applies an MCP client's limits to this connector's transport before it connects. */
    synchronized McpClientTransport bind(int maxMessageBytes, int maxCalls, Duration timeout) {
        StdioMcpClientTransport bound = (StdioMcpClientTransport) getTransport();
        bound.setLimits(maxMessageBytes, maxCalls, timeout);
        return bound;
    }

    /** Returns the native SDK transport owned by this connector. One SDK client may bind it. */
    public synchronized McpClientTransport getTransport() {
        if (transport == null) {
            throw new IllegalStateException("Start the MCP STDIO client connector first");
        }
        return transport;
    }

    @Override
    public synchronized void stop() {
        try {
            if (transport != null) {
                transport.closeGracefully().block(Duration.ofSeconds(30));
            }
        } finally {
            getContext().getAttributes().remove(TRANSPORT_ATTRIBUTE, this);
        }
    }

    @Override
    public void handle(Request request, Response response) {
        throw new UnsupportedOperationException(
                "Use McpClientAdapter or the native MCP SDK on this helper's transport");
    }
}
