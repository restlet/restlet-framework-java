/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.engine.util;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Wrapper of a {@link ScheduledExecutorService} instance, to prevent
 * manipulation of the actual service.
 * 
 * @author Jerome Louvel
 */
public class WrapperScheduledExecutorService implements
        ScheduledExecutorService {

    /** The wrapped executor service. */
    private final ScheduledExecutorService wrapped;

    /**
     * Constructor.
     * 
     * @param wrapped
     *            The wrapped executor service.
     */
    public WrapperScheduledExecutorService(ScheduledExecutorService wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit)
            throws InterruptedException {
        return getWrapped().awaitTermination(timeout, unit);
    }

    @Override
    public void execute(Runnable command) {
        getWrapped().execute(command);
    }

    /**
     * Returns the wrapped executor service.
     * 
     * @return The wrapped executor service.
     */
    protected ScheduledExecutorService getWrapped() {
        return wrapped;
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks)
            throws InterruptedException {
        return getWrapped().invokeAll(tasks);
    }

    @Override
    public <T> List<Future<T>> invokeAll(
            Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
            throws InterruptedException {
        return getWrapped().invokeAll(tasks, timeout, unit);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks)
            throws InterruptedException, ExecutionException {
        return getWrapped().invokeAny(tasks);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks,
            long timeout, TimeUnit unit) throws InterruptedException,
            ExecutionException, TimeoutException {
        return getWrapped().invokeAny(tasks, timeout, unit);
    }

    @Override
    public boolean isShutdown() {
        return getWrapped().isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return getWrapped().isTerminated();
    }

    @Override
    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay,
            TimeUnit unit) {
        return getWrapped().schedule(callable, delay, unit);
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable command, long delay,
            TimeUnit unit) {
        return getWrapped().schedule(command, delay, unit);
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command,
            long initialDelay, long period, TimeUnit unit) {
        return getWrapped().scheduleAtFixedRate(command, initialDelay, period,
                unit);
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command,
            long initialDelay, long delay, TimeUnit unit) {
        return getWrapped().scheduleWithFixedDelay(command, initialDelay,
                delay, unit);
    }

    @Override
    public void shutdown() {
        getWrapped().shutdown();
    }

    @Override
    public List<Runnable> shutdownNow() {
        return getWrapped().shutdownNow();
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return getWrapped().submit(task);
    }

    @Override
    public Future<?> submit(Runnable task) {
        return getWrapped().submit(task);
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        return getWrapped().submit(task, result);
    }

}
