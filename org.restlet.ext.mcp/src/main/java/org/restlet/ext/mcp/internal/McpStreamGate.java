/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.mcp.internal;

import io.modelcontextprotocol.modern.McpSchema.SubscriptionFilter;
import io.modelcontextprotocol.modern.server.feature.ServerChange;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * What a {@code subscriptions/listen} stream's transport tells the {@link McpCoalescingFeed}, which
 * the SDK can't: the subscriptions the stream was acknowledged with, and how many of its changes
 * are still on their way to the client. The SDK queues a stream's notifications without bound and
 * keeps asking for more, so a stream's backlog is only seen where its events are written; the
 * exchange puts the gate in the Reactor context of the stream it reads. Internal to the MCP
 * extension.
 */
public final class McpStreamGate {
    /** The Reactor context key of a stream's gate. */
    public static final Object KEY = McpStreamGate.class;

    /** The changes a stream may have on their way to the client before the feed holds the next. */
    public static final int MAX_IN_FLIGHT = 16;

    private final AtomicInteger inFlight = new AtomicInteger();
    private volatile SubscriptionFilter honoured;
    private volatile Runnable wake = () -> {};

    /**
     * Records the subscriptions the stream was acknowledged with, before the acknowledgment is
     * queued for writing.
     *
     * @param filter The honoured subscriptions.
     */
    public void acknowledged(SubscriptionFilter filter) {
        // The acknowledgment's own write is counted like a change's.
        inFlight.incrementAndGet();
        honoured = filter;
        wake.run();
    }

    /** Records that one of the stream's events was written to the client. */
    public void written() {
        if (inFlight.decrementAndGet() < MAX_IN_FLIGHT) {
            wake.run();
        }
    }

    /**
     * Whether a change may reach the stream at all: once acknowledged, only those its honoured
     * subscriptions let through, as the SDK filters them.
     *
     * @param change The change.
     * @return False if the stream was acknowledged and its subscriptions refuse the change.
     */
    boolean admits(ServerChange change) {
        SubscriptionFilter filter = honoured;
        return filter == null || change.isHonouredBy(filter);
    }

    /** Whether the stream was acknowledged. */
    boolean isAcknowledged() {
        return honoured != null;
    }

    /** Takes a slot for one more change in flight, if the stream was acknowledged and has room. */
    boolean tryAcquire() {
        // Only the stream's single drainer acquires, so the check and the increment don't race.
        if (honoured == null || inFlight.get() >= MAX_IN_FLIGHT) {
            return false;
        }
        inFlight.incrementAndGet();
        return true;
    }

    /** Sets what to run when the stream may take more changes. */
    void onReady(Runnable action) {
        wake = action;
    }
}
