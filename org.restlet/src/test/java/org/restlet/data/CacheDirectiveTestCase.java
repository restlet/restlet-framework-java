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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import org.junit.jupiter.api.Test;

/** Test {@link org.restlet.data.CacheDirective}. */
class CacheDirectiveTestCase {

    @Test
    void maxAge_setsDigitValue() {
        CacheDirective directive = CacheDirective.maxAge(3600);
        assertEquals("max-age", directive.getName());
        assertEquals("3600", directive.getValue());
        assertTrue(directive.isDigit());
    }

    @Test
    void maxStale_noArgument_hasNullValue() {
        CacheDirective directive = CacheDirective.maxStale();
        assertEquals("max-stale", directive.getName());
        assertNull(directive.getValue());
        assertFalse(directive.isDigit());
    }

    @Test
    void maxStale_withArgument_setsDigitValue() {
        CacheDirective directive = CacheDirective.maxStale(120);
        assertEquals("max-stale", directive.getName());
        assertEquals("120", directive.getValue());
        assertTrue(directive.isDigit());
    }

    @Test
    void minFresh_setsDigitValue() {
        CacheDirective directive = CacheDirective.minFresh(30);
        assertEquals("min-fresh", directive.getName());
        assertEquals("30", directive.getValue());
        assertTrue(directive.isDigit());
    }

    @Test
    void mustRevalidate_hasNoValue() {
        CacheDirective directive = CacheDirective.mustRevalidate();
        assertEquals("must-revalidate", directive.getName());
        assertNull(directive.getValue());
    }

    @Test
    void noCache_noArgument_hasNoValue() {
        CacheDirective directive = CacheDirective.noCache();
        assertEquals("no-cache", directive.getName());
        assertNull(directive.getValue());
    }

    @Test
    void noCache_withFieldName_quotesTheName() {
        CacheDirective directive = CacheDirective.noCache("X-Custom");
        assertEquals("no-cache", directive.getName());
        assertEquals("\"X-Custom\"", directive.getValue());
    }

    @Test
    void noCache_withFieldNames_joinsWithComma() {
        CacheDirective directive = CacheDirective.noCache(Arrays.asList("A", "B"));
        assertEquals("\"A\",\"B\"", directive.getValue());
    }

    @Test
    void noCache_withNullFieldNames_producesEmptyValue() {
        CacheDirective directive = CacheDirective.noCache((java.util.List<String>) null);
        assertEquals("", directive.getValue());
    }

    @Test
    void noStore_hasNoValue() {
        CacheDirective directive = CacheDirective.noStore();
        assertEquals("no-store", directive.getName());
        assertNull(directive.getValue());
    }

    @Test
    void noTransform_hasNoValue() {
        CacheDirective directive = CacheDirective.noTransform();
        assertEquals("no-transform", directive.getName());
    }

    @Test
    void onlyIfCached_hasNoValue() {
        CacheDirective directive = CacheDirective.onlyIfCached();
        assertEquals("only-if-cached", directive.getName());
    }

    @Test
    void privateInfo_noArgument_hasNoValue() {
        CacheDirective directive = CacheDirective.privateInfo();
        assertEquals("private", directive.getName());
        assertNull(directive.getValue());
    }

    @Test
    void privateInfo_withFieldName_quotesTheName() {
        CacheDirective directive = CacheDirective.privateInfo("X-Custom");
        assertEquals("private", directive.getName());
        assertEquals("\"X-Custom\"", directive.getValue());
    }

    @Test
    void privateInfo_withFieldNames_joinsWithComma() {
        CacheDirective directive = CacheDirective.privateInfo(Arrays.asList("A", "B"));
        assertEquals("\"A\",\"B\"", directive.getValue());
    }

    @Test
    void proxyMustRevalidate_hasNoValue() {
        CacheDirective directive = CacheDirective.proxyMustRevalidate();
        assertEquals("proxy-revalidate", directive.getName());
    }

    @Test
    void publicInfo_hasNoValue() {
        CacheDirective directive = CacheDirective.publicInfo();
        assertEquals("public", directive.getName());
    }

    @Test
    void sharedMaxAge_setsDigitValue() {
        CacheDirective directive = CacheDirective.sharedMaxAge(600);
        assertEquals("s-maxage", directive.getName());
        assertEquals("600", directive.getValue());
        assertTrue(directive.isDigit());
    }

    @Test
    void constructor_withNameOnly_hasNoValueAndNotDigit() {
        CacheDirective directive = new CacheDirective("custom");
        assertEquals("custom", directive.getName());
        assertNull(directive.getValue());
        assertFalse(directive.isDigit());
    }

    @Test
    void constructor_withNameAndValue_isNotDigitByDefault() {
        CacheDirective directive = new CacheDirective("custom", "value");
        assertEquals("value", directive.getValue());
        assertFalse(directive.isDigit());
    }

    @Test
    void settersUpdateState() {
        CacheDirective directive = new CacheDirective("custom", "value");
        directive.setName("other");
        directive.setValue("otherValue");
        directive.setDigit(true);
        assertEquals("other", directive.getName());
        assertEquals("otherValue", directive.getValue());
        assertTrue(directive.isDigit());
    }

    @Test
    void equals_sameNameValueDigit_returnsTrue() {
        CacheDirective d1 = new CacheDirective("max-age", "10", true);
        CacheDirective d2 = new CacheDirective("max-age", "10", true);
        assertEquals(d1, d2);
        assertEquals(d1.hashCode(), d2.hashCode());
    }

    @Test
    void equals_sameInstance_returnsTrue() {
        CacheDirective directive = CacheDirective.noStore();
        assertEquals(directive, directive);
    }

    @Test
    void equals_differentType_returnsFalse() {
        CacheDirective directive = CacheDirective.noStore();
        assertNotEquals(directive, "no-store");
    }

    @Test
    void equals_differentDigitFlag_returnsFalse() {
        CacheDirective d1 = new CacheDirective("max-age", "10", true);
        CacheDirective d2 = new CacheDirective("max-age", "10", false);
        assertNotEquals(d1, d2);
    }

    @Test
    void equals_differentValue_returnsFalse() {
        CacheDirective d1 = CacheDirective.maxAge(10);
        CacheDirective d2 = CacheDirective.maxAge(20);
        assertNotEquals(d1, d2);
    }

    @Test
    void toString_containsFields() {
        CacheDirective directive = new CacheDirective("no-store", null, false);
        String result = directive.toString();
        assertTrue(result.contains("no-store"));
        assertTrue(result.contains("digit=false"));
    }
}
