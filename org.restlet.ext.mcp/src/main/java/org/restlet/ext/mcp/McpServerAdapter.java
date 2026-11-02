/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.mcp;

import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import io.modelcontextprotocol.common.McpTransportContext;
import io.modelcontextprotocol.json.McpJsonDefaults;
import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.json.jackson2.JacksonMcpJsonMapper;
import io.modelcontextprotocol.server.McpAsyncListFilter;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpStatelessAsyncServer;
import io.modelcontextprotocol.server.McpStatelessServerFeatures;
import io.modelcontextprotocol.server.McpStatelessServerHandler;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpStatelessServerTransport;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.ClientInfo;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.engine.header.PreferenceReader;
import org.restlet.ext.mcp.internal.McpMessages;
import org.restlet.ext.mcp.internal.McpWorkers;
import org.restlet.representation.ByteArrayRepresentation;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

/**
 * Sessionless SDK MCP server on an existing Restlet routing chain. HTTP and STDIO share the same
 * SDK dispatcher. Configure before start; feature collections are snapshotted at start.
 *
 * <p>Only the 2025-11-25 SDK profile is supported. Ikanos revision compatibility, sessions,
 * listening GET streams, replay and server-initiated requests are deliberately not advertised.
 */
public final class McpServerAdapter extends Restlet {
    private static final McpJsonMapper MAPPER = initMapper();

    private static McpJsonMapper initMapper() {
        McpJsonMapper mapper = McpJsonDefaults.getMapper();
        if (mapper instanceof JacksonMcpJsonMapper jacksonMapper) {
            return new JacksonMcpJsonMapper(
                    jacksonMapper.getObjectMapper().copy().registerModule(new Jdk8Module()));
        }
        return mapper;
    }

    private final List<McpStatelessServerFeatures.AsyncToolSpecification> tools =
            new CopyOnWriteArrayList<>();
    private final List<McpStatelessServerFeatures.AsyncResourceSpecification> resources =
            new CopyOnWriteArrayList<>();
    private final List<McpStatelessServerFeatures.AsyncResourceTemplateSpecification>
            resourceTemplates = new CopyOnWriteArrayList<>();
    private final List<McpStatelessServerFeatures.AsyncPromptSpecification> prompts =
            new CopyOnWriteArrayList<>();
    private final List<McpAsyncListFilter<McpSchema.Tool>> toolFilters =
            new CopyOnWriteArrayList<>();
    private final List<McpInterceptor> interceptors = new CopyOnWriteArrayList<>();
    private final Set<String> allowedOrigins = ConcurrentHashMap.newKeySet();
    private McpSchema.Implementation serverInfo =
            McpSchema.Implementation.builder("restlet", "3.0").build();
    private String instructions;
    private int maxMessageBytes = McpProtocol.DEFAULT_MAX_MESSAGE_BYTES;
    private int maxConcurrentCalls = McpProtocol.DEFAULT_MAX_CONCURRENT_CALLS;
    private Duration requestTimeout = Duration.ofSeconds(30);
    private volatile McpStatelessServerHandler handler;
    private volatile McpStatelessAsyncServer sdkServer;
    private volatile McpWorkers workers;
    private volatile boolean accepting;
    private volatile Sinks.One<Boolean> stopping;
    private volatile List<McpInterceptor> runningInterceptors = List.of();
    private volatile Set<String> runningOrigins = Set.of();
    private boolean configured;

    public McpServerAdapter() {
        this(new Context());
    }

    public McpServerAdapter(Context context) {
        super(context);
    }

    public List<McpStatelessServerFeatures.AsyncToolSpecification> getTools() {
        return tools;
    }

    public List<McpStatelessServerFeatures.AsyncResourceSpecification> getResources() {
        return resources;
    }

    public List<McpStatelessServerFeatures.AsyncResourceTemplateSpecification>
            getResourceTemplates() {
        return resourceTemplates;
    }

    public List<McpStatelessServerFeatures.AsyncPromptSpecification> getPrompts() {
        return prompts;
    }

    public List<McpAsyncListFilter<McpSchema.Tool>> getToolFilters() {
        return toolFilters;
    }

    public List<McpInterceptor> getInterceptors() {
        return interceptors;
    }

    /** Allowed exact Origin header values. Requests with any other Origin are forbidden. */
    public Set<String> getAllowedOrigins() {
        return allowedOrigins;
    }

    public synchronized void setServerInfo(McpSchema.Implementation value) {
        requireConfigurable();
        serverInfo = Objects.requireNonNull(value, "serverInfo");
    }

    public synchronized McpSchema.Implementation getServerInfo() {
        return serverInfo;
    }

    public synchronized void setInstructions(String value) {
        requireConfigurable();
        instructions = value;
    }

    public synchronized String getInstructions() {
        return instructions;
    }

    public synchronized int getMaxMessageBytes() {
        return maxMessageBytes;
    }

    public synchronized void setMaxMessageBytes(int value) {
        requireConfigurable();
        if (value <= 0 || value == Integer.MAX_VALUE) {
            throw new IllegalArgumentException(
                    "maxMessageBytes must be positive and below Integer.MAX_VALUE");
        }
        maxMessageBytes = value;
    }

    public synchronized int getMaxConcurrentCalls() {
        return maxConcurrentCalls;
    }

    public synchronized void setMaxConcurrentCalls(int value) {
        requireConfigurable();
        if (value <= 0) {
            throw new IllegalArgumentException("maxConcurrentCalls must be positive");
        }
        maxConcurrentCalls = value;
    }

    public synchronized Duration getRequestTimeout() {
        return requestTimeout;
    }

    public synchronized void setRequestTimeout(Duration value) {
        requireConfigurable();
        if (value.isZero() || value.isNegative()) {
            throw new IllegalArgumentException("requestTimeout must be positive");
        }
        requestTimeout = Duration.ofNanos(value.toNanos());
    }

    private void requireConfigurable() {
        if (configured) {
            throw new IllegalStateException("MCP server configuration is fixed after start");
        }
    }

    @Override
    public synchronized void start() throws Exception {
        if (isStarted()) {
            return;
        }
        requireConfigurable();
        var caps = McpSchema.ServerCapabilities.builder();
        if (!tools.isEmpty()) {
            caps.tools(false);
        }
        if (!resources.isEmpty() || !resourceTemplates.isEmpty()) {
            caps.resources(false, false);
        }
        if (!prompts.isEmpty()) {
            caps.prompts(false);
        }
        var transport =
                new McpStatelessServerTransport() {
                    @Override
                    public void setMcpHandler(McpStatelessServerHandler value) {
                        handler = value;
                    }

                    @Override
                    public Mono<Void> closeGracefully() {
                        return Mono.empty();
                    }

                    @Override
                    public List<String> protocolVersions() {
                        return List.of(McpProtocol.VERSION);
                    }
                };
        sdkServer =
                McpServer.async(transport)
                        .jsonMapper(MAPPER)
                        .serverInfo(serverInfo)
                        .instructions(instructions)
                        .capabilities(caps.build())
                        .requestTimeout(requestTimeout)
                        .tools(List.copyOf(tools))
                        .resources(List.copyOf(resources))
                        .resourceTemplates(List.copyOf(resourceTemplates))
                        .prompts(List.copyOf(prompts))
                        .toolFilters(filters -> filters.addAll(toolFilters))
                        .build();
        workers = new McpWorkers(maxConcurrentCalls, requestTimeout);
        stopping = Sinks.one();
        runningInterceptors = List.copyOf(interceptors);
        runningOrigins = Set.copyOf(allowedOrigins);
        configured = true;
        super.start();
        accepting = true;
    }

    @Override
    public synchronized void stop() throws Exception {
        if (!isStarted()) {
            return;
        }
        accepting = false;
        stopping.tryEmitValue(Boolean.TRUE);
        try {
            workers.close();
        } finally {
            try {
                sdkServer.closeGracefully().block(requestTimeout);
            } finally {
                super.stop();
            }
        }
    }

    /**
     * Native SDK dispatcher shared with the transport paths; notifications return Optional.empty.
     */
    public Optional<McpSchema.JSONRPCResponse> dispatch(
            McpSchema.JSONRPCMessage message, McpRequestContext context) {
        Objects.requireNonNull(message, "message");
        Objects.requireNonNull(context, "context");
        if (!accepting) {
            throw new IllegalStateException("MCP server is not accepting calls");
        }
        validateVersion(message);
        var completed =
                admit(
                        context,
                        () -> invoke(0, message, context).blockOptional(requestTimeout),
                        () -> {});
        return message instanceof McpSchema.JSONRPCNotification ? Optional.empty() : completed;
    }

    /**
     * Runs work under one call permit and the request deadline, ending it early when the server
     * stops or the peer disconnects.
     */
    private <T> Optional<T> admit(
            McpRequestContext context, Callable<Optional<T>> work, Runnable cancel) {
        var result = new AtomicReference<Optional<T>>(Optional.empty());
        Mono<T> exchange =
                workers.submit(() -> result.set(work.call()), cancel)
                        .then(Mono.defer(() -> Mono.justOrEmpty(result.get())));
        Mono<T> stopped =
                stopping.asMono()
                        .flatMap(ignored -> Mono.<T>error(new IOException("MCP server stopped")));
        Mono<T> disconnected =
                context.transportRequest()
                        .map(
                                request ->
                                        Mono.fromCompletionStage(request.getDisconnectSignal())
                                                .flatMap(
                                                        cause ->
                                                                Mono.<T>error(
                                                                        new IOException(
                                                                                "MCP peer disconnected",
                                                                                cause))))
                        .orElseGet(Mono::never);
        return Mono.firstWithSignal(stopped, Mono.firstWithSignal(disconnected, exchange))
                .blockOptional(requestTimeout);
    }

    private Mono<McpSchema.JSONRPCResponse> invoke(
            int index, McpSchema.JSONRPCMessage message, McpRequestContext context) {
        if (index < runningInterceptors.size()) {
            return runningInterceptors
                    .get(index)
                    .intercept(
                            message,
                            context,
                            (next, nextContext) -> invoke(index + 1, next, nextContext));
        }
        McpTransportContext sdkContext =
                McpTransportContext.create(Map.of(McpProtocol.CONTEXT_KEY, context));
        if (message instanceof McpSchema.JSONRPCRequest request) {
            return handler.handleRequest(sdkContext, request);
        }
        if (message instanceof McpSchema.JSONRPCNotification notification) {
            return handler.handleNotification(sdkContext, notification).then(Mono.empty());
        }
        return Mono.error(
                new IllegalArgumentException("Only MCP requests and notifications are accepted"));
    }

    private static void validateVersion(McpSchema.JSONRPCMessage message) {
        if (message instanceof McpSchema.JSONRPCRequest request
                && "initialize".equals(request.method())
                && request.params() instanceof Map<?, ?> params
                && !McpProtocol.VERSION.equals(params.get("protocolVersion"))) {
            throw new IllegalArgumentException("Unsupported MCP initialization version");
        }
    }

    @Override
    public void handle(Request request, Response response) {
        if (!accepting) {
            response.setStatus(Status.SERVER_ERROR_SERVICE_UNAVAILABLE);
            return;
        }
        if (!Method.POST.equals(request.getMethod())) {
            response.setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
            response.getAllowedMethods().add(Method.POST);
            return;
        }
        String origin = request.getHeaders().getFirstValue("Origin", true);
        if (request.getHeaders().getValuesArray("Origin", true).length > 1
                || request.getHeaders().getValuesArray(McpProtocol.VERSION_HEADER, true).length
                        > 1) {
            response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            return;
        }
        if (origin != null && !runningOrigins.contains(origin)) {
            response.setStatus(Status.CLIENT_ERROR_FORBIDDEN);
            return;
        }
        String version = request.getHeaders().getFirstValue(McpProtocol.VERSION_HEADER, true);
        if (version != null && !McpProtocol.VERSION.equals(version)
                || request.getHeaders().getFirstValue("Mcp-Session-Id", true) != null) {
            response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            return;
        }
        ClientInfo accepted = request.getClientInfo();
        String accept = request.getHeaders().getValues("Accept");
        if (accept != null) {
            accepted = new ClientInfo();
            PreferenceReader.addMediaTypes(accept, accepted);
        }
        boolean jsonAccepted =
                accepted.getAcceptedMediaTypes().stream()
                        .anyMatch(
                                p ->
                                        p.getQuality() > 0
                                                && MediaType.APPLICATION_JSON.isCompatible(
                                                        p.getMetadata()));
        boolean sseAccepted =
                accepted.getAcceptedMediaTypes().stream()
                        .anyMatch(
                                p ->
                                        p.getQuality() > 0
                                                && MediaType.TEXT_EVENT_STREAM.isCompatible(
                                                        p.getMetadata()));
        if (!jsonAccepted || !sseAccepted) {
            response.setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE);
            return;
        }
        if (request.getEntity() == null
                || request.getEntity().getMediaType() == null
                || !MediaType.APPLICATION_JSON.isCompatible(request.getEntity().getMediaType())) {
            response.setStatus(Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE);
            return;
        }
        if (request.getEntity().getSize() > maxMessageBytes) {
            response.setStatus(Status.CLIENT_ERROR_REQUEST_ENTITY_TOO_LARGE);
            response.setEntity((org.restlet.representation.Representation) null);
            return;
        }
        var context = new McpRequestContext(request);
        var parsed = new AtomicReference<McpSchema.JSONRPCMessage>();
        Reply reply;
        try {
            reply =
                    admit(
                                    context,
                                    () -> Optional.of(exchange(request, version, context, parsed)),
                                    () -> request.getEntity().release())
                            .orElseThrow(() -> new IllegalStateException("MCP exchange ended"));
        } catch (RuntimeException e) {
            getLogger().log(Level.WARNING, "MCP exchange failed", e);
            reply =
                    new Reply(
                            Status.SERVER_ERROR_SERVICE_UNAVAILABLE,
                            error(parsed.get(), -32603, "MCP exchange unavailable"));
        }
        response.setStatus(reply.status());
        if (reply.body() != null) {
            write(response, reply.body());
        }
    }

    /** Reads, decodes and dispatches one HTTP message in an admitted worker. */
    private Reply exchange(
            Request request,
            String version,
            McpRequestContext context,
            AtomicReference<McpSchema.JSONRPCMessage> parsed) {
        McpSchema.JSONRPCMessage message;
        try (var input = request.getEntity().getStream()) {
            byte[] bytes = McpMessages.read(input, maxMessageBytes);
            message = McpMessages.decode(MAPPER, McpMessages.utf8(bytes));
        } catch (McpMessages.MessageLimitException e) {
            getLogger().log(Level.FINE, "Oversized MCP request", e);
            return new Reply(Status.CLIENT_ERROR_REQUEST_ENTITY_TOO_LARGE, null);
        } catch (McpMessages.InvalidEnvelopeException e) {
            getLogger().log(Level.FINE, "Invalid MCP envelope", e);
            return new Reply(
                    Status.CLIENT_ERROR_BAD_REQUEST,
                    McpMessages.error(null, -32600, "Invalid MCP request envelope"));
        } catch (IOException | IllegalArgumentException e) {
            getLogger().log(Level.FINE, "Invalid MCP request", e);
            return new Reply(
                    Status.CLIENT_ERROR_BAD_REQUEST,
                    McpMessages.error(null, -32700, "Invalid MCP JSON request"));
        }
        if (request.getEntity().getSize() == 0) {
            return new Reply(
                    Status.CLIENT_ERROR_BAD_REQUEST,
                    McpMessages.error(null, -32600, "Empty MCP request body"));
        }
        parsed.set(message);
        if (version == null
                && !(message instanceof McpSchema.JSONRPCRequest rpc
                        && "initialize".equals(rpc.method()))) {
            return new Reply(
                    Status.CLIENT_ERROR_BAD_REQUEST,
                    error(message, -32600, "MCP-Protocol-Version is required"));
        }
        try {
            validateVersion(message);
        } catch (IllegalArgumentException e) {
            getLogger().log(Level.FINE, "Unsupported MCP request", e);
            return new Reply(
                    Status.CLIENT_ERROR_BAD_REQUEST,
                    error(message, -32600, "Unsupported MCP version"));
        }
        Optional<McpSchema.JSONRPCResponse> result =
                invoke(0, message, context).blockOptional(requestTimeout);
        if (message instanceof McpSchema.JSONRPCNotification || result.isEmpty()) {
            return new Reply(Status.SUCCESS_ACCEPTED, null);
        }
        return new Reply(Status.SUCCESS_OK, result.orElseThrow());
    }

    /** A JSON-RPC error answering a request, or null when the message expects no response. */
    private static Map<String, Object> error(
            McpSchema.JSONRPCMessage message, int code, String text) {
        return message instanceof McpSchema.JSONRPCRequest rpc
                ? McpMessages.error(rpc.id(), code, text)
                : null;
    }

    private record Reply(Status status, Object body) {}

    private void write(Response response, Object message) {
        try {
            byte[] bytes = McpMessages.encode(MAPPER, message, maxMessageBytes);
            response.setEntity(new ByteArrayRepresentation(bytes, MediaType.APPLICATION_JSON));
        } catch (IOException e) {
            getLogger().log(Level.WARNING, "Unable to encode MCP response", e);
            response.setStatus(Status.SERVER_ERROR_INTERNAL);
            response.setEntity((org.restlet.representation.Representation) null);
        }
    }
}
