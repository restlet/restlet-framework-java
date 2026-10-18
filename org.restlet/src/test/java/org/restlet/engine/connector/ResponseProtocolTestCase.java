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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.restlet.Client;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.engine.Engine;
import org.restlet.util.WrapperResponse;

/** Tests the protocol version recorded in the responses received by the HTTP client. */
class ResponseProtocolTestCase {

    private Server server;

    private Client client;

    @BeforeEach
    void setUp() throws Exception {
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
    void clientResponseRecordsHttp11() throws Exception {
        server =
                new Server(
                        Protocol.HTTP,
                        0,
                        new Restlet() {
                            @Override
                            public void handle(Request request, Response response) {
                                response.setEntity("ok", MediaType.TEXT_PLAIN);
                            }
                        });
        server.start();
        client = new Client(Protocol.HTTP);
        client.start();

        Response response =
                client.handle(
                        new Request(Method.GET, "http://localhost:" + server.getActualPort()));

        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals(Protocol.HTTP, response.getProtocol());
        assertEquals("1.1", response.getProtocol().getVersion());
        assertEquals("HTTP/1.1 - " + Status.SUCCESS_OK, response.toString());
    }

    @Test
    void connectorErrorKeepsRequestProtocol() throws Exception {
        client = new Client(Protocol.HTTP);
        client.start();

        // Nothing listens on port 1.
        Request request = new Request(Method.GET, "http://localhost:1/");
        Response response = client.handle(request);

        assertEquals(Status.CONNECTOR_ERROR_CONNECTION, response.getStatus());
        assertSame(request.getProtocol(), response.getProtocol());
    }

    @Test
    void defaultsToRequestProtocol() {
        Request request = new Request(Method.GET, "https://example.com/");
        Response response = new Response(request);
        assertEquals(Protocol.HTTPS, response.getProtocol());

        Protocol http2 = Protocol.valueOf("https", "2.0");
        response.setProtocol(http2);
        assertSame(http2, response.getProtocol());

        response.setProtocol(null);
        assertEquals(Protocol.HTTPS, response.getProtocol());

        assertNull(new Response(null).getProtocol());
    }

    @Test
    void wrapperDelegatesProtocol() {
        Response wrapped = new Response(new Request(Method.GET, "http://example.com/"));
        WrapperResponse wrapper = new WrapperResponse(wrapped);
        Protocol http11 = Protocol.valueOf("http", "1.1");

        wrapper.setProtocol(http11);

        assertSame(http11, wrapped.getProtocol());
        assertSame(http11, wrapper.getProtocol());
    }
}
