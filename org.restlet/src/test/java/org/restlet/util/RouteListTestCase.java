/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import org.junit.jupiter.api.Test;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;
import org.restlet.routing.Route;

class RouteListTestCase {

    private static class ScoredRoute extends Route {
        private final float scoreValue;

        ScoredRoute(Restlet next, float scoreValue) {
            super(next);
            this.scoreValue = scoreValue;
        }

        @Override
        public float score(Request request, Response response) {
            return scoreValue;
        }
    }

    private static Request newRequest() {
        return new Request(Method.GET, "http://localhost/test");
    }

    @Test
    void constructor_withDelegate_copiesElements() {
        Restlet target = new Restlet() {};
        ArrayList<Route> delegate = new ArrayList<>();
        delegate.add(new ScoredRoute(target, 1.0F));
        RouteList list = new RouteList(delegate);
        assertEquals(1, list.size());
    }

    @Test
    void getBest_returnsHighestScoringRouteAboveThreshold() {
        RouteList list = new RouteList();
        Route low = new ScoredRoute(new Restlet() {}, 0.3F);
        Route high = new ScoredRoute(new Restlet() {}, 0.9F);
        list.add(low);
        list.add(high);

        Route best = list.getBest(newRequest(), new Response(newRequest()), 0.1F);

        assertEquals(high, best);
    }

    @Test
    void getBest_noRouteMeetsThreshold_returnsNull() {
        RouteList list = new RouteList();
        list.add(new ScoredRoute(new Restlet() {}, 0.1F));

        assertNull(list.getBest(newRequest(), new Response(newRequest()), 0.5F));
    }

    @Test
    void getFirst_returnsFirstMatchingRoute() {
        RouteList list = new RouteList();
        Route first = new ScoredRoute(new Restlet() {}, 0.5F);
        Route second = new ScoredRoute(new Restlet() {}, 0.9F);
        list.add(first);
        list.add(second);

        assertEquals(first, list.getFirst(newRequest(), new Response(newRequest()), 0.1F));
    }

    @Test
    void getFirst_noMatch_returnsNull() {
        RouteList list = new RouteList();
        list.add(new ScoredRoute(new Restlet() {}, 0.1F));

        assertNull(list.getFirst(newRequest(), new Response(newRequest()), 0.5F));
    }

    @Test
    void getLast_returnsLastMatchingRoute() {
        RouteList list = new RouteList();
        Route first = new ScoredRoute(new Restlet() {}, 0.5F);
        Route second = new ScoredRoute(new Restlet() {}, 0.9F);
        list.add(first);
        list.add(second);

        assertEquals(second, list.getLast(newRequest(), new Response(newRequest()), 0.1F));
    }

    @Test
    void getLast_noMatch_returnsNull() {
        RouteList list = new RouteList();
        list.add(new ScoredRoute(new Restlet() {}, 0.1F));

        assertNull(list.getLast(newRequest(), new Response(newRequest()), 0.5F));
    }

    @Test
    void getNext_cyclesThroughRoutesRoundRobin() {
        RouteList list = new RouteList();
        Route first = new ScoredRoute(new Restlet() {}, 1.0F);
        Route second = new ScoredRoute(new Restlet() {}, 1.0F);
        list.add(first);
        list.add(second);

        Route firstCall = list.getNext(newRequest(), new Response(newRequest()), 0.1F);
        Route secondCall = list.getNext(newRequest(), new Response(newRequest()), 0.1F);

        assertTrue(firstCall == first || firstCall == second);
        assertTrue(secondCall == first || secondCall == second);
    }

    @Test
    void getNext_emptyList_returnsNull() {
        RouteList list = new RouteList();
        assertNull(list.getNext(newRequest(), new Response(newRequest()), 0.1F));
    }

    @Test
    void getRandom_singleRouteMeetingThreshold_returnsIt() {
        RouteList list = new RouteList();
        Route only = new ScoredRoute(new Restlet() {}, 1.0F);
        list.add(only);

        assertEquals(only, list.getRandom(newRequest(), new Response(newRequest()), 0.1F));
    }

    @Test
    void getRandom_emptyList_returnsNull() {
        RouteList list = new RouteList();
        assertNull(list.getRandom(newRequest(), new Response(newRequest()), 0.1F));
    }

    @Test
    void removeAll_removesRoutesTargetingGivenRestlet() {
        RouteList list = new RouteList();
        Restlet target = new Restlet() {};
        Restlet other = new Restlet() {};
        list.add(new ScoredRoute(target, 1.0F));
        list.add(new ScoredRoute(other, 1.0F));

        list.removeAll(target);

        assertEquals(1, list.size());
        assertEquals(other, list.getFirst().getNext());
    }

    @Test
    void subList_returnsRouteListInstance() {
        RouteList list = new RouteList();
        list.add(new ScoredRoute(new Restlet() {}, 1.0F));
        list.add(new ScoredRoute(new Restlet() {}, 1.0F));

        java.util.List<Route> sub = list.subList(0, 1);

        assertTrue(sub instanceof RouteList);
        assertEquals(1, sub.size());
    }
}
