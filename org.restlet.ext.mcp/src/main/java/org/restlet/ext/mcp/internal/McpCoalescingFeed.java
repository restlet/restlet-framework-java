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
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.restlet.ext.mcp.McpNotification;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

/**
 * The changes of an adapter, kept for each listening stream until it asks for them, so that the
 * publisher is never held by a slow stream: a change whose {@link McpNotification#key() key} is
 * already pending replaces it, and past a bound the oldest unkeyed change is dropped. Internal to
 * the MCP extension.
 */
public final class McpCoalescingFeed implements McpChangePublisher, McpChangeBroadcaster {
    private static final Logger LOGGER = Logger.getLogger(McpCoalescingFeed.class.getName());

    /** The pending changes a stream may hold. */
    public static final int MAX_PENDING = 1024;

    private final Set<Stream> streams = ConcurrentHashMap.newKeySet();

    @Override
    public void broadcast(ServerChange change) {
        for (Stream stream : streams) {
            stream.offer(change);
        }
    }

    @Override
    public Flux<ServerChange> changes() {
        return Flux.create(
                sink -> {
                    Stream stream = new Stream(sink);
                    streams.add(stream);
                    sink.onRequest(ignored -> stream.drain());
                    sink.onDispose(() -> streams.remove(stream));
                },
                FluxSink.OverflowStrategy.ERROR);
    }

    /** One listening stream's pending changes. */
    private static final class Stream {
        private final FluxSink<ServerChange> sink;
        private final Map<Object, ServerChange> pending = new LinkedHashMap<>();
        private final AtomicInteger draining = new AtomicInteger();

        private Stream(FluxSink<ServerChange> sink) {
            this.sink = sink;
        }

        private void offer(ServerChange change) {
            Object key =
                    change instanceof McpNotification notification && notification.key() != null
                            ? new Key(notification.key())
                            : new Object();
            synchronized (pending) {
                // A replaced change keeps its place: the stream still sees changes in order.
                if (pending.replace(key, change) == null) {
                    if (pending.size() >= MAX_PENDING && !dropOldestUnkeyed()) {
                        LOGGER.log(
                                Level.WARNING,
                                "Dropping an MCP change for a stream too slow: {0}",
                                change.method());
                        return;
                    }
                    pending.put(key, change);
                }
            }
            drain();
        }

        private boolean dropOldestUnkeyed() {
            for (Iterator<Object> keys = pending.keySet().iterator(); keys.hasNext(); ) {
                if (!(keys.next() instanceof Key)) {
                    keys.remove();
                    LOGGER.warning("Dropping the oldest MCP change for a stream too slow");
                    return true;
                }
            }
            return false;
        }

        /** Emits what the stream asked for, one drainer at a time. */
        private void drain() {
            if (draining.getAndIncrement() != 0) {
                return;
            }
            int missed = 1;
            do {
                while (sink.requestedFromDownstream() > 0 && !sink.isCancelled()) {
                    ServerChange next;
                    synchronized (pending) {
                        Iterator<ServerChange> changes = pending.values().iterator();
                        if (!changes.hasNext()) {
                            break;
                        }
                        next = changes.next();
                        changes.remove();
                    }
                    sink.next(next);
                }
                missed = draining.addAndGet(-missed);
            } while (missed != 0);
        }
    }

    /** A coalescing key, apart from the unkeyed changes' own identities. */
    private record Key(Object value) {}
}
