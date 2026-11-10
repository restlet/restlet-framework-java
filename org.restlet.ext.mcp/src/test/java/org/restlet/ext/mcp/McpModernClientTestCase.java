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

import io.modelcontextprotocol.modern.JsonRpc;
import io.modelcontextprotocol.modern.McpException;
import io.modelcontextprotocol.modern.McpSchema;
import io.modelcontextprotocol.modern.McpSchema.CallToolOutcome;
import io.modelcontextprotocol.modern.McpSchema.CallToolRequest;
import io.modelcontextprotocol.modern.McpSchema.CallToolResult;
import io.modelcontextprotocol.modern.McpSchema.ElicitFormRequest;
import io.modelcontextprotocol.modern.McpSchema.InputRequiredResult;
import io.modelcontextprotocol.modern.McpSchema.TextContent;
import io.modelcontextprotocol.modern.McpSchema.Tool;
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
 * HTTP connectors: discovery, listings, calls, streamed progress, {@code input_required} retries,
 * {@code subscriptions/listen} and cancellation.
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
        McpModernClient modern = start(mode, null);

        McpSchema.DiscoverResult discovered = modern.discover().block(WAIT);
        assertEquals(List.of(McpProtocol.MODERN_VERSION), discovered.supportedVersions());
        assertEquals(
                List.of("echo", "progress", "approve", "endless", "region", "level"),
                modern.listTools(null).block(WAIT).tools().stream().map(Tool::name).toList());
        assertEquals(
                "echo:café",
                text(
                        modern.callTool(
                                        new CallToolRequest(
                                                "echo", Map.of("text", "café"), null, null, null))
                                .block(WAIT)));
        assertEquals(
                List.of("test://modern"),
                modern.listResources(null).block(WAIT).resources().stream()
                        .map(McpSchema.Resource::uri)
                        .toList());
        McpSchema.ReadResourceResult read =
                (McpSchema.ReadResourceResult)
                        modern.readResource(
                                        new McpSchema.ReadResourceRequest(
                                                "test://modern", null, null, null),
                                        null)
                                .block(WAIT);
        assertEquals(
                "modern", ((McpSchema.TextResourceContents) read.contents().getFirst()).text());
        assertEquals(
                List.of("welcome"),
                modern.listPrompts(null).block(WAIT).prompts().stream()
                        .map(McpSchema.Prompt::name)
                        .toList());
        McpSchema.GetPromptResult prompt =
                (McpSchema.GetPromptResult)
                        modern.getPrompt(
                                        new McpSchema.GetPromptRequest(
                                                "welcome", null, null, null, null),
                                        null)
                                .block(WAIT);
        assertEquals("modern welcome", prompt.description());
    }

    /** A call's progress notifications reach its consumer before its result. */
    @Test
    void streamsProgress() throws Exception {
        McpModernClient modern = start("HTTP2", null);
        List<String> progress = new CopyOnWriteArrayList<>();

        CallToolOutcome outcome =
                modern.callTool(
                                new CallToolRequest("progress", Map.of(), null, null, null),
                                update -> progress.add(update.message()))
                        .block(WAIT);

        assertEquals("done", text(outcome));
        assertEquals(List.of("half"), progress);
    }

    /** Without a handler, an input_required result is returned; with one, the call is retried. */
    @Test
    void answersInputRequired() throws Exception {
        McpModernClient modern = start("HTTP2", null);
        CallToolRequest approve = new CallToolRequest("approve", Map.of(), null, null, null);

        InputRequiredResult required =
                assertInstanceOf(InputRequiredResult.class, modern.callTool(approve).block(WAIT));
        assertNotNull(required.requestState()); // Sealed by the server.
        assertTrue(required.inputRequests().containsKey("confirm"));
        assertEquals(
                "approved:s1",
                text(
                        modern.callTool(
                                        new CallToolRequest(
                                                "approve",
                                                Map.of(),
                                                Map.of("confirm", accepted()),
                                                required.requestState(),
                                                null))
                                .block(WAIT)));

        client.stop();
        McpModernClient handled =
                start("HTTP2", result -> Mono.just(Map.of("confirm", accepted())));
        assertEquals("approved:s1", text(handled.callTool(approve).block(WAIT)));
    }

    /** A listen stream carries the changes published, and completes when the server stops. */
    @Test
    void listensToChanges() throws Exception {
        McpModernClient modern = start("HTTP2", null);
        BlockingQueue<JsonRpc.JSONRPCNotification> changes = new LinkedBlockingQueue<>();
        CountDownLatch completed = new CountDownLatch(1);
        List<Throwable> errors = new CopyOnWriteArrayList<>();
        Disposable listening =
                modern.listen(new McpSchema.SubscriptionFilter(true, null, null, null))
                        .doOnComplete(completed::countDown)
                        .subscribe(changes::add, errors::add);
        try {
            // The listen stream opens asynchronously: publish until the change arrives.
            JsonRpc.JSONRPCNotification change = null;
            for (int i = 0; i < 200 && change == null; i++) {
                adapter.getChanges().broadcast(new ServerChange.ToolsListChanged());
                change = changes.poll(50, TimeUnit.MILLISECONDS);
            }
            assertEquals(
                    McpSchema.METHOD_NOTIFICATION_TOOLS_LIST_CHANGED,
                    change == null ? null : change.method(),
                    errors.toString());

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
        McpModernClient modern = start("HTTP2", null);
        CountDownLatch started = new CountDownLatch(1);

        Disposable call =
                modern.callTool(
                                new CallToolRequest("endless", Map.of(), null, null, null),
                                update -> started.countDown())
                        .subscribe();
        assertTrue(started.await(10, TimeUnit.SECONDS), "the tool did not start");
        call.dispose();

        assertTrue(cancelled.await(10, TimeUnit.SECONDS), "the tool was not cancelled");
        assertEquals(
                "echo:after",
                text(
                        modern.callTool(
                                        new CallToolRequest(
                                                "echo", Map.of("text", "after"), null, null, null))
                                .block(WAIT)));
    }

    /** A listed tool's x-mcp-header arguments are mirrored into Mcp-Param headers. */
    @Test
    void mirrorsAnnotatedArguments() throws Exception {
        McpModernClient modern = start("HTTP2", null);
        CallToolRequest region =
                new CallToolRequest("region", Map.of("region", "eu"), null, null, null);

        Mono<CallToolOutcome> unlistedCall = modern.callTool(region);
        McpException unlisted = assertThrows(McpException.class, () -> unlistedCall.block(WAIT));
        assertEquals(McpSchema.ErrorCodes.HEADER_MISMATCH, unlisted.error().code().intValue());

        modern.listTools(null).block(WAIT);
        assertEquals("region:eu", text(modern.callTool(region).block(WAIT)));
    }

    /**
     * With a single call slot, an input handler may make a blocking call: the first call no longer
     * holds its slot, nor runs the handler on its interrupted worker.
     */
    @Test
    void anInputHandlerMayCallWithOneSlot() throws Exception {
        AtomicReference<McpModernClient> holder = new AtomicReference<>();
        List<String> nested = new CopyOnWriteArrayList<>();
        McpModernClient modern =
                start(
                        "HTTP2",
                        required ->
                                Mono.fromCallable(
                                        () -> {
                                            nested.add(
                                                    text(
                                                            holder.get()
                                                                    .callTool(
                                                                            new CallToolRequest(
                                                                                    "echo",
                                                                                    Map.of(
                                                                                            "text",
                                                                                            "nested"),
                                                                                    null,
                                                                                    null,
                                                                                    null))
                                                                    .block(WAIT)));
                                            return Map.of("confirm", accepted());
                                        }),
                        configured -> configured.setMaxConcurrentCalls(1));
        holder.set(modern);

        assertEquals(
                "approved:s1",
                text(
                        modern.callTool(new CallToolRequest("approve", Map.of(), null, null, null))
                                .block(WAIT)));
        assertEquals(List.of("echo:nested"), nested);
    }

    /** A call's headers are worked out when it is sent, after a listing chained before it. */
    @Test
    void headersFollowAChainedListing() throws Exception {
        McpModernClient modern = start("HTTP2", null);
        CallToolRequest region =
                new CallToolRequest("region", Map.of("region", "eu"), null, null, null);

        assertEquals(
                "region:eu",
                text(modern.listTools(null).then(modern.callTool(region)).block(WAIT)));
    }

    /** A number argument is mirrored in plain decimal notation. */
    @Test
    void mirrorsNumbersInPlainNotation() throws Exception {
        McpModernClient modern = start("HTTP2", null);
        modern.listTools(null).block(WAIT);

        assertEquals(
                "level:1.0E7",
                text(
                        modern.callTool(
                                        new CallToolRequest(
                                                "level", Map.of("level", 1.0e7), null, null, null))
                                .block(WAIT)));
    }

    /** An error response fails the request with its JSON-RPC error. */
    @Test
    void anErrorResponseFails() throws Exception {
        McpModernClient modern = start("HTTP2", null);

        Mono<McpSchema.ReadResourceOutcome> missing =
                modern.readResource(
                        new McpSchema.ReadResourceRequest("test://missing", null, null, null),
                        null);
        McpException error = assertThrows(McpException.class, () -> missing.block(WAIT));
        assertEquals(McpSchema.ErrorCodes.INVALID_PARAMS, error.error().code().intValue());
    }

    /** The modern client is reached through getModernClient() only, over HTTP for now. */
    @Test
    void modernModeLimits() throws Exception {
        start("HTTP2", null);
        assertThrows(IllegalStateException.class, client::getSdkClient);
        client.stop();
        assertFalse(client.isStarted());

        McpClientAdapter stdio = new McpClientAdapter(connector, "stdio://localhost/mcp");
        stdio.setProtocolVersion(McpProtocol.MODERN_VERSION);
        assertThrows(UnsupportedOperationException.class, stdio::start);
        McpClientAdapter unknown = new McpClientAdapter(connector, "http://localhost/mcp");
        assertThrows(
                IllegalArgumentException.class, () -> unknown.setProtocolVersion("2024-11-05"));
    }

    /** Starts the server, if needed, and a modern client with an optional input handler. */
    private McpModernClient start(String mode, McpInputHandler inputHandler) throws Exception {
        return start(mode, inputHandler, ignored -> {});
    }

    private McpModernClient start(
            String mode, McpInputHandler inputHandler, Consumer<McpClientAdapter> configure)
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
        client.setModernCapabilities(
                McpSchema.ClientCapabilities.builder()
                        .elicitation(
                                new McpSchema.ClientCapabilities.Elicitation(
                                        new McpSchema.ClientCapabilities.Elicitation.Form(), null))
                        .build());
        client.setInputHandler(inputHandler);
        configure.accept(client);
        client.start();
        return client.getModernClient();
    }

    private McpServerAdapter adapter() throws Exception {
        McpServerAdapter served = new McpServerAdapter();
        McpServerAdapterTestCase.addEcho(served);
        served.getModernTools()
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
        served.getModernTools()
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
        served.getModernTools()
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
        served.getModernTools()
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
        served.getModernTools()
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
        served.getModernResources()
                .add(
                        new McpModernResource(
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
        served.getModernPrompts()
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
