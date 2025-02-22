package org.restlet.ext.jetty;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class Lock {
    private final CountDownLatch lock = new CountDownLatch(1);

    public void unlock() {
        lock.countDown();
    }

    public boolean awaitForUnlockingFor(final Duration waitTime) throws InterruptedException {
        final long waitTimeInMs = waitTime.toMillis();
        return lock.await(waitTimeInMs, MILLISECONDS);
    }
}
