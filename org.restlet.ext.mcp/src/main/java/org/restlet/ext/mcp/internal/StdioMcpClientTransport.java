/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.mcp.internal;

import io.modelcontextprotocol.json.McpJsonDefaults;
import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.json.TypeRef;
import io.modelcontextprotocol.spec.McpClientTransport;
import io.modelcontextprotocol.spec.McpSchema;
import java.io.EOFException;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.restlet.ext.mcp.McpProtocol;
import reactor.core.publisher.Mono;

/** One reader per pipe, finite outstanding IDs for admission only; the SDK owns correlation. */
public final class StdioMcpClientTransport implements McpClientTransport {
    private final McpJsonMapper mapper = McpJsonDefaults.getMapper();
    private final org.restlet.util.StreamTransport stream;
    private final int maxMessageBytes;
    private final int maxPendingCalls;
    private final Duration timeout;
    private final McpWorkers writers;
    private final Set<Object> outstanding = ConcurrentHashMap.newKeySet();
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
        if (maxMessageBytes <= 0 || maxMessageBytes == Integer.MAX_VALUE || maxPendingCalls <= 0) {
            throw new IllegalArgumentException("Positive finite MCP byte/call limits required");
        }
        this.maxMessageBytes = maxMessageBytes;
        this.maxPendingCalls = maxPendingCalls;
        this.timeout = timeout;
        writers = new McpWorkers(maxPendingCalls, timeout);
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
        return writers.submit(
                () -> {
                    if (closed || failure != null || incoming == null) {
                        throw new IOException("MCP STDIO is not connected", failure);
                    }
                    Object id = message instanceof McpSchema.JSONRPCRequest rpc ? rpc.id() : null;
                    if (id != null) {
                        synchronized (outstanding) {
                            if (outstanding.size() >= maxPendingCalls || !outstanding.add(id)) {
                                throw new IOException(
                                        "MCP STDIO pending call limit or duplicate id");
                            }
                        }
                    }
                    try {
                        McpMessages.writeFrame(
                                stream, McpMessages.encode(mapper, message, maxMessageBytes));
                    } catch (IOException | RuntimeException e) {
                        if (id != null) {
                            outstanding.remove(id);
                        }
                        throw e;
                    }
                },
                () -> {});
    }

    private void read() {
        try {
            byte[] bytes;
            while (!closed && (bytes = McpMessages.readFrame(stream, maxMessageBytes)) != null) {
                McpSchema.JSONRPCMessage message =
                        McpSchema.deserializeJsonRpcMessage(mapper, McpMessages.utf8(bytes));
                if (message instanceof McpSchema.JSONRPCResponse response) {
                    if (!outstanding.remove(response.id())) {
                        throw new IOException("Unsolicited MCP STDIO response id");
                    }
                }
                incoming.apply(Mono.just(message)).block(timeout);
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
                                writers.close();
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
                            outstanding.clear();
                            if (error != null) {
                                throw error;
                            }
                            return Boolean.TRUE;
                        })
                .then();
    }
}
