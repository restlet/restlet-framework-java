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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.restlet.Client;
import org.restlet.Component;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Dimension;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.ext.graphql.internal.GraphqlJson;

/** GET requests, with the operation selection, method and caching rules that apply to them. */
class GraphqlGetBindingTestCase {

    private final AtomicInteger counter = new AtomicInteger();

    private Component component;

    private Client client;

    private String base;

    @BeforeEach
    void setUp() throws Exception {
        component = new Component();
        component.getServers().add(Protocol.HTTP, 0);

        GraphqlServerAdapter get = new GraphqlServerAdapter();
        get.setGraphQL(GraphqlTestSchemas.graphQL(counter));
        get.setGetEnabled(true);
        component.getDefaultHost().attach("/get", get);

        // A customizer that selects another operation than the request.
        GraphqlServerAdapter switching = new GraphqlServerAdapter();
        switching.setGraphQL(GraphqlTestSchemas.graphQL(counter));
        switching.setGetEnabled(true);
        switching.setExecutionInputCustomizer((request, input) -> input.operationName("M"));
        component.getDefaultHost().attach("/switching", switching);

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

    private Response get(String path, Map<String, String> params) {
        Reference reference = new Reference(base + path);
        params.forEach(reference::addQueryParameter);
        return client.handle(new Request(Method.GET, reference));
    }

    @Test
    void customizedOperationIsChecked() {
        Response response =
                get(
                        "/switching",
                        Map.of(
                                "query",
                                "query Q { hello } mutation M { increment(by: 1) }",
                                "operationName",
                                "Q"));
        assertEquals(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED, response.getStatus());
        assertFalse(response.getAllowedMethods().contains(Method.GET));
        assertEquals(0, counter.get());
    }

    @Test
    void emptyOperationNameIsOmitted() throws Exception {
        Response response = get("/get", Map.of("query", "{ hello }", "operationName", ""));
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals(
                Map.of("hello", "Hello world"),
                GraphqlJson.asObject(GraphqlJson.parse(response.getEntity().getText()))
                        .get("data"));
    }

    @Test
    void negotiatedResponsesVaryOnAccept() {
        Response response = get("/get", Map.of("query", "{ hello }"));
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertTrue(response.getDimensions().contains(Dimension.MEDIA_TYPE));
    }

    @Test
    void otherMethodsAllowGet() {
        Response response = client.handle(new Request(Method.PUT, base + "/get"));
        assertEquals(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED, response.getStatus());
        assertTrue(response.getAllowedMethods().contains(Method.GET));
        assertTrue(response.getAllowedMethods().contains(Method.POST));
    }
}
