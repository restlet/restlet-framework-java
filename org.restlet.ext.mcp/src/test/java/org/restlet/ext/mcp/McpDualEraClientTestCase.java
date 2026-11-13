/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.mcp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.modelcontextprotocol.json.McpJsonDefaults;
import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.json.TypeRef;
import io.modelcontextprotocol.server.McpAsyncServer;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpError;
import io.modelcontextprotocol.spec.McpSchema;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.UnaryOperator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.ext.mcp.internal.McpEras;
import org.restlet.representation.StringRepresentation;
import org.restlet.routing.Filter;
import reactor.core.publisher.Mono;

/**
 * {@link McpClientAdapter} in {@link McpProtocol#AUTO} mode, the dual-era client of the 2026-07-28
 * revision: era detection with {@code server/discover}, fallback to an {@code initialize}
 * handshake, opt-in legacy sessions, the limits of the legacy edge, and the era cache.
 */
class McpDualEraClientTestCase {
    private static final Duration WAIT = Duration.ofSeconds(10);
    private static final McpJsonMapper MAPPER = McpJsonDefaults.getMapper();

    private final List<AutoCloseable> closing = new ArrayList<>();
    private Client connector;

    @BeforeEach
    void setUp() throws Exception {
        McpEras.clear();
        Context context = new Context();
        context.getParameters().set("followRedirects", "false");
        connector = new Client(context, Protocol.HTTP);
        connector.start();
    }

    @AfterEach
    void cleanup() throws Exception {
        Exception failure = null;
        // Clients and their connector first: a server stopping gracefully would otherwise wait
        // on the connector's idle connections.
        List<AutoCloseable> order = new ArrayList<>();
        closing.reversed().stream().filter(McpClientAdapter.class::isInstance).forEach(order::add);
        order.add(connector::stop);
        closing.reversed().stream()
                .filter(resource -> !(resource instanceof McpClientAdapter))
                .forEach(order::add);
        for (AutoCloseable resource : order) {
            try {
                resource.close();
            } catch (Exception e) {
                failure = failure == null ? e : failure;
            }
        }
        McpEras.clear();
        if (failure != null) {
            throw failure;
        }
    }

    /** A server answering {@code server/discover} gets the modern client. */
    @Test
    void speaksModernToADualEraServer() throws Exception {
        McpServerAdapter served = new McpServerAdapter();
        McpServerAdapterTestCase.addEcho(served);
        String target = serve(served, served::stop);

        McpClientAdapter client = auto(target);

        assertEquals(McpProtocol.MODERN_VERSION, client.getNegotiatedVersion());
        assertTrue(client.listTools(null).tools().stream().anyMatch(t -> "echo".equals(t.name())));
        assertEquals("echo:café", text(client.callTool("echo", Map.of("text", "café"))));
        assertEquals(McpEras.MODERN, McpEras.get(target));
    }

    /**
     * A sessionful legacy server answers the probe with a bare 400: the client falls back to {@code
     * initialize}, and with legacy sessions on, keeps the session, sends the negotiated version,
     * and deletes the session on stop.
     */
    @Test
    void fallsBackToASessionfulLegacyServer() throws Exception {
        Legacy legacy = legacy();

        McpClientAdapter client = sessions(legacy.target);

        assertEquals(McpProtocol.VERSION, client.getNegotiatedVersion());
        assertEquals(List.of("server/discover", "initialize"), legacy.host.methods);
        assertEquals(1, legacy.host.sessions.size());
        assertEquals(legacy.host.sessions.keySet().iterator().next(), client.getLegacySessionId());
        assertEquals(List.of(Map.of()), legacy.host.capabilities);
        assertEquals("echo:hi", text(client.callTool("echo", Map.of("text", "hi"))));
        assertEquals(
                McpProtocol.VERSION, legacy.host.versions.get(legacy.host.versions.size() - 1));
        assertEquals(McpEras.LEGACY, McpEras.get(legacy.target));

        client.stop();
        assertEquals(1, legacy.host.deleted.get());
        assertTrue(legacy.host.sessions.isEmpty());
    }

    /** Legacy sessions are off by default: a server opening one fails the handshake. */
    @Test
    void refusesALegacySessionUnlessAskedTo() throws Exception {
        Legacy legacy = legacy();

        McpClientAdapter client = adapter(legacy.target);

        assertThrows(RuntimeException.class, client::start);
        assertFalse(client.isStarted());
        assertNull(client.getLegacySessionId());
        assertEquals(List.of("server/discover", "initialize"), legacy.host.methods);
    }

    /**
     * A session id the caller owns is sent with every legacy request, the handshake included, and
     * never deleted.
     */
    @Test
    void sendsTheCallersSessionIdAndNeverDeletesIt() throws Exception {
        McpServerAdapter served = new McpServerAdapter();
        McpServerAdapterTestCase.addEcho(served);
        List<String> seen = new CopyOnWriteArrayList<>();
        Filter relay =
                new Filter() {
                    @Override
                    protected int beforeHandle(Request request, Response response) {
                        seen.add(
                                request.getMethod()
                                        + " "
                                        + request.getHeaders()
                                                .getFirstValue(McpProtocol.SESSION_HEADER, true));
                        // The stateless adapter refuses sessions: the relay keeps it upstream.
                        request.getHeaders().removeAll(McpProtocol.SESSION_HEADER, true);
                        return CONTINUE;
                    }
                };
        relay.setNext(served);
        String target = serve(relay, served::stop, served);
        McpClientAdapter client = new McpClientAdapter(connector, target);
        client.setRequestTimeout(WAIT);
        client.setLegacySessionId("relayed-1");
        closing.add(client);

        client.start();
        assertEquals("echo:x", text(client.callTool("echo", Map.of("text", "x"))));
        assertEquals("relayed-1", client.getLegacySessionId());
        client.stop();

        assertFalse(seen.isEmpty());
        assertTrue(seen.stream().allMatch("POST relayed-1"::equals), seen.toString());
    }

    /** The era found is remembered: the next client to the endpoint skips the probe. */
    @Test
    void remembersTheEraOfAnEndpoint() throws Exception {
        Legacy legacy = legacy();
        sessions(legacy.target).stop();
        legacy.host.methods.clear();

        McpClientAdapter again = sessions(legacy.target);

        assertEquals(McpProtocol.VERSION, again.getNegotiatedVersion());
        assertEquals(List.of("initialize"), legacy.host.methods.subList(0, 1));
        assertFalse(legacy.host.methods.contains("server/discover"));
    }

    /** A remembered era that proves wrong is forgotten, and the endpoint probed again. */
    @Test
    void probesAgainWhenTheRememberedEraIsWrong() throws Exception {
        McpServerAdapter served = new McpServerAdapter();
        McpServerAdapterTestCase.addEcho(served);
        String target = serve(served, served::stop);
        McpEras.put(target, McpEras.LEGACY);

        McpClientAdapter client = auto(target);

        // The 2025-11-25 stateless server takes the handshake: the cache only skipped the probe.
        assertEquals(McpProtocol.VERSION, client.getNegotiatedVersion());
        assertEquals("echo:x", text(client.callTool("echo", Map.of("text", "x"))));

        McpEras.put(target, McpEras.MODERN);
        Legacy legacy = legacy();
        McpEras.put(legacy.target, McpEras.MODERN);
        McpClientAdapter fallback = sessions(legacy.target);
        assertEquals(McpProtocol.VERSION, fallback.getNegotiatedVersion());
        assertEquals(McpEras.LEGACY, McpEras.get(legacy.target));
    }

    /**
     * The legacy edge declares no capability and refuses what a legacy server asks: an elicitation
     * is never sent, and a ping from the server is refused, even with an input handler set.
     */
    @Test
    void refusesWhatALegacyServerAsks() throws Exception {
        Legacy legacy = legacy();
        McpClientAdapter client =
                auto(
                        legacy.target,
                        adapter -> {
                            adapter.setLegacySessions(true);
                            adapter.setModernCapabilities(
                                    io.modelcontextprotocol.modern.McpSchema.ClientCapabilities
                                            .builder()
                                            .elicitation(
                                                    new io.modelcontextprotocol.modern.McpSchema
                                                            .ClientCapabilities.Elicitation(
                                                            new io.modelcontextprotocol.modern
                                                                    .McpSchema.ClientCapabilities
                                                                    .Elicitation.Form(),
                                                            null))
                                            .build());
                            adapter.setInputHandler(required -> Mono.just(Map.of()));
                        });

        assertEquals(List.of(Map.of()), legacy.host.capabilities);
        assertEquals("refused", text(client.callTool("ask", Map.of())));
        assertFalse(legacy.host.replies.get("ask").contains("request"));
        assertEquals("refused", text(client.callTool("ping", Map.of())));
        assertEquals(List.of("request", "response"), legacy.host.replies.get("ping"));
    }

    /**
     * A legacy server without sessions may still send a request on a response stream: it is
     * refused, and the call goes on.
     */
    @Test
    void refusesWhatASessionlessLegacyServerAsks() throws Exception {
        Legacy legacy = legacy();
        legacy.host.sessionless = true;

        McpClientAdapter client = auto(legacy.target);

        assertNull(client.getLegacySessionId());
        assertEquals("refused", text(client.callTool("ping", Map.of())));
        assertEquals(List.of("request", "response"), legacy.host.replies.get("ping"));
    }

    /** The refusal doesn't wait for a call slot: the stream it answers may hold the only one. */
    @Test
    void refusesWhatALegacyServerAsksWithASingleCallSlot() throws Exception {
        Legacy legacy = legacy();

        McpClientAdapter client =
                auto(
                        legacy.target,
                        adapter -> {
                            adapter.setLegacySessions(true);
                            adapter.setMaxConcurrentCalls(1);
                        });

        assertEquals("refused", text(client.callTool("ping", Map.of())));
        assertEquals(List.of("request", "response"), legacy.host.replies.get("ping"));
    }

    /** A stalled session deletion is aborted at the request timeout: stopping doesn't hang. */
    @Test
    void stopsWhenTheSessionDeletionStalls() throws Exception {
        var release = new CountDownLatch(1);
        var deleting = new CountDownLatch(1);
        Legacy legacy =
                legacy(
                        host -> {
                            Filter stall =
                                    new Filter() {
                                        @Override
                                        protected int beforeHandle(
                                                Request request, Response response) {
                                            if (Method.DELETE.equals(request.getMethod())) {
                                                deleting.countDown();
                                                try {
                                                    release.await();
                                                } catch (InterruptedException e) {
                                                    Thread.currentThread().interrupt();
                                                }
                                            }
                                            return CONTINUE;
                                        }
                                    };
                            stall.setNext(host);
                            return stall;
                        });
        closing.add(release::countDown);
        McpClientAdapter client =
                auto(
                        legacy.target,
                        adapter -> {
                            adapter.setLegacySessions(true);
                            adapter.setRequestTimeout(Duration.ofMillis(500));
                        });
        assertEquals("echo:x", text(client.callTool("echo", Map.of("text", "x"))));

        assertTimeoutPreemptively(Duration.ofSeconds(5), client::stop);
        assertEquals(0, deleting.getCount(), "the deletion was sent");
    }

    /** The legacy fallback only sends what 2026-07-28 keeps, asserted on the wire. */
    @Test
    void theLegacyFallbackSendsNoDeprecatedMethod() throws Exception {
        Legacy legacy = legacy();
        McpClientAdapter client = sessions(legacy.target);

        client.listTools(null);
        client.callTool("echo", Map.of("text", "wire"));
        client.stop();

        assertTrue(
                Set.of("server/discover", "initialize", "tools/list", "tools/call")
                        .containsAll(legacy.host.methods),
                legacy.host.methods.toString());
        assertEquals(0, legacy.host.opened.get(), "no listening GET stream");
    }

    /** A JSON-RPC error other than an unsupported version, such as method not found, is legacy. */
    @Test
    void fallsBackOnANonModernJsonRpcError() throws Exception {
        McpServerAdapter served = new McpServerAdapter();
        McpServerAdapterTestCase.addEcho(served);
        // Every request reaches the 2025-11-25 server, which doesn't know server/discover.
        Filter legacyOnly =
                new Filter() {
                    @Override
                    protected int beforeHandle(Request request, Response response) {
                        request.getHeaders().set(McpProtocol.VERSION_HEADER, McpProtocol.VERSION);
                        return CONTINUE;
                    }
                };
        legacyOnly.setNext(served);
        String target = serve(legacyOnly, served::stop, served);

        McpClientAdapter client = auto(target);

        assertEquals(McpProtocol.VERSION, client.getNegotiatedVersion());
        assertEquals("echo:ok", text(client.callTool("echo", Map.of("text", "ok"))));
    }

    /** An unsupported version error naming a legacy revision leads to the handshake. */
    @Test
    void fallsBackWhenTheModernServerOffersALegacyRevision() throws Exception {
        McpServerAdapter served = new McpServerAdapter();
        McpServerAdapterTestCase.addEcho(served);
        String target =
                serve(
                        new UnsupportedModern(List.of(McpProtocol.VERSION), served),
                        served::stop,
                        served);

        McpClientAdapter client = auto(target);

        assertEquals(McpProtocol.VERSION, client.getNegotiatedVersion());
    }

    /** An unsupported version error naming no revision the client knows fails the start. */
    @Test
    void failsWhenNoRevisionIsShared() throws Exception {
        McpServerAdapter served = new McpServerAdapter();
        String target =
                serve(new UnsupportedModern(List.of("2099-01-01"), served), served::stop, served);

        McpClientAdapter client = adapter(target);
        IllegalStateException failure = assertThrows(IllegalStateException.class, client::start);

        assertTrue(failure.getMessage().contains("2099-01-01"), failure.getMessage());
        assertFalse(client.isStarted());
        assertNull(McpEras.get(target));
    }

    /**
     * A header mismatch or a missing client capability error comes from a modern server: the start
     * fails with it, without an {@code initialize} handshake.
     */
    @Test
    void doesNotFallBackOnAModernOnlyError() throws Exception {
        for (int code :
                List.of(
                        io.modelcontextprotocol.modern.McpSchema.ErrorCodes.HEADER_MISMATCH,
                        io.modelcontextprotocol.modern.McpSchema.ErrorCodes
                                .MISSING_REQUIRED_CLIENT_CAPABILITY)) {
            var handshakes = new AtomicInteger();
            Restlet legacy =
                    new Restlet() {
                        @Override
                        public void handle(Request request, Response response) {
                            handshakes.incrementAndGet();
                            response.setStatus(Status.SERVER_ERROR_INTERNAL);
                        }
                    };
            String target = serve(new UnsupportedModern(code, null, legacy), () -> {});

            McpClientAdapter client = adapter(target);

            RuntimeException failure = assertThrows(RuntimeException.class, client::start);
            assertTrue(
                    failure instanceof io.modelcontextprotocol.modern.McpException mcp
                            && mcp.error().code() == code,
                    failure.toString());
            assertEquals(0, handshakes.get());
            assertFalse(client.isStarted());
            assertNull(McpEras.get(target));
        }
    }

    /** A server error is not a sign of a legacy server: the start fails without a handshake. */
    @Test
    void doesNotFallBackOnAServerError() throws Exception {
        var posts = new AtomicInteger();
        Restlet unavailable =
                new Restlet() {
                    @Override
                    public void handle(Request request, Response response) {
                        posts.incrementAndGet();
                        response.setStatus(Status.SERVER_ERROR_SERVICE_UNAVAILABLE);
                    }
                };
        String target = serve(unavailable, () -> {});

        McpClientAdapter client = adapter(target);

        assertThrows(RuntimeException.class, client::start);
        assertEquals(1, posts.get());
        assertNull(McpEras.get(target));
    }

    /** An expired session is reported to the SDK client, whose next request initializes again. */
    @Test
    void initializesAgainAfterTheSessionExpires() throws Exception {
        Legacy legacy = legacy();
        McpClientAdapter client = sessions(legacy.target);
        String first = legacy.host.sessions.keySet().iterator().next();

        legacy.host.expire(first);
        assertThrows(RuntimeException.class, () -> client.callTool("echo", Map.of("text", "a")));

        assertEquals("echo:b", text(client.callTool("echo", Map.of("text", "b"))));
        assertEquals(2, legacy.host.methods.stream().filter("initialize"::equals).count());
        assertFalse(legacy.host.sessions.containsKey(first));
        assertEquals(1, legacy.host.sessions.size());
    }

    /** Auto is a known setting; an unknown revision is refused, and nothing is negotiated yet. */
    @Test
    void acceptsAutoAndOnlyKnownRevisions() {
        McpClientAdapter client = new McpClientAdapter(connector, "http://localhost:1/mcp");
        client.setProtocolVersion(McpProtocol.AUTO);
        assertEquals(McpProtocol.AUTO, client.getProtocolVersion());
        assertNull(client.getNegotiatedVersion());
        assertFalse(client.isLegacySessions());
        client.setLegacySessionId("before-start");
        assertEquals("before-start", client.getLegacySessionId());
        assertThrows(IllegalArgumentException.class, () -> client.setProtocolVersion("1999-01-01"));
    }

    private record Legacy(McpLegacySessionHost host, String target) {}

    /** A sessionful 2025-11-25 server with an echo tool and an elicitation tool. */
    private Legacy legacy() throws Exception {
        return legacy(UnaryOperator.identity());
    }

    /** A sessionful 2025-11-25 server, reached through what {@code front} puts before it. */
    private Legacy legacy(UnaryOperator<Restlet> front) throws Exception {
        var host = new McpLegacySessionHost(Duration.ofMillis(200), WAIT);
        McpAsyncServer sdk =
                McpServer.async(host)
                        .serverInfo("legacy", "1")
                        .capabilities(McpSchema.ServerCapabilities.builder().tools(true).build())
                        .requestTimeout(WAIT)
                        .tools(
                                McpServerFeatures.AsyncToolSpecification.builder()
                                        .tool(
                                                McpSchema.Tool.builder(
                                                                "echo", Map.of("type", "object"))
                                                        .build())
                                        .callHandler(
                                                (exchange, request) ->
                                                        Mono.just(
                                                                result(
                                                                        "echo:"
                                                                                + request.arguments()
                                                                                        .get(
                                                                                                "text"))))
                                        .build(),
                                McpServerFeatures.AsyncToolSpecification.builder()
                                        .tool(
                                                McpSchema.Tool.builder(
                                                                "ask", Map.of("type", "object"))
                                                        .build())
                                        .callHandler(McpDualEraClientTestCase::ask)
                                        .build(),
                                McpServerFeatures.AsyncToolSpecification.builder()
                                        .tool(
                                                McpSchema.Tool.builder(
                                                                "ping", Map.of("type", "object"))
                                                        .build())
                                        .callHandler(
                                                (exchange, request) ->
                                                        exchange.ping()
                                                                .map(pong -> result("answered"))
                                                                .onErrorResume(
                                                                        error ->
                                                                                Mono.just(
                                                                                        result(
                                                                                                refused(
                                                                                                                error)
                                                                                                        ? "refused"
                                                                                                        : "failed"))))
                                        .build())
                        .build();
        String target =
                serve(front.apply(host), () -> sdk.closeGracefully().onErrorComplete().block(WAIT));
        return new Legacy(host, target);
    }

    /** Whether the client answered a server request as not found, rather than not at all. */
    private static boolean refused(Throwable error) {
        return error instanceof McpError mcp
                && mcp.getJsonRpcError() != null
                && mcp.getJsonRpcError().code() == McpSchema.ErrorCodes.METHOD_NOT_FOUND;
    }

    /** Asks the client for a region; any failure, such as no capability, is a refusal. */
    private static Mono<McpSchema.CallToolResult> ask(
            io.modelcontextprotocol.server.McpAsyncServerExchange exchange,
            McpSchema.CallToolRequest request) {
        return Mono.defer(
                        () ->
                                exchange.createElicitation(
                                        McpSchema.ElicitFormRequest.builder(
                                                        "Which region?", Map.of("type", "object"))
                                                .build()))
                .map(
                        answer ->
                                result(
                                        answer.action().name().toLowerCase(java.util.Locale.ROOT)
                                                + ":"
                                                + (answer.content() == null
                                                        ? "none"
                                                        : answer.content().get("region"))))
                .onErrorResume(error -> Mono.just(result("refused")));
    }

    /**
     * Starts an HTTP server for a Restlet, stopped after the test along with what else it needs.
     */
    private String serve(Restlet restlet, AutoCloseable stop, McpServerAdapter... adapters)
            throws Exception {
        for (McpServerAdapter adapter : adapters) {
            if (!adapter.isStarted()) {
                adapter.start();
            }
        }
        if (restlet instanceof McpServerAdapter adapter && !adapter.isStarted()) {
            adapter.start();
        }
        Server server = new Server(new Context(), Protocol.HTTP, 0, restlet);
        server.start();
        closing.add(stop);
        closing.add(server::stop);
        return "http://localhost:" + server.getActualPort() + "/mcp";
    }

    private McpClientAdapter adapter(String target) {
        McpClientAdapter client = new McpClientAdapter(connector, target);
        client.setRequestTimeout(WAIT);
        client.setProtocolVersion(McpProtocol.AUTO);
        closing.add(client);
        return client;
    }

    private McpClientAdapter auto(String target) {
        return auto(target, adapter -> {});
    }

    /** An auto client keeping legacy sessions. */
    private McpClientAdapter sessions(String target) {
        return auto(target, adapter -> adapter.setLegacySessions(true));
    }

    private McpClientAdapter auto(
            String target, java.util.function.Consumer<McpClientAdapter> set) {
        McpClientAdapter client = adapter(target);
        set.accept(client);
        client.start();
        return client;
    }

    private static McpSchema.CallToolResult result(String text) {
        return McpSchema.CallToolResult.builder().addTextContent(text).build();
    }

    private static String text(io.modelcontextprotocol.modern.McpSchema.CallToolResult result) {
        return ((io.modelcontextprotocol.modern.McpSchema.TextContent) result.content().getFirst())
                .text();
    }

    /**
     * A modern endpoint refusing 2026-07-28 with an unsupported version error naming other
     * revisions; other requests go to a 2025-11-25 server.
     */
    private static final class UnsupportedModern extends Restlet {
        private final int code;
        private final Object data;
        private final Restlet legacy;

        private UnsupportedModern(List<String> supported, Restlet legacy) {
            this(
                    McpProtocol.UNSUPPORTED_PROTOCOL_VERSION,
                    Map.of("supported", supported, "requested", McpProtocol.MODERN_VERSION),
                    legacy);
        }

        /** Refuses modern requests with another error code, and its data, if any. */
        private UnsupportedModern(int code, Object data, Restlet legacy) {
            this.code = code;
            this.data = data;
            this.legacy = legacy;
        }

        @Override
        public void handle(Request request, Response response) {
            if (!McpProtocol.MODERN_VERSION.equals(
                    request.getHeaders().getFirstValue(McpProtocol.VERSION_HEADER, true))) {
                legacy.handle(request, response);
                return;
            }
            try {
                Map<String, Object> rpc =
                        MAPPER.readValue(request.getEntity().getText(), new TypeRef<>() {});
                Map<String, Object> failure = new LinkedHashMap<>();
                failure.put("code", code);
                failure.put("message", "Refused MCP request");
                if (data != null) {
                    failure.put("data", data);
                }
                var error = Map.of("jsonrpc", "2.0", "id", rpc.get("id"), "error", failure);
                response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                response.setEntity(
                        new StringRepresentation(
                                MAPPER.writeValueAsString(error), MediaType.APPLICATION_JSON));
            } catch (IOException e) {
                response.setStatus(Status.SERVER_ERROR_INTERNAL, e);
            }
        }
    }
}
