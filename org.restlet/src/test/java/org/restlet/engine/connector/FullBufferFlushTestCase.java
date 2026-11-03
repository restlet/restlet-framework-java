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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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

/** Full response buffers reach the client while the producer pauses (issue #1632). */
class FullBufferFlushTestCase {
    /** The default output buffer size. */
    private static final int BUFFER = 32 * 1024;

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

    @ParameterizedTest
    @CsvSource({"HTTP1_1, 1", "HTTP1_1, 2", "HTTP2, 1", "HTTP2, 2"})
    void fullBuffersReachTheClientWhileTheProducerPauses(String mode, int buffers)
            throws Exception {
        int produced = buffers * BUFFER;
        CountDownLatch read = new CountDownLatch(1);
        AtomicBoolean heldBack = new AtomicBoolean();
        Context serverContext = new Context();
        serverContext.getParameters().add("http.transport.protocol", mode);
        server =
                new Server(
                        serverContext,
                        Protocol.HTTP,
                        0,
                        new Restlet() {
                            @Override
                            public void handle(Request request, Response response) {
                                response.setEntity(
                                        new OutputRepresentation(
                                                MediaType.APPLICATION_OCTET_STREAM) {
                                            @Override
                                            public void write(OutputStream out) throws IOException {
                                                out.write(new byte[produced]);
                                                try {
                                                    // Waits, without flushing, until the client
                                                    // has read what was produced.
                                                    heldBack.set(!read.await(5, TimeUnit.SECONDS));
                                                } catch (InterruptedException e) {
                                                    Thread.currentThread().interrupt();
                                                }
                                                out.write(new byte[16]);
                                            }
                                        });
                            }
                        });
        server.start();
        Context clientContext = new Context();
        clientContext.getParameters().add("httpClientTransportMode", mode);
        client = new Client(clientContext, Protocol.HTTP);
        client.start();

        Response response =
                client.handle(
                        new Request(Method.GET, "http://localhost:" + server.getActualPort()));
        try (InputStream body = response.getEntity().getStream()) {
            assertEquals(produced, body.readNBytes(produced).length);
            read.countDown();
            // The rest of the body follows the producer's wait, so its result is recorded.
            assertEquals(16, body.readAllBytes().length);
        }
        assertFalse(heldBack.get(), "full buffers were held back until the producer resumed");
    }
}
