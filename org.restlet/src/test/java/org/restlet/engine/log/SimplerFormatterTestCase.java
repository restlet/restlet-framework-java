/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.log;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link SimplerFormatter}. */
class SimplerFormatterTestCase {

    @Test
    void format_withoutThrowable_includesLevelLoggerAndMessage() {
        LogRecord record = new LogRecord(Level.INFO, "hello");
        record.setLoggerName("my.logger");

        String result = new SimplerFormatter().format(record);

        assertTrue(result.contains(Level.INFO.getLocalizedName()));
        assertTrue(result.contains("my.logger"));
        assertTrue(result.contains("hello"));
    }

    @Test
    void format_withThrowable_appendsStackTrace() {
        LogRecord record = new LogRecord(Level.SEVERE, "boom");
        record.setLoggerName("my.logger");
        record.setThrown(new RuntimeException("failure"));

        String result = new SimplerFormatter().format(record);

        assertTrue(result.contains("boom"));
        assertTrue(result.contains("RuntimeException"));
        assertTrue(result.contains("failure"));
    }
}
