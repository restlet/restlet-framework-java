/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.header;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * Test {@link ContentType}
 *
 * @author Jerome Louvel
 */
class ContentTypeTestCase {

    @Test
    void testParsingInvalid() {
        String h1 = "application/docbook+xml; version='my version 1.0'";

        assertThrows(IllegalArgumentException.class, () -> new ContentType(h1));
    }

    @Test
    void testParsing() {
        String h1 = "application/docbook+xml; version=\"my version 1.0\"";
        String h2 = "application/docbook+xml; version='my%20version%201.0'";

        ContentType ct1 = new ContentType(h1);
        ContentType ct2 = new ContentType(h2);

        assertEquals(h1, ct1.getMediaType().getName());
        assertEquals("my version 1.0", ct1.getMediaType().getParameters().getFirstValue("version"));

        assertEquals(h2, ct2.getMediaType().getName());
        assertEquals(
                "'my%20version%201.0'",
                ct2.getMediaType().getParameters().getFirstValue("version"));
    }
}
