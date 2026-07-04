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

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.restlet.data.Header;

/** Unit tests for {@link StringReader}. */
class StringReaderTestCase {

    @Test
    void readValue_returnsToken() throws Exception {
        StringReader reader = new StringReader("token");
        assertEquals("token", reader.readValue());
    }

    @Test
    void addValues_appendsParsedTokenToCollection() {
        Header header = new Header("X-Custom", "value1, value2");
        List<String> collection = new ArrayList<>();

        StringReader.addValues(header, collection);

        assertEquals(List.of("value1", "value2"), collection);
    }
}
