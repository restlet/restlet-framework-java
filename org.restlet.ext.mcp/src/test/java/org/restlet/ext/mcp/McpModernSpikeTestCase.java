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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.modelcontextprotocol.common.McpTransportContext;
import io.modelcontextprotocol.json.McpJsonDefaults;
import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.json.TypeRef;
import io.modelcontextprotocol.modern.JsonRpc;
import io.modelcontextprotocol.modern.JsonRpc.JSONRPCMessage;
import io.modelcontextprotocol.modern.JsonRpc.JSONRPCNotification;
import io.modelcontextprotocol.modern.JsonRpc.JSONRPCRequest;
import io.modelcontextprotocol.modern.JsonRpc.JSONRPCResponse;
import io.modelcontextprotocol.modern.JsonRpc.JSONRPCResponse.JSONRPCError;
import io.modelcontextprotocol.modern.McpSchema;
import io.modelcontextprotocol.modern.McpSchema.CallToolOutcome;
import io.modelcontextprotocol.modern.McpSchema.CallToolResult;
import io.modelcontextprotocol.modern.McpSchema.ErrorCodes;
import io.modelcontextprotocol.modern.McpSchema.Implementation;
import io.modelcontextprotocol.modern.McpSchema.MetaKeys;
import io.modelcontextprotocol.modern.McpSchema.TextContent;
import io.modelcontextprotocol.modern.McpSchema.Tool;
import io.modelcontextprotocol.modern.server.McpRequestContext;
import io.modelcontextprotocol.modern.server.McpServer;
import io.modelcontextprotocol.modern.server.McpSyncResponse;
import io.modelcontextprotocol.modern.server.McpTransportResponse;
import io.modelcontextprotocol.modern.server.feature.McpSyncTool;
import io.modelcontextprotocol.modern.server.feature.McpSyncToolRepository;
import io.modelcontextprotocol.modern.server.feature.ToolsFeature;
import io.modelcontextprotocol.modern.server.feature.ToolsPage;
import io.modelcontextprotocol.modern.server.transport.McpHeaderDecoder;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.ServerSentEvent;
import org.restlet.data.Status;
import org.restlet.representation.EventStreamRepresentation;
import org.restlet.representation.StringRepresentation;

/**
 * R6b gate 0 for MCP 2026-07-28: the MCP SDK's modern stateless server (mcp-core 2.2.0-M2, a
 * preview) behind a test-only Restlet transport, mirroring the SDK's own servlet transport, and
 * called as the Ikanos client calls a server. Test-only: no production API is implied.
 */
class McpModernSpikeTestCase {
    private static final Duration TIMEOUT = Duration.ofSeconds(10);
    private static final McpJsonMapper MAPPER = McpJsonDefaults.getMapper();
    private static final String VERSION = McpSchema.LATEST_PROTOCOL_VERSION;

    private static final Tool ECHO =
            Tool.builder(
                            "echo",
                            Map.of(
                                    "type",
                                    "object",
                                    "properties",
                                    Map.of("text", Map.of("type", "string"))))
                    .description("Echoes text")
                    .build();

    private static final Tool PROGRESS =
            Tool.builder("progress", Map.of("type", "object"))
                    .description("Reports progress, then answers")
                    .build();

    private McpServer sdkServer;
    private Server server;
    private HttpClient http;
    private String target;

    @BeforeEach
    void start() throws Exception {
        McpSyncToolRepository tools =
                new McpSyncToolRepository() {
                    @Override
                    public ToolsPage list(McpRequestContext ctx, String cursor) {
                        return ToolsPage.of(List.of(ECHO, PROGRESS));
                    }

                    @Override
                    public McpSyncTool find(McpRequestContext ctx, String name) {
                        if (ECHO.name().equals(name)) {
                            return McpSyncTool.of(
                                    ECHO,
                                    (context, request) ->
                                            McpSyncResponse.<CallToolOutcome>result(
                                                    text(
                                                            String.valueOf(
                                                                    request.arguments()
                                                                            .get("text")))));
                        }
                        if (PROGRESS.name().equals(name)) {
                            // A streaming reply: a progress notification, then the result.
                            return McpSyncTool.of(
                                    PROGRESS,
                                    (context, request) ->
                                            McpSyncResponse.<CallToolOutcome>streaming(
                                                    notifier -> {
                                                        notifier.progress(1, 2.0, "half");
                                                        return text("done");
                                                    }));
                        }
                        return null;
                    }
                };
        sdkServer =
                McpServer.builder()
                        .serverInfo(Implementation.builder("restlet-modern-spike", "1").build())
                        .feature(ToolsFeature.ofSync(tools))
                        .build();
        server = new Server(new Context(), Protocol.HTTP, 0, new ModernHost(sdkServer));
        server.start();
        target = "http://localhost:" + server.getActualPort() + "/mcp";
        http = HttpClient.newHttpClient();
    }

    @AfterEach
    void stop() throws Exception {
        try {
            if (http != null) {
                http.close();
            }
        } finally {
            try {
                if (sdkServer != null) {
                    sdkServer.closeGracefully();
                }
            } finally {
                if (server != null) {
                    server.stop();
                }
            }
        }
    }

    /** Discovery replaces initialize: versions, capabilities and the server identity. */
    @Test
    void serverDiscoverAdvertisesTheRevision() throws Exception {
        Reply reply = call("server/discover", Map.of(), null);
        assertEquals(200, reply.status, reply.body);
        Map<String, Object> result = reply.result();
        assertEquals(List.of(VERSION), result.get("supportedVersions"));
        assertEquals("complete", result.get("resultType"));
        assertTrue(((Map<?, ?>) result.get("capabilities")).containsKey("tools"), reply.body);
        assertEquals(
                "restlet-modern-spike",
                ((Map<?, ?>) ((Map<?, ?>) result.get("_meta")).get(MetaKeys.SERVER_INFO))
                        .get("name"));
    }

    /** A tool is listed and called as Ikanos calls one, with a plain or base64 Mcp-Name. */
    @Test
    void toolsAreListedAndCalledAsIkanosCallsThem() throws Exception {
        Reply list = call("tools/list", Map.of(), null);
        assertEquals(200, list.status, list.body);
        assertEquals(
                "echo", ((Map<?, ?>) ((List<?>) list.result().get("tools")).get(0)).get("name"));
        assertNotNull(list.result().get("ttlMs"), list.body);
        assertNotNull(list.result().get("cacheScope"), list.body);

        Map<String, Object> params = Map.of("name", "echo", "arguments", Map.of("text", "hello"));
        Reply plain = call("tools/call", params, "echo");
        assertEquals(200, plain.status, plain.body);
        assertEquals("hello", firstText(plain.result()));
        assertEquals("complete", plain.result().get("resultType"));

        String encoded =
                "=?base64?"
                        + Base64.getEncoder()
                                .encodeToString("echo".getBytes(StandardCharsets.UTF_8))
                        + "?=";
        Reply base64 = call("tools/call", params, encoded);
        assertEquals(200, base64.status, base64.body);
        assertEquals("hello", firstText(base64.result()));
    }

    /**
     * A streaming reply comes as an SSE stream over core SSE: the progress notification, then the
     * result answering the request, then the end of the stream.
     */
    @Test
    void aStreamingReplyCarriesProgressThenTheResult() throws Exception {
        String request =
                body(
                        "tools/call",
                        Map.of("name", "progress", "arguments", Map.of()),
                        Map.of(
                                MetaKeys.PROTOCOL_VERSION,
                                VERSION,
                                MetaKeys.CLIENT_CAPABILITIES,
                                Map.of(),
                                MetaKeys.PROGRESS_TOKEN,
                                "p1"));
        HttpRequest.Builder post =
                HttpRequest.newBuilder(URI.create(target))
                        .timeout(TIMEOUT)
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(request));
        headers("tools/call", VERSION, "progress").forEach(post::header);
        // Returns once the stream has ended.
        HttpResponse<String> response =
                http.send(post.build(), HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), response.body());
        assertTrue(
                response.headers()
                        .firstValue("Content-Type")
                        .orElse("")
                        .startsWith("text/event-stream"),
                response.headers().toString());
        List<Map<String, Object>> events = events(response.body());
        assertEquals(2, events.size(), response.body());

        Map<String, Object> progress = events.get(0);
        assertEquals("notifications/progress", progress.get("method"));
        Map<?, ?> params = (Map<?, ?>) progress.get("params");
        assertEquals("p1", params.get("progressToken"));
        assertEquals("half", params.get("message"));

        Map<String, Object> answer = events.get(1);
        assertEquals(1, ((Number) answer.get("id")).intValue());
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) answer.get("result");
        assertEquals("done", firstText(result));
        assertEquals("complete", result.get("resultType"));
    }

    /** Header and version errors use the 2026-07-28 codes and the HTTP statuses Ikanos uses. */
    @Test
    void protocolErrorsMatchTheRevision() throws Exception {
        Map<String, Object> params = Map.of("name", "echo", "arguments", Map.of("text", "x"));

        Reply mismatch = call("tools/call", params, "other");
        assertEquals(400, mismatch.status, mismatch.body);
        assertEquals(ErrorCodes.HEADER_MISMATCH, mismatch.errorCode());

        Reply noMethod = send(body("tools/list", Map.of(), VERSION), headers(null, VERSION, null));
        assertEquals(400, noMethod.status, noMethod.body);
        assertEquals(ErrorCodes.HEADER_MISMATCH, noMethod.errorCode());

        Reply oldVersion =
                send(
                        body("tools/list", Map.of(), "2025-11-25"),
                        headers("tools/list", "2025-11-25", null));
        assertEquals(400, oldVersion.status, oldVersion.body);
        assertEquals(ErrorCodes.UNSUPPORTED_PROTOCOL_VERSION, oldVersion.errorCode());
        assertEquals(
                List.of(VERSION), ((Map<?, ?>) oldVersion.error().get("data")).get("supported"));

        Reply unknown = call("nothing/here", Map.of(), null);
        assertEquals(404, unknown.status, unknown.body);
        assertEquals(ErrorCodes.METHOD_NOT_FOUND, unknown.errorCode());
    }

    /**
     * Legacy traffic: GET is refused with 405, a notification gets 202, and a 2025-11-25 initialize
     * is refused, which a dual-era adapter would route to the stateless server instead.
     */
    @Test
    void legacyTrafficIsRefused() throws Exception {
        HttpResponse<String> get =
                http.send(
                        HttpRequest.newBuilder(URI.create(target)).timeout(TIMEOUT).GET().build(),
                        HttpResponse.BodyHandlers.ofString());
        assertEquals(405, get.statusCode());

        String notification =
                "{\"jsonrpc\":\"2.0\",\"method\":\"notifications/cancelled\",\"params\":{\"requestId\":7}}";
        Reply accepted = send(notification, headers("notifications/cancelled", VERSION, null));
        assertEquals(202, accepted.status, accepted.body);

        String initialize =
                "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"initialize\",\"params\":"
                        + "{\"protocolVersion\":\"2025-11-25\",\"capabilities\":{},"
                        + "\"clientInfo\":{\"name\":\"legacy\",\"version\":\"1\"}}}";
        Reply legacy = send(initialize, headers("initialize", null, null));
        // The SDK refuses it as invalid params; Ikanos answers -32022 with the supported version.
        assertEquals(400, legacy.status, legacy.body);
        assertEquals(ErrorCodes.INVALID_PARAMS, legacy.errorCode());
    }

    /**
     * Every 2026-07-28 request carries its client capabilities in _meta. Ikanos' client sends only
     * the protocol version, which the SDK refuses: the client must declare its capabilities, even
     * empty, to call an SDK-based server.
     */
    @Test
    void aRequestWithoutClientCapabilitiesIsRefused() throws Exception {
        String request = body("tools/list", Map.of(), Map.of(MetaKeys.PROTOCOL_VERSION, VERSION));
        Reply reply = send(request, headers("tools/list", VERSION, null));
        assertEquals(400, reply.status, reply.body);
        assertEquals(ErrorCodes.INVALID_PARAMS, reply.errorCode());
        assertTrue(
                ((String) reply.error().get("message")).contains(MetaKeys.CLIENT_CAPABILITIES),
                reply.body);
    }

    private Reply call(String method, Map<String, Object> params, String name) throws Exception {
        return send(body(method, params, VERSION), headers(method, VERSION, name));
    }

    /** A request as the 2026-07-28 revision requires: version and client capabilities in _meta. */
    private static String body(String method, Map<String, Object> params, String version)
            throws IOException {
        return body(
                method,
                params,
                Map.of(MetaKeys.PROTOCOL_VERSION, version, MetaKeys.CLIENT_CAPABILITIES, Map.of()));
    }

    /** A request with the given _meta. */
    private static String body(String method, Map<String, Object> params, Map<String, Object> meta)
            throws IOException {
        Map<String, Object> withMeta = new LinkedHashMap<>(params);
        withMeta.put("_meta", meta);
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("jsonrpc", "2.0");
        request.put("id", 1);
        request.put("method", method);
        request.put("params", withMeta);
        return MAPPER.writeValueAsString(request);
    }

    private static Map<String, String> headers(String method, String version, String name) {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Accept", "application/json, text/event-stream");
        if (version != null) {
            headers.put("MCP-Protocol-Version", version);
        }
        if (method != null) {
            headers.put("Mcp-Method", method);
        }
        if (name != null) {
            headers.put("Mcp-Name", name);
        }
        return headers;
    }

    private Reply send(String body, Map<String, String> headers) throws Exception {
        HttpRequest.Builder request =
                HttpRequest.newBuilder(URI.create(target))
                        .timeout(TIMEOUT)
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(body));
        headers.forEach(request::header);
        HttpResponse<String> response =
                http.send(request.build(), HttpResponse.BodyHandlers.ofString());
        return new Reply(response.statusCode(), response.body());
    }

    private static CallToolResult text(String text) {
        return CallToolResult.builder().addContent(TextContent.builder(text).build()).build();
    }

    /** Parses the data lines of an SSE body, one JSON-RPC message per event. */
    private static List<Map<String, Object>> events(String body) throws IOException {
        List<Map<String, Object>> events = new java.util.ArrayList<>();
        for (String line : body.split("\n")) {
            if (line.startsWith("data:")) {
                events.add(
                        MAPPER.readValue(
                                line.substring(5).trim(), new TypeRef<Map<String, Object>>() {}));
            }
        }
        return events;
    }

    private static String firstText(Map<String, Object> result) {
        return (String) ((Map<?, ?>) ((List<?>) result.get("content")).get(0)).get("text");
    }

    private record Reply(int status, String body) {
        Map<String, Object> json() throws IOException {
            return MAPPER.readValue(body, new TypeRef<Map<String, Object>>() {});
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> result() throws IOException {
            return (Map<String, Object>) json().get("result");
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> error() throws IOException {
            return (Map<String, Object>) json().get("error");
        }

        int errorCode() throws IOException {
            return ((Number) error().get("code")).intValue();
        }
    }

    /**
     * The test-only Restlet transport: what the SDK's HttpServletMcpTransport does, on the Restlet
     * API. POST only; header checks against the body; one JSON reply, an error mapped to its HTTP
     * status, or an SSE stream for a streaming reply.
     */
    private static final class ModernHost extends Restlet {
        private static final Map<Integer, Status> ERROR_STATUSES =
                Map.of(
                        ErrorCodes.PARSE_ERROR, Status.CLIENT_ERROR_BAD_REQUEST,
                        ErrorCodes.INVALID_REQUEST, Status.CLIENT_ERROR_BAD_REQUEST,
                        ErrorCodes.INVALID_PARAMS, Status.CLIENT_ERROR_BAD_REQUEST,
                        ErrorCodes.HEADER_MISMATCH, Status.CLIENT_ERROR_BAD_REQUEST,
                        ErrorCodes.MISSING_REQUIRED_CLIENT_CAPABILITY,
                                Status.CLIENT_ERROR_BAD_REQUEST,
                        ErrorCodes.UNSUPPORTED_PROTOCOL_VERSION, Status.CLIENT_ERROR_BAD_REQUEST,
                        ErrorCodes.METHOD_NOT_FOUND, Status.CLIENT_ERROR_NOT_FOUND);

        private final McpServer sdk;

        private ModernHost(McpServer sdk) {
            this.sdk = sdk;
        }

        @Override
        public void handle(Request request, Response response) {
            if (!Method.POST.equals(request.getMethod())) {
                response.setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
                response.getAllowedMethods().add(Method.POST);
                return;
            }
            if (request.getEntity() == null
                    || !MediaType.APPLICATION_JSON.isCompatible(
                            request.getEntity().getMediaType())) {
                response.setStatus(Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE);
                return;
            }
            JSONRPCMessage message;
            try {
                message = JsonRpc.deserializeMessage(MAPPER, request.getEntity().getText());
            } catch (JsonRpc.InvalidMessageException e) {
                error(response, e.id(), ErrorCodes.INVALID_REQUEST, "Invalid JSON-RPC message");
                return;
            } catch (IOException _) {
                error(response, null, ErrorCodes.PARSE_ERROR, "Parse error");
                return;
            }
            if (message instanceof JSONRPCNotification notification) {
                sdk.handleNotification(McpTransportContext.EMPTY, notification).block(TIMEOUT);
                response.setStatus(Status.SUCCESS_ACCEPTED);
                return;
            }
            if (!(message instanceof JSONRPCRequest rpc)) {
                error(response, null, ErrorCodes.INVALID_REQUEST, "Requests or notifications only");
                return;
            }
            String mismatch = mismatch(request, rpc);
            if (mismatch != null) {
                error(response, rpc.id(), ErrorCodes.HEADER_MISMATCH, mismatch);
                return;
            }
            McpTransportResponse reply =
                    sdk.handleBlocking(McpTransportContext.EMPTY, rpc).block(TIMEOUT);
            if (reply instanceof McpTransportResponse.Result result) {
                json(response, Status.SUCCESS_OK, result.response());
            } else if (reply instanceof McpTransportResponse.Error failure) {
                json(
                        response,
                        ERROR_STATUSES.getOrDefault(
                                failure.response().error().code(), Status.SUCCESS_OK),
                        failure.response());
            } else if (reply instanceof McpTransportResponse.Streaming streaming) {
                response.setEntity(
                        new EventStreamRepresentation(
                                sink ->
                                        streaming
                                                .messages()
                                                .doOnNext(
                                                        each -> {
                                                            try {
                                                                sink.send(
                                                                        ServerSentEvent.builder()
                                                                                .event("message")
                                                                                .data(
                                                                                        MAPPER
                                                                                                .writeValueAsString(
                                                                                                        each))
                                                                                .build());
                                                            } catch (IOException e) {
                                                                throw new java.io
                                                                        .UncheckedIOException(e);
                                                            }
                                                        })
                                                .blockLast(TIMEOUT)));
            }
        }

        /** The servlet transport's header checks, on Restlet headers. */
        private static String mismatch(Request request, JSONRPCRequest rpc) {
            String method = header(request, "Mcp-Method");
            if (method == null) {
                return "Missing required header: Mcp-Method";
            }
            if (!method.equals(rpc.method())) {
                return "Mcp-Method header does not match request method";
            }
            Map<?, ?> params = rpc.params() instanceof Map<?, ?> map ? map : Map.of();
            if (params.get("_meta") instanceof Map<?, ?> meta
                    && meta.get(MetaKeys.PROTOCOL_VERSION) instanceof String version
                    && !version.equals(header(request, "MCP-Protocol-Version"))) {
                return "MCP-Protocol-Version header does not match _meta protocol version";
            }
            String key =
                    switch (rpc.method()) {
                        case "tools/call", "prompts/get" -> "name";
                        case "resources/read" -> "uri";
                        default -> null;
                    };
            if (key != null && params.get(key) instanceof String expected) {
                String name = header(request, "Mcp-Name");
                String decoded = name == null ? null : McpHeaderDecoder.decodeValue(name);
                if (!expected.equals(decoded)) {
                    return "Mcp-Name header does not match request name/uri";
                }
            }
            return null;
        }

        private static String header(Request request, String name) {
            return request.getHeaders().getFirstValue(name, true);
        }

        private static void error(Response response, Object id, int code, String message) {
            json(
                    response,
                    ERROR_STATUSES.getOrDefault(code, Status.SUCCESS_OK),
                    JSONRPCResponse.error(id, new JSONRPCError(code, message)));
        }

        private static void json(Response response, Status status, JSONRPCResponse message) {
            try {
                response.setStatus(status);
                response.setEntity(
                        new StringRepresentation(
                                MAPPER.writeValueAsString(message), MediaType.APPLICATION_JSON));
            } catch (IOException _) {
                response.setStatus(Status.SERVER_ERROR_INTERNAL);
            }
        }
    }
}
