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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.restlet.data.Encoding;

class EncodingReaderTestCase {

    @Test
    void readValue_gzip_parsesEncoding() throws IOException {
        assertEquals(Encoding.GZIP, new EncodingReader("gzip").readValue());
    }

    @Test
    void addValues_excludesIdentityEncoding() {
        List<Encoding> encodings = new ArrayList<>();
        new EncodingReader("identity, gzip").addValues(encodings);
        assertEquals(1, encodings.size());
        assertTrue(encodings.contains(Encoding.GZIP));
        assertFalse(encodings.contains(Encoding.IDENTITY));
    }
}
