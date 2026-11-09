/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.mcp;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.restlet.ext.mcp.internal.McpWorkers;

/** The admission workers shared by the MCP transports. */
class McpWorkersTestCase {

    /** A call chained on the end of another is admitted, with a single slot. */
    @Test
    void aChainedCallIsAdmittedWithOneSlot() throws Exception {
        McpWorkers workers = new McpWorkers(1, Duration.ofSeconds(5));
        AtomicInteger calls = new AtomicInteger();
        try {
            assertDoesNotThrow(
                    () ->
                            workers.submit(calls::incrementAndGet, () -> {})
                                    .then(workers.submit(calls::incrementAndGet, () -> {}))
                                    .block(Duration.ofSeconds(5)));
            assertEquals(2, calls.get());
        } finally {
            workers.close();
        }
    }

    /** A call is running until the subscribers of its end are done. */
    @Test
    void awaitIdleWaitsForTheSubscribersOfTheEnd() throws Exception {
        McpWorkers workers = new McpWorkers(1, Duration.ofSeconds(5));
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        try {
            workers.submit(() -> {}, () -> {})
                    .doOnSuccess(
                            ignored -> {
                                entered.countDown();
                                try {
                                    release.await(5, TimeUnit.SECONDS);
                                } catch (InterruptedException _) {
                                    Thread.currentThread().interrupt();
                                }
                            })
                    .subscribe();
            assertTrue(entered.await(5, TimeUnit.SECONDS));

            assertFalse(workers.awaitIdle(Duration.ofMillis(50)));
            release.countDown();
            assertTrue(workers.awaitIdle(Duration.ofSeconds(5)));
        } finally {
            workers.close();
        }
    }
}
