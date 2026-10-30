/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.mcp.internal;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import reactor.core.publisher.Mono;

/** Finite admission before submission, without a task queue or shared global scheduler. */
public final class McpWorkers {
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    private final Semaphore admission;
    private final Duration timeout;
    private boolean closed;

    public McpWorkers(int maxCalls, Duration timeout) {
        if (maxCalls <= 0 || timeout.isZero() || timeout.isNegative()) {
            throw new IllegalArgumentException("Positive call limit and timeout required");
        }
        this.admission = new Semaphore(maxCalls);
        this.timeout = Duration.ofNanos(timeout.toNanos());
    }

    @FunctionalInterface
    public interface Operation {
        void run() throws Exception;
    }

    public Mono<Void> submit(Operation operation, Runnable cancel) {
        return Mono.<Void>create(
                        sink -> {
                            AtomicReference<Thread> worker = new AtomicReference<>();
                            AtomicBoolean cancelled = new AtomicBoolean();
                            sink.onCancel(
                                    () -> {
                                        cancelled.set(true);
                                        cancel.run();
                                        Thread thread = worker.get();
                                        if (thread != null) {
                                            thread.interrupt();
                                        }
                                    });
                            synchronized (this) {
                                if (closed || !admission.tryAcquire()) {
                                    sink.error(
                                            new IOException(
                                                    "MCP transport closed or call limit exceeded"));
                                    return;
                                }
                                executor.execute(
                                        () -> {
                                            Thread thread = Thread.currentThread();
                                            worker.set(thread);
                                            Exception error = null;
                                            try {
                                                if (cancelled.get()) {
                                                    return;
                                                }
                                                operation.run();
                                            } catch (Exception e) {
                                                error = e;
                                            } finally {
                                                admission.release();
                                            }
                                            if (!cancelled.get()) {
                                                if (error == null) {
                                                    sink.success();
                                                } else {
                                                    sink.error(error);
                                                }
                                            }
                                        });
                            }
                        })
                .timeout(timeout);
    }

    public void close() throws IOException {
        synchronized (this) {
            closed = true;
            executor.shutdownNow();
        }
        try {
            if (!executor.awaitTermination(timeout.toNanos(), TimeUnit.NANOSECONDS)) {
                throw new IOException("MCP worker shutdown timed out");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted awaiting MCP worker shutdown", e);
        }
    }
}
