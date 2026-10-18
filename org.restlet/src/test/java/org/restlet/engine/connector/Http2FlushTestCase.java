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
import java.io.InputStream;
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

class Http2FlushTestCase {

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

    private void startHttp2(Restlet restlet) throws Exception {
        Context serverContext = new Context();
        serverContext.getParameters().add("http.transport.protocol", "HTTP2");
        server = new Server(serverContext, Protocol.HTTP, 0, restlet);
        server.start();

        Context clientContext = new Context();
        clientContext.getParameters().add("httpClientTransportMode", "HTTP2");
        client = new Client(clientContext, Protocol.HTTP);
        client.start();
    }

    @Test
    void requestFlushDeliversDataBeforeTheEntityIsFinished() throws Exception {
        CountDownLatch requestWriteStarted = new CountDownLatch(1);
        CountDownLatch continueRequestWrite = new CountDownLatch(1);
        CountDownLatch requestWriteFinished = new CountDownLatch(1);

        Restlet readFirstBytes =
                new Restlet() {
                    @Override
                    public void handle(Request request, Response response) {
                        try {
                            byte[] bytes = request.getEntity().getStream().readNBytes(5);
                            response.setEntity(
                                    new String(bytes, StandardCharsets.UTF_8),
                                    MediaType.TEXT_PLAIN);
                        } catch (IOException e) {
                            throw new IllegalStateException(e);
                        }
                    }
                };
        startHttp2(readFirstBytes);

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
        try {
            Response response = client.handle(request);
            assertTrue(requestWriteStarted.await(5, TimeUnit.SECONDS));
            assertEquals("first", response.getEntityAsText());
            assertFalse(
                    requestWriteFinished.await(100, TimeUnit.MILLISECONDS),
                    "The request should still be writing after its first flushed chunk");
        } finally {
            continueRequestWrite.countDown();
        }
        assertTrue(requestWriteFinished.await(5, TimeUnit.SECONDS));
    }

    @Test
    void responseFlushDeliversDataBeforeTheRepresentationIsFinished() throws Exception {
        CountDownLatch responseWriteStarted = new CountDownLatch(1);
        CountDownLatch continueResponseWrite = new CountDownLatch(1);
        CountDownLatch responseWriteFinished = new CountDownLatch(1);

        Restlet writeChunks =
                new Restlet() {
                    @Override
                    public void handle(Request request, Response response) {
                        response.setEntity(
                                new OutputRepresentation(MediaType.TEXT_PLAIN) {
                                    @Override
                                    public void write(OutputStream output) throws IOException {
                                        try {
                                            output.write("first".getBytes(StandardCharsets.UTF_8));
                                            output.flush();
                                            responseWriteStarted.countDown();
                                            if (!continueResponseWrite.await(
                                                    10, TimeUnit.SECONDS)) {
                                                throw new IOException(
                                                        "Timed out waiting to continue the response");
                                            }
                                            output.write("second".getBytes(StandardCharsets.UTF_8));
                                        } catch (InterruptedException e) {
                                            Thread.currentThread().interrupt();
                                            throw new IOException(
                                                    "Interrupted while writing the response", e);
                                        } finally {
                                            responseWriteFinished.countDown();
                                        }
                                    }
                                });
                    }
                };
        startHttp2(writeChunks);

        Response response =
                client.handle(
                        new Request(
                                Method.GET, "http://localhost:" + server.getActualPort() + "/"));
        InputStream input = response.getEntity().getStream();
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var firstByte = executor.submit(() -> input.read());
            try {
                assertTrue(responseWriteStarted.await(5, TimeUnit.SECONDS));
                assertEquals((int) 'f', firstByte.get(5, TimeUnit.SECONDS));
                assertFalse(
                        responseWriteFinished.await(100, TimeUnit.MILLISECONDS),
                        "The response should still be writing after its first flushed chunk");
            } finally {
                continueResponseWrite.countDown();
            }
            assertEquals("irstsecond", new String(input.readAllBytes(), StandardCharsets.UTF_8));
        }
        assertTrue(responseWriteFinished.await(5, TimeUnit.SECONDS));
    }
}
