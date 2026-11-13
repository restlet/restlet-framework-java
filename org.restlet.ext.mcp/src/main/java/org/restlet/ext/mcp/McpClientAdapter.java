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
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import org.restlet.Client;
import org.restlet.Request;
import org.restlet.ext.mcp.internal.McpEras;
import org.restlet.ext.mcp.internal.McpHttpStatusException;
import org.restlet.ext.mcp.internal.McpLegacyEdge;
import org.restlet.ext.mcp.internal.McpModernHttpTransport;
import reactor.core.Exceptions;

/**
 * Lifecycle facade of an MCP client, on the SDK's {@code 2026-07-28} types. Borrowed Restlet
 * connectors are never stopped.
 *
 * <p>By default it speaks MCP {@link McpProtocol#VERSION} through the SDK's client, behind a legacy
 * edge. With {@link #setProtocolVersion(String)} set to {@link McpProtocol#MODERN_VERSION}, it
 * speaks {@code 2026-07-28} over HTTP instead, experimentally: {@link #start()} discovers the
 * server, and {@link #getModernClient()} makes the requests.
 *
 * <p>With {@link McpProtocol#AUTO}, also experimental, it speaks whichever era the server does, as
 * the 2026-07-28 revision specifies for a dual-era client. Over HTTP, {@link #start()} sends {@code
 * server/discover}: a discovery result naming {@code 2026-07-28} selects the modern client; a
 * {@code 4xx} without a JSON-RPC response, or a JSON-RPC error other than an unsupported version,
 * marks a legacy server, which gets an {@code initialize} handshake offering {@link
 * McpProtocol#LEGACY_VERSIONS}. Other failures, such as a {@code 5xx} or a timeout, are thrown
 * without falling back. The era found is remembered for the endpoint URI, for the life of the JVM,
 * and probed again if it later proves wrong. Over stdio, it speaks {@link McpProtocol#VERSION}, the
 * modern client being HTTP only.
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
    private McpSchema.ClientCapabilities modernCapabilities = McpSchema.ClientCapabilities.NONE;
    private McpInputHandler inputHandler;
    private int maxInputRounds = DEFAULT_MAX_INPUT_ROUNDS;
    private boolean legacySessions;
    private String legacySessionId;
    private McpModernClient modern;
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
     * client; {@link McpProtocol#MODERN_VERSION} through {@link #getModernClient()}, over HTTP only
     * for now; or {@link McpProtocol#AUTO}, whichever the server speaks. Experimental for {@code
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
     * one negotiated by {@code initialize}.
     *
     * @return The revision, or null before {@link #start()}.
     */
    public synchronized String getNegotiatedVersion() {
        return started ? negotiatedVersion : null;
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
    public synchronized void setModernCapabilities(McpSchema.ClientCapabilities value) {
        requireConfigurable();
        modernCapabilities = Objects.requireNonNull(value, "value");
    }

    public synchronized McpSchema.ClientCapabilities getModernCapabilities() {
        return modernCapabilities;
    }

    /**
     * Sets what answers the {@code input_required} results of {@link McpProtocol#MODERN_VERSION}
     * requests, after which the request is retried; without one, the result is returned. Legacy
     * servers ask for no input: the legacy edge declares no elicitation capability.
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
        if (McpProtocol.MODERN_VERSION.equals(protocolVersion)) {
            startModern(createModern());
            return;
        }
        if (McpProtocol.AUTO.equals(protocolVersion)) {
            startAuto();
            return;
        }
        startLegacy(List.of(McpProtocol.VERSION));
    }

    /** Starts the legacy client: an {@code initialize} handshake offering these revisions. */
    private void startLegacy(List<String> versions) {
        legacy = createLegacy(versions);
        negotiatedVersion = legacy.negotiatedVersion();
        started = true;
    }

    /**
     * Starts the client of the era the server speaks: the one remembered for the endpoint, unless
     * it fails, else the one a {@code server/discover} probe tells.
     */
    private void startAuto() {
        if (stream != null || target.startsWith("stdio:")) {
            startLegacy(List.of(McpProtocol.VERSION));
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
                McpEras.remove(target);
            }
        }
        McpModernClient candidate = createModern();
        if (probe(candidate)) {
            modern = candidate;
            negotiatedVersion = McpProtocol.MODERN_VERSION;
            started = true;
            McpEras.put(target, McpEras.MODERN);
        } else {
            startLegacy(McpProtocol.LEGACY_VERSIONS);
            McpEras.put(target, McpEras.LEGACY);
        }
    }

    /**
     * Probes the server with {@code server/discover}.
     *
     * @return True for a modern server, which the candidate then serves; false for a legacy one,
     *     for which the candidate is closed.
     */
    private boolean probe(McpModernClient candidate) {
        boolean kept = false;
        try {
            McpSchema.DiscoverResult discovered = candidate.discover().block(requestTimeout);
            List<String> supported =
                    discovered == null || discovered.supportedVersions() == null
                            ? List.of()
                            : discovered.supportedVersions();
            kept = supported.contains(McpProtocol.MODERN_VERSION);
            if (!kept && !offersLegacy(supported)) {
                throw new IllegalStateException(
                        "No MCP revision supported by both the client and the peer: " + supported);
            }
            return kept;
        } catch (RuntimeException e) {
            if (!isLegacy(Exceptions.unwrap(e))) {
                throw e;
            }
            return false;
        } finally {
            if (!kept) {
                try {
                    candidate.close();
                } catch (IOException cleanup) {
                    // The probe exchange is over: nothing is left to release.
                }
            }
        }
    }

    /**
     * Whether a failed {@code server/discover} marks a legacy server: a JSON-RPC error that isn't a
     * recognized modern one, or a {@code 4xx} without a response. A header mismatch or a missing
     * client capability error comes from a modern server, whose failure is kept. An unsupported
     * version error comes from a modern server too, which is legacy-compatible only if it offers a
     * legacy revision.
     */
    private static boolean isLegacy(Throwable failure) {
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

    private McpModernClient createModern() {
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
        return new McpModernClient(
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
    }

    /** Starts a {@code 2026-07-28} client, and discovers the server's support of it. */
    private void startModern(McpModernClient candidate) {
        try {
            McpSchema.DiscoverResult discovered = candidate.discover().block(requestTimeout);
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
        negotiatedVersion = McpProtocol.MODERN_VERSION;
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

    /** Starts a legacy client, over the stream, the stdio connector or HTTP. */
    private McpLegacyEdge.Client createLegacy(List<String> versions) {
        if (stream != null) {
            return McpLegacyEdge.stdio(stream, maxMessageBytes, maxConcurrentCalls, requestTimeout);
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
            return helper.bind(maxMessageBytes, maxConcurrentCalls, requestTimeout);
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

    private synchronized McpModernClient modernOrNull() {
        if (!started) {
            throw new IllegalStateException("MCP client is not started");
        }
        return modern;
    }

    /**
     * Lists tools, in either era.
     *
     * @param cursor The cursor of the page, or null for the first.
     * @return The page of tools.
     */
    public McpSchema.ListToolsResult listTools(String cursor) {
        McpModernClient current = modernOrNull();
        if (current != null) {
            return current.listTools(cursor).block(requestTimeout);
        }
        return legacy().listTools(cursor).block(requestTimeout);
    }

    /**
     * Calls a tool, in either era. An {@code input_required} result the input handler doesn't
     * answer fails the call.
     *
     * @param name The tool.
     * @param arguments Its arguments.
     * @return The tool's result.
     */
    public McpSchema.CallToolResult callTool(String name, Map<String, Object> arguments) {
        McpModernClient current = modernOrNull();
        if (current == null) {
            return legacy().callTool(
                            new McpSchema.CallToolRequest(name, arguments, null, null, null))
                    .block(requestTimeout);
        }
        var outcome =
                current.callTool(new McpSchema.CallToolRequest(name, arguments, null, null, null))
                        .block(requestTimeout);
        if (!(outcome instanceof McpSchema.CallToolResult result)) {
            throw new IllegalStateException(
                    "MCP tool " + name + " requires input that was not provided");
        }
        return result;
    }

    /**
     * Reads a resource, in either era. An {@code input_required} result the input handler doesn't
     * answer fails the read.
     *
     * @param uri The resource URI.
     * @return The resource's contents.
     */
    public McpSchema.ReadResourceResult readResource(String uri) {
        McpModernClient current = modernOrNull();
        if (current == null) {
            return legacy().readResource(new McpSchema.ReadResourceRequest(uri, null, null, null))
                    .block(requestTimeout);
        }
        var outcome =
                current.readResource(new McpSchema.ReadResourceRequest(uri, null, null, null), null)
                        .block(requestTimeout);
        if (!(outcome instanceof McpSchema.ReadResourceResult result)) {
            throw new IllegalStateException(
                    "MCP resource " + uri + " requires input that was not provided");
        }
        return result;
    }

    /**
     * Gets a prompt, in either era. An {@code input_required} result the input handler doesn't
     * answer fails the request.
     *
     * @param name The prompt.
     * @param arguments Its arguments.
     * @return The prompt.
     */
    public McpSchema.GetPromptResult getPrompt(String name, Map<String, String> arguments) {
        McpModernClient current = modernOrNull();
        var request = new McpSchema.GetPromptRequest(name, arguments, null, null, null);
        if (current == null) {
            return legacy().getPrompt(request).block(requestTimeout);
        }
        var outcome = current.getPrompt(request, null).block(requestTimeout);
        if (!(outcome instanceof McpSchema.GetPromptResult result)) {
            throw new IllegalStateException(
                    "MCP prompt " + name + " requires input that was not provided");
        }
        return result;
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
            } catch (IOException e) {
                throw new UncheckedIOException(e);
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
    }

    @Override
    public void close() {
        stop();
    }
}
