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

import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.data.Header;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.engine.Engine;

class Http2TrailerHeadersTestCase {

    private final AtomicReference<String> receivedTe = new AtomicReference<>();

    private final AtomicReference<List<String>> receivedHeaderNames = new AtomicReference<>();

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
    void sendsTeTrailersAndNormalizesHttp2HeaderNames() throws Exception {
        Restlet echo =
                new Restlet() {
                    @Override
                    public void handle(Request request, Response response) {
                        receivedTe.set(request.getHeaders().getFirstValue("te", true));
                        receivedHeaderNames.set(
                                request.getHeaders().stream().map(Header::getName).toList());
                        response.getHeaders().add(new Header("X-Response-Header", "response"));
                        response.getTrailers().add(new Header("X-Response-Trailer", "response"));
                        response.setEntity("ok", MediaType.TEXT_PLAIN);
                    }
                };
        Context serverContext = new Context();
        serverContext.getParameters().add("http.transport.protocol", "HTTP2");
        server = new Server(serverContext, Protocol.HTTP, 0, echo);
        server.start();

        Context clientContext = new Context();
        clientContext.getParameters().add("httpClientTransportMode", "HTTP2");
        client = new Client(clientContext, Protocol.HTTP);
        client.start();

        Request request =
                new Request(Method.GET, "http://localhost:" + server.getActualPort() + "/");
        request.getHeaders().add(new Header("X-Request-Header", "request"));

        Response response = client.handle(request);

        assertEquals("trailers", receivedTe.get());
        assertTrue(
                receivedHeaderNames.get().stream()
                        .allMatch(name -> name.equals(name.toLowerCase(Locale.ROOT))));
        assertEquals("ok", response.getEntityAsText());
        assertTrue(
                response.getHeaders().stream()
                        .allMatch(
                                header ->
                                        header.getName()
                                                .equals(
                                                        header.getName()
                                                                .toLowerCase(Locale.ROOT))));
        assertEquals("response", response.getTrailers().getFirstValue("x-response-trailer", true));
        assertEquals("x-response-trailer", response.getTrailers().get(0).getName());
    }
}
