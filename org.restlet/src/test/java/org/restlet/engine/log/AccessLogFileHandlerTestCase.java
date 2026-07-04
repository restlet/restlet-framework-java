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

import java.io.File;
import java.io.IOException;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link AccessLogFileHandler}. */
class AccessLogFileHandlerTestCase {

    private String tempPattern(String prefix) throws IOException {
        File dir = new File(System.getProperty("java.io.tmpdir"), "AccessLogFileHandlerTestCase");
        dir.mkdirs();
        dir.deleteOnExit();
        File file = File.createTempFile(prefix, ".log", dir);
        file.deleteOnExit();
        file.delete();
        return file.getAbsolutePath();
    }

    @Test
    void constructor_withPattern_usesAccessLogFormatter() throws Exception {
        AccessLogFileHandler handler = new AccessLogFileHandler(tempPattern("aaa"));
        try {
            assertTrue(handler.getFormatter() instanceof AccessLogFormatter);
        } finally {
            handler.close();
        }
    }

    @Test
    void constructor_withPatternAndAppend_usesAccessLogFormatter() throws Exception {
        AccessLogFileHandler handler = new AccessLogFileHandler(tempPattern("bbb"), true);
        try {
            assertTrue(handler.getFormatter() instanceof AccessLogFormatter);
        } finally {
            handler.close();
        }
    }

    @Test
    void constructor_withPatternLimitAndCount_usesAccessLogFormatter() throws Exception {
        AccessLogFileHandler handler = new AccessLogFileHandler(tempPattern("ccc"), 10000, 1);
        try {
            assertTrue(handler.getFormatter() instanceof AccessLogFormatter);
        } finally {
            handler.close();
        }
    }

    @Test
    void constructor_withPatternLimitCountAndAppend_usesAccessLogFormatter() throws Exception {
        AccessLogFileHandler handler =
                new AccessLogFileHandler(tempPattern("ddd"), 10000, 1, false);
        try {
            assertTrue(handler.getFormatter() instanceof AccessLogFormatter);
        } finally {
            handler.close();
        }
    }
}
