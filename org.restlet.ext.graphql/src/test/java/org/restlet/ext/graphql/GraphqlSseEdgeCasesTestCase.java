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
import static org.junit.jupiter.api.Assertions.assertTrue;

import graphql.ExecutionResult;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.restlet.Client;
import org.restlet.Component;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Preference;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.ext.graphql.internal.GraphqlJson;
import org.restlet.representation.StringRepresentation;

/** Edge cases of the {@code graphql-sse} binding: request errors, restarts and demand. */
class GraphqlSseEdgeCasesTestCase {

    private final AtomicInteger requests = new AtomicInteger();

    private Component component;

    private Client client;

    private String base;

    @BeforeEach
    void setUp() throws Exception {
        component = new Component();
        component.getServers().add(Protocol.HTTP, 0);
        GraphqlServerAdapter sse = new GraphqlServerAdapter();
        sse.setGraphQL(GraphqlTestSchemas.graphQL(new AtomicInteger()));
        sse.getSubscriptionTransports().add(GraphqlTransport.SSE);
        component.getDefaultHost().attach("/graphql", sse);
        component
                .getDefaultHost()
                .attach(
                        "/counted",
                        new Restlet() {
                            @Override
                            public void handle(Request request, Response response) {
                                requests.incrementAndGet();
                                response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
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

    private Response stream(Map<String, Object> body) {
        Request request = new Request(Method.POST, base + "/graphql");
        request.setEntity(
                new StringRepresentation(
                        GraphqlJson.writeString(body), MediaType.APPLICATION_JSON));
        request.getClientInfo()
                .getAcceptedMediaTypes()
                .add(new Preference<>(MediaType.TEXT_EVENT_STREAM));
        return client.handle(request);
    }

    @Test
    void documentErrorsAreBadRequests() {
        assertEquals(
                Status.CLIENT_ERROR_BAD_REQUEST,
                stream(Map.of("query", "subscription {")).getStatus());
        assertEquals(
                Status.CLIENT_ERROR_BAD_REQUEST,
                stream(Map.of("query", "{ hello }", "operationName", "Unknown")).getStatus());
        assertEquals(
                Status.CLIENT_ERROR_BAD_REQUEST,
                stream(Map.of("query", "query A { hello } query B { hello }")).getStatus());
    }

    @Test
    void adapterWithoutTransportsRestarts() throws Exception {
        GraphqlServerAdapter adapter = new GraphqlServerAdapter();
        adapter.setGraphQL(GraphqlTestSchemas.graphQL(new AtomicInteger()));
        adapter.start();
        adapter.stop();
        adapter.start();
        assertTrue(adapter.isStarted());
        assertTrue(adapter.getSubscriptionTransports().isEmpty());
        adapter.stop();
    }

    @Test
    void nonPositiveDemandFailsWithoutRequest() throws Exception {
        GraphqlClientAdapter graphql = new GraphqlClientAdapter();
        graphql.setClient(client);
        graphql.setEndpoint(base + "/counted");
        graphql.start();
        CountDownLatch done = new CountDownLatch(1);
        AtomicReference<Throwable> error = new AtomicReference<>();
        graphql.subscribe("subscription { ticks(count: 1) }", null, null)
                .subscribe(
                        new Subscriber<ExecutionResult>() {
                            @Override
                            public void onSubscribe(Subscription subscription) {
                                subscription.request(0);
                            }

                            @Override
                            public void onNext(ExecutionResult item) {}

                            @Override
                            public void onError(Throwable throwable) {
                                error.set(throwable);
                                done.countDown();
                            }

                            @Override
                            public void onComplete() {
                                done.countDown();
                            }
                        });
        assertTrue(done.await(5, TimeUnit.SECONDS));
        assertInstanceOf(IllegalArgumentException.class, error.get());
        assertEquals(0, requests.get());
        graphql.stop();
    }
}
