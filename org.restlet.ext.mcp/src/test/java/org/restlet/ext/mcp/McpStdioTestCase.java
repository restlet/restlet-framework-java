/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.mcp;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.modelcontextprotocol.spec.McpSchema;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.Pipe;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.Server;
import org.restlet.data.Protocol;
import org.restlet.engine.Engine;
import org.restlet.ext.mcp.internal.McpMessages;
import org.restlet.ext.mcp.internal.legacy.StdioMcpClientTransport;

class McpStdioTestCase {
    @Test
    void registeredHelpersShareNativeDispatcherAndDoNotOwnBorrowedConnector() throws Exception {
        Pipe requests = Pipe.open();
        Pipe responses = Pipe.open();
        org.restlet.util.StreamTransport serverStream =
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
        org.restlet.util.StreamTransport clientStream =
                new org.restlet.util.StreamTransport(
                        Channels.newInputStream(responses.source()),
                        Channels.newOutputStream(requests.sink()),
                        () -> {
                            try {
                                responses.source().close();
                                requests.sink().close();
                            } catch (IOException e) {
                                throw new java.io.UncheckedIOException(e);
                            }
                        });
        Engine engine = Engine.getInstance();
        StdioServerHelper serverPrototype = new StdioServerHelper(null);
        StdioClientHelper clientPrototype = new StdioClientHelper(null);
        engine.getRegisteredServers().add(serverPrototype);
        engine.getRegisteredClients().add(clientPrototype);
        McpServerAdapter adapter = new McpServerAdapter();
        McpServerAdapterTestCase.addEcho(adapter);
        adapter.start();
        Context sc = new Context();
        sc.getAttributes().put(McpProtocol.STREAM_TRANSPORT_ATTRIBUTE, serverStream);
        Server server =
                new Server(
                        sc,
                        List.of(Protocol.STDIO),
                        null,
                        -1,
                        adapter,
                        StdioServerHelper.class.getCanonicalName());
        Context cc = new Context();
        cc.getAttributes().put(McpProtocol.STREAM_TRANSPORT_ATTRIBUTE, clientStream);
        Client connector =
                new Client(cc, List.of(Protocol.STDIO), StdioClientHelper.class.getCanonicalName());
        McpClientAdapter client = new McpClientAdapter(connector, "stdio://localhost/mcp");
        try {
            server.start();
            connector.start();
            client.start();
            assertEquals(1, client.listTools(null).tools().size());
            var calls =
                    java.util.stream.IntStream.range(0, 12)
                            .mapToObj(
                                    i ->
                                            CompletableFuture.supplyAsync(
                                                    () ->
                                                            client.callTool(
                                                                    "echo",
                                                                    Map.of("text", "pipe-" + i))))
                            .toList();
            for (int i = 0; i < calls.size(); i++) {
                assertEquals(
                        "echo:pipe-" + i,
                        ((io.modelcontextprotocol.modern.McpSchema.TextContent)
                                        calls.get(i).get(5, TimeUnit.SECONDS).content().getFirst())
                                .text());
            }
            client.stop();
            assertTrue(connector.isStarted());
            assertTrue(server.isStarted());
        } finally {
            try {
                client.stop();
            } finally {
                try {
                    connector.stop();
                } finally {
                    try {
                        server.stop();
                    } finally {
                        adapter.stop();
                        engine.getRegisteredServers().remove(serverPrototype);
                        engine.getRegisteredClients().remove(clientPrototype);
                    }
                }
            }
        }
    }

    @Test
    void ownedSubprocessUsesOnlyNewlineProtocolOnStdout() throws Exception {
        String java = Path.of(System.getProperty("java.home"), "bin", "java").toString();
        org.restlet.util.StreamTransport stream =
                new org.restlet.util.StreamTransport(
                        List.of(
                                java,
                                "-cp",
                                System.getProperty(
                                        "surefire.test.class.path",
                                        System.getProperty("java.class.path")),
                                FixtureMain.class.getName()));
        McpClientAdapter client = new McpClientAdapter(stream);
        try {
            client.start();
            assertEquals(1, client.listTools(null).tools().size());
            assertEquals(
                    "echo:process",
                    ((io.modelcontextprotocol.modern.McpSchema.TextContent)
                                    client.callTool("echo", Map.of("text", "process"))
                                            .content()
                                            .getFirst())
                            .text());
        } catch (RuntimeException e) {
            throw new AssertionError(
                    "MCP subprocess failed; stderr:\n"
                            + new String(stream.getStderrTail(), StandardCharsets.UTF_8),
                    e);
        } finally {
            client.stop();
        }
        assertFalse(client.isStarted());
    }

    @Test
    void boundedFramesAndBorrowedStreamOwnership() throws Exception {
        AtomicCloseInput input = new AtomicCloseInput("1234\n".getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (org.restlet.util.StreamTransport stream =
                new org.restlet.util.StreamTransport(input, output, () -> {})) {
            stream.setMaxOperationBytes(5);
            stream.start();
            assertArrayEquals(
                    "1234".getBytes(StandardCharsets.UTF_8), McpMessages.readFrame(stream, 4));
            assertNull(McpMessages.readFrame(stream, 4));
            McpMessages.writeFrame(stream, "1234".getBytes(StandardCharsets.UTF_8));
            assertEquals("1234\n", output.toString(StandardCharsets.UTF_8));
            assertThrows(
                    IOException.class,
                    () -> McpMessages.writeFrame(stream, "12345".getBytes(StandardCharsets.UTF_8)));
        }
        assertFalse(input.closed);
        output.write(1);
    }

    @Test
    void rejectsOversizedOrTruncatedFramesWithoutPrefetch() throws Exception {
        for (String content : List.of("12345\n", "1234")) {
            try (org.restlet.util.StreamTransport stream =
                    new org.restlet.util.StreamTransport(
                            new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)),
                            new ByteArrayOutputStream(),
                            () -> {})) {
                stream.start();
                assertThrows(IOException.class, () -> McpMessages.readFrame(stream, 4));
            }
        }
    }

    private static org.restlet.util.StreamTransport stream(Pipe in, Pipe out) {
        return new org.restlet.util.StreamTransport(
                Channels.newInputStream(in.source()),
                Channels.newOutputStream(out.sink()),
                () -> {
                    try {
                        in.source().close();
                        out.sink().close();
                    } catch (IOException e) {
                        throw new java.io.UncheckedIOException(e);
                    }
                });
    }

    @Test
    void borrowedHelperTransportTakesTheClientLimits() throws Exception {
        Context context = new Context();
        context.getAttributes()
                .put(McpProtocol.STREAM_TRANSPORT_ATTRIBUTE, stream(Pipe.open(), Pipe.open()));
        StdioClientHelper helper = new StdioClientHelper(new Client(context, List.of()));
        helper.start();
        try {
            // Too small for the initialize request: the handshake fails, the transport stays bound.
            assertThrows(RuntimeException.class, () -> helper.bind(16, 1, Duration.ofSeconds(3)));
            assertThrows(
                    IllegalStateException.class, () -> helper.bind(1024, 1, Duration.ofSeconds(3)));
        } finally {
            helper.stop();
        }
    }

    @Test
    void unansweredCallFreesItsSlotAfterTheTimeoutAndItsLateReplyIsDropped() throws Exception {
        Pipe requests = Pipe.open();
        Pipe responses = Pipe.open();
        var transport =
                new StdioMcpClientTransport(
                        stream(responses, requests), 1024, 1, Duration.ofMillis(200));
        List<McpSchema.JSONRPCMessage> delivered = new CopyOnWriteArrayList<>();
        var peer = Channels.newOutputStream(responses.sink());
        try {
            transport.connect(message -> message.doOnNext(delivered::add)).block();
            transport
                    .sendMessage(new McpSchema.JSONRPCRequest("2.0", "ping", 1, Map.of()))
                    .block(Duration.ofSeconds(5));
            assertThrows(
                    RuntimeException.class,
                    () ->
                            transport
                                    .sendMessage(
                                            new McpSchema.JSONRPCRequest(
                                                    "2.0", "ping", 2, Map.of()))
                                    .block(Duration.ofSeconds(5)));
            TimeUnit.MILLISECONDS.sleep(300);
            transport
                    .sendMessage(new McpSchema.JSONRPCRequest("2.0", "ping", 3, Map.of()))
                    .block(Duration.ofSeconds(5));
            peer.write(
                    ("{\"jsonrpc\":\"2.0\",\"id\":1,\"result\":{}}\n"
                                    + "{\"jsonrpc\":\"2.0\",\"id\":3,\"result\":{}}\n")
                            .getBytes(StandardCharsets.UTF_8));
            peer.flush();
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(3);
            while (delivered.isEmpty() && System.nanoTime() < deadline) {
                Thread.onSpinWait();
            }
            assertEquals(1, delivered.size());
            assertEquals(3, ((McpSchema.JSONRPCResponse) delivered.getFirst()).id());
        } finally {
            transport.closeGracefully().block(Duration.ofSeconds(5));
        }
    }

    @Test
    void helpersRequireExplicitStreams() {
        assertThrows(
                IllegalStateException.class,
                () -> new StdioClientHelper(new Client(new Context(), List.of())).start());
    }

    private static final class AtomicCloseInput extends ByteArrayInputStream {
        private boolean closed;

        private AtomicCloseInput(byte[] data) {
            super(data);
        }

        @Override
        public void close() {
            closed = true;
        }
    }

    /** Real child entry point, never writes diagnostics to stdout. */
    public static final class FixtureMain {
        public static void main(String[] args) throws Exception {
            McpServerAdapter adapter = new McpServerAdapter();
            McpServerAdapterTestCase.addEcho(adapter);
            adapter.start();
            org.restlet.util.StreamTransport stream =
                    new org.restlet.util.StreamTransport(
                            System.in,
                            System.out,
                            () -> {
                                try {
                                    System.in.close();
                                } catch (IOException e) {
                                    throw new java.io.UncheckedIOException(e);
                                }
                            });
            Context context = new Context();
            context.getAttributes().put(McpProtocol.STREAM_TRANSPORT_ATTRIBUTE, stream);
            Engine.getInstance().getRegisteredServers().add(new StdioServerHelper(null));
            Server connector =
                    new Server(
                            context,
                            List.of(Protocol.STDIO),
                            null,
                            -1,
                            adapter,
                            StdioServerHelper.class.getCanonicalName());
            try {
                connector.start();
                var helper =
                        (StdioServerHelper)
                                context.getAttributes().get("org.restlet.engine.helper");
                while (helper.getFailure() == null) {
                    Thread.sleep(20);
                }
            } finally {
                connector.stop();
                adapter.stop();
            }
        }
    }
}
