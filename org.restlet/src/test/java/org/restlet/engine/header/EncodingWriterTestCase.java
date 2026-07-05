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

import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.restlet.data.Encoding;

class EncodingWriterTestCase {

    @Test
    void write_gzip_writesEncodingName() {
        assertEquals(Encoding.GZIP.getName(), EncodingWriter.write(Arrays.asList(Encoding.GZIP)));
    }

    @Test
    void canWrite_identity_returnsFalse() {
        EncodingWriter writer = new EncodingWriter();
        assertFalse(writer.canWrite(Encoding.IDENTITY));
    }

    @Test
    void write_identityIsSkipped() {
        String result = EncodingWriter.write(Arrays.asList(Encoding.IDENTITY, Encoding.GZIP));
        assertEquals(Encoding.GZIP.getName(), result);
    }
}
