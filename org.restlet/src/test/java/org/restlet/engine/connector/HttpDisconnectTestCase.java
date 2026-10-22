/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.connector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.engine.Engine;
import org.restlet.representation.StringRepresentation;

class HttpDisconnectTestCase {

    private Client client;
    private Server server;

    @BeforeEach
    void setUp() {
        Engine.clearThreadLocalVariables();
        Engine.register(false);
        Engine.getInstance().getRegisteredClients().add(new HttpClientHelper(null));
        Engine.getInstance().getRegisteredServers().add(new HttpServerHelper(null));
    }

    @AfterEach
    void tearDown() throws Exception {
        if (client != null) {
            client.stop();
        }
        if (server != null) {
            server.stop();
        }
        Engine.clearThreadLocalVariables();
    }

    private void start(String mode, Restlet handler) throws Exception {
        Context serverContext = new Context();
        serverContext.getParameters().add("http.transport.protocol", mode);
        server = new Server(serverContext, Protocol.HTTP, 0, handler);
        server.start();
        Context clientContext = new Context();
        clientContext.getParameters().add("httpClientTransportMode", mode);
        client = new Client(clientContext, Protocol.HTTP);
        client.start();
    }

    @ParameterizedTest
    @ValueSource(strings = {"HTTP1_1", "HTTP2"})
    void notifiesIdleServerAfterEntireRequestBodyWasConsumed(String mode) throws Exception {
        CountDownLatch idle = new CountDownLatch(1);
        CountDownLatch finished = new CountDownLatch(1);
        AtomicReference<Throwable> cause = new AtomicReference<>();
        AtomicReference<Throwable> handlerFailure = new AtomicReference<>();
        AtomicInteger notifications = new AtomicInteger();
        start(
                mode,
                new Restlet() {
                    @Override
                    public void handle(Request request, Response response) {
                        if (request.getResourceRef().getPath().equals("/healthy")) {
                            response.setEntity("healthy", MediaType.TEXT_PLAIN);
                            return;
                        }
                        try {
                            assertEquals("body-consumed", request.getEntityAsText());
                            var disconnected =
                                    request.getDisconnectSignal()
                                            .thenAccept(
                                                    error -> {
                                                        notifications.incrementAndGet();
                                                        cause.set(error);
                                                    });
                            idle.countDown();
                            disconnected.toCompletableFuture().get(5, TimeUnit.SECONDS);
                        } catch (Exception | AssertionError e) {
                            handlerFailure.set(e);
                        } finally {
                            finished.countDown();
                        }
                    }
                });
        Request request =
                new Request(
                        Method.POST,
                        "http://localhost:" + server.getActualPort() + "/idle",
                        new StringRepresentation("body-consumed", MediaType.TEXT_PLAIN));
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var response = executor.submit(() -> client.handle(request));
            assertTrue(idle.await(5, TimeUnit.SECONDS));
            assertTrue(request.abort());
            assertNotNull(
                    request.getDisconnectSignal().toCompletableFuture().get(5, TimeUnit.SECONDS));
            assertTrue(finished.await(5, TimeUnit.SECONDS));
            if (handlerFailure.get() != null) {
                throw new AssertionError(
                        "Idle server did not observe disconnect", handlerFailure.get());
            }
            assertNotNull(cause.get());
            assertEquals(1, notifications.get());
            assertTrue(response.get(5, TimeUnit.SECONDS).getStatus().isConnectorError());
        }
        Response healthy =
                client.handle(
                        new Request(
                                Method.GET,
                                "http://localhost:" + server.getActualPort() + "/healthy"));
        assertEquals("healthy", healthy.getEntityAsText());
        assertFalse(healthy.getRequest().getDisconnectSignal().toCompletableFuture().isDone());
    }

    @ParameterizedTest
    @ValueSource(strings = {"HTTP1_1", "HTTP2"})
    void remembersCancellationBeforeConnectorRegistration(String mode) throws Exception {
        AtomicInteger received = new AtomicInteger();
        start(
                mode,
                new Restlet() {
                    @Override
                    public void handle(Request request, Response response) {
                        received.incrementAndGet();
                        response.setEntity("unexpected", MediaType.TEXT_PLAIN);
                    }
                });
        Request request = new Request(Method.GET, "http://localhost:" + server.getActualPort());
        assertFalse(request.abort(), "No active connector has accepted cancellation yet");
        Response response = client.handle(request);
        assertTrue(response.getStatus().isConnectorError());
        assertNotNull(request.getDisconnectSignal().toCompletableFuture().get(5, TimeUnit.SECONDS));
        assertEquals(0, received.get());
    }

    @Test
    void preservesHttp1PipeliningOrderWhileWatchingForIdleDisconnect() throws Exception {
        CountDownLatch firstStarted = new CountDownLatch(1);
        CountDownLatch releaseFirst = new CountDownLatch(1);
        AtomicInteger received = new AtomicInteger();
        AtomicReference<Request> first = new AtomicReference<>();
        start(
                "HTTP1_1",
                new Restlet() {
                    @Override
                    public void handle(Request request, Response response) {
                        int index = received.incrementAndGet();
                        if (index == 1) {
                            first.set(request);
                            firstStarted.countDown();
                            try {
                                if (!releaseFirst.await(5, TimeUnit.SECONDS)) {
                                    throw new AssertionError(
                                            "First pipelined request was not released");
                                }
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                throw new AssertionError(e);
                            }
                        }
                        response.setEntity("response-" + index, MediaType.TEXT_PLAIN);
                    }
                });
        try (var socket = new java.net.Socket("localhost", server.getActualPort())) {
            socket.setSoTimeout(5000);
            var output = socket.getOutputStream();
            output.write(
                    ("GET /first HTTP/1.1\r\nHost: localhost\r\n\r\n")
                            .getBytes(java.nio.charset.StandardCharsets.US_ASCII));
            output.flush();
            assertTrue(firstStarted.await(5, TimeUnit.SECONDS));
            output.write(
                    ("GET /second HTTP/1.1\r\nHost: localhost\r\nConnection: close\r\n\r\n")
                            .getBytes(java.nio.charset.StandardCharsets.US_ASCII));
            output.flush();
            releaseFirst.countDown();
            String responses =
                    new String(
                            socket.getInputStream().readAllBytes(),
                            java.nio.charset.StandardCharsets.US_ASCII);
            assertEquals(2, received.get());
            assertTrue(responses.contains("response-1"), responses);
            assertTrue(
                    responses.indexOf("response-2") > responses.indexOf("response-1"), responses);
            assertFalse(first.get().getDisconnectSignal().toCompletableFuture().isDone());
        } finally {
            releaseFirst.countDown();
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"HTTP1_1", "HTTP2"})
    void preservesEmptyErrorStatusWhenTheClientClosesImmediately(String mode) throws Exception {
        start(
                mode,
                new Restlet() {
                    @Override
                    public void handle(Request request, Response response) {
                        response.setStatus(
                                request.getResourceRef().getPath().equals("/401")
                                        ? Status.CLIENT_ERROR_UNAUTHORIZED
                                        : Status.CLIENT_ERROR_FORBIDDEN);
                    }
                });
        for (int i = 0; i < 64; i++) {
            Status expected =
                    i % 2 == 0 ? Status.CLIENT_ERROR_UNAUTHORIZED : Status.CLIENT_ERROR_FORBIDDEN;
            Request request =
                    new Request(
                            Method.GET,
                            "http://localhost:"
                                    + server.getActualPort()
                                    + "/"
                                    + expected.getCode());
            Response response = client.handle(request);
            client.stop();
            assertEquals(
                    expected,
                    response.getStatus(),
                    () -> "Terminal response changed: " + response.getStatus().getThrowable());
            assertFalse(request.getDisconnectSignal().toCompletableFuture().isDone());
            client.start();
        }
    }
}
