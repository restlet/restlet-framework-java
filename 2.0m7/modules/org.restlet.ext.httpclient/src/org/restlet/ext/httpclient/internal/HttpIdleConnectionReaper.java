package org.restlet.ext.httpclient.internal;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.http.client.HttpClient;

/**
 * Class that embodies a Reaper thread that will reap idle connections. Whether
 * this class starts a reaper or not is based of whether the
 * sleepBetweenChecksMillis is greater than 0. Otherwise this code is pass-thru.
 * 
 * @author Sanjay Acharya
 */
public class HttpIdleConnectionReaper {
    private final HttpClient httpClient;
    private final long sleepBetweenChecksMillis;
    private final long reapConnectionIdleMillis;

    private final ReaperThread reaperThread;
    
    /**
     * Class Constructor
     * 
     * @param httpClient The HttpClient for which this is the reaper
     * @param sleepBetweenChecksMillis Time to sleep between checks for idle connections. 
     *                                 Note if this is 0L, then reaping won't occur
     * @param reapConnectionIdleMillis Reap connections older than
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
     * Used for testing purposes
     * 
     * @return {@code true} If the reaper was started
     */
    public boolean isStarted() {	
	return reaperThread != null && reaperThread.isAlive();
    }

    /**
     * Used for testing purposes
     * 
     * @return {@code true} If the reaper was stopped or never started
     */ 
    public boolean isStopped() {
	return (reaperThread != null || !reaperThread.isAlive());
    }

    /**
     * Exposed for testing
     * 
     * @param millis
     *            Time to wait before abandoning wait for start
     * @throws InterruptedException
     *             If the current thread was interrupted
     */
    public void waitForReaperStart(long millis) throws InterruptedException {
	reaperThread.waitForStart(millis);
    }
   
    /**
     * Exposed for testing
     * 
     * @param millis Time to wait before abandoning wait
     * @throws InterruptedException  If current thread was interrupted
     */
    public void waitForReaperStop(long millis) throws InterruptedException {
	reaperThread.waitForStop(millis);
    }
    
    /**
     * Stops the Idle Connection Reaper if running
     * 
     * @throws InterruptedException If the call to stop was interrupted
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
     * Thread that reaps Idle and Expired Connections.
     */
    private class ReaperThread extends Thread {
	private volatile boolean shutdown;
	private final CountDownLatch startupLatch = new CountDownLatch(1);
	private final CountDownLatch shutdownLatch = new CountDownLatch(1);

	void waitForStart(long millis) throws InterruptedException {
	    startupLatch.await(millis, TimeUnit.MILLISECONDS);
	}

	void waitForStop(long millis) throws InterruptedException {
	    shutdownLatch.await(millis, TimeUnit.MILLISECONDS);
	}

	@Override
	public void run() {
	    try {
		startupLatch.countDown();
		// While shutdown has not been called and the thread has not been interrupted
		// do the following.
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
    }
}
