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
import org.restlet.representation.StringRepresentation;

class HttpTrailersTestCase {

    private static final String REQUEST_TRAILER = "X-Request-Trailer";

    private static final String RESPONSE_TRAILER = "X-Response-Trailer";

    private final AtomicReference<String> receivedRequestTrailer = new AtomicReference<>();

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

    private void roundTrip(String transportProtocol, String clientMode) throws Exception {
        Restlet echo =
                new Restlet() {
                    @Override
                    public void handle(Request request, Response response) {
                        String entity = request.getEntityAsText();
                        receivedRequestTrailer.set(
                                request.getTrailers().getFirstValue(REQUEST_TRAILER, true));
                        response.getTrailers().add(new Header(RESPONSE_TRAILER, "response"));
                        response.setEntity(entity, MediaType.TEXT_PLAIN);
                    }
                };
        Context serverContext = new Context();
        if (transportProtocol != null) {
            serverContext.getParameters().add("http.transport.protocol", transportProtocol);
        }
        server = new Server(serverContext, Protocol.HTTP, 0, echo);
        server.start();

        Context clientContext = new Context();
        if (clientMode != null) {
            clientContext.getParameters().add("httpClientTransportMode", clientMode);
        }
        client = new Client(clientContext, Protocol.HTTP);
        client.start();

        Request request =
                new Request(
                        Method.POST,
                        "http://localhost:" + server.getActualPort() + "/",
                        new StringRepresentation("request", MediaType.TEXT_PLAIN));
        request.getTrailers().add(new Header(REQUEST_TRAILER, "request"));

        Response response = client.handle(request);

        assertEquals("request", receivedRequestTrailer.get());
        assertEquals("request", response.getEntityAsText());
        assertEquals("response", response.getTrailers().getFirstValue(RESPONSE_TRAILER, true));
    }

    @Test
    void exchangesTrailersOverHttp11() throws Exception {
        roundTrip("HTTP1_1", "HTTP1_1");
    }

    @Test
    void exchangesTrailersOverHttp2() throws Exception {
        roundTrip("HTTP2", "HTTP2");
    }
}
