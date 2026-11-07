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
import io.modelcontextprotocol.modern.server.HmacRequestStateCodec;
import io.modelcontextprotocol.modern.server.feature.McpAsyncPrompt;
import io.modelcontextprotocol.modern.server.feature.McpAsyncTool;
import io.modelcontextprotocol.modern.server.feature.McpChangeBroadcaster;
import io.modelcontextprotocol.modern.server.feature.McpChangeFeed;
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
import org.restlet.ext.mcp.internal.McpModernExchange;
import org.restlet.ext.mcp.internal.McpModernFeatures;
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

    /** How long stopping waits for listen streams to end with their result. */
    private static final Duration LISTEN_DRAIN = Duration.ofSeconds(2);

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
    private final List<McpAsyncTool> modernTools = new CopyOnWriteArrayList<>();
    private final List<McpModernResource> modernResources = new CopyOnWriteArrayList<>();
    private final List<McpModernResourceTemplate> modernResourceTemplates =
            new CopyOnWriteArrayList<>();
    private final List<McpAsyncPrompt> modernPrompts = new CopyOnWriteArrayList<>();
    private final McpChangeFeed changes = new McpChangeFeed();
    private byte[] requestStateKey;
    private int maxListenStreams = McpProtocol.DEFAULT_MAX_LISTEN_STREAMS;
    private final Set<String> allowedOrigins = ConcurrentHashMap.newKeySet();
    private McpSchema.Implementation serverInfo =
            McpSchema.Implementation.builder("restlet", "3.0").build();
    private String instructions;
    private int maxMessageBytes = McpProtocol.DEFAULT_MAX_MESSAGE_BYTES;
    private int maxConcurrentCalls = McpProtocol.DEFAULT_MAX_CONCURRENT_CALLS;
    private Duration requestTimeout = Duration.ofSeconds(30);
    private volatile McpStatelessServerHandler handler;
    private volatile McpStatelessAsyncServer sdkServer;
    private volatile io.modelcontextprotocol.modern.server.McpServer modernServer;
    private volatile McpModernExchange modernExchange;
    private volatile McpWorkers listeners;
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

    /**
     * Tools served to MCP {@link McpProtocol#MODERN_VERSION} clients only, on the SDK's modern
     * types, for what that revision adds: {@code input_required} results and streamed progress. The
     * tools of {@link #getTools()} are served to both revisions. A name may only be used once
     * across both lists. The tool filters apply to them too, on their {@code 2025-11-25}
     * projection. Experimental: the SDK's modern API is a preview.
     *
     * @return The modern tools, modifiable until the adapter starts.
     */
    public List<McpAsyncTool> getModernTools() {
        return modernTools;
    }

    /**
     * Resources served to MCP {@link McpProtocol#MODERN_VERSION} clients only, whose read may
     * answer {@code input_required}. The resources of {@link #getResources()} are served to both
     * revisions; a URI may only be used once across both lists. Experimental.
     *
     * @return The modern resources, modifiable until the adapter starts.
     */
    public List<McpModernResource> getModernResources() {
        return modernResources;
    }

    /**
     * Resource templates served to MCP {@link McpProtocol#MODERN_VERSION} clients only. The
     * templates of {@link #getResourceTemplates()} are served to both revisions; a URI template may
     * only be used once across both lists. Experimental.
     *
     * @return The modern resource templates, modifiable until the adapter starts.
     */
    public List<McpModernResourceTemplate> getModernResourceTemplates() {
        return modernResourceTemplates;
    }

    /**
     * Prompts served to MCP {@link McpProtocol#MODERN_VERSION} clients only, on the SDK's modern
     * types. The prompts of {@link #getPrompts()} are served to both revisions; a name may only be
     * used once across both lists. Experimental.
     *
     * @return The modern prompts, modifiable until the adapter starts.
     */
    public List<McpAsyncPrompt> getModernPrompts() {
        return modernPrompts;
    }

    /**
     * Publishes changes to the MCP {@link McpProtocol#MODERN_VERSION} clients listening through
     * {@code subscriptions/listen}: tools, prompts and resources list changes, and resource
     * updates. A change nobody listens to is dropped. Experimental.
     *
     * @return The change broadcaster, usable at any time.
     */
    public McpChangeBroadcaster getChanges() {
        return changes;
    }

    public synchronized int getMaxListenStreams() {
        return maxListenStreams;
    }

    /**
     * Sets how many {@code subscriptions/listen} streams may be open at once, {@value
     * McpProtocol#DEFAULT_MAX_LISTEN_STREAMS} by default. Listen streams have no time limit and
     * don't count against {@link #getMaxConcurrentCalls()}; they end when the adapter stops or the
     * client goes away, and carry heartbeats.
     *
     * @param value The number of streams, positive.
     */
    public synchronized void setMaxListenStreams(int value) {
        requireConfigurable();
        if (value <= 0) {
            throw new IllegalArgumentException("maxListenStreams must be positive");
        }
        maxListenStreams = value;
    }

    /**
     * Sets the key sealing the {@code requestState} of {@code input_required} results, at least 32
     * bytes. Without it, each adapter generates its own random key, so a retry must reach the
     * instance that answered; instances behind a load balancer need the same key. The state is also
     * bound to the authenticated user of the request, so another user can't replay it.
     *
     * @param value The key, copied.
     */
    public synchronized void setRequestStateKey(byte[] value) {
        requireConfigurable();
        if (value == null || value.length < 32) {
            throw new IllegalArgumentException("requestStateKey must have at least 32 bytes");
        }
        requestStateKey = value.clone();
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
        // Built first: it rejects duplicate tool names before anything needs releasing.
        var modern = modernServer();
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
        listeners = new McpWorkers(maxListenStreams, null, requestTimeout);
        stopping = Sinks.one();
        modernServer = modern;
        modernExchange =
                new McpModernExchange(
                        modernServer,
                        MAPPER,
                        workers,
                        listeners,
                        requestTimeout,
                        maxMessageBytes,
                        (message, context, terminal) -> invoke(0, message, context, terminal),
                        () -> stopping.asMono());
        runningInterceptors = List.copyOf(interceptors);
        runningOrigins = Set.copyOf(allowedOrigins);
        configured = true;
        super.start();
        accepting = true;
    }

    /** Builds the SDK's modern server from the registrations, bridged and modern. */
    private io.modelcontextprotocol.modern.server.McpServer modernServer() {
        var builder =
                io.modelcontextprotocol.modern.server.McpServer.builder()
                        .serverInfo(
                                MAPPER.convertValue(
                                        serverInfo,
                                        io.modelcontextprotocol.modern.McpSchema.Implementation
                                                .class))
                        .instructions(instructions)
                        .jsonMapper(MAPPER)
                        .features(
                                McpModernFeatures.of(
                                        MAPPER,
                                        new McpModernFeatures.Registrations(
                                                List.copyOf(tools),
                                                List.copyOf(resources),
                                                List.copyOf(resourceTemplates),
                                                List.copyOf(prompts),
                                                List.copyOf(toolFilters),
                                                List.copyOf(modernTools),
                                                List.copyOf(modernResources),
                                                List.copyOf(modernResourceTemplates),
                                                List.copyOf(modernPrompts))))
                        .subscriptions(changes);
        var codec =
                HmacRequestStateCodec.builder()
                        .principalExtractor(McpServerAdapter::principal)
                        .jsonMapper(MAPPER);
        if (requestStateKey != null) {
            codec.key(requestStateKey);
        }
        return builder.requestStateCodec(codec.build()).build();
    }

    /**
     * The authenticated user of a request, binding its {@code input_required} state, or null.
     *
     * @param transport The SDK transport context.
     * @return The user identifier, else the first principal name, or null.
     */
    private static String principal(McpTransportContext transport) {
        if (!(transport.get(McpProtocol.CONTEXT_KEY) instanceof McpRequestContext context)) {
            return null;
        }
        return context.transportRequest()
                .map(Request::getClientInfo)
                .map(
                        info ->
                                info.getUser() != null
                                        ? info.getUser().getIdentifier()
                                        : info.getPrincipals().stream()
                                                .map(java.security.Principal::getName)
                                                .findFirst()
                                                .orElse(null))
                .orElse(null);
    }

    @Override
    public synchronized void stop() throws Exception {
        if (!isStarted()) {
            return;
        }
        accepting = false;
        // Listen streams end with their result first; what remains is then cut short.
        modernServer.closeGracefully();
        try {
            listeners.awaitIdle(
                    requestTimeout.compareTo(LISTEN_DRAIN) < 0 ? requestTimeout : LISTEN_DRAIN);
        } catch (InterruptedException _) {
            Thread.currentThread().interrupt();
        }
        stopping.tryEmitValue(Boolean.TRUE);
        try {
            try {
                workers.close();
            } finally {
                listeners.close();
            }
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
        return invoke(index, message, context, this::dispatchStateless);
    }

    /** Runs the interceptors from {@code index}, then {@code terminal}. */
    private Mono<McpSchema.JSONRPCResponse> invoke(
            int index,
            McpSchema.JSONRPCMessage message,
            McpRequestContext context,
            McpInterceptor.Chain terminal) {
        if (index < runningInterceptors.size()) {
            return runningInterceptors
                    .get(index)
                    .intercept(
                            message,
                            context,
                            (next, nextContext) -> invoke(index + 1, next, nextContext, terminal));
        }
        return terminal.proceed(message, context);
    }

    /** Dispatches a 2025-11-25 message to the SDK's stateless server. */
    private Mono<McpSchema.JSONRPCResponse> dispatchStateless(
            McpSchema.JSONRPCMessage message, McpRequestContext context) {
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
        String version = request.getHeaders().getFirstValue(McpProtocol.VERSION_HEADER, true);
        // A 2026-07-28 request goes to the modern server; it ignores a stale Mcp-Session-Id.
        boolean modern = McpProtocol.MODERN_VERSION.equals(version);
        Status rejected = headerRejection(request, version, modern);
        if (rejected == null && !acceptsJsonAndEventStream(request)) {
            rejected = Status.CLIENT_ERROR_NOT_ACCEPTABLE;
        }
        if (rejected == null) {
            rejected = entityRejection(request);
        }
        if (rejected != null) {
            response.setStatus(rejected);
            if (Status.CLIENT_ERROR_REQUEST_ENTITY_TOO_LARGE.equals(rejected)) {
                response.setEntity((org.restlet.representation.Representation) null);
            }
            return;
        }
        if (modern) {
            modernExchange.handle(request, response);
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

    /**
     * Checks the headers: one Origin and one version at most, an allowed Origin, and for a
     * 2025-11-25 request a supported version and no session.
     *
     * @return The status refusing the request, or null.
     */
    private Status headerRejection(Request request, String version, boolean modern) {
        if (request.getHeaders().getValuesArray("Origin", true).length > 1
                || request.getHeaders().getValuesArray(McpProtocol.VERSION_HEADER, true).length
                        > 1) {
            return Status.CLIENT_ERROR_BAD_REQUEST;
        }
        String origin = request.getHeaders().getFirstValue("Origin", true);
        if (origin != null && !runningOrigins.contains(origin)) {
            return Status.CLIENT_ERROR_FORBIDDEN;
        }
        if (!modern
                && (version != null && !McpProtocol.VERSION.equals(version)
                        || request.getHeaders().getFirstValue("Mcp-Session-Id", true) != null)) {
            return Status.CLIENT_ERROR_BAD_REQUEST;
        }
        return null;
    }

    /** Whether the request accepts both JSON and an event stream, as MCP requires. */
    private static boolean acceptsJsonAndEventStream(Request request) {
        ClientInfo accepted = request.getClientInfo();
        String accept = request.getHeaders().getValues("Accept");
        if (accept != null) {
            accepted = new ClientInfo();
            PreferenceReader.addMediaTypes(accept, accepted);
        }
        return accepts(accepted, MediaType.APPLICATION_JSON)
                && accepts(accepted, MediaType.TEXT_EVENT_STREAM);
    }

    private static boolean accepts(ClientInfo accepted, MediaType type) {
        return accepted.getAcceptedMediaTypes().stream()
                .anyMatch(p -> p.getQuality() > 0 && type.isCompatible(p.getMetadata()));
    }

    /**
     * Checks the request entity: JSON, within the message size.
     *
     * @return The status refusing the request, or null.
     */
    private Status entityRejection(Request request) {
        if (request.getEntity() == null
                || request.getEntity().getMediaType() == null
                || !MediaType.APPLICATION_JSON.isCompatible(request.getEntity().getMediaType())) {
            return Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE;
        }
        if (request.getEntity().getSize() > maxMessageBytes) {
            return Status.CLIENT_ERROR_REQUEST_ENTITY_TOO_LARGE;
        }
        return null;
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
