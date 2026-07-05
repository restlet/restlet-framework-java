/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;
import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.LocalReference;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.routing.Filter;

class ChildClientDispatcherTestCase {

    @Test
    void doHandle_riapApplication_dispatchesToChildApplicationInboundRoot() throws Exception {
        Context parentContext = new Context();
        ChildContext childContext = new ChildContext(parentContext);
        AtomicBoolean handled = new AtomicBoolean();
        Application application =
                new Application(childContext) {
                    @Override
                    public Restlet createInboundRoot() {
                        return new Restlet() {
                            @Override
                            public void handle(Request request, Response response) {
                                handled.set(true);
                                response.setStatus(Status.SUCCESS_OK);
                            }
                        };
                    }
                };
        application.start();
        childContext.setChild(application);

        ChildClientDispatcher dispatcher = new ChildClientDispatcher(childContext);
        Request request =
                new Request(
                        Method.GET,
                        LocalReference.createRiapReference(LocalReference.RIAP_APPLICATION, "/path")
                                .toString());
        Response response = new Response(request);

        int result = dispatcher.doHandle(request, response);

        assertTrue(handled.get());
        assertEquals(Filter.CONTINUE, result);
    }

    @Test
    void doHandle_riapComponent_delegatesToParentClientDispatcher() {
        Context parentContext = new Context();
        AtomicBoolean handled = new AtomicBoolean();
        parentContext.setClientDispatcher(
                new Restlet() {
                    @Override
                    public void handle(Request request, Response response) {
                        handled.set(true);
                    }
                });
        ChildContext childContext = new ChildContext(parentContext);
        ChildClientDispatcher dispatcher = new ChildClientDispatcher(childContext);

        Request request =
                new Request(
                        Method.GET,
                        LocalReference.createRiapReference(LocalReference.RIAP_COMPONENT, "/path")
                                .toString());
        Response response = new Response(request);

        dispatcher.doHandle(request, response);

        assertTrue(handled.get());
    }

    @Test
    void doHandle_riapHost_delegatesToParentClientDispatcher() {
        Context parentContext = new Context();
        AtomicBoolean handled = new AtomicBoolean();
        parentContext.setClientDispatcher(
                new Restlet() {
                    @Override
                    public void handle(Request request, Response response) {
                        handled.set(true);
                    }
                });
        ChildContext childContext = new ChildContext(parentContext);
        ChildClientDispatcher dispatcher = new ChildClientDispatcher(childContext);

        Request request =
                new Request(
                        Method.GET,
                        LocalReference.createRiapReference(LocalReference.RIAP_HOST, "/path")
                                .toString());
        Response response = new Response(request);

        dispatcher.doHandle(request, response);

        assertTrue(handled.get());
    }

    @Test
    void doHandle_nonRiapProtocol_delegatesToParentClientDispatcher() {
        Context parentContext = new Context();
        AtomicBoolean handled = new AtomicBoolean();
        parentContext.setClientDispatcher(
                new Restlet() {
                    @Override
                    public void handle(Request request, Response response) {
                        handled.set(true);
                    }
                });
        ChildContext childContext = new ChildContext(parentContext);
        ChildClientDispatcher dispatcher = new ChildClientDispatcher(childContext);

        Request request = new Request(Method.GET, "http://localhost/test");
        Response response = new Response(request);

        int result = dispatcher.doHandle(request, response);

        assertTrue(handled.get());
        assertEquals(Filter.CONTINUE, result);
    }

    @Test
    void doHandle_noParentContext_logsWarningAndDoesNotThrow() {
        ChildContext childContext = new ChildContext(null);
        ChildClientDispatcher dispatcher = new ChildClientDispatcher(childContext);

        Request request = new Request(Method.GET, "http://localhost/test");
        Response response = new Response(request);

        int result = dispatcher.doHandle(request, response);

        assertEquals(Filter.CONTINUE, result);
    }
}
