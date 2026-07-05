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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.restlet.data.ClientInfo;
import org.restlet.data.Expectation;

class ExpectationReaderTestCase {

    @Test
    void readValue_nameOnly_parsesName() throws IOException {
        ExpectationReader reader = new ExpectationReader("100-continue");
        Expectation expectation = reader.readValue();
        assertEquals("100-continue", expectation.getName());
        assertTrue(expectation.getParameters().isEmpty());
    }

    @Test
    void readValue_withParameter_addsParameter() throws IOException {
        ExpectationReader reader = new ExpectationReader("foo;bar=baz");
        Expectation expectation = reader.readValue();
        assertEquals("foo", expectation.getName());
        assertEquals(1, expectation.getParameters().size());
        assertEquals("bar", expectation.getParameters().get(0).getName());
        assertEquals("baz", expectation.getParameters().get(0).getValue());
    }

    @Test
    void addValues_populatesClientInfoExpectations() {
        ClientInfo clientInfo = new ClientInfo();
        ExpectationReader.addValues("100-continue", clientInfo);
        assertEquals(1, clientInfo.getExpectations().size());
        assertEquals("100-continue", clientInfo.getExpectations().get(0).getName());
    }

    @Test
    void addValues_nullHeader_leavesExpectationsUnchanged() {
        ClientInfo clientInfo = new ClientInfo();
        ExpectationReader.addValues(null, clientInfo);
        assertTrue(clientInfo.getExpectations().isEmpty());
    }
}
