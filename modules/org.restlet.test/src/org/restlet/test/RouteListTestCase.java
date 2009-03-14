/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
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
