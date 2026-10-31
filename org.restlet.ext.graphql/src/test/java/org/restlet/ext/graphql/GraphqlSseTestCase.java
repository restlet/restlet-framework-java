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
import graphql.parser.Parser;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.UnExecutableSchemaGenerator;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
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
import org.restlet.data.Reference;
import org.restlet.data.ServerSentEvent;
import org.restlet.data.Status;
import org.restlet.ext.graphql.internal.GraphqlJson;
import org.restlet.representation.EventStreamReader;
import org.restlet.representation.EventStreamRepresentation;
import org.restlet.representation.StringRepresentation;

/** Subscriptions over {@code graphql-sse}, on the server and the client. */
class GraphqlSseTestCase {

    private Component component;

    private Client client;

    private String base;

    private GraphqlServerAdapter sse;

    private GraphqlServerAdapter single;

    @BeforeEach
    void setUp() throws Exception {
        component = new Component();
        component.getServers().add(Protocol.HTTP, 0);

        sse = adapter();
        sse.setGetEnabled(true);
        sse.setHeartbeatInterval(Duration.ofMillis(50));
        component.getDefaultHost().attach("/graphql", sse);

        single = adapter();
        single.setMaxSubscriptions(1);
        single.setMaxQueuedBytes(16);
        single.setHeartbeatInterval(null);
        component.getDefaultHost().attach("/limited", single);

        GraphqlServerAdapter disabled = new GraphqlServerAdapter();
        disabled.setGraphQL(GraphqlTestSchemas.graphQL(new AtomicInteger()));
        component.getDefaultHost().attach("/disabled", disabled);

        component
                .getDefaultHost()
                .attach(
                        "/truncated",
                        new Restlet() {
                            @Override
                            public void handle(Request request, Response response) {
                                response.setEntity(
                                        new EventStreamRepresentation(
                                                sink ->
                                                        sink.send(
                                                                ServerSentEvent.builder()
                                                                        .event("next")
                                                                        .data("{\"data\":{}}")
                                                                        .build())));
                            }
                        });
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

    private static GraphqlServerAdapter adapter() {
        GraphqlServerAdapter adapter = new GraphqlServerAdapter();
        adapter.setGraphQL(GraphqlTestSchemas.graphQL(new AtomicInteger()));
        adapter.getSubscriptionTransports().add(GraphqlTransport.SSE);
        return adapter;
    }

    private Response stream(String path, String query) {
        Request request = new Request(Method.POST, base + path);
        request.setEntity(
                new StringRepresentation(
                        GraphqlJson.writeString(Map.of("query", query)),
                        MediaType.APPLICATION_JSON));
        request.getClientInfo()
                .getAcceptedMediaTypes()
                .add(new Preference<>(MediaType.TEXT_EVENT_STREAM));
        return client.handle(request);
    }

    private GraphqlClientAdapter graphql(String path) {
        GraphqlClientAdapter graphql = new GraphqlClientAdapter();
        graphql.setClient(client);
        graphql.setEndpoint(base + path);
        return graphql;
    }

    private static void await(BooleanSupplier condition) throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (!condition.getAsBoolean()) {
            assertTrue(System.nanoTime() < deadline, "Timed out");
            Thread.sleep(10);
        }
    }

    /** Reads the next event, skipping comments. */
    private static ServerSentEvent nextEvent(EventStreamReader reader) throws Exception {
        ServerSentEvent event;
        do {
            event = reader.readEvent();
        } while (event != null && event.getEvent() == null);
        return event;
    }

    private static List<ServerSentEvent> events(Response response) throws Exception {
        List<ServerSentEvent> events = new ArrayList<>();
        try (EventStreamReader reader = new EventStreamReader(response.getEntity().getStream())) {
            ServerSentEvent event;
            while ((event = reader.readEvent()) != null) {
                if (event.getEvent() != null) {
                    events.add(event);
                }
            }
        }
        return events;
    }

    @Test
    void subscriptionStreamsNextThenComplete() throws Exception {
        Response response = stream("/graphql", "subscription { ticks(count: 3) }");
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertTrue(MediaType.TEXT_EVENT_STREAM.equals(response.getEntity().getMediaType(), true));
        List<ServerSentEvent> events = events(response);
        assertEquals(4, events.size());
        for (int i = 0; i < 3; i++) {
            assertEquals("next", events.get(i).getEvent());
            assertEquals("{\"data\":{\"ticks\":" + (i + 1) + "}}", events.get(i).getData());
        }
        assertEquals("complete", events.get(3).getEvent());
        assertEquals("", events.get(3).getData());
        await(() -> sse.getActiveSubscriptions() == 0);
    }

    @Test
    void queriesOverSseAreOneNextThenComplete() throws Exception {
        List<ServerSentEvent> events = events(stream("/graphql", "{ hello }"));
        assertEquals(2, events.size());
        assertEquals("{\"data\":{\"hello\":\"Hello world\"}}", events.get(0).getData());
        assertEquals("complete", events.get(1).getEvent());
    }

    @Test
    void requestErrorsAreJson() throws Exception {
        Response response = stream("/graphql", "subscription { unknown }");
        assertEquals(Status.CLIENT_ERROR_BAD_REQUEST, response.getStatus());
        assertTrue(MediaType.APPLICATION_JSON.equals(response.getEntity().getMediaType(), true));
        assertTrue(response.getEntity().getText().contains("\"errors\""));
    }

    @Test
    void getAcceptsSubscriptionsOverSse() throws Exception {
        Reference reference = new Reference(base + "/graphql");
        reference.addQueryParameter("query", "subscription { ticks(count: 1) }");
        Request request = new Request(Method.GET, reference);
        request.getClientInfo()
                .getAcceptedMediaTypes()
                .add(new Preference<>(MediaType.TEXT_EVENT_STREAM));
        assertEquals(2, events(client.handle(request)).size());

        Reference mutation = new Reference(base + "/graphql");
        mutation.addQueryParameter("query", "mutation { increment(by: 1) }");
        Request mutationRequest = new Request(Method.GET, mutation);
        mutationRequest
                .getClientInfo()
                .getAcceptedMediaTypes()
                .add(new Preference<>(MediaType.TEXT_EVENT_STREAM));
        assertEquals(
                Status.CLIENT_ERROR_METHOD_NOT_ALLOWED, client.handle(mutationRequest).getStatus());
    }

    @Test
    void sseIsOffUnlessEnabled() {
        assertEquals(
                Status.CLIENT_ERROR_NOT_ACCEPTABLE,
                stream("/disabled", "subscription { ticks(count: 1) }").getStatus());
    }

    @Test
    void clientDisconnectCancelsTheSubscription() throws Exception {
        int cancelled = TickPublisher.CANCELLED.get();
        Response response = stream("/graphql", "subscription { ticks(count: -1) }");
        try (EventStreamReader reader = new EventStreamReader(response.getEntity().getStream())) {
            assertEquals("next", nextEvent(reader).getEvent());
            assertEquals("next", nextEvent(reader).getEvent());
        }
        response.abort();
        await(() -> TickPublisher.CANCELLED.get() > cancelled);
        await(() -> sse.getActiveSubscriptions() == 0);
    }

    @Test
    void heartbeatsDetectDisconnectedIdleClients() throws Exception {
        int cancelled = TickPublisher.CANCELLED.get();
        Response response = stream("/graphql", "subscription { idle }");
        try (EventStreamReader reader = new EventStreamReader(response.getEntity().getStream())) {
            ServerSentEvent heartbeat = reader.readEvent();
            assertNull(heartbeat.getEvent());
            assertEquals("", heartbeat.getComment());
            assertEquals(1, sse.getActiveSubscriptions());
        }
        response.abort();
        await(() -> TickPublisher.CANCELLED.get() > cancelled);
        await(() -> sse.getActiveSubscriptions() == 0);
    }

    @Test
    void subscriptionsAreCapped() throws Exception {
        Response first = stream("/limited", "subscription { idle }");
        await(() -> single.getActiveSubscriptions() == 1);
        Response second = stream("/limited", "subscription { idle }");
        assertEquals(Status.SERVER_ERROR_SERVICE_UNAVAILABLE, second.getStatus());
        first.abort();
    }

    @Test
    void queuedBytesAreBounded() throws Exception {
        int cancelled = TickPublisher.CANCELLED.get();
        List<ServerSentEvent> events =
                events(stream("/limited", "subscription { ticks(count: 3) }"));
        assertEquals(2, events.size());
        assertTrue(events.get(0).getData().contains("queued bytes limit"));
        assertEquals("complete", events.get(1).getEvent());
        assertTrue(TickPublisher.CANCELLED.get() > cancelled);
    }

    @Test
    void clientHonorsDemandAndCompletes() throws Exception {
        GraphqlClientAdapter graphql = graphql("/graphql");
        graphql.start();
        TestSubscriber subscriber = new TestSubscriber();
        graphql.subscribe("subscription { ticks(count: 3) }", null, null).subscribe(subscriber);

        subscriber.subscription.request(1);
        assertEquals(Map.of("ticks", 1), subscriber.next().getData());
        Thread.sleep(100);
        assertTrue(subscriber.items.isEmpty(), "No result without demand");

        subscriber.subscription.request(5);
        assertEquals(Map.of("ticks", 2), subscriber.next().getData());
        assertEquals(Map.of("ticks", 3), subscriber.next().getData());
        assertTrue(subscriber.done.await(5, TimeUnit.SECONDS));
        assertNull(subscriber.error.get());
    }

    @Test
    void clientCancellationAbortsTheRequest() throws Exception {
        int cancelled = TickPublisher.CANCELLED.get();
        GraphqlClientAdapter graphql = graphql("/graphql");
        graphql.start();
        TestSubscriber subscriber = new TestSubscriber();
        graphql.subscribe(Parser.parse("subscription { ticks(count: -1) }"), null, Map.of())
                .subscribe(subscriber);
        subscriber.subscription.request(2);
        subscriber.next();
        subscriber.next();
        subscriber.subscription.cancel();
        await(() -> TickPublisher.CANCELLED.get() > cancelled);
        await(() -> sse.getActiveSubscriptions() == 0);
        assertEquals(1, subscriber.done.getCount());
    }

    @Test
    void clientStopFailsOpenSubscriptions() throws Exception {
        GraphqlClientAdapter graphql = graphql("/graphql");
        graphql.start();
        TestSubscriber running = new TestSubscriber();
        graphql.subscribe("subscription { idle }", null, null).subscribe(running);
        running.subscription.request(1);
        await(() -> sse.getActiveSubscriptions() == 1);
        TestSubscriber waiting = new TestSubscriber();
        graphql.subscribe("subscription { idle }", null, null).subscribe(waiting);

        graphql.stop();
        assertTrue(running.done.await(5, TimeUnit.SECONDS));
        assertInstanceOf(GraphqlTransportException.class, running.error.get());
        assertTrue(waiting.done.await(5, TimeUnit.SECONDS));
        assertInstanceOf(GraphqlTransportException.class, waiting.error.get());
        assertTrue(client.isStarted());
        await(() -> sse.getActiveSubscriptions() == 0);
        assertThrows(
                IllegalStateException.class,
                () -> graphql.subscribe("subscription { idle }", null, null));
    }

    @Test
    void clientPublishesRequestErrorsAsResults() throws Exception {
        GraphqlClientAdapter remote = graphql("/graphql");
        remote.start();
        TestSubscriber subscriber = new TestSubscriber();
        remote.subscribe("subscription { unknown }", null, null).subscribe(subscriber);
        subscriber.subscription.request(1);
        assertFalse(subscriber.next().isDataPresent());
        assertTrue(subscriber.done.await(5, TimeUnit.SECONDS));
        assertNull(subscriber.error.get());

        GraphqlClientAdapter local = graphql("/graphql");
        local.setSchema(
                UnExecutableSchemaGenerator.makeUnExecutableSchema(
                        new SchemaParser().parse(GraphqlTestSchemas.SDL)));
        local.start();
        for (Publisher<ExecutionResult> publisher :
                List.of(
                        local.subscribe("subscription { unknown }", null, null),
                        local.subscribe("subscription {", null, null),
                        local.subscribe(Parser.parse("subscription { unknown }"), null, null))) {
            TestSubscriber invalid = new TestSubscriber();
            publisher.subscribe(invalid);
            invalid.subscription.request(1);
            assertFalse(invalid.next().isDataPresent());
            assertTrue(invalid.done.await(5, TimeUnit.SECONDS));
        }
        TestSubscriber valid = new TestSubscriber();
        local.subscribe("subscription { ticks(count: 1) }", null, null).subscribe(valid);
        valid.subscription.request(Long.MAX_VALUE);
        assertEquals(Map.of("ticks", 1), valid.next().getData());
    }

    @Test
    void clientSignalsTransportErrors() throws Exception {
        for (String path : List.of("/html", "/truncated")) {
            GraphqlClientAdapter graphql = graphql(path);
            graphql.start();
            TestSubscriber subscriber = new TestSubscriber();
            graphql.subscribe("subscription { ticks(count: 1) }", null, null).subscribe(subscriber);
            subscriber.subscription.request(Long.MAX_VALUE);
            assertTrue(subscriber.done.await(5, TimeUnit.SECONDS), path);
            assertInstanceOf(GraphqlTransportException.class, subscriber.error.get(), path);
        }

        GraphqlClientAdapter limited = graphql("/graphql");
        limited.setMaxResponseBytes(1024);
        limited.start();
        TestSubscriber large = new TestSubscriber();
        limited.subscribe(
                        "query($n: String) { hello(name: $n) }",
                        null,
                        Map.of("n", "x".repeat(20_000)))
                .subscribe(large);
        large.subscription.request(Long.MAX_VALUE);
        assertTrue(large.done.await(5, TimeUnit.SECONDS));
        assertInstanceOf(GraphqlTransportException.class, large.error.get());
    }

    @Test
    void clientRejectsNonPositiveDemand() throws Exception {
        GraphqlClientAdapter graphql = graphql("/graphql");
        graphql.start();
        TestSubscriber subscriber = new TestSubscriber();
        graphql.subscribe("subscription { ticks(count: 1) }", null, null).subscribe(subscriber);
        subscriber.subscription.request(0);
        assertTrue(subscriber.done.await(5, TimeUnit.SECONDS));
        assertInstanceOf(IllegalArgumentException.class, subscriber.error.get());
    }

    @Test
    void configurationIsValidated() {
        GraphqlServerAdapter adapter = new GraphqlServerAdapter();
        assertThrows(IllegalArgumentException.class, () -> adapter.setMaxSubscriptions(0));
        assertThrows(IllegalArgumentException.class, () -> adapter.setMaxQueuedBytes(0));
        assertThrows(
                IllegalArgumentException.class, () -> adapter.setHeartbeatInterval(Duration.ZERO));
        assertEquals(GraphqlServerAdapter.DEFAULT_MAX_SUBSCRIPTIONS, adapter.getMaxSubscriptions());
        assertEquals(GraphqlServerAdapter.DEFAULT_MAX_QUEUED_BYTES, adapter.getMaxQueuedBytes());
        assertEquals(
                GraphqlServerAdapter.DEFAULT_HEARTBEAT_INTERVAL, adapter.getHeartbeatInterval());
        assertThrows(
                UnsupportedOperationException.class,
                () -> sse.getSubscriptionTransports().remove(GraphqlTransport.SSE));
    }

    /** Collects the signals of a subscription. */
    private static final class TestSubscriber implements Subscriber<ExecutionResult> {

        final BlockingQueue<ExecutionResult> items = new LinkedBlockingQueue<>();

        final CountDownLatch done = new CountDownLatch(1);

        final AtomicReference<Throwable> error = new AtomicReference<>();

        volatile Subscription subscription;

        @Override
        public void onSubscribe(Subscription subscription) {
            this.subscription = subscription;
        }

        @Override
        public void onNext(ExecutionResult item) {
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

        ExecutionResult next() throws InterruptedException {
            ExecutionResult item = items.poll(5, TimeUnit.SECONDS);
            assertTrue(item != null, "Timed out waiting for a result");
            return item;
        }
    }
}
