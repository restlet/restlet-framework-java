/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.data;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/** Test {@link org.restlet.data.Dimension}. */
class DimensionTestCase {

    @Test
    void values_containsAllExpectedConstants() {
        Dimension[] values = Dimension.values();
        assertEquals(10, values.length);
        assertArrayEquals(
                new Dimension[] {
                    Dimension.AUTHORIZATION,
                    Dimension.CHARACTER_SET,
                    Dimension.CLIENT_ADDRESS,
                    Dimension.CLIENT_AGENT,
                    Dimension.UNSPECIFIED,
                    Dimension.ENCODING,
                    Dimension.LANGUAGE,
                    Dimension.MEDIA_TYPE,
                    Dimension.TIME,
                    Dimension.ORIGIN
                },
                values);
    }

    @Test
    void valueOf_returnsMatchingConstant() {
        assertEquals(Dimension.MEDIA_TYPE, Dimension.valueOf("MEDIA_TYPE"));
    }

    @Test
    void valueOf_unknownName_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> Dimension.valueOf("UNKNOWN"));
    }

    @Test
    void name_returnsDeclaredConstantName() {
        assertEquals("TIME", Dimension.TIME.name());
    }
}
