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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.restlet.data.CacheDirective;

class CacheDirectiveWriterTestCase {

    @Test
    void write_nameOnlyDirective_writesNameOnly() {
        String result = CacheDirectiveWriter.write(Arrays.asList(CacheDirective.noCache()));
        assertEquals("no-cache", result);
    }

    @Test
    void write_nameValueDirective_writesNameEqualsValue() {
        String result = CacheDirectiveWriter.write(Arrays.asList(CacheDirective.maxAge(3600)));
        assertTrue(result.startsWith("max-age="));
    }

    @Test
    void write_multipleDirectives_joinsWithSeparator() {
        String result =
                CacheDirectiveWriter.write(
                        Arrays.asList(CacheDirective.noCache(), CacheDirective.noStore()));
        assertEquals("no-cache, no-store", result);
    }
}
