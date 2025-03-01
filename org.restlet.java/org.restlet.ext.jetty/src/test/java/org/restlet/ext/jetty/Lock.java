package org.restlet.ext.jetty;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class Lock {
    private static final Logger LOGGER = Logger.getLogger("Lock");

    private final CountDownLatch lock = new CountDownLatch(1);
    private final String name;

    public Lock(String name) {
        this.name = name;
    }

    public void unlock() {
        log("lock " + name + " unlock");
        lock.countDown();
    }

    public boolean awaitForUnlockingFor(final Duration waitTime) throws InterruptedException {
        log("lock " + name + " awaitForUnlocking");

        final long waitTimeInMs = waitTime.toMillis();
        boolean await = lock.await(waitTimeInMs, MILLISECONDS);
        log("lock " + name + " awaitForUnlocking done " + await);
        return await;
    }

    private static void log(final String message) {
        LOGGER.fine(Instant.now().toString() + " " + message);
    }

}
