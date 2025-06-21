/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.engine.header;

import org.junit.jupiter.api.Test;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for the header.
 *
 * @author Thierry Boileau
 */
public class RangeReaderTestCase {

    @Test
    public void testUpdateRangeFirst9Bytes() {
        final String contentRangeHeaderValue = "bytes 1-9/10";
        final Representation representation = new StringRepresentation("0123456789", MediaType.TEXT_PLAIN);

        RangeReader.update(contentRangeHeaderValue, representation);

        assertEquals(10, representation.getSize());
        assertEquals(9, representation.getAvailableSize());
        assertEquals(1, representation.getRange().getIndex());
        assertEquals(9, representation.getRange().getSize());
    }

    @Test
    public void testUpdateRange0To100Bytes() {
        final String contentRangeHeaderValue = "bytes 0-100/10";
        final Representation representation = new StringRepresentation("0123456789", MediaType.TEXT_PLAIN);

        RangeReader.update(contentRangeHeaderValue, representation);

        assertEquals(10, representation.getSize());
        assertEquals(10, representation.getAvailableSize());
        assertEquals(0, representation.getRange().getIndex());
        assertEquals(101, representation.getRange().getSize());
    }

    @Test
    public void testUpdateRange1To9Bytes() {
        final String contentRangeHeaderValue = "bytes 1-9/*";
        final Representation representation = new StringRepresentation("0123456789", MediaType.TEXT_PLAIN);

        RangeReader.update(contentRangeHeaderValue, representation);

        assertEquals(10, representation.getSize());
        assertEquals(9, representation.getAvailableSize());
        assertEquals(1, representation.getRange().getIndex());
        assertEquals(9, representation.getRange().getSize());
    }

}
