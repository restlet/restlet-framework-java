/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.connector;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
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
import org.restlet.engine.header.HeaderConstants;

/**
 * Tests that concurrent client calls share one HTTP/2 connection, one stream per call, and that the
 * calls are processed concurrently by the server.
 */
class Http2MultiplexingTestCase {

    private static final int CALLS = 50;

    /** Number of calls blocked together on the server, proving that streams are concurrent. */
    private static final int BARRIER = 10;

    @TempDir Path tempDir;

    /** The distinct client ports seen by the server, one per TCP connection. */
    private final Set<Integer> clientPorts = ConcurrentHashMap.newKeySet();

    private final Set<String> versions = ConcurrentHashMap.newKeySet();

    private final AtomicInteger inFlight = new AtomicInteger();

    private final AtomicInteger maxInFlight = new AtomicInteger();

    private CountDownLatch barrier;

    private Server server;

    private Client client;

    @BeforeEach
    void setUp() {
        Engine.clearThreadLocalVariables();
        Engine.register(false);
        Engine.getInstance().getRegisteredServers().add(new HttpServerHelper(null));
        Engine.getInstance().getRegisteredServers().add(new HttpsServerHelper(null));
        Engine.getInstance().getRegisteredClients().add(new HttpClientHelper(null));
        barrier = new CountDownLatch(BARRIER);
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

    private final Restlet echo =
            new Restlet() {
                @Override
                public void handle(Request request, Response response) {
                    clientPorts.add(request.getClientInfo().getPort());
                    versions.add(
                            String.valueOf(
                                    request.getAttributes()
                                            .get(HeaderConstants.ATTRIBUTE_VERSION)));
                    int current = inFlight.incrementAndGet();
                    maxInFlight.accumulateAndGet(current, Math::max);
                    try {
                        // The first calls wait for each other: they can only all get here if the
                        // streams are processed concurrently.
                        barrier.countDown();
                        barrier.await(10, TimeUnit.SECONDS);
                    } catch (InterruptedException _) {
                        Thread.currentThread().interrupt();
                    } finally {
                        inFlight.decrementAndGet();
                    }
                    response.setEntity(
                            "item " + request.getResourceRef().getQueryAsForm().getFirstValue("i"),
                            MediaType.TEXT_PLAIN);
                }
            };

    private void runConcurrentCalls(String baseUri) throws Exception {
        List<Future<String>> results = new ArrayList<>();
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < CALLS; i++) {
                final int item = i;
                results.add(
                        executor.submit(
                                () -> {
                                    Response response =
                                            client.handle(
                                                    new Request(
                                                            Method.GET, baseUri + "/?i=" + item));
                                    assertEquals(Status.SUCCESS_OK, response.getStatus());
                                    return response.getEntityAsText();
                                }));
            }
            for (int i = 0; i < CALLS; i++) {
                assertEquals("item " + i, results.get(i).get(30, TimeUnit.SECONDS));
            }
        }

        assertEquals(1, clientPorts.size(), "Calls should share one connection: " + clientPorts);
        assertEquals(Set.of("HTTP/2.0"), versions);
        assertTrue(
                maxInFlight.get() >= BARRIER,
                "Streams should be processed concurrently, max in flight: " + maxInFlight.get());
    }

    @Test
    void sharesOneTlsConnection() throws Exception {
        Path keystore = tempDir.resolve("dummy.p12");
        try (InputStream in = SslBaseConnectorsTestCase.class.getResourceAsStream("dummy.p12")) {
            Files.copy(in, keystore, REPLACE_EXISTING);
        }

        Context serverContext = new Context();
        serverContext.getParameters().add("http.transport.protocols", "HTTP2,HTTP1_1");
        serverContext.getParameters().add("keystorePath", keystore.toString());
        serverContext.getParameters().add("keystorePassword", "testtest");
        serverContext.getParameters().add("keyStoreType", "PKCS12");
        serverContext.getParameters().add("keyPassword", "testtest");
        server = new Server(serverContext, Protocol.HTTPS, 0, echo);
        server.start();

        Context context = new Context();
        context.getParameters().add("httpClientTransportMode", "DYNAMIC");
        context.getParameters().add("truststorePath", keystore.toString());
        context.getParameters().add("truststorePassword", "testtest");
        context.getParameters().add("trustStoreType", "PKCS12");
        client = new Client(context, Protocol.HTTPS);
        client.start();

        runConcurrentCalls("https://localhost:" + server.getActualPort());
    }

    @Test
    void sharesOneCleartextConnection() throws Exception {
        Context serverContext = new Context();
        serverContext.getParameters().add("http.transport.protocol", "HTTP2");
        server = new Server(serverContext, Protocol.HTTP, 0, echo);
        server.start();

        Context context = new Context();
        context.getParameters().add("httpClientTransportMode", "HTTP2");
        client = new Client(context, Protocol.HTTP);
        client.start();

        runConcurrentCalls("http://localhost:" + server.getActualPort());
    }
}
