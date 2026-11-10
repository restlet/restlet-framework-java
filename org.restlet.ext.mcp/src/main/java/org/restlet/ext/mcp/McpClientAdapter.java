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
import io.modelcontextprotocol.json.McpJsonDefaults;
import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.json.TypeRef;
import io.modelcontextprotocol.spec.McpClientTransport;
import io.modelcontextprotocol.spec.McpSchema;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import org.restlet.Client;
import org.restlet.Request;
import org.restlet.ext.mcp.internal.McpModernHttpTransport;
import org.restlet.ext.mcp.internal.StdioMcpClientTransport;
import reactor.core.publisher.Mono;

/**
 * Lifecycle facade over the native SDK. Borrowed Restlet connectors are never stopped.
 *
 * <p>By default it speaks MCP {@link McpProtocol#VERSION} through the SDK's client, {@link
 * #getSdkClient()}. With {@link #setProtocolVersion(String)} set to {@link
 * McpProtocol#MODERN_VERSION}, it speaks {@code 2026-07-28} over HTTP instead, experimentally:
 * {@link #start()} discovers the server, and {@link #getModernClient()} makes the requests.
 */
public final class McpClientAdapter implements AutoCloseable {
    /** The default number of {@code input_required} rounds answered for one request. */
    public static final int DEFAULT_MAX_INPUT_ROUNDS = 8;

    private final Client client;
    private final String target;
    private final org.restlet.util.StreamTransport stream;
    private Duration requestTimeout = Duration.ofSeconds(30);
    private int maxMessageBytes = McpProtocol.DEFAULT_MAX_MESSAGE_BYTES;
    private int maxConcurrentCalls = McpProtocol.DEFAULT_MAX_CONCURRENT_CALLS;
    private Consumer<Request> requestCustomizer = request -> {};
    private String protocolVersion = McpProtocol.VERSION;
    private io.modelcontextprotocol.modern.McpSchema.ClientCapabilities modernCapabilities =
            io.modelcontextprotocol.modern.McpSchema.ClientCapabilities.NONE;
    private McpInputHandler inputHandler;
    private int maxInputRounds = DEFAULT_MAX_INPUT_ROUNDS;
    private McpModernClient modern;
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

    /**
     * Sets the MCP revision spoken: {@link McpProtocol#VERSION}, the default, through the SDK's
     * client, or {@link McpProtocol#MODERN_VERSION} through {@link #getModernClient()}, over HTTP
     * only for now. Experimental for {@code 2026-07-28}.
     *
     * @param value The revision.
     */
    public synchronized void setProtocolVersion(String value) {
        requireConfigurable();
        if (!McpProtocol.VERSION.equals(value) && !McpProtocol.MODERN_VERSION.equals(value)) {
            throw new IllegalArgumentException("Unsupported MCP revision: " + value);
        }
        protocolVersion = value;
    }

    public synchronized String getProtocolVersion() {
        return protocolVersion;
    }

    /**
     * Sets the client capabilities sent with each {@link McpProtocol#MODERN_VERSION} request, none
     * by default. A server asks only for the input they declare, such as elicitation.
     *
     * @param value The capabilities.
     */
    public synchronized void setModernCapabilities(
            io.modelcontextprotocol.modern.McpSchema.ClientCapabilities value) {
        requireConfigurable();
        modernCapabilities = Objects.requireNonNull(value, "value");
    }

    public synchronized io.modelcontextprotocol.modern.McpSchema.ClientCapabilities
            getModernCapabilities() {
        return modernCapabilities;
    }

    /**
     * Sets what answers the {@code input_required} results of {@link McpProtocol#MODERN_VERSION}
     * requests, after which the request is retried; without one, the result is returned.
     *
     * @param value The handler, or null for none.
     */
    public synchronized void setInputHandler(McpInputHandler value) {
        requireConfigurable();
        inputHandler = value;
    }

    public synchronized McpInputHandler getInputHandler() {
        return inputHandler;
    }

    /**
     * Sets how many {@code input_required} results the input handler answers for one request,
     * {@value #DEFAULT_MAX_INPUT_ROUNDS} by default; the next one is returned.
     *
     * @param value The number of rounds, zero or more.
     */
    public synchronized void setMaxInputRounds(int value) {
        requireConfigurable();
        if (value < 0) {
            throw new IllegalArgumentException("Input rounds must not be negative");
        }
        maxInputRounds = value;
    }

    public synchronized int getMaxInputRounds() {
        return maxInputRounds;
    }

    public synchronized void start() {
        if (started) {
            return;
        }
        requireConfigurable();
        configured = true;
        if (McpProtocol.MODERN_VERSION.equals(protocolVersion)) {
            startModern();
            return;
        }
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

    /** Starts a {@code 2026-07-28} client, and discovers the server's support of it. */
    private void startModern() {
        if (stream != null || target.startsWith("stdio:")) {
            throw new UnsupportedOperationException(
                    "The MCP 2026-07-28 client supports HTTP only for now");
        }
        if (!client.isStarted()
                || client.getContext() == null
                || !"false"
                        .equalsIgnoreCase(
                                client.getContext()
                                        .getParameters()
                                        .getFirstValue("followRedirects", "true"))) {
            throw new IllegalStateException(
                    "Borrowed MCP client must be started with followRedirects=false");
        }
        McpJsonMapper mapper = McpJsonDefaults.getMapper();
        McpModernClient candidate =
                new McpModernClient(
                        new McpModernHttpTransport(
                                client,
                                target,
                                mapper,
                                maxMessageBytes,
                                maxConcurrentCalls,
                                requestTimeout,
                                requestCustomizer),
                        mapper,
                        modernCapabilities,
                        inputHandler,
                        maxInputRounds);
        try {
            io.modelcontextprotocol.modern.McpSchema.DiscoverResult discovered =
                    candidate.discover().block(requestTimeout);
            if (discovered == null
                    || discovered.supportedVersions() == null
                    || !discovered.supportedVersions().contains(McpProtocol.MODERN_VERSION)) {
                throw new IllegalStateException("Peer does not support MCP 2026-07-28");
            }
        } catch (RuntimeException e) {
            try {
                candidate.close();
            } catch (IOException cleanup) {
                e.addSuppressed(cleanup);
            }
            throw e;
        }
        modern = candidate;
        started = true;
    }

    /**
     * The {@link McpProtocol#MODERN_VERSION} client, when that is the revision set.
     *
     * @return The client.
     */
    public synchronized McpModernClient getModernClient() {
        if (!started || modern == null) {
            throw new IllegalStateException("MCP 2026-07-28 client is not started");
        }
        return modern;
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
        if (modern != null) {
            throw new IllegalStateException(
                    "This MCP client speaks 2026-07-28: use getModernClient()");
        }
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
        if (modern != null) {
            started = false;
            try {
                modern.close();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            } finally {
                modern = null;
            }
        }
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
