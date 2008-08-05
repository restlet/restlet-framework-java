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
 * http://www.noelios.com/products/restlet-engine/.
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.test;

import org.restlet.data.Cookie;

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
        final Cookie c1 = new Cookie(1, "name1", "value1", "path1", "domain1");
        final Cookie c2 = new Cookie(1, "name1", "value1", "path1", "domain1");

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
