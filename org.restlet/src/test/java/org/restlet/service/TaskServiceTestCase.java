/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link TaskService}. */
class TaskServiceTestCase {

    private TaskService taskService;

    @AfterEach
    void tearDown() throws Exception {
        if (taskService != null) {
            taskService.setShutdownAllowed(true);
            taskService.shutdownNow();
            taskService.awaitTermination(2, TimeUnit.SECONDS);
        }
    }

    @Test
    void defaultConstructor_isEnabledWithFourThreads() {
        taskService = new TaskService();

        assertTrue(taskService.isEnabled());
        assertEquals(4, taskService.getCorePoolSize());
        assertTrue(taskService.isDaemon());
        assertFalse(taskService.isShutdownAllowed());
    }

    @Test
    void constructorWithCorePoolSize_setsSize() {
        taskService = new TaskService(2);

        assertEquals(2, taskService.getCorePoolSize());
    }

    @Test
    void constructorWithEnabledAndDaemon_setsDaemonFlag() {
        taskService = new TaskService(true, false);

        assertFalse(taskService.isDaemon());
    }

    @Test
    void setCorePoolSizeAndSetDaemon_roundTrip() {
        taskService = new TaskService();

        taskService.setCorePoolSize(8);
        taskService.setDaemon(false);

        assertEquals(8, taskService.getCorePoolSize());
        assertFalse(taskService.isDaemon());
    }

    @Test
    void submitRunnable_executesTheTask() throws Exception {
        taskService = new TaskService();
        AtomicBoolean ran = new AtomicBoolean(false);

        Future<?> future = taskService.submit(() -> ran.set(true));
        future.get(5, TimeUnit.SECONDS);

        assertTrue(ran.get());
    }

    @Test
    void submitCallable_returnsComputedResult() throws Exception {
        taskService = new TaskService();

        Future<String> future = taskService.submit(() -> "done");

        assertEquals("done", future.get(5, TimeUnit.SECONDS));
    }

    @Test
    void submitRunnableWithResult_returnsGivenResult() throws Exception {
        taskService = new TaskService();
        AtomicBoolean ran = new AtomicBoolean(false);

        Future<String> future = taskService.submit(() -> ran.set(true), "the-result");

        assertEquals("the-result", future.get(5, TimeUnit.SECONDS));
        assertTrue(ran.get());
    }

    @Test
    void execute_runsCommandAsynchronously() throws Exception {
        taskService = new TaskService();
        CountDownLatch latch = new CountDownLatch(1);

        taskService.execute(latch::countDown);

        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }

    @Test
    void schedule_runsCallableAfterDelay() throws Exception {
        taskService = new TaskService();

        ScheduledFuture<String> future =
                taskService.schedule(
                        (Callable<String>) () -> "scheduled", 10, TimeUnit.MILLISECONDS);

        assertEquals("scheduled", future.get(5, TimeUnit.SECONDS));
    }

    @Test
    void schedule_runsRunnableAfterDelay() throws Exception {
        taskService = new TaskService();
        CountDownLatch latch = new CountDownLatch(1);

        ScheduledFuture<?> future =
                taskService.schedule((Runnable) latch::countDown, 10, TimeUnit.MILLISECONDS);
        future.get(5, TimeUnit.SECONDS);

        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }

    @Test
    void scheduleAtFixedRate_runsMultipleTimes() throws Exception {
        taskService = new TaskService();
        CountDownLatch latch = new CountDownLatch(3);

        ScheduledFuture<?> future =
                taskService.scheduleAtFixedRate(latch::countDown, 0, 10, TimeUnit.MILLISECONDS);

        assertTrue(latch.await(5, TimeUnit.SECONDS));
        future.cancel(true);
    }

    @Test
    void scheduleWithFixedDelay_runsMultipleTimes() throws Exception {
        taskService = new TaskService();
        CountDownLatch latch = new CountDownLatch(3);

        ScheduledFuture<?> future =
                taskService.scheduleWithFixedDelay(latch::countDown, 0, 10, TimeUnit.MILLISECONDS);

        assertTrue(latch.await(5, TimeUnit.SECONDS));
        future.cancel(true);
    }

    @Test
    void invokeAll_executesAllTasks() throws Exception {
        taskService = new TaskService();

        @SuppressWarnings({"rawtypes", "unchecked"})
        List<Future<String>> futures =
                taskService.invokeAll(
                        List.of((Callable<String>) () -> "a", (Callable<String>) () -> "b"));

        assertEquals(2, futures.size());
        assertEquals("a", futures.get(0).get());
        assertEquals("b", futures.get(1).get());
    }

    @Test
    void invokeAny_returnsOneResult() throws Exception {
        taskService = new TaskService();

        @SuppressWarnings("unchecked")
        Object result = taskService.invokeAny(List.of((Callable<String>) () -> "only"));

        assertEquals("only", result);
    }

    @Test
    void isShutdown_beforeStart_returnsTrue() {
        taskService = new TaskService();

        assertTrue(taskService.isShutdown());
    }

    @Test
    void isShutdown_afterStart_returnsFalse() throws Exception {
        taskService = new TaskService();

        taskService.start();

        assertFalse(taskService.isShutdown());
    }

    @Test
    void shutdown_withoutAllowedFlag_isNoOp() throws Exception {
        taskService = new TaskService();
        taskService.start();

        taskService.shutdown();

        assertFalse(taskService.isShutdown());
    }

    @Test
    void shutdown_withAllowedFlag_shutsDownExecutor() throws Exception {
        taskService = new TaskService();
        taskService.start();
        taskService.setShutdownAllowed(true);

        taskService.shutdown();

        assertTrue(taskService.awaitTermination(5, TimeUnit.SECONDS));
        assertTrue(taskService.isShutdown());
    }

    @Test
    void shutdownNow_withoutAllowedFlag_returnsEmptyList() throws Exception {
        taskService = new TaskService();
        taskService.start();

        List<Runnable> pending = taskService.shutdownNow();

        assertTrue(pending.isEmpty());
        assertFalse(taskService.isShutdown());
    }

    @Test
    void shutdownNow_withAllowedFlag_stopsExecutor() throws Exception {
        taskService = new TaskService();
        taskService.start();
        taskService.setShutdownAllowed(true);

        taskService.shutdownNow();

        assertTrue(taskService.awaitTermination(5, TimeUnit.SECONDS));
        assertTrue(taskService.isTerminated());
    }

    @Test
    void stop_shutsDownWrappedExecutorRegardlessOfAllowedFlag() throws Exception {
        taskService = new TaskService();
        taskService.start();

        taskService.stop();

        // Note: isShutdown() is checked directly (rather than awaitTermination(),
        // which calls startIfNeeded() and would transparently restart the
        // service since stop() resets the started flag).
        assertTrue(taskService.isShutdown());
        assertFalse(taskService.isStarted());
    }

    @Test
    void wrap_copiesThreadLocalsAndDelegatesToWrappedExecutor() throws Exception {
        java.util.concurrent.ScheduledExecutorService delegate =
                java.util.concurrent.Executors.newSingleThreadScheduledExecutor();
        try {
            ScheduledExecutorService wrapped = TaskService.wrap(delegate);
            AtomicInteger counter = new AtomicInteger();

            Future<?> future = wrapped.submit(counter::incrementAndGet);
            future.get(5, TimeUnit.SECONDS);

            assertEquals(1, counter.get());
            assertFalse(wrapped.isShutdown());
            assertFalse(wrapped.isTerminated());
        } finally {
            delegate.shutdownNow();
        }
    }
}
