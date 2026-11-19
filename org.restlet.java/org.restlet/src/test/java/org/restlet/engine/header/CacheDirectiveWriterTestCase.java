/**
 * Copyright 2005-2026 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.engine.header;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.restlet.data.CacheDirective;

class CacheDirectiveWriterTestCase {

    @Test
    void write_nameOnlyDirective_writesNameOnly() {
        String result = CacheDirectiveWriter.write(List.of(CacheDirective.noCache()));
        assertEquals("no-cache", result);
    }

    @Test
    void write_nameValueDirective_writesNameEqualsValue() {
        String result = CacheDirectiveWriter.write(List.of(CacheDirective.maxAge(3600)));
        assertTrue(result.startsWith("max-age="));
    }

    @Test
    void write_multipleDirectives_joinsWithSeparator() {
        String result =
                CacheDirectiveWriter.write(
                        Arrays.asList(CacheDirective.noCache(), CacheDirective.noStore()));
        assertEquals("no-cache, no-store", result);
    }

    @Test
    void write_noCacheWithFieldName_writesOneQuotedString() {
        String result = CacheDirectiveWriter.write(List.of(CacheDirective.noCache("Set-Cookie")));
        assertEquals("no-cache=\"Set-Cookie\"", result);
    }

    @Test
    void write_noCacheWithFieldNames_writesOneQuotedList() {
        String result =
                CacheDirectiveWriter.write(
                        List.of(
                                CacheDirective.noCache(
                                        Arrays.asList("Set-Cookie", "Authorization"))));
        assertEquals("no-cache=\"Set-Cookie, Authorization\"", result);
    }

    @Test
    void write_privateWithFieldName_writesOneQuotedString() {
        String result =
                CacheDirectiveWriter.write(List.of(CacheDirective.privateInfo("Authorization")));
        assertEquals("private=\"Authorization\"", result);
    }

    @Test
    void write_privateWithFieldNames_writesOneQuotedList() {
        String result =
                CacheDirectiveWriter.write(
                        List.of(CacheDirective.privateInfo(Arrays.asList("A", "B"))));
        assertEquals("private=\"A, B\"", result);
    }

    @Test
    void write_noCacheWithEmptyFieldNames_writesNameOnly() {
        String result =
                CacheDirectiveWriter.write(List.of(CacheDirective.noCache(List.<String>of())));
        assertEquals("no-cache", result);
    }

    @Test
    void write_noCacheMixedWithOtherDirectives_keepsTheOtherOnesUnquoted() {
        String result =
                CacheDirectiveWriter.write(
                        Arrays.asList(
                                CacheDirective.noCache("Set-Cookie"),
                                CacheDirective.maxAge(60),
                                CacheDirective.publicInfo()));
        assertEquals("no-cache=\"Set-Cookie\", max-age=60, public", result);
    }
}
