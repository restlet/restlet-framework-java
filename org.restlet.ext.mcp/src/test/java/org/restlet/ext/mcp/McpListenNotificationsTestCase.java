/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.mcp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.modelcontextprotocol.json.McpJsonDefaults;
import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.json.TypeRef;
import io.modelcontextprotocol.modern.McpSchema;
import io.modelcontextprotocol.modern.McpSchema.MetaKeys;
import io.modelcontextprotocol.modern.McpSchema.SubscriptionFilter;
import io.modelcontextprotocol.modern.server.feature.ServerChange;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.ServerSentEvent;
import org.restlet.ext.mcp.internal.McpCoalescingFeed;
import org.restlet.representation.EventStreamReader;
import org.restlet.representation.StringRepresentation;
import org.restlet.security.User;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Mono;

/**
 * Application notifications on {@code subscriptions/listen} streams: the subscription fields the
 * application honours, scoped notifications, the subscription restriction, and the coalescing of a
 * slow stream's pending changes.
 */
class McpListenNotificationsTestCase {
    private static final McpJsonMapper MAPPER = McpJsonDefaults.getMapper();
    private static final String MODERN = McpProtocol.MODERN_VERSION;
    private static final String JOBS = "com.example/jobs";

    private final List<McpServerAdapter> adapters = new ArrayList<>();

    @AfterEach
    void cleanup() throws Exception {
        for (McpServerAdapter adapter : adapters) {
            adapter.stop();
        }
    }

    /** A stream asking for an honoured field receives the notifications published on it. */
    @Test
    void anHonouredFieldCarriesTheApplicationsNotifications() throws Exception {
        McpServerAdapter adapter = adapter();
        adapter.getSubscriptions().add(McpSubscription.of(JOBS));
        adapter.start();

        EventStreamReader reader = listen(adapter, Map.of(JOBS, true), null);
        Map<String, Object> ack = next(reader);
        assertEquals(Map.of(JOBS, true), honoured(ack));

        adapter.getChanges()
                .broadcast(McpNotification.of("notifications/jobs", Map.of("id", "j-1"), JOBS));
        Map<String, Object> notification = next(reader);
        assertEquals("notifications/jobs", notification.get("method"));
        assertEquals("j-1", ((Map<?, ?>) notification.get("params")).get("id"));
    }

    /** A field the application doesn't honour, or the stream didn't ask for, carries nothing. */
    @Test
    void onlyAskedAndHonouredFieldsCarryNotifications() throws Exception {
        McpServerAdapter adapter = adapter();
        McpServerAdapterTestCase.addEcho(adapter);
        adapter.getSubscriptions().add(McpSubscription.of(JOBS));
        adapter.start();

        EventStreamReader reader =
                listen(adapter, Map.of("toolsListChanged", true, "com.example/other", true), null);
        assertEquals(Map.of("toolsListChanged", true), honoured(next(reader)));

        adapter.getChanges()
                .broadcast(McpNotification.of("notifications/jobs", Map.of("id", "j-1"), JOBS));
        adapter.getChanges()
                .broadcast(
                        McpNotification.of("notifications/other", Map.of(), "com.example/other"));
        adapter.getChanges().broadcast(new ServerChange.ToolsListChanged());
        assertEquals(McpSchema.METHOD_NOTIFICATION_TOOLS_LIST_CHANGED, next(reader).get("method"));
    }

    /** A notification scoped to a principal reaches only that principal's streams. */
    @Test
    void aScopedNotificationReachesOnlyItsPrincipal() throws Exception {
        McpServerAdapter adapter = adapter();
        McpServerAdapterTestCase.addEcho(adapter);
        adapter.getSubscriptions().add(McpSubscription.scopedToPrincipal(JOBS));
        adapter.start();

        EventStreamReader alice =
                listen(adapter, Map.of(JOBS, true, "toolsListChanged", true), "alice");
        EventStreamReader bob =
                listen(adapter, Map.of(JOBS, true, "toolsListChanged", true), "bob");
        Object aliceScope = honoured(next(alice)).get(JOBS);
        assertEquals(McpSubscription.principalScope("alice"), aliceScope);
        assertNotEquals(aliceScope, honoured(next(bob)).get(JOBS));

        adapter.getChanges()
                .broadcast(
                        McpNotification.of("notifications/jobs", Map.of("id", "j-1"), JOBS)
                                .scopedTo(McpSubscription.principalScope("alice")));
        adapter.getChanges().broadcast(new ServerChange.ToolsListChanged());

        assertEquals("notifications/jobs", next(alice).get("method"));
        assertEquals(McpSchema.METHOD_NOTIFICATION_TOOLS_LIST_CHANGED, next(alice).get("method"));
        assertEquals(McpSchema.METHOD_NOTIFICATION_TOOLS_LIST_CHANGED, next(bob).get("method"));
    }

    /** The subscription restriction narrows what a caller receives. */
    @Test
    void theRestrictionNarrowsTheSubscriptions() throws Exception {
        McpServerAdapter adapter = adapter();
        McpServerAdapterTestCase.addEcho(adapter);
        adapter.getSubscriptions().add(McpSubscription.of(JOBS));
        adapter.setSubscriptionRestriction(
                (ctx, requested, honoured) ->
                        Mono.just(
                                SubscriptionFilter.of(
                                        Map.of(SubscriptionFilter.TOOLS_LIST_CHANGED, true))));
        adapter.start();

        EventStreamReader reader =
                listen(adapter, Map.of(JOBS, true, "toolsListChanged", true), null);
        assertEquals(Map.of("toolsListChanged", true), honoured(next(reader)));
    }

    /** A field MCP defines, or one honoured twice, is refused at start. */
    @Test
    void coreAndDuplicateFieldsAreRefused() {
        McpServerAdapter core = adapter();
        core.getSubscriptions().add(McpSubscription.of(SubscriptionFilter.TOOLS_LIST_CHANGED));
        assertThrows(IllegalArgumentException.class, core::start);

        McpServerAdapter twice = adapter();
        twice.getSubscriptions().add(McpSubscription.of(JOBS));
        twice.getSubscriptions().add(McpSubscription.scopedToPrincipal(JOBS));
        assertThrows(IllegalArgumentException.class, twice::start);
    }

    /**
     * A slow stream keeps the latest change per key, in the place of the first, and changes without
     * a key in order; past the bound, the oldest change without a key goes.
     */
    @Test
    void aSlowStreamKeepsTheLatestChangePerKey() {
        McpCoalescingFeed feed = new McpCoalescingFeed();
        List<ServerChange> received = new ArrayList<>();
        BaseSubscriber<ServerChange> slow =
                new BaseSubscriber<>() {
                    @Override
                    protected void hookOnSubscribe(org.reactivestreams.Subscription subscription) {
                        // Asks for nothing yet.
                    }

                    @Override
                    protected void hookOnNext(ServerChange value) {
                        received.add(value);
                    }
                };
        feed.changes().subscribe(slow);

        McpNotification working = state("t-1", "working");
        feed.broadcast(working);
        feed.broadcast(McpNotification.of("notifications/jobs", Map.of(), JOBS));
        feed.broadcast(state("t-1", "input_required"));
        McpNotification completed = state("t-1", "completed");
        feed.broadcast(completed);
        // Exactly what is pending: no demand is left for what follows.
        slow.request(2);

        assertEquals(2, received.size(), received.toString());
        assertEquals(completed, received.get(0));
        assertEquals("notifications/jobs", received.get(1).method());

        received.clear();
        for (int i = 0; i < McpCoalescingFeed.MAX_PENDING + 5; i++) {
            feed.broadcast(McpNotification.of("notifications/jobs", Map.of("n", i), JOBS));
        }
        slow.request(Long.MAX_VALUE);
        assertEquals(McpCoalescingFeed.MAX_PENDING, received.size());
        assertEquals(5, ((McpNotification) received.get(0)).params().get("n"));
        slow.dispose();
        feed.broadcast(working);
        assertEquals(McpCoalescingFeed.MAX_PENDING, received.size());
    }

    /** Notifications and subscriptions check their arguments and how they match a filter. */
    @Test
    void notificationsAndSubscriptionsCheckTheirArguments() {
        assertThrows(IllegalArgumentException.class, () -> McpNotification.of(" ", Map.of(), JOBS));
        assertThrows(
                IllegalArgumentException.class, () -> McpNotification.of("n/x", Map.of(), " "));
        assertThrows(NullPointerException.class, () -> McpNotification.of("n/x", null, JOBS));
        assertThrows(IllegalArgumentException.class, () -> McpSubscription.of(" "));
        assertThrows(NullPointerException.class, () -> new McpSubscription(JOBS, null));

        McpNotification any = McpNotification.of("n/x", Map.of(), JOBS);
        assertTrue(any.isHonouredBy(SubscriptionFilter.of(Map.of(JOBS, true))));
        assertFalse(any.isHonouredBy(SubscriptionFilter.of(Map.of(JOBS, false))));
        assertFalse(any.isHonouredBy(SubscriptionFilter.EMPTY));
        McpNotification scoped = any.scopedTo("s1");
        assertTrue(scoped.isHonouredBy(SubscriptionFilter.of(Map.of(JOBS, "s1"))));
        assertFalse(scoped.isHonouredBy(SubscriptionFilter.of(Map.of(JOBS, "s2"))));
        assertNotEquals(McpSubscription.principalScope(null), McpSubscription.principalScope(""));
    }

    private static McpNotification state(String taskId, String status) {
        return McpNotification.of(
                        "notifications/tasks", Map.of("taskId", taskId, "status", status), JOBS)
                .coalescedBy(taskId);
    }

    private McpServerAdapter adapter() {
        McpServerAdapter adapter = new McpServerAdapter();
        adapters.add(adapter);
        return adapter;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> honoured(Map<String, Object> ack) {
        assertEquals(
                McpSchema.METHOD_NOTIFICATION_SUBSCRIPTIONS_ACKNOWLEDGED,
                ack.get("method"),
                ack.toString());
        return (Map<String, Object>) ((Map<?, ?>) ack.get("params")).get("notifications");
    }

    /** Opens a listen stream, written in the background to be read event by event. */
    private static EventStreamReader listen(
            McpServerAdapter adapter, Map<String, Object> notifications, String user)
            throws Exception {
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put(MetaKeys.PROTOCOL_VERSION, MODERN);
        meta.put(MetaKeys.CLIENT_CAPABILITIES, Map.of());
        Map<String, Object> envelope = new LinkedHashMap<>();
        envelope.put("jsonrpc", "2.0");
        envelope.put("id", 1);
        envelope.put("method", McpSchema.METHOD_SUBSCRIPTIONS_LISTEN);
        envelope.put("params", Map.of("notifications", notifications, "_meta", meta));
        Request request =
                new Request(
                        Method.POST,
                        "http://localhost/mcp",
                        new StringRepresentation(
                                MAPPER.writeValueAsString(envelope), MediaType.APPLICATION_JSON));
        request.getHeaders().set(McpProtocol.VERSION_HEADER, MODERN);
        request.getHeaders().set("Accept", "application/json, text/event-stream");
        request.getHeaders().set("Mcp-Method", McpSchema.METHOD_SUBSCRIPTIONS_LISTEN);
        if (user != null) {
            request.getClientInfo().setUser(new User(user));
        }
        Response response = adapter.handle(request);
        assertEquals(MediaType.TEXT_EVENT_STREAM, response.getEntity().getMediaType());
        var input = new java.io.PipedInputStream(1 << 16);
        var output = new java.io.PipedOutputStream(input);
        Thread.ofVirtual()
                .start(
                        () -> {
                            try (output) {
                                response.getEntity().write(output);
                            } catch (java.io.IOException _) {
                                // The reader went away.
                            }
                        });
        return new EventStreamReader(input);
    }

    /** The next message of a stream, skipping heartbeats. */
    private static Map<String, Object> next(EventStreamReader reader) throws Exception {
        return CompletableFuture.supplyAsync(
                        () -> {
                            try {
                                for (ServerSentEvent event = reader.readEvent();
                                        event != null;
                                        event = reader.readEvent()) {
                                    if (event.getData() != null && !event.getData().isEmpty()) {
                                        return MAPPER.readValue(
                                                event.getData(),
                                                new TypeRef<Map<String, Object>>() {});
                                    }
                                }
                                throw new IllegalStateException("The stream ended");
                            } catch (java.io.IOException e) {
                                throw new IllegalStateException(e);
                            }
                        })
                .get(10, TimeUnit.SECONDS);
    }
}
