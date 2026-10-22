/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.restlet.data.Method;
import org.restlet.util.WrapperRequest;

class RequestLifecycleTestCase {

    @Test
    void reportsOnceWithLateSubscriptionAndWrapperDelegation() throws Exception {
        Request request = new Request(Method.GET, "http://localhost/");
        WrapperRequest wrapper = new WrapperRequest(request);
        IOException failure = new IOException("first");
        wrapper.notifyDisconnect(failure);
        request.notifyDisconnect(new IOException("second"));
        AtomicInteger callbacks = new AtomicInteger();
        var signal =
                wrapper.getDisconnectSignal()
                        .thenAccept(
                                cause -> {
                                    assertSame(failure, cause);
                                    callbacks.incrementAndGet();
                                });
        signal.toCompletableFuture().get(5, TimeUnit.SECONDS);
        assertEquals(1, callbacks.get());
        assertSame(
                failure,
                request.getDisconnectSignal().toCompletableFuture().get(5, TimeUnit.SECONDS));
        assertFalse(new Request(request).getDisconnectSignal().toCompletableFuture().isDone());
    }

    @Test
    void racesAbortWithConnectorRegistrationWithoutLosingCancellation() throws Exception {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < 100; i++) {
                Request request = new Request();
                AtomicInteger accepted = new AtomicInteger();
                var abort = executor.submit(request::abort);
                var register =
                        executor.submit(
                                () ->
                                        request.setAbortAction(
                                                () -> {
                                                    accepted.incrementAndGet();
                                                    return true;
                                                }));
                abort.get(5, TimeUnit.SECONDS);
                register.get(5, TimeUnit.SECONDS);
                org.junit.jupiter.api.Assertions.assertTrue(accepted.get() >= 1);
            }
        }
    }
}
