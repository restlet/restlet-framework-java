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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Test {@link org.restlet.data.Encoding}. */
class EncodingTestCase {

    @Test
    void constructor_withName_setsDefaultDescription() {
        Encoding encoding = new Encoding("custom");
        assertEquals("custom", encoding.getName());
        assertEquals("Encoding applied to a representation", encoding.getDescription());
    }

    @Test
    void constructor_withNameAndDescription() {
        Encoding encoding = new Encoding("custom", "my description");
        assertEquals("my description", encoding.getDescription());
    }

    @Test
    void valueOf_returnsSharedConstant() {
        assertSame(Encoding.GZIP, Encoding.valueOf("gzip"));
        assertSame(Encoding.ZIP, Encoding.valueOf("ZIP"));
        assertSame(Encoding.COMPRESS, Encoding.valueOf("compress"));
        assertSame(Encoding.DEFLATE, Encoding.valueOf("deflate"));
        assertSame(Encoding.DEFLATE_NOWRAP, Encoding.valueOf("deflate-no-wrap"));
        assertSame(Encoding.IDENTITY, Encoding.valueOf("identity"));
        assertSame(Encoding.FREEMARKER, Encoding.valueOf("freemarker"));
        assertSame(Encoding.VELOCITY, Encoding.valueOf("velocity"));
        assertSame(Encoding.ALL, Encoding.valueOf("*"));
    }

    @Test
    void valueOf_unknownName_createsNewInstance() {
        Encoding encoding = Encoding.valueOf("custom-encoding");
        assertEquals("custom-encoding", encoding.getName());
    }

    @Test
    void valueOf_nullOrEmptyName_returnsNull() {
        assertNull(Encoding.valueOf(null));
        assertNull(Encoding.valueOf(""));
    }

    @Test
    void equals_isCaseInsensitive() {
        Encoding e1 = new Encoding("gzip");
        Encoding e2 = new Encoding("GZIP");
        assertEquals(e1, e2);
        assertEquals(e1.hashCode(), e2.hashCode());
    }

    @Test
    void getParent_forAll_returnsNull() {
        assertNull(Encoding.ALL.getParent());
    }

    @Test
    void getParent_forSpecificEncoding_returnsAll() {
        assertEquals(Encoding.ALL, Encoding.GZIP.getParent());
    }

    @Test
    void includes_allIncludesEverything() {
        assertTrue(Encoding.ALL.includes(Encoding.GZIP));
        assertFalse(Encoding.GZIP.includes(Encoding.ALL));
    }

    @Test
    void includes_nullIsAlwaysIncluded() {
        assertTrue(Encoding.GZIP.includes(null));
    }

    @Test
    void includes_sameEncoding_returnsTrue() {
        assertTrue(Encoding.GZIP.includes(Encoding.GZIP));
    }
}
