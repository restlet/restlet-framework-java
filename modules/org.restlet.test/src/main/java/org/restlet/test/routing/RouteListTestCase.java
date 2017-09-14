/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.test.routing;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.routing.Route;
import org.restlet.test.RestletTestCase;
import org.restlet.util.RouteList;

/**
 * Test case for RouteList class.
 * 
 * @author Kevin Conaway
 */
public class RouteListTestCase extends RestletTestCase {

    static class MockScoringRoute extends Route {
        int score;

        public MockScoringRoute(int score) {
            super(null);
            this.score = score;
        }

        @Override
        public float score(Request request, Response response) {
            return this.score;
        }
    }

    public void testGetLast() {
        final RouteList list = new RouteList();

        assertNull(list.getLast(null, null, 1f));

        final Route last = new MockScoringRoute(5);

        list.add(new MockScoringRoute(5));
        list.add(new MockScoringRoute(5));
        list.add(last);

        assertSame(last, list.getLast(null, null, 1f));
        assertNull(list.getLast(null, null, 6f));
    }

    public void testGetNext() {
        final RouteList list = new RouteList();

        assertNull(list.getNext(null, null, 1f));

        final Route first = new MockScoringRoute(5);
        final Route second = new MockScoringRoute(5);
        final Route third = new MockScoringRoute(5);

        list.add(first);
        list.add(second);
        list.add(third);

        assertSame(first, list.getNext(null, null, 1f));
        assertSame(second, list.getNext(null, null, 1f));
        assertSame(third, list.getNext(null, null, 1f));

        assertSame(first, list.getNext(null, null, 1f));
    }

    public void testGetRandom() {
        final RouteList list = new RouteList();

        assertNull(list.getRandom(null, null, 1f));

        list.add(new MockScoringRoute(2));
        list.add(new MockScoringRoute(3));
        list.add(new MockScoringRoute(4));

        assertNull(list.getRandom(null, null, 9f));

        list.add(new MockScoringRoute(6));
        list.add(new MockScoringRoute(7));
        list.add(new MockScoringRoute(8));

        final MockScoringRoute r = (MockScoringRoute) list.getRandom(null,
                null, 5f);

        assertFalse(r == null);
        assertTrue(r.score > 5);

        assertNull(list.getRandom(null, null, 9f));
    }

}
