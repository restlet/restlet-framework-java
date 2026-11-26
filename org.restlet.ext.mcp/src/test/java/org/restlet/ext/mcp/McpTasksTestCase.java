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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.modelcontextprotocol.json.McpJsonDefaults;
import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.json.TypeRef;
import io.modelcontextprotocol.modern.McpException;
import io.modelcontextprotocol.modern.McpSchema;
import io.modelcontextprotocol.modern.McpSchema.CallToolOutcome;
import io.modelcontextprotocol.modern.McpSchema.CallToolRequest;
import io.modelcontextprotocol.modern.McpSchema.MetaKeys;
import io.modelcontextprotocol.modern.McpSchema.Tool;
import io.modelcontextprotocol.modern.server.McpAsyncResponse;
import io.modelcontextprotocol.modern.server.feature.McpAsyncTool;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Server;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.representation.StringRepresentation;
import org.restlet.security.User;
import reactor.core.publisher.Mono;

/**
 * The MCP Tasks extension: the {@code tasks/*} methods on an application's store, task results of
 * tools, their negotiation, and the client's polling.
 */
class McpTasksTestCase {
    private static final McpJsonMapper MAPPER = McpJsonDefaults.getMapper();
    private static final String MODERN = McpProtocol.MODERN_VERSION;
    private static final Instant CREATED = Instant.parse("2026-11-26T10:00:00Z");
    private static final Duration WAIT = Duration.ofSeconds(10);

    private final List<McpServerAdapter> adapters = new ArrayList<>();
    private final Tasks store = new Tasks();
    private Server server;
    private Client connector;
    private McpClientAdapter client;

    @AfterEach
    void cleanup() throws Exception {
        if (client != null) {
            client.stop();
            client = null;
        }
        if (connector != null) {
            connector.stop();
            connector = null;
        }
        if (server != null) {
            server.stop();
            server = null;
        }
        for (McpServerAdapter adapter : adapters) {
            adapter.stop();
        }
        adapters.clear();
    }

    /** A tool answers a task to a client declaring the extension, which discover advertises. */
    @Test
    void aToolAnswersATaskToAClientDeclaringTheExtension() throws Exception {
        McpServerAdapter adapter = adapter();
        adapter.start();

        Map<String, Object> discover =
                result(adapter.handle(modern("server/discover", Map.of(), null, meta(true))));
        Map<?, ?> extensions =
                (Map<?, ?>) ((Map<?, ?>) discover.get("capabilities")).get("extensions");
        assertEquals(Map.of(), extensions.get(McpTask.EXTENSION_ID));

        Map<String, Object> task = call(adapter, "slow", meta(true));
        assertEquals("task", task.get("resultType"));
        assertEquals("t-1", task.get("taskId"));
        assertEquals("working", task.get("status"));
        assertEquals(CREATED.toString(), task.get("createdAt"));
        assertEquals(60000, ((Number) task.get("ttlMs")).intValue());
        assertEquals(100, ((Number) task.get("pollIntervalMs")).intValue());
        assertEquals("alice", store.owners.get("t-1"));

        Map<String, Object> direct = call(adapter, "slow", meta(false));
        assertEquals("complete", direct.get("resultType"));
    }

    /** tasks/get answers the store's state, for the task's owner only. */
    @Test
    void tasksGetAnswersTheStateToItsOwner() throws Exception {
        McpServerAdapter adapter = adapter();
        adapter.start();
        call(adapter, "slow", meta(true));
        store.finish("t-1");

        Map<String, Object> got =
                result(
                        adapter.handle(
                                as(
                                        "alice",
                                        modern(
                                                "tasks/get",
                                                Map.of("taskId", "t-1"),
                                                "t-1",
                                                meta(true)))));
        assertEquals("complete", got.get("resultType"));
        assertEquals("completed", got.get("status"));
        assertEquals("done", firstText((Map<?, ?>) got.get("result")));
        assertFalse(got.containsKey("pollIntervalMs"));

        Response other =
                adapter.handle(
                        as("bob", modern("tasks/get", Map.of("taskId", "t-1"), "t-1", meta(true))));
        Map<?, ?> error = (Map<?, ?>) envelope(other).get("error");
        assertEquals(-32602, ((Number) error.get("code")).intValue());
        assertEquals("Task not found", error.get("message"));
    }

    /** The task methods check their taskId and its Mcp-Name header. */
    @Test
    void taskMethodsCheckTheirTaskId() throws Exception {
        McpServerAdapter adapter = adapter();
        adapter.start();

        Response missingHeader =
                adapter.handle(modern("tasks/get", Map.of("taskId", "t-1"), null, meta(true)));
        assertEquals(-32020, code(missingHeader));
        Response missingId = adapter.handle(modern("tasks/get", Map.of(), null, meta(true)));
        assertEquals(-32602, code(missingId));
        Response unknown =
                adapter.handle(
                        as(
                                "alice",
                                modern(
                                        "tasks/cancel",
                                        Map.of("taskId", "t-9"),
                                        "t-9",
                                        meta(true))));
        assertEquals(-32602, code(unknown));
    }

    /** tasks/update delivers the answers, tasks/cancel the cancellation. */
    @Test
    void updateAndCancelReachTheStore() throws Exception {
        McpServerAdapter adapter = adapter();
        adapter.start();
        call(adapter, "slow", meta(true));

        Map<String, Object> update = new LinkedHashMap<>();
        update.put("taskId", "t-1");
        update.put("inputResponses", Map.of("confirm", Map.of("action", "accept")));
        Map<String, Object> updated =
                result(
                        adapter.handle(
                                as("alice", modern("tasks/update", update, "t-1", meta(true)))));
        assertEquals("complete", updated.get("resultType"));
        assertEquals(Map.of("confirm", Map.of("action", "accept")), store.answers.get("t-1"));

        Response malformed =
                adapter.handle(
                        as(
                                "alice",
                                modern(
                                        "tasks/update",
                                        Map.of("taskId", "t-1", "inputResponses", "yes"),
                                        "t-1",
                                        meta(true))));
        assertEquals(-32602, code(malformed));

        result(
                adapter.handle(
                        as(
                                "alice",
                                modern(
                                        "tasks/cancel",
                                        Map.of("taskId", "t-1"),
                                        "t-1",
                                        meta(true)))));
        assertEquals(List.of("t-1"), store.cancelled);
    }

    /** A task can't reach a client that didn't declare the extension, nor come without a store. */
    @Test
    void aTaskReachesOnlyDeclaringClientsOfAStore() throws Exception {
        McpServerAdapter adapter = new McpServerAdapter();
        adapters.add(adapter);
        adapter.getTools().add(alwaysTask());
        adapter.setTaskStore(store);
        adapter.start();
        assertEquals(
                -32603,
                code(
                        adapter.handle(
                                as(
                                        "alice",
                                        modern(
                                                "tools/call",
                                                Map.of("name", "always", "arguments", Map.of()),
                                                "always",
                                                meta(false))))));

        McpServerAdapter storeless = new McpServerAdapter();
        adapters.add(storeless);
        storeless.getTools().add(alwaysTask());
        storeless.start();
        assertEquals(
                -32603,
                code(
                        storeless.handle(
                                modern(
                                        "tools/call",
                                        Map.of("name", "always", "arguments", Map.of()),
                                        "always",
                                        meta(true)))));
    }

    /** The application's own settings for the extension are kept. */
    @Test
    void applicationSettingsForTheExtensionAreKept() throws Exception {
        McpServerAdapter adapter = adapter();
        adapter.getExtensions().put(McpTask.EXTENSION_ID, Map.of("maxTtlMs", 5));
        adapter.start();

        Map<String, Object> discover =
                result(adapter.handle(modern("server/discover", Map.of(), null, meta(true))));
        Map<?, ?> extensions =
                (Map<?, ?>) ((Map<?, ?>) discover.get("capabilities")).get("extensions");
        assertEquals(
                5,
                ((Number) ((Map<?, ?>) extensions.get(McpTask.EXTENSION_ID)).get("maxTtlMs"))
                        .intValue());
    }

    /**
     * The client gets the task of a call, then waits for it: the task's questions go to the input
     * handler and its answers to tasks/update, until the result.
     */
    @Test
    void theClientAwaitsATaskAnsweringItsQuestions() throws Exception {
        store.askFirst = true;
        List<Map<String, Object>> asked = new CopyOnWriteArrayList<>();
        McpModernClient modern =
                start(
                        required -> {
                            asked.add(MAPPER.convertValue(required, new TypeRef<>() {}));
                            return Mono.just(Map.of("confirm", Map.of("action", "accept")));
                        });

        CallToolOutcome outcome =
                modern.callTool(new CallToolRequest("slow", Map.of(), null, null, null))
                        .block(WAIT);
        McpTaskResult task = assertInstanceOf(McpTaskResult.class, outcome);
        assertEquals("t-1", task.task().taskId());

        CallToolOutcome done = modern.awaitTask(task).block(WAIT);
        assertEquals(
                "done",
                firstText(MAPPER.convertValue(done, new TypeRef<Map<String, Object>>() {})));
        assertEquals(1, asked.size(), asked.toString());
        assertTrue(((Map<?, ?>) asked.get(0).get("inputRequests")).containsKey("confirm"));
        assertEquals(Map.of("confirm", Map.of("action", "accept")), store.answers.get("t-1"));
    }

    /** Waiting for a task through thousands of polls grows neither the stack nor the chain. */
    @Test
    void theClientAwaitsThroughManyPolls() throws Exception {
        store.workingPolls.set(3000);
        McpModernClient modern = start(null);
        McpTaskResult task =
                assertInstanceOf(
                        McpTaskResult.class,
                        modern.callTool(new CallToolRequest("slow", Map.of(), null, null, null))
                                .block(WAIT));

        CallToolOutcome done = modern.awaitTask(task, 0).block(Duration.ofMinutes(2));
        assertEquals(
                "done",
                firstText(MAPPER.convertValue(done, new TypeRef<Map<String, Object>>() {})));
        assertEquals(0, store.workingPolls.get());
    }

    /**
     * A task waiting on input is handed back without a handler, when the handler gives no answers,
     * and past the round limit.
     */
    @Test
    void theClientHandsBackATaskWaitingOnInput() throws Exception {
        store.askFirst = true;
        McpModernClient bare = start(null);
        McpTaskResult first =
                assertInstanceOf(
                        McpTaskResult.class,
                        bare.callTool(new CallToolRequest("slow", Map.of(), null, null, null))
                                .block(WAIT));
        McpTaskResult waiting =
                assertInstanceOf(McpTaskResult.class, bare.awaitTask(first).block(WAIT));
        assertEquals(McpTask.Status.INPUT_REQUIRED, waiting.task().status());
        cleanup();

        McpModernClient silent = start(required -> Mono.empty());
        McpTaskResult second =
                assertInstanceOf(
                        McpTaskResult.class,
                        silent.callTool(new CallToolRequest("slow", Map.of(), null, null, null))
                                .block(WAIT));
        assertEquals(
                McpTask.Status.INPUT_REQUIRED,
                assertInstanceOf(McpTaskResult.class, silent.awaitTask(second).block(WAIT))
                        .task()
                        .status());
        cleanup();

        store.askAlways = true;
        List<String> rounds = new CopyOnWriteArrayList<>();
        McpModernClient limited =
                start(
                        required -> {
                            rounds.add("asked");
                            return Mono.just(Map.of("confirm", Map.of("action", "accept")));
                        },
                        client -> client.setMaxInputRounds(2));
        McpTaskResult third =
                assertInstanceOf(
                        McpTaskResult.class,
                        limited.callTool(new CallToolRequest("slow", Map.of(), null, null, null))
                                .block(WAIT));
        assertEquals(
                McpTask.Status.INPUT_REQUIRED,
                assertInstanceOf(McpTaskResult.class, limited.awaitTask(third).block(WAIT))
                        .task()
                        .status());
        assertEquals(2, rounds.size());
    }

    /** The guard also covers a task streamed as the result of a streaming tool. */
    @Test
    void theGuardCoversStreamedTasks() throws Exception {
        McpServerAdapter adapter = new McpServerAdapter();
        adapters.add(adapter);
        adapter.getTools()
                .add(
                        McpAsyncTool.of(
                                Tool.builder("streamed", Map.of("type", "object")).build(),
                                (ctx, request) ->
                                        Mono.just(
                                                McpAsyncResponse.<CallToolOutcome>streaming(
                                                        notifier ->
                                                                Mono.just(
                                                                        new McpTaskResult(
                                                                                McpTask.working(
                                                                                        "t-1",
                                                                                        CREATED,
                                                                                        null,
                                                                                        null)))))));
        adapter.setTaskStore(store);
        adapter.start();

        Response response =
                adapter.handle(
                        modern(
                                "tools/call",
                                Map.of("name", "streamed", "arguments", Map.of()),
                                "streamed",
                                meta(false)));
        var bytes = new ByteArrayOutputStream();
        response.getEntity().write(bytes);
        String body = bytes.toString(StandardCharsets.UTF_8);
        assertTrue(body.contains("-32603"), body);
        assertFalse(body.contains("\"taskId\""), body);
    }

    /** The client's task methods, and a failed or cancelled task ending the wait with an error. */
    @Test
    void theClientReportsFailedAndCancelledTasks() throws Exception {
        McpModernClient modern = start(null);
        CallToolOutcome outcome =
                modern.callTool(new CallToolRequest("slow", Map.of(), null, null, null))
                        .block(WAIT);
        McpTaskResult task = assertInstanceOf(McpTaskResult.class, outcome);

        store.fail("t-1");
        McpException failed =
                assertThrows(McpException.class, () -> modern.awaitTask(task).block(WAIT));
        assertEquals(-32603, failed.error().code());
        assertEquals("boom", failed.error().message());

        modern.cancelTask("t-1").block(WAIT);
        assertEquals(List.of("t-1"), store.cancelled);
        assertEquals(McpTask.Status.CANCELLED, modern.getTask("t-1").block(WAIT).status());
        assertThrows(McpException.class, () -> modern.awaitTask(task).block(WAIT));
        assertThrows(McpException.class, () -> modern.getTask("t-9").block(WAIT));
    }

    /** A task state checks what its status carries, and reads back what it writes. */
    @Test
    void aTaskStateChecksItsFieldsAndRoundTrips() {
        McpTask working = McpTask.working("t-1", CREATED, null, 500L);
        assertNull(working.toFields().get("ttlMs"));
        assertTrue(working.toFields().containsKey("ttlMs"));
        assertEquals(working, McpTask.fromFields(working.toFields()));
        McpTask asking =
                new McpTask(
                        "t-1",
                        McpTask.Status.INPUT_REQUIRED,
                        CREATED,
                        CREATED,
                        1L,
                        null,
                        Map.of("q", Map.of()),
                        null,
                        null);
        assertEquals(asking, McpTask.fromFields(asking.toFields()));

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        new McpTask(
                                "t-1",
                                McpTask.Status.WORKING,
                                CREATED,
                                CREATED,
                                null,
                                null,
                                null,
                                Map.of(),
                                null));
        assertThrows(
                IllegalArgumentException.class, () -> McpTask.working(" ", CREATED, null, null));
        assertThrows(
                IllegalArgumentException.class, () -> McpTask.working("t-1", CREATED, -1L, null));
        assertThrows(IllegalArgumentException.class, () -> McpTask.Status.of("done"));
        assertThrows(IllegalArgumentException.class, () -> McpTask.fromFields(Map.of("taskId", 1)));
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        new McpTaskResult(
                                new McpTask(
                                        "t-1",
                                        McpTask.Status.CANCELLED,
                                        CREATED,
                                        CREATED,
                                        null,
                                        null,
                                        null,
                                        null,
                                        null)));
        assertTrue(McpTask.Status.FAILED.isTerminal());
        assertFalse(McpTask.Status.INPUT_REQUIRED.isTerminal());
    }

    /** The tasks of the tests: t-1, t-2… owned by their creator. */
    private static final class Tasks implements McpTaskStore {
        private final Map<String, McpTask> tasks = new ConcurrentHashMap<>();
        private final Map<String, String> owners = new ConcurrentHashMap<>();
        private final Map<String, Map<String, Object>> answers = new ConcurrentHashMap<>();
        private final List<String> cancelled = new CopyOnWriteArrayList<>();
        private volatile boolean askFirst;
        private volatile boolean askAlways;
        private final java.util.concurrent.atomic.AtomicInteger workingPolls =
                new java.util.concurrent.atomic.AtomicInteger();

        McpTask create(McpRequestContext context) {
            String id = "t-" + (tasks.size() + 1);
            McpTask task = McpTask.working(id, CREATED, 60000L, 100L);
            tasks.put(id, task);
            owners.put(id, context.principal().orElse(""));
            return task;
        }

        void finish(String id) {
            tasks.put(id, state(id, McpTask.Status.COMPLETED, null, done(), null));
        }

        void fail(String id) {
            tasks.put(
                    id,
                    state(
                            id,
                            McpTask.Status.FAILED,
                            null,
                            null,
                            Map.of("code", -32603, "message", "boom")));
        }

        @Override
        public Mono<McpTask> get(String taskId, McpRequestContext context) {
            if (!context.principal().orElse("").equals(owners.get(taskId))) {
                return Mono.empty();
            }
            McpTask task = tasks.get(taskId);
            if (workingPolls.get() > 0 && task.status() == McpTask.Status.WORKING) {
                return Mono.just(
                        workingPolls.decrementAndGet() > 0
                                ? new McpTask(
                                        taskId,
                                        McpTask.Status.WORKING,
                                        CREATED,
                                        CREATED,
                                        null,
                                        0L,
                                        null,
                                        null,
                                        null)
                                : state(taskId, McpTask.Status.COMPLETED, null, done(), null));
            }
            if (askFirst && task.status() == McpTask.Status.WORKING) {
                return Mono.just(
                        answers.containsKey(taskId) && !askAlways
                                ? state(taskId, McpTask.Status.COMPLETED, null, done(), null)
                                : state(
                                        taskId,
                                        McpTask.Status.INPUT_REQUIRED,
                                        Map.of(
                                                "confirm",
                                                Map.of(
                                                        "method",
                                                        "elicitation/create",
                                                        "params",
                                                        Map.of(
                                                                "mode",
                                                                "form",
                                                                "message",
                                                                "Proceed?",
                                                                "requestedSchema",
                                                                Map.of(
                                                                        "type",
                                                                        "object",
                                                                        "properties",
                                                                        Map.of())))),
                                        null,
                                        null));
            }
            return Mono.just(task);
        }

        @Override
        public Mono<Boolean> update(
                String taskId, Map<String, Object> inputResponses, McpRequestContext context) {
            if (!context.principal().orElse("").equals(owners.get(taskId))) {
                return Mono.just(false);
            }
            answers.put(taskId, inputResponses);
            return Mono.just(true);
        }

        @Override
        public Mono<Boolean> cancel(String taskId, McpRequestContext context) {
            if (!context.principal().orElse("").equals(owners.get(taskId))) {
                return Mono.just(false);
            }
            cancelled.add(taskId);
            tasks.put(taskId, state(taskId, McpTask.Status.CANCELLED, null, null, null));
            return Mono.just(true);
        }

        private static McpTask state(
                String id,
                McpTask.Status status,
                Map<String, Object> inputRequests,
                Map<String, Object> result,
                Map<String, Object> error) {
            return new McpTask(
                    id,
                    status,
                    CREATED,
                    CREATED.plusSeconds(1),
                    60000L,
                    status.isTerminal() ? null : 100L,
                    inputRequests,
                    result,
                    error);
        }

        private static Map<String, Object> done() {
            return Map.of("content", List.of(Map.of("type", "text", "text", "done")));
        }
    }

    private McpServerAdapter adapter() {
        McpServerAdapter adapter = new McpServerAdapter();
        adapters.add(adapter);
        adapter.getTools()
                .add(
                        McpAsyncTool.of(
                                Tool.builder("slow", Map.of("type", "object")).build(),
                                (ctx, request) ->
                                        Mono.just(
                                                McpAsyncResponse.<CallToolOutcome>result(
                                                        McpTaskResult.isDeclared(ctx)
                                                                ? new McpTaskResult(
                                                                        store.create(
                                                                                (McpRequestContext)
                                                                                        ctx.transportContext()
                                                                                                .get(
                                                                                                        McpProtocol
                                                                                                                .CONTEXT_KEY)))
                                                                : McpTestFeatures.text("done")))));
        adapter.setTaskStore(store);
        return adapter;
    }

    private static McpAsyncTool alwaysTask() {
        return McpAsyncTool.of(
                Tool.builder("always", Map.of("type", "object")).build(),
                (ctx, request) ->
                        Mono.just(
                                McpAsyncResponse.<CallToolOutcome>result(
                                        new McpTaskResult(
                                                McpTask.working("t-1", CREATED, null, null)))));
    }

    private McpModernClient start(McpInputHandler inputHandler) throws Exception {
        return start(inputHandler, ignored -> {});
    }

    private McpModernClient start(
            McpInputHandler inputHandler, java.util.function.Consumer<McpClientAdapter> configure)
            throws Exception {
        McpServerAdapter adapter = adapter();
        adapter.start();
        server = new Server(new Context(), Protocol.HTTP, 0, adapter);
        server.start();
        Context cc = new Context();
        cc.getParameters().set("followRedirects", "false");
        connector = new Client(cc, Protocol.HTTP);
        connector.start();
        client =
                new McpClientAdapter(
                        connector, "http://localhost:" + server.getActualPort() + "/mcp");
        client.setProtocolVersion(MODERN);
        client.setModernCapabilities(
                McpSchema.ClientCapabilities.builder()
                        .elicitation(
                                new McpSchema.ClientCapabilities.Elicitation(
                                        new McpSchema.ClientCapabilities.Elicitation.Form(), null))
                        .extensions(Map.of(McpTask.EXTENSION_ID, Map.of()))
                        .build());
        client.setInputHandler(inputHandler);
        configure.accept(client);
        client.start();
        return client.getModernClient();
    }

    private Map<String, Object> call(
            McpServerAdapter adapter, String tool, Map<String, Object> meta) throws Exception {
        return result(
                adapter.handle(
                        as(
                                "alice",
                                modern(
                                        "tools/call",
                                        Map.of("name", tool, "arguments", Map.of()),
                                        tool,
                                        meta))));
    }

    private static Request as(String user, Request request) {
        request.getClientInfo().setUser(new User(user));
        return request;
    }

    private static Map<String, Object> meta(boolean tasks) {
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put(MetaKeys.PROTOCOL_VERSION, MODERN);
        meta.put(
                MetaKeys.CLIENT_CAPABILITIES,
                tasks ? Map.of("extensions", Map.of(McpTask.EXTENSION_ID, Map.of())) : Map.of());
        return meta;
    }

    /** A 2026-07-28 request with its _meta and headers. */
    private static Request modern(
            String method, Map<String, Object> params, String name, Map<String, Object> meta) {
        Map<String, Object> withMeta = new LinkedHashMap<>(params);
        withMeta.put("_meta", meta);
        Map<String, Object> envelope = new LinkedHashMap<>();
        envelope.put("jsonrpc", "2.0");
        envelope.put("id", 1);
        envelope.put("method", method);
        envelope.put("params", withMeta);
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
        request.getHeaders().set("Mcp-Method", method);
        if (name != null) {
            request.getHeaders().set("Mcp-Name", name);
        }
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

    private static String firstText(Map<?, ?> result) {
        return (String) ((Map<?, ?>) ((List<?>) result.get("content")).get(0)).get("text");
    }
}
