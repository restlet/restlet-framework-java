/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class WrapperScheduledExecutorServiceTestCase {

    private ScheduledExecutorService delegate;

    private WrapperScheduledExecutorService wrapper;

    @BeforeEach
    void setUpEach() {
        delegate = Executors.newSingleThreadScheduledExecutor();
        wrapper = new WrapperScheduledExecutorService(delegate);
    }

    @AfterEach
    void tearDownEach() {
        delegate.shutdownNow();
    }

    @Test
    void getWrapped_returnsConstructorArgument() {
        assertEquals(delegate, wrapper.getWrapped());
    }

    @Test
    void submitCallable_executesAndReturnsResult() throws Exception {
        Future<String> future = wrapper.submit(() -> "done");
        assertEquals("done", future.get());
    }

    @Test
    void submitRunnable_executesTask() throws Exception {
        java.util.concurrent.atomic.AtomicBoolean ran =
                new java.util.concurrent.atomic.AtomicBoolean();
        Future<?> future = wrapper.submit(() -> ran.set(true));
        future.get();
        assertTrue(ran.get());
    }

    @Test
    void submitRunnableWithResult_returnsGivenResult() throws Exception {
        Future<String> future = wrapper.submit(() -> {}, "result");
        assertEquals("result", future.get());
    }

    @Test
    void execute_runsCommand() throws Exception {
        java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
        wrapper.execute(latch::countDown);
        assertTrue(latch.await(2, TimeUnit.SECONDS));
    }

    @Test
    void schedule_callable_executesAfterDelay() throws Exception {
        ScheduledFuture<String> future =
                wrapper.schedule(() -> "scheduled", 1, TimeUnit.MILLISECONDS);
        assertEquals("scheduled", future.get());
    }

    @Test
    void schedule_runnable_executesAfterDelay() throws Exception {
        ScheduledFuture<?> future = wrapper.schedule(() -> {}, 1, TimeUnit.MILLISECONDS);
        future.get();
        assertNotNull(future);
    }

    @Test
    void invokeAll_executesAllTasks() throws Exception {
        List<Callable<String>> tasks = List.of(() -> "a", () -> "b");
        List<Future<String>> results = wrapper.invokeAll(tasks);
        assertEquals(2, results.size());
    }

    @Test
    void invokeAllWithTimeout_executesAllTasks() throws Exception {
        List<Callable<String>> tasks = List.of(() -> "a");
        List<Future<String>> results = wrapper.invokeAll(tasks, 2, TimeUnit.SECONDS);
        assertEquals(1, results.size());
    }

    @Test
    void invokeAny_returnsResultOfOneTask() throws Exception {
        List<Callable<String>> tasks = List.of(() -> "a");
        assertEquals("a", wrapper.invokeAny(tasks));
    }

    @Test
    void invokeAnyWithTimeout_returnsResultOfOneTask() throws Exception {
        List<Callable<String>> tasks = List.of(() -> "a");
        assertEquals("a", wrapper.invokeAny(tasks, 2, TimeUnit.SECONDS));
    }

    @Test
    void isShutdownAndIsTerminated_reflectDelegateState() {
        assertFalse(wrapper.isShutdown());
        assertFalse(wrapper.isTerminated());
    }

    @Test
    void shutdownAndAwaitTermination_stopsAcceptingTasks() throws Exception {
        wrapper.shutdown();
        boolean terminated = wrapper.awaitTermination(2, TimeUnit.SECONDS);
        assertTrue(terminated);
        assertTrue(wrapper.isShutdown());
    }

    @Test
    void shutdownNow_returnsPendingTasks() {
        List<Runnable> pending = wrapper.shutdownNow();
        assertNotNull(pending);
    }
}
