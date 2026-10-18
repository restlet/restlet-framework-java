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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
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
import org.restlet.data.Status;
import org.restlet.engine.Engine;

/**
 * Tests that the server answers 503 Service Unavailable once "server.maxConcurrentCalls" calls are
 * in progress, and accepts calls again once they complete.
 */
class ServerMaxConcurrentCallsTestCase {

    private static final int MAX_CALLS = 2;

    private final CountDownLatch inProgress = new CountDownLatch(MAX_CALLS);

    private final CountDownLatch release = new CountDownLatch(1);

    private Server server;

    private Client client;

    private String uri;

    @BeforeEach
    void setUp() throws Exception {
        Engine.clearThreadLocalVariables();
        Engine.register(false);
        Engine.getInstance().getRegisteredServers().add(new HttpServerHelper(null));
        Engine.getInstance().getRegisteredClients().add(new HttpClientHelper(null));

        Context context = new Context();
        context.getParameters().add("server.maxConcurrentCalls", Integer.toString(MAX_CALLS));
        server =
                new Server(
                        context,
                        Protocol.HTTP,
                        0,
                        new Restlet() {
                            @Override
                            public void handle(Request request, Response response) {
                                if ("/block".equals(request.getResourceRef().getPath())) {
                                    inProgress.countDown();
                                    try {
                                        release.await(30, TimeUnit.SECONDS);
                                    } catch (InterruptedException _) {
                                        Thread.currentThread().interrupt();
                                    }
                                }
                                response.setEntity("done", MediaType.TEXT_PLAIN);
                            }
                        });
        server.start();
        uri = "http://localhost:" + server.getActualPort();

        client = new Client(new Context(), Protocol.HTTP);
        client.start();
    }

    @AfterEach
    void tearDown() throws Exception {
        release.countDown();
        client.stop();
        server.stop();
        Engine.clearThreadLocalVariables();
    }

    @Test
    void rejectsCallsAboveTheLimit() throws Exception {
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<Status>> blocked = new ArrayList<>();
            for (int i = 0; i < MAX_CALLS; i++) {
                blocked.add(
                        executor.submit(
                                () ->
                                        client.handle(new Request(Method.GET, uri + "/block"))
                                                .getStatus()));
            }
            assertTrue(inProgress.await(10, TimeUnit.SECONDS), "The calls didn't start");

            // The limit is reached: extra calls are rejected right away.
            for (int i = 0; i < 3; i++) {
                Response response = client.handle(new Request(Method.GET, uri + "/extra"));
                assertEquals(Status.SERVER_ERROR_SERVICE_UNAVAILABLE, response.getStatus());
            }

            // Once the calls complete, the server accepts calls again.
            release.countDown();
            for (Future<Status> status : blocked) {
                assertEquals(Status.SUCCESS_OK, status.get(10, TimeUnit.SECONDS));
            }
            Response response = client.handle(new Request(Method.GET, uri + "/extra"));
            assertEquals(Status.SUCCESS_OK, response.getStatus());
            assertEquals("done", response.getEntityAsText());
        }
    }
}
