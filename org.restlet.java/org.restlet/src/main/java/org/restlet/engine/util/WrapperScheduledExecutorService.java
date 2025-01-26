/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.engine.util;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

/**
 * Wrapper of a {@link ScheduledExecutorService} instance, to prevent
 * manipulation of the actual service.
 * 
 * @author Jerome Louvel
 */
public class WrapperScheduledExecutorService implements ScheduledExecutorService {

	/** The wrapped executor service. */
	private final ScheduledExecutorService wrapped;

	/**
	 * Constructor.
	 * 
	 * @param wrapped The wrapped executor service.
	 */
	public WrapperScheduledExecutorService(ScheduledExecutorService wrapped) {
		this.wrapped = wrapped;
	}

	@Override
	public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
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
	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
		return getWrapped().invokeAll(tasks);
	}

	@Override
	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
			throws InterruptedException {
		return getWrapped().invokeAll(tasks, timeout, unit);
	}

	@Override
	public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
		return getWrapped().invokeAny(tasks);
	}

	@Override
	public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
			throws InterruptedException, ExecutionException, TimeoutException {
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
	public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
		return getWrapped().schedule(callable, delay, unit);
	}

	@Override
	public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
		return getWrapped().schedule(command, delay, unit);
	}

	@Override
	public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
		return getWrapped().scheduleAtFixedRate(command, initialDelay, period, unit);
	}

	@Override
	public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
		return getWrapped().scheduleWithFixedDelay(command, initialDelay, delay, unit);
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
