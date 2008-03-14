/*
 * Copyright 2005-2008 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package org.restlet.test;

import junit.framework.TestCase;

import org.restlet.Route;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.util.RouteList;

/**
 * Test case for RouteList class.
 * 
 * @author Kevin Conaway
 */
public class RouteListTestCase extends TestCase {

    public void testGetLast() {
        RouteList list = new RouteList();

        assertNull(list.getLast(null, null, 1f));

        Route last = new MockScoringRoute(5);

        list.add(new MockScoringRoute(5));
        list.add(new MockScoringRoute(5));
        list.add(last);

        assertSame(last, list.getLast(null, null, 1f));
        assertNull(list.getLast(null, null, 6f));
    }

    public void testGetNext() {
        RouteList list = new RouteList();

        assertNull(list.getNext(null, null, 1f));

        Route first = new MockScoringRoute(5);
        Route second = new MockScoringRoute(5);
        Route third = new MockScoringRoute(5);

        list.add(first);
        list.add(second);
        list.add(third);

        assertSame(first, list.getNext(null, null, 1f));
        assertSame(second, list.getNext(null, null, 1f));
        assertSame(third, list.getNext(null, null, 1f));

        assertSame(first, list.getNext(null, null, 1f));
    }

    @SuppressWarnings("null")
    public void testGetRandom() {
        RouteList list = new RouteList();

        assertNull(list.getRandom(null, null, 1f));

        list.add(new MockScoringRoute(2));
        list.add(new MockScoringRoute(3));
        list.add(new MockScoringRoute(4));

        assertNull(list.getRandom(null, null, 9f));

        list.add(new MockScoringRoute(6));
        list.add(new MockScoringRoute(7));
        list.add(new MockScoringRoute(8));

        MockScoringRoute r = (MockScoringRoute) list.getRandom(null, null, 5f);

        assertFalse(r == null);
        assertTrue(r.score > 5);

        assertNull(list.getRandom(null, null, 9f));
    }

    static class MockScoringRoute extends Route {
        int score;

        public MockScoringRoute(int score) {
            super(null);
            this.score = score;
        }

        @Override
        public float score(Request request, Response response) {
            return score;
        }
    }

}
