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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
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
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.engine.connector.ServerHelper;
import org.restlet.ext.mcp.internal.McpMessages;
import org.restlet.ext.mcp.internal.McpModernExchange;
import org.restlet.ext.mcp.internal.McpWorkers;
import org.restlet.representation.ByteArrayRepresentation;
import org.restlet.representation.Representation;
import org.restlet.routing.Filter;
import org.restlet.util.Series;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.core.ObjectReadContext;
import tools.jackson.core.json.JsonFactory;

/**
 * Explicit newline-framed MCP STDIO connector. Every message traverses the Server's configured
 * Restlet chain. The owner must supply a StreamTransport; stdout is exclusively protocol output.
 *
 * <p>A message whose {@code _meta} names MCP {@link McpProtocol#MODERN_VERSION} goes to the
 * adapter's 2026-07-28 server: the messages it streams, such as progress, are written as separate
 * lines as they come, a {@code subscriptions/listen} stream stays open without time limit, and a
 * {@code notifications/cancelled} naming an in-flight request ends it without a response.
 *
 * <p>A request whose {@code _meta} names another protocol version than 2026-07-28 and 2025-11-25,
 * newer, older or malformed, is answered with {@code UnsupportedProtocolVersionError} (-32022),
 * listing the versions served, as over HTTP; a notification naming one is dropped. A message
 * without a {@code _meta} protocol version, or naming 2025-11-25, goes to the 2025-11-25 edge.
 *
 * <p>Its limits are those of the {@link McpServerAdapter} it serves, read when the connector
 * starts: {@link McpServerAdapter#getMaxMessageBytes()}, {@link
 * McpServerAdapter#getMaxConcurrentCalls()}, {@link McpServerAdapter#getRequestTimeout()} and
 * {@link McpServerAdapter#getMaxListenStreams()}. Serving another Restlet, it takes them from the
 * {@link Server}'s parameters of the same names, {@code requestTimeout} in milliseconds, with the
 * same defaults; given those parameters as well, an adapter refuses to start. A message over the
 * size limit is skipped and answered with an invalid request error (-32600), under its id when its
 * first bytes give it; a request the limits refuse or cut short, or whose response or streamed
 * message exceeds the size limit, is answered with an internal error (-32603), unless it was
 * answered already. Either way the session goes on.
 */
public final class StdioServerHelper extends ServerHelper {
    private static final String CANCELLED = "notifications/cancelled";

    /** Request attribute marking a request the client cancelled, which gets no response. */
    private static final String CANCELLED_ATTRIBUTE = "org.restlet.ext.mcp.stdio.cancelled";

    /** Request attribute marking a request whose response was written, which gets no other. */
    private static final String ANSWERED_ATTRIBUTE = "org.restlet.ext.mcp.stdio.answered";

    /** Room for the {@code data: } prefix of a streamed message's line. */
    private static final int EVENT_LINE_OVERHEAD = 16;

    /** The bytes of a message over the size limit kept to read its id. */
    private static final int OVERSIZED_PREFIX = 64 * 1024;

    /** The parameters giving the limits when the connector serves no adapter. */
    private static final List<String> LIMIT_PARAMETERS =
            List.of("maxMessageBytes", "maxConcurrentCalls", "requestTimeout", "maxListenStreams");

    private static final JsonFactory JSON = new JsonFactory();

    private static final byte NEWLINE = (byte) 10;

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

    /** The size limit of a message, read or written. */
    private int maxMessageBytes = McpProtocol.DEFAULT_MAX_MESSAGE_BYTES;

    /**
     * The workers of 2026-07-28 requests, without limits of their own: the adapter's call and
     * listen limits and request timeout apply, and what it refuses or cuts short is answered rather
     * than failing the connector.
     */
    private McpWorkers modernWorkers;

    /** The pending slots of 2026-07-28 requests, which bound their workers and output. */
    private Semaphore pending;

    /**
     * The pending slots of 2025-11-25 requests, running or waiting to be refused, as many as the
     * call limit.
     */
    private Semaphore legacyPending;

    private volatile Thread reader;
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
        Limits limits = limits();
        stream = configured;
        maxMessageBytes = limits.maxMessageBytes();
        stream.setMaxOperationBytes(maxMessageBytes + 1);
        stream.start();
        workers = new McpWorkers(limits.maxConcurrentCalls(), limits.requestTimeout());
        modernWorkers = new McpWorkers(Integer.MAX_VALUE, null, Duration.ofSeconds(30));
        legacyPending = new Semaphore(limits.maxConcurrentCalls());
        pending =
                new Semaphore(
                        (int)
                                Math.min(
                                        (long) limits.maxListenStreams()
                                                + limits.maxConcurrentCalls(),
                                        Integer.MAX_VALUE));
        reader = Thread.ofVirtual().name("restlet-mcp-stdio-server").start(this::read);
    }

    private void read() {
        try {
            Frame frame;
            while (!closed && (frame = readFrame()) != null) {
                if (frame.oversized()) {
                    refuseOversized(frame.bytes());
                } else {
                    accept(frame.bytes());
                }
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
        String version = version(envelope);
        boolean modern = McpProtocol.MODERN_VERSION.equals(version);
        String method = envelope.get("method") instanceof String m ? m : null;
        // A cancellation of an in-flight 2026-07-28 request, with or without _meta. A request
        // naming this method, or a malformed envelope, goes on to be answered.
        if (isCancellation(envelope, method) && (cancel(envelope) || modern)) {
            return;
        }
        if (version != null && !modern && !McpProtocol.VERSION.equals(version)) {
            if (!envelope.containsKey("id")) {
                // A notification of an unsupported version: not answered, as JSON-RPC wants.
                getLogger().log(Level.FINE, "MCP STDIO notification of an unsupported version");
                return;
            }
        } else if (!modern) {
            version = McpProtocol.VERSION;
        }
        // As over HTTP, the adapter answers a version it doesn't serve with -32022.
        Request request = request(bytes, version);
        if (modern) {
            submitModern(request, envelope, method);
        } else {
            submitLegacy(request, envelope);
        }
    }

    /**
     * Hands a 2025-11-25 request to a worker if one of the pending slots is free, else refuses it
     * on the reader, which waits for the output as for any answer: a client that stops reading
     * stops being read. A slot is held until the request ends or its refusal is written, so the
     * requests cut short while the output is held, and waiting to be refused, are bounded too.
     */
    private void submitLegacy(Request request, Map<?, ?> envelope) {
        boolean answered = envelope.containsKey("id");
        Object id = validId(envelope.get("id"));
        if (!legacyPending.tryAcquire()) {
            refuse(request, answered, id, new IOException("Too many pending MCP STDIO requests"));
            return;
        }
        AtomicBoolean held = new AtomicBoolean(true);
        Runnable done =
                () -> {
                    if (held.compareAndSet(true, false)) {
                        legacyPending.release();
                    }
                };
        workers.submit(() -> dispatch(request, null), request::abort)
                .subscribe(
                        null,
                        error -> {
                            if (closed) {
                                done.run();
                                return;
                            }
                            // Over the call limit, too slow or too large: answered. Refused at
                            // admission, on the reader; else on a thread of its own, not a shared
                            // timer's or a worker's, as the output may be held by a slow write.
                            Runnable refusal =
                                    () -> {
                                        try {
                                            refuse(request, answered, id, error);
                                        } finally {
                                            done.run();
                                        }
                                    };
                            if (Thread.currentThread() == reader) {
                                refusal.run();
                            } else {
                                Thread.ofVirtual().name("restlet-mcp-stdio-refusal").start(refusal);
                            }
                        },
                        done);
    }

    /**
     * A frame read, or the first bytes of one over the size limit, the rest skipped. Not a record:
     * its bytes are handed over, never compared.
     */
    private static final class Frame {
        private final byte[] bytes;
        private final boolean oversized;

        private Frame(byte[] bytes, boolean oversized) {
            this.bytes = bytes;
            this.oversized = oversized;
        }

        private byte[] bytes() {
            return bytes;
        }

        private boolean oversized() {
            return oversized;
        }
    }

    /**
     * Reads the next frame. One over the size limit is read to its end, keeping its first bytes.
     *
     * @return The frame, or null at the end of the input.
     */
    private Frame readFrame() throws IOException {
        // Grown as needed up to the limit; past it, only the first bytes are kept.
        byte[] frame = new byte[Math.min(maxMessageBytes, 8192)];
        byte[] prefix = null;
        byte[] single = new byte[1];
        long size = 0;
        while (stream.read(single, 0, 1) != -1) {
            if (single[0] == NEWLINE) {
                return prefix == null
                        ? new Frame(Arrays.copyOf(frame, (int) size), false)
                        : new Frame(prefix, true);
            }
            if (prefix == null) {
                if (size == maxMessageBytes) {
                    prefix = Arrays.copyOf(frame, Math.min(maxMessageBytes, OVERSIZED_PREFIX));
                    frame = null;
                } else {
                    if (size == frame.length) {
                        frame =
                                Arrays.copyOf(
                                        frame, (int) Math.min(2L * frame.length, maxMessageBytes));
                    }
                    frame[(int) size] = single[0];
                }
            }
            size++;
        }
        if (size != 0) {
            throw new IOException("Truncated MCP STDIO frame");
        }
        return null;
    }

    /** Answers a message over the size limit, under its id when its first bytes give it. */
    private void refuseOversized(byte[] prefix) {
        getLogger().log(Level.FINE, "MCP STDIO message over {0} bytes skipped", maxMessageBytes);
        try {
            emit(
                    null,
                    McpMessages.encode(
                            mapper,
                            McpMessages.error(
                                    idOf(prefix),
                                    -32600,
                                    "MCP message exceeds " + maxMessageBytes + " bytes"),
                            maxMessageBytes),
                    true);
        } catch (IOException | RuntimeException e) {
            fail(e);
        }
    }

    /** The id of the JSON-RPC object a message starts with, if it comes before its cut. */
    private static Object idOf(byte[] prefix) {
        try (JsonParser parser = JSON.createParser(ObjectReadContext.empty(), prefix)) {
            if (parser.nextToken() != JsonToken.START_OBJECT) {
                return null;
            }
            while (parser.nextToken() == JsonToken.PROPERTY_NAME) {
                String name = parser.currentName();
                JsonToken value = parser.nextToken();
                if ("id".equals(name)) {
                    return switch (value) {
                        case VALUE_STRING -> parser.getString();
                        case VALUE_NUMBER_INT, VALUE_NUMBER_FLOAT -> parser.getNumberValue();
                        default -> null;
                    };
                }
                parser.skipChildren();
            }
        } catch (JacksonException e) {
            // Cut short before its id, or not JSON.
        }
        return null;
    }

    /** Whether a message is a {@code notifications/cancelled} notification. */
    private static boolean isCancellation(Map<?, ?> envelope, String method) {
        return CANCELLED.equals(method)
                && "2.0".equals(envelope.get("jsonrpc"))
                && !envelope.containsKey("id");
    }

    /** The request of a frame, with the headers it would have over HTTP. */
    private static Request request(byte[] bytes, String version) {
        Request request =
                new Request(
                        Method.POST,
                        "stdio://localhost/mcp",
                        new ByteArrayRepresentation(bytes, MediaType.APPLICATION_JSON));
        request.getHeaders().set(McpProtocol.VERSION_HEADER, version);
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
        Object key = id == null ? null : McpMessages.idKey(id);
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
     * The limits of the connector. The 2026-07-28 requests pending at once, writing or waiting to
     * write, are as many as the open listen streams allowed and the call limit.
     */
    private record Limits(
            int maxMessageBytes,
            int maxConcurrentCalls,
            Duration requestTimeout,
            int maxListenStreams) {}

    /**
     * The limits of the adapter served, or else of the connector's parameters.
     *
     * @throws IllegalStateException If the parameters are given while an adapter is served.
     * @throws IllegalArgumentException If a parameter isn't a valid limit.
     */
    private Limits limits() {
        Restlet next = getHelped() == null ? null : getHelped().getNext();
        while (next instanceof Filter filter) {
            next = filter.getNext();
        }
        Series<Parameter> parameters =
                getHelped() == null ? new Series<>(Parameter.class) : getHelpedParameters();
        if (next instanceof McpServerAdapter adapter) {
            for (String name : LIMIT_PARAMETERS) {
                if (parameters.getFirst(name) != null) {
                    throw new IllegalStateException(
                            "The MCP STDIO "
                                    + name
                                    + " is the McpServerAdapter's, not a connector parameter");
                }
            }
            return new Limits(
                    adapter.getMaxMessageBytes(),
                    adapter.getMaxConcurrentCalls(),
                    adapter.getRequestTimeout(),
                    adapter.getMaxListenStreams());
        }
        int bytes = limit(parameters, "maxMessageBytes", maxMessageBytes);
        if (bytes == Integer.MAX_VALUE) {
            throw new IllegalArgumentException("maxMessageBytes must be below Integer.MAX_VALUE");
        }
        return new Limits(
                bytes,
                limit(parameters, "maxConcurrentCalls", McpProtocol.DEFAULT_MAX_CONCURRENT_CALLS),
                Duration.ofMillis(limit(parameters, "requestTimeout", 30_000)),
                limit(parameters, "maxListenStreams", McpProtocol.DEFAULT_MAX_LISTEN_STREAMS));
    }

    /** A positive int parameter, or its default. */
    private static int limit(Series<Parameter> parameters, String name, int defaultValue) {
        String value = parameters.getFirstValue(name);
        if (value == null) {
            return defaultValue;
        }
        int parsed;
        try {
            parsed = Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "The MCP STDIO " + name + " isn't a number: " + value, e);
        }
        if (parsed <= 0) {
            throw new IllegalArgumentException(
                    "The MCP STDIO " + name + " must be positive: " + value);
        }
        return parsed;
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

    /**
     * The protocol version a message's {@code _meta} names, as its JSON text when it isn't a
     * string, or null when it names none.
     */
    private String version(Map<?, ?> envelope) {
        if (!(envelope.get("params") instanceof Map<?, ?> params
                && params.get("_meta") instanceof Map<?, ?> meta)) {
            return null;
        }
        Object version =
                meta.get(io.modelcontextprotocol.modern.McpSchema.MetaKeys.PROTOCOL_VERSION);
        if (version == null || version instanceof String) {
            return (String) version;
        }
        try {
            return mapper.writeValueAsString(version);
        } catch (IOException | RuntimeException _) {
            return String.valueOf(version);
        }
    }

    /**
     * Ends the in-flight request a {@code notifications/cancelled} names, if any.
     *
     * @return True if it named one.
     */
    private boolean cancel(Map<?, ?> envelope) {
        Object id =
                envelope.get("params") instanceof Map<?, ?> params ? params.get("requestId") : null;
        Request request = id == null ? null : inFlight.remove(McpMessages.idKey(id));
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
     * Writes a frame of a request, unless the client cancelled it or it was answered.
     *
     * @param request The request answered, or null for none.
     * @param frame The frame, without its newline.
     * @param last Whether it is the request's response, after which nothing of it is written.
     */
    private void emit(Request request, byte[] frame, boolean last) throws IOException {
        synchronized (output) {
            if (request == null) {
                writeFrame(frame);
            } else if (!isCancelled(request) && !isAnswered(request)) {
                writeFrame(frame);
                if (last) {
                    request.getAttributes().put(ANSWERED_ATTRIBUTE, Boolean.TRUE);
                }
            }
        }
    }

    /**
     * Writes a frame whole, held by the output lock. The write runs on a thread of its own, which
     * the caller waits for without being interruptible: a worker cut short by its timeout is
     * interrupted, and an interruptible channel would close half-way through its frame.
     */
    private void writeFrame(byte[] frame) throws IOException {
        CompletableFuture<Void> written = new CompletableFuture<>();
        Thread.ofVirtual()
                .name("restlet-mcp-stdio-writer")
                .start(
                        () -> {
                            try {
                                McpMessages.writeFrame(stream, frame);
                                written.complete(null);
                            } catch (Throwable e) {
                                written.completeExceptionally(e);
                            }
                        });
        try {
            written.join();
        } catch (CompletionException e) {
            if (e.getCause() instanceof IOException cause) {
                throw cause;
            }
            if (e.getCause() instanceof RuntimeException cause) {
                throw cause;
            }
            throw new IOException("MCP STDIO frame not written", e.getCause());
        }
    }

    private static boolean isAnswered(Request request) {
        return Boolean.TRUE.equals(request.getAttributes().get(ANSWERED_ATTRIBUTE));
    }

    /** Whether a streamed message is a response, carrying a result or an error. */
    private static boolean isResponse(byte[] message) {
        try (JsonParser parser = JSON.createParser(ObjectReadContext.empty(), message)) {
            if (parser.nextToken() != JsonToken.START_OBJECT) {
                return false;
            }
            while (parser.nextToken() == JsonToken.PROPERTY_NAME) {
                String name = parser.currentName();
                if ("result".equals(name) || "error".equals(name)) {
                    return true;
                }
                if ("method".equals(name)) {
                    return false;
                }
                parser.nextToken();
                parser.skipChildren();
            }
        } catch (JacksonException e) {
            // Not JSON: not a response.
        }
        return false;
    }

    /**
     * Answers a request refused, cut short or failed, unless it was cancelled; nothing of it is
     * written after.
     *
     * @param request The request.
     * @param answered Whether it is a request, which gets an answer, rather than a notification.
     * @param id Its valid id, or null to answer with a null id.
     * @param cause Why it is refused.
     */
    private void refuse(Request request, boolean answered, Object id, Throwable cause) {
        getLogger().log(Level.FINE, "MCP STDIO request refused or failed", cause);
        try {
            byte[] frame =
                    McpMessages.encode(
                            mapper,
                            McpMessages.error(id, -32603, "MCP exchange unavailable"),
                            maxMessageBytes);
            synchronized (output) {
                if (isCancelled(request) || isAnswered(request)) {
                    return; // Cancelled, or answered just before being cut short.
                }
                // A worker cut short may still be answering: its answer is dropped.
                request.getAttributes().put(CANCELLED_ATTRIBUTE, Boolean.TRUE);
                if (answered) {
                    writeFrame(frame);
                }
            }
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
                        emit(request, withId(McpMessages.read(input, maxMessageBytes), id), true);
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
        return McpMessages.encode(mapper, answered, maxMessageBytes);
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
            } else if (line.size() > (long) maxMessageBytes + EVENT_LINE_OVERHEAD) {
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
                byte[] message = data.toByteArray();
                emit(request, message, isResponse(message));
            }
            data.reset();
            hasData = false;
        }

        /** Adds a data line to the event, joined by a newline, within the message size. */
        private void addData(String value) throws IOException {
            byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
            int separator = hasData ? 1 : 0;
            if ((long) data.size() + separator + bytes.length > maxMessageBytes) {
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
