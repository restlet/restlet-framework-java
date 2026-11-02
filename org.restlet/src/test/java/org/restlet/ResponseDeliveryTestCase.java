/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.restlet.engine.Engine;
import org.restlet.util.WrapperResponse;

class ResponseDeliveryTestCase {

    @Test
    void firstDeliveryReportWins() throws Exception {
        Response response = new Response(new Request());
        IOException failure = new IOException("Peer reset");
        response.notifyDelivered(failure);
        response.notifyDelivered(null);
        assertSame(
                failure,
                response.getDeliverySignal().toCompletableFuture().get(5, TimeUnit.SECONDS));
    }

    @Test
    void wrapperSharesTheDeliverySignalOfItsResponse() throws Exception {
        Response response = new Response(new Request());
        Response wrapper = new WrapperResponse(response);
        response.notifyDelivered(null);
        assertNull(wrapper.getDeliverySignal().toCompletableFuture().get(5, TimeUnit.SECONDS));

        Response other = new Response(new Request());
        new WrapperResponse(other).notifyDelivered(null);
        assertNull(other.getDeliverySignal().toCompletableFuture().get(5, TimeUnit.SECONDS));
    }

    @Test
    void localVariablesAreRestoredInAnotherThreadThenCleared() throws Exception {
        Response response = new Response(new Request());
        Response.setCurrent(response);
        AtomicReference<Response> seen = new AtomicReference<>();
        AtomicReference<Response> after = new AtomicReference<>(response);
        try {
            Runnable task = Engine.withLocalVariables(() -> seen.set(Response.getCurrent()));
            Thread.ofVirtual()
                    .start(
                            () -> {
                                task.run();
                                after.set(Response.getCurrent());
                            })
                    .join();
        } finally {
            Engine.clearThreadLocalVariables();
        }
        assertSame(response, seen.get());
        assertNull(after.get());
    }
}
