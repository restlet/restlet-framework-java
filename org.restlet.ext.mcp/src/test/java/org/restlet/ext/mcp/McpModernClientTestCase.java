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
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.modelcontextprotocol.modern.McpException;
import io.modelcontextprotocol.modern.McpSchema;
import io.modelcontextprotocol.modern.McpSchema.CallToolOutcome;
import io.modelcontextprotocol.modern.McpSchema.CallToolResult;
import io.modelcontextprotocol.modern.McpSchema.ElicitFormRequest;
import io.modelcontextprotocol.modern.McpSchema.ElicitResult;
import io.modelcontextprotocol.modern.McpSchema.InputRequiredResult;
import io.modelcontextprotocol.modern.McpSchema.TextContent;
import io.modelcontextprotocol.modern.McpSchema.Tool;
import io.modelcontextprotocol.modern.client.McpAsyncClient;
import io.modelcontextprotocol.modern.client.McpAsyncElicitationHandler;
import io.modelcontextprotocol.modern.client.McpTimeoutException;
import io.modelcontextprotocol.modern.client.SubscriptionEvent;
import io.modelcontextprotocol.modern.server.McpAsyncResponse;
import io.modelcontextprotocol.modern.server.feature.McpAsyncPrompt;
import io.modelcontextprotocol.modern.server.feature.McpAsyncResource;
import io.modelcontextprotocol.modern.server.feature.McpAsyncTool;
import io.modelcontextprotocol.modern.server.feature.ServerChange;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.Server;
import org.restlet.data.Protocol;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;

/**
 * {@link McpClientAdapter} speaking MCP {@code 2026-07-28} to {@link McpServerAdapter} over real
 * HTTP connectors, through the SDK's client: discovery, listings, calls, streamed progress, {@code
 * input_required} retries, {@code subscriptions/listen} and cancellation.
 */
class McpModernClientTestCase {
    private static final Duration WAIT = Duration.ofSeconds(10);

    private final CountDownLatch cancelled = new CountDownLatch(1);
    private McpServerAdapter adapter;
    private Server server;
    private Client connector;
    private McpClientAdapter client;

    @AfterEach
    void cleanup() throws Exception {
        try {
            if (client != null) {
                client.stop();
            }
        } finally {
            try {
                if (connector != null) {
                    connector.stop();
                }
            } finally {
                try {
                    if (server != null) {
                        server.stop();
                    }
                } finally {
                    if (adapter != null) {
                        adapter.stop();
                    }
                }
            }
        }
    }

    @Test
    void discoversAndCallsOverHttp1() throws Exception {
        discoversAndCalls("HTTP1_1");
    }

    @Test
    void discoversAndCallsOverHttp2() throws Exception {
        discoversAndCalls("HTTP2");
    }

    /** Discovery, then the tools, resources and prompts of both revisions' registrations. */
    private void discoversAndCalls(String mode) throws Exception {
        McpAsyncClient modern = start(mode, null);

        McpSchema.DiscoverResult discovered = modern.discover().block(WAIT);
        assertEquals(List.of(McpProtocol.MODERN_VERSION), discovered.supportedVersions());
        // The SDK's client leaves out a tool it can't call, such as level, whose header argument
        // is a number.
        assertEquals(
                List.of("echo", "progress", "approve", "endless", "region", "slow"),
                modern.tools().list().block(WAIT).tools().stream().map(Tool::name).toList());
        assertEquals(
                "echo:café",
                text(modern.tool("echo").arguments(Map.of("text", "café")).call().block(WAIT)));
        assertEquals(
                List.of("test://modern"),
                modern.resources().list().block(WAIT).resources().stream()
                        .map(McpSchema.Resource::uri)
                        .toList());
        McpSchema.ReadResourceResult read = modern.resource("test://modern").read().block(WAIT);
        assertEquals(
                "modern", ((McpSchema.TextResourceContents) read.contents().getFirst()).text());
        assertEquals(
                List.of("welcome"),
                modern.prompts().list().block(WAIT).prompts().stream()
                        .map(McpSchema.Prompt::name)
                        .toList());
        assertEquals("modern welcome", modern.prompt("welcome").get().block(WAIT).description());
    }

    /** The adapter's convenience methods go through the SDK's client. */
    @Test
    void theAdapterCallsThroughTheSdkClient() throws Exception {
        start("HTTP2", null);

        assertEquals(6, client.listTools(null).tools().size());
        assertEquals("echo:hi", text(client.callTool("echo", Map.of("text", "hi"))));
        assertEquals(
                "modern",
                ((McpSchema.TextResourceContents)
                                client.readResource("test://modern").contents().getFirst())
                        .text());
        assertEquals("modern welcome", client.getPrompt("welcome", null).description());
        // Without an elicitation handler, the client declares no elicitation capability, and the
        // server refuses to ask for one.
        McpException refused =
                assertThrows(McpException.class, () -> client.callTool("approve", null));
        assertEquals(McpSchema.ErrorCodes.INTERNAL_ERROR, refused.error().code().intValue());
    }

    /** A call's progress notifications reach its listener before its result. */
    @Test
    void streamsProgress() throws Exception {
        McpAsyncClient modern = start("HTTP2", null);
        List<String> progress = new CopyOnWriteArrayList<>();

        CallToolResult result =
                modern.tool("progress")
                        .progress(
                                update -> {
                                    progress.add(update.message());
                                    return Mono.empty();
                                })
                        .call()
                        .block(WAIT);

        assertEquals("done", text(result));
        assertEquals(List.of("half"), progress);
    }

    /**
     * A single round returns an input_required result, for the caller to answer; a call answers it
     * with the form elicitation handler, which also declares the capability the server requires.
     */
    @Test
    void answersInputRequired() throws Exception {
        List<String> asked = new CopyOnWriteArrayList<>();
        McpAsyncClient modern =
                start(
                        "HTTP2",
                        request -> {
                            asked.add(request.message());
                            return Mono.just(ElicitResult.accept(Map.of()));
                        });

        InputRequiredResult required =
                assertInstanceOf(
                        InputRequiredResult.class, modern.tool("approve").callOnce().block(WAIT));
        assertNotNull(required.requestState()); // Sealed by the server.
        assertTrue(required.inputRequests().containsKey("confirm"));
        assertEquals(
                "approved:s1",
                text(
                        modern.tool("approve")
                                .inputResponses(Map.of("confirm", accepted()))
                                .requestState(required.requestState())
                                .call()
                                .block(WAIT)));
        assertEquals(List.of(), asked);

        assertEquals("approved:s1", text(modern.tool("approve").call().block(WAIT)));
        assertEquals(List.of("Proceed?"), asked);
    }

    /** A listen stream carries the changes published, and completes when the server stops. */
    @Test
    void listensToChanges() throws Exception {
        McpAsyncClient modern = start("HTTP2", null);
        BlockingQueue<SubscriptionEvent> changes = new LinkedBlockingQueue<>();
        CountDownLatch completed = new CountDownLatch(1);
        List<Throwable> errors = new CopyOnWriteArrayList<>();
        Disposable listening =
                modern.listen()
                        .toolsChanged()
                        .events()
                        .filter(SubscriptionEvent.ToolsListChanged.class::isInstance)
                        .doOnComplete(completed::countDown)
                        .subscribe(changes::add, errors::add);
        try {
            // The listen stream opens asynchronously: publish until the change arrives.
            SubscriptionEvent change = null;
            for (int i = 0; i < 200 && change == null; i++) {
                adapter.getChanges().broadcast(new ServerChange.ToolsListChanged());
                change = changes.poll(50, TimeUnit.MILLISECONDS);
            }
            assertInstanceOf(SubscriptionEvent.ToolsListChanged.class, change, errors.toString());

            adapter.stop();
            assertTrue(completed.await(10, TimeUnit.SECONDS), "the listen stream did not end");
            assertEquals(List.of(), errors);
        } finally {
            listening.dispose();
        }
    }

    /** Cancelling a call aborts its exchange, which cancels the tool on the server. */
    @Test
    void cancellingACallCancelsTheTool() throws Exception {
        McpAsyncClient modern = start("HTTP2", null);
        CountDownLatch started = new CountDownLatch(1);

        Disposable call =
                modern.tool("endless")
                        .progress(
                                update -> {
                                    started.countDown();
                                    return Mono.empty();
                                })
                        .call()
                        .subscribe();
        assertTrue(started.await(10, TimeUnit.SECONDS), "the tool did not start");
        call.dispose();

        assertTrue(cancelled.await(10, TimeUnit.SECONDS), "the tool was not cancelled");
        assertEquals(
                "echo:after",
                text(modern.tool("echo").arguments(Map.of("text", "after")).call().block(WAIT)));
    }

    /**
     * A tool's x-mcp-header arguments are mirrored into Mcp-Param headers, the client listing the
     * tools it doesn't know first.
     */
    @Test
    void mirrorsAnnotatedArguments() throws Exception {
        McpAsyncClient modern = start("HTTP2", null);

        assertEquals(
                "region:eu",
                text(modern.tool("region").arguments(Map.of("region", "eu")).call().block(WAIT)));
        assertEquals(
                "region:us",
                text(modern.tool("region").arguments(Map.of("region", "us")).call().block(WAIT)));
    }

    /**
     * With a single call slot, an elicitation handler may make a blocking call: the first call no
     * longer holds its slot.
     */
    @Test
    void anElicitationHandlerMayCallWithOneSlot() throws Exception {
        AtomicReference<McpAsyncClient> holder = new AtomicReference<>();
        List<String> nested = new CopyOnWriteArrayList<>();
        McpAsyncClient modern =
                start(
                        "HTTP2",
                        request ->
                                Mono.fromCallable(
                                        () -> {
                                            nested.add(
                                                    text(
                                                            holder.get()
                                                                    .tool("echo")
                                                                    .arguments(
                                                                            Map.of(
                                                                                    "text",
                                                                                    "nested"))
                                                                    .call()
                                                                    .block(WAIT)));
                                            return ElicitResult.accept(Map.of());
                                        }),
                        configured -> configured.setMaxConcurrentCalls(1));
        holder.set(modern);

        assertEquals("approved:s1", text(modern.tool("approve").call().block(WAIT)));
        assertEquals(List.of("echo:nested"), nested);
    }

    /**
     * A tool whose x-mcp-header argument is a number isn't called: the SDK's client mirrors only
     * strings, integers and booleans.
     */
    @Test
    void aToolWithANumberHeaderIsNotCalled() throws Exception {
        McpAsyncClient modern = start("HTTP2", null);

        Mono<CallToolResult> level = modern.tool("level").arguments(Map.of("level", 1.0e7)).call();
        IllegalArgumentException refused =
                assertThrows(IllegalArgumentException.class, () -> level.block(WAIT));
        assertTrue(refused.getMessage().contains("unsupported type number"), refused.getMessage());
    }

    /**
     * The SDK's round timeout is a request's only time limit: the adapter's request timeout is its
     * default, and a longer one given for a call lets it run past it.
     */
    @Test
    void theRoundTimeoutLimitsACall() throws Exception {
        McpAsyncClient modern =
                start(
                        "HTTP2",
                        null,
                        configured -> configured.setRequestTimeout(Duration.ofMillis(300)));

        Mono<CallToolResult> cut = modern.tool("slow").call();
        McpTimeoutException timeout =
                assertThrows(McpTimeoutException.class, () -> cut.block(WAIT));
        assertEquals(McpTimeoutException.Kind.ROUND, timeout.kind());
        assertEquals("slow", text(modern.tool("slow").roundTimeout(WAIT).call().block(WAIT)));
    }

    /** An error response fails the request with its JSON-RPC error. */
    @Test
    void anErrorResponseFails() throws Exception {
        McpAsyncClient modern = start("HTTP2", null);

        Mono<McpSchema.ReadResourceResult> missing = modern.resource("test://missing").read();
        McpException error = assertThrows(McpException.class, () -> missing.block(WAIT));
        assertEquals(McpSchema.ErrorCodes.INVALID_PARAMS, error.error().code().intValue());
    }

    /** The modern client is reached through getModernClient() only, over HTTP for now. */
    @Test
    void modernModeLimits() throws Exception {
        start("HTTP2", null);
        client.stop();
        assertFalse(client.isStarted());
        assertThrows(IllegalStateException.class, client::getModernClient);
        assertThrows(IllegalStateException.class, client::getTasksClient);

        McpClientAdapter stdio = new McpClientAdapter(connector, "stdio://localhost/mcp");
        stdio.setProtocolVersion(McpProtocol.MODERN_VERSION);
        assertThrows(UnsupportedOperationException.class, stdio::start);
        McpClientAdapter unknown = new McpClientAdapter(connector, "http://localhost/mcp");
        assertThrows(
                IllegalArgumentException.class, () -> unknown.setProtocolVersion("2024-11-05"));
        assertThrows(IllegalArgumentException.class, () -> unknown.setMaxInputRounds(0));
    }

    /** Starts the server, if needed, and a modern client with an optional form handler. */
    private McpAsyncClient start(
            String mode, McpAsyncElicitationHandler<McpSchema.ElicitFormRequest> form)
            throws Exception {
        return start(mode, form, ignored -> {});
    }

    private McpAsyncClient start(
            String mode,
            McpAsyncElicitationHandler<McpSchema.ElicitFormRequest> form,
            Consumer<McpClientAdapter> configure)
            throws Exception {
        if (adapter == null) {
            adapter = adapter();
            Context sc = new Context();
            sc.getParameters().set("http.transport.protocol", mode);
            server = new Server(sc, Protocol.HTTP, 0, adapter);
            server.start();
            Context cc = new Context();
            cc.getParameters().set("httpClientTransportMode", mode);
            cc.getParameters().set("followRedirects", "false");
            connector = new Client(cc, Protocol.HTTP);
            connector.start();
        }
        client =
                new McpClientAdapter(
                        connector, "http://localhost:" + server.getActualPort() + "/mcp");
        client.setProtocolVersion(McpProtocol.MODERN_VERSION);
        client.setFormElicitation(form);
        configure.accept(client);
        client.start();
        return client.getModernClient();
    }

    private McpServerAdapter adapter() throws Exception {
        McpServerAdapter served = new McpServerAdapter();
        McpServerAdapterTestCase.addEcho(served);
        served.getTools()
                .add(
                        McpAsyncTool.of(
                                Tool.builder("progress", Map.of("type", "object")).build(),
                                (ctx, request) ->
                                        Mono.just(
                                                McpAsyncResponse.<CallToolOutcome>streaming(
                                                        notifier ->
                                                                notifier.progress(1, 2.0, "half")
                                                                        .then(
                                                                                Mono.just(
                                                                                        result(
                                                                                                "done")))))));
        served.getTools()
                .add(
                        McpAsyncTool.of(
                                Tool.builder("approve", Map.of("type", "object")).build(),
                                (ctx, request) -> {
                                    CallToolOutcome outcome =
                                            request.requestState() == null
                                                    ? InputRequiredResult.builder()
                                                            .elicit(
                                                                    "confirm",
                                                                    ElicitFormRequest.builder(
                                                                                    "Proceed?",
                                                                                    Map.of(
                                                                                            "type",
                                                                                            "object",
                                                                                            "properties",
                                                                                            Map
                                                                                                    .of()))
                                                                            .build())
                                                            .requestState("s1")
                                                            .build()
                                                    : result("approved:" + request.requestState());
                                    return Mono.just(McpAsyncResponse.result(outcome));
                                }));
        served.getTools()
                .add(
                        McpAsyncTool.of(
                                Tool.builder("endless", Map.of("type", "object")).build(),
                                (ctx, request) ->
                                        Mono.just(
                                                McpAsyncResponse.<CallToolOutcome>streaming(
                                                        notifier ->
                                                                notifier.progress(
                                                                                1, null, "started")
                                                                        .then(
                                                                                Mono
                                                                                        .<CallToolOutcome>
                                                                                                never()
                                                                                        .doOnCancel(
                                                                                                cancelled
                                                                                                        ::countDown))))));
        served.getTools()
                .add(
                        McpAsyncTool.of(
                                Tool.builder(
                                                "region",
                                                Map.of(
                                                        "type",
                                                        "object",
                                                        "properties",
                                                        Map.of(
                                                                "region",
                                                                Map.of(
                                                                        "type",
                                                                        "string",
                                                                        "x-mcp-header",
                                                                        "Region"))))
                                        .build(),
                                (ctx, request) ->
                                        Mono.just(
                                                McpAsyncResponse.<CallToolOutcome>result(
                                                        result(
                                                                "region:"
                                                                        + request.arguments()
                                                                                .get("region"))))));
        served.getTools()
                .add(
                        McpAsyncTool.of(
                                Tool.builder(
                                                "level",
                                                Map.of(
                                                        "type",
                                                        "object",
                                                        "properties",
                                                        Map.of(
                                                                "level",
                                                                Map.of(
                                                                        "type",
                                                                        "number",
                                                                        "x-mcp-header",
                                                                        "Level"))))
                                        .build(),
                                (ctx, request) ->
                                        Mono.just(
                                                McpAsyncResponse.<CallToolOutcome>result(
                                                        result(
                                                                "level:"
                                                                        + request.arguments()
                                                                                .get("level"))))));
        served.getTools()
                .add(
                        McpAsyncTool.of(
                                Tool.builder("slow", Map.of("type", "object")).build(),
                                (ctx, request) ->
                                        Mono.delay(Duration.ofMillis(900))
                                                .thenReturn(
                                                        McpAsyncResponse.<CallToolOutcome>result(
                                                                result("slow")))));
        served.getResources()
                .add(
                        new McpResource(
                                McpSchema.Resource.builder("test://modern", "modern").build(),
                                McpAsyncResource.of(
                                        (ctx, request) -> {
                                            McpSchema.ReadResourceOutcome outcome =
                                                    McpSchema.ReadResourceResult.builder(
                                                                    List.of(
                                                                            new McpSchema
                                                                                    .TextResourceContents(
                                                                                    request.uri(),
                                                                                    "text/plain",
                                                                                    "modern",
                                                                                    null)))
                                                            .build();
                                            return Mono.just(McpAsyncResponse.result(outcome));
                                        })));
        served.getPrompts()
                .add(
                        McpAsyncPrompt.of(
                                McpSchema.Prompt.builder("welcome").build(),
                                (ctx, request) -> {
                                    McpSchema.GetPromptOutcome outcome =
                                            new McpSchema.GetPromptResult(
                                                    "modern welcome",
                                                    List.of(
                                                            new McpSchema.PromptMessage(
                                                                    McpSchema.Role.USER,
                                                                    TextContent.builder("hi")
                                                                            .build())),
                                                    null,
                                                    null);
                                    return Mono.just(McpAsyncResponse.result(outcome));
                                }));
        served.start();
        return served;
    }

    private static Map<String, Object> accepted() {
        return Map.of("action", "accept", "content", Map.of());
    }

    private static CallToolResult result(String text) {
        return CallToolResult.builder().addContent(TextContent.builder(text).build()).build();
    }

    private static String text(Object outcome) {
        CallToolResult result = assertInstanceOf(CallToolResult.class, outcome);
        return ((TextContent) result.content().getFirst()).text();
    }
}
