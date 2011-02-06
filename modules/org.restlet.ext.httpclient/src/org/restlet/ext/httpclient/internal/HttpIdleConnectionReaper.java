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

package org.restlet.ext.httpclient.internal;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.http.client.HttpClient;

/**
 * Class that embodies a Reaper thread that reaps idle connections. Note that
 * the thread won't be started if the value of the idleCheckInterval parameter
 * is equal to 0.
 * 
 * @author Sanjay Acharya
 */
public class HttpIdleConnectionReaper {

    /**
     * Thread that reaps idle and expired connections.
     */
    private class ReaperThread extends Thread {
        /** Indicates if the thread is shut down. */
        private volatile boolean shutdown;

        /** CountDownLatch used when stopping the thread. */
        private final CountDownLatch shutdownLatch = new CountDownLatch(1);

        /** CountDownLatch used when starting the thread. */
        private final CountDownLatch startupLatch = new CountDownLatch(1);

        @Override
        public void run() {
            try {
                startupLatch.countDown();
                // While shutdown has not been called and the thread has not
                // been interrupted do the following.
                while (!shutdown && !isInterrupted()) {
                    try {
                        Thread.sleep(idleCheckInterval);
                    } catch (InterruptedException interrupted) {
                        continue;
                    }

                    httpClient.getConnectionManager().closeExpiredConnections();
                    httpClient.getConnectionManager().closeIdleConnections(
                            idleTimeOut, TimeUnit.MILLISECONDS);
                }
            } finally {
                shutdownLatch.countDown();
            }
        }

        /**
         * Tells the reaper thread the maximum time to wait before starting.
         * 
         * @param millis
         *            The maximum time to wait before starting the thread.
         * @throws InterruptedException
         *             If the current thread was interrupted.
         */
        void waitForStart(long millis) throws InterruptedException {
            startupLatch.await(millis, TimeUnit.MILLISECONDS);
        }

        /**
         * Tells the reaper thread the maximum time to wait before stopping.
         * 
         * @param millis
         *            The maximum time to wait before stopping the thread.
         * @throws InterruptedException
         *             If the current thread was interrupted.
         */
        void waitForStop(long millis) throws InterruptedException {
            shutdownLatch.await(millis, TimeUnit.MILLISECONDS);
        }
    }

    /** The HttpClient for which this is the reaper. */
    private final HttpClient httpClient;

    /** The time to sleep between checks for idle connections. */
    private final long idleCheckInterval;

    /** The age of connections to reap. */
    private final long idleTimeOut;

    /** The thread that gleans the idle connections. */
    private final ReaperThread reaperThread;

    /**
     * Constructor.
     * 
     * @param httpClient
     *            The HttpClient for which this is the reaper.
     * @param idleCheckInterval
     *            The time to sleep between checks for idle connections. Note
     *            that if this is 0, then reaping won't occur.
     * @param idleTimeout
     *            The age of connections to reap.
     */
    public HttpIdleConnectionReaper(HttpClient httpClient,
            long idleCheckInterval, long idleTimeout) {
        if (httpClient == null) {
            throw new IllegalArgumentException(
                    "HttpClient is a required parameter");
        }
        this.httpClient = httpClient;
        this.idleCheckInterval = idleCheckInterval;
        this.idleTimeOut = idleTimeout;

        this.reaperThread = idleCheckInterval > 0L ? new ReaperThread() : null;

        if (reaperThread != null) {
            reaperThread.start();
        }
    }

    /**
     * Returns {@code true} if the reaper is started.
     * 
     * @return {@code true} If the reaper is started.
     */
    public boolean isStarted() {
        return reaperThread != null && reaperThread.isAlive();
    }

    /**
     * Returns {@code true} if the reaper is stopped.
     * 
     * @return {@code true} if the reaper is stopped.
     */
    public boolean isStopped() {
        return (reaperThread != null || !reaperThread.isAlive());
    }

    /**
     * Stops the Idle Connection Reaper if running.
     * 
     * @throws InterruptedException
     *             If the call to stop was interrupted
     */
    public void stop() throws InterruptedException {
        if (reaperThread == null) {
            return;
        }

        reaperThread.shutdown = true;
        reaperThread.interrupt();
        // Wait for a second to join
        reaperThread.join(1000L);
    }

    /**
     * Tells the reaper thread the maximum time to wait before starting.
     * 
     * @param millis
     *            The maximum time to wait before starting the thread.
     * @throws InterruptedException
     *             If the current thread was interrupted.
     */
    public void waitForReaperStart(long millis) throws InterruptedException {
        reaperThread.waitForStart(millis);
    }

    /**
     * Tells the reaper thread the maximum time to wait before stopping.
     * 
     * @param millis
     *            The maximum time to wait before stopping the thread.
     * @throws InterruptedException
     *             If the current thread was interrupted.
     */
    public void waitForReaperStop(long millis) throws InterruptedException {
        reaperThread.waitForStop(millis);
    }
}
