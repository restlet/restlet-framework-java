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
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import graphql.ExecutionResult;
import java.util.List;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;
import org.restlet.Client;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Preference;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.ext.graphql.internal.GraphqlMediaTypes;
import org.restlet.representation.EmptyRepresentation;

/** Conversion of responses by the client adapter, and response media type negotiation. */
class GraphqlClientResponseTestCase {

    private static GraphqlClientAdapter adapter(Consumer<Response> responder) {
        Client client =
                new Client(Protocol.HTTP) {
                    @Override
                    public void handle(Request request, Response response) {
                        responder.accept(response);
                    }
                };
        GraphqlClientAdapter adapter = new GraphqlClientAdapter();
        adapter.setClient(client);
        adapter.setEndpoint("http://localhost/graphql");
        adapter.start();
        return adapter;
    }

    @Test
    void entityWithoutStreamIsTransportError() {
        GraphqlClientAdapter adapter =
                adapter(
                        response -> {
                            response.setStatus(Status.SUCCESS_OK);
                            response.setEntity(new EmptyRepresentation());
                        });
        GraphqlTransportException e =
                assertThrows(
                        GraphqlTransportException.class,
                        () -> adapter.execute("{ hello }", null, null));
        assertEquals(Status.SUCCESS_OK, e.getStatus());
        assertNull(e.getBodyExcerpt());
    }

    @Test
    void malformedResultIsTransportError() {
        GraphqlClientAdapter adapter =
                adapter(
                        response ->
                                response.setEntity(
                                        "{\"errors\":\"Unauthorized\"}",
                                        MediaType.APPLICATION_JSON));
        GraphqlTransportException e =
                assertThrows(
                        GraphqlTransportException.class,
                        () -> adapter.execute("{ hello }", null, null));
        assertEquals(Status.SUCCESS_OK, e.getStatus());
        assertEquals("{\"errors\":\"Unauthorized\"}", e.getBodyExcerpt());
        assertInstanceOf(ClassCastException.class, e.getCause());
    }

    @Test
    void explicitNullDataIsPreserved() throws Exception {
        GraphqlClientAdapter adapter =
                adapter(
                        response ->
                                response.setEntity(
                                        "{\"data\":null,\"errors\":[{\"message\":\"boom\"}]}",
                                        MediaType.APPLICATION_JSON));
        ExecutionResult result = adapter.execute("{ hello }", null, null);
        assertTrue(result.isDataPresent());
        assertNull(result.getData());
        assertTrue(result.toSpecification().containsKey("data"));
        assertEquals(1, result.getErrors().size());
    }

    @Test
    void mostSpecificRangeWins() {
        assertNull(
                GraphqlMediaTypes.negotiate(
                        List.of(
                                new Preference<>(MediaType.APPLICATION_JSON, 0F),
                                new Preference<>(MediaType.ALL, 1F))));
        assertEquals(
                MediaType.APPLICATION_JSON,
                GraphqlMediaTypes.negotiate(
                        List.of(
                                new Preference<>(MediaType.ALL, 0F),
                                new Preference<>(MediaType.APPLICATION_JSON, 0.5F))));
        assertEquals(
                0F,
                GraphqlMediaTypes.quality(
                        List.of(
                                new Preference<>(MediaType.valueOf("application/*"), 0F),
                                new Preference<>(MediaType.ALL, 1F)),
                        MediaType.APPLICATION_JSON,
                        false));
    }
}
