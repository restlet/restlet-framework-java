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
import java.util.Objects;
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
    private final int maxCalls;

    /** The time limit of each call, or null for none. */
    private final Duration timeout;

    /** How long {@link #close()} waits for the calls to end. */
    private final Duration closeTimeout;

    private boolean closed;

    public McpWorkers(int maxCalls, Duration timeout) {
        this(maxCalls, Objects.requireNonNull(timeout, "timeout"), timeout);
    }

    /**
     * Creates workers whose calls may have no time limit, such as long-lived streams.
     *
     * @param maxCalls The number of concurrent calls.
     * @param timeout The time limit of each call, or null for none.
     * @param closeTimeout How long {@link #close()} waits for the calls to end.
     */
    public McpWorkers(int maxCalls, Duration timeout, Duration closeTimeout) {
        if (maxCalls <= 0
                || (timeout != null && (timeout.isZero() || timeout.isNegative()))
                || closeTimeout.isZero()
                || closeTimeout.isNegative()) {
            throw new IllegalArgumentException("Positive call limit and timeouts required");
        }
        this.admission = new Semaphore(maxCalls);
        this.maxCalls = maxCalls;
        this.timeout = timeout == null ? null : Duration.ofNanos(timeout.toNanos());
        this.closeTimeout = Duration.ofNanos(closeTimeout.toNanos());
    }

    @FunctionalInterface
    public interface Operation {
        void run() throws Exception;
    }

    public Mono<Void> submit(Operation operation, Runnable cancel) {
        Mono<Void> call =
                Mono.<Void>create(
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
                        });
        return timeout == null ? call : call.timeout(timeout);
    }

    /**
     * Waits for the running calls to end, without refusing new ones.
     *
     * @param wait How long to wait at most.
     * @return True if no call was running.
     * @throws InterruptedException If interrupted while waiting.
     */
    public boolean awaitIdle(Duration wait) throws InterruptedException {
        if (!admission.tryAcquire(maxCalls, wait.toNanos(), TimeUnit.NANOSECONDS)) {
            return false;
        }
        admission.release(maxCalls);
        return true;
    }

    public void close() throws IOException {
        synchronized (this) {
            closed = true;
            executor.shutdownNow();
        }
        try {
            if (!executor.awaitTermination(closeTimeout.toNanos(), TimeUnit.NANOSECONDS)) {
                throw new IOException("MCP worker shutdown timed out");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted awaiting MCP worker shutdown", e);
        }
    }
}
