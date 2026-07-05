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

import java.util.LinkedHashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class StringWriterTestCase {

    @Test
    void write_singleValue_writesValue() {
        Set<String> values = new LinkedHashSet<>();
        values.add("a");
        assertEquals("a", StringWriter.write(values));
    }

    @Test
    void write_multipleValues_joinsWithSeparator() {
        Set<String> values = new LinkedHashSet<>();
        values.add("a");
        values.add("b");
        assertEquals("a, b", StringWriter.write(values));
    }

    @Test
    void append_value_writesTokenizedValue() {
        StringWriter writer = new StringWriter();
        writer.append("token");
        assertEquals("token", writer.toString());
    }
}
