/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

/**
 * Test {@link org.restlet.data.Cookie}.
 *
 * @author Jerome Louvel
 */
class CookieTestCase {

    /** Equality tests. */
    @Test
    void testEquals() {
        Cookie c1 = new Cookie(1, "name1", "value1", "path1", "domain1");
        Cookie c2 = new Cookie(1, "name1", "value1", "path1", "domain1");

        assertEquals(c1, c2);
        assertEquals(c1.hashCode(), c2.hashCode());
        assertEquals(c1, c2);
    }

    /** Inequality tests. */
    @Test
    void testUnEquals() {
        Cookie c1 = new Cookie(1, "name1", "value1", "path1", "domain1");
        Cookie c2 = new Cookie(2, "name2", "value2", "path2", "domain2");
        assertNotEquals(c1, c2);
        assertNotEquals(c1.hashCode(), c2.hashCode());
        assertNotEquals(null, c1);
        assertNotEquals(null, c2);

        c1 = new Cookie(1, "name", "value", "path", "domain");
        c2 = new Cookie(2, "name", "value", "path", "domain");
        assertNotEquals(c1, c2);
        assertNotEquals(c1.hashCode(), c2.hashCode());
    }
}
