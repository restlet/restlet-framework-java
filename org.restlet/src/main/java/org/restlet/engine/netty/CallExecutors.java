/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.netty;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Creates the executors that run Restlet calls off the Netty event loops. Restlet's programming
 * model is blocking, so a call must run on a thread that may block: by default one virtual thread
 * per call, or a bounded pool of platform threads when virtual threads are disabled.
 *
 * <p>This class is internal to the Netty connectors.
 */
final class CallExecutors {

    private CallExecutors() {}

    /**
     * Creates an executor for Restlet calls.
     *
     * @param name The thread name prefix.
     * @param virtual True for one virtual thread per task, false for a platform thread pool.
     * @param minThreads Core size of the platform pool, ignored for virtual threads.
     * @param maxThreads Maximum size of the platform pool, ignored for virtual threads.
     * @param idleTimeoutMs Idle timeout of platform threads, ignored for virtual threads.
     * @param priority Priority of platform threads, ignored for virtual threads.
     * @return A new executor.
     */
    public static ExecutorService create(
            String name,
            boolean virtual,
            int minThreads,
            int maxThreads,
            long idleTimeoutMs,
            int priority) {
        if (virtual) {
            ThreadFactory factory = Thread.ofVirtual().name(name + "-vt-", 0).factory();
            return Executors.newThreadPerTaskExecutor(factory);
        }

        int max = Math.max(1, maxThreads);
        int core = Math.max(1, Math.min(minThreads, max));
        AtomicInteger counter = new AtomicInteger();
        ThreadFactory factory =
                runnable -> {
                    Thread thread = new Thread(runnable, name + "-" + counter.incrementAndGet());
                    thread.setDaemon(true);
                    thread.setPriority(priority);
                    return thread;
                };
        // Grow to "max" threads before queueing: use a pool that never queues while it can grow,
        // then queues the overflow.
        ThreadPoolExecutor executor =
                new ThreadPoolExecutor(
                        max,
                        max,
                        Math.max(1, idleTimeoutMs),
                        TimeUnit.MILLISECONDS,
                        new LinkedBlockingQueue<>(),
                        factory);
        executor.allowCoreThreadTimeOut(core < max);
        return executor;
    }
}
