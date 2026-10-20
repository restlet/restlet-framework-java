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
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.modelcontextprotocol.client.McpAsyncClient;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.common.McpTransportContext;
import io.modelcontextprotocol.json.McpJsonDefaults;
import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpStatelessAsyncServer;
import io.modelcontextprotocol.server.McpStatelessServerHandler;
import io.modelcontextprotocol.spec.McpClientTransport;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpStatelessServerTransport;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
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

    @Test
    void dispatchesMcpSdkClientAndServerThroughRestletHttp2() throws Exception {
        McpJsonMapper mapper = McpJsonDefaults.getMapper();
        McpStatelessServerTransport transport = new RestletHttpTransport(mapper);
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
                                (context, call) ->
                                        Mono.just(
                                                McpSchema.CallToolResult.builder()
                                                        .addTextContent(
                                                                "echo:"
                                                                        + call.arguments()
                                                                                .get("text"))
                                                        .build()))
                        .build();

        RestletHttpTransport restlet = (RestletHttpTransport) transport;
        Context serverContext = new Context();
        serverContext.getParameters().add("http.transport.protocol", "HTTP2");
        server = new Server(serverContext, Protocol.HTTP, 0, restlet);
        server.start();

        Context clientContext = new Context();
        clientContext.getParameters().add("httpClientTransportMode", "HTTP2");
        client = new Client(clientContext, Protocol.HTTP);
        client.start();
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

        private RestletHttpTransport(McpJsonMapper jsonMapper) {
            this.jsonMapper = jsonMapper;
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
            McpStatelessServerHandler currentHandler = handler;
            if (currentHandler == null) {
                response.setStatus(org.restlet.data.Status.SERVER_ERROR_SERVICE_UNAVAILABLE);
                return;
            }
            if (!request.isEntityAvailable()) {
                response.setStatus(org.restlet.data.Status.CLIENT_ERROR_BAD_REQUEST);
                return;
            }

            try (var input = request.getEntity().getStream()) {
                byte[] message = input.readNBytes(MAX_MESSAGE_BYTES + 1);
                if (message.length > MAX_MESSAGE_BYTES) {
                    response.setStatus(
                            org.restlet.data.Status.CLIENT_ERROR_REQUEST_ENTITY_TOO_LARGE);
                    return;
                }
                McpSchema.JSONRPCMessage parsed =
                        McpSchema.deserializeJsonRpcMessage(
                                jsonMapper,
                                new String(message, java.nio.charset.StandardCharsets.UTF_8));
                if (parsed instanceof McpSchema.JSONRPCRequest rpcRequest) {
                    McpSchema.JSONRPCResponse rpcResponse =
                            currentHandler
                                    .handleRequest(McpTransportContext.EMPTY, rpcRequest)
                                    .block(REQUEST_TIMEOUT);
                    if (rpcResponse == null) {
                        response.setStatus(org.restlet.data.Status.SERVER_ERROR_INTERNAL);
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
