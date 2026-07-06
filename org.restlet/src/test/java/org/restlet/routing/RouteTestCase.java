/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.routing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;

/** Unit tests for {@link Route}. */
class RouteTestCase {

    /** Minimal concrete fixture, since {@link Route} is abstract. */
    private static class FixedScoreRoute extends Route {
        private final float score;

        FixedScoreRoute(Router router, Restlet next, float score) {
            super(router, next);
            this.score = score;
        }

        @Override
        public float score(Request request, Response response) {
            return score;
        }
    }

    @Test
    void constructorWithNext_hasNoRouterAndUsesNextContext() {
        Restlet next = new MockRestlet(new Context());

        Route route = new FixedScoreRoute(null, next, 1.0F);

        assertNull(route.getRouter());
        assertSame(next.getContext(), route.getContext());
    }

    @Test
    void constructorWithRouter_usesRouterContext() {
        Router router = new Router(new Context());
        Restlet next = new MockRestlet(null);

        Route route = new FixedScoreRoute(router, next, 1.0F);

        assertSame(router, route.getRouter());
        assertSame(router.getContext(), route.getContext());
    }

    @Test
    void setRouter_roundTrip() {
        Router router = new Router();
        Route route = new FixedScoreRoute(null, new MockRestlet(null), 1.0F);

        route.setRouter(router);

        assertSame(router, route.getRouter());
    }

    @Test
    void score_returnsValueFromSubclass() {
        Route route = new FixedScoreRoute(null, new MockRestlet(null), 0.75F);

        float score = route.score(new Request(), new Response(new Request()));

        assertEquals(0.75F, score);
    }
}
