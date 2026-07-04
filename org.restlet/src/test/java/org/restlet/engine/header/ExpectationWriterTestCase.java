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

import java.util.List;
import org.junit.jupiter.api.Test;
import org.restlet.data.Expectation;
import org.restlet.data.Parameter;

/** Unit tests for {@link ExpectationWriter}. */
class ExpectationWriterTestCase {

    @Test
    void write_singleExpectationWithoutParameters() {
        Expectation expectation = new Expectation("100-continue");
        assertEquals("100-continue", ExpectationWriter.write(List.of(expectation)));
    }

    @Test
    void write_expectationWithParameters_appendsParametersWithSeparator() {
        Expectation expectation = new Expectation("name", "value");
        expectation.getParameters().add(new Parameter("p1", "v1"));

        String result = ExpectationWriter.write(List.of(expectation));

        assertEquals("name=value;p1=v1", result);
    }

    @Test
    void write_multipleExpectations_joinsWithComma() {
        Expectation first = new Expectation("first");
        Expectation second = new Expectation("second");

        String result = ExpectationWriter.write(List.of(first, second));

        assertEquals("first, second", result);
    }

    @Test
    void write_expectationWithEmptyName_isSkipped() {
        Expectation expectation = new Expectation("");
        assertEquals("", ExpectationWriter.write(List.of(expectation)));
    }
}
