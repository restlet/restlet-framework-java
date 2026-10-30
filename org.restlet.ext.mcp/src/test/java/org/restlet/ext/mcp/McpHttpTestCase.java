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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.modelcontextprotocol.spec.McpSchema;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.representation.StringRepresentation;
import reactor.core.publisher.Mono;

class McpHttpTestCase {
    private McpServerAdapter adapter;
    private Server server;
    private Client connector;
    private McpClientAdapter client;

    private String start(String mode, Restlet handler) throws Exception {
        Context sc = new Context();
        sc.getParameters().set("http.transport.protocol", mode);
        server = new Server(sc, Protocol.HTTP, 0, handler);
        server.start();
        Context cc = new Context();
        cc.getParameters().set("httpClientTransportMode", mode);
        cc.getParameters().set("followRedirects", "false");
        connector = new Client(cc, Protocol.HTTP);
        connector.start();
        return "http://localhost:" + server.getActualPort() + "/mcp";
    }

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

    private void interoperability(String mode) throws Exception {
        adapter = new McpServerAdapter();
        McpServerAdapterTestCase.addEcho(adapter);
        adapter.start();
        String target = start(mode, adapter);
        client = new McpClientAdapter(connector, target);
        client.start();
        assertEquals(
                List.of("echo"),
                client.listTools(null).tools().stream().map(McpSchema.Tool::name).toList());
        assertEquals(
                "echo:network",
                ((McpSchema.TextContent)
                                client.callTool("echo", Map.of("text", "network"))
                                        .content()
                                        .getFirst())
                        .text());
        assertNotNull(client.getSdkClient().ping().block(Duration.ofSeconds(5)));
        client.stop();
        assertTrue(connector.isStarted());
        assertTrue(server.isStarted());
        assertFalse(client.isStarted());
        assertThrows(IllegalStateException.class, client::start);
    }

    @Test
    void nativeSdkThroughSharedHttp1() throws Exception {
        interoperability("HTTP1_1");
    }

    @Test
    void nativeSdkThroughSharedHttp2() throws Exception {
        interoperability("HTTP2");
    }

    @Test
    void closingHttpTransportCancelsRemoteCallbackWithoutStoppingBorrowedConnector()
            throws Exception {
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch cancelled = new CountDownLatch(1);
        adapter = new McpServerAdapter();
        adapter.getInterceptors()
                .add(
                        (message, context, chain) -> {
                            entered.countDown();
                            return Mono.<McpSchema.JSONRPCResponse>never()
                                    .doOnCancel(cancelled::countDown);
                        });
        adapter.start();
        String target = start("HTTP2", adapter);
        var transport = new RestletMcpClientTransport(connector, target);
        try {
            transport.connect(message -> message).block();
            var exchange =
                    CompletableFuture.runAsync(
                            () ->
                                    transport
                                            .sendMessage(
                                                    new McpSchema.JSONRPCRequest(
                                                            "2.0", "ping", 1, Map.of()))
                                            .block());
            assertTrue(entered.await(30, TimeUnit.SECONDS));
            transport.closeGracefully().block(Duration.ofSeconds(30));
            assertThrows(
                    java.util.concurrent.ExecutionException.class,
                    () -> exchange.get(30, TimeUnit.SECONDS));
            assertTrue(cancelled.await(30, TimeUnit.SECONDS));
            assertTrue(connector.isStarted());
        } finally {
            transport.closeGracefully().block(Duration.ofSeconds(30));
        }
    }

    @Test
    void readsBoundedOneShotSseWithoutSessionOrReconnect() throws Exception {
        adapter = new McpServerAdapter();
        McpServerAdapterTestCase.addEcho(adapter);
        adapter.start();
        AtomicInteger posts = new AtomicInteger();
        String target =
                start(
                        "HTTP1_1",
                        new Restlet() {
                            @Override
                            public void handle(Request request, org.restlet.Response response) {
                                posts.incrementAndGet();
                                adapter.handle(request, response);
                                if (response.getEntity() != null
                                        && response.getStatus().isSuccess()) {
                                    try {
                                        String json = response.getEntity().getText();
                                        response.setEntity(
                                                ": keepalive\n\nevent: message\ndata: "
                                                        + json
                                                        + "\n\n",
                                                MediaType.TEXT_EVENT_STREAM);
                                    } catch (java.io.IOException e) {
                                        throw new java.io.UncheckedIOException(e);
                                    }
                                }
                            }
                        });
        client = new McpClientAdapter(connector, target);
        client.start();
        assertEquals(1, client.listTools(null).tools().size());
        assertEquals(3, posts.get());
    }

    @Test
    void rejectsSessionResponseAndDoesNotRetryPost() throws Exception {
        AtomicInteger posts = new AtomicInteger();
        String target =
                start(
                        "HTTP1_1",
                        new Restlet() {
                            @Override
                            public void handle(Request request, org.restlet.Response response) {
                                posts.incrementAndGet();
                                response.getHeaders()
                                        .set("Mcp-Session-Id", "stateful-not-supported");
                                response.setEntity("{}", MediaType.APPLICATION_JSON);
                            }
                        });
        client = new McpClientAdapter(connector, target);
        assertThrows(RuntimeException.class, client::start);
        assertEquals(1, posts.get());
        assertTrue(connector.isStarted());
    }

    @Test
    void clientRejectsRedirectAndDoesNotReplayPost() throws Exception {
        AtomicInteger posts = new AtomicInteger();
        String target =
                start(
                        "HTTP1_1",
                        new Restlet() {
                            @Override
                            public void handle(Request request, org.restlet.Response response) {
                                posts.incrementAndGet();
                                response.setStatus(Status.REDIRECTION_TEMPORARY);
                                response.setLocationRef(request.getResourceRef());
                            }
                        });
        client = new McpClientAdapter(connector, target);
        assertThrows(RuntimeException.class, client::start);
        assertEquals(1, posts.get());
    }

    @Test
    void clientRequiresExplicitBorrowedConnectorPolicy() throws Exception {
        Context context = new Context();
        connector = new Client(context, Protocol.HTTP);
        connector.start();
        var transport = new RestletMcpClientTransport(connector, "http://localhost/mcp");
        try {
            assertThrows(
                    RuntimeException.class, () -> transport.connect(message -> message).block());
        } finally {
            transport.closeGracefully().block();
        }
        assertTrue(connector.isStarted());
    }

    @Test
    void oneShotSseBudgetIncludesCommentsAndTrailingOpenStreamIsNotRead() throws Exception {
        String target =
                start(
                        "HTTP1_1",
                        new Restlet() {
                            @Override
                            public void handle(Request request, org.restlet.Response response) {
                                response.setEntity(
                                        new StringRepresentation(
                                                ":" + "x".repeat(4096) + "\n\n",
                                                MediaType.TEXT_EVENT_STREAM));
                            }
                        });
        var transport =
                new RestletMcpClientTransport(
                        connector, target, 512, 1, Duration.ofSeconds(3), request -> {});
        try {
            transport.connect(message -> message).block();
            assertThrows(
                    RuntimeException.class,
                    () ->
                            transport
                                    .sendMessage(
                                            new McpSchema.JSONRPCRequest(
                                                    "2.0", "ping", 1, Map.of()))
                                    .block(Duration.ofSeconds(5)));
        } finally {
            transport.closeGracefully().block();
        }
    }

    @Test
    void mismatchedResponseIdFailsRatherThanLeavingNativePendingUntilTimeout() throws Exception {
        String target =
                start(
                        "HTTP1_1",
                        new Restlet() {
                            @Override
                            public void handle(Request request, org.restlet.Response response) {
                                response.setEntity(
                                        "{\"jsonrpc\":\"2.0\",\"id\":\"wrong\",\"result\":{}}",
                                        MediaType.APPLICATION_JSON);
                            }
                        });
        var transport = new RestletMcpClientTransport(connector, target);
        try {
            AtomicInteger delivered = new AtomicInteger();
            transport
                    .connect(message -> message.doOnNext(ignored -> delivered.incrementAndGet()))
                    .block();
            assertThrows(
                    RuntimeException.class,
                    () ->
                            transport
                                    .sendMessage(
                                            new McpSchema.JSONRPCRequest(
                                                    "2.0", "ping", 1, Map.of()))
                                    .block(Duration.ofSeconds(5)));
            assertEquals(0, delivered.get());
        } finally {
            transport.closeGracefully().block();
        }
    }
}
