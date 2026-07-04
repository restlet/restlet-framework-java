/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.restlet.engine.header.HeaderConstants;

/** Unit tests for {@link Expectation}. */
class ExpectationTestCase {

    @Test
    void continueResponse_hasExpectContinueName() {
        Expectation expectation = Expectation.continueResponse();
        assertEquals(HeaderConstants.EXPECT_CONTINUE, expectation.getName());
    }

    @Test
    void constructorWithNameOnly_hasNullValue() {
        Expectation expectation = new Expectation("name");
        assertEquals("name", expectation.getName());
        assertEquals(null, expectation.getValue());
    }

    @Test
    void gettersAndSetters_roundTrip() {
        Expectation expectation = new Expectation("name", "value");
        assertEquals("name", expectation.getName());
        assertEquals("value", expectation.getValue());

        expectation.setName("newName");
        assertEquals("newName", expectation.getName());

        expectation.setValue("newValue");
        assertEquals("newValue", expectation.getValue());

        assertTrue(expectation.getParameters().isEmpty());
        expectation.setParameters(List.of(new Parameter("p", "v")));
        assertEquals(1, expectation.getParameters().size());
    }

    @Test
    void equalsAndHashCode_considerNameValueAndParameters() {
        Expectation e1 = new Expectation("name", "value");
        Expectation e2 = new Expectation("name", "value");
        Expectation e3 = new Expectation("other", "value");

        assertEquals(e1, e1);
        assertEquals(e1, e2);
        assertEquals(e1.hashCode(), e2.hashCode());
        assertNotEquals(e1, e3);
        assertFalse(e1.equals("not an expectation"));
    }

    @Test
    void toString_includesNameValueAndParameters() {
        Expectation expectation = new Expectation("name", "value");
        String text = expectation.toString();
        assertTrue(text.contains("name"));
        assertTrue(text.contains("value"));
    }
}
