/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.engine.log;

import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Thread factory that logs uncaught exceptions thrown by the created threads.
 * 
 * @author Jerome Louvel
 */
public class LoggingThreadFactory implements ThreadFactory {

	/**
	 * Handle uncaught thread exceptions.
	 */
	private class LoggingExceptionHandler implements Thread.UncaughtExceptionHandler {

		public void uncaughtException(Thread t, Throwable ex) {
			logger.log(Level.SEVERE, "Thread: " + t.getName() + " terminated with exception: " + ex.getMessage(), ex);
		}
	}

	/** The associated logger. */
	private final Logger logger;

	/** Indicates if threads should be created as daemons. */
	private final boolean daemon;

	/**
	 * Constructor.
	 * 
	 * @param logger The associated logger.
	 */
	public LoggingThreadFactory(Logger logger) {
		this(logger, false);
	}

	/**
	 * Constructor.
	 * 
	 * @param logger The associated logger.
	 * @param daemon Indicates if threads should be created as daemons.
	 */
	public LoggingThreadFactory(Logger logger, boolean daemon) {
		this.logger = logger;
		this.daemon = daemon;
	}

	/**
	 * Creates a new thread.
	 * 
	 * @param r The runnable task.
	 */
	public Thread newThread(Runnable r) {
		Thread result = new Thread(r);
		result.setName("Restlet-" + result.hashCode());
		result.setUncaughtExceptionHandler(new LoggingExceptionHandler());
		result.setDaemon(this.daemon);
		return result;
	}
}
