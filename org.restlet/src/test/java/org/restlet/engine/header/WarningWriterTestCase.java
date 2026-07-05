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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.restlet.data.Status;
import org.restlet.data.Warning;

class WarningWriterTestCase {

    private static Warning newWarning() {
        Warning warning = new Warning();
        warning.setStatus(Status.valueOf(110));
        warning.setAgent("restlet/2.7");
        warning.setText("Response is stale");
        return warning;
    }

    @Test
    void write_validWarning_formatsCodeAgentAndQuotedText() {
        String result = WarningWriter.write(Arrays.asList(newWarning()));
        assertEquals("110 restlet/2.7 \"Response is stale\"", result);
    }

    @Test
    void write_withDate_appendsQuotedDate() {
        Warning warning = newWarning();
        warning.setDate(new java.util.Date(0));
        String result = WarningWriter.write(Arrays.asList(warning));
        assertTrue(result.startsWith("110 restlet/2.7 \"Response is stale\" \""));
    }

    @Test
    void append_nullStatus_throwsIllegalArgumentException() {
        Warning warning = newWarning();
        warning.setStatus(null);
        assertThrows(IllegalArgumentException.class, () -> new WarningWriter().append(warning));
    }

    @Test
    void append_missingAgent_throwsIllegalArgumentException() {
        Warning warning = newWarning();
        warning.setAgent(null);
        assertThrows(IllegalArgumentException.class, () -> new WarningWriter().append(warning));
    }

    @Test
    void append_missingText_throwsIllegalArgumentException() {
        Warning warning = newWarning();
        warning.setText("");
        assertThrows(IllegalArgumentException.class, () -> new WarningWriter().append(warning));
    }
}
