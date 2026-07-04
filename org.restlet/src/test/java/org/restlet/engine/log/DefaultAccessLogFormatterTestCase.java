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

import org.junit.jupiter.api.Test;

/** Unit tests for {@link DefaultAccessLogFormatter}. */
class DefaultAccessLogFormatterTestCase {

    @Test
    void getHead_containsSoftwareVersionAndFieldsHeader() {
        String head = new DefaultAccessLogFormatter().getHead(null);

        assertTrue(head.contains("#Software: Restlet Framework"));
        assertTrue(head.contains("#Version: 1.0"));
        assertTrue(head.contains("#Date:"));
        assertTrue(head.contains("#Fields:"));
        assertTrue(head.contains("cs-method"));
    }
}
