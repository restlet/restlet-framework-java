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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
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
import org.restlet.engine.Engine;
import org.restlet.representation.OutputRepresentation;

class Http2FullDuplexTestCase {

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
    void receivesResponseWhileRequestEntityIsStillBeingWritten() throws Exception {
        CountDownLatch requestWriteStarted = new CountDownLatch(1);
        CountDownLatch continueRequestWrite = new CountDownLatch(1);
        CountDownLatch requestWriteFinished = new CountDownLatch(1);

        Restlet earlyResponse =
                new Restlet() {
                    @Override
                    public void handle(Request request, Response response) {
                        response.setEntity("early response", MediaType.TEXT_PLAIN);
                    }
                };
        Context serverContext = new Context();
        serverContext.getParameters().add("http.transport.protocol", "HTTP2");
        server = new Server(serverContext, Protocol.HTTP, 0, earlyResponse);
        server.start();

        Context clientContext = new Context();
        clientContext.getParameters().add("httpClientTransportMode", "HTTP2");
        client = new Client(clientContext, Protocol.HTTP);
        client.start();

        OutputRepresentation entity =
                new OutputRepresentation(MediaType.TEXT_PLAIN) {
                    @Override
                    public void write(OutputStream output) throws IOException {
                        try {
                            output.write("first".getBytes(StandardCharsets.UTF_8));
                            output.flush();
                            requestWriteStarted.countDown();
                            if (!continueRequestWrite.await(10, TimeUnit.SECONDS)) {
                                throw new IOException("Timed out waiting to continue the request");
                            }
                            output.write("second".getBytes(StandardCharsets.UTF_8));
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            throw new IOException("Interrupted while writing the request", e);
                        } finally {
                            requestWriteFinished.countDown();
                        }
                    }
                };
        Request request =
                new Request(
                        Method.POST, "http://localhost:" + server.getActualPort() + "/", entity);

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var responseFuture = executor.submit(() -> client.handle(request));
            try {
                assertTrue(requestWriteStarted.await(5, TimeUnit.SECONDS));
                Response response = responseFuture.get(5, TimeUnit.SECONDS);
                assertEquals("early response", response.getEntityAsText());
                assertFalse(
                        requestWriteFinished.await(100, TimeUnit.MILLISECONDS),
                        "The response must arrive before the request entity is finished");
            } finally {
                continueRequestWrite.countDown();
            }
            assertTrue(requestWriteFinished.await(5, TimeUnit.SECONDS));
        }
    }
}
