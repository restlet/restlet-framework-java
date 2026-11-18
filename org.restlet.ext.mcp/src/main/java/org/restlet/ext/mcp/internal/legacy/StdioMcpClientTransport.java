/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.mcp.internal.legacy;

import io.modelcontextprotocol.json.McpJsonDefaults;
import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.json.TypeRef;
import io.modelcontextprotocol.spec.McpClientTransport;
import io.modelcontextprotocol.spec.McpSchema;
import java.io.EOFException;
import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.restlet.ext.mcp.McpProtocol;
import org.restlet.ext.mcp.internal.McpLegacyEdge;
import org.restlet.ext.mcp.internal.McpMessages;
import org.restlet.ext.mcp.internal.McpWorkers;
import reactor.core.publisher.Mono;

/**
 * One reader per pipe, finite outstanding IDs for admission only; the SDK owns correlation. An ID
 * left unanswered past the call timeout frees its slot, and its late reply is dropped. Requests the
 * server sends, such as sampling, roots or elicitation, are answered as not found without reaching
 * the SDK client: the legacy edge declares no capability and answers none.
 */
public final class StdioMcpClientTransport
        implements McpClientTransport, McpLegacyEdge.StdioTransport {
    private final McpJsonMapper mapper = McpJsonDefaults.getMapper();
    private final org.restlet.util.StreamTransport stream;
    private volatile int maxMessageBytes;
    private volatile int maxPendingCalls;
    private volatile Duration timeout;
    private volatile McpWorkers writers;
    private final Map<Object, Long> outstanding = new HashMap<>();
    private final Set<Object> expired = new HashSet<>();
    private volatile Function<Mono<McpSchema.JSONRPCMessage>, Mono<McpSchema.JSONRPCMessage>>
            incoming;
    private volatile Consumer<Throwable> exceptionHandler =
            error ->
                    Logger.getLogger(StdioMcpClientTransport.class.getName())
                            .log(Level.WARNING, "MCP STDIO failure", error);
    private volatile Throwable failure;
    private volatile boolean closed;
    private Thread reader;

    public StdioMcpClientTransport(
            org.restlet.util.StreamTransport stream,
            int maxMessageBytes,
            int maxPendingCalls,
            Duration timeout) {
        this.stream = Objects.requireNonNull(stream, "stream");
        setLimits(maxMessageBytes, maxPendingCalls, timeout);
    }

    /** Replaces the byte, call and timeout limits; only allowed before connecting. */
    @Override
    public synchronized void setLimits(int maxMessageBytes, int maxPendingCalls, Duration timeout) {
        if (closed || incoming != null) {
            throw new IllegalStateException("MCP STDIO limits are fixed once connected");
        }
        if (maxMessageBytes <= 0
                || maxMessageBytes == Integer.MAX_VALUE
                || maxPendingCalls <= 0
                || timeout.isZero()
                || timeout.isNegative()) {
            throw new IllegalArgumentException(
                    "Positive finite MCP byte/call/time limits required");
        }
        this.maxMessageBytes = maxMessageBytes;
        this.maxPendingCalls = maxPendingCalls;
        this.timeout = timeout;
    }

    @Override
    public synchronized Mono<Void> connect(
            Function<Mono<McpSchema.JSONRPCMessage>, Mono<McpSchema.JSONRPCMessage>> handler) {
        if (closed || incoming != null) {
            return Mono.error(new IllegalStateException("MCP STDIO is single-use"));
        }
        try {
            stream.setMaxOperationBytes(maxMessageBytes + 1);
            stream.start();
            writers = new McpWorkers(maxPendingCalls, timeout);
            incoming = Objects.requireNonNull(handler, "handler");
            reader = Thread.ofVirtual().name("restlet-mcp-stdio-reader").start(this::read);
            return Mono.empty();
        } catch (IOException | RuntimeException e) {
            return Mono.error(e);
        }
    }

    @Override
    public void setExceptionHandler(Consumer<Throwable> handler) {
        exceptionHandler = Objects.requireNonNull(handler);
    }

    @Override
    public List<String> protocolVersions() {
        return List.of(McpProtocol.VERSION);
    }

    @Override
    public Mono<Void> sendMessage(McpSchema.JSONRPCMessage message) {
        McpWorkers current = writers;
        if (current == null) {
            return Mono.error(new IOException("MCP STDIO is not connected"));
        }
        return current.submit(
                () -> {
                    if (closed || failure != null || incoming == null) {
                        throw new IOException("MCP STDIO is not connected", failure);
                    }
                    Object id = message instanceof McpSchema.JSONRPCRequest rpc ? rpc.id() : null;
                    if (id != null) {
                        admit(id);
                    }
                    try {
                        McpMessages.writeFrame(
                                stream, McpMessages.encode(mapper, message, maxMessageBytes));
                    } catch (IOException | RuntimeException e) {
                        if (id != null) {
                            synchronized (outstanding) {
                                outstanding.remove(id);
                            }
                        }
                        throw e;
                    }
                },
                () -> {});
    }

    /**
     * Takes a pending slot for a request ID, first freeing the slots of IDs whose call timed out.
     * At most maxPendingCalls expired IDs are remembered to drop their late replies; beyond that,
     * the peer is considered unresponsive and sending fails.
     */
    private void admit(Object id) throws IOException {
        synchronized (outstanding) {
            long now = System.nanoTime();
            outstanding
                    .entrySet()
                    .removeIf(entry -> now - entry.getValue() >= 0 && expired.add(entry.getKey()));
            if (expired.size() > maxPendingCalls) {
                throw new IOException("MCP STDIO peer left too many calls unanswered");
            }
            if (outstanding.size() >= maxPendingCalls
                    || outstanding.containsKey(id)
                    || expired.contains(id)) {
                throw new IOException("MCP STDIO pending call limit or duplicate id");
            }
            outstanding.put(id, now + timeout.toNanos());
        }
    }

    /** Returns false for the late reply of an expired call, which the SDK no longer awaits. */
    private boolean correlate(McpSchema.JSONRPCResponse response) throws IOException {
        synchronized (outstanding) {
            if (outstanding.remove(response.id()) != null) {
                return true;
            }
            if (expired.remove(response.id())) {
                return false;
            }
        }
        throw new IOException("Unsolicited MCP STDIO response id");
    }

    private void read() {
        try {
            byte[] bytes;
            while (!closed && (bytes = McpMessages.readFrame(stream, maxMessageBytes)) != null) {
                McpSchema.JSONRPCMessage message =
                        McpSchema.deserializeJsonRpcMessage(mapper, McpMessages.utf8(bytes));
                if (message instanceof McpSchema.JSONRPCRequest request) {
                    reject(request);
                } else if (!(message instanceof McpSchema.JSONRPCResponse response)
                        || correlate(response)) {
                    incoming.apply(Mono.just(message)).block(timeout);
                }
            }
            if (!closed) {
                throw new EOFException("MCP subprocess or pipe ended");
            }
        } catch (IOException | RuntimeException e) {
            if (!closed) {
                failure = e;
                exceptionHandler.accept(e);
            }
        }
    }

    /** Answers a request from the server as not found, off the reader thread. */
    private void reject(McpSchema.JSONRPCRequest request) {
        sendMessage(
                        new McpSchema.JSONRPCResponse(
                                McpSchema.JSONRPC_VERSION,
                                request.id(),
                                null,
                                new McpSchema.JSONRPCResponse.JSONRPCError(
                                        McpSchema.ErrorCodes.METHOD_NOT_FOUND,
                                        "Method not found: " + request.method(),
                                        null)))
                .subscribe(
                        null,
                        error ->
                                Logger.getLogger(StdioMcpClientTransport.class.getName())
                                        .log(Level.FINE, "MCP server request not answered", error));
    }

    @Override
    public void close(Duration timeout) {
        closeGracefully().block(timeout);
    }

    @Override
    public <T> T unmarshalFrom(Object data, TypeRef<T> type) {
        return mapper.convertValue(data, type);
    }

    @Override
    public Mono<Void> closeGracefully() {
        return Mono.fromCallable(
                        () -> {
                            closed = true;
                            IOException error = null;
                            try {
                                stream.stop();
                            } catch (IOException e) {
                                error = e;
                            }
                            try {
                                if (writers != null) {
                                    writers.close();
                                }
                            } catch (IOException e) {
                                if (error == null) {
                                    error = e;
                                } else {
                                    error.addSuppressed(e);
                                }
                            }
                            if (reader != null && !reader.join(timeout)) {
                                IOException join =
                                        new IOException("MCP STDIO reader shutdown timed out");
                                if (error == null) {
                                    error = join;
                                } else {
                                    error.addSuppressed(join);
                                }
                            }
                            synchronized (outstanding) {
                                outstanding.clear();
                                expired.clear();
                            }
                            if (error != null) {
                                throw error;
                            }
                            return Boolean.TRUE;
                        })
                .then();
    }
}
