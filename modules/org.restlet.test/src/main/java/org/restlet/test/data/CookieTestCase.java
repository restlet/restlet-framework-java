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

package org.restlet.test.data;

import org.restlet.data.Cookie;
import org.restlet.test.RestletTestCase;

/**
 * Test {@link org.restlet.data.Cookie}.
 * 
 * @author Jerome Louvel
 */
public class CookieTestCase extends RestletTestCase {

    /**
     * Equality tests.
     */
    public void testEquals() throws Exception {
        Cookie c1 = new Cookie(1, "name1", "value1", "path1", "domain1");
        Cookie c2 = new Cookie(1, "name1", "value1", "path1", "domain1");

        assertTrue(c1.equals(c2));
        assertTrue(c1.hashCode() == c2.hashCode());
        assertEquals(c1, c2);

        assertTrue(c1.equals(c1));
        assertEquals(c1, c1);
    }

    /**
     * Unequality tests.
     */
    public void testUnEquals() throws Exception {
        Cookie c1 = new Cookie(1, "name1", "value1", "path1", "domain1");
        Cookie c2 = new Cookie(2, "name2", "value2", "path2", "domain2");
        assertFalse(c1.equals(c2));
        assertFalse(c1.hashCode() == c2.hashCode());
        assertFalse(c1.equals(null));
        assertFalse(c2.equals(null));

        c1 = new Cookie(1, "name", "value", "path", "domain");
        c2 = new Cookie(2, "name", "value", "path", "domain");
        assertFalse(c1.equals(c2));
        assertFalse(c1.hashCode() == c2.hashCode());
    }

}
