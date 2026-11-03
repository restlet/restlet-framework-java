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
import org.restlet.engine.Engine;
import org.restlet.representation.OutputRepresentation;

/** Full response buffers reach the client while the producer pauses (issue #1632). */
class FullBufferFlushTestCase {
    /** Two full buffers of the default 32 KiB output buffer size. */
    private static final int PRODUCED = 64 * 1024;

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
    @ValueSource(strings = {"HTTP1_1", "HTTP2"})
    void fullBuffersReachTheClientWhileTheProducerPauses(String mode) throws Exception {
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
                                                out.write(new byte[PRODUCED]);
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
            assertEquals(PRODUCED, body.readNBytes(PRODUCED).length);
            read.countDown();
            assertFalse(heldBack.get(), "full buffers were held back until the producer resumed");
            assertEquals(16, body.readAllBytes().length);
        }
    }
}
