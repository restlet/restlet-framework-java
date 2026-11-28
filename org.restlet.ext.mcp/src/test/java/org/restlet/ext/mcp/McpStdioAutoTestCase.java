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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.modelcontextprotocol.json.McpJsonDefaults;
import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.json.TypeRef;
import io.modelcontextprotocol.modern.McpSchema;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.channels.Channels;
import java.nio.channels.Pipe;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.Server;
import org.restlet.data.Protocol;
import org.restlet.engine.Engine;
import org.restlet.ext.mcp.internal.McpStdioChannel;
import org.restlet.util.StreamTransport;

/**
 * {@link McpProtocol#AUTO} over stdio: the client probes with {@code server/discover} on the stream
 * it then keeps, selecting the modern client for a {@code 2026-07-28} server and falling back to a
 * legacy {@code initialize} for a legacy one, which answers the probe with an error or not at all.
 */
class McpStdioAutoTestCase {
    private static final McpJsonMapper MAPPER = McpJsonDefaults.getMapper();
    private static final TypeRef<Map<String, Object>> MAP = new TypeRef<>() {};

    private final List<AutoCloseable> cleanup = new ArrayList<>();

    @AfterEach
    void stop() throws Exception {
        Exception failure = null;
        for (AutoCloseable closeable : cleanup.reversed()) {
            try {
                closeable.close();
            } catch (Exception e) {
                if (failure == null) {
                    failure = e;
                }
            }
        }
        if (failure != null) {
            throw failure;
        }
    }

    /** A stream over two pipes, from one and to the other. */
    private static StreamTransport stream(Pipe in, Pipe out) {
        return new StreamTransport(
                Channels.newInputStream(in.source()),
                Channels.newOutputStream(out.sink()),
                () -> {
                    try {
                        in.source().close();
                        out.sink().close();
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });
    }

    /** A Restlet server on stdio, serving both eras, and the client's end of its stream. */
    private StreamTransport restletServer() throws Exception {
        Pipe requests = Pipe.open();
        Pipe responses = Pipe.open();
        McpServerAdapter adapter = new McpServerAdapter();
        McpServerAdapterTestCase.addEcho(adapter);
        adapter.start();
        cleanup.add(adapter::stop);
        StdioServerHelper prototype = new StdioServerHelper(null);
        Engine.getInstance().getRegisteredServers().add(prototype);
        cleanup.add(() -> Engine.getInstance().getRegisteredServers().remove(prototype));
        Context context = new Context();
        context.getAttributes()
                .put(McpProtocol.STREAM_TRANSPORT_ATTRIBUTE, stream(requests, responses));
        Server server =
                new Server(
                        context,
                        List.of(Protocol.STDIO),
                        null,
                        -1,
                        adapter,
                        StdioServerHelper.class.getCanonicalName());
        server.start();
        cleanup.add(server::stop);
        return stream(responses, requests);
    }

    private McpClientAdapter client(StreamTransport stream, String version, Duration timeout) {
        McpClientAdapter client = new McpClientAdapter(stream);
        client.setProtocolVersion(version);
        client.setRequestTimeout(timeout);
        cleanup.add(client::stop);
        return client;
    }

    private static String text(McpSchema.CallToolResult result) {
        return ((McpSchema.TextContent) result.content().getFirst()).text();
    }

    /** A dual-era Restlet server, on a stdio connector, gets the modern client. */
    @Test
    void aDualEraServerGetsTheModernClient() throws Exception {
        StreamTransport stream = restletServer();
        StdioClientHelper prototype = new StdioClientHelper(null);
        Engine.getInstance().getRegisteredClients().add(prototype);
        cleanup.add(() -> Engine.getInstance().getRegisteredClients().remove(prototype));
        Context context = new Context();
        context.getAttributes().put(McpProtocol.STREAM_TRANSPORT_ATTRIBUTE, stream);
        Client connector =
                new Client(
                        context,
                        List.of(Protocol.STDIO),
                        StdioClientHelper.class.getCanonicalName());
        connector.start();
        cleanup.add(connector::stop);
        McpClientAdapter client = new McpClientAdapter(connector, "stdio://localhost/mcp");
        client.setProtocolVersion(McpProtocol.AUTO);
        cleanup.add(client::stop);

        client.start();

        assertEquals(McpProtocol.MODERN_VERSION, client.getNegotiatedVersion());
        assertEquals("echo:auto", text(client.callTool("echo", Map.of("text", "auto"))));
        assertEquals(1, client.listTools(null).tools().size());
        client.stop();
        assertTrue(connector.isStarted());
    }

    /**
     * Explicit {@code 2026-07-28} over the client's own stream discovers and calls; the Tasks
     * client is HTTP only.
     */
    @Test
    void theModernClientSpeaksOverItsOwnStream() throws Exception {
        McpClientAdapter client =
                client(restletServer(), McpProtocol.MODERN_VERSION, Duration.ofSeconds(30));

        client.start();

        assertEquals(McpProtocol.MODERN_VERSION, client.getNegotiatedVersion());
        assertEquals(
                "echo:modern",
                text(
                        client.getModernClient()
                                .tool("echo")
                                .arguments(Map.of("text", "modern"))
                                .call()
                                .block(Duration.ofSeconds(30))));
        assertEquals("echo:again", text(client.callTool("echo", Map.of("text", "again"))));
        assertThrows(UnsupportedOperationException.class, client::getTasksClient);
        client.stop();
        assertFalse(client.isStarted());
    }

    /**
     * A legacy server answering the probe with method not found gets an {@code initialize} offering
     * the legacy revisions, and after the probe, nothing 2026-07-28 introduced or removed.
     */
    @Test
    void aLegacyServerRejectingTheProbeGetsTheLegacyClient() throws Exception {
        FakeServer fake =
                new FakeServer(
                        (message, server) -> {
                            if ("server/discover".equals(message.get("method"))) {
                                server.error(message.get("id"), -32601, "Method not found");
                            } else {
                                server.legacy(message);
                            }
                        });
        McpClientAdapter client =
                client(fake.clientStream(), McpProtocol.AUTO, Duration.ofSeconds(30));

        client.start();

        assertEquals("2025-06-18", client.getNegotiatedVersion());
        assertEquals("echo:legacy", text(client.callTool("echo", Map.of("text", "legacy"))));
        List<Map<String, Object>> received = List.copyOf(fake.received);
        assertEquals("server/discover", received.getFirst().get("method"));
        List<Map<String, Object>> after = received.subList(1, received.size());
        assertEquals(
                List.of("initialize", "notifications/initialized", "tools/call"),
                after.stream().map(message -> message.get("method")).toList());
        Map<?, ?> initialize = (Map<?, ?>) after.getFirst().get("params");
        assertEquals(McpProtocol.VERSION, initialize.get("protocolVersion"));
        assertEquals(Map.of(), initialize.get("capabilities"));
        for (Map<String, Object> message : after) {
            assertFalse(
                    MAPPER.writeValueAsString(message).contains(McpProtocol.MODERN_VERSION),
                    () -> "2026-07-28 sent to a legacy server: " + message);
        }
    }

    /**
     * A legacy server ignoring the probe gets the legacy client once the probe times out, and the
     * late answer it then sends to the probe is dropped without ending the session.
     */
    @Test
    void aLegacyServerIgnoringTheProbeGetsTheLegacyClient() throws Exception {
        List<Object> probes = new CopyOnWriteArrayList<>();
        FakeServer fake =
                new FakeServer(
                        (message, server) -> {
                            if ("server/discover".equals(message.get("method"))) {
                                probes.add(message.get("id"));
                            } else {
                                if ("initialize".equals(message.get("method"))) {
                                    for (Object id : probes) {
                                        server.send(
                                                Map.of(
                                                        "jsonrpc",
                                                        "2.0",
                                                        "id",
                                                        id,
                                                        "result",
                                                        Map.of(
                                                                "supportedVersions",
                                                                List.of("2026-07-28"))));
                                    }
                                }
                                server.legacy(message);
                            }
                        });
        McpClientAdapter client =
                client(fake.clientStream(), McpProtocol.AUTO, Duration.ofSeconds(2));

        client.start();

        assertEquals(1, probes.size());
        assertEquals("2025-06-18", client.getNegotiatedVersion());
        assertEquals("echo:late", text(client.callTool("echo", Map.of("text", "late"))));
        assertEquals("echo:still", text(client.callTool("echo", Map.of("text", "still"))));
        assertEquals(
                1,
                fake.received.stream()
                        .filter(message -> "server/discover".equals(message.get("method")))
                        .count());
    }

    /** A server offering only revisions this client doesn't speak fails the start. */
    @Test
    void noSharedRevisionFailsTheStart() throws Exception {
        FakeServer fake =
                new FakeServer(
                        (message, server) -> {
                            if ("server/discover".equals(message.get("method"))) {
                                server.send(
                                        Map.of(
                                                "jsonrpc",
                                                "2.0",
                                                "id",
                                                message.get("id"),
                                                "error",
                                                Map.of(
                                                        "code",
                                                        McpProtocol.UNSUPPORTED_PROTOCOL_VERSION,
                                                        "message",
                                                        "Unsupported protocol version",
                                                        "data",
                                                        Map.of(
                                                                "supported",
                                                                List.of("2099-01-01")))));
                            } else {
                                server.legacy(message);
                            }
                        });
        McpClientAdapter client =
                client(fake.clientStream(), McpProtocol.AUTO, Duration.ofSeconds(30));

        IllegalStateException failure = assertThrows(IllegalStateException.class, client::start);

        assertTrue(failure.getMessage().contains("No MCP revision supported"), failure::getMessage);
        assertFalse(client.isStarted());
        assertTrue(
                fake.received.stream().noneMatch(m -> "initialize".equals(m.get("method"))),
                "No initialize after a no-shared-revision error");
    }

    /**
     * A server answering {@code -32022} with a legacy revision among those it supports gets the
     * legacy client.
     */
    @Test
    void anUnsupportedVersionOfferingALegacyRevisionGetsTheLegacyClient() throws Exception {
        FakeServer fake =
                new FakeServer(
                        (message, server) -> {
                            if ("server/discover".equals(message.get("method"))) {
                                server.send(
                                        Map.of(
                                                "jsonrpc",
                                                "2.0",
                                                "id",
                                                message.get("id"),
                                                "error",
                                                Map.of(
                                                        "code",
                                                        McpProtocol.UNSUPPORTED_PROTOCOL_VERSION,
                                                        "message",
                                                        "Unsupported protocol version",
                                                        "data",
                                                        Map.of(
                                                                "supported",
                                                                List.of("2025-06-18")))));
                            } else {
                                server.legacy(message);
                            }
                        });
        McpClientAdapter client =
                client(fake.clientStream(), McpProtocol.AUTO, Duration.ofSeconds(30));

        client.start();

        assertEquals("2025-06-18", client.getNegotiatedVersion());
        assertEquals(1, client.listTools(null).tools().size());
    }

    /**
     * The channel queues what it reads while no sink is attached, drops the late response to a
     * request its sink left unanswered, and tells the next sink that the stream ended.
     */
    @Test
    void theChannelQueuesFramesAndDropsAbandonedResponses() throws Exception {
        Pipe requests = Pipe.open();
        Pipe responses = Pipe.open();
        McpStdioChannel channel = new McpStdioChannel(stream(responses, requests));
        cleanup.add(() -> channel.close(Duration.ofSeconds(5)));
        OutputStream peer = Channels.newOutputStream(responses.sink());
        channel.open(1024);
        channel.open(1024);
        assertEquals(1024, channel.maxMessageBytes());
        assertThrows(IllegalStateException.class, () -> channel.open(2048));

        BlockingQueue<String> first = new LinkedBlockingQueue<>();
        McpStdioChannel.Sink probe = sink(first, new LinkedBlockingQueue<>());
        channel.attach(probe);
        assertThrows(IllegalStateException.class, () -> channel.attach(probe));
        channel.write(
                probe,
                "{\"jsonrpc\":\"2.0\",\"id\":7,\"method\":\"server/discover\"}"
                        .getBytes(StandardCharsets.UTF_8));
        channel.write(
                probe,
                "{\"jsonrpc\":\"2.0\",\"id\":8,\"method\":\"ping\"}"
                        .getBytes(StandardCharsets.UTF_8));
        channel.write(
                probe,
                "{\"jsonrpc\":\"2.0\",\"method\":\"notifications/cancelled\",\"params\":{\"requestId\":8}}"
                        .getBytes(StandardCharsets.UTF_8));
        channel.detach(probe);
        assertThrows(
                IOException.class,
                () -> channel.write(probe, "{}".getBytes(StandardCharsets.UTF_8)));

        peer.write(
                ("{\"jsonrpc\":\"2.0\",\"id\":7,\"result\":{}}\n"
                                + "{\"jsonrpc\":\"2.0\",\"id\":8,\"result\":{}}\n"
                                + "{\"jsonrpc\":\"2.0\",\"method\":\"notifications/message\"}\n")
                        .getBytes(StandardCharsets.UTF_8));
        peer.flush();
        peer.close();
        TimeUnit.MILLISECONDS.sleep(200);

        BlockingQueue<String> second = new LinkedBlockingQueue<>();
        BlockingQueue<IOException> ends = new LinkedBlockingQueue<>();
        channel.attach(sink(second, ends));
        assertEquals(
                "{\"jsonrpc\":\"2.0\",\"method\":\"notifications/message\"}",
                second.poll(5, TimeUnit.SECONDS));
        assertTrue(ends.poll(5, TimeUnit.SECONDS) instanceof java.io.EOFException);
        assertTrue(second.isEmpty());
        assertTrue(first.isEmpty());
    }

    /**
     * A server that stops reading its input doesn't keep the client from stopping: a write stuck on
     * the full pipe doesn't hold the detach, and stopping the stream ends it.
     */
    @Test
    void aServerThatStopsReadingDoesNotHoldTheStop() throws Exception {
        java.util.concurrent.CountDownLatch stalled = new java.util.concurrent.CountDownLatch(1);
        FakeServer fake =
                new FakeServer(
                        (message, server) -> {
                            if ("server/discover".equals(message.get("method"))) {
                                server.error(message.get("id"), -32601, "Method not found");
                            } else if ("notifications/initialized".equals(message.get("method"))) {
                                try {
                                    stalled.await(); // Reads nothing more.
                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                }
                            } else {
                                server.legacy(message);
                            }
                        });
        cleanup.add(stalled::countDown);
        McpClientAdapter client = new McpClientAdapter(fake.clientStream());
        client.setProtocolVersion(McpProtocol.AUTO);
        client.setRequestTimeout(Duration.ofSeconds(2));
        client.setMaxMessageBytes(32 * 1024 * 1024);
        client.start();
        assertEquals("2025-06-18", client.getNegotiatedVersion());

        // Larger than the pipe holds: the write waits for a reader that never comes.
        String large = "x".repeat(16 * 1024 * 1024);
        Thread call =
                Thread.ofVirtual()
                        .start(
                                () -> {
                                    try {
                                        client.callTool("echo", Map.of("text", large));
                                    } catch (RuntimeException _) {
                                        // Timed out, or ended by the stop.
                                    }
                                });
        TimeUnit.MILLISECONDS.sleep(500);

        org.junit.jupiter.api.Assertions.assertTimeoutPreemptively(
                Duration.ofSeconds(20),
                () -> {
                    try {
                        client.stop();
                    } catch (RuntimeException _) {
                        // A client may fail to close in time; the stream is stopped anyway.
                    }
                });
        assertFalse(client.isStarted());
        assertTrue(call.join(Duration.ofSeconds(10)), "the stuck call did not end");
    }

    private static McpStdioChannel.Sink sink(
            BlockingQueue<String> frames, BlockingQueue<IOException> ends) {
        return new McpStdioChannel.Sink() {
            @Override
            public void receive(byte[] frame) {
                frames.add(new String(frame, StandardCharsets.UTF_8));
            }

            @Override
            public void end(IOException failure) {
                ends.add(failure);
            }
        };
    }

    /** A scripted server at the other end of a pair of pipes, one JSON-RPC message per line. */
    private final class FakeServer {
        private final List<Map<String, Object>> received = new CopyOnWriteArrayList<>();
        private final OutputStream out;
        private final StreamTransport clientStream;

        FakeServer(BiConsumer<Map<String, Object>, FakeServer> script) throws IOException {
            Pipe requests = Pipe.open();
            Pipe responses = Pipe.open();
            clientStream = stream(responses, requests);
            out = Channels.newOutputStream(responses.sink());
            BufferedReader in =
                    new BufferedReader(
                            new InputStreamReader(
                                    Channels.newInputStream(requests.source()),
                                    StandardCharsets.UTF_8));
            Thread reader =
                    Thread.ofVirtual()
                            .start(
                                    () -> {
                                        try {
                                            for (String line = in.readLine();
                                                    line != null;
                                                    line = in.readLine()) {
                                                Map<String, Object> message =
                                                        MAPPER.readValue(line, MAP);
                                                received.add(message);
                                                script.accept(message, this);
                                            }
                                        } catch (IOException | RuntimeException _) {
                                            // The client closed its end.
                                        }
                                    });
            cleanup.add(
                    () -> {
                        requests.source().close();
                        responses.sink().close();
                        reader.join(Duration.ofSeconds(5));
                    });
        }

        StreamTransport clientStream() {
            return clientStream;
        }

        synchronized void send(Map<String, Object> message) {
            try {
                out.write(
                        (MAPPER.writeValueAsString(message) + "\n")
                                .getBytes(StandardCharsets.UTF_8));
                out.flush();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        void error(Object id, int code, String text) {
            send(
                    Map.of(
                            "jsonrpc",
                            "2.0",
                            "id",
                            id,
                            "error",
                            Map.of("code", code, "message", text)));
        }

        private void result(Object id, Map<String, Object> result) {
            send(Map.of("jsonrpc", "2.0", "id", id, "result", result));
        }

        /** Answers as a legacy server negotiating 2025-06-18, with an echo tool. */
        void legacy(Map<String, Object> message) {
            Object id = message.get("id");
            Object method = message.get("method");
            if (id == null || method == null) {
                return; // A notification, or a response.
            }
            Map<?, ?> params = message.get("params") instanceof Map<?, ?> map ? map : Map.of();
            switch (String.valueOf(method)) {
                case "initialize" ->
                        result(
                                id,
                                Map.of(
                                        "protocolVersion",
                                        "2025-06-18",
                                        "capabilities",
                                        Map.of("tools", Map.of()),
                                        "serverInfo",
                                        Map.of("name", "legacy", "version", "1")));
                case "tools/list" ->
                        result(
                                id,
                                Map.of(
                                        "tools",
                                        List.of(
                                                Map.of(
                                                        "name",
                                                        "echo",
                                                        "inputSchema",
                                                        Map.of("type", "object")))));
                case "tools/call" -> {
                    Map<?, ?> arguments = (Map<?, ?>) params.get("arguments");
                    Map<String, Object> content = new LinkedHashMap<>();
                    content.put("type", "text");
                    content.put("text", "echo:" + arguments.get("text"));
                    result(id, Map.of("content", List.of(content)));
                }
                default -> error(id, -32601, "Method not found");
            }
        }
    }
}
