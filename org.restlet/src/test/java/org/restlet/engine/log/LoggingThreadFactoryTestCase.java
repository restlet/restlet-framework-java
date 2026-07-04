/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.log;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link LoggingThreadFactory}. */
class LoggingThreadFactoryTestCase {

    @Test
    void newThread_defaultConstructor_isNotDaemon() {
        LoggingThreadFactory factory = new LoggingThreadFactory(Logger.getLogger("test"));
        Thread thread = factory.newThread(() -> {});
        assertFalse(thread.isDaemon());
        assertTrue(thread.getName().startsWith("Restlet-"));
    }

    @Test
    void newThread_daemonTrue_createsDaemonThread() {
        LoggingThreadFactory factory = new LoggingThreadFactory(Logger.getLogger("test"), true);
        Thread thread = factory.newThread(() -> {});
        assertTrue(thread.isDaemon());
    }

    @Test
    void uncaughtExceptionHandler_logsException() throws Exception {
        Logger logger = Logger.getLogger("LoggingThreadFactoryTestCase");
        CountDownLatch latch = new CountDownLatch(1);
        Handler captor =
                new Handler() {
                    @Override
                    public void publish(LogRecord rec) {
                        latch.countDown();
                    }

                    @Override
                    public void flush() {
                        throw new UnsupportedOperationException();
                    }

                    @Override
                    public void close() {
                        throw new UnsupportedOperationException();
                    }
                };
        logger.addHandler(captor);

        LoggingThreadFactory factory = new LoggingThreadFactory(logger, true);
        Thread thread =
                factory.newThread(
                        () -> {
                            throw new RuntimeException("boom");
                        });
        thread.start();
        thread.join();

        assertTrue(latch.await(5, TimeUnit.SECONDS));
        logger.removeHandler(captor);
    }
}
