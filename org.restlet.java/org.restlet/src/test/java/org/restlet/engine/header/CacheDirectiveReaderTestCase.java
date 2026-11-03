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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.restlet.data.CacheDirective;
import org.restlet.data.Header;

class CacheDirectiveReaderTestCase {

    @Test
    void readValue_nameOnlyDirective_parsesName() throws IOException {
        CacheDirectiveReader reader = new CacheDirectiveReader("no-cache");
        CacheDirective directive = reader.readValue();
        assertEquals("no-cache", directive.getName());
    }

    @Test
    void readValue_nameValueDirective_parsesNameAndValue() throws IOException {
        CacheDirectiveReader reader = new CacheDirectiveReader("max-age=3600");
        CacheDirective directive = reader.readValue();
        assertEquals("max-age", directive.getName());
        assertEquals("3600", directive.getValue());
    }

    @Test
    void addValues_multipleDirectives_populatesCollection() {
        List<CacheDirective> directives = new ArrayList<>();
        new CacheDirectiveReader("no-cache, max-age=3600").addValues(directives);
        assertEquals(2, directives.size());
        assertEquals("no-cache", directives.get(0).getName());
        assertEquals("max-age", directives.get(1).getName());
    }

    @Test
    void addValuesStatic_fromHeader_populatesCollection() {
        List<CacheDirective> directives = new ArrayList<>();
        Header header = new Header("Cache-Control", "no-cache");
        CacheDirectiveReader.addValues(header, directives);
        assertEquals(1, directives.size());
    }

    @Test
    void readValue_quotedFieldNameList_returnsUnquotedValue() throws IOException {
        CacheDirective directive =
                new CacheDirectiveReader("no-cache=\"Set-Cookie, Authorization\"").readValue();
        assertEquals("no-cache", directive.getName());
        assertEquals("Set-Cookie, Authorization", directive.getValue());
    }

    @Test
    void readValue_unquotedFieldName_isAccepted() throws IOException {
        CacheDirective directive = new CacheDirectiveReader("private=Authorization").readValue();
        assertEquals("Authorization", directive.getValue());
    }

    @Test
    void writeThenRead_fieldNames_roundTripsTheValue() {
        for (CacheDirective original :
                List.of(
                        CacheDirective.noCache("Set-Cookie"),
                        CacheDirective.noCache(java.util.Arrays.asList("A", "B")),
                        CacheDirective.privateInfo("X-Custom"),
                        CacheDirective.privateInfo(java.util.Arrays.asList("A", "B")))) {
            List<CacheDirective> read = new ArrayList<>();
            new CacheDirectiveReader(CacheDirectiveWriter.write(List.of(original))).addValues(read);
            assertEquals(1, read.size());
            assertEquals(original.getName(), read.get(0).getName());
            assertEquals(original.getValue(), read.get(0).getValue());
        }
    }
}
