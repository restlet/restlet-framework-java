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

import org.restlet.ext.jaxrs.internal.util.OrderedMap;

/**
 * @author Stephan Koops
 * @see OrderedMap
 */
@SuppressWarnings("all")
public class OrderedMapTest extends TestCase {

    public void test2() {
        OrderedMap<String, Integer> sob = new OrderedMap<String, Integer>();
        sob.add("a", 1);
        assertEquals("[a -> 1]", sob.toString());

        sob.add("b", 2);
        assertEquals("[a -> 1, b -> 2]", sob.toString());

        sob.add("d", 1);
        assertEquals("[a -> 1, b -> 2, d -> 1]", sob.toString());

        sob.add("c", 0);
        assertEquals("[a -> 1, b -> 2, d -> 1, c -> 0]", sob.toString());
    }
}
