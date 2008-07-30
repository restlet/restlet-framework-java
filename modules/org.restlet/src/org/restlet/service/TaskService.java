/*
 * Copyright 2005-2008 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package org.restlet.service;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.VirtualHost;
import org.restlet.data.Response;

/**
 * The service instance returned will not invoke the runnable task in the
 * current thread.
 * 
 * In addition to allowing pooling, this method will ensure that the threads
 * executing the tasks will have the thread local variables copied from the
 * calling thread. This will ensure that call to static methods like
 * {@link Application#getCurrent()} still work.
 * 
 * Also, note that this executor service will be shared among all Restlets and
 * Resources that are part of your context. In general this context corresponds
 * to a parent Application's context. If you want to have your own service
 * instance, you can use the {@link #wrap(ExecutorService)} method to ensure
 * that thread local variables are correctly set.
 * 
 * @author Jerome Louvel
 * @author Doug Lea (docs of ExecutorService in public domain)
 */
public class TaskService extends Service implements ExecutorService {

    /**
     * The default thread factory
     * 
     * @author Jerome Louvel
     * @author Doug Lea (initial code in public domain)
     */
    private static class RestletThreadFactory implements ThreadFactory {
        static final AtomicInteger restletPoolNumber = new AtomicInteger(1);

        final ThreadGroup group;

        final String namePrefix;

        final AtomicInteger threadNumber = new AtomicInteger(1);

        /**
         * Constructor.
         */
        public RestletThreadFactory() {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() : Thread.currentThread()
                    .getThreadGroup();
            namePrefix = "restlet-" + restletPoolNumber.getAndIncrement()
                    + "-thread-";
        }

        /**
         * Creates and name a new thread.
         * 
         * @param runnable
         *            The runnable task.
         */
        public Thread newThread(Runnable runnable) {
            Thread t = new Thread(group, runnable, namePrefix
                    + threadNumber.getAndIncrement(), 0);
            if (t.isDaemon())
                t.setDaemon(false);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }

    /**
     * Wraps a JDK's executor service to ensure that the threads executing the
     * tasks will have the thread local variables copied from the calling
     * thread. This will ensure that call to static methods like
     * {@link Application#getCurrent()} still work.
     * 
     * @param jdkExecutorService
     *            The JDK service to wrap.
     * @return The wrapper service to use.
     */
    public static ExecutorService wrap(final ExecutorService jdkExecutorService) {
        return new AbstractExecutorService() {

            public boolean awaitTermination(long timeout, TimeUnit unit)
                    throws InterruptedException {
                return jdkExecutorService.awaitTermination(timeout, unit);
            }

            public void execute(final Runnable runnable) {
                // Save the thread local variables
                final Application currentApplication = Application.getCurrent();
                final Context currentContext = Context.getCurrent();
                final Integer currentVirtualHost = VirtualHost.getCurrent();
                final Response currentResponse = Response.getCurrent();

                jdkExecutorService.execute(new Runnable() {
                    public void run() {
                        // Copy the thread local variables
                        Response.setCurrent(currentResponse);
                        Context.setCurrent(currentContext);
                        VirtualHost.setCurrent(currentVirtualHost);
                        Application.setCurrent(currentApplication);

                        try {
                            // Run the user task
                            runnable.run();
                        } finally {
                            // Reset the thread local variables
                            Response.setCurrent(null);
                            Context.setCurrent(null);
                            VirtualHost.setCurrent(-1);
                            Application.setCurrent(null);
                        }
                    }
                });
            }

            public boolean isShutdown() {
                return jdkExecutorService.isShutdown();
            }

            public boolean isTerminated() {
                return jdkExecutorService.isTerminated();
            }

            public void shutdown() {
                jdkExecutorService.shutdown();
            }

            public List<Runnable> shutdownNow() {
                return jdkExecutorService.shutdownNow();
            }
        };
    }

    /** The wrapped JDK executor service. */
    private volatile ExecutorService wrapped;

    /**
     * Constructor.
     */
    public TaskService() {
        setWrapped(wrap(createExecutorService()));
    }

    /**
     * Blocks until all tasks have completed execution after a shutdown request,
     * or the timeout occurs, or the current thread is interrupted, whichever
     * happens first.
     * 
     * @param timeout
     *            The maximum time to wait.
     * @param unit
     *            The time unit.
     * @return True if this executor terminated and false if the timeout elapsed
     *         before termination.
     */
    public boolean awaitTermination(long timeout, TimeUnit unit)
            throws InterruptedException {
        return getWrapped().awaitTermination(timeout, unit);
    }

    /**
     * Creates a new JDK executor service that will be wrapped. By default it
     * calls {@link Executors#newCachedThreadPool(ThreadFactory))}, passing the
     * result of {@link #createThreadFactory()} as a parameter.
     * 
     * @return A new JDK executor service.
     */
    protected ExecutorService createExecutorService() {
        return Executors.newCachedThreadPool(createThreadFactory());
    }

    /**
     * Creates a new thread factory that will properly name the Restlet created
     * threads with a "restlet-" prefix.
     * 
     * @return A new thread factory.
     */
    protected ThreadFactory createThreadFactory() {
        return new RestletThreadFactory();
    }

    /**
     * Executes the given command asynchronously.
     * 
     * @param command
     *            The command to execute.
     */
    public void execute(Runnable command) {
        getWrapped().execute(command);
    }

    /**
     * Returns the wrapped JDK executor service.
     * 
     * @return The wrapped JDK executor service.
     */
    private ExecutorService getWrapped() {
        return wrapped;
    }

    /**
     * Executes the given tasks, returning a list of Futures holding their
     * status and results when all complete.
     * 
     * @param tasks
     *            The task to execute.
     * @return The list of futures.
     */
    public <T> List<Future<T>> invokeAll(Collection<Callable<T>> tasks)
            throws InterruptedException {
        return getWrapped().invokeAll(tasks);
    }

    /**
     * Executes the given tasks, returning a list of Futures holding their
     * status and results when all complete or the timeout expires, whichever
     * happens first. Future.isDone() is true for each element of the returned
     * list. Upon return, tasks that have not completed are cancelled. Note that
     * a completed task could have terminated either normally or by throwing an
     * exception. The results of this method are undefined if the given
     * collection is modified while this operation is in progress.
     * 
     * @param tasks
     *            The task to execute.
     * @param timeout
     *            The maximum time to wait.
     * @param unit
     *            The time unit.
     * @return The list of futures.
     */
    public <T> List<Future<T>> invokeAll(Collection<Callable<T>> tasks,
            long timeout, TimeUnit unit) throws InterruptedException {
        return getWrapped().invokeAll(tasks, timeout, unit);
    }

    /**
     * Executes the given tasks, returning the result of one that has completed
     * successfully (i.e., without throwing an exception), if any do. Upon
     * normal or exceptional return, tasks that have not completed are
     * cancelled. The results of this method are undefined if the given
     * collection is modified while this operation is in progress.
     * 
     * @param tasks
     *            The task to execute.
     * @return The result returned by one of the tasks.
     */
    public <T> T invokeAny(Collection<Callable<T>> tasks)
            throws InterruptedException, ExecutionException {
        return getWrapped().invokeAny(tasks);
    }

    /**
     * Executes the given tasks, returning the result of one that has completed
     * successfully (i.e., without throwing an exception), if any do before the
     * given timeout elapses. Upon normal or exceptional return, tasks that have
     * not completed are cancelled. The results of this method are undefined if
     * the given collection is modified while this operation is in progress.
     * 
     * @param tasks
     *            The task to execute.
     * @param timeout
     *            The maximum time to wait.
     * @param unit
     *            The time unit.
     * @return The result returned by one of the tasks.
     */
    public <T> T invokeAny(Collection<Callable<T>> tasks, long timeout,
            TimeUnit unit) throws InterruptedException, ExecutionException,
            TimeoutException {
        return getWrapped().invokeAny(tasks, timeout, unit);
    }

    /**
     * Returns true if this executor has been shut down.
     * 
     * @return True if this executor has been shut down.
     */
    public boolean isShutdown() {
        return getWrapped().isShutdown();
    }

    /**
     * Returns true if all tasks have completed following shut down. Note that
     * isTerminated is never true unless either shutdown or shutdownNow was
     * called first.
     * 
     * @return True if all tasks have completed following shut down.
     */
    public boolean isTerminated() {
        return getWrapped().isTerminated();
    }

    /**
     * Sets the wrapped JDK executor service.
     * 
     * @param wrapped
     *            The wrapped JDK executor service.
     */
    private void setWrapped(ExecutorService wrapped) {
        this.wrapped = wrapped;
    }

    /**
     * Initiates an orderly shutdown in which previously submitted tasks are
     * executed, but no new tasks will be accepted.
     */
    public void shutdown() {
        getWrapped().shutdown();
    }

    /**
     * Attempts to stop all actively executing tasks, halts the processing of
     * waiting tasks, and returns a list of the tasks that were awaiting
     * execution.
     * 
     * @return The list of tasks that never commenced execution;
     */
    public List<Runnable> shutdownNow() {
        return getWrapped().shutdownNow();
    }

    @Override
    public synchronized void start() throws Exception {
        if (getWrapped().isShutdown()) {
            setWrapped(wrap(createExecutorService()));
        }

        super.start();
    }

    @Override
    public synchronized void stop() throws Exception {
        super.stop();

        if (!getWrapped().isShutdown()) {
            getWrapped().shutdown();
        }
    }

    /**
     * Submits a value-returning task for execution and returns a Future
     * representing the pending results of the task.
     * 
     * @param task
     *            The task to submit.
     * @return A Future representing pending completion of the task, and whose
     *         get() method will return the given result upon completion.
     */
    public <T> Future<T> submit(Callable<T> task) {
        return getWrapped().submit(task);
    }

    /**
     * 
     * @param task
     *            The task to submit.
     * @return A Future representing pending completion of the task, and whose
     *         get() method will return the given result upon completion.
     */
    public Future<?> submit(Runnable task) {
        return getWrapped().submit(task);
    }

    /**
     * 
     * @param task
     *            The task to submit.
     * @param result
     *            The result to return.
     * @return A Future representing pending completion of the task, and whose
     *         get() method will return the given result upon completion.
     */
    public <T> Future<T> submit(Runnable task, T result) {
        return getWrapped().submit(task, result);
    }

}
