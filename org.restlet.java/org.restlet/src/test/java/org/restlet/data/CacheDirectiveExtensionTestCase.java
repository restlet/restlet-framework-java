/**
 * Copyright 2005-2026 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.restlet.engine.header.CacheDirectiveReader;
import org.restlet.engine.header.CacheDirectiveWriter;
import org.restlet.engine.header.HeaderConstants;

/**
 * Tests of the Cache-Control directives that are extensions of RFC 9111 (must-understand), RFC 8246
 * (immutable) and RFC 5861 (stale-while-revalidate, stale-if-error).
 */
class CacheDirectiveExtensionTestCase {

    @Test
    void constants_haveTheNamesOfTheDirectives() {
        assertEquals("must-understand", HeaderConstants.CACHE_MUST_UNDERSTAND);
        assertEquals("immutable", HeaderConstants.CACHE_IMMUTABLE);
        assertEquals("stale-while-revalidate", HeaderConstants.CACHE_STALE_WHILE_REVALIDATE);
        assertEquals("stale-if-error", HeaderConstants.CACHE_STALE_IF_ERROR);
    }

    @Test
    void mustUnderstand_hasNoValue() {
        CacheDirective directive = CacheDirective.mustUnderstand();
        assertEquals("must-understand", directive.getName());
        assertNull(directive.getValue());
    }

    @Test
    void immutable_hasNoValue() {
        CacheDirective directive = CacheDirective.immutable();
        assertEquals("immutable", directive.getName());
        assertNull(directive.getValue());
    }

    @Test
    void staleWhileRevalidate_setsDigitValue() {
        CacheDirective directive = CacheDirective.staleWhileRevalidate(30);
        assertEquals("stale-while-revalidate", directive.getName());
        assertEquals("30", directive.getValue());
        assertTrue(directive.isDigit());
    }

    @Test
    void staleIfError_setsDigitValue() {
        CacheDirective directive = CacheDirective.staleIfError(86400);
        assertEquals("stale-if-error", directive.getName());
        assertEquals("86400", directive.getValue());
        assertTrue(directive.isDigit());
    }

    @Test
    void staleDirectives_acceptZero() {
        assertEquals("0", CacheDirective.staleWhileRevalidate(0).getValue());
        assertEquals("0", CacheDirective.staleIfError(0).getValue());
    }

    /**
     * The argument is a delta-seconds (RFC 5861): a negative number would make the header invalid.
     */
    @Test
    void staleDirectives_rejectANegativeNumberOfSeconds() {
        assertThrows(IllegalArgumentException.class, () -> CacheDirective.staleWhileRevalidate(-1));
        assertThrows(IllegalArgumentException.class, () -> CacheDirective.staleIfError(-1));
    }

    @Test
    void writer_writesTheDirectivesAsTheyAreSentInHeaders() {
        assertEquals(
                "max-age=31536000, immutable",
                CacheDirectiveWriter.write(
                        List.of(CacheDirective.maxAge(31536000), CacheDirective.immutable())));
        assertEquals(
                "max-age=60, stale-while-revalidate=30, stale-if-error=600",
                CacheDirectiveWriter.write(
                        List.of(
                                CacheDirective.maxAge(60),
                                CacheDirective.staleWhileRevalidate(30),
                                CacheDirective.staleIfError(600))));
        assertEquals(
                "must-understand, no-store",
                CacheDirectiveWriter.write(
                        List.of(CacheDirective.mustUnderstand(), CacheDirective.noStore())));
    }

    @Test
    void reader_readsTheDirectivesBack() {
        List<CacheDirective> directives = new ArrayList<>();
        new CacheDirectiveReader(
                        "max-age=31536000, immutable, stale-while-revalidate=30, stale-if-error=600,"
                                + " must-understand")
                .addValues(directives);

        assertEquals(5, directives.size());
        assertEquals("immutable", directives.get(1).getName());
        assertNull(directives.get(1).getValue());
        assertEquals("stale-while-revalidate", directives.get(2).getName());
        assertEquals("30", directives.get(2).getValue());
        assertEquals("stale-if-error", directives.get(3).getName());
        assertEquals("600", directives.get(3).getValue());
        assertEquals("must-understand", directives.get(4).getName());
    }

    @Test
    void factories_roundTripThroughTheHeader() {
        for (CacheDirective built :
                List.of(
                        CacheDirective.mustUnderstand(),
                        CacheDirective.immutable(),
                        CacheDirective.staleWhileRevalidate(30),
                        CacheDirective.staleIfError(600))) {
            List<CacheDirective> read = new ArrayList<>();
            new CacheDirectiveReader(CacheDirectiveWriter.write(List.of(built))).addValues(read);

            assertEquals(1, read.size());
            assertEquals(built.getName(), read.get(0).getName());
            assertEquals(built.getValue(), read.get(0).getValue());
            assertFalse(read.get(0).getName().isEmpty());
        }
    }
}
