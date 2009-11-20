/**
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royaltee free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */
package org.restlet.test.jaxrs.util;

import java.util.Arrays;

import junit.framework.TestCase;

import org.restlet.ext.jaxrs.internal.util.PathRegExp;
import org.restlet.ext.jaxrs.internal.util.SortedOrderedBag;

/**
 * @author Stephan Koops
 * @see SortedOrderedBag
 */
@SuppressWarnings("all")
public class SortedOrderedBagTest extends TestCase {

    public void test2() {
        SortedOrderedBag<Integer> sob = new SortedOrderedBag<Integer>();
        sob.add(1);
        assertEqualsSOB(sob, 1);

        sob.add(1);
        assertEqualsSOB(sob, 1, 1);

        sob.add(1);
        assertEqualsSOB(sob, 1, 1, 1);

        sob.add(0);
        assertEqualsSOB(sob, 0, 1, 1, 1);

        sob.add(4);
        assertEqualsSOB(sob, 0, 1, 1, 1, 4);

        sob.add(2);
        assertEqualsSOB(sob, 0, 1, 1, 1, 2, 4);
    }

    public void test1() {
        SortedOrderedBag<Integer> sob = new SortedOrderedBag<Integer>();
        sob.add(1);
        assertEqualsSOB(sob, 1);

        sob.add(2);
        assertEqualsSOB(sob, 1, 2);

        sob.add(3);
        assertEqualsSOB(sob, 1, 2, 3);

        sob.add(4);
        assertEqualsSOB(sob, 1, 2, 3, 4);

        sob.add(5);
        assertEqualsSOB(sob, 1, 2, 3, 4, 5);

        sob.add(6);
        assertEqualsSOB(sob, 1, 2, 3, 4, 5, 6);
    }

    public void test0() {
        SortedOrderedBag<Integer> sob = new SortedOrderedBag<Integer>();
        sob.add(6);
        assertEqualsSOB(sob, 6);

        sob.add(5);
        assertEqualsSOB(sob, 5, 6);

        sob.add(4);
        assertEqualsSOB(sob, 4, 5, 6);

        sob.add(3);
        assertEqualsSOB(sob, 3, 4, 5, 6);

        sob.add(2);
        assertEqualsSOB(sob, 2, 3, 4, 5, 6);

        sob.add(1);
        assertEqualsSOB(sob, 1, 2, 3, 4, 5, 6);
    }

    public void test3() {
        SortedOrderedBag<Integer> sob = new SortedOrderedBag<Integer>();
        sob.add(1);
        assertEqualsSOB(sob, 1);

        sob.add(1);
        assertEqualsSOB(sob, 1, 1);

        sob.add(0);
        assertEqualsSOB(sob, 0, 1, 1);

        sob.add(4);
        assertEqualsSOB(sob, 0, 1, 1, 4);

        sob.add(2);
        assertEqualsSOB(sob, 0, 1, 1, 2, 4);
    }

    public void testString() {
        SortedOrderedBag<String> sob = new SortedOrderedBag<String>(
                String.CASE_INSENSITIVE_ORDER);
        sob.add("a");
        assertEqualsSOB(sob, "a");

        sob.add("b");
        assertEqualsSOB(sob, "a", "b");

        sob.add("B");
        assertEqualsSOB(sob, "a", "b", "B");

        sob.add("b");
        assertEqualsSOB(sob, "a", "b", "B", "b");

        sob.add("A");
        assertEqualsSOB(sob, "a", "A", "b", "B", "b");
    }

    public static <A> void assertEqualsSOB(SortedOrderedBag<A> given,
            A... expected) {
        // System.out.println("given  = " + given);
        // System.out.println("expected=" + Arrays.asList(expected));
        assertEquals("the size differs:", expected.length, given.size());
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], given.get(i));
        }
    }
}