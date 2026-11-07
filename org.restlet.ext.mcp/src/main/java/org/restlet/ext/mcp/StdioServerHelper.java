/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.mcp;

import io.modelcontextprotocol.json.McpJsonDefaults;
import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.json.TypeRef;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Server;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.engine.connector.ServerHelper;
import org.restlet.ext.mcp.internal.McpMessages;
import org.restlet.ext.mcp.internal.McpModernExchange;
import org.restlet.ext.mcp.internal.McpWorkers;
import org.restlet.representation.ByteArrayRepresentation;
import org.restlet.representation.Representation;

/**
 * Explicit newline-framed MCP STDIO connector. Every message traverses the Server's configured
 * Restlet chain. The owner must supply a StreamTransport; stdout is exclusively protocol output.
 *
 * <p>A message whose {@code _meta} names MCP {@link McpProtocol#MODERN_VERSION} goes to the
 * adapter's 2026-07-28 server: the messages it streams, such as progress, are written as separate
 * lines as they come, a {@code subscriptions/listen} stream stays open without time limit, and a
 * {@code notifications/cancelled} naming an in-flight request ends it without a response.
 */
public final class StdioServerHelper extends ServerHelper {
    private static final String CANCELLED = "notifications/cancelled";

    /** Request attribute marking a request the client cancelled, which gets no response. */
    private static final String CANCELLED_ATTRIBUTE = "org.restlet.ext.mcp.stdio.cancelled";

    /** Room for the {@code data: } prefix of a streamed message's line. */
    private static final int EVENT_LINE_OVERHEAD = 16;

    private final McpJsonMapper mapper = McpJsonDefaults.getMapper();
    private final Set<Request> requests = ConcurrentHashMap.newKeySet();

    /** The in-flight 2026-07-28 requests, by id, which a client may cancel. */
    private final Map<Object, Request> inFlight = new ConcurrentHashMap<>();

    private org.restlet.util.StreamTransport stream;
    private McpWorkers workers;

    /**
     * The workers of 2026-07-28 requests, without time limit of their own: the adapter's request
     * timeout applies, and a request it cuts short is answered rather than failing the connector.
     */
    private McpWorkers modernWorkers;

    /** The workers of {@code subscriptions/listen} streams, without time limit. */
    private McpWorkers listeners;

    private Thread reader;
    private volatile boolean closed;

    /** Whether the connector was stopped, after which failures are expected. */
    private volatile boolean stopped;

    private volatile Throwable failure;

    public StdioServerHelper(Server server) {
        super(server);
        getProtocols().add(Protocol.STDIO);
    }

    @Override
    public synchronized void start() throws IOException {
        if (reader != null) {
            throw new IllegalStateException("MCP STDIO connector is single-use");
        }
        Object value = getContext().getAttributes().get(McpProtocol.STREAM_TRANSPORT_ATTRIBUTE);
        if (!(value instanceof org.restlet.util.StreamTransport configured)) {
            throw new IllegalStateException(
                    "MCP STDIO requires an explicitly configured StreamTransport");
        }
        stream = configured;
        stream.setMaxOperationBytes(McpProtocol.DEFAULT_MAX_MESSAGE_BYTES + 1);
        stream.start();
        workers = new McpWorkers(McpProtocol.DEFAULT_MAX_CONCURRENT_CALLS, Duration.ofSeconds(30));
        modernWorkers =
                new McpWorkers(
                        McpProtocol.DEFAULT_MAX_CONCURRENT_CALLS, null, Duration.ofSeconds(30));
        listeners =
                new McpWorkers(
                        McpProtocol.DEFAULT_MAX_LISTEN_STREAMS, null, Duration.ofSeconds(30));
        reader = Thread.ofVirtual().name("restlet-mcp-stdio-server").start(this::read);
    }

    private void read() {
        try {
            byte[] bytes;
            while (!closed
                    && (bytes =
                                    McpMessages.readFrame(
                                            stream, McpProtocol.DEFAULT_MAX_MESSAGE_BYTES))
                            != null) {
                accept(bytes);
            }
            if (!closed) {
                fail(new IOException("MCP STDIO input ended"));
            }
        } catch (IOException | RuntimeException e) {
            if (!closed) {
                fail(e);
            }
        }
    }

    /** Builds the request of a frame, in reading order, and hands it to a worker. */
    private void accept(byte[] bytes) {
        Map<?, ?> envelope = envelope(bytes);
        boolean modern = envelope != null && modern(envelope);
        String method = envelope != null && envelope.get("method") instanceof String m ? m : null;
        // A cancellation of an in-flight 2026-07-28 request, with or without _meta.
        if (CANCELLED.equals(method) && (cancel(envelope) || modern)) {
            return;
        }
        Request request =
                new Request(
                        Method.POST,
                        "stdio://localhost/mcp",
                        new ByteArrayRepresentation(bytes, MediaType.APPLICATION_JSON));
        request.getHeaders()
                .set(
                        McpProtocol.VERSION_HEADER,
                        modern ? McpProtocol.MODERN_VERSION : McpProtocol.VERSION);
        request.getHeaders().set("Accept", "application/json, text/event-stream");
        if (!modern) {
            workers.submit(() -> dispatch(request), () -> {}).subscribe(ignored -> {}, this::fail);
            return;
        }
        if (method != null) {
            request.getHeaders().set("Mcp-Method", method);
        }
        request.getAttributes().put(McpModernExchange.STDIO_ATTRIBUTE, Boolean.TRUE);
        Object id = envelope.get("id");
        Object key = id == null ? null : key(id);
        if (key != null && inFlight.putIfAbsent(key, request) != null) {
            key = null; // A duplicate id: only the first may be cancelled.
        }
        Object registered = key;
        boolean listen = McpModernExchange.isListen(method);
        (listen ? listeners : modernWorkers)
                .submit(
                        () -> {
                            try {
                                dispatch(request);
                            } finally {
                                if (registered != null) {
                                    inFlight.remove(registered, request);
                                }
                            }
                        },
                        () -> {})
                .subscribe(
                        ignored -> {},
                        error -> {
                            if (registered != null) {
                                inFlight.remove(registered, request);
                            }
                            if (!closed && !isCancelled(request)) {
                                // Refused, too large or broken: answered, the connector stays up.
                                refuse(id, error);
                            }
                        });
    }

    /** The JSON object of a frame, or null when it isn't one; the adapter then rejects it. */
    private Map<?, ?> envelope(byte[] bytes) {
        try {
            return mapper.readValue(McpMessages.utf8(bytes), new TypeRef<Object>() {})
                            instanceof Map<?, ?> map
                    ? map
                    : null;
        } catch (IOException | RuntimeException _) {
            return null;
        }
    }

    /** Whether a message's {@code _meta} names the 2026-07-28 revision. */
    private static boolean modern(Map<?, ?> envelope) {
        return envelope.get("params") instanceof Map<?, ?> params
                && params.get("_meta") instanceof Map<?, ?> meta
                && McpProtocol.MODERN_VERSION.equals(
                        meta.get(
                                io.modelcontextprotocol.modern.McpSchema.MetaKeys
                                        .PROTOCOL_VERSION));
    }

    /** A request id comparable across its JSON integer forms, such as 1 and 1.0. */
    private static Object key(Object id) {
        return id instanceof Number number && number.doubleValue() == number.longValue()
                ? (Object) number.longValue()
                : id;
    }

    /**
     * Ends the in-flight request a {@code notifications/cancelled} names, if any.
     *
     * @return True if it named one.
     */
    private boolean cancel(Map<?, ?> envelope) {
        Object id =
                envelope.get("params") instanceof Map<?, ?> params ? params.get("requestId") : null;
        Request request = id == null ? null : inFlight.remove(key(id));
        if (request == null) {
            return false;
        }
        request.getAttributes().put(CANCELLED_ATTRIBUTE, Boolean.TRUE);
        request.abort();
        return true;
    }

    private static boolean isCancelled(Request request) {
        return Boolean.TRUE.equals(request.getAttributes().get(CANCELLED_ATTRIBUTE));
    }

    /** Answers a 2026-07-28 request refused, cut short or failed, unless it was cancelled. */
    private void refuse(Object id, Throwable cause) {
        getLogger().log(Level.FINE, "MCP STDIO request refused or failed", cause);
        if (id == null) {
            return;
        }
        try {
            McpMessages.writeFrame(
                    stream,
                    McpMessages.encode(
                            mapper,
                            McpMessages.error(id, -32603, "MCP exchange unavailable"),
                            McpProtocol.DEFAULT_MAX_MESSAGE_BYTES));
        } catch (IOException | RuntimeException e) {
            fail(e);
        }
    }

    private void fail(Throwable cause) {
        if (stopped) {
            return; // Stopping cuts exchanges short: not a failure.
        }
        failure = cause;
        closed = true;
        getLogger().log(Level.WARNING, "MCP STDIO server input/exchange failed", cause);
        requests.forEach(Request::abort);
        try {
            stream.stop();
        } catch (IOException e) {
            cause.addSuppressed(e);
            getLogger().log(Level.WARNING, "Unable to stop failed MCP STDIO stream", e);
        }
    }

    private void dispatch(Request request) throws IOException {
        if (closed) {
            throw new IOException("MCP STDIO server is closed", failure);
        }
        Response response = new Response(request);
        requests.add(request);
        try {
            if (closed) {
                request.abort();
                throw new IOException("MCP STDIO server is closed", failure);
            }
            handle(request, response);
            Representation entity = response.getEntity();
            if (isCancelled(request)) {
                return; // A cancelled request gets no response.
            }
            if (entity != null) {
                if (MediaType.TEXT_EVENT_STREAM.isCompatible(entity.getMediaType())) {
                    try (var frames = new EventFrames(request)) {
                        entity.write(frames);
                    }
                } else if (MediaType.APPLICATION_JSON.isCompatible(entity.getMediaType())) {
                    try (var input = entity.getStream()) {
                        McpMessages.writeFrame(
                                stream,
                                McpMessages.read(input, McpProtocol.DEFAULT_MAX_MESSAGE_BYTES));
                    }
                } else {
                    throw new IOException("MCP STDIO handler returned a non-JSON entity");
                }
            } else if (!response.getStatus().isSuccess()) {
                throw new IOException(
                        "MCP STDIO handler rejected exchange: " + response.getStatus());
            }
        } finally {
            requests.remove(request);
            if (response.getEntity() != null) {
                response.getEntity().release();
            }
        }
    }

    /**
     * Writes the data of each event of a server-sent event stream as one line, as it ends.
     * Comments, such as heartbeats, are dropped, and so is what follows a cancellation.
     */
    private final class EventFrames extends OutputStream {
        private final Request request;
        private final ByteArrayOutputStream line = new ByteArrayOutputStream();
        private final ByteArrayOutputStream data = new ByteArrayOutputStream();
        private boolean hasData;

        /**
         * @param request The request whose stream this is, which may be cancelled meanwhile.
         */
        private EventFrames(Request request) {
            this.request = request;
        }

        @Override
        public void write(int b) throws IOException {
            if (b == '\n') {
                endLine();
            } else if (line.size() > McpProtocol.DEFAULT_MAX_MESSAGE_BYTES + EVENT_LINE_OVERHEAD) {
                throw new IOException("MCP streamed message exceeds the message size");
            } else {
                line.write(b);
            }
        }

        @Override
        public void write(byte[] bytes, int offset, int length) throws IOException {
            for (int i = offset; i < offset + length; i++) {
                write(bytes[i]);
            }
        }

        private void endLine() throws IOException {
            byte[] bytes = line.toByteArray();
            line.reset();
            int length = bytes.length;
            if (length > 0 && bytes[length - 1] == '\r') {
                length--;
            }
            if (length == 0) {
                // A cancelled request's stream ends without the error that ends it.
                if (hasData && !isCancelled(request)) {
                    McpMessages.writeFrame(stream, data.toByteArray());
                }
                data.reset();
                hasData = false;
                return;
            }
            String text = new String(bytes, 0, length, StandardCharsets.UTF_8);
            if (text.startsWith("data:")) {
                String value = text.substring(text.startsWith("data: ") ? 6 : 5);
                byte[] value8 = value.getBytes(StandardCharsets.UTF_8);
                if (data.size() + value8.length + 1 > McpProtocol.DEFAULT_MAX_MESSAGE_BYTES) {
                    throw new IOException("MCP streamed message exceeds the message size");
                }
                if (hasData) {
                    data.write('\n');
                }
                data.writeBytes(value8);
                hasData = true;
            }
        }
    }

    /**
     * Terminal input/worker failure, if any; connector owners should monitor this for subprocesses.
     */
    public Throwable getFailure() {
        return failure;
    }

    @Override
    public synchronized void stop() throws Exception {
        stopped = true;
        closed = true;
        requests.forEach(Request::abort);
        Exception error = null;
        try {
            if (stream != null) {
                stream.stop();
            }
        } catch (IOException e) {
            error = e;
        }
        for (McpWorkers pool : new McpWorkers[] {workers, modernWorkers, listeners}) {
            try {
                if (pool != null) {
                    pool.close();
                }
            } catch (IOException e) {
                if (error == null) {
                    error = e;
                } else {
                    error.addSuppressed(e);
                }
            }
        }
        if (reader != null && !reader.join(Duration.ofSeconds(30))) {
            IOException join = new IOException("MCP STDIO server reader shutdown timed out");
            if (error == null) {
                error = join;
            } else {
                error.addSuppressed(join);
            }
        }
        super.stop();
        if (error != null) {
            throw error;
        }
    }
}
