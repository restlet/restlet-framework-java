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
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

/** Test {@link org.restlet.data.Header}. */
class HeaderTestCase {

    @Test
    void defaultConstructor_hasNullNameAndValue() {
        Header header = new Header();
        assertNull(header.getName());
        assertNull(header.getValue());
    }

    @Test
    void constructor_setsNameAndValue() {
        Header header = new Header("Content-Type", "text/plain");
        assertEquals("Content-Type", header.getName());
        assertEquals("text/plain", header.getValue());
    }

    @Test
    void settersUpdateState() {
        Header header = new Header();
        header.setName("X-Custom");
        header.setValue("value");
        assertEquals("X-Custom", header.getName());
        assertEquals("value", header.getValue());
    }

    @Test
    void equals_sameNameAndValue_returnsTrue() {
        Header header1 = new Header("X-Custom", "value");
        Header header2 = new Header("X-Custom", "value");
        assertEquals(header1, header2);
        assertEquals(header1.hashCode(), header2.hashCode());
    }

    @Test
    void equals_differentValue_returnsFalse() {
        Header header1 = new Header("X-Custom", "value1");
        Header header2 = new Header("X-Custom", "value2");
        assertNotEquals(header1, header2);
    }

    @Test
    void toString_containsNameAndValue() {
        Header header = new Header("X-Custom", "value");
        assertEquals("[X-Custom: value]", header.toString());
    }
}
