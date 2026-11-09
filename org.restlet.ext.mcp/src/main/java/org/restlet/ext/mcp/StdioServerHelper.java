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
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
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
import org.restlet.routing.Filter;

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

    /**
     * Held while a frame is written, and while a request is marked cancelled: once a cancellation
     * is marked, no frame of that request follows.
     */
    private final Object output = new Object();

    private org.restlet.util.StreamTransport stream;
    private McpWorkers workers;

    /**
     * The workers of 2026-07-28 requests, without limits of their own: the adapter's call and
     * listen limits and request timeout apply, and what it refuses or cuts short is answered rather
     * than failing the connector.
     */
    private McpWorkers modernWorkers;

    /** The pending slots of 2026-07-28 requests, which bound their workers and output. */
    private Semaphore pending;

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
        modernWorkers = new McpWorkers(Integer.MAX_VALUE, null, Duration.ofSeconds(30));
        pending = new Semaphore(pendingLimit());
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
        boolean modern = modern(envelope);
        String method = envelope.get("method") instanceof String m ? m : null;
        // A cancellation of an in-flight 2026-07-28 request, with or without _meta. A request
        // naming this method, or a malformed envelope, goes on to be answered.
        if (isCancellation(envelope, method) && (cancel(envelope) || modern)) {
            return;
        }
        Request request = request(bytes, modern);
        if (modern) {
            submitModern(request, envelope, method);
        } else {
            workers.submit(() -> dispatch(request, null), () -> {})
                    .subscribe(ignored -> {}, this::fail);
        }
    }

    /** Whether a message is a {@code notifications/cancelled} notification. */
    private static boolean isCancellation(Map<?, ?> envelope, String method) {
        return CANCELLED.equals(method)
                && "2.0".equals(envelope.get("jsonrpc"))
                && !envelope.containsKey("id");
    }

    /** The request of a frame, with the headers it would have over HTTP. */
    private static Request request(byte[] bytes, boolean modern) {
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
        return request;
    }

    /**
     * Hands a 2026-07-28 request to a worker if one of the pending slots is free, else refuses it
     * at once: the reader keeps reading, cancellations included, and a client that stops reading
     * the output stops being read in turn, when the refusal cannot be written.
     */
    private void submitModern(Request request, Map<?, ?> envelope, String method) {
        if (method != null) {
            request.getHeaders().set("Mcp-Method", method);
        }
        request.getAttributes().put(McpModernExchange.STDIO_ATTRIBUTE, Boolean.TRUE);
        boolean answered = envelope.containsKey("id");
        Object id = validId(envelope.get("id"));
        if (!pending.tryAcquire()) {
            refuse(request, answered, id, new IOException("Too many pending MCP STDIO requests"));
            return;
        }
        Object key = id == null ? null : key(id);
        if (key != null && inFlight.putIfAbsent(key, request) != null) {
            key = null; // A duplicate id: only the first may be cancelled.
        }
        Object registered = key;
        AtomicBoolean held = new AtomicBoolean(true);
        // Freed once the response, or the error answering a failure, is written.
        Runnable done =
                () -> {
                    if (registered != null) {
                        inFlight.remove(registered, request);
                    }
                    if (held.compareAndSet(true, false)) {
                        pending.release();
                    }
                };
        modernWorkers
                .submit(() -> dispatch(request, id), () -> {})
                .subscribe(
                        null,
                        error -> {
                            try {
                                if (!closed && !isCancelled(request)) {
                                    // Refused, too large or broken: answered, the connector stays.
                                    refuse(request, answered, id, error);
                                }
                            } finally {
                                done.run();
                            }
                        },
                        done);
    }

    /** A request id as JSON-RPC allows it, a string or a number; else null. */
    private static Object validId(Object id) {
        return id instanceof String || id instanceof Number ? id : null;
    }

    /**
     * How many 2026-07-28 requests may be pending at once, writing or waiting to write: the open
     * listen streams the adapter allows, and as many other requests as the default call limit.
     */
    private int pendingLimit() {
        Restlet next = getHelped() == null ? null : getHelped().getNext();
        while (next instanceof Filter filter) {
            next = filter.getNext();
        }
        int listens =
                next instanceof McpServerAdapter adapter
                        ? adapter.getMaxListenStreams()
                        : McpProtocol.DEFAULT_MAX_LISTEN_STREAMS;
        return listens + McpProtocol.DEFAULT_MAX_CONCURRENT_CALLS;
    }

    /** The JSON object of a frame, or an empty map when it isn't one; the adapter rejects it. */
    private Map<?, ?> envelope(byte[] bytes) {
        try {
            return mapper.readValue(McpMessages.utf8(bytes), new TypeRef<Object>() {})
                            instanceof Map<?, ?> map
                    ? map
                    : Map.of();
        } catch (IOException | RuntimeException _) {
            return Map.of();
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
        synchronized (output) {
            request.getAttributes().put(CANCELLED_ATTRIBUTE, Boolean.TRUE);
        }
        request.abort();
        return true;
    }

    private static boolean isCancelled(Request request) {
        return Boolean.TRUE.equals(request.getAttributes().get(CANCELLED_ATTRIBUTE));
    }

    /**
     * Writes a frame of a request, unless the client cancelled it.
     *
     * @param request The request answered, or null for none.
     * @param frame The frame, without its newline.
     */
    private void emit(Request request, byte[] frame) throws IOException {
        synchronized (output) {
            if (request == null || !isCancelled(request)) {
                McpMessages.writeFrame(stream, frame);
            }
        }
    }

    /**
     * Answers a 2026-07-28 request refused, cut short or failed, unless it was cancelled.
     *
     * @param request The request.
     * @param answered Whether it is a request, which gets an answer, rather than a notification.
     * @param id Its valid id, or null to answer with a null id.
     * @param cause Why it is refused.
     */
    private void refuse(Request request, boolean answered, Object id, Throwable cause) {
        getLogger().log(Level.FINE, "MCP STDIO request refused or failed", cause);
        if (!answered) {
            return;
        }
        try {
            emit(
                    request,
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

    /**
     * Handles a request and writes its response.
     *
     * @param request The request.
     * @param id The id of a 2026-07-28 request, given to an error the adapter answered without it,
     *     having refused the request unread; or null.
     */
    private void dispatch(Request request, Object id) throws IOException {
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
                        emit(
                                request,
                                withId(
                                        McpMessages.read(
                                                input, McpProtocol.DEFAULT_MAX_MESSAGE_BYTES),
                                        id));
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

    /** A reply, given the request id when it is an error without one. */
    private byte[] withId(byte[] reply, Object id) throws IOException {
        if (id == null) {
            return reply;
        }
        Map<?, ?> envelope = envelope(reply);
        if (envelope.get("id") != null || !(envelope.get("error") instanceof Map<?, ?> error)) {
            return reply;
        }
        Map<String, Object> answered =
                McpMessages.error(
                        id,
                        error.get("code") instanceof Number code ? code.intValue() : -32603,
                        String.valueOf(error.get("message")));
        return McpMessages.encode(mapper, answered, McpProtocol.DEFAULT_MAX_MESSAGE_BYTES);
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
                endEvent();
                return;
            }
            String text = new String(bytes, 0, length, StandardCharsets.UTF_8);
            if (text.startsWith("data:")) {
                addData(text.substring(text.startsWith("data: ") ? 6 : 5));
            }
        }

        /** Writes the event that ends, if it has data; a cancelled request's is dropped. */
        private void endEvent() throws IOException {
            if (hasData) {
                emit(request, data.toByteArray());
            }
            data.reset();
            hasData = false;
        }

        /** Adds a data line to the event, joined by a newline, within the message size. */
        private void addData(String value) throws IOException {
            byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
            int separator = hasData ? 1 : 0;
            if (data.size() + separator + bytes.length > McpProtocol.DEFAULT_MAX_MESSAGE_BYTES) {
                throw new IOException("MCP streamed message exceeds the message size");
            }
            if (hasData) {
                data.write('\n');
            }
            data.writeBytes(bytes);
            hasData = true;
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
        Exception error =
                attempt(
                        null,
                        () -> {
                            if (stream != null) {
                                stream.stop();
                            }
                        });
        for (McpWorkers pool : new McpWorkers[] {workers, modernWorkers}) {
            error =
                    attempt(
                            error,
                            () -> {
                                if (pool != null) {
                                    pool.close();
                                }
                            });
        }
        boolean joined = reader == null || reader.join(Duration.ofSeconds(30));
        error =
                attempt(
                        error,
                        () -> {
                            if (!joined) {
                                throw new IOException("MCP STDIO server reader shutdown timed out");
                            }
                        });
        super.stop();
        if (error != null) {
            throw error;
        }
    }

    /** A shutdown step. */
    @FunctionalInterface
    private interface Step {
        void run() throws IOException;
    }

    /**
     * Runs a shutdown step, keeping the first error and suppressing the next ones in it.
     *
     * @param error The error so far, or null.
     * @param step The step.
     * @return The error so far, after the step.
     */
    private static Exception attempt(Exception error, Step step) {
        try {
            step.run();
            return error;
        } catch (IOException e) {
            if (error == null) {
                return e;
            }
            error.addSuppressed(e);
            return error;
        }
    }
}
