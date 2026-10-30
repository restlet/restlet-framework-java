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
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.incremental.DeferPayload;
import graphql.incremental.DelayedIncrementalPartialResult;
import graphql.incremental.IncrementalExecutionResult;
import graphql.schema.DataFetcher;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
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
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Preference;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.ext.graphql.internal.GraphqlJson;
import org.restlet.representation.MultiPartRepresentation;
import org.restlet.representation.MultipartStreamReader;
import org.restlet.representation.StringRepresentation;

/** Incremental delivery of {@code @defer} results over {@code multipart/mixed}. */
class GraphqlIncrementalTestCase {

    private static final String SDL =
            """
            directive @defer(if: Boolean, label: String) on FRAGMENT_SPREAD | INLINE_FRAGMENT
            type Query {
              fast: String
              slow: String
              broken: String
            }
            """;

    private static final String DEFERRED = "{ fast ... @defer(label: \"d\") { slow } }";

    private final CountDownLatch release = new CountDownLatch(1);

    private Component component;

    private Client client;

    private String base;

    @BeforeEach
    void setUp() throws Exception {
        DataFetcher<Object> slow =
                env ->
                        CompletableFuture.supplyAsync(
                                () -> {
                                    try {
                                        release.await(5, TimeUnit.SECONDS);
                                    } catch (InterruptedException e) {
                                        Thread.currentThread().interrupt();
                                    }
                                    return "slow";
                                });
        DataFetcher<Object> broken =
                env -> {
                    throw new IllegalStateException("broken");
                };
        RuntimeWiring wiring =
                RuntimeWiring.newRuntimeWiring()
                        .type(
                                "Query",
                                type ->
                                        type.dataFetcher("fast", env -> "fast")
                                                .dataFetcher("slow", slow)
                                                .dataFetcher("broken", broken))
                        .build();
        GraphQL graphQL =
                GraphQL.newGraphQL(
                                new SchemaGenerator()
                                        .makeExecutableSchema(
                                                new SchemaParser().parse(SDL), wiring))
                        .build();

        component = new Component();
        component.getServers().add(Protocol.HTTP, 0);
        GraphqlServerAdapter incremental = new GraphqlServerAdapter();
        incremental.setGraphQL(graphQL);
        incremental.setIncrementalDeliveryEnabled(true);
        component.getDefaultHost().attach("/graphql", incremental);

        GraphqlServerAdapter limited = new GraphqlServerAdapter();
        limited.setGraphQL(graphQL);
        limited.setIncrementalDeliveryEnabled(true);
        limited.setMaxQueuedBytes(10);
        component.getDefaultHost().attach("/limited", limited);

        GraphqlServerAdapter disabled = new GraphqlServerAdapter();
        disabled.setGraphQL(graphQL);
        component.getDefaultHost().attach("/disabled", disabled);
        component.start();
        base = "http://localhost:" + component.getServers().get(0).getActualPort();
        client = new Client(Protocol.HTTP);
        client.start();
    }

    @AfterEach
    void tearDown() throws Exception {
        release.countDown();
        client.stop();
        component.stop();
    }

    private Response post(String path, String query, MediaType... accepted) {
        Request request = new Request(Method.POST, base + path);
        request.setEntity(
                new StringRepresentation(
                        GraphqlJson.writeString(Map.of("query", query)),
                        MediaType.APPLICATION_JSON));
        for (MediaType type : accepted) {
            request.getClientInfo().getAcceptedMediaTypes().add(new Preference<>(type));
        }
        return client.handle(request);
    }

    private static Map<String, Object> json(MultiPartRepresentation.Part part) throws Exception {
        assertEquals(
                "application/json; charset=utf-8", part.getHeaders().getFirstValue("Content-Type"));
        return GraphqlJson.asObject(
                GraphqlJson.parse(part.getContentAsString(StandardCharsets.UTF_8)));
    }

    private GraphqlClientAdapter graphql(String path) {
        GraphqlClientAdapter graphql = new GraphqlClientAdapter();
        graphql.setClient(client);
        graphql.setEndpoint(base + path);
        graphql.setIncrementalDeliveryEnabled(true);
        graphql.start();
        return graphql;
    }

    @Test
    void initialResultIsSentBeforeDeferredOnes() throws Exception {
        Response response = post("/graphql", DEFERRED, MediaType.MULTIPART_MIXED);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        MediaType type = response.getEntity().getMediaType();
        assertTrue(MediaType.MULTIPART_MIXED.equals(type, true));
        assertEquals("-", MultiPartRepresentation.getBoundary(type));
        assertEquals("20220824", type.getParameters().getFirstValue("deferSpec"));

        try (MultipartStreamReader reader = new MultipartStreamReader(response.getEntity(), 4096)) {
            Map<String, Object> initial = json(reader.readPart());
            assertEquals(Map.of("fast", "fast"), initial.get("data"));
            assertEquals(true, initial.get("hasNext"));

            release.countDown();
            Map<String, Object> deferred = json(reader.readPart());
            assertEquals(false, deferred.get("hasNext"));
            Map<String, Object> payload =
                    GraphqlJson.asObject(((List<?>) deferred.get("incremental")).get(0));
            assertEquals(Map.of("slow", "slow"), payload.get("data"));
            assertEquals("d", payload.get("label"));
            assertEquals(List.of(), payload.get("path"));
            assertNull(reader.readPart());
        }
    }

    @Test
    void withoutMultipartDeferIsNotIncremental() throws Exception {
        release.countDown();
        Response json = post("/graphql", DEFERRED, MediaType.APPLICATION_JSON);
        assertEquals(
                "{\"data\":{\"fast\":\"fast\",\"slow\":\"slow\"}}", json.getEntity().getText());

        Response disabled =
                post("/disabled", DEFERRED, MediaType.MULTIPART_MIXED, MediaType.APPLICATION_JSON);
        assertTrue(MediaType.APPLICATION_JSON.equals(disabled.getEntity().getMediaType(), true));
        assertEquals(
                Status.CLIENT_ERROR_NOT_ACCEPTABLE,
                post("/disabled", DEFERRED, MediaType.MULTIPART_MIXED).getStatus());

        Response plain = post("/graphql", "{ fast }", MediaType.MULTIPART_MIXED);
        assertTrue(MediaType.APPLICATION_JSON.equals(plain.getEntity().getMediaType(), true));
    }

    @Test
    void clientPublishesDeferredResults() throws Exception {
        GraphqlClientAdapter graphql = graphql("/graphql");
        assertTrue(graphql.isIncrementalDeliveryEnabled());
        ExecutionResult result = graphql.execute(DEFERRED, null, null);
        IncrementalExecutionResult incremental =
                assertInstanceOf(IncrementalExecutionResult.class, result);
        assertTrue(incremental.hasNext());
        assertEquals(Map.of("fast", "fast"), incremental.getData());

        TestSubscriber subscriber = new TestSubscriber();
        incremental.getIncrementalItemPublisher().subscribe(subscriber);
        subscriber.subscription.request(1);
        release.countDown();
        DelayedIncrementalPartialResult delayed = subscriber.next();
        assertFalse(delayed.hasNext());
        DeferPayload payload =
                assertInstanceOf(DeferPayload.class, delayed.getIncremental().get(0));
        assertEquals(Map.of("slow", "slow"), payload.getData());
        assertEquals("d", payload.getLabel());
        assertTrue(subscriber.done.await(5, TimeUnit.SECONDS));
        assertNull(subscriber.error.get());

        TestSubscriber second = new TestSubscriber();
        incremental.getIncrementalItemPublisher().subscribe(second);
        assertInstanceOf(IllegalStateException.class, second.error.get());
    }

    @Test
    void deferredErrorsArePayloadErrors() throws Exception {
        GraphqlClientAdapter graphql = graphql("/graphql");
        IncrementalExecutionResult incremental =
                (IncrementalExecutionResult)
                        graphql.execute("{ fast ... @defer { broken } }", null, null);
        TestSubscriber subscriber = new TestSubscriber();
        incremental.getIncrementalItemPublisher().subscribe(subscriber);
        subscriber.subscription.request(Long.MAX_VALUE);
        DeferPayload payload = (DeferPayload) subscriber.next().getIncremental().get(0);
        assertEquals(1, payload.getErrors().size());
        assertEquals(List.of("broken"), payload.getErrors().get(0).getPath());
        assertTrue(subscriber.done.await(5, TimeUnit.SECONDS));
    }

    @Test
    void plainResultsStayPlain() throws Exception {
        GraphqlClientAdapter graphql = graphql("/graphql");
        ExecutionResult result = graphql.execute("{ fast }", null, null);
        assertFalse(result instanceof IncrementalExecutionResult);
        assertEquals(Map.of("fast", "fast"), result.getData());
    }

    @Test
    void cancellationAndInvalidDemand() throws Exception {
        GraphqlClientAdapter graphql = graphql("/graphql");
        IncrementalExecutionResult cancelled =
                (IncrementalExecutionResult) graphql.execute(DEFERRED, null, null);
        TestSubscriber subscriber = new TestSubscriber();
        cancelled.getIncrementalItemPublisher().subscribe(subscriber);
        subscriber.subscription.request(1);
        subscriber.subscription.cancel();
        subscriber.subscription.cancel();
        release.countDown();
        assertFalse(subscriber.done.await(200, TimeUnit.MILLISECONDS));

        IncrementalExecutionResult invalid =
                (IncrementalExecutionResult) graphql.execute(DEFERRED, null, null);
        TestSubscriber negative = new TestSubscriber();
        invalid.getIncrementalItemPublisher().subscribe(negative);
        negative.subscription.request(0);
        assertTrue(negative.done.await(5, TimeUnit.SECONDS));
        assertInstanceOf(IllegalArgumentException.class, negative.error.get());
    }

    @Test
    void failedDeliveryIsATransportError() throws Exception {
        release.countDown();
        GraphqlClientAdapter graphql = graphql("/limited");
        IncrementalExecutionResult incremental =
                (IncrementalExecutionResult) graphql.execute(DEFERRED, null, null);
        TestSubscriber subscriber = new TestSubscriber();
        incremental.getIncrementalItemPublisher().subscribe(subscriber);
        subscriber.subscription.request(1);
        assertTrue(subscriber.done.await(5, TimeUnit.SECONDS));
        assertInstanceOf(GraphqlTransportException.class, subscriber.error.get());

        GraphqlServerAdapter adapter = new GraphqlServerAdapter();
        assertFalse(adapter.isIncrementalDeliveryEnabled());
        adapter.setGraphQL(
                GraphQL.newGraphQL(
                                GraphqlTestSchemas.schema(
                                        new java.util.concurrent.atomic.AtomicInteger()))
                        .build());
        adapter.start();
        assertThrows(
                IllegalStateException.class, () -> adapter.setIncrementalDeliveryEnabled(true));
    }

    /** Collects the signals of a subscription. */
    private static final class TestSubscriber
            implements Subscriber<DelayedIncrementalPartialResult> {

        final BlockingQueue<DelayedIncrementalPartialResult> items = new LinkedBlockingQueue<>();

        final CountDownLatch done = new CountDownLatch(1);

        final AtomicReference<Throwable> error = new AtomicReference<>();

        volatile Subscription subscription;

        @Override
        public void onSubscribe(Subscription subscription) {
            this.subscription = subscription;
        }

        @Override
        public void onNext(DelayedIncrementalPartialResult item) {
            items.add(item);
        }

        @Override
        public void onError(Throwable throwable) {
            error.set(throwable);
            done.countDown();
        }

        @Override
        public void onComplete() {
            done.countDown();
        }

        DelayedIncrementalPartialResult next() throws InterruptedException {
            DelayedIncrementalPartialResult item = items.poll(5, TimeUnit.SECONDS);
            assertTrue(item != null, "Timed out waiting for a result");
            return item;
        }
    }
}
