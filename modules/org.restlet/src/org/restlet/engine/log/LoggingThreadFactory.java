/**
 * Copyright 2005-2011 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
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
