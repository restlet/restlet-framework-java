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
import org.restlet.data.Disposition;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for the HTTP calls.
 * 
 * @author Kevin Conaway
 */
public class DispositionReaderTestCase {

    @Test
    public void testParseContentDisposition() throws IOException {
        Disposition disposition = new DispositionReader(
                "attachment; fileName=\"file.txt\"").readValue();
        assertEquals("file.txt",
                disposition.getParameters().getFirstValue("fileName"));

        disposition = new DispositionReader("attachment; fileName=file.txt")
                .readValue();
        assertEquals("file.txt",
                disposition.getParameters().getFirstValue("fileName"));

        disposition = new DispositionReader(
                "attachment; filename=\"file with space.txt\"").readValue();
        assertEquals("file with space.txt", disposition.getParameters()
                .getFirstValue("filename"));

        disposition = new DispositionReader("attachment; filename=\"\"")
                .readValue();
        assertEquals("", disposition.getParameters().getFirstValue("filename"));

        disposition = new DispositionReader("attachment; filename=")
                .readValue();
        assertNull(disposition.getParameters().getFirstValue("filename"));

        disposition = new DispositionReader("attachment; filenam").readValue();
        assertNull(disposition.getParameters().getFirstValue("filename"));

        disposition = new DispositionReader(
                "attachment; modification-date=\"Wed, 11 Nov 09 22:11:12 GMT\"")
                .readValue();
        String str = disposition.getParameters().getFirstValue(
                "modification-date");
        assertEquals("Wed, 11 Nov 09 22:11:12 GMT", str);

    }
}
