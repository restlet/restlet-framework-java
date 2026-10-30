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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.ServerSentEvent;
import org.restlet.data.Status;
import org.restlet.engine.Engine;
import org.restlet.representation.EventStreamReader;
import org.restlet.representation.EventStreamRepresentation;

class EventStreamConnectorTestCase {

    private Server server;

    private Client client;

    @BeforeEach
    void setUp() {
        Engine.clearThreadLocalVariables();
        Engine.register(false);
        Engine.getInstance().getRegisteredServers().add(new HttpServerHelper(null));
        Engine.getInstance().getRegisteredClients().add(new HttpClientHelper(null));
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

    @Test
    void flushesEventsBeforeTheResourceCompletesOverHttp11() throws Exception {
        assertStreamsBeforeCompletion(false);
    }

    @Test
    void flushesEventsBeforeTheResourceCompletesOverHttp2() throws Exception {
        assertStreamsBeforeCompletion(true);
    }

    @Test
    void sendsTheResponseHeadBeforeTheFirstEventOverHttp11() throws Exception {
        assertHeadBeforeFirstEvent(false);
    }

    @Test
    void sendsTheResponseHeadBeforeTheFirstEventOverHttp2() throws Exception {
        assertHeadBeforeFirstEvent(true);
    }

    @Test
    void enforcesTheConfiguredOpenStreamLimit() throws Exception {
        CountDownLatch firstStreamStarted = new CountDownLatch(1);
        CountDownLatch finishFirstStream = new CountDownLatch(1);
        Restlet restlet =
                new Restlet() {
                    @Override
                    public void handle(Request request, Response response) {
                        response.setEntity(
                                new EventStreamRepresentation(
                                        sink -> {
                                            sink.send(
                                                    ServerSentEvent.builder()
                                                            .data("started")
                                                            .build());
                                            firstStreamStarted.countDown();
                                            try {
                                                if (!finishFirstStream.await(5, TimeUnit.SECONDS)) {
                                                    throw new java.io.IOException(
                                                            "Timed out waiting to finish the event stream");
                                                }
                                                sink.send(
                                                        ServerSentEvent.builder()
                                                                .data("done")
                                                                .build());
                                            } catch (InterruptedException e) {
                                                Thread.currentThread().interrupt();
                                                throw new java.io.IOException(
                                                        "Interrupted while writing the event stream",
                                                        e);
                                            }
                                        }));
                    }
                };
        Context serverContext = new Context();
        serverContext.getParameters().add("server.maxEventStreams", "1");
        server = new Server(serverContext, Protocol.HTTP, 0, restlet);
        HttpServerHelper helper =
                (HttpServerHelper) serverContext.getAttributes().get("org.restlet.engine.helper");
        assertEquals(1, helper.getServerMaxEventStreams());
        server.start();
        client = new Client(new Context(), Protocol.HTTP);
        client.start();
        Client otherClient = new Client(new Context(), Protocol.HTTP);
        otherClient.start();

        try {
            Response first =
                    client.handle(
                            new Request(
                                    Method.GET,
                                    "http://localhost:" + server.getActualPort() + "/first"));
            assertTrue(firstStreamStarted.await(5, TimeUnit.SECONDS));
            Response second =
                    otherClient.handle(
                            new Request(
                                    Method.GET,
                                    "http://localhost:" + server.getActualPort() + "/second"));
            assertEquals(Status.SERVER_ERROR_SERVICE_UNAVAILABLE, second.getStatus());

            finishFirstStream.countDown();
            try (EventStreamReader reader = new EventStreamReader(first.getEntity().getStream())) {
                assertEquals("started", reader.readEvent().getData());
                assertEquals("done", reader.readEvent().getData());
                assertNull(reader.readEvent());
            }

            Response third =
                    otherClient.handle(
                            new Request(
                                    Method.GET,
                                    "http://localhost:" + server.getActualPort() + "/third"));
            assertEquals(Status.SUCCESS_OK, third.getStatus());
            try (EventStreamReader reader = new EventStreamReader(third.getEntity().getStream())) {
                assertEquals("started", reader.readEvent().getData());
                assertEquals("done", reader.readEvent().getData());
                assertNull(reader.readEvent());
            }
        } finally {
            finishFirstStream.countDown();
            otherClient.stop();
        }
    }

    @Test
    void requestExposesLastEventIdHeader() throws Exception {
        Restlet restlet =
                new Restlet() {
                    @Override
                    public void handle(Request request, Response response) {
                        response.setEntity(request.getLastEventId(), MediaType.TEXT_PLAIN);
                    }
                };
        server = new Server(new Context(), Protocol.HTTP, 0, restlet);
        server.start();
        client = new Client(new Context(), Protocol.HTTP);
        client.start();
        Request request =
                new Request(Method.GET, "http://localhost:" + server.getActualPort() + "/");
        request.getHeaders().add("Last-Event-ID", "resume-here");

        Response response = client.handle(request);

        assertEquals("resume-here", response.getEntityAsText());
    }

    @Test
    void openEventStreamIsNotClosedByTheConnectorIdleTimeout() throws Exception {
        Restlet restlet =
                new Restlet() {
                    @Override
                    public void handle(Request request, Response response) {
                        response.setEntity(
                                new EventStreamRepresentation(
                                        sink -> {
                                            try {
                                                // Waits past the idle timeout before sending.
                                                boolean released =
                                                        new CountDownLatch(1)
                                                                .await(350, TimeUnit.MILLISECONDS);
                                                assertFalse(released);
                                            } catch (InterruptedException e) {
                                                Thread.currentThread().interrupt();
                                                throw new java.io.IOException(
                                                        "Interrupted while awaiting an event", e);
                                            }
                                            sink.send(
                                                    ServerSentEvent.builder()
                                                            .data("after idle")
                                                            .build());
                                        }));
                    }
                };
        Context serverContext = new Context();
        serverContext.getParameters().add("connector.idleTimeout", "100");
        server = new Server(serverContext, Protocol.HTTP, 0, restlet);
        server.start();
        client = new Client(new Context(), Protocol.HTTP);
        client.start();

        Response response =
                client.handle(
                        new Request(
                                Method.GET, "http://localhost:" + server.getActualPort() + "/"));
        try (EventStreamReader reader = new EventStreamReader(response.getEntity().getStream())) {
            assertEquals("after idle", reader.readEvent().getData());
            assertNull(reader.readEvent());
        }
    }

    private void assertHeadBeforeFirstEvent(boolean http2) throws Exception {
        CountDownLatch sendEvent = new CountDownLatch(1);
        Restlet restlet =
                new Restlet() {
                    @Override
                    public void handle(Request request, Response response) {
                        response.setEntity(
                                new EventStreamRepresentation(
                                        sink -> {
                                            // An idle stream, without heartbeats
                                            try {
                                                if (!sendEvent.await(10, TimeUnit.SECONDS)) {
                                                    throw new java.io.IOException(
                                                            "Timed out waiting to send the event");
                                                }
                                            } catch (InterruptedException e) {
                                                Thread.currentThread().interrupt();
                                                throw new java.io.IOException(
                                                        "Interrupted while awaiting an event", e);
                                            }
                                            sink.send(
                                                    ServerSentEvent.builder().data("late").build());
                                        }));
                    }
                };
        Context serverContext = new Context();
        if (http2) {
            serverContext.getParameters().add("http.transport.protocol", "HTTP2");
        }
        server = new Server(serverContext, Protocol.HTTP, 0, restlet);
        server.start();

        // The JDK client returns the response as soon as its head arrives, before any body byte
        try (HttpClient jdkClient =
                HttpClient.newBuilder()
                        .version(http2 ? HttpClient.Version.HTTP_2 : HttpClient.Version.HTTP_1_1)
                        .build()) {
            HttpRequest request =
                    HttpRequest.newBuilder(
                                    URI.create("http://localhost:" + server.getActualPort() + "/"))
                            .build();
            HttpResponse<InputStream> response =
                    jdkClient
                            .sendAsync(request, HttpResponse.BodyHandlers.ofInputStream())
                            .get(5, TimeUnit.SECONDS);
            assertEquals(200, response.statusCode());
            assertEquals(
                    http2 ? HttpClient.Version.HTTP_2 : HttpClient.Version.HTTP_1_1,
                    response.version());
            assertTrue(
                    response.headers()
                            .firstValue("Content-Type")
                            .orElseThrow()
                            .startsWith(MediaType.TEXT_EVENT_STREAM.getName()));

            sendEvent.countDown();
            try (EventStreamReader reader = new EventStreamReader(response.body())) {
                assertEquals("late", reader.readEvent().getData());
                assertNull(reader.readEvent());
            }
        } finally {
            sendEvent.countDown();
        }
    }

    private void assertStreamsBeforeCompletion(boolean http2) throws Exception {
        CountDownLatch firstEventSent = new CountDownLatch(1);
        CountDownLatch continueWriting = new CountDownLatch(1);
        CountDownLatch writerFinished = new CountDownLatch(1);
        Restlet restlet =
                new Restlet() {
                    @Override
                    public void handle(Request request, Response response) {
                        response.setEntity(
                                new EventStreamRepresentation(
                                        sink -> {
                                            try {
                                                sink.send(
                                                        ServerSentEvent.builder()
                                                                .id("one")
                                                                .data("first")
                                                                .build());
                                                firstEventSent.countDown();
                                                if (!continueWriting.await(5, TimeUnit.SECONDS)) {
                                                    throw new java.io.IOException(
                                                            "Timed out waiting to continue the event stream");
                                                }
                                                sink.send(
                                                        ServerSentEvent.builder()
                                                                .data("second")
                                                                .build());
                                            } catch (InterruptedException e) {
                                                Thread.currentThread().interrupt();
                                                throw new java.io.IOException(
                                                        "Interrupted while writing the event stream",
                                                        e);
                                            } finally {
                                                writerFinished.countDown();
                                            }
                                        }));
                    }
                };

        Context serverContext = new Context();
        Context clientContext = new Context();
        if (http2) {
            serverContext.getParameters().add("http.transport.protocol", "HTTP2");
            clientContext.getParameters().add("httpClientTransportMode", "HTTP2");
        }
        server = new Server(serverContext, Protocol.HTTP, 0, restlet);
        server.start();
        client = new Client(clientContext, Protocol.HTTP);
        client.start();

        Response response =
                client.handle(
                        new Request(
                                Method.GET, "http://localhost:" + server.getActualPort() + "/"));
        assertEquals(http2 ? "2.0" : "1.1", response.getProtocol().getVersion());
        assertTrue(MediaType.TEXT_EVENT_STREAM.includes(response.getEntity().getMediaType()));
        InputStream body = response.getEntity().getStream();
        try (EventStreamReader reader = new EventStreamReader(body)) {
            assertTrue(firstEventSent.await(5, TimeUnit.SECONDS));
            ServerSentEvent first = reader.readEvent();
            assertEquals("one", first.getId());
            assertEquals("first", first.getData());
            assertFalse(
                    writerFinished.await(100, TimeUnit.MILLISECONDS),
                    "The first event must arrive while the response is still open");

            continueWriting.countDown();
            assertEquals("second", reader.readEvent().getData());
            assertNull(reader.readEvent());
        } finally {
            continueWriting.countDown();
        }
        assertTrue(writerFinished.await(5, TimeUnit.SECONDS));
    }
}
