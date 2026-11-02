/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.mcp;

import io.modelcontextprotocol.client.McpAsyncClient;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.json.TypeRef;
import io.modelcontextprotocol.spec.McpClientTransport;
import io.modelcontextprotocol.spec.McpSchema;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import org.restlet.Client;
import org.restlet.Request;
import org.restlet.ext.mcp.internal.StdioMcpClientTransport;
import reactor.core.publisher.Mono;

/** Lifecycle facade over the native SDK. Borrowed Restlet connectors are never stopped. */
public final class McpClientAdapter implements AutoCloseable {
    private final Client client;
    private final String target;
    private final org.restlet.util.StreamTransport stream;
    private Duration requestTimeout = Duration.ofSeconds(30);
    private int maxMessageBytes = McpProtocol.DEFAULT_MAX_MESSAGE_BYTES;
    private int maxConcurrentCalls = McpProtocol.DEFAULT_MAX_CONCURRENT_CALLS;
    private Consumer<Request> requestCustomizer = request -> {};
    private McpAsyncClient sdk;
    private McpClientTransport transport;
    private boolean started;
    private boolean configured;

    public McpClientAdapter(Client client, String target) {
        this.client = Objects.requireNonNull(client, "client");
        this.target = Objects.requireNonNull(target, "target");
        stream = null;
    }

    /** Owns this stream transport's lifecycle; underlying borrowed streams remain borrowed. */
    public McpClientAdapter(org.restlet.util.StreamTransport stream) {
        this.stream = Objects.requireNonNull(stream, "stream");
        client = null;
        target = null;
    }

    private void requireConfigurable() {
        if (configured) {
            throw new IllegalStateException("MCP client is single-use and configuration is fixed");
        }
    }

    public synchronized void setRequestTimeout(Duration value) {
        requireConfigurable();
        if (value.isZero() || value.isNegative()) {
            throw new IllegalArgumentException("Positive timeout required");
        }
        requestTimeout = Duration.ofNanos(value.toNanos());
    }

    public synchronized Duration getRequestTimeout() {
        return requestTimeout;
    }

    public synchronized void setMaxMessageBytes(int value) {
        requireConfigurable();
        if (value <= 0 || value == Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Positive finite limit required");
        }
        maxMessageBytes = value;
    }

    public synchronized int getMaxMessageBytes() {
        return maxMessageBytes;
    }

    public synchronized void setMaxConcurrentCalls(int value) {
        requireConfigurable();
        if (value <= 0) {
            throw new IllegalArgumentException("Positive call limit required");
        }
        maxConcurrentCalls = value;
    }

    public synchronized int getMaxConcurrentCalls() {
        return maxConcurrentCalls;
    }

    public synchronized void setRequestCustomizer(Consumer<Request> value) {
        requireConfigurable();
        requestCustomizer = Objects.requireNonNull(value);
    }

    public synchronized Consumer<Request> getRequestCustomizer() {
        return requestCustomizer;
    }

    public synchronized void start() {
        if (started) {
            return;
        }
        requireConfigurable();
        configured = true;
        transport = createTransport();
        try {
            sdk =
                    McpClient.async(transport)
                            .requestTimeout(requestTimeout)
                            .initializationTimeout(requestTimeout)
                            .build();
            McpSchema.InitializeResult initialized = sdk.initialize().block(requestTimeout);
            if (initialized == null || !McpProtocol.VERSION.equals(initialized.protocolVersion())) {
                throw new IllegalStateException("Peer did not negotiate the supported MCP version");
            }
            started = true;
        } catch (RuntimeException e) {
            try {
                if (sdk != null) {
                    sdk.closeGracefully().block(requestTimeout);
                } else {
                    transport.closeGracefully().block(requestTimeout);
                }
            } catch (RuntimeException cleanup) {
                e.addSuppressed(cleanup);
            }
            throw e;
        }
    }

    private McpClientTransport createTransport() {
        if (stream != null) {
            return new StdioMcpClientTransport(
                    stream, maxMessageBytes, maxConcurrentCalls, requestTimeout);
        }
        if (target.startsWith("stdio:")) {
            if (!client.isStarted()
                    || client.getContext() == null
                    || !(client.getContext()
                                    .getAttributes()
                                    .get(StdioClientHelper.TRANSPORT_ATTRIBUTE)
                            instanceof StdioClientHelper helper)) {
                throw new IllegalStateException(
                        "A started explicitly selected MCP STDIO helper is required");
            }
            return new BorrowedTransport(
                    helper.bind(maxMessageBytes, maxConcurrentCalls, requestTimeout));
        }
        return new RestletMcpClientTransport(
                client,
                target,
                maxMessageBytes,
                maxConcurrentCalls,
                requestTimeout,
                requestCustomizer);
    }

    /** Exposes native MCP operations, errors and call options without a parallel invocation API. */
    public synchronized McpAsyncClient getSdkClient() {
        if (!started) {
            throw new IllegalStateException("MCP client is not started");
        }
        return sdk;
    }

    public synchronized boolean isStarted() {
        return started;
    }

    public McpSchema.ListToolsResult listTools(String cursor) {
        return getSdkClient().listTools(cursor).block(requestTimeout);
    }

    public McpSchema.CallToolResult callTool(String name, Map<String, Object> arguments) {
        return getSdkClient()
                .callTool(new McpSchema.CallToolRequest(name, arguments, null))
                .block(requestTimeout);
    }

    public synchronized void stop() {
        if (sdk != null) {
            started = false;
            sdk.closeGracefully().block(requestTimeout);
            sdk = null;
        }
    }

    @Override
    public void close() {
        stop();
    }

    private record BorrowedTransport(McpClientTransport delegate) implements McpClientTransport {
        @Override
        public Mono<Void> connect(
                Function<Mono<McpSchema.JSONRPCMessage>, Mono<McpSchema.JSONRPCMessage>> handler) {
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
        public Mono<Void> sendMessage(McpSchema.JSONRPCMessage message) {
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
