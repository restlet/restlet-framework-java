/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.graphql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import graphql.ExecutionResult;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.restlet.Client;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Preference;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.ext.graphql.internal.GraphqlMediaTypes;
import org.restlet.representation.StringRepresentation;

/** Request errors on the server, and non-GraphQL responses on the client. */
class GraphqlHttpErrorsTestCase {

    private Component component;

    private Client client;

    private String base;

    @BeforeEach
    void setUp() throws Exception {
        component = new Component();
        component.getServers().add(Protocol.HTTP, 0);
        GraphqlServerAdapter adapter = new GraphqlServerAdapter();
        adapter.setGraphQL(GraphqlTestSchemas.graphQL(new AtomicInteger()));
        component.getDefaultHost().attach("/graphql", adapter);
        component
                .getDefaultHost()
                .attach(
                        "/html",
                        new Restlet() {
                            @Override
                            public void handle(Request request, Response response) {
                                response.setStatus(Status.SERVER_ERROR_BAD_GATEWAY);
                                response.setEntity("<html>Bad gateway</html>", MediaType.TEXT_HTML);
                            }
                        });
        component
                .getDefaultHost()
                .attach(
                        "/empty401",
                        new Restlet() {
                            @Override
                            public void handle(Request request, Response response) {
                                response.setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                            }
                        });
        component
                .getDefaultHost()
                .attach(
                        "/badjson",
                        new Restlet() {
                            @Override
                            public void handle(Request request, Response response) {
                                response.setEntity("{not json", MediaType.APPLICATION_JSON);
                            }
                        });
        component.start();
        base = "http://localhost:" + component.getServers().get(0).getActualPort();
        client = new Client(Protocol.HTTP);
        client.start();
    }

    @AfterEach
    void tearDown() throws Exception {
        client.stop();
        component.stop();
    }

    private Response post(String path, String body, MediaType accept) {
        Request request = new Request(Method.POST, base + path);
        if (body != null) {
            request.setEntity(new StringRepresentation(body, MediaType.APPLICATION_JSON));
        }
        if (accept != null) {
            request.getClientInfo().getAcceptedMediaTypes().add(new Preference<>(accept));
        }
        return client.handle(request);
    }

    @Test
    void otherMethodsAreNotAllowed() {
        Response response = client.handle(new Request(Method.PUT, base + "/graphql"));
        assertEquals(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED, response.getStatus());
        assertTrue(response.getAllowedMethods().contains(Method.POST));
    }

    @Test
    void malformedRequestsAreRequestErrors() throws Exception {
        for (String body : new String[] {null, "{bad", "[]", "{}", "{\"query\": 1}"}) {
            Response response = post("/graphql", body, null);
            assertEquals(Status.CLIENT_ERROR_BAD_REQUEST, response.getStatus(), body);
            assertTrue(response.getEntity().getText().contains("\"errors\""), body);
        }
    }

    @Test
    void responseMediaTypeIsNegotiated() throws Exception {
        String query = "{\"query\": \"{ hello }\"}";
        assertEquals(
                MediaType.APPLICATION_JSON,
                post("/graphql", query, null).getEntity().getMediaType());
        assertEquals(
                MediaType.APPLICATION_JSON,
                post("/graphql", query, MediaType.ALL).getEntity().getMediaType());
        assertEquals(
                GraphqlMediaTypes.GRAPHQL_RESPONSE_JSON,
                post("/graphql", query, GraphqlMediaTypes.GRAPHQL_RESPONSE_JSON)
                        .getEntity()
                        .getMediaType());
        assertEquals(
                MediaType.APPLICATION_JSON,
                post("/graphql", query, MediaType.valueOf("application/*"))
                        .getEntity()
                        .getMediaType());
        assertEquals(
                Status.CLIENT_ERROR_NOT_ACCEPTABLE,
                post("/graphql", query, MediaType.TEXT_HTML).getStatus());
    }

    @Test
    void operationNameVariablesAndExtensionsAreForwarded() throws Exception {
        Response response =
                post(
                        "/graphql",
                        """
                        {"query": "query A { hello } query B($n: String) { hello(name: $n) }",
                         "operationName": "B", "variables": {"n": "B"}, "extensions": {"x": 1}}
                        """,
                        null);
        assertEquals("{\"data\":{\"hello\":\"Hello B\"}}", response.getEntity().getText());
    }

    @Test
    void adapterWithoutGraphQLFails() {
        GraphqlServerAdapter adapter = new GraphqlServerAdapter(new Context());
        assertThrows(IllegalStateException.class, adapter::start);
        Request request = new Request(Method.POST, "http://localhost/graphql");
        Response response = new Response(request);
        adapter.handle(request, response);
        assertEquals(Status.SERVER_ERROR_INTERNAL, response.getStatus());
    }

    @Test
    void nonGraphqlResponsesAreTransportErrors() throws Exception {
        GraphqlTransportException html = execute("/html");
        assertEquals(Status.SERVER_ERROR_BAD_GATEWAY, html.getStatus());
        assertEquals("<html>Bad gateway</html>", html.getBodyExcerpt());

        GraphqlTransportException empty = execute("/empty401");
        // Restlet's status service gives the empty error response an HTML page.
        assertEquals(Status.CLIENT_ERROR_UNAUTHORIZED, empty.getStatus());

        GraphqlTransportException json = execute("/badjson");
        assertEquals(Status.SUCCESS_OK, json.getStatus());
        assertEquals("{not json", json.getBodyExcerpt());
    }

    @Test
    void requestErrorsAreResults() throws Exception {
        GraphqlClientAdapter graphql = new GraphqlClientAdapter();
        graphql.setClient(client);
        graphql.setEndpoint(base + "/graphql");
        graphql.start();
        ExecutionResult result = graphql.execute("{ unknownField }", null, null);
        assertFalse(result.isDataPresent());
        assertEquals(1, result.getErrors().size());
    }

    private GraphqlTransportException execute(String path) {
        GraphqlClientAdapter graphql = new GraphqlClientAdapter();
        graphql.setClient(client);
        graphql.setEndpoint(base + path);
        graphql.start();
        return assertThrows(
                GraphqlTransportException.class, () -> graphql.execute("{ hello }", null, null));
    }
}
