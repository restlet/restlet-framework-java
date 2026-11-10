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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.modelcontextprotocol.json.McpJsonDefaults;
import io.modelcontextprotocol.modern.JsonRpc;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscription;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.ext.mcp.internal.McpMessages;
import org.restlet.ext.mcp.internal.McpModernHttpTransport;
import org.restlet.representation.ByteArrayRepresentation;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.util.retry.Retry;

/**
 * {@link McpModernHttpTransport} against a server sending crafted replies: correlation by exact id,
 * strict UTF-8, per-message byte limits and demand.
 */
class McpModernHttpTransportTestCase {
    private static final Duration WAIT = Duration.ofSeconds(10);
    private static final String NOTIFICATION =
            "{\"jsonrpc\":\"2.0\",\"method\":\"notifications/progress\",\"params\":{}}";

    private volatile byte[] reply = new byte[0];
    private volatile MediaType type = MediaType.TEXT_EVENT_STREAM;
    private Server server;
    private Client connector;
    private McpModernHttpTransport transport;

    @AfterEach
    void cleanup() throws Exception {
        try {
            if (transport != null) {
                transport.close();
            }
        } finally {
            try {
                if (connector != null) {
                    connector.stop();
                }
            } finally {
                if (server != null) {
                    server.stop();
                }
            }
        }
    }

    @Test
    void comparesIdsExactly() {
        assertTrue(McpModernHttpTransport.sameId(1L, 1.0));
        assertTrue(McpModernHttpTransport.sameId("a", "a"));
        assertFalse(McpModernHttpTransport.sameId(1L, 1.5));
        assertFalse(McpModernHttpTransport.sameId(9007199254740993L, 9007199254740992.0));
        assertFalse(McpModernHttpTransport.sameId(1L, "1"));
    }

    /** Other requests' responses are dropped; the stream ends with its own. */
    @Test
    void streamsUntilItsOwnResponse() throws Exception {
        start(1024, 1);
        reply = events(NOTIFICATION, response(99), response(1), NOTIFICATION);

        List<JsonRpc.JSONRPCMessage> messages = send(1).collectList().block(WAIT);

        assertEquals(2, messages.size());
        assertInstanceOf(JsonRpc.JSONRPCNotification.class, messages.get(0));
        assertEquals(1, ((Number) ((JsonRpc.JSONRPCResponse) messages.get(1)).id()).intValue());
    }

    @Test
    void aStreamWithoutItsResponseFails() throws Exception {
        start(1024, 1);
        reply =
                events(
                        NOTIFICATION,
                        response(99),
                        "{\"jsonrpc\":\"2.0\",\"id\":\"1\",\"result\":{}}");

        Flux<JsonRpc.JSONRPCMessage> sent = send(1);
        IOException failure = assertThrows(IOException.class, () -> block(sent));
        assertTrue(failure.getMessage().contains("without a response"), failure.getMessage());
    }

    @Test
    void aJsonReplyMustBeItsResponse() throws Exception {
        start(1024, 1);
        type = MediaType.APPLICATION_JSON;
        for (String body : List.of(NOTIFICATION, response(99))) {
            reply = body.getBytes(StandardCharsets.UTF_8);
            Flux<JsonRpc.JSONRPCMessage> sent = send(1);
            assertThrows(IOException.class, () -> block(sent));
        }
        reply = response(1).getBytes(StandardCharsets.UTF_8);
        assertEquals(1, send(1).collectList().block(WAIT).size());
    }

    @Test
    void aStreamWithMalformedUtf8Fails() throws Exception {
        start(1024, 1);
        ByteArrayOutputStream body = new ByteArrayOutputStream();
        body.writeBytes(
                "data: {\"jsonrpc\":\"2.0\",\"id\":1,\"result\":{\"text\":\""
                        .getBytes(StandardCharsets.UTF_8));
        body.write(0xFF);
        body.writeBytes("\"}}\n\n".getBytes(StandardCharsets.UTF_8));
        reply = body.toByteArray();

        Flux<JsonRpc.JSONRPCMessage> sent = send(1);
        assertThrows(CharacterCodingException.class, () -> block(sent));
    }

    /** The limit is on each message's bytes: framing doesn't count, non-ASCII characters do. */
    @Test
    void limitsEachMessageInBytes() throws Exception {
        // Padded past the size of the request sent.
        String exact = response(1).replace("{}", "{\"pad\":\"" + "x".repeat(200) + "\"}");
        start(exact.getBytes(StandardCharsets.UTF_8).length, 1);
        reply =
                (": a comment\nevent: message\nid: 7\ndata: " + exact + "\n\n")
                        .getBytes(StandardCharsets.UTF_8);
        assertEquals(1, send(1).collectList().block(WAIT).size());

        // As many characters, but "é" takes 2 bytes.
        String wide = exact.replace("xx\"", "xé\"");
        transport.close();
        start(exact.length(), 1);
        reply = events(wide);
        Flux<JsonRpc.JSONRPCMessage> sent = send(1);
        assertThrows(McpMessages.MessageLimitException.class, () -> block(sent));
    }

    /**
     * A subscriber demanding nothing holds back the stream, which it can still cancel: the worker
     * waiting for demand ends, and frees the only call slot.
     */
    @Test
    void aSlowSubscriberHoldsBackTheStream() throws Exception {
        start(1024, 1);
        String[] many = new String[5000];
        java.util.Arrays.fill(many, NOTIFICATION);
        reply = events(many);
        CountDownLatch first = new CountDownLatch(1);
        BaseSubscriber<JsonRpc.JSONRPCMessage> slow =
                new BaseSubscriber<>() {
                    @Override
                    protected void hookOnSubscribe(Subscription subscription) {
                        request(1);
                    }

                    @Override
                    protected void hookOnNext(JsonRpc.JSONRPCMessage message) {
                        first.countDown();
                    }
                };
        send(1).subscribe(slow);
        assertTrue(first.await(WAIT.toSeconds(), TimeUnit.SECONDS));

        // Read ahead without demand, the stream would have ended and freed its slot by now.
        Thread.sleep(500);
        reply = events(response(2));
        Flux<JsonRpc.JSONRPCMessage> refused = send(2);
        IOException busy = assertThrows(IOException.class, () -> block(refused));
        assertTrue(busy.getMessage().contains("call limit"), busy.getMessage());

        slow.cancel();
        assertEquals(
                1,
                send(2).retryWhen(Retry.fixedDelay(50, Duration.ofMillis(100)))
                        .collectList()
                        .block(WAIT)
                        .size());
    }

    private void start(int maxMessageBytes, int maxCalls) throws Exception {
        if (server == null) {
            server =
                    new Server(
                            new Context(),
                            Protocol.HTTP,
                            0,
                            new Restlet() {
                                @Override
                                public void handle(Request request, Response response) {
                                    response.setEntity(new ByteArrayRepresentation(reply, type));
                                }
                            });
            server.start();
            Context cc = new Context();
            cc.getParameters().set("followRedirects", "false");
            connector = new Client(cc, Protocol.HTTP);
            connector.start();
        }
        transport =
                new McpModernHttpTransport(
                        connector,
                        "http://localhost:" + server.getActualPort() + "/mcp",
                        McpJsonDefaults.getMapper(),
                        maxMessageBytes,
                        maxCalls,
                        WAIT,
                        ignored -> {});
    }

    private Flux<JsonRpc.JSONRPCMessage> send(long id) {
        return transport.send(
                new JsonRpc.JSONRPCRequest("tools/call", id, Map.of()), null, Map.of(), false);
    }

    /** Blocks on a stream, rethrowing the checked exception it failed with. */
    private static void block(Flux<JsonRpc.JSONRPCMessage> sent) throws Exception {
        try {
            sent.blockLast(WAIT);
        } catch (RuntimeException e) {
            Throwable cause = reactor.core.Exceptions.unwrap(e);
            if (cause instanceof Exception checked) {
                throw checked;
            }
            throw e;
        }
    }

    private static String response(int id) {
        return "{\"jsonrpc\":\"2.0\",\"id\":" + id + ",\"result\":{}}";
    }

    private static byte[] events(String... messages) {
        StringBuilder body = new StringBuilder();
        for (String message : messages) {
            body.append("data: ").append(message).append("\n\n");
        }
        return body.toString().getBytes(StandardCharsets.UTF_8);
    }
}
