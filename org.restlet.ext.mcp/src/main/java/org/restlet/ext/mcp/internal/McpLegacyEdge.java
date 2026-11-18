/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.mcp.internal;

import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.modern.JsonRpc;
import io.modelcontextprotocol.modern.McpSchema;
import java.time.Duration;
import java.util.List;
import java.util.function.Consumer;
import org.restlet.Request;
import org.restlet.data.Status;
import org.restlet.ext.mcp.McpRequestContext;
import org.restlet.ext.mcp.internal.legacy.McpLegacyClient;
import org.restlet.ext.mcp.internal.legacy.McpLegacyServer;
import org.restlet.ext.mcp.internal.legacy.StdioMcpClientTransport;
import org.restlet.util.StreamTransport;
import reactor.core.publisher.Mono;

/**
 * The one seam between the modern core of the MCP extension and the legacy edge in {@code
 * internal.legacy}, which translates MCP {@code 2025-11-25} and earlier into the {@code 2026-07-28}
 * model and back. Nothing else references the legacy package, so dropping legacy support means
 * deleting that package and this interface.
 *
 * <p>The edge exposes nothing 2026-07-28 deprecates or removes: no logging, sampling or roots, no
 * HTTP+SSE transport, no listening {@code GET} stream or resumption, and no server-issued session.
 * A client may only send a legacy server's session id when its caller opts in.
 */
public interface McpLegacyEdge {

    /** The modern core a legacy server edge translates requests into. */
    @FunctionalInterface
    interface Core {

        /**
         * Runs a {@code 2026-07-28} request through the interceptors and the modern server, keeping
         * only its final response.
         *
         * @param request The request, with the {@code _meta} the revision requires.
         * @param context The request context, naming the legacy revision.
         * @return The final response.
         */
        Mono<JsonRpc.JSONRPCResponse> call(
                JsonRpc.JSONRPCRequest request, McpRequestContext context);
    }

    /**
     * The reply to a legacy HTTP exchange.
     *
     * @param status The HTTP status.
     * @param body The JSON-RPC message to write, or null for none.
     */
    record Reply(Status status, Object body) {}

    /** The server edge, answering legacy clients from the modern core. */
    @FunctionalInterface
    interface Server {

        /**
         * Reads, decodes and answers one legacy exchange, in an admitted worker.
         *
         * @param request The request, whose headers the adapter already checked.
         * @param version The {@code MCP-Protocol-Version} header, or null.
         * @param context The request context.
         * @param decoded Given the id of the request once decoded, so that a failure the adapter
         *     answers past the edge, such as a timeout, still names it.
         * @return The reply.
         */
        Reply exchange(
                Request request,
                String version,
                McpRequestContext context,
                Consumer<Object> decoded);
    }

    /**
     * A started client of a legacy server, on the SDK's modern types: legacy results are converted
     * at the edge, a missing {@code resultType} meaning complete.
     */
    interface Client {

        /**
         * The revision the last {@code initialize} handshake negotiated, which may change when a
         * session expires and the client initializes again.
         *
         * @return The revision.
         */
        String negotiatedVersion();

        /**
         * The session id sent with each request, if any.
         *
         * @return The session id, or null.
         */
        String sessionId();

        Mono<McpSchema.ListToolsResult> listTools(String cursor);

        Mono<McpSchema.CallToolResult> callTool(McpSchema.CallToolRequest request);

        Mono<McpSchema.ListResourcesResult> listResources(String cursor);

        Mono<McpSchema.ListResourceTemplatesResult> listResourceTemplates(String cursor);

        Mono<McpSchema.ReadResourceResult> readResource(McpSchema.ReadResourceRequest request);

        Mono<McpSchema.ListPromptsResult> listPrompts(String cursor);

        Mono<McpSchema.GetPromptResult> getPrompt(McpSchema.GetPromptRequest request);

        /** Ends the client, and the session it opened, if any. */
        void close();
    }

    /**
     * The options of a legacy HTTP client.
     *
     * @param client The borrowed, started connector, not following redirects.
     * @param target The endpoint URI.
     * @param maxMessageBytes The largest message.
     * @param maxConcurrentCalls The most exchanges at once.
     * @param timeout The request timeout.
     * @param customizer Customizes each request.
     * @param versions The revisions offered, newest first.
     * @param sessions Whether the session id a server returns on {@code initialize} is kept, sent
     *     and finally deleted.
     * @param sessionId A session id the caller owns, sent but never deleted, or null.
     */
    record HttpOptions(
            org.restlet.Client client,
            String target,
            int maxMessageBytes,
            int maxConcurrentCalls,
            Duration timeout,
            Consumer<Request> customizer,
            List<String> versions,
            boolean sessions,
            String sessionId) {}

    /** The transport a stdio client connector owns, for one legacy client to bind. */
    interface StdioTransport {

        /** Applies a client's limits, before it connects. */
        void setLimits(int maxMessageBytes, int maxConcurrentCalls, Duration timeout);

        /** Ends the transport. */
        void close(Duration timeout);
    }

    /**
     * Creates the server edge of an adapter.
     *
     * @param mapper The JSON mapper.
     * @param core The modern core.
     * @param maxMessageBytes The largest request or response message.
     * @param timeout The request timeout.
     * @return The server edge.
     */
    static Server server(McpJsonMapper mapper, Core core, int maxMessageBytes, Duration timeout) {
        return new McpLegacyServer(mapper, core, maxMessageBytes, timeout);
    }

    /**
     * Whether a legacy client's failed start proves the server isn't a legacy one: a {@code 4xx} or
     * a JSON-RPC error answering {@code initialize}, unlike a timeout or a {@code 5xx}.
     *
     * @param failure The failure.
     * @return True if it does.
     */
    static boolean wrongEra(Throwable failure) {
        return McpLegacyClient.wrongEra(failure);
    }

    /**
     * Starts a legacy client over HTTP: an {@code initialize} handshake offering the options'
     * revisions.
     *
     * @param options The options.
     * @return The started client.
     */
    static Client http(HttpOptions options) {
        return McpLegacyClient.http(options);
    }

    /**
     * Starts a legacy client over a stream it owns.
     *
     * @param stream The stream.
     * @param maxMessageBytes The largest message.
     * @param maxConcurrentCalls The most exchanges at once.
     * @param timeout The request timeout.
     * @return The started client.
     */
    static Client stdio(
            StreamTransport stream, int maxMessageBytes, int maxConcurrentCalls, Duration timeout) {
        return McpLegacyClient.stdio(
                new StdioMcpClientTransport(stream, maxMessageBytes, maxConcurrentCalls, timeout),
                true,
                timeout);
    }

    /**
     * Starts a legacy client over the transport a stdio connector owns, which stays open when the
     * client closes.
     *
     * @param transport The connector's transport.
     * @param maxMessageBytes The largest message.
     * @param maxConcurrentCalls The most exchanges at once.
     * @param timeout The request timeout.
     * @return The started client.
     */
    static Client stdio(
            StdioTransport transport,
            int maxMessageBytes,
            int maxConcurrentCalls,
            Duration timeout) {
        transport.setLimits(maxMessageBytes, maxConcurrentCalls, timeout);
        return McpLegacyClient.stdio((StdioMcpClientTransport) transport, false, timeout);
    }

    /**
     * Creates the transport of a stdio client connector.
     *
     * @param stream The stream.
     * @return The transport.
     */
    static StdioTransport stdioTransport(StreamTransport stream) {
        return new StdioMcpClientTransport(
                stream,
                org.restlet.ext.mcp.McpProtocol.DEFAULT_MAX_MESSAGE_BYTES,
                org.restlet.ext.mcp.McpProtocol.DEFAULT_MAX_CONCURRENT_CALLS,
                Duration.ofSeconds(30));
    }
}
