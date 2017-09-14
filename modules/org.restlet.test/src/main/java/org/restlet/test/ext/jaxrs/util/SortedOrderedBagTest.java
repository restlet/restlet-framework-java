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

package org.restlet.test.ext.jaxrs.util;

import junit.framework.TestCase;

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
