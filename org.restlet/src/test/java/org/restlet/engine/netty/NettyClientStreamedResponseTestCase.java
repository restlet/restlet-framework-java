/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.netty;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.sun.net.httpserver.HttpServer;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.engine.Engine;
import org.restlet.engine.connector.HttpClientHelper;
import org.restlet.engine.header.HeaderConstants;

/** Tests HTTP/1.1 responses returned at their head, before their first body byte. */
class NettyClientStreamedResponseTestCase {
    private final CountDownLatch bodyAllowed = new CountDownLatch(1);
    private final AtomicBoolean bodySent = new AtomicBoolean();
    private HttpServer peer;
    private Client client;

    @BeforeEach
    void start() throws Exception {
        Engine.clearThreadLocalVariables();
        Engine.register(false);
        Engine.getInstance().getRegisteredClients().add(new HttpClientHelper(null));
        peer = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        peer.createContext(
                "/delayed",
                exchange -> {
                    exchange.sendResponseHeaders(200, 0);
                    try (OutputStream out = exchange.getResponseBody()) {
                        out.flush();
                        bodyAllowed.await(10, TimeUnit.SECONDS);
                        bodySent.set(true);
                        out.write("late".getBytes(StandardCharsets.US_ASCII));
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
        peer.createContext(
                "/empty",
                exchange -> {
                    exchange.sendResponseHeaders(200, 0);
                    exchange.close();
                });
        peer.start();
        client = new Client(new Context(), Protocol.HTTP);
        client.start();
    }

    @AfterEach
    void stop() throws Exception {
        bodyAllowed.countDown();
        client.stop();
        peer.stop(0);
    }

    private Request request(String path, boolean streamed) {
        Request request =
                new Request(Method.GET, "http://localhost:" + peer.getAddress().getPort() + path);
        if (streamed) {
            request.getAttributes().put(HeaderConstants.ATTRIBUTE_STREAMED_RESPONSE, true);
        }
        return request;
    }

    @Test
    void streamedResponseReturnsBeforeItsFirstBodyByte() throws Exception {
        Response response =
                CompletableFuture.supplyAsync(() -> client.handle(request("/delayed", true)))
                        .get(5, TimeUnit.SECONDS);
        assertEquals(200, response.getStatus().getCode());
        assertFalse(bodySent.get());
        bodyAllowed.countDown();
        try (InputStream body = response.getEntity().getStream()) {
            assertArrayEquals("late".getBytes(StandardCharsets.US_ASCII), body.readAllBytes());
        }
    }

    /** Without the attribute, an empty chunked body still gives no entity. */
    @Test
    void emptyBodyIsAnEntityOnlyWhenStreamed() throws Exception {
        assertNull(client.handle(request("/empty", false)).getEntity());
        Response streamed = client.handle(request("/empty", true));
        assertNotNull(streamed.getEntity());
        try (InputStream body = streamed.getEntity().getStream()) {
            assertEquals(0, body.readAllBytes().length);
        }
    }
}
