/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.mcp.internal;

import io.modelcontextprotocol.modern.server.feature.McpChangeBroadcaster;
import io.modelcontextprotocol.modern.server.feature.McpChangePublisher;
import io.modelcontextprotocol.modern.server.feature.ServerChange;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.restlet.ext.mcp.McpNotification;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

/**
 * The changes of an adapter, kept for each listening stream until it can take them, so that the
 * publisher is never held by a slow stream. A stream read through an exchange takes a change when
 * fewer than {@link McpStreamGate#MAX_IN_FLIGHT} of its changes are on their way to the client, and
 * only keeps the changes its acknowledged subscriptions let through; otherwise it takes what its
 * subscriber asks for. A pending change is replaced in place by a later one of the same coalescing
 * identity: an {@link McpNotification} with the same method, field, scope and {@link
 * McpNotification#key() key}, or another change equal to it, such as a list change. Past {@link
 * #MAX_PENDING}, the oldest pending change is dropped. Internal to the MCP extension.
 */
public final class McpCoalescingFeed implements McpChangePublisher, McpChangeBroadcaster {
    private static final Logger LOGGER = Logger.getLogger(McpCoalescingFeed.class.getName());

    /** The pending changes a stream may hold. */
    public static final int MAX_PENDING = 1024;

    private final Set<Stream> streams = ConcurrentHashMap.newKeySet();

    private final Consumer<ServerChange> check;

    /** Creates a feed broadcasting any change. */
    public McpCoalescingFeed() {
        this(change -> {});
    }

    /**
     * Creates a feed checking each change before broadcasting it.
     *
     * @param check Throws if a change must not be broadcast.
     */
    public McpCoalescingFeed(Consumer<ServerChange> check) {
        this.check = Objects.requireNonNull(check, "check");
    }

    @Override
    public void broadcast(ServerChange change) {
        Objects.requireNonNull(change, "change");
        check.accept(change);
        for (Stream stream : streams) {
            stream.offer(change);
        }
    }

    @Override
    public Flux<ServerChange> changes() {
        return Flux.create(
                sink -> {
                    McpStreamGate gate = sink.contextView().getOrDefault(McpStreamGate.KEY, null);
                    Stream stream = new Stream(sink, gate);
                    streams.add(stream);
                    sink.onRequest(ignored -> stream.drain());
                    sink.onDispose(() -> streams.remove(stream));
                    if (gate != null) {
                        gate.onReady(stream::drain);
                        stream.drain();
                    }
                },
                FluxSink.OverflowStrategy.ERROR);
    }

    /** The identity under which a change replaces a pending one. */
    private static Object coalescing(ServerChange change) {
        if (change instanceof McpNotification notification) {
            return notification.key() == null
                    ? new Object()
                    : new Key(
                            notification.method(),
                            notification.field(),
                            notification.scope(),
                            notification.key());
        }
        // The SDK's changes are records: an equal one says the same.
        return change;
    }

    /** One listening stream's pending changes. */
    private static final class Stream {
        private final FluxSink<ServerChange> sink;
        private final McpStreamGate gate;
        private final Map<Object, ServerChange> pending = new LinkedHashMap<>();
        private final AtomicInteger draining = new AtomicInteger();

        private Stream(FluxSink<ServerChange> sink, McpStreamGate gate) {
            this.sink = sink;
            this.gate = gate;
        }

        private void offer(ServerChange change) {
            if (gate != null && !gate.admits(change)) {
                return;
            }
            Object key = coalescing(change);
            synchronized (pending) {
                // A replaced change keeps its place: the stream still sees changes in order.
                if (pending.replace(key, change) == null) {
                    if (pending.size() >= MAX_PENDING) {
                        Iterator<ServerChange> oldest = pending.values().iterator();
                        LOGGER.log(
                                Level.WARNING,
                                "Dropping the oldest MCP change for a stream too slow: {0}",
                                oldest.next().method());
                        oldest.remove();
                    }
                    pending.put(key, change);
                }
            }
            drain();
        }

        /** Whether the stream can take one more change now. */
        private boolean ready() {
            return sink.requestedFromDownstream() > 0
                    && !sink.isCancelled()
                    && (gate == null || gate.isAcknowledged());
        }

        /** Emits what the stream can take, one drainer at a time. */
        private void drain() {
            if (draining.getAndIncrement() != 0) {
                return;
            }
            int missed = 1;
            do {
                while (ready()) {
                    ServerChange next;
                    synchronized (pending) {
                        next = take();
                    }
                    if (next == null) {
                        break;
                    }
                    sink.next(next);
                }
                missed = draining.addAndGet(-missed);
            } while (missed != 0);
        }

        /**
         * Takes the next pending change the stream admits, dropping those queued before its
         * acknowledgment that it refuses, or null if none or no room.
         */
        private ServerChange take() {
            for (Iterator<ServerChange> changes = pending.values().iterator();
                    changes.hasNext(); ) {
                ServerChange next = changes.next();
                if (gate != null && !gate.admits(next)) {
                    changes.remove();
                } else if (gate == null || gate.tryAcquire()) {
                    changes.remove();
                    return next;
                } else {
                    return null;
                }
            }
            return null;
        }
    }

    /** A coalescing key, apart from the unkeyed notifications' own identities. */
    private record Key(String method, String field, Object scope, Object value) {}
}
