/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.routing.Filter;

class CompositeHelperTestCase {

    private static class TestCompositeHelper extends CompositeHelper<Restlet> {
        TestCompositeHelper(Restlet helped) {
            super(helped);
        }

        @Override
        public void start() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void stop() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void update() {
            throw new UnsupportedOperationException();
        }
    }

    private static Filter markerFilter(java.util.concurrent.atomic.AtomicBoolean invoked) {
        return new Filter() {
            @Override
            protected int doHandle(Request request, Response response) {
                invoked.set(true);
                return CONTINUE;
            }
        };
    }

    @Test
    void addInboundFilter_singleFilter_becomesFirstAndLast() {
        Context context = new Context();
        TestCompositeHelper helper = new TestCompositeHelper(new Restlet(context) {});
        Filter filter = new Filter() {};

        helper.addInboundFilter(filter);

        assertEquals(filter, helper.getFirstInboundFilter());
    }

    @Test
    void addOutboundFilter_singleFilter_becomesFirstAndLast() {
        Context context = new Context();
        TestCompositeHelper helper = new TestCompositeHelper(new Restlet(context) {});
        Filter filter = new Filter() {};

        helper.addOutboundFilter(filter);

        assertEquals(filter, helper.getFirstOutboundFilter());
    }

    @Test
    void addOutboundFilter_secondFilter_chainsAfterFirst() {
        Context context = new Context();
        TestCompositeHelper helper = new TestCompositeHelper(new Restlet(context) {});
        Filter first = new Filter() {};
        Filter second = new Filter() {};

        helper.addOutboundFilter(first);
        helper.addOutboundFilter(second);

        assertEquals(first, helper.getFirstOutboundFilter());
        assertEquals(second, first.getNext());
    }

    @Test
    void clear_resetsAllFiltersAndNextRestlets() {
        Context context = new Context();
        TestCompositeHelper helper = new TestCompositeHelper(new Restlet(context) {});
        helper.addInboundFilter(new Filter() {});
        helper.addOutboundFilter(new Filter() {});

        helper.clear();

        assertNull(helper.getFirstInboundFilter());
        assertNull(helper.getFirstOutboundFilter());
        assertNull(helper.getOutboundNext());
    }

    @Test
    void handle_withInboundFilter_delegatesToFilter() {
        Context context = new Context();
        TestCompositeHelper helper = new TestCompositeHelper(new Restlet(context) {});
        java.util.concurrent.atomic.AtomicBoolean invoked =
                new java.util.concurrent.atomic.AtomicBoolean();
        helper.addInboundFilter(markerFilter(invoked));

        Request request = new Request(Method.GET, "http://localhost/test");
        Response response = new Response(request);
        helper.handle(request, response);

        assertTrue(invoked.get());
    }

    @Test
    void handle_withoutFilterOrNext_setsInternalServerError() {
        Context context = new Context();
        TestCompositeHelper helper = new TestCompositeHelper(new Restlet(context) {});

        Request request = new Request(Method.GET, "http://localhost/test");
        Response response = new Response(request);
        helper.handle(request, response);

        assertEquals(Status.SERVER_ERROR_INTERNAL, response.getStatus());
    }

    @Test
    void getOutboundNext_withoutFilters_returnsSetNext() {
        Context context = new Context();
        TestCompositeHelper helper = new TestCompositeHelper(new Restlet(context) {});
        Restlet next = new Restlet(context) {};

        helper.setOutboundNext(next);

        assertEquals(next, helper.getOutboundNext());
    }
}
