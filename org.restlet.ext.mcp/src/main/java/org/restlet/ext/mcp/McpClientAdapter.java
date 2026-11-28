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
import io.modelcontextprotocol.modern.McpSchema;
import io.modelcontextprotocol.modern.client.McpAsyncClient;
import io.modelcontextprotocol.modern.client.McpAsyncElicitationHandler;
import io.modelcontextprotocol.modern.client.McpClient;
import io.modelcontextprotocol.modern.client.McpTimeoutException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import org.restlet.Client;
import org.restlet.Request;
import org.restlet.ext.mcp.internal.McpEras;
import org.restlet.ext.mcp.internal.McpHttpStatusException;
import org.restlet.ext.mcp.internal.McpLegacyEdge;
import org.restlet.ext.mcp.internal.McpModernHttpTransport;
import org.restlet.ext.mcp.internal.McpModernStdioTransport;
import org.restlet.ext.mcp.internal.McpServerErrorException;
import org.restlet.ext.mcp.internal.McpStdioChannel;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

/**
 * Lifecycle facade of an MCP client, on the SDK's {@code 2026-07-28} types. Borrowed Restlet
 * connectors are never stopped.
 *
 * <p>By default it speaks MCP {@link McpProtocol#VERSION} through the SDK's client, behind a legacy
 * edge. With {@link #setProtocolVersion(String)} set to {@link McpProtocol#MODERN_VERSION}, it
 * speaks {@code 2026-07-28} instead, experimentally, over HTTP or stdio: {@link #start()} discovers
 * the server, and {@link #getModernClient()}, the SDK's client over this adapter's connector or
 * stream, makes the requests, the task-augmented ones excepted, which {@link #getTasksClient()}
 * makes, over HTTP only for now.
 *
 * <p>With {@link McpProtocol#AUTO}, also experimental, it speaks whichever era the server does, as
 * the 2026-07-28 revision specifies for a dual-era client. Over HTTP, {@link #start()} sends {@code
 * server/discover}: a discovery result naming {@code 2026-07-28} selects the modern client; a
 * {@code 4xx} without a JSON-RPC response, or a JSON-RPC error other than an unsupported version,
 * marks a legacy server, which gets an {@code initialize} handshake offering {@link
 * McpProtocol#LEGACY_VERSIONS}. Other failures, such as a {@code 5xx} or a timeout, are thrown
 * without falling back. The era found is remembered for the endpoint URI, for the life of the JVM:
 * a later start skips the probe, and probes again only if the server contradicts that era, with a
 * {@code 4xx} or a JSON-RPC error that the other era would answer with, not with a timeout or a
 * {@code 5xx}. A started client keeps its era: requests are never replayed on the other one, so
 * when a server changes era, stop the client and start a new one.
 *
 * <p>Over stdio, whether its own stream or a {@code stdio:} connector's, {@link McpProtocol#AUTO}
 * probes the same way, within the request timeout, on the stream it then keeps using: a discovery
 * result naming {@code 2026-07-28}, or a recognized modern error, selects the modern client; an
 * unsupported version error offering a legacy revision, any other JSON-RPC error, or a timeout
 * selects the legacy one, whose {@code initialize} offers {@link McpProtocol#LEGACY_VERSIONS}. A
 * late answer to the probe is dropped. No era is remembered: the process at the other end of the
 * stream serves this client only, and is probed once per start.
 *
 * <p>With a legacy server, the client exposes nothing 2026-07-28 deprecates or removes: it declares
 * no roots, sampling or elicitation capability, never sets a logging level, refuses the requests a
 * server sends, and opens no listening {@code GET} stream. It keeps no session unless asked to,
 * through {@link #setLegacySessions(boolean)} or {@link #setLegacySessionId(String)}.
 *
 * <p>{@link #getNegotiatedVersion()} tells the revision in use. {@link #listTools(String)}, {@link
 * #callTool(String, Map)}, {@link #readResource(String)} and {@link #getPrompt(String, Map)} work
 * in both eras, on the modern types.
 */
public final class McpClientAdapter implements AutoCloseable {
    /** The default number of {@code input_required} rounds answered for one request. */
    public static final int DEFAULT_MAX_INPUT_ROUNDS = 8;

    /** JSON-RPC errors only a {@code 2026-07-28} server answers {@code server/discover} with. */
    private static final Set<Integer> MODERN_ERRORS =
            Set.of(
                    McpSchema.ErrorCodes.HEADER_MISMATCH,
                    McpSchema.ErrorCodes.MISSING_REQUIRED_CLIENT_CAPABILITY);

    private final Client client;
    private final String target;
    private final org.restlet.util.StreamTransport stream;
    private Duration requestTimeout = Duration.ofSeconds(30);
    private int maxMessageBytes = McpProtocol.DEFAULT_MAX_MESSAGE_BYTES;
    private int maxConcurrentCalls = McpProtocol.DEFAULT_MAX_CONCURRENT_CALLS;
    private Consumer<Request> requestCustomizer = request -> {};
    private String protocolVersion = McpProtocol.VERSION;
    private McpAsyncElicitationHandler<McpSchema.ElicitFormRequest> formElicitation;
    private McpAsyncElicitationHandler<McpSchema.ElicitUrlRequest> urlElicitation;
    private int maxInputRounds = DEFAULT_MAX_INPUT_ROUNDS;
    private McpJsonMapper jsonMapper = McpJsonDefaults.getMapper();
    private boolean legacySessions;
    private String legacySessionId;
    private McpStdioChannel ownChannel;
    private Modern modern;
    private McpLegacyEdge.Client legacy;
    private String negotiatedVersion;
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
     * client; {@link McpProtocol#MODERN_VERSION} through {@link #getModernClient()}, over HTTP or
     * stdio; or {@link McpProtocol#AUTO}, whichever the server speaks. Experimental for {@code
     * 2026-07-28} and {@code auto}.
     *
     * @param value The revision, or {@link McpProtocol#AUTO}.
     */
    public synchronized void setProtocolVersion(String value) {
        requireConfigurable();
        if (!McpProtocol.VERSION.equals(value)
                && !McpProtocol.MODERN_VERSION.equals(value)
                && !McpProtocol.AUTO.equals(value)) {
            throw new IllegalArgumentException("Unsupported MCP revision: " + value);
        }
        protocolVersion = value;
    }

    /**
     * The revision spoken with the server, once started: {@link McpProtocol#MODERN_VERSION}, or the
     * one negotiated by the last {@code initialize}, which a handshake after an expired legacy
     * session may change.
     *
     * @return The revision, or null before {@link #start()}.
     */
    public synchronized String getNegotiatedVersion() {
        if (!started) {
            return null;
        }
        return legacy != null ? legacy.negotiatedVersion() : negotiatedVersion;
    }

    public synchronized String getProtocolVersion() {
        return protocolVersion;
    }

    /**
     * Sets the JSON mapper of the {@link McpProtocol#MODERN_VERSION} client's messages, so that
     * they share the application's settings. The SDK's default, Jackson 3 based, otherwise.
     *
     * @param value The mapper.
     */
    public synchronized void setJsonMapper(McpJsonMapper value) {
        requireConfigurable();
        jsonMapper = Objects.requireNonNull(value, "jsonMapper");
    }

    public synchronized McpJsonMapper getJsonMapper() {
        return jsonMapper;
    }

    /**
     * Sets what answers the form elicitations of {@link McpProtocol#MODERN_VERSION} requests,
     * through {@code input_required} results, after which the request is retried. With a handler,
     * each request declares the form elicitation capability. Legacy servers ask for no input: the
     * legacy edge declares no elicitation capability.
     *
     * @param value The handler, or null for none.
     */
    public synchronized void setFormElicitation(
            McpAsyncElicitationHandler<McpSchema.ElicitFormRequest> value) {
        requireConfigurable();
        formElicitation = value;
    }

    public synchronized McpAsyncElicitationHandler<McpSchema.ElicitFormRequest>
            getFormElicitation() {
        return formElicitation;
    }

    /**
     * Sets what answers the URL elicitations of {@link McpProtocol#MODERN_VERSION} requests, as
     * {@link #setFormElicitation} does for forms.
     *
     * @param value The handler, or null for none.
     */
    public synchronized void setUrlElicitation(
            McpAsyncElicitationHandler<McpSchema.ElicitUrlRequest> value) {
        requireConfigurable();
        urlElicitation = value;
    }

    public synchronized McpAsyncElicitationHandler<McpSchema.ElicitUrlRequest> getUrlElicitation() {
        return urlElicitation;
    }

    /**
     * Sets how many {@code input_required} rounds the elicitation handlers answer for one request,
     * {@value #DEFAULT_MAX_INPUT_ROUNDS} by default. Past it, the SDK's client fails the request,
     * and {@link McpTasksClient} returns the result or task as it is.
     *
     * @param value The number of rounds, one or more.
     */
    public synchronized void setMaxInputRounds(int value) {
        requireConfigurable();
        if (value <= 0) {
            throw new IllegalArgumentException("Input rounds must be positive");
        }
        maxInputRounds = value;
    }

    public synchronized int getMaxInputRounds() {
        return maxInputRounds;
    }

    /**
     * Keeps the {@code Mcp-Session-Id} a legacy server returns on {@code initialize}, off by
     * default: the id is then sent with each later request, the client initializes once more when
     * the session expires, and it deletes the session when it stops. Without it, a legacy server
     * opening a session fails the handshake.
     *
     * <p><b>Legacy only.</b> MCP 2026-07-28 removed sessions: this property exists so the client
     * can reach legacy servers that require one, and goes away with the legacy edge. It has no
     * effect on modern servers. Even with a session, the client opens no listening {@code GET}
     * stream, does not resume with {@code Last-Event-ID}, and refuses the requests the server
     * sends, such as elicitation, sampling or roots, so servers depending on those will not work.
     *
     * @param value True to keep a legacy server's session.
     */
    public synchronized void setLegacySessions(boolean value) {
        requireConfigurable();
        legacySessions = value;
    }

    /**
     * Whether the session of a legacy server is kept.
     *
     * @return True if kept.
     * @see #setLegacySessions(boolean)
     */
    public synchronized boolean isLegacySessions() {
        return legacySessions;
    }

    /**
     * Sets a legacy server's {@code Mcp-Session-Id} the caller owns, such as one a proxy relays: it
     * is sent with each legacy request, from {@code initialize} on, but never replaced, never
     * deleted, and its expiry fails the request.
     *
     * <p><b>Legacy only</b>, with the same limits as {@link #setLegacySessions(boolean)}: MCP
     * 2026-07-28 removed sessions, and this property goes away with the legacy edge. It has no
     * effect on modern servers.
     *
     * @param value The session id, or null for none.
     */
    public synchronized void setLegacySessionId(String value) {
        requireConfigurable();
        legacySessionId = value;
    }

    /**
     * The {@code Mcp-Session-Id} sent to a legacy server: once started, the one in use, either the
     * caller's or the server's; before, the one the caller set. <b>Legacy only</b>: see {@link
     * #setLegacySessions(boolean)}.
     *
     * @return The session id, or null.
     */
    public synchronized String getLegacySessionId() {
        return legacy != null ? legacy.sessionId() : legacySessionId;
    }

    public synchronized void start() {
        if (started) {
            return;
        }
        requireConfigurable();
        configured = true;
        try {
            if (McpProtocol.MODERN_VERSION.equals(protocolVersion)) {
                startModern(createModern());
            } else if (McpProtocol.AUTO.equals(protocolVersion)) {
                startAuto();
            } else {
                startLegacy(List.of(McpProtocol.VERSION));
            }
        } catch (RuntimeException e) {
            try {
                closeOwnChannel();
            } catch (RuntimeException cleanup) {
                e.addSuppressed(cleanup);
            }
            throw e;
        }
    }

    /** Whether this client speaks over stdio: its own stream, or a {@code stdio:} connector. */
    private boolean isStdio() {
        return stream != null || target.startsWith("stdio:");
    }

    /**
     * The framing of the stdio stream: this adapter's own, or the connector's, which the clients of
     * either era attach to in turn.
     */
    private McpStdioChannel stdioChannel() {
        if (stream != null) {
            if (ownChannel == null) {
                ownChannel = new McpStdioChannel(stream);
            }
            return ownChannel;
        }
        if (!client.isStarted()
                || client.getContext() == null
                || !(client.getContext().getAttributes().get(StdioClientHelper.TRANSPORT_ATTRIBUTE)
                        instanceof StdioClientHelper helper)) {
            throw new IllegalStateException(
                    "A started explicitly selected MCP STDIO helper is required");
        }
        return helper.channel();
    }

    /** Stops this adapter's own stream, if it has one. */
    private void closeOwnChannel() {
        if (ownChannel != null) {
            try {
                ownChannel.close(requestTimeout);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            } finally {
                ownChannel = null;
            }
        }
    }

    /** Starts the legacy client: an {@code initialize} handshake offering these revisions. */
    private void startLegacy(List<String> versions) {
        legacy = createLegacy(versions);
        negotiatedVersion = legacy.negotiatedVersion();
        started = true;
    }

    /**
     * Starts the client of the era the server speaks: the one remembered for the endpoint, unless
     * the server contradicts it, else the one a {@code server/discover} probe tells.
     */
    private void startAuto() {
        if (isStdio()) {
            // One client per process or connector: there is no era to remember.
            if (probe(createModern())) {
                negotiatedVersion = McpProtocol.MODERN_VERSION;
                started = true;
            } else {
                startLegacy(McpProtocol.LEGACY_VERSIONS);
            }
            return;
        }
        String cached = McpEras.get(target);
        if (cached != null) {
            try {
                if (McpEras.MODERN.equals(cached)) {
                    startModern(createModern());
                } else {
                    startLegacy(McpProtocol.LEGACY_VERSIONS);
                }
                return;
            } catch (RuntimeException e) {
                // A timeout or a server error says nothing of the era: probing again would only
                // add requests, and might repeat a handshake that opened a session.
                if (!contradicts(cached, e)) {
                    throw e;
                }
                McpEras.remove(target);
            }
        }
        if (probe(createModern())) {
            negotiatedVersion = McpProtocol.MODERN_VERSION;
            started = true;
            McpEras.put(target, McpEras.MODERN);
        } else {
            startLegacy(McpProtocol.LEGACY_VERSIONS);
            McpEras.put(target, McpEras.LEGACY);
        }
    }

    /**
     * Whether a client's failed start contradicts the era remembered for the endpoint.
     *
     * @param era The era remembered.
     * @param failure The failure.
     * @return True if the endpoint should be probed again.
     */
    private static boolean contradicts(String era, RuntimeException failure) {
        if (McpEras.LEGACY.equals(era)) {
            return McpLegacyEdge.wrongEra(failure);
        }
        if (failure instanceof NotModern) {
            return true;
        }
        try {
            return isLegacy(Exceptions.unwrap(failure));
        } catch (IllegalStateException noSharedRevision) {
            return true;
        }
    }

    /** A discovery result not naming {@code 2026-07-28}. */
    private static final class NotModern extends IllegalStateException {
        private static final long serialVersionUID = 1L;

        private NotModern() {
            super("Peer does not support MCP 2026-07-28");
        }
    }

    /**
     * Probes the server with {@code server/discover}. Over stdio, where a legacy server may leave
     * the request unanswered, a timeout marks a legacy server too.
     *
     * @return True for a modern server, which the candidate then serves; false for a legacy one,
     *     for which the candidate is closed, and detached from the stdio stream.
     */
    private boolean probe(Modern candidate) {
        boolean kept = false;
        try {
            Mono<McpSchema.DiscoverResult> discover = candidate.client().discover();
            McpSchema.DiscoverResult discovered =
                    isStdio()
                            ? discover.timeout(requestTimeout).block()
                            : discover.block(requestTimeout);
            List<String> supported =
                    discovered == null || discovered.supportedVersions() == null
                            ? List.of()
                            : discovered.supportedVersions();
            kept = supported.contains(McpProtocol.MODERN_VERSION);
            if (!kept && !offersLegacy(supported)) {
                throw new IllegalStateException(
                        "No MCP revision supported by both the client and the peer: " + supported);
            }
            if (kept) {
                modern = candidate;
            }
            return kept;
        } catch (RuntimeException e) {
            Throwable cause = Exceptions.unwrap(e);
            if (!(isStdio() && isTimeout(cause)) && !isLegacy(cause)) {
                throw e;
            }
            return false;
        } finally {
            if (!kept) {
                try {
                    candidate.close();
                } catch (RuntimeException cleanup) {
                    // The probe exchange is over: its failure to close neither replaces the
                    // fallback nor the probe's own failure.
                }
            }
        }
    }

    /** Whether a failure is a timeout: the probe's, or the SDK's round timeout. */
    private static boolean isTimeout(Throwable failure) {
        return failure instanceof TimeoutException || failure instanceof McpTimeoutException;
    }

    /**
     * Whether a failed {@code server/discover} marks a legacy server: a JSON-RPC error that isn't a
     * recognized modern one and doesn't come with a {@code 5xx}, or a {@code 4xx} without a
     * response. A header mismatch or a missing client capability error comes from a modern server,
     * whose failure is kept. An unsupported version error comes from a modern server too, which is
     * legacy-compatible only if it offers a legacy revision.
     */
    private static boolean isLegacy(Throwable failure) {
        if (failure instanceof McpServerErrorException) {
            return false; // A server error says nothing of the era, whatever its JSON-RPC error.
        }
        if (failure instanceof io.modelcontextprotocol.modern.McpException mcp) {
            var error = mcp.error();
            if (error != null && error.code() != null && MODERN_ERRORS.contains(error.code())) {
                return false;
            }
            if (error != null
                    && error.code() != null
                    && error.code() == McpProtocol.UNSUPPORTED_PROTOCOL_VERSION) {
                List<String> supported = supportedVersions(error.data());
                if (offersLegacy(supported)) {
                    return true;
                }
                throw new IllegalStateException(
                        "No MCP revision supported by both the client and the peer: " + supported,
                        mcp);
            }
            return true;
        }
        return failure instanceof McpHttpStatusException status && status.isClientError();
    }

    private static List<String> supportedVersions(Object data) {
        if (data instanceof Map<?, ?> map && map.get("supported") instanceof List<?> list) {
            return list.stream().map(String::valueOf).toList();
        }
        return List.of();
    }

    private static boolean offersLegacy(List<String> supported) {
        return supported.stream().anyMatch(McpProtocol.LEGACY_VERSIONS::contains);
    }

    /**
     * The {@code 2026-07-28} clients over one transport: the SDK's and, over HTTP, the Tasks
     * extension's.
     */
    private record Modern(McpAsyncClient client, McpTasksClient tasks, AutoCloseable transport) {

        /** Ends the exchanges in flight and listen streams. */
        void close() {
            try {
                transport.close();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
    }

    private Modern createModern() {
        if (isStdio()) {
            McpModernStdioTransport transport;
            try {
                transport =
                        new McpModernStdioTransport(
                                stdioChannel(),
                                jsonMapper,
                                maxMessageBytes,
                                maxConcurrentCalls,
                                requestTimeout);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            return new Modern(modernBuilder(transport).build(), null, transport);
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
        McpModernHttpTransport transport =
                new McpModernHttpTransport(
                        client,
                        target,
                        jsonMapper,
                        maxMessageBytes,
                        maxConcurrentCalls,
                        requestTimeout,
                        requestCustomizer);
        return new Modern(
                modernBuilder(transport).build(),
                new McpTasksClient(
                        transport, jsonMapper, formElicitation, urlElicitation, maxInputRounds),
                transport::close);
    }

    /** The builder of the SDK's modern client over a transport, with this adapter's settings. */
    private McpClient.AsyncBuilder modernBuilder(
            io.modelcontextprotocol.modern.client.transport.McpClientTransport transport) {
        McpClient.AsyncBuilder builder =
                McpClient.async(transport)
                        .jsonMapper(jsonMapper)
                        .roundTimeout(requestTimeout)
                        .maxInputRounds(maxInputRounds);
        if (formElicitation != null) {
            builder.formElicitation(formElicitation);
        }
        if (urlElicitation != null) {
            builder.urlElicitation(urlElicitation);
        }
        return builder;
    }

    /** Starts a {@code 2026-07-28} client, and discovers the server's support of it. */
    private void startModern(Modern candidate) {
        try {
            McpSchema.DiscoverResult discovered =
                    candidate.client().discover().block(requestTimeout);
            if (discovered == null
                    || discovered.supportedVersions() == null
                    || !discovered.supportedVersions().contains(McpProtocol.MODERN_VERSION)) {
                throw new NotModern();
            }
        } catch (RuntimeException e) {
            try {
                candidate.close();
            } catch (RuntimeException cleanup) {
                e.addSuppressed(cleanup);
            }
            throw e;
        }
        modern = candidate;
        negotiatedVersion = McpProtocol.MODERN_VERSION;
        started = true;
    }

    /**
     * The SDK's {@link McpProtocol#MODERN_VERSION} client, once that is the revision spoken. It
     * sends its requests over this adapter's HTTP connector or stdio stream, within its limits, and
     * answers their {@code input_required} results with its elicitation handlers. It declares the
     * capabilities of those handlers only: task-augmented calls go through {@link
     * #getTasksClient()}.
     *
     * @return The client.
     */
    public synchronized McpAsyncClient getModernClient() {
        return modern().client();
    }

    /**
     * The client of the Tasks extension, once {@link McpProtocol#MODERN_VERSION} is the revision
     * spoken, over HTTP only for now.
     *
     * @return The client.
     * @throws UnsupportedOperationException Over stdio.
     */
    public synchronized McpTasksClient getTasksClient() {
        McpTasksClient tasks = modern().tasks();
        if (tasks == null) {
            throw new UnsupportedOperationException(
                    "The MCP Tasks extension client supports HTTP only for now");
        }
        return tasks;
    }

    private Modern modern() {
        if (!started || modern == null) {
            throw new IllegalStateException("MCP 2026-07-28 client is not started");
        }
        return modern;
    }

    /** Starts a legacy client, over the stream, the stdio connector or HTTP. */
    private McpLegacyEdge.Client createLegacy(List<String> versions) {
        if (isStdio()) {
            return McpLegacyEdge.stdio(
                    stdioChannel(), versions, maxMessageBytes, maxConcurrentCalls, requestTimeout);
        }
        return McpLegacyEdge.http(
                new McpLegacyEdge.HttpOptions(
                        client,
                        target,
                        maxMessageBytes,
                        maxConcurrentCalls,
                        requestTimeout,
                        requestCustomizer,
                        versions,
                        legacySessions,
                        legacySessionId));
    }

    public synchronized boolean isStarted() {
        return started;
    }

    private synchronized McpAsyncClient modernOrNull() {
        if (!started) {
            throw new IllegalStateException("MCP client is not started");
        }
        return modern == null ? null : modern.client();
    }

    /**
     * Lists tools, in either era.
     *
     * @param cursor The cursor of the page, or null for the first.
     * @return The page of tools.
     */
    public McpSchema.ListToolsResult listTools(String cursor) {
        McpAsyncClient current = modernOrNull();
        if (current != null) {
            return current.tools().cursor(cursor).list().block(requestTimeout);
        }
        return legacy().listTools(cursor).block(requestTimeout);
    }

    /**
     * Calls a tool, in either era. An {@code input_required} result the elicitation handlers don't
     * answer fails the call.
     *
     * @param name The tool.
     * @param arguments Its arguments.
     * @return The tool's result.
     */
    public McpSchema.CallToolResult callTool(String name, Map<String, Object> arguments) {
        McpAsyncClient current = modernOrNull();
        if (current == null) {
            return legacy().callTool(
                            new McpSchema.CallToolRequest(name, arguments, null, null, null))
                    .block(requestTimeout);
        }
        var tool = current.tool(name);
        return (arguments == null ? tool : tool.arguments(arguments)).call().block(requestTimeout);
    }

    /**
     * Reads a resource, in either era. An {@code input_required} result the elicitation handlers
     * don't answer fails the read.
     *
     * @param uri The resource URI.
     * @return The resource's contents.
     */
    public McpSchema.ReadResourceResult readResource(String uri) {
        McpAsyncClient current = modernOrNull();
        if (current == null) {
            return legacy().readResource(new McpSchema.ReadResourceRequest(uri, null, null, null))
                    .block(requestTimeout);
        }
        return current.resource(uri).read().block(requestTimeout);
    }

    /**
     * Gets a prompt, in either era. An {@code input_required} result the elicitation handlers don't
     * answer fails the request.
     *
     * @param name The prompt.
     * @param arguments Its arguments.
     * @return The prompt.
     */
    public McpSchema.GetPromptResult getPrompt(String name, Map<String, String> arguments) {
        McpAsyncClient current = modernOrNull();
        if (current == null) {
            return legacy().getPrompt(
                            new McpSchema.GetPromptRequest(name, arguments, null, null, null))
                    .block(requestTimeout);
        }
        var prompt = current.prompt(name);
        return (arguments == null ? prompt : prompt.arguments(arguments))
                .get()
                .block(requestTimeout);
    }

    private synchronized McpLegacyEdge.Client legacy() {
        if (!started || legacy == null) {
            throw new IllegalStateException("MCP client is not started");
        }
        return legacy;
    }

    public synchronized void stop() {
        if (modern != null) {
            started = false;
            try {
                modern.close();
            } finally {
                modern = null;
            }
        }
        if (legacy != null) {
            started = false;
            try {
                legacy.close();
            } finally {
                legacy = null;
            }
        }
        closeOwnChannel();
    }

    @Override
    public void close() {
        stop();
    }
}
