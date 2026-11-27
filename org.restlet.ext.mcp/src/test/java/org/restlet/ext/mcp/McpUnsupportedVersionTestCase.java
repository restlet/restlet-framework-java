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
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.modelcontextprotocol.modern.JsonRpc;
import io.modelcontextprotocol.modern.McpSchema;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;

/**
 * An {@code MCP-Protocol-Version} header the adapter doesn't serve is answered with {@code
 * UnsupportedProtocolVersionError} (-32022), listing the versions it serves.
 */
class McpUnsupportedVersionTestCase {
    private static final String LIST = "{\"jsonrpc\":\"2.0\",\"id\":%s,\"method\":\"tools/list\"}";

    private final McpServerAdapter adapter = new McpServerAdapter();

    @BeforeEach
    void start() throws Exception {
        McpServerAdapterTestCase.addEcho(adapter);
        adapter.setMaxMessageBytes(1024);
        adapter.start();
    }

    @AfterEach
    void stop() throws Exception {
        adapter.stop();
    }

    /** A future revision, so the client can pick one the adapter serves. */
    @Test
    void anUnknownFutureVersionIsAnsweredWithTheSupportedOnes() throws Exception {
        assertUnsupported(send(String.format(LIST, 7), "2027-03-15"), 7, "2027-03-15");
    }

    /** A malformed version, with a string id. */
    @Test
    void aMalformedVersionIsAnsweredWithTheSupportedOnes() throws Exception {
        assertUnsupported(send(String.format(LIST, "\"a1\""), "garbage"), "a1", "garbage");
    }

    /** An older revision the legacy edge doesn't speak. */
    @Test
    void anOlderLegacyVersionIsAnsweredWithTheSupportedOnes() throws Exception {
        assertUnsupported(send(String.format(LIST, 3), "2025-06-18"), 3, "2025-06-18");
    }

    /** A body that isn't a JSON-RPC object is still answered, without an id. */
    @Test
    void anUnreadableBodyIsAnsweredWithoutAnId() throws Exception {
        assertUnsupported(send("not json", "2027-03-15"), null, "2027-03-15");
        assertUnsupported(
                send("[" + String.format(LIST, 1) + "]", "2027-03-15"), null, "2027-03-15");
        assertUnsupported(
                send(
                        "{\"jsonrpc\":\"2.0\",\"id\":{\"x\":1},\"method\":\"tools/list\"}",
                        "2027-03-15"),
                null,
                "2027-03-15");
    }

    /** A session only matters to a 2025-11-25 request. */
    @Test
    void aSessionDoesNotHideTheSupportedVersions() throws Exception {
        Request request = request(String.format(LIST, 5), "2027-03-15");
        request.getHeaders().set(McpProtocol.SESSION_HEADER, "stale");
        assertUnsupported(adapter.handle(request), 5, "2027-03-15");

        Request legacy = request(String.format(LIST, 5), McpProtocol.VERSION);
        legacy.getHeaders().set(McpProtocol.SESSION_HEADER, "stale");
        Response refused = adapter.handle(legacy);
        assertEquals(400, refused.getStatus().getCode());
        assertNull(refused.getEntity(), "a legacy session is refused as before");
    }

    /** The limits still apply first: too large, or the version sent twice. */
    @Test
    void limitsStillApply() throws Exception {
        String padded =
                "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"tools/list\",\"params\":{\"pad\":\""
                        + "x".repeat(2048)
                        + "\"}}";
        Response tooLarge = send(padded, "2027-03-15");
        assertEquals(413, tooLarge.getStatus().getCode());
        // Of unknown size, as when chunked: refused while read, within the call.
        Request chunked = request(padded, "2027-03-15");
        chunked.setEntity(
                new InputRepresentation(
                        new ByteArrayInputStream(padded.getBytes(StandardCharsets.UTF_8)),
                        MediaType.APPLICATION_JSON));
        assertEquals(Representation.UNKNOWN_SIZE, chunked.getEntity().getSize());
        Response unknownSize = adapter.handle(chunked);
        assertEquals(413, unknownSize.getStatus().getCode());
        assertNull(unknownSize.getEntity());

        Request twice = request(String.format(LIST, 1), "2027-03-15");
        twice.getHeaders().add(McpProtocol.VERSION_HEADER, "2027-04-01");
        Response duplicated = adapter.handle(twice);
        assertEquals(400, duplicated.getStatus().getCode());
        assertNull(duplicated.getEntity());
    }

    /**
     * The modern server's own -32022 lists the legacy version too: a legacy initialize sent with
     * the 2026-07-28 header is told that 2025-11-25 is served, without the header.
     */
    @Test
    void aHeaderedInitializeIsToldTheSupportedOnes() throws Exception {
        Request request =
                request(
                        "{\"jsonrpc\":\"2.0\",\"id\":4,\"method\":\"initialize\",\"params\":"
                                + "{\"protocolVersion\":\"2025-11-25\",\"capabilities\":{},"
                                + "\"clientInfo\":{\"name\":\"c\",\"version\":\"1\"}}}",
                        McpProtocol.MODERN_VERSION);
        request.getHeaders().set("Mcp-Method", "initialize");
        assertUnsupported(adapter.handle(request), 4, McpProtocol.VERSION);

        // A long protocolVersion fits the request but, echoed in full, not the answer.
        String version = "2025-" + "x".repeat(850);
        Request lengthy =
                request(
                        "{\"jsonrpc\":\"2.0\",\"id\":5,\"method\":\"initialize\",\"params\":"
                                + "{\"protocolVersion\":\""
                                + version
                                + "\",\"capabilities\":{},"
                                + "\"clientInfo\":{\"name\":\"c\",\"version\":\"1\"}}}",
                        McpProtocol.MODERN_VERSION);
        lengthy.getHeaders().set("Mcp-Method", "initialize");
        assertUnsupported(adapter.handle(lengthy), 5, version.substring(0, 64));
    }

    /** Without headers, an unsupported _meta version gets the same list, through dispatch. */
    @Test
    void anUnsupportedMetaVersionIsToldTheSupportedOnes() {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put(
                "_meta",
                Map.of(
                        McpSchema.MetaKeys.PROTOCOL_VERSION,
                        "2027-03-15",
                        McpSchema.MetaKeys.CLIENT_CAPABILITIES,
                        Map.of()));
        JsonRpc.JSONRPCResponse answer =
                adapter.dispatch(
                                new JsonRpc.JSONRPCRequest("2.0", "tools/list", 9, params),
                                new McpRequestContext(null))
                        .orElseThrow();
        assertEquals(McpProtocol.UNSUPPORTED_PROTOCOL_VERSION, answer.error().code());
        McpSchema.UnsupportedProtocolVersionData data =
                assertInstanceOf(
                        McpSchema.UnsupportedProtocolVersionData.class, answer.error().data());
        assertEquals(List.of(McpProtocol.MODERN_VERSION, McpProtocol.VERSION), data.supported());
        assertEquals("2027-03-15", data.requested());
    }

    /** A long version is echoed cut, so that the answer stays within the message limit. */
    @Test
    void aLongVersionIsEchoedCut() throws Exception {
        String version = "2027-" + "x".repeat(900);
        assertUnsupported(send(String.format(LIST, 6), version), 6, version.substring(0, 64));
    }

    /** The Origin and Accept checks still come before the answer. */
    @Test
    void originAndAcceptStillApply() throws Exception {
        Request foreign = request(String.format(LIST, 1), "2027-03-15");
        foreign.getHeaders().set("Origin", "https://evil.example");
        Response forbidden = adapter.handle(foreign);
        assertEquals(403, forbidden.getStatus().getCode());
        assertNull(forbidden.getEntity());

        Request jsonOnly = request(String.format(LIST, 1), "2027-03-15");
        jsonOnly.getHeaders().set("Accept", "application/json");
        Response notAcceptable = adapter.handle(jsonOnly);
        assertEquals(406, notAcceptable.getStatus().getCode());
        assertNull(notAcceptable.getEntity());
    }

    /** Requests without the header, or with 2025-11-25, still reach the legacy edge. */
    @Test
    void legacyRequestsAreStillServed() throws Exception {
        Response initialize =
                send(
                        "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"initialize\",\"params\":"
                                + "{\"protocolVersion\":\"2025-11-25\",\"capabilities\":{},"
                                + "\"clientInfo\":{\"name\":\"c\",\"version\":\"1\"}}}",
                        null);
        assertEquals(200, initialize.getStatus().getCode());
        Map<String, Object> answer = McpServerAdapterTestCase.envelope(initialize);
        assertTrue(answer.containsKey("result"), answer.toString());

        Response listed = send(String.format(LIST, 2), McpProtocol.VERSION);
        assertEquals(200, listed.getStatus().getCode());
        assertTrue(McpServerAdapterTestCase.envelope(listed).containsKey("result"));
    }

    private Response send(String body, String version) {
        return adapter.handle(request(body, version));
    }

    private static Request request(String body, String version) {
        Request request = McpServerAdapterTestCase.request(body);
        if (version == null) {
            request.getHeaders().removeAll(McpProtocol.VERSION_HEADER, true);
        } else {
            request.getHeaders().set(McpProtocol.VERSION_HEADER, version);
        }
        return request;
    }

    private static void assertUnsupported(Response response, Object id, String requested)
            throws Exception {
        assertEquals(400, response.getStatus().getCode());
        Map<String, Object> envelope = McpServerAdapterTestCase.envelope(response);
        assertEquals("2.0", envelope.get("jsonrpc"), envelope.toString());
        assertTrue(envelope.containsKey("id"), envelope.toString());
        if (id instanceof Number number) {
            assertEquals(number.intValue(), ((Number) envelope.get("id")).intValue());
        } else {
            assertEquals(id, envelope.get("id"));
        }
        Map<?, ?> error = (Map<?, ?>) envelope.get("error");
        assertEquals(
                McpProtocol.UNSUPPORTED_PROTOCOL_VERSION, ((Number) error.get("code")).intValue());
        Map<?, ?> data = (Map<?, ?>) error.get("data");
        assertEquals(
                List.of(McpProtocol.MODERN_VERSION, McpProtocol.VERSION), data.get("supported"));
        assertEquals(requested, data.get("requested"));
        assertEquals("Unsupported protocol version", error.get("message"));
    }
}
