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
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.restlet.Client;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Protocol;
import org.restlet.ext.websocket.WebSocketUpgrade;
import org.restlet.routing.Filter;

/** {@link GraphqlClientAdapter} subscriptions over {@code graphql-transport-ws}. */
class GraphqlWebSocketClientTestCase {

    private final AtomicInteger upgrades = new AtomicInteger();

    private Component component;

    private GraphqlServerAdapter server;

    private Client client;

    private String base;

    @BeforeEach
    void setUp() throws Exception {
        component = new Component();
        component.getServers().add(Protocol.HTTP, 0);
        server = new GraphqlServerAdapter();
        server.setGraphQL(GraphqlTestSchemas.graphQL(new AtomicInteger()));
        server.getSubscriptionTransports().add(GraphqlTransport.WEBSOCKET);
        Filter counter =
                new Filter() {
                    @Override
                    protected int beforeHandle(Request request, Response response) {
                        if (WebSocketUpgrade.isRequested(request)) {
                            upgrades.incrementAndGet();
                        }
                        return CONTINUE;
                    }
                };
        counter.setNext(server);
        component.getDefaultHost().attach("/graphql", counter);

        GraphqlServerAdapter httpOnly = new GraphqlServerAdapter();
        httpOnly.setGraphQL(GraphqlTestSchemas.graphQL(new AtomicInteger()));
        component.getDefaultHost().attach("/http", httpOnly);
        component.start();
        base = "http://localhost:" + component.getServers().get(0).getActualPort();

        // The WebSocket client borrows the event loop of a client having a context.
        client = new Client(new Context(), Protocol.HTTP);
        client.start();
    }

    @AfterEach
    void tearDown() throws Exception {
        client.stop();
        component.stop();
    }

    private GraphqlClientAdapter graphql(String path) {
        GraphqlClientAdapter graphql = new GraphqlClientAdapter();
        graphql.setClient(client);
        graphql.setEndpoint(base + path);
        graphql.setSubscriptionTransport(GraphqlTransport.WEBSOCKET);
        return graphql;
    }

    private static void await(BooleanSupplier condition) throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (!condition.getAsBoolean()) {
            assertTrue(System.nanoTime() < deadline, "Timed out");
            Thread.sleep(10);
        }
    }

    @Test
    void subscriptionsShareOneConnectionAndHonorDemand() throws Exception {
        GraphqlClientAdapter graphql = graphql("/graphql");
        graphql.setConnectionInitPayload(Map.of("token", "secret"));
        graphql.setRequestCustomizer(request -> request.getHeaders().add("X-Api-Key", "k1"));
        graphql.start();

        TestSubscriber ticks = new TestSubscriber();
        graphql.subscribe("subscription { ticks(count: 3) }", null, null).subscribe(ticks);
        ticks.subscription.request(1);
        assertEquals(Map.of("ticks", 1), ticks.next().getData());
        Thread.sleep(100);
        assertTrue(ticks.items.isEmpty(), "No result without demand");
        ticks.subscription.request(2);
        assertEquals(Map.of("ticks", 2), ticks.next().getData());
        assertEquals(Map.of("ticks", 3), ticks.next().getData());
        assertTrue(ticks.done.await(5, TimeUnit.SECONDS));
        assertNull(ticks.error.get());

        TestSubscriber query = new TestSubscriber();
        graphql.subscribe("{ initToken header(name: \"X-Api-Key\") }", null, null).subscribe(query);
        query.subscription.request(Long.MAX_VALUE);
        assertEquals(Map.of("initToken", "secret", "header", "k1"), query.next().getData());
        assertTrue(query.done.await(5, TimeUnit.SECONDS));
        assertEquals(1, upgrades.get());
        graphql.stop();
    }

    @Test
    void cancellationCompletesTheServerSubscription() throws Exception {
        int cancelled = TickPublisher.CANCELLED.get();
        GraphqlClientAdapter graphql = graphql("/graphql");
        graphql.start();
        TestSubscriber first = new TestSubscriber();
        TestSubscriber second = new TestSubscriber();
        graphql.subscribe("subscription { idle }", null, null).subscribe(first);
        graphql.subscribe("subscription { idle }", null, null).subscribe(second);
        first.subscription.request(1);
        second.subscription.request(1);
        await(() -> server.getActiveSubscriptions() == 2);

        first.subscription.cancel();
        await(() -> server.getActiveSubscriptions() == 1);
        assertEquals(cancelled + 1, TickPublisher.CANCELLED.get());
        assertEquals(1, first.done.getCount());

        graphql.stop();
        assertTrue(second.done.await(5, TimeUnit.SECONDS));
        assertInstanceOf(GraphqlTransportException.class, second.error.get());
        await(() -> server.getActiveSubscriptions() == 0);
        assertTrue(client.isStarted(), "The borrowed client must survive stop()");
        assertThrows(
                IllegalStateException.class,
                () -> graphql.subscribe("subscription { idle }", null, null));
    }

    @Test
    void errorsBeforeExecutionAreResults() throws Exception {
        GraphqlClientAdapter graphql = graphql("/graphql");
        graphql.start();
        TestSubscriber subscriber = new TestSubscriber();
        graphql.subscribe("subscription { unknown }", null, null).subscribe(subscriber);
        subscriber.subscription.request(1);
        ExecutionResult result = subscriber.next();
        assertFalse(result.isDataPresent());
        assertFalse(result.getErrors().isEmpty());
        assertTrue(subscriber.done.await(5, TimeUnit.SECONDS));
        assertNull(subscriber.error.get());
        graphql.stop();
    }

    @Test
    void bufferedResultsAreBounded() throws Exception {
        int cancelled = TickPublisher.CANCELLED.get();
        GraphqlClientAdapter graphql = graphql("/graphql");
        graphql.setMaxResponseBytes(512);
        graphql.start();
        TestSubscriber subscriber = new TestSubscriber();
        graphql.subscribe("subscription { ticks(count: -1) }", null, null).subscribe(subscriber);
        subscriber.subscription.request(1);
        subscriber.next();
        assertTrue(subscriber.done.await(5, TimeUnit.SECONDS));
        assertInstanceOf(GraphqlTransportException.class, subscriber.error.get());
        await(() -> TickPublisher.CANCELLED.get() > cancelled);
        graphql.stop();
    }

    @Test
    void serverClosureFailsSubscriptions() throws Exception {
        GraphqlClientAdapter graphql = graphql("/graphql");
        graphql.start();
        TestSubscriber subscriber = new TestSubscriber();
        graphql.subscribe("subscription { idle }", null, null).subscribe(subscriber);
        subscriber.subscription.request(1);
        await(() -> server.getActiveSubscriptions() == 1);
        server.stop();
        assertTrue(subscriber.done.await(5, TimeUnit.SECONDS));
        GraphqlTransportException e =
                assertInstanceOf(GraphqlTransportException.class, subscriber.error.get());
        assertTrue(e.getMessage().contains("1001"), e.getMessage());
        graphql.stop();
    }

    @Test
    void endpointsWithoutWebSocketFail() throws Exception {
        GraphqlClientAdapter graphql = graphql("/http");
        graphql.start();
        TestSubscriber subscriber = new TestSubscriber();
        graphql.subscribe("subscription { idle }", null, null).subscribe(subscriber);
        subscriber.subscription.request(1);
        assertTrue(subscriber.done.await(5, TimeUnit.SECONDS));
        assertInstanceOf(GraphqlTransportException.class, subscriber.error.get());
        graphql.stop();
    }

    @Test
    void webSocketNeedsAStartedNettyClient() {
        GraphqlClientAdapter graphql = graphql("/graphql");
        graphql.setClient(new Client(Protocol.HTTP));
        assertThrows(IllegalStateException.class, graphql::start);
        assertEquals(GraphqlTransport.WEBSOCKET, graphql.getSubscriptionTransport());
        assertNull(graphql.getConnectionInitPayload());
        assertThrows(NullPointerException.class, () -> graphql.setSubscriptionTransport(null));
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
