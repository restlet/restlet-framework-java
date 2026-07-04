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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.restlet.data.Header;
import org.restlet.data.Warning;

/** Unit tests for {@link WarningReader}. */
class WarningReaderTestCase {

    @Test
    void readValue_withoutDate_parsesCodeAgentAndText() throws IOException {
        WarningReader reader = new WarningReader("110 agent \"text\"");
        Warning warning = reader.readValue();

        assertEquals(110, warning.getStatus().getCode());
        assertEquals("agent", warning.getAgent());
        assertEquals("text", warning.getText());
        assertNull(warning.getDate());
    }

    @Test
    void readValue_withDate_parsesAllFields() throws IOException {
        WarningReader reader =
                new WarningReader("110 agent \"text\" \"Thu, 01 Jan 1970 00:00:00 GMT\"");
        Warning warning = reader.readValue();

        assertEquals(110, warning.getStatus().getCode());
        assertEquals("agent", warning.getAgent());
        assertEquals("text", warning.getText());
        assertNotNull(warning.getDate());
    }

    @Test
    void readValue_malformedHeader_throwsIOException() {
        WarningReader reader = new WarningReader("110 agent");
        assertThrows(IOException.class, reader::readValue);
    }

    @Test
    void addValues_appendsParsedWarningToCollection() {
        Header header = new Header("Warning", "110 agent \"text\"");
        List<Warning> collection = new ArrayList<>();

        WarningReader.addValues(header, collection);

        assertEquals(1, collection.size());
        assertEquals("agent", collection.getFirst().getAgent());
    }
}
