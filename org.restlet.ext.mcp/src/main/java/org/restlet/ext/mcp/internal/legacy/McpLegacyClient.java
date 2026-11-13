/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.mcp.internal.legacy;

import io.modelcontextprotocol.client.McpAsyncClient;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.json.McpJsonDefaults;
import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.json.TypeRef;
import io.modelcontextprotocol.modern.McpSchema;
import io.modelcontextprotocol.spec.McpClientTransport;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import org.restlet.ext.mcp.McpProtocol;
import org.restlet.ext.mcp.internal.McpLegacyEdge;
import reactor.core.publisher.Mono;

/**
 * The SDK's client of a legacy MCP server, on the modern types: requests are converted to the
 * legacy ones and results back, a missing {@code resultType} meaning complete. The handshake
 * declares no capability, so the server may ask for no roots, sampling or elicitation, and the
 * client never sets a logging level.
 */
public final class McpLegacyClient implements McpLegacyEdge.Client {
    private static final TypeRef<Map<String, Object>> MAP = new TypeRef<>() {};

    private final McpJsonMapper mapper = McpJsonDefaults.getMapper();
    private final McpAsyncClient sdk;
    private final RestletMcpClientTransport http;
    private final Duration timeout;
    private final String negotiated;

    private McpLegacyClient(
            McpAsyncClient sdk,
            RestletMcpClientTransport http,
            Duration timeout,
            String negotiated) {
        this.sdk = sdk;
        this.http = http;
        this.timeout = timeout;
        this.negotiated = negotiated;
    }

    /**
     * Starts a client over HTTP.
     *
     * @param options The options.
     * @return The started client.
     */
    public static McpLegacyClient http(McpLegacyEdge.HttpOptions options) {
        var transport =
                new RestletMcpClientTransport(
                        options.client(),
                        options.target(),
                        options.maxMessageBytes(),
                        options.maxConcurrentCalls(),
                        options.timeout(),
                        options.customizer(),
                        options.versions(),
                        options.sessions(),
                        options.sessionId());
        return start(transport, transport, options.versions(), options.timeout());
    }

    /**
     * Starts a client over stdio.
     *
     * @param transport The transport.
     * @param owned Whether the client closes the transport, rather than its connector.
     * @param timeout The request timeout.
     * @return The started client.
     */
    public static McpLegacyClient stdio(
            StdioMcpClientTransport transport, boolean owned, Duration timeout) {
        return start(
                owned ? transport : new Borrowed(transport),
                null,
                List.of(McpProtocol.VERSION),
                timeout);
    }

    /** Initializes the SDK client, closing the transport if that fails. */
    private static McpLegacyClient start(
            McpClientTransport transport,
            RestletMcpClientTransport http,
            List<String> versions,
            Duration timeout) {
        McpAsyncClient sdk = null;
        try {
            sdk =
                    McpClient.async(transport)
                            .requestTimeout(timeout)
                            .initializationTimeout(timeout)
                            .build();
            var initialized = sdk.initialize().block(timeout);
            if (initialized == null || !versions.contains(initialized.protocolVersion())) {
                throw new IllegalStateException("Peer did not negotiate the supported MCP version");
            }
            return new McpLegacyClient(sdk, http, timeout, initialized.protocolVersion());
        } catch (RuntimeException e) {
            try {
                if (sdk != null) {
                    sdk.closeGracefully().block(timeout);
                } else {
                    transport.closeGracefully().block(timeout);
                }
            } catch (RuntimeException cleanup) {
                e.addSuppressed(cleanup);
            }
            throw e;
        }
    }

    @Override
    public String negotiatedVersion() {
        return negotiated;
    }

    @Override
    public String sessionId() {
        return http == null ? null : http.getSessionId();
    }

    @Override
    public Mono<McpSchema.ListToolsResult> listTools(String cursor) {
        return sdk.listTools(cursor).map(result -> modern(result, McpSchema.ListToolsResult.class));
    }

    @Override
    public Mono<McpSchema.CallToolResult> callTool(McpSchema.CallToolRequest request) {
        return sdk.callTool(
                        new io.modelcontextprotocol.spec.McpSchema.CallToolRequest(
                                request.name(), request.arguments(), request.meta()))
                .map(result -> modern(result, McpSchema.CallToolResult.class));
    }

    @Override
    public Mono<McpSchema.ListResourcesResult> listResources(String cursor) {
        return sdk.listResources(cursor)
                .map(result -> modern(result, McpSchema.ListResourcesResult.class));
    }

    @Override
    public Mono<McpSchema.ListResourceTemplatesResult> listResourceTemplates(String cursor) {
        return sdk.listResourceTemplates(cursor)
                .map(result -> modern(result, McpSchema.ListResourceTemplatesResult.class));
    }

    @Override
    public Mono<McpSchema.ReadResourceResult> readResource(McpSchema.ReadResourceRequest request) {
        return sdk.readResource(
                        new io.modelcontextprotocol.spec.McpSchema.ReadResourceRequest(
                                request.uri(), request.meta()))
                .map(result -> modern(result, McpSchema.ReadResourceResult.class));
    }

    @Override
    public Mono<McpSchema.ListPromptsResult> listPrompts(String cursor) {
        return sdk.listPrompts(cursor)
                .map(result -> modern(result, McpSchema.ListPromptsResult.class));
    }

    @Override
    public Mono<McpSchema.GetPromptResult> getPrompt(McpSchema.GetPromptRequest request) {
        return sdk.getPrompt(
                        new io.modelcontextprotocol.spec.McpSchema.GetPromptRequest(
                                request.name(),
                                request.arguments() == null
                                        ? null
                                        : Map.<String, Object>copyOf(request.arguments()),
                                request.meta()))
                .map(result -> modern(result, McpSchema.GetPromptResult.class));
    }

    /** A legacy result on the modern types, complete since legacy results have no type. */
    private <T> T modern(Object result, Class<T> type) {
        Map<String, Object> fields = mapper.convertValue(result, MAP);
        fields.putIfAbsent("resultType", McpSchema.ResultType.COMPLETE);
        return mapper.convertValue(fields, type);
    }

    @Override
    public void close() {
        sdk.closeGracefully().block(timeout);
    }

    /** A connector's transport, which the client uses but doesn't close. */
    private record Borrowed(StdioMcpClientTransport delegate) implements McpClientTransport {
        @Override
        public Mono<Void> connect(
                Function<
                                Mono<io.modelcontextprotocol.spec.McpSchema.JSONRPCMessage>,
                                Mono<io.modelcontextprotocol.spec.McpSchema.JSONRPCMessage>>
                        handler) {
            return delegate.connect(handler);
        }

        @Override
        public void setExceptionHandler(Consumer<Throwable> handler) {
            delegate.setExceptionHandler(handler);
        }

        @Override
        public List<String> protocolVersions() {
            return delegate.protocolVersions();
        }

        @Override
        public Mono<Void> sendMessage(
                io.modelcontextprotocol.spec.McpSchema.JSONRPCMessage message) {
            return delegate.sendMessage(message);
        }

        @Override
        public <T> T unmarshalFrom(Object data, TypeRef<T> type) {
            return delegate.unmarshalFrom(data, type);
        }

        @Override
        public Mono<Void> closeGracefully() {
            return Mono.empty();
        }
    }
}
