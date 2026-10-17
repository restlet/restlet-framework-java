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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.engine.Engine;
import org.restlet.representation.StringRepresentation;

/** Tests the HTTP redirects followed by the client connector. */
class RedirectTestCase {

    /** The requests received by the origin server, as "METHOD path [entity]". */
    private final List<String> received = new CopyOnWriteArrayList<>();

    /** The Authorization headers received by the second server. */
    private final List<String> otherOriginAuthorizations = new CopyOnWriteArrayList<>();

    private Server server;

    private Server otherOrigin;

    private Client client;

    private String base;

    @BeforeEach
    void setUp() throws Exception {
        Engine.clearThreadLocalVariables();
        Engine.register(false);
        Engine.getInstance().getRegisteredServers().add(new HttpServerHelper(null));
        Engine.getInstance().getRegisteredClients().add(new HttpClientHelper(null));

        otherOrigin =
                new Server(
                        new Context(),
                        Protocol.HTTP,
                        0,
                        new Restlet() {
                            @Override
                            public void handle(Request request, Response response) {
                                otherOriginAuthorizations.add(
                                        String.valueOf(
                                                request.getHeaders()
                                                        .getFirstValue("Authorization", true)));
                                response.setEntity("other origin", MediaType.TEXT_PLAIN);
                            }
                        });
        otherOrigin.start();

        server =
                new Server(
                        new Context(),
                        Protocol.HTTP,
                        0,
                        new Restlet() {
                            @Override
                            public void handle(Request request, Response response) {
                                String path = request.getResourceRef().getPath();
                                String entity =
                                        request.isEntityAvailable()
                                                ? request.getEntityAsText()
                                                : "";
                                received.add(request.getMethod() + " " + path + " " + entity);
                                redirect(path, response);
                            }
                        });
        server.start();
        base = "http://localhost:" + server.getActualPort();
    }

    private void redirect(String path, Response response) {
        switch (path) {
            case "/301" -> redirect(response, Status.REDIRECTION_PERMANENT, "/target");
            case "/302" -> redirect(response, Status.REDIRECTION_FOUND, "/target");
            case "/303" -> redirect(response, Status.REDIRECTION_SEE_OTHER, "/target");
            case "/307" -> redirect(response, Status.REDIRECTION_TEMPORARY, "/target");
            case "/308" -> redirect(response, new Status(308), "/target");
            case "/chain1" -> redirect(response, Status.REDIRECTION_FOUND, "/chain2");
            case "/chain2" -> redirect(response, Status.REDIRECTION_FOUND, "target");
            case "/loop" -> redirect(response, Status.REDIRECTION_FOUND, "/loop");
            case "/other" ->
                    redirect(
                            response,
                            Status.REDIRECTION_FOUND,
                            "http://localhost:" + otherOrigin.getActualPort() + "/x");
            default -> response.setEntity("target reached", MediaType.TEXT_PLAIN);
        }
    }

    private static void redirect(Response response, Status status, String location) {
        response.setStatus(status);
        response.setLocationRef(location);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (client != null) {
            client.stop();
        }
        server.stop();
        otherOrigin.stop();
        Engine.clearThreadLocalVariables();
    }

    private Response send(Request request, String... parameters) throws Exception {
        Context context = new Context();
        for (int i = 0; i < parameters.length; i += 2) {
            context.getParameters().add(parameters[i], parameters[i + 1]);
        }
        client = new Client(context, Protocol.HTTP);
        client.start();
        return client.handle(request);
    }

    @Test
    void followsRedirectsWithGet() throws Exception {
        for (String code : List.of("301", "302", "303", "307", "308")) {
            received.clear();
            Response response = send(new Request(Method.GET, base + "/" + code));
            assertEquals(Status.SUCCESS_OK, response.getStatus(), code);
            assertEquals("target reached", response.getEntityAsText(), code);
            assertEquals(List.of("GET /" + code + " ", "GET /target "), received, code);
            client.stop();
        }
    }

    @Test
    void postBecomesGetOn302And303() throws Exception {
        for (String code : List.of("301", "302", "303")) {
            received.clear();
            Response response =
                    send(
                            new Request(
                                    Method.POST, base + "/" + code, new StringRepresentation("x")));
            assertEquals(Status.SUCCESS_OK, response.getStatus(), code);
            assertEquals(List.of("POST /" + code + " x", "GET /target "), received, code);
            client.stop();
        }
    }

    @Test
    void methodAndEntityKeptOn307And308() throws Exception {
        for (String code : List.of("307", "308")) {
            received.clear();
            Response response =
                    send(
                            new Request(
                                    Method.PUT,
                                    base + "/" + code,
                                    new StringRepresentation("payload")));
            assertEquals(Status.SUCCESS_OK, response.getStatus(), code);
            assertEquals(
                    List.of("PUT /" + code + " payload", "PUT /target payload"), received, code);
            client.stop();
        }
    }

    @Test
    void followsRelativeChains() throws Exception {
        Response response = send(new Request(Method.GET, base + "/chain1"));
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals(List.of("GET /chain1 ", "GET /chain2 ", "GET /target "), received);
        // The response is attached to the original request.
        assertEquals(base + "/chain1", response.getRequest().getResourceRef().toString());
    }

    @Test
    void stopsAfterMaxRedirects() throws Exception {
        Response response = send(new Request(Method.GET, base + "/loop"), "maxRedirects", "3");
        assertEquals(Status.REDIRECTION_FOUND, response.getStatus());
        assertEquals(4, received.size());
    }

    @Test
    void doesNotFollowWhenDisabled() throws Exception {
        Response response =
                send(new Request(Method.GET, base + "/302"), "followRedirects", "false");
        assertEquals(Status.REDIRECTION_FOUND, response.getStatus());
        assertEquals(base + "/target", response.getLocationRef().toString());
        assertEquals(1, received.size());
    }

    @Test
    void dropsCredentialsForOtherOrigins() throws Exception {
        Request request = new Request(Method.GET, base + "/other");
        request.setChallengeResponse(
                new ChallengeResponse(ChallengeScheme.HTTP_BASIC, "user", "secret"));
        Response response = send(request);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("other origin", response.getEntityAsText());
        assertEquals(List.of("null"), otherOriginAuthorizations);
    }
}
