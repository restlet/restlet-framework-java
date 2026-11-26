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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.modelcontextprotocol.common.McpTransportContext;
import io.modelcontextprotocol.json.McpJsonDefaults;
import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.json.TypeRef;
import io.modelcontextprotocol.modern.McpSchema.CallToolOutcome;
import io.modelcontextprotocol.modern.McpSchema.ElicitFormRequest;
import io.modelcontextprotocol.modern.McpSchema.InputRequiredResult;
import io.modelcontextprotocol.modern.McpSchema.MetaKeys;
import io.modelcontextprotocol.modern.McpSchema.Tool;
import io.modelcontextprotocol.modern.server.McpAsyncResponse;
import io.modelcontextprotocol.modern.server.McpRequestContext;
import io.modelcontextprotocol.modern.server.RequestStateCodec;
import io.modelcontextprotocol.modern.server.feature.McpAsyncTool;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.representation.StringRepresentation;
import org.restlet.security.User;
import reactor.core.publisher.Mono;

/**
 * The codec sealing the {@code requestState} of {@code input_required} results: the default HMAC
 * one, the AEAD one, and an application's own, with what they bind the state to.
 */
class McpRequestStateCodecTestCase {
    private static final McpJsonMapper MAPPER = McpJsonDefaults.getMapper();
    private static final String MODERN = McpProtocol.MODERN_VERSION;
    private static final byte[] KEY =
            "0123456789abcdef0123456789abcdef".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] OTHER_KEY =
            "fedcba9876543210fedcba9876543210".getBytes(StandardCharsets.US_ASCII);

    private final List<McpServerAdapter> adapters = new ArrayList<>();

    @AfterEach
    void cleanup() throws Exception {
        for (McpServerAdapter adapter : adapters) {
            adapter.stop();
        }
    }

    /** The default codec binds the state to the call's arguments. */
    @Test
    void theDefaultCodecBindsTheArguments() throws Exception {
        McpServerAdapter adapter = adapter(null);
        adapter.start();

        String state = ask(adapter, Map.of("amount", 10));
        assertEquals("approved:s1", firstText(answer(adapter, Map.of("amount", 10), state)));
        assertEquals(-32602, code(adapter.handle(retry(Map.of("amount", 99), state))));
    }

    /**
     * The AEAD codec hides the state, opens it for the same call on an instance sharing the key,
     * and for no other call, key, expired state or tampered state.
     */
    @Test
    void theAeadCodecHidesAndBindsTheState() throws Exception {
        AtomicReference<Instant> now = new AtomicReference<>(Instant.parse("2026-11-26T10:00:00Z"));
        Clock clock =
                new Clock() {
                    @Override
                    public ZoneOffset getZone() {
                        return ZoneOffset.UTC;
                    }

                    @Override
                    public Clock withZone(java.time.ZoneId zone) {
                        return this;
                    }

                    @Override
                    public Instant instant() {
                        return now.get();
                    }
                };
        McpServerAdapter first =
                adapter(
                        McpAeadRequestStateCodec.builder()
                                .key(KEY)
                                .ttl(Duration.ofMinutes(1))
                                .clock(clock)
                                .build());
        McpServerAdapter second =
                adapter(McpAeadRequestStateCodec.builder().key(KEY).clock(clock).build());
        McpServerAdapter other =
                adapter(McpAeadRequestStateCodec.builder().key(OTHER_KEY).clock(clock).build());
        first.start();
        second.start();
        other.start();

        String state = ask(first, Map.of("amount", 10));
        String decoded =
                new String(Base64.getUrlDecoder().decode(state), StandardCharsets.ISO_8859_1);
        assertFalse(decoded.contains("s1"), "the state must be encrypted");
        assertNotEquals(state, ask(first, Map.of("amount", 10)), "each seal takes a new nonce");

        assertEquals("approved:s1", firstText(answer(second, Map.of("amount", 10), state)));
        assertEquals(-32602, code(second.handle(retry(Map.of("amount", 99), state))));
        assertEquals(-32602, code(other.handle(retry(Map.of("amount", 10), state))));
        char last = state.charAt(state.length() - 1);
        String tampered = state.substring(0, state.length() - 1) + (last == 'A' ? 'B' : 'A');
        assertEquals(-32602, code(second.handle(retry(Map.of("amount", 10), tampered))));
        assertEquals(-32602, code(second.handle(retry(Map.of("amount", 10), "not-base64!"))));

        now.set(now.get().plus(Duration.ofMinutes(11)));
        assertEquals(-32602, code(second.handle(retry(Map.of("amount", 10), state))));
    }

    /** The state of one user doesn't open for another. */
    @Test
    void theAeadCodecBindsTheUser() throws Exception {
        McpServerAdapter adapter = adapter(McpAeadRequestStateCodec.builder().key(KEY).build());
        adapter.start();

        Request asked = as("alice", call(Map.of("amount", 10), null));
        String state = (String) result(adapter.handle(asked)).get("requestState");
        assertEquals(
                "approved:s1",
                firstText(result(adapter.handle(as("alice", retry(Map.of("amount", 10), state))))));
        assertEquals(-32602, code(adapter.handle(as("bob", retry(Map.of("amount", 10), state)))));
    }

    /** A key and a codec can't both be set. */
    @Test
    void aKeyAndACodecCannotBothBeSet() {
        McpServerAdapter keyed = new McpServerAdapter();
        keyed.setRequestStateKey(KEY);
        assertThrows(
                IllegalStateException.class,
                () -> keyed.setRequestStateCodec(McpAeadRequestStateCodec.builder().build()));

        McpServerAdapter coded = new McpServerAdapter();
        coded.setRequestStateCodec(McpAeadRequestStateCodec.builder().build());
        assertThrows(IllegalStateException.class, () -> coded.setRequestStateKey(KEY));
        assertNotNull(coded.getRequestStateCodec());
        coded.setRequestStateCodec(null);
        coded.setRequestStateKey(KEY);
    }

    /** An application's codec reads what the state is bound to. */
    @Test
    void anApplicationCodecReadsTheBinding() throws Exception {
        List<String> seen = new CopyOnWriteArrayList<>();
        RequestStateCodec codec =
                new RequestStateCodec() {
                    @Override
                    public String seal(McpRequestContext ctx, String state) {
                        McpTransportContext transport = ctx.transportContext();
                        seen.add(
                                McpRequestBinding.user(transport).orElse("?")
                                        + "|"
                                        + McpRequestBinding.argumentsDigest(transport).orElse("?"));
                        return "sealed-" + state;
                    }

                    @Override
                    public Optional<String> open(McpRequestContext ctx, String sealed) {
                        return sealed.startsWith("sealed-")
                                ? Optional.of(sealed.substring(7))
                                : Optional.empty();
                    }
                };
        McpServerAdapter adapter = adapter(codec);
        adapter.start();

        Map<String, Object> asked =
                result(adapter.handle(as("alice", call(Map.of("amount", 10), null))));
        assertEquals("sealed-s1", asked.get("requestState"));
        assertEquals(1, seen.size());
        assertTrue(seen.get(0).startsWith("alice|"), seen.toString());
        assertEquals(64, seen.get(0).length() - "alice|".length());
    }

    /** The digest ignores key order, number forms and the params a retry adds. */
    @Test
    void theArgumentsDigestIsCanonical() {
        Map<String, Object> first = new LinkedHashMap<>();
        first.put("name", "approve");
        first.put("arguments", Map.of("a", 1, "b", List.of("x", "y\"")));
        Map<String, Object> second = new LinkedHashMap<>();
        second.put("arguments", Map.of("b", List.of("x", "y\""), "a", 1.0));
        second.put("name", "approve");
        second.put("requestState", "s");
        second.put("inputResponses", Map.of());
        second.put("_meta", Map.of("k", "v"));
        assertEquals(
                org.restlet.ext.mcp.internal.McpBindings.digest(first),
                org.restlet.ext.mcp.internal.McpBindings.digest(second));
        assertNotEquals(
                org.restlet.ext.mcp.internal.McpBindings.digest(first),
                org.restlet.ext.mcp.internal.McpBindings.digest(Map.of("name", "approve")));

        McpTransportContext none = McpTransportContext.create(Map.of());
        assertEquals("-;-;-;", McpRequestBinding.identity(none));
        assertTrue(McpRequestBinding.argumentsDigest(none).isEmpty());
        assertNotEquals(
                McpRequestBinding.identity(
                        McpTransportContext.create(
                                Map.of(
                                        org.restlet.ext.mcp.internal.McpBindings.BOUND_NAME_KEY,
                                        "a;1:b"))),
                McpRequestBinding.identity(
                        McpTransportContext.create(
                                Map.of(
                                        org.restlet.ext.mcp.internal.McpBindings.BOUND_NAME_KEY,
                                        "a",
                                        org.restlet.ext.mcp.internal.McpBindings
                                                .ARGUMENTS_DIGEST_KEY,
                                        "b"))));
    }

    /** The AEAD builder checks its settings. */
    @Test
    void theAeadBuilderChecksItsSettings() {
        assertThrows(
                IllegalArgumentException.class,
                () -> McpAeadRequestStateCodec.builder().key(new byte[16]));
        assertThrows(
                IllegalArgumentException.class,
                () -> McpAeadRequestStateCodec.builder().ttl(Duration.ZERO));
        assertThrows(
                NullPointerException.class, () -> McpAeadRequestStateCodec.builder().clock(null));
    }

    private McpServerAdapter adapter(RequestStateCodec codec) {
        McpServerAdapter adapter = new McpServerAdapter();
        adapters.add(adapter);
        if (codec != null) {
            adapter.setRequestStateCodec(codec);
        }
        adapter.getTools()
                .add(
                        McpAsyncTool.of(
                                Tool.builder("approve", Map.of("type", "object")).build(),
                                (ctx, request) ->
                                        Mono.just(
                                                request.requestState() == null
                                                        ? McpAsyncResponse.<CallToolOutcome>result(
                                                                InputRequiredResult.builder()
                                                                        .elicit(
                                                                                "confirm",
                                                                                ElicitFormRequest
                                                                                        .builder(
                                                                                                "Proceed?",
                                                                                                Map
                                                                                                        .of(
                                                                                                                "type",
                                                                                                                "object",
                                                                                                                "properties",
                                                                                                                Map
                                                                                                                        .of()))
                                                                                        .build())
                                                                        .requestState("s1")
                                                                        .build())
                                                        : McpAsyncResponse.<CallToolOutcome>result(
                                                                McpTestFeatures.text(
                                                                        "approved:"
                                                                                + request
                                                                                        .requestState())))));
        return adapter;
    }

    private static String ask(McpServerAdapter adapter, Map<String, Object> arguments)
            throws Exception {
        Map<String, Object> asked = result(adapter.handle(call(arguments, null)));
        assertEquals("input_required", asked.get("resultType"), asked.toString());
        return (String) asked.get("requestState");
    }

    private static Map<String, Object> answer(
            McpServerAdapter adapter, Map<String, Object> arguments, String state)
            throws Exception {
        return result(adapter.handle(retry(arguments, state)));
    }

    private static Request retry(Map<String, Object> arguments, String state) {
        return call(arguments, state);
    }

    private static Request call(Map<String, Object> arguments, String state) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("name", "approve");
        params.put("arguments", arguments);
        if (state != null) {
            params.put("requestState", state);
            params.put(
                    "inputResponses",
                    Map.of("confirm", Map.of("action", "accept", "content", Map.of())));
        }
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put(MetaKeys.PROTOCOL_VERSION, MODERN);
        meta.put(MetaKeys.CLIENT_CAPABILITIES, Map.of("elicitation", Map.of("form", Map.of())));
        params.put("_meta", meta);
        Map<String, Object> envelope = new LinkedHashMap<>();
        envelope.put("jsonrpc", "2.0");
        envelope.put("id", 1);
        envelope.put("method", "tools/call");
        envelope.put("params", params);
        String body;
        try {
            body = MAPPER.writeValueAsString(envelope);
        } catch (java.io.IOException e) {
            throw new IllegalStateException(e);
        }
        Request request =
                new Request(
                        Method.POST,
                        "http://localhost/mcp",
                        new StringRepresentation(body, MediaType.APPLICATION_JSON));
        request.getHeaders().set(McpProtocol.VERSION_HEADER, MODERN);
        request.getHeaders().set("Accept", "application/json, text/event-stream");
        request.getHeaders().set("Mcp-Method", "tools/call");
        request.getHeaders().set("Mcp-Name", "approve");
        return request;
    }

    private static Request as(String user, Request request) {
        request.getClientInfo().setUser(new User(user));
        return request;
    }

    private static Map<String, Object> envelope(Response response) throws Exception {
        var bytes = new ByteArrayOutputStream();
        if (response.getEntity() != null) {
            response.getEntity().write(bytes);
        }
        return MAPPER.readValue(
                bytes.toString(StandardCharsets.UTF_8), new TypeRef<Map<String, Object>>() {});
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> result(Response response) throws Exception {
        Map<String, Object> envelope = envelope(response);
        assertTrue(envelope.containsKey("result"), envelope.toString());
        return (Map<String, Object>) envelope.get("result");
    }

    private static int code(Response response) throws Exception {
        return ((Number) ((Map<?, ?>) envelope(response).get("error")).get("code")).intValue();
    }

    private static String firstText(Map<String, Object> result) {
        return (String) ((Map<?, ?>) ((List<?>) result.get("content")).get(0)).get("text");
    }
}
