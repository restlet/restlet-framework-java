/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.mcp;

import java.time.Duration;
import org.restlet.Client;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Protocol;
import org.restlet.engine.connector.ClientHelper;
import org.restlet.ext.mcp.internal.McpLegacyEdge;

/**
 * Explicit MCP STDIO connector helper. Its owner supplies an unstarted StreamTransport in the
 * connector context under STREAM_TRANSPORT_ATTRIBUTE and stops the connector after the {@link
 * McpClientAdapter} using it. No executable is inferred from the stdio URI and no system stream is
 * installed by default. The connector speaks MCP {@link McpProtocol#VERSION}, the modern client
 * being HTTP only for now.
 */
public final class StdioClientHelper extends ClientHelper {
    static final String TRANSPORT_ATTRIBUTE = "org.restlet.ext.mcp.clientTransport";
    private McpLegacyEdge.StdioTransport transport;

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
        transport = McpLegacyEdge.stdioTransport(stream);
        getContext().getAttributes().put(TRANSPORT_ATTRIBUTE, this);
    }

    /**
     * Starts a client on this connector's transport, with an MCP client's limits. One client may
     * bind it.
     */
    synchronized McpLegacyEdge.Client bind(int maxMessageBytes, int maxCalls, Duration timeout) {
        if (transport == null) {
            throw new IllegalStateException("Start the MCP STDIO client connector first");
        }
        return McpLegacyEdge.stdio(transport, maxMessageBytes, maxCalls, timeout);
    }

    @Override
    public synchronized void stop() {
        try {
            if (transport != null) {
                transport.close(Duration.ofSeconds(30));
            }
        } finally {
            getContext().getAttributes().remove(TRANSPORT_ATTRIBUTE, this);
        }
    }

    @Override
    public void handle(Request request, Response response) {
        throw new UnsupportedOperationException("Use McpClientAdapter on this helper's transport");
    }
}
