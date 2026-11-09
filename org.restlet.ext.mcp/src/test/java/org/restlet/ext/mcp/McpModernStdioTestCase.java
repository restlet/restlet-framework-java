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

import io.modelcontextprotocol.json.McpJsonDefaults;
import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.json.TypeRef;
import io.modelcontextprotocol.modern.McpSchema;
import io.modelcontextprotocol.modern.McpSchema.CallToolOutcome;
import io.modelcontextprotocol.modern.McpSchema.CallToolResult;
import io.modelcontextprotocol.modern.McpSchema.MetaKeys;
import io.modelcontextprotocol.modern.McpSchema.TextContent;
import io.modelcontextprotocol.modern.McpSchema.Tool;
import io.modelcontextprotocol.modern.server.McpAsyncResponse;
import io.modelcontextprotocol.modern.server.feature.McpAsyncTool;
import io.modelcontextprotocol.modern.server.feature.ServerChange;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.Pipe;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.restlet.Context;
import org.restlet.Server;
import org.restlet.data.Protocol;
import org.restlet.engine.Engine;
import reactor.core.publisher.Mono;

/**
 * MCP {@code 2026-07-28} over stdio, one JSON-RPC message per line: the revision is read from each
 * message's {@code _meta}, streamed messages come as separate lines, listen streams stay open, and
 * a cancelled request ends without a response.
 */
class McpModernStdioTestCase {
    private static final McpJsonMapper MAPPER = McpJsonDefaults.getMapper();

    /** The adapter's listen limit, above the default, which stdio must not lower. */
    private static final int LISTENS = McpProtocol.DEFAULT_MAX_LISTEN_STREAMS + 2;

    private final CountDownLatch cancelled = new CountDownLatch(1);
    private final StdioServerHelper prototype = new StdioServerHelper(null);
    private McpServerAdapter adapter;
    private Server server;
    private OutputStream toServer;
    private final java.util.concurrent.BlockingQueue<String> lines =
            new java.util.concurrent.LinkedBlockingQueue<>();

    @BeforeEach
    void start() throws Exception {
        Pipe requests = Pipe.open();
        Pipe responses = Pipe.open();
        org.restlet.util.StreamTransport stream =
                new org.restlet.util.StreamTransport(
                        Channels.newInputStream(requests.source()),
                        Channels.newOutputStream(responses.sink()),
                        () -> {
                            try {
                                requests.source().close();
                                responses.sink().close();
                            } catch (IOException e) {
                                throw new java.io.UncheckedIOException(e);
                            }
                        });
        toServer = Channels.newOutputStream(requests.sink());
        BufferedReader fromServer =
                new BufferedReader(
                        new InputStreamReader(
                                Channels.newInputStream(responses.source()),
                                StandardCharsets.UTF_8));
        Thread.ofVirtual()
                .start(
                        () -> {
                            try {
                                for (String line = fromServer.readLine();
                                        line != null;
                                        line = fromServer.readLine()) {
                                    lines.add(line);
                                }
                            } catch (IOException _) {
                                // The server output ended.
                            }
                        });
        adapter = new McpServerAdapter();
        McpServerAdapterTestCase.addEcho(adapter);
        adapter.getModernTools()
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
                                                                                        text(
                                                                                                "done")))))));
        adapter.getModernTools()
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
        adapter.setMaxListenStreams(LISTENS);
        adapter.start();
        Engine.getInstance().getRegisteredServers().add(prototype);
        Context context = new Context();
        context.getAttributes().put(McpProtocol.STREAM_TRANSPORT_ATTRIBUTE, stream);
        server =
                new Server(
                        context,
                        List.of(Protocol.STDIO),
                        null,
                        -1,
                        adapter,
                        StdioServerHelper.class.getCanonicalName());
        server.start();
    }

    @AfterEach
    void stop() throws Exception {
        try {
            if (server != null) {
                server.stop();
            }
        } finally {
            Engine.getInstance().getRegisteredServers().remove(prototype);
            adapter.stop();
        }
    }

    /** Discovery and calls are answered on one line each, without HTTP headers. */
    @Test
    void discoveryAndCallsAreAnswered() throws Exception {
        send(request(1, "server/discover", Map.of(), meta()));
        Map<String, Object> discover = result(read());
        assertEquals(List.of(McpProtocol.MODERN_VERSION), discover.get("supportedVersions"));

        send(
                request(
                        2,
                        "tools/call",
                        Map.of("name", "echo", "arguments", Map.of("text", "line")),
                        meta()));
        Map<String, Object> reply = read();
        assertEquals(2, ((Number) reply.get("id")).intValue());
        assertEquals("echo:line", firstText(result(reply)));
    }

    /** A streamed call writes its progress, then its result, as separate lines. */
    @Test
    void streamedMessagesComeAsSeparateLines() throws Exception {
        Map<String, Object> meta = meta();
        meta.put(MetaKeys.PROGRESS_TOKEN, "p1");
        send(request(3, "tools/call", Map.of("name", "progress", "arguments", Map.of()), meta));

        Map<String, Object> progress = read();
        assertEquals("notifications/progress", progress.get("method"));
        Map<String, Object> reply = read();
        assertEquals(3, ((Number) reply.get("id")).intValue());
        assertEquals("done", firstText(result(reply)));
    }

    /** A cancelled request ends its handler and gets no response; the cancel needs no _meta. */
    @Test
    void aCancelledRequestEndsWithoutAResponse() throws Exception {
        Map<String, Object> meta = meta();
        meta.put(MetaKeys.PROGRESS_TOKEN, "p2");
        send(request(4, "tools/call", Map.of("name", "endless", "arguments", Map.of()), meta));
        assertEquals("notifications/progress", read().get("method"));

        Map<String, Object> cancel = new LinkedHashMap<>();
        cancel.put("jsonrpc", "2.0");
        cancel.put("method", "notifications/cancelled");
        cancel.put("params", Map.of("requestId", 4));
        send(cancel);
        assertTrue(cancelled.await(10, TimeUnit.SECONDS), "the handler was not cancelled");

        send(
                request(
                        5,
                        "tools/call",
                        Map.of("name", "echo", "arguments", Map.of("text", "after")),
                        meta()));
        Map<String, Object> next = read();
        assertEquals(5, ((Number) next.get("id")).intValue(), next.toString());
    }

    /** A listen stream stays open over stdio, carrying changes between other responses. */
    @Test
    void aListenStreamCarriesChanges() throws Exception {
        send(
                request(
                        6,
                        McpSchema.METHOD_SUBSCRIPTIONS_LISTEN,
                        Map.of("notifications", Map.of("toolsListChanged", true)),
                        meta()));
        assertEquals(
                McpSchema.METHOD_NOTIFICATION_SUBSCRIPTIONS_ACKNOWLEDGED, read().get("method"));

        send(
                request(
                        7,
                        "tools/call",
                        Map.of("name", "echo", "arguments", Map.of("text", "beside")),
                        meta()));
        assertEquals("echo:beside", firstText(result(read())));
        adapter.getChanges().broadcast(new ServerChange.ToolsListChanged());
        assertEquals(McpSchema.METHOD_NOTIFICATION_TOOLS_LIST_CHANGED, read().get("method"));
    }

    /** A request naming the cancellation method is answered, and cancels nothing. */
    @Test
    void aRequestNamedCancelledIsAnsweredAndCancelsNothing() throws Exception {
        Map<String, Object> meta = meta();
        meta.put(MetaKeys.PROGRESS_TOKEN, "p3");
        send(request(8, "tools/call", Map.of("name", "endless", "arguments", Map.of()), meta));
        assertEquals("notifications/progress", read().get("method"));

        send(request(9, "notifications/cancelled", Map.of("requestId", 8), meta()));
        Map<String, Object> reply = read();

        assertEquals(9, ((Number) reply.get("id")).intValue(), reply.toString());
        assertTrue(reply.containsKey("error"), reply.toString());
        assertEquals(1, cancelled.getCount(), "the call was cancelled");
    }

    /** The adapter's listen limit applies over stdio, above the default too. */
    @Test
    void theAdapterListenLimitAppliesOverStdio() throws Exception {
        for (int id = 100; id < 100 + LISTENS; id++) {
            send(
                    request(
                            id,
                            McpSchema.METHOD_SUBSCRIPTIONS_LISTEN,
                            Map.of("notifications", Map.of("toolsListChanged", true)),
                            meta()));
        }
        for (int i = 0; i < LISTENS; i++) {
            Map<String, Object> ack = read();
            assertEquals(
                    McpSchema.METHOD_NOTIFICATION_SUBSCRIPTIONS_ACKNOWLEDGED,
                    ack.get("method"),
                    ack.toString());
        }

        send(
                request(
                        200,
                        McpSchema.METHOD_SUBSCRIPTIONS_LISTEN,
                        Map.of("notifications", Map.of("toolsListChanged", true)),
                        meta()));
        Map<String, Object> refused = read();
        assertEquals(200, ((Number) refused.get("id")).intValue(), refused.toString());
        assertTrue(refused.containsKey("error"), refused.toString());
    }

    /** A request whose id is neither a string nor a number is answered with a null id. */
    @Test
    void anInvalidIdIsNotEchoed() throws Exception {
        Map<String, Object> envelope = request(0, "tools/list", Map.of(), meta());
        envelope.put("id", Map.of("not", "an id"));
        send(envelope);
        Map<String, Object> reply = read();

        assertTrue(reply.containsKey("id"), reply.toString());
        assertEquals(null, reply.get("id"), reply.toString());
        assertTrue(reply.containsKey("error"), reply.toString());
    }

    private static Map<String, Object> request(
            int id, String method, Map<String, Object> params, Map<String, Object> meta) {
        Map<String, Object> withMeta = new LinkedHashMap<>(params);
        withMeta.put("_meta", meta);
        Map<String, Object> envelope = new LinkedHashMap<>();
        envelope.put("jsonrpc", "2.0");
        envelope.put("id", id);
        envelope.put("method", method);
        envelope.put("params", withMeta);
        return envelope;
    }

    private static Map<String, Object> meta() {
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put(MetaKeys.PROTOCOL_VERSION, McpProtocol.MODERN_VERSION);
        meta.put(MetaKeys.CLIENT_CAPABILITIES, Map.of());
        return meta;
    }

    private void send(Map<String, Object> message) throws IOException {
        toServer.write(
                (MAPPER.writeValueAsString(message) + "\n").getBytes(StandardCharsets.UTF_8));
        toServer.flush();
    }

    private Map<String, Object> read() throws Exception {
        String line = lines.poll(10, TimeUnit.SECONDS);
        assertNotNull(line, "no server output");
        return MAPPER.readValue(line, new TypeRef<Map<String, Object>>() {});
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> result(Map<String, Object> envelope) {
        assertTrue(envelope.containsKey("result"), envelope.toString());
        return (Map<String, Object>) envelope.get("result");
    }

    private static String firstText(Map<String, Object> result) {
        return (String) ((Map<?, ?>) ((List<?>) result.get("content")).get(0)).get("text");
    }

    private static CallToolResult text(String text) {
        return CallToolResult.builder().addContent(TextContent.builder(text).build()).build();
    }
}
