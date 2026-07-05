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
}
