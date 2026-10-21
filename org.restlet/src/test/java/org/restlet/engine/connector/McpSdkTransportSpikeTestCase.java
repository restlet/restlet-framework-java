/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.connector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.modelcontextprotocol.client.McpAsyncClient;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.common.McpTransportContext;
import io.modelcontextprotocol.json.McpJsonDefaults;
import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.json.TypeRef;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpStatelessAsyncServer;
import io.modelcontextprotocol.server.McpStatelessServerHandler;
import io.modelcontextprotocol.spec.McpClientTransport;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpStatelessServerTransport;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
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
import org.restlet.engine.Engine;
import org.restlet.representation.StringRepresentation;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

class McpSdkTransportSpikeTestCase {

    private static final int MAX_MESSAGE_BYTES = 64 * 1024;

    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(5);

    private Client client;

    private McpAsyncClient sdkClient;

    private McpStatelessAsyncServer sdkServer;

    private McpAsyncClient stdioClient;

    private Server server;

    private final AtomicInteger toolCalls = new AtomicInteger();

    @BeforeEach
    void setUp() {
        Engine.clearThreadLocalVariables();
        Engine.register(false);
        Engine.getInstance().getRegisteredServers().add(new HttpServerHelper(null));
        Engine.getInstance().getRegisteredClients().add(new HttpClientHelper(null));
    }

    @AfterEach
    void tearDown() throws Exception {
        if (sdkServer != null) {
            sdkServer.close();
        }
        if (stdioClient != null) {
            stdioClient.close();
        }
        if (sdkClient != null) {
            sdkClient.close();
        }
        if (client != null) {
            client.stop();
        }
        if (server != null) {
            server.stop();
        }
        Engine.clearThreadLocalVariables();
    }

    private void startHttpServer(boolean ikanosProfile) throws Exception {
        McpJsonMapper mapper = McpJsonDefaults.getMapper();
        RestletHttpTransport transport = new RestletHttpTransport(mapper, ikanosProfile);
        var tool =
                new McpSchema.Tool(
                        "echo",
                        null,
                        "Returns the supplied text",
                        Map.of(
                                "type",
                                "object",
                                "properties",
                                Map.of("text", Map.of("type", "string")),
                                "required",
                                java.util.List.of("text")),
                        null,
                        null,
                        null,
                        null);
        sdkServer =
                McpServer.async(transport)
                        .serverInfo("restlet-r0-spike", "1")
                        .toolCall(
                                tool,
                                (context, call) -> {
                                    toolCalls.incrementAndGet();
                                    return Mono.just(
                                            McpSchema.CallToolResult.builder()
                                                    .isError(
                                                            "fail"
                                                                    .equals(
                                                                            call.arguments()
                                                                                    .get("text")))
                                                    .addTextContent(
                                                            "echo:" + call.arguments().get("text"))
                                                    .build());
                                })
                        .build();

        Context serverContext = new Context();
        serverContext.getParameters().add("http.transport.protocol", "HTTP2");
        server = new Server(serverContext, Protocol.HTTP, 0, transport);
        server.start();

        Context clientContext = new Context();
        clientContext.getParameters().add("httpClientTransportMode", "HTTP2");
        client = new Client(clientContext, Protocol.HTTP);
        client.start();
    }

    @Test
    void dispatchesMcpSdkClientAndServerThroughRestletHttp2() throws Exception {
        startHttpServer(false);
        McpJsonMapper mapper = McpJsonDefaults.getMapper();
        RestletHttpClientTransport clientTransport =
                new RestletHttpClientTransport(
                        client, mapper, "http://localhost:" + server.getActualPort() + "/mcp");
        sdkClient =
                McpClient.async(clientTransport)
                        .requestTimeout(REQUEST_TIMEOUT)
                        .initializationTimeout(REQUEST_TIMEOUT)
                        .build();

        try {
            sdkClient.initialize().block(REQUEST_TIMEOUT);
        } catch (RuntimeException e) {
            throw new AssertionError(
                    "MCP initialization failed. Request: "
                            + clientTransport.lastRequestPayload.get()
                            + "; response: "
                            + clientTransport.lastResponsePayload.get(),
                    e);
        }
        McpSchema.ListToolsResult listedTools = sdkClient.listTools().block(REQUEST_TIMEOUT);
        McpSchema.CallToolResult result =
                sdkClient
                        .callTool(
                                new McpSchema.CallToolRequest(
                                        "echo", Map.of("text", "hello"), null))
                        .block(REQUEST_TIMEOUT);

        assertTrue(listedTools.tools().stream().anyMatch(item -> item.name().equals("echo")));
        assertEquals("echo:hello", ((McpSchema.TextContent) result.content().getFirst()).text());
        assertEquals("2.0", clientTransport.lastResponseProtocol.get().getVersion());

        String profileRequestBody =
                "{\"jsonrpc\":\"2.0\",\"id\":7,\"method\":\"tools/list\",\"params\":{\"_meta\":{\"io.modelcontextprotocol/protocolVersion\":\"2026-07-28\"}}}";
        Request statelessRequest =
                new Request(
                        Method.POST,
                        "http://localhost:" + server.getActualPort() + "/mcp",
                        new StringRepresentation(profileRequestBody, MediaType.APPLICATION_JSON));
        statelessRequest.getHeaders().add("Mcp-Method", "tools/list");
        statelessRequest.getHeaders().add("Mcp-Protocol-Version", "2026-07-28");
        Response statelessResponse = client.handle(statelessRequest);
        assertEquals(org.restlet.data.Status.SUCCESS_OK, statelessResponse.getStatus());
        assertEquals("2.0", statelessResponse.getProtocol().getVersion());
        try (InputStream input = statelessResponse.getEntity().getStream()) {
            String body =
                    new String(
                            input.readNBytes(MAX_MESSAGE_BYTES),
                            java.nio.charset.StandardCharsets.UTF_8);
            assertTrue(body.contains("\"echo\""));
        }
    }

    @Test
    void bridgesIkanosToolResultsWithoutAnSdkSession() throws Exception {
        startHttpServer(true);
        McpSchema.JSONRPCResponse listed = profileRequest("tools/list", Map.of(), Map.of());
        assertNull(listed.error());
        Map<String, Object> listing = resultMap(listed.result());
        assertEquals("complete", listing.get("resultType"));
        assertTrue(
                listing.get("tools") instanceof List<?> tools
                        && tools.size() == 1
                        && tools.getFirst() instanceof Map<?, ?> tool
                        && "echo".equals(tool.get("name")));

        McpSchema.JSONRPCResponse called =
                profileRequest(
                        "tools/call",
                        Map.of("name", "echo", "arguments", Map.of("text", "profile")),
                        Map.of("Mcp-Name", "=?base64?ZWNobw==?="));
        assertNull(called.error());
        Map<String, Object> result = resultMap(called.result());
        assertEquals("complete", result.get("resultType"));
        assertEquals(
                List.of(Map.of("type", "text", "text", "echo:profile")), result.get("content"));
        assertEquals(false, result.get("isError"));
        assertEquals(1, toolCalls.get());

        McpSchema.JSONRPCResponse failed =
                profileRequest(
                        "tools/call",
                        Map.of("name", "echo", "arguments", Map.of("text", "fail")),
                        Map.of());
        assertNull(failed.error());
        Map<String, Object> failedResult = resultMap(failed.result());
        assertEquals("complete", failedResult.get("resultType"));
        assertEquals(true, failedResult.get("isError"));
        assertEquals(
                List.of(Map.of("type", "text", "text", "echo:fail")), failedResult.get("content"));
        assertEquals(2, toolCalls.get());
    }

    @Test
    void pinnedSdkCannotRepresentNullIdParseErrors() {
        var parseError = new McpSchema.JSONRPCResponse.JSONRPCError(-32700, "Parse error");
        assertEquals(parseError, McpSchema.JSONRPCResponse.error(17, parseError).error());
        IllegalArgumentException rejected =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> McpSchema.JSONRPCResponse.error(null, parseError));
        assertTrue(rejected.getMessage().contains("null IDs"));
    }

    @Test
    void comparesToolsProfileWithRecordedIkanosResponses() throws Exception {
        startHttpServer(true);
        McpIkanosDifferentialProbe.runRecorded(client, endpoint());
    }

    @Test
    @EnabledIfSystemProperty(named = "restlet.mcp.ikanos.home", matches = ".+")
    void comparesToolsProfileWithRealIkanosEndpoint() throws Exception {
        startHttpServer(true);
        McpIkanosDifferentialProbe.run(client, endpoint());
    }

    @Test
    void rejectsIkanosProfileMismatchesBeforeToolDispatch() throws Exception {
        startHttpServer(true);
        Map<String, Object> params = Map.of("name", "echo", "arguments", Map.of("text", "unused"));
        for (Map<String, String> headers :
                List.of(
                        Map.of("Mcp-Method", ""),
                        Map.of("Mcp-Method", "tools/list"),
                        Map.of("MCP-Protocol-Version", ""),
                        Map.of("MCP-Protocol-Version", "2025-11-25"),
                        Map.of("Mcp-Name", ""),
                        Map.of("Mcp-Name", "wrong"),
                        Map.of("Mcp-Name", "=?base64?%%%?="))) {
            McpSchema.JSONRPCResponse rejected = profileRequest("tools/call", params, headers);
            assertEquals(-32020, rejected.error().code());
            assertNull(rejected.result());
        }
        McpSchema.JSONRPCResponse unsupported =
                profileRequest(
                        "tools/call",
                        Map.of(
                                "name",
                                "echo",
                                "_meta",
                                Map.of("io.modelcontextprotocol/protocolVersion", "2025-11-25")),
                        Map.of("MCP-Protocol-Version", "2025-11-25"));
        assertEquals(-32022, unsupported.error().code());
        assertEquals(
                Map.of("supported", List.of("2026-07-28"), "requested", "2025-11-25"),
                unsupported.error().data());
        assertEquals(0, toolCalls.get());
        for (Method method : List.of(Method.GET, Method.DELETE)) {
            Response response = client.handle(new Request(method, endpoint()));
            try {
                assertEquals(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED, response.getStatus());
            } finally {
                if (response.getEntity() != null) {
                    response.getEntity().release();
                }
            }
        }
    }

    private String endpoint() {
        return "http://localhost:" + server.getActualPort() + "/mcp";
    }

    private static Map<String, Object> resultMap(Object value) {
        return McpJsonDefaults.getMapper()
                .convertValue(value, new TypeRef<Map<String, Object>>() {});
    }

    private McpSchema.JSONRPCResponse profileRequest(
            String method, Map<String, Object> params, Map<String, String> overrides)
            throws IOException {
        Map<String, Object> bodyParams = new LinkedHashMap<>(params);
        bodyParams.putIfAbsent(
                "_meta", Map.of("io.modelcontextprotocol/protocolVersion", "2026-07-28"));
        McpJsonMapper mapper = McpJsonDefaults.getMapper();
        Request request =
                new Request(
                        Method.POST,
                        endpoint(),
                        new StringRepresentation(
                                mapper.writeValueAsString(
                                        new McpSchema.JSONRPCRequest(method, 17, bodyParams)),
                                MediaType.APPLICATION_JSON));
        Map<String, String> headers =
                new LinkedHashMap<>(
                        Map.of(
                                "Mcp-Method",
                                method,
                                "MCP-Protocol-Version",
                                "2026-07-28",
                                "Mcp-Name",
                                "echo"));
        headers.putAll(overrides);
        headers.forEach(
                (name, value) -> {
                    if (!value.isEmpty()) {
                        request.getHeaders().add(name.toLowerCase(Locale.ROOT), value);
                    }
                });
        Response response = client.handle(request);
        try (InputStream input = response.getEntity().getStream()) {
            assertEquals("2.0", response.getProtocol().getVersion());
            assertNull(response.getHeaders().getFirstValue("Mcp-Session-Id", true));
            byte[] bytes = input.readNBytes(MAX_MESSAGE_BYTES + 1);
            assertTrue(bytes.length <= MAX_MESSAGE_BYTES);
            McpSchema.JSONRPCMessage message =
                    McpSchema.deserializeJsonRpcMessage(
                            mapper, new String(bytes, StandardCharsets.UTF_8));
            McpSchema.JSONRPCResponse rpcResponse =
                    assertInstanceOf(McpSchema.JSONRPCResponse.class, message);
            assertEquals(17, rpcResponse.id());
            assertEquals(
                    rpcResponse.error() == null
                            ? Status.SUCCESS_OK
                            : Status.CLIENT_ERROR_BAD_REQUEST,
                    response.getStatus());
            return rpcResponse;
        } finally {
            response.getEntity().release();
        }
    }

    @Test
    void dispatchesMcpSdkClientAndServerThroughOfficialStdioTransports() throws Exception {
        McpJsonMapper mapper = McpJsonDefaults.getMapper();
        String javaExecutable =
                Path.of(
                                System.getProperty("java.home"),
                                "bin",
                                System.getProperty("os.name").toLowerCase().contains("win")
                                        ? "java.exe"
                                        : "java")
                        .toString();
        String classPath =
                System.getProperty(
                        "surefire.test.class.path", System.getProperty("java.class.path"));
        assertTrue(Files.isRegularFile(Path.of(javaExecutable)));
        assertTrue(classPath.contains("test-classes") || classPath.contains("surefire"));
        ServerParameters parameters =
                ServerParameters.builder(javaExecutable)
                        .args("-cp", classPath, McpStdioSdkServerProcess.class.getName())
                        .build();
        StdioClientTransport transport = new StdioClientTransport(parameters, mapper);
        stdioClient =
                McpClient.async(transport)
                        .requestTimeout(REQUEST_TIMEOUT)
                        .initializationTimeout(REQUEST_TIMEOUT)
                        .build();

        stdioClient.initialize().block(REQUEST_TIMEOUT);
        McpSchema.ListToolsResult listedTools = stdioClient.listTools().block(REQUEST_TIMEOUT);
        McpSchema.CallToolResult result =
                stdioClient
                        .callTool(
                                new McpSchema.CallToolRequest(
                                        "echo", Map.of("text", "stdio"), null))
                        .block(REQUEST_TIMEOUT);

        assertTrue(listedTools.tools().stream().anyMatch(item -> item.name().equals("echo")));
        assertEquals("echo:stdio", ((McpSchema.TextContent) result.content().getFirst()).text());
    }

    private static final class RestletHttpTransport extends Restlet
            implements McpStatelessServerTransport {

        private final McpJsonMapper jsonMapper;

        private volatile McpStatelessServerHandler handler;

        private final boolean ikanosProfile;

        private RestletHttpTransport(McpJsonMapper jsonMapper, boolean ikanosProfile) {
            this.jsonMapper = jsonMapper;
            this.ikanosProfile = ikanosProfile;
        }

        @Override
        public void setMcpHandler(McpStatelessServerHandler handler) {
            this.handler = handler;
        }

        @Override
        public Mono<Void> closeGracefully() {
            return Mono.empty();
        }

        @Override
        public void handle(Request request, Response response) {
            if (ikanosProfile && !Method.POST.equals(request.getMethod())) {
                response.setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
                response.setEntity(
                        request.getMethod().getName() + " not supported", MediaType.TEXT_PLAIN);
                return;
            }
            McpStatelessServerHandler currentHandler = handler;
            if (currentHandler == null) {
                response.setStatus(org.restlet.data.Status.SERVER_ERROR_SERVICE_UNAVAILABLE);
                return;
            }
            if (!request.isEntityAvailable()) {
                response.setStatus(org.restlet.data.Status.CLIENT_ERROR_BAD_REQUEST);
                if (ikanosProfile) {
                    try {
                        writeParseError(response, "Parse error: empty body");
                    } catch (IOException e) {
                        response.setStatus(Status.SERVER_ERROR_INTERNAL);
                        getLogger()
                                .warning(
                                        "Unable to encode MCP spike parse error: "
                                                + e.getMessage());
                    }
                }
                return;
            }

            try (var input = request.getEntity().getStream()) {
                byte[] message = input.readNBytes(MAX_MESSAGE_BYTES + 1);
                if (message.length > MAX_MESSAGE_BYTES) {
                    response.setStatus(
                            org.restlet.data.Status.CLIENT_ERROR_REQUEST_ENTITY_TOO_LARGE);
                    return;
                }
                String payload = new String(message, StandardCharsets.UTF_8);
                McpSchema.JSONRPCMessage parsed;
                if (ikanosProfile && payload.isBlank()) {
                    writeParseError(response, "Parse error: empty body");
                    return;
                }
                try {
                    parsed = McpSchema.deserializeJsonRpcMessage(jsonMapper, payload);
                } catch (IOException e) {
                    if (!ikanosProfile) {
                        throw e;
                    }
                    writeParseError(response, "Parse error: " + e.getMessage());
                    return;
                }
                if (parsed instanceof McpSchema.JSONRPCRequest rpcRequest) {
                    if (ikanosProfile && "initialize".equals(rpcRequest.method())) {
                        Object version = params(rpcRequest).get("protocolVersion");
                        String requested = version instanceof String text ? text : "";
                        writeProfileResponse(
                                response,
                                profileError(
                                        rpcRequest,
                                        -32022,
                                        "Unsupported protocol version"
                                                + ("".equals(requested) ? "" : " " + requested)
                                                + ": this server supports only MCP 2026-07-28, which has no initialize handshake",
                                        Map.of(
                                                "supported",
                                                List.of("2026-07-28"),
                                                "requested",
                                                requested)));
                        return;
                    }
                    McpSchema.JSONRPCResponse profileError =
                            ikanosProfile ? validateProfile(request, rpcRequest) : null;
                    if (profileError != null) {
                        writeProfileResponse(response, profileError);
                        return;
                    }
                    if (ikanosProfile && "server/discover".equals(rpcRequest.method())) {
                        writeProfileResponse(
                                response,
                                profileResult(
                                        rpcRequest,
                                        Map.of(
                                                "supportedVersions", List.of("2026-07-28"),
                                                "capabilities", Map.of("tools", Map.of()))));
                        return;
                    }
                    McpSchema.JSONRPCResponse rpcResponse =
                            currentHandler
                                    .handleRequest(McpTransportContext.EMPTY, rpcRequest)
                                    .block(REQUEST_TIMEOUT);
                    if (rpcResponse == null) {
                        response.setStatus(org.restlet.data.Status.SERVER_ERROR_INTERNAL);
                        return;
                    }
                    if (ikanosProfile) {
                        if (rpcResponse.error() == null
                                && List.of("tools/list", "tools/call")
                                        .contains(rpcRequest.method())) {
                            rpcResponse =
                                    profileResult(rpcRequest, resultMap(rpcResponse.result()));
                        } else if (rpcResponse.error() != null
                                && rpcResponse.error().code() == -32602
                                && "tools/call".equals(rpcRequest.method())) {
                            Object name = params(rpcRequest).get("name");
                            // Only adapt the pinned SDK's unknown-tool error, not other invalid
                            // params.
                            if (("Tool not found: " + name).equals(rpcResponse.error().data())) {
                                rpcResponse =
                                        profileError(
                                                rpcRequest,
                                                -32602,
                                                "Invalid params: Unknown tool: " + name,
                                                null);
                            }
                        }
                        writeProfileResponse(response, rpcResponse);
                        return;
                    }
                    response.setEntity(
                            jsonMapper.writeValueAsString(rpcResponse), MediaType.APPLICATION_JSON);
                } else if (parsed instanceof McpSchema.JSONRPCNotification notification) {
                    currentHandler
                            .handleNotification(McpTransportContext.EMPTY, notification)
                            .block(REQUEST_TIMEOUT);
                    response.setStatus(org.restlet.data.Status.SUCCESS_ACCEPTED);
                } else {
                    response.setStatus(org.restlet.data.Status.CLIENT_ERROR_BAD_REQUEST);
                }
            } catch (IOException e) {
                response.setStatus(org.restlet.data.Status.SERVER_ERROR_INTERNAL);
                getLogger().warning("Unable to read MCP spike request: " + e.getMessage());
            }
        }

        private void writeProfileResponse(Response response, McpSchema.JSONRPCResponse rpcResponse)
                throws IOException {
            if (rpcResponse.error() != null) {
                Status status =
                        switch (rpcResponse.error().code()) {
                            case -32601 -> Status.CLIENT_ERROR_NOT_FOUND;
                            case -32700, -32600, -32602, -32020, -32021, -32022 ->
                                    Status.CLIENT_ERROR_BAD_REQUEST;
                            case -32603 -> Status.SERVER_ERROR_INTERNAL;
                            default -> {
                                getLogger()
                                        .warning(
                                                "Unmapped MCP profile error: "
                                                        + rpcResponse.error().code());
                                yield Status.SERVER_ERROR_INTERNAL;
                            }
                        };
                response.setStatus(status);
            }
            response.setEntity(
                    jsonMapper.writeValueAsString(rpcResponse), MediaType.APPLICATION_JSON);
        }

        private void writeParseError(Response response, String message) throws IOException {
            getLogger().warning("Invalid MCP profile request: " + message);
            // SDK 2.1.0 rejects null response IDs; only this proven envelope gap is SDK-external.
            Map<String, Object> envelope = new LinkedHashMap<>();
            envelope.put("jsonrpc", "2.0");
            envelope.put("id", null);
            envelope.put("error", new McpSchema.JSONRPCResponse.JSONRPCError(-32700, message));
            response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            response.setEntity(jsonMapper.writeValueAsString(envelope), MediaType.APPLICATION_JSON);
        }

        // Fixture-specific tools/discovery metadata; this is not a full Ikanos implementation.
        private static McpSchema.JSONRPCResponse profileResult(
                McpSchema.JSONRPCRequest request, Map<String, Object> sdkResult) {
            Map<String, Object> result = new LinkedHashMap<>(sdkResult);
            result.put("resultType", "complete");
            result.put(
                    "_meta",
                    Map.of(
                            "io.modelcontextprotocol/serverInfo",
                            Map.of("name", "restlet-r0-reference", "version", "unspecified")));
            if (List.of("tools/list", "server/discover").contains(request.method())) {
                result.put("ttlMs", 0);
                result.put("cacheScope", "private");
            }
            return McpSchema.JSONRPCResponse.result(request.id(), result);
        }

        private static Map<String, Object> params(McpSchema.JSONRPCRequest request) {
            return request.params() == null ? Map.of() : resultMap(request.params());
        }

        private static McpSchema.JSONRPCResponse validateProfile(
                Request request, McpSchema.JSONRPCRequest rpcRequest) {
            Map<String, Object> params = params(rpcRequest);
            Object version =
                    params.get("_meta") instanceof Map<?, ?> meta
                            ? meta.get("io.modelcontextprotocol/protocolVersion")
                            : null;
            Map<String, Object> expected = new LinkedHashMap<>();
            expected.put("Mcp-Method", rpcRequest.method());
            boolean supportedMethod =
                    List.of("tools/list", "tools/call", "server/discover")
                            .contains(rpcRequest.method());
            if (supportedMethod) {
                expected.put("MCP-Protocol-Version", version);
            }
            if ("tools/call".equals(rpcRequest.method())) {
                expected.put("Mcp-Name", params.get("name"));
            }
            for (var header : expected.entrySet()) {
                String value = request.getHeaders().getFirstValue(header.getKey(), true);
                if ("Mcp-Name".equals(header.getKey())
                        && value != null
                        && value.startsWith("=?base64?")
                        && value.endsWith("?=")) {
                    try {
                        value =
                                new String(
                                        Base64.getDecoder()
                                                .decode(value.substring(9, value.length() - 2)),
                                        StandardCharsets.UTF_8);
                    } catch (IllegalArgumentException e) {
                        return profileError(
                                rpcRequest,
                                -32020,
                                "The value of the header Mcp-Name contains invalid characters",
                                null);
                    }
                }
                if (value == null) {
                    return profileError(
                            rpcRequest,
                            -32020,
                            "Missing mandatory header: " + header.getKey(),
                            null);
                }
                if (!value.equals(header.getValue())) {
                    return profileError(
                            rpcRequest,
                            -32020,
                            "Header mismatch: %s header value '%s' does not match body value '%s'"
                                    .formatted(header.getKey(), value, header.getValue()),
                            null);
                }
            }
            if (supportedMethod && !"2026-07-28".equals(version)) {
                return profileError(
                        rpcRequest,
                        -32022,
                        "Unsupported protocol version",
                        Map.of(
                                "supported",
                                List.of("2026-07-28"),
                                "requested",
                                version == null ? "" : version));
            }
            return null;
        }

        private static McpSchema.JSONRPCResponse profileError(
                McpSchema.JSONRPCRequest request, int code, String message, Object data) {
            return McpSchema.JSONRPCResponse.error(
                    request.id(), new McpSchema.JSONRPCResponse.JSONRPCError(code, message, data));
        }
    }

    private static final class RestletHttpClientTransport implements McpClientTransport {

        private final Client client;

        private final McpJsonMapper jsonMapper;

        private final String target;

        private final AtomicReference<Protocol> lastResponseProtocol = new AtomicReference<>();

        private final AtomicReference<String> lastRequestPayload = new AtomicReference<>();

        private final AtomicReference<String> lastResponsePayload = new AtomicReference<>();

        private volatile java.util.function.Function<
                        Mono<McpSchema.JSONRPCMessage>, Mono<McpSchema.JSONRPCMessage>>
                incomingMessageHandler;

        private RestletHttpClientTransport(Client client, McpJsonMapper jsonMapper, String target) {
            this.client = client;
            this.jsonMapper = jsonMapper;
            this.target = target;
        }

        @Override
        public Mono<Void> connect(
                java.util.function.Function<
                                Mono<McpSchema.JSONRPCMessage>, Mono<McpSchema.JSONRPCMessage>>
                        handler) {
            incomingMessageHandler = handler;
            return Mono.empty();
        }

        @Override
        public Mono<Void> sendMessage(McpSchema.JSONRPCMessage message) {
            return Mono.fromRunnable(() -> sendMessageBlocking(message))
                    .subscribeOn(Schedulers.boundedElastic())
                    .then();
        }

        private void sendMessageBlocking(McpSchema.JSONRPCMessage message) {
            String payload;
            try {
                payload = jsonMapper.writeValueAsString(message);
            } catch (IOException e) {
                throw new IllegalStateException("Unable to encode MCP spike request", e);
            }
            lastRequestPayload.set(payload);

            Request request =
                    new Request(
                            Method.POST,
                            target,
                            new StringRepresentation(payload, MediaType.APPLICATION_JSON));
            Response response = client.handle(request);
            lastResponseProtocol.set(response.getProtocol());
            if (response.getEntity() == null) {
                lastResponsePayload.set("");
                return;
            }

            try {
                InputStream responseStream = response.getEntity().getStream();
                if (responseStream == null) {
                    lastResponsePayload.set("");
                    return;
                }
                try (var input = responseStream) {
                    byte[] bytes = input.readNBytes(MAX_MESSAGE_BYTES + 1);
                    if (bytes.length > MAX_MESSAGE_BYTES) {
                        throw new IllegalStateException(
                                "MCP spike response exceeds " + MAX_MESSAGE_BYTES + " bytes");
                    }
                    if (bytes.length == 0) {
                        lastResponsePayload.set("");
                        return;
                    }
                    lastResponsePayload.set(
                            new String(bytes, java.nio.charset.StandardCharsets.UTF_8));
                    McpSchema.JSONRPCMessage incoming =
                            McpSchema.deserializeJsonRpcMessage(
                                    jsonMapper, lastResponsePayload.get());
                    var handler = incomingMessageHandler;
                    if (handler == null) {
                        throw new IllegalStateException("MCP client is not connected");
                    }
                    handler.apply(Mono.just(incoming)).block(REQUEST_TIMEOUT);
                }
            } catch (IOException e) {
                throw new IllegalStateException("Unable to read MCP spike response", e);
            }
        }

        @Override
        public <T> T unmarshalFrom(Object data, io.modelcontextprotocol.json.TypeRef<T> typeRef) {
            return jsonMapper.convertValue(data, typeRef);
        }

        @Override
        public Mono<Void> closeGracefully() {
            return Mono.empty();
        }
    }
}
