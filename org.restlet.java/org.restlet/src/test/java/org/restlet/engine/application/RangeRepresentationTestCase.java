/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.engine.application;

import org.junit.jupiter.api.Test;
import org.restlet.data.Range;
import org.restlet.representation.StringRepresentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit test case for the {@link RangeRepresentation} class.
 * 
 * @author Jerome Louvel
 */
public class RangeRepresentationTestCase {

    @Test
    public void testAppendable() throws Exception {
        StringRepresentation sr = new StringRepresentation("1234567890");
        RangeRepresentation rr = new RangeRepresentation(sr);
        rr.setRange(new Range(2, 5));
        assertNull(sr.getRange());
        assertEquals(10, sr.getAvailableSize());
        assertEquals(5, rr.getAvailableSize());
        assertEquals("1234567890", sr.getText());
        assertEquals("34567", rr.getText());
    }

    @Test
    public void testSize() throws Exception {
        StringRepresentation sr = new StringRepresentation("1234567890");
        RangeRepresentation rr = new RangeRepresentation(sr);
        rr.setRange(new Range(5, 10000));
        assertNull(sr.getRange());
        assertEquals(10, sr.getAvailableSize());
        assertEquals(5, rr.getAvailableSize());
        assertEquals("1234567890", sr.getText());
        assertEquals("67890", rr.getText());
    }

}
