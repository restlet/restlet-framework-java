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
    private class LoggingExceptionHandler implements
            Thread.UncaughtExceptionHandler {

        public void uncaughtException(Thread t, Throwable ex) {
            logger.log(Level.SEVERE, "Thread: " + t.getName()
                    + " terminated with exception: " + ex.getMessage(), ex);
        }
    }

    /** The associated logger. */
    private final Logger logger;

    /** Indicates if threads should be created as daemons. */
    private final boolean daemon;

    /**
     * Constructor.
     * 
     * @param logger
     *            The associated logger.
     */
    public LoggingThreadFactory(Logger logger) {
        this(logger, false);
    }

    /**
     * Constructor.
     * 
     * @param logger
     *            The associated logger.
     * @param daemon
     *            Indicates if threads should be created as daemons.
     */
    public LoggingThreadFactory(Logger logger, boolean daemon) {
        this.logger = logger;
        this.daemon = daemon;
    }

    /**
     * Creates a new thread.
     * 
     * @param r
     *            The runnable task.
     */
    public Thread newThread(Runnable r) {
        Thread result = new Thread(r);
        result.setName("Restlet-" + result.hashCode());
        result.setUncaughtExceptionHandler(new LoggingExceptionHandler());
        result.setDaemon(this.daemon);
        return result;
    }
}
