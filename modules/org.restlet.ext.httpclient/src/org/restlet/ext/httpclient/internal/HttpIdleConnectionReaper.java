package org.restlet.ext.httpclient.internal;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.http.client.HttpClient;

/**
 * Class that embodies a Reaper thread that reaps idle connections. Note that
 * the reaper thread won't be started if the value of the
 * sleepBetweenChecksMillis parameter is equals to 0.
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
                        Thread.sleep(sleepBetweenChecksMillis);
                    } catch (InterruptedException interrupted) {
                        continue;
                    }

                    httpClient.getConnectionManager().closeExpiredConnections();
                    httpClient.getConnectionManager().closeIdleConnections(
                            reapConnectionIdleMillis, TimeUnit.MILLISECONDS);
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

    /** The age of connections to reap. */
    private final long reapConnectionIdleMillis;

    /** The thread that gleans the idle connections. */
    private final ReaperThread reaperThread;

    /** The time to sleep between checks for idle connections. */
    private final long sleepBetweenChecksMillis;

    /**
     * Constructor.
     * 
     * @param httpClient
     *            The HttpClient for which this is the reaper.
     * @param sleepBetweenChecksMillis
     *            The time to sleep between checks for idle connections. Note if
     *            this is 0L, then reaping won't occur.
     * @param reapConnectionIdleMillis
     *            The age of connections to reap.
     */
    public HttpIdleConnectionReaper(HttpClient httpClient,
            long sleepBetweenChecksMillis, long reapConnectionIdleMillis) {
        if (httpClient == null) {
            throw new IllegalArgumentException(
                    "HttpClient is a required parameter");
        }
        this.httpClient = httpClient;
        this.sleepBetweenChecksMillis = sleepBetweenChecksMillis;
        this.reapConnectionIdleMillis = reapConnectionIdleMillis;

        this.reaperThread = sleepBetweenChecksMillis > 0L ? new ReaperThread()
                : null;

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
