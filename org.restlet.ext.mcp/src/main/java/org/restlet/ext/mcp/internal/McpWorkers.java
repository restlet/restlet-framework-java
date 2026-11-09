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
import reactor.core.publisher.MonoSink;

/** Finite admission before submission, without a task queue or shared global scheduler. */
public final class McpWorkers {
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    private final Semaphore admission;

    /** Guards {@link #running}, and signals when it drops to zero. */
    private final Object idle = new Object();

    /**
     * The calls admitted and not yet done signalling their end. A call frees its admission permit
     * before it signals, so that a call chained on its end is admitted, but counts here until its
     * subscribers are done.
     */
    private int running;

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
        this.timeout = timeout == null ? null : Duration.ofNanos(timeout.toNanos());
        this.closeTimeout = Duration.ofNanos(closeTimeout.toNanos());
    }

    @FunctionalInterface
    public interface Operation {
        void run() throws Exception;
    }

    public Mono<Void> submit(Operation operation, Runnable cancel) {
        Mono<Void> call = Mono.<Void>create(sink -> admit(sink, operation, cancel));
        return timeout == null ? call : call.timeout(timeout);
    }

    /** Admits a call, or refuses it at once; cancelling it interrupts its worker. */
    private void admit(MonoSink<Void> sink, Operation operation, Runnable cancel) {
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
                sink.error(new IOException("MCP transport closed or call limit exceeded"));
                return;
            }
            synchronized (idle) {
                running++;
            }
            executor.execute(() -> work(sink, operation, worker, cancelled));
        }
    }

    /**
     * Runs an admitted call and signals its end. The permit is released before the signal, so a
     * call its subscribers chain on its end is admitted; the call still counts as running until
     * they are done, so a caller of {@link #awaitIdle(Duration)} sees what they did.
     */
    private void work(
            MonoSink<Void> sink,
            Operation operation,
            AtomicReference<Thread> worker,
            AtomicBoolean cancelled) {
        worker.set(Thread.currentThread());
        Throwable error = null;
        try {
            boolean ran = false;
            try {
                if (!cancelled.get()) {
                    ran = true;
                    operation.run();
                }
            } catch (Exception | Error e) {
                // An Error, such as a failed assertion, ends the call too, and is rethrown.
                error = e;
            } finally {
                admission.release();
            }
            if (ran && !cancelled.get()) {
                if (error == null) {
                    sink.success();
                } else {
                    sink.error(error);
                }
            }
        } finally {
            synchronized (idle) {
                if (--running == 0) {
                    idle.notifyAll();
                }
            }
        }
        if (error instanceof Error fatal) {
            throw fatal;
        }
    }

    /**
     * Waits for the running calls to end, without refusing new ones.
     *
     * @param wait How long to wait at most.
     * @return True if no call was running.
     * @throws InterruptedException If interrupted while waiting.
     */
    public boolean awaitIdle(Duration wait) throws InterruptedException {
        long deadline = System.nanoTime() + wait.toNanos();
        synchronized (idle) {
            while (running > 0) {
                long remaining = deadline - System.nanoTime();
                if (remaining <= 0) {
                    return false;
                }
                TimeUnit.NANOSECONDS.timedWait(idle, remaining);
            }
            return true;
        }
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
