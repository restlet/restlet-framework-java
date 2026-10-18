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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
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
import org.restlet.Uniform;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.engine.Engine;
import org.restlet.representation.OutputRepresentation;

class Http2CancellationTestCase {

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
    void abortFailsTheResponseStreamAndNotifiesTheResponseCallback() throws Exception {
        CountDownLatch responseWriteStarted = new CountDownLatch(1);
        CountDownLatch continueResponseWrite = new CountDownLatch(1);
        CountDownLatch responseWriteFailed = new CountDownLatch(1);

        Restlet streamingResponse =
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
                                            output.write(new byte[2 * 1024 * 1024]);
                                            output.flush();
                                        } catch (InterruptedException e) {
                                            Thread.currentThread().interrupt();
                                            throw new IOException(
                                                    "Interrupted while writing the response", e);
                                        } catch (IOException e) {
                                            responseWriteFailed.countDown();
                                            throw e;
                                        }
                                    }
                                });
                    }
                };
        Context serverContext = new Context();
        serverContext.getParameters().add("http.transport.protocol", "HTTP2");
        server = new Server(serverContext, Protocol.HTTP, 0, streamingResponse);
        server.start();

        Context clientContext = new Context();
        clientContext.getParameters().add("httpClientTransportMode", "HTTP2");
        client = new Client(clientContext, Protocol.HTTP);
        client.start();

        List<Status> callbackStatuses = new ArrayList<>();
        Request request =
                new Request(Method.GET, "http://localhost:" + server.getActualPort() + "/");
        Uniform onResponse = (req, res) -> callbackStatuses.add(res.getStatus());
        request.setOnResponse(onResponse);

        try {
            Response response = client.handle(request);
            assertTrue(responseWriteStarted.await(5, TimeUnit.SECONDS));
            assertEquals(List.of(Status.SUCCESS_OK), callbackStatuses);

            response.abort();

            assertEquals(Status.CONNECTOR_ERROR_COMMUNICATION, response.getStatus());
            assertEquals(2, callbackStatuses.size());
            assertEquals(Status.CONNECTOR_ERROR_COMMUNICATION, callbackStatuses.get(1));
            assertThrows(IOException.class, () -> response.getEntity().getStream().readAllBytes());
        } finally {
            continueResponseWrite.countDown();
        }

        assertTrue(responseWriteFailed.await(10, TimeUnit.SECONDS));
    }
}
