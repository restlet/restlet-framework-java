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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.modelcontextprotocol.client.McpAsyncClient;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.json.McpJsonDefaults;
import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.json.TypeRef;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.spec.McpClientTransport;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpStreamableServerSession;
import io.modelcontextprotocol.spec.McpStreamableServerTransport;
import io.modelcontextprotocol.spec.McpStreamableServerTransportProvider;
import io.modelcontextprotocol.spec.ProtocolVersions;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
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
import org.restlet.data.Preference;
import org.restlet.data.Protocol;
import org.restlet.engine.Engine;
import org.restlet.representation.OutputRepresentation;
import org.restlet.representation.StringRepresentation;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

class McpStreamableSdkTransportSpikeTestCase {

    private static final int MAX_MESSAGE_BYTES = 64 * 1024;

    private static final int MAX_REPLAY_EVENTS = 8;

    private static final int MAX_LISTENING_EVENTS = 2;

    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(5);

    private Client client;

    private McpAsyncClient sdkClient;

    private io.modelcontextprotocol.server.McpAsyncServer sdkServer;

    private Server server;

    @Test
    void servesListeningSseAndReplaysEventsAfterLastEventId() throws Exception {
        McpJsonMapper mapper = McpJsonDefaults.getMapper();
        RestletStreamableTransport transport = new RestletStreamableTransport(mapper);
        sdkServer =
                McpServer.async(transport).serverInfo("restlet-r0-streamable-spike", "1").build();
        Context serverContext = new Context();
        serverContext.getParameters().add("http.transport.protocol", "HTTP2");
        server = new Server(serverContext, Protocol.HTTP, 0, transport);
        server.start();
        Context clientContext = new Context();
        clientContext.getParameters().add("httpClientTransportMode", "HTTP2");
        client = new Client(clientContext, Protocol.HTTP);
        client.start();

        RestletStreamableClientTransport clientTransport =
                new RestletStreamableClientTransport(
                        client, mapper, "http://localhost:" + server.getActualPort() + "/mcp");
        sdkClient =
                McpClient.async(clientTransport)
                        .requestTimeout(REQUEST_TIMEOUT)
                        .initializationTimeout(REQUEST_TIMEOUT)
                        .build();
        sdkClient.initialize().block(REQUEST_TIMEOUT);
        String sessionId = clientTransport.sessionId;
        assertTrue(sessionId != null && !sessionId.isBlank());

        Request get = new Request(Method.GET, clientTransport.target);
        get.getHeaders().add("Mcp-Session-Id", sessionId);
        var responseFuture =
                java.util.concurrent.CompletableFuture.supplyAsync(() -> client.handle(get));
        assertTrue(
                transport.awaitListeningStream(sessionId, REQUEST_TIMEOUT),
                "GET did not establish an SDK listening stream");
        transport
                .notifyClients(
                        sessionId,
                        new McpSchema.JSONRPCNotification(
                                "notifications/message", Map.of("text", "first")))
                .block(REQUEST_TIMEOUT);
        transport
                .notifyClients(
                        sessionId,
                        new McpSchema.JSONRPCNotification(
                                "notifications/message", Map.of("text", "second")))
                .block(REQUEST_TIMEOUT);
        Response response = responseFuture.get(REQUEST_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
        assertEquals(org.restlet.data.Status.SUCCESS_OK, response.getStatus());
        assertEquals("text/event-stream", response.getEntity().getMediaType().getName());
        assertEquals(sessionId, response.getHeaders().getFirstValue("Mcp-Session-Id", true));

        String firstId;
        String secondEvent;
        try (BufferedReader reader =
                new BufferedReader(
                        new InputStreamReader(
                                response.getEntity().getStream(), StandardCharsets.UTF_8))) {
            String firstEvent = readSseEvent(reader);
            firstId = sseField(firstEvent, "id");
            assertTrue(firstId != null && !firstId.isBlank());
            assertTrue(firstEvent.contains("\"text\":\"first\""));

            secondEvent = readSseEvent(reader);
            String secondId = sseField(secondEvent, "id");
            assertTrue(secondId != null && !secondId.isBlank() && !secondId.equals(firstId));
            assertTrue(secondEvent.contains("\"text\":\"second\""));
            assertNull(reader.readLine(), "Bounded listening probe should close after two events");
        } finally {
            response.getEntity().release();
        }

        Request replay = new Request(Method.GET, clientTransport.target);
        replay.getHeaders().add("Mcp-Session-Id", sessionId);
        replay.getHeaders().add("Last-Event-ID", firstId);
        Response replayResponse = client.handle(replay);
        assertEquals(org.restlet.data.Status.SUCCESS_OK, replayResponse.getStatus());
        try (InputStream input = replayResponse.getEntity().getStream()) {
            byte[] replayBytes = input.readNBytes(MAX_MESSAGE_BYTES + 1);
            assertTrue(replayBytes.length <= MAX_MESSAGE_BYTES);
            String replayBody = new String(replayBytes, StandardCharsets.UTF_8);
            assertTrue(replayBody.contains("id: " + sseField(secondEvent, "id")));
            assertTrue(replayBody.contains("\"text\":\"second\""));
            assertFalse(replayBody.contains("\"text\":\"first\""));
        } finally {
            replayResponse.getEntity().release();
        }

        Request expiredReplay = new Request(Method.GET, clientTransport.target);
        expiredReplay.getHeaders().add("Mcp-Session-Id", sessionId);
        expiredReplay.getHeaders().add("Last-Event-ID", "expired-event");
        Response expiredReplayResponse = client.handle(expiredReplay);
        assertEquals(
                org.restlet.data.Status.CLIENT_ERROR_BAD_REQUEST,
                expiredReplayResponse.getStatus());
    }

    @BeforeEach
    void setUp() {
        Engine.clearThreadLocalVariables();
        Engine.register(false);
        Engine.getInstance().getRegisteredServers().add(new HttpServerHelper(null));
        Engine.getInstance().getRegisteredClients().add(new HttpClientHelper(null));
    }

    @AfterEach
    void tearDown() throws Exception {
        if (sdkClient != null) {
            sdkClient.close();
        }
        if (sdkServer != null) {
            sdkServer.close();
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
    void dispatchesMcpSdkClientAndServerThroughRestletStreamableHttp() throws Exception {
        McpJsonMapper mapper = McpJsonDefaults.getMapper();
        RestletStreamableTransport transport = new RestletStreamableTransport(mapper);
        McpSchema.Tool tool =
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
                                List.of("text")),
                        null,
                        null,
                        null,
                        null);
        sdkServer =
                McpServer.async(transport)
                        .serverInfo("restlet-r0-streamable-spike", "1")
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

        Context serverContext = new Context();
        serverContext.getParameters().add("http.transport.protocol", "HTTP2");
        server = new Server(serverContext, Protocol.HTTP, 0, transport);
        server.start();

        Context clientContext = new Context();
        clientContext.getParameters().add("httpClientTransportMode", "HTTP2");
        client = new Client(clientContext, Protocol.HTTP);
        client.start();
        RestletStreamableClientTransport clientTransport =
                new RestletStreamableClientTransport(
                        client, mapper, "http://localhost:" + server.getActualPort() + "/mcp");
        sdkClient =
                McpClient.async(clientTransport)
                        .requestTimeout(REQUEST_TIMEOUT)
                        .initializationTimeout(REQUEST_TIMEOUT)
                        .build();

        sdkClient.initialize().block(REQUEST_TIMEOUT);
        McpSchema.ListToolsResult listedTools = sdkClient.listTools().block(REQUEST_TIMEOUT);
        McpSchema.CallToolResult result =
                sdkClient
                        .callTool(
                                new McpSchema.CallToolRequest(
                                        "echo", Map.of("text", "streamable"), null))
                        .block(REQUEST_TIMEOUT);

        assertTrue(listedTools.tools().stream().anyMatch(item -> item.name().equals("echo")));
        assertEquals(
                "echo:streamable", ((McpSchema.TextContent) result.content().getFirst()).text());
        assertEquals(3, transport.postCount.get());
        assertEquals("2.0", clientTransport.lastResponseProtocol.get().getVersion());
        assertTrue(clientTransport.sawEventStreamResponse.get());
    }

    @Test
    void pinnedSdkDoesNotAdvertiseIkanosProtocolRevision() {
        HttpClientStreamableHttpTransport transport =
                HttpClientStreamableHttpTransport.builder("http://localhost/mcp")
                        .jsonMapper(McpJsonDefaults.getMapper())
                        .build();
        try {
            assertTrue(transport.protocolVersions().contains(ProtocolVersions.MCP_2025_11_25));
            assertFalse(transport.protocolVersions().contains("2026-07-28"));
        } finally {
            transport.closeGracefully().block(REQUEST_TIMEOUT);
        }
    }

    private static String readSseEvent(BufferedReader reader) throws IOException {
        StringBuilder event = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.isEmpty()) {
                if (!event.isEmpty()) {
                    return event.toString();
                }
            } else {
                event.append(line).append('\n');
            }
        }
        throw new IOException("MCP SSE stream closed before the expected event");
    }

    private static String sseField(String event, String field) {
        return event.lines()
                .filter(line -> line.startsWith(field + ":"))
                .map(line -> line.substring(field.length() + 1).strip())
                .findFirst()
                .orElse(null);
    }

    private record ServerEvent(String id, String json) {}

    private static final class RestletStreamableTransport extends Restlet
            implements McpStreamableServerTransportProvider {

        private final McpJsonMapper jsonMapper;

        private final Map<String, McpStreamableServerSession> sessions = new ConcurrentHashMap<>();

        private final AtomicInteger postCount = new AtomicInteger();

        private final Map<String, ArrayDeque<ServerEvent>> eventHistory = new ConcurrentHashMap<>();

        private final Map<String, ListeningSseTransport> listeningTransports =
                new ConcurrentHashMap<>();

        private final Map<String, CountDownLatch> listeningStreamReady = new ConcurrentHashMap<>();

        private volatile McpStreamableServerSession.Factory sessionFactory;

        private RestletStreamableTransport(McpJsonMapper jsonMapper) {
            this.jsonMapper = jsonMapper;
        }

        @Override
        public void setSessionFactory(McpStreamableServerSession.Factory sessionFactory) {
            this.sessionFactory = sessionFactory;
        }

        @Override
        public Mono<Void> notifyClients(String sessionId, Object notification) {
            if (!(notification instanceof McpSchema.JSONRPCNotification rpcNotification)) {
                return Mono.error(
                        new IllegalArgumentException(
                                "The R0 probe requires an SDK JSON-RPC notification"));
            }
            McpStreamableServerSession session = sessions.get(sessionId);
            if (session == null) {
                return Mono.error(new IllegalArgumentException("Unknown MCP session"));
            }
            return session.sendNotification(rpcNotification.method(), rpcNotification.params());
        }

        private boolean awaitListeningStream(String sessionId, Duration timeout)
                throws InterruptedException {
            return listeningStreamReady
                    .computeIfAbsent(sessionId, ignored -> new CountDownLatch(1))
                    .await(timeout.toMillis(), TimeUnit.MILLISECONDS);
        }

        private void recordEvent(String sessionId, ServerEvent event) {
            ArrayDeque<ServerEvent> events =
                    eventHistory.computeIfAbsent(sessionId, ignored -> new ArrayDeque<>());
            synchronized (events) {
                events.addLast(event);
                while (events.size() > MAX_REPLAY_EVENTS) {
                    events.removeFirst();
                }
            }
        }

        private List<ServerEvent> eventsAfter(String sessionId, String lastEventId) {
            ArrayDeque<ServerEvent> events = eventHistory.get(sessionId);
            if (events == null) {
                return null;
            }
            synchronized (events) {
                List<ServerEvent> snapshot = new ArrayList<>(events);
                int lastIndex = -1;
                for (int index = 0; index < snapshot.size(); index++) {
                    if (snapshot.get(index).id().equals(lastEventId)) {
                        lastIndex = index;
                        break;
                    }
                }
                return lastIndex < 0
                        ? null
                        : List.copyOf(snapshot.subList(lastIndex + 1, snapshot.size()));
            }
        }

        @Override
        public Mono<Void> closeGracefully() {
            return Mono.<Void>fromRunnable(
                            () -> {
                                sessions.values().forEach(McpStreamableServerSession::close);
                                listeningTransports.values().forEach(ListeningSseTransport::close);
                                listeningStreamReady.clear();
                            })
                    .doFinally(signal -> sessions.clear());
        }

        @Override
        public void handle(Request request, Response response) {
            if (request.getMethod().equals(Method.DELETE)) {
                deleteSession(request, response);
            } else if (request.getMethod().equals(Method.POST)) {
                postMessage(request, response);
            } else if (request.getMethod().equals(Method.GET)) {
                getListeningStream(request, response);
            } else {
                response.setStatus(org.restlet.data.Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
            }
        }

        private void getListeningStream(Request request, Response response) {
            String sessionId = request.getHeaders().getFirstValue("Mcp-Session-Id", true);
            if (sessionId == null || sessionId.isBlank()) {
                response.setStatus(org.restlet.data.Status.CLIENT_ERROR_BAD_REQUEST);
                return;
            }
            McpStreamableServerSession session = sessions.get(sessionId);
            if (session == null) {
                response.setStatus(org.restlet.data.Status.CLIENT_ERROR_NOT_FOUND);
                return;
            }
            response.getHeaders().add("Mcp-Session-Id", sessionId);
            String lastEventId = request.getHeaders().getFirstValue("Last-Event-ID", true);
            if (lastEventId != null) {
                List<ServerEvent> replay = eventsAfter(sessionId, lastEventId);
                if (replay == null) {
                    response.setStatus(org.restlet.data.Status.CLIENT_ERROR_BAD_REQUEST);
                    return;
                }
                response.setEntity(formatEvents(replay), new MediaType("text/event-stream"));
                return;
            }

            ListeningSseTransport listening = new ListeningSseTransport(this, sessionId);
            listeningTransports.put(sessionId, listening);
            session.listeningStream(listening);
            listeningStreamReady
                    .computeIfAbsent(sessionId, ignored -> new CountDownLatch(1))
                    .countDown();
            response.setEntity(
                    new OutputRepresentation(new MediaType("text/event-stream")) {
                        @Override
                        public void write(OutputStream output) throws IOException {
                            try {
                                for (int index = 0; index < MAX_LISTENING_EVENTS; index++) {
                                    ServerEvent event =
                                            listening.events.poll(
                                                    REQUEST_TIMEOUT.toMillis(),
                                                    TimeUnit.MILLISECONDS);
                                    if (event == null) {
                                        throw new IOException(
                                                "Timed out waiting for bounded MCP SSE event");
                                    }
                                    if (event.id() == null) {
                                        break;
                                    }
                                    output.write(
                                            formatEvent(event).getBytes(StandardCharsets.UTF_8));
                                    output.flush();
                                }
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                throw new IOException("Interrupted while writing MCP SSE", e);
                            } finally {
                                listeningTransports.remove(sessionId, listening);
                                session.releaseListeningStream();
                            }
                        }
                    });
        }

        private static String formatEvents(List<ServerEvent> events) {
            StringBuilder result = new StringBuilder();
            events.forEach(event -> result.append(formatEvent(event)));
            return result.toString();
        }

        private static String formatEvent(ServerEvent event) {
            return "id: " + event.id() + "\nevent: message\ndata: " + event.json() + "\n\n";
        }

        private void postMessage(Request request, Response response) {
            if (!request.isEntityAvailable()) {
                response.setStatus(org.restlet.data.Status.CLIENT_ERROR_BAD_REQUEST);
                return;
            }

            try (InputStream input = request.getEntity().getStream()) {
                byte[] bytes = input.readNBytes(MAX_MESSAGE_BYTES + 1);
                if (bytes.length > MAX_MESSAGE_BYTES) {
                    response.setStatus(
                            org.restlet.data.Status.CLIENT_ERROR_REQUEST_ENTITY_TOO_LARGE);
                    return;
                }
                McpSchema.JSONRPCMessage message =
                        McpSchema.deserializeJsonRpcMessage(
                                jsonMapper,
                                new String(bytes, java.nio.charset.StandardCharsets.UTF_8));
                if (message instanceof McpSchema.JSONRPCRequest) {
                    postCount.incrementAndGet();
                }
                if (message instanceof McpSchema.JSONRPCRequest rpcRequest
                        && McpSchema.METHOD_INITIALIZE.equals(rpcRequest.method())) {
                    initializeSession(rpcRequest, response);
                    return;
                }

                String sessionId = request.getHeaders().getFirstValue("Mcp-Session-Id", true);
                McpStreamableServerSession session = sessions.get(sessionId);
                if (session == null) {
                    response.setStatus(org.restlet.data.Status.CLIENT_ERROR_NOT_FOUND);
                    response.setEntity(
                            "Unknown MCP session: "
                                    + sessionId
                                    + "; request headers: "
                                    + request.getHeaders(),
                            MediaType.TEXT_PLAIN);
                    return;
                }
                response.getHeaders().add("Mcp-Session-Id", sessionId);

                if (message instanceof McpSchema.JSONRPCRequest rpcRequest) {
                    AtomicReference<McpSchema.JSONRPCMessage> result = new AtomicReference<>();
                    McpStreamableServerSession.McpStreamableServerSessionStream stream =
                            session.responseStream(new JsonResponseTransport(jsonMapper, result));
                    stream.handle(rpcRequest)
                            .doFinally(signal -> stream.releaseTransport())
                            .block(REQUEST_TIMEOUT);
                    McpSchema.JSONRPCMessage reply = result.get();
                    if (reply == null) {
                        response.setStatus(org.restlet.data.Status.SERVER_ERROR_INTERNAL);
                    } else {
                        writeEventStreamResponse(response, reply);
                    }
                } else if (message instanceof McpSchema.JSONRPCNotification notification) {
                    session.accept(notification).block(REQUEST_TIMEOUT);
                    response.setStatus(org.restlet.data.Status.SUCCESS_ACCEPTED);
                } else if (message instanceof McpSchema.JSONRPCResponse rpcResponse) {
                    session.accept(rpcResponse).block(REQUEST_TIMEOUT);
                    response.setStatus(org.restlet.data.Status.SUCCESS_ACCEPTED);
                } else {
                    response.setStatus(org.restlet.data.Status.CLIENT_ERROR_BAD_REQUEST);
                }
            } catch (IOException e) {
                getLogger().warning("Unable to read MCP streamable request: " + e.getMessage());
                response.setStatus(org.restlet.data.Status.SERVER_ERROR_INTERNAL);
            } catch (RuntimeException e) {
                getLogger().warning("Unable to dispatch MCP streamable request: " + e.getMessage());
                response.setStatus(org.restlet.data.Status.SERVER_ERROR_INTERNAL);
            }
        }

        private void initializeSession(McpSchema.JSONRPCRequest request, Response response)
                throws IOException {
            McpStreamableServerSession.Factory currentFactory = sessionFactory;
            if (currentFactory == null) {
                response.setStatus(org.restlet.data.Status.SERVER_ERROR_SERVICE_UNAVAILABLE);
                return;
            }
            McpSchema.InitializeRequest initializeRequest =
                    jsonMapper.convertValue(
                            request.params(), new TypeRef<McpSchema.InitializeRequest>() {});
            McpStreamableServerSession.McpStreamableServerSessionInit init =
                    currentFactory.startSession(initializeRequest);
            McpSchema.InitializeResult initResult = init.initResult().block(REQUEST_TIMEOUT);
            sessions.put(init.session().getId(), init.session());
            response.getHeaders().add("Mcp-Session-Id", init.session().getId());
            writeJsonResponse(response, McpSchema.JSONRPCResponse.result(request.id(), initResult));
        }

        private void deleteSession(Request request, Response response) {
            String sessionId = request.getHeaders().getFirstValue("Mcp-Session-Id", true);
            McpStreamableServerSession session = sessions.remove(sessionId);
            if (session == null) {
                response.setStatus(org.restlet.data.Status.CLIENT_ERROR_NOT_FOUND);
                return;
            }
            eventHistory.remove(sessionId);
            ListeningSseTransport listening = listeningTransports.remove(sessionId);
            if (listening != null) {
                listening.close();
            }
            listeningStreamReady.remove(sessionId);
            session.close();
            response.setStatus(org.restlet.data.Status.SUCCESS_NO_CONTENT);
        }

        private void writeJsonResponse(Response response, McpSchema.JSONRPCMessage message)
                throws IOException {
            response.setEntity(jsonMapper.writeValueAsString(message), MediaType.APPLICATION_JSON);
        }

        private void writeEventStreamResponse(Response response, McpSchema.JSONRPCMessage message)
                throws IOException {
            response.setEntity(
                    "event: message\ndata: " + jsonMapper.writeValueAsString(message) + "\n\n",
                    new MediaType("text/event-stream"));
        }
    }

    private static final class ListeningSseTransport implements McpStreamableServerTransport {

        private final RestletStreamableTransport owner;

        private final String sessionId;

        private final LinkedBlockingQueue<ServerEvent> events =
                new LinkedBlockingQueue<>(MAX_LISTENING_EVENTS);

        private ListeningSseTransport(RestletStreamableTransport owner, String sessionId) {
            this.owner = owner;
            this.sessionId = sessionId;
        }

        @Override
        public Mono<Void> sendMessage(McpSchema.JSONRPCMessage message, String messageId) {
            return Mono.fromCallable(
                            () -> {
                                if (messageId == null || messageId.isBlank()) {
                                    throw new IllegalArgumentException(
                                            "SDK server event omitted its replay ID");
                                }
                                ServerEvent event =
                                        new ServerEvent(
                                                messageId,
                                                owner.jsonMapper.writeValueAsString(message));
                                owner.recordEvent(sessionId, event);
                                if (!events.offer(event)) {
                                    throw new IllegalStateException(
                                            "Bounded MCP listening queue is full");
                                }
                                return event;
                            })
                    .then();
        }

        @Override
        public Mono<Void> sendMessage(McpSchema.JSONRPCMessage message) {
            return Mono.error(
                    new IllegalStateException("SDK server event did not include its message ID"));
        }

        @Override
        public <T> T unmarshalFrom(Object value, TypeRef<T> typeRef) {
            return owner.jsonMapper.convertValue(value, typeRef);
        }

        @Override
        public void close() {
            events.clear();
            events.offer(new ServerEvent(null, null));
        }

        @Override
        public Mono<Void> closeGracefully() {
            close();
            return Mono.empty();
        }
    }

    private static final class JsonResponseTransport implements McpStreamableServerTransport {

        private final McpJsonMapper jsonMapper;

        private final AtomicReference<McpSchema.JSONRPCMessage> response;

        private JsonResponseTransport(
                McpJsonMapper jsonMapper, AtomicReference<McpSchema.JSONRPCMessage> response) {
            this.jsonMapper = jsonMapper;
            this.response = response;
        }

        @Override
        public Mono<Void> sendMessage(McpSchema.JSONRPCMessage message, String messageId) {
            response.set(message);
            return Mono.empty();
        }

        @Override
        public Mono<Void> sendMessage(McpSchema.JSONRPCMessage message) {
            return sendMessage(message, null);
        }

        @Override
        public <T> T unmarshalFrom(Object value, TypeRef<T> typeRef) {
            return jsonMapper.convertValue(value, typeRef);
        }

        @Override
        public Mono<Void> closeGracefully() {
            return Mono.empty();
        }
    }

    private static final class RestletStreamableClientTransport implements McpClientTransport {

        private final Client client;

        private final McpJsonMapper jsonMapper;

        private final String target;

        private final AtomicReference<Protocol> lastResponseProtocol = new AtomicReference<>();

        private final AtomicReference<Boolean> sawEventStreamResponse =
                new AtomicReference<>(false);

        private volatile java.util.function.Function<
                        Mono<McpSchema.JSONRPCMessage>, Mono<McpSchema.JSONRPCMessage>>
                incomingMessageHandler;

        private volatile String sessionId;

        private volatile String protocolVersion;

        private RestletStreamableClientTransport(
                Client client, McpJsonMapper jsonMapper, String target) {
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
            try {
                Request request =
                        new Request(
                                Method.POST,
                                target,
                                new StringRepresentation(
                                        jsonMapper.writeValueAsString(message),
                                        MediaType.APPLICATION_JSON));
                request.getClientInfo()
                        .getAcceptedMediaTypes()
                        .add(new Preference<>(MediaType.APPLICATION_JSON));
                request.getClientInfo()
                        .getAcceptedMediaTypes()
                        .add(new Preference<>(new MediaType("text/event-stream")));
                if (sessionId != null) {
                    request.getHeaders().add("Mcp-Session-Id", sessionId);
                    request.getHeaders().add("Mcp-Protocol-Version", protocolVersion);
                }

                Response response = client.handle(request);
                lastResponseProtocol.set(response.getProtocol());
                boolean initializing =
                        message instanceof McpSchema.JSONRPCRequest rpcRequest
                                && McpSchema.METHOD_INITIALIZE.equals(rpcRequest.method());
                if (message instanceof McpSchema.JSONRPCRequest rpcRequest
                        && McpSchema.METHOD_INITIALIZE.equals(rpcRequest.method())
                        && response.getHeaders().getFirstValue("Mcp-Session-Id", true) == null) {
                    throw new IllegalStateException(
                            "MCP initialize response omitted session ID; headers: "
                                    + response.getHeaders());
                }
                if (!response.getStatus().isSuccess()) {
                    String errorBody = "";
                    if (response.getEntity() != null) {
                        try (InputStream errorStream = response.getEntity().getStream()) {
                            if (errorStream != null) {
                                errorBody =
                                        new String(
                                                errorStream.readNBytes(MAX_MESSAGE_BYTES),
                                                java.nio.charset.StandardCharsets.UTF_8);
                            }
                        }
                    }
                    throw new IllegalStateException(
                            "MCP streamable request failed: "
                                    + response.getStatus()
                                    + " "
                                    + errorBody);
                }
                if (message instanceof McpSchema.JSONRPCRequest) {
                    if (initializing) {
                        sessionId = response.getHeaders().getFirstValue("Mcp-Session-Id", true);
                    }
                    if (response.getEntity() == null) {
                        throw new IllegalStateException(
                                "MCP streamable response has no JSON entity: "
                                        + response.getStatus());
                    }
                    try (InputStream input = response.getEntity().getStream()) {
                        byte[] bytes = input.readNBytes(MAX_MESSAGE_BYTES + 1);
                        if (bytes.length > MAX_MESSAGE_BYTES) {
                            throw new IllegalStateException(
                                    "MCP streamable response exceeds the configured size limit");
                        }
                        String payload = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
                        if ("text/event-stream"
                                .equalsIgnoreCase(response.getEntity().getMediaType().getName())) {
                            sawEventStreamResponse.set(true);
                            payload =
                                    payload.lines()
                                            .filter(line -> line.startsWith("data:"))
                                            .map(line -> line.substring("data:".length()).strip())
                                            .findFirst()
                                            .orElseThrow(
                                                    () ->
                                                            new IllegalStateException(
                                                                    "MCP SSE response has no data event"));
                        }
                        McpSchema.JSONRPCMessage reply =
                                McpSchema.deserializeJsonRpcMessage(jsonMapper, payload);
                        if (reply instanceof McpSchema.JSONRPCResponse rpcResponse
                                && rpcResponse.result() != null
                                && message instanceof McpSchema.JSONRPCRequest requestMessage
                                && McpSchema.METHOD_INITIALIZE.equals(requestMessage.method())) {
                            protocolVersion =
                                    jsonMapper
                                            .convertValue(
                                                    rpcResponse.result(),
                                                    McpSchema.InitializeResult.class)
                                            .protocolVersion();
                        }
                        java.util.function.Function<
                                        Mono<McpSchema.JSONRPCMessage>,
                                        Mono<McpSchema.JSONRPCMessage>>
                                handler = incomingMessageHandler;
                        if (handler == null) {
                            throw new IllegalStateException(
                                    "MCP client transport is not connected");
                        }
                        handler.apply(Mono.just(reply)).block(REQUEST_TIMEOUT);
                    }
                } else if (!response.getStatus().isSuccess()) {
                    throw new IllegalStateException(
                            "MCP streamable notification failed: " + response.getStatus());
                }
            } catch (IOException e) {
                throw new IllegalStateException("Unable to encode MCP streamable request", e);
            }
        }

        @Override
        public <T> T unmarshalFrom(Object value, TypeRef<T> typeRef) {
            return jsonMapper.convertValue(value, typeRef);
        }

        @Override
        public Mono<Void> closeGracefully() {
            if (sessionId == null) {
                return Mono.empty();
            }
            return Mono.fromRunnable(
                            () -> {
                                Request request = new Request(Method.DELETE, target);
                                request.getHeaders().add("Mcp-Session-Id", sessionId);
                                Response response = client.handle(request);
                                if (!response.getStatus().isSuccess()) {
                                    throw new IllegalStateException(
                                            "MCP streamable session close failed: "
                                                    + response.getStatus());
                                }
                                sessionId = null;
                            })
                    .subscribeOn(Schedulers.boundedElastic())
                    .then();
        }
    }
}
