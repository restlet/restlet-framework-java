/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.xml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import javax.xml.transform.sax.SAXSource;
import org.junit.jupiter.api.Test;
import org.restlet.data.MediaType;
import org.restlet.representation.StringRepresentation;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

/** Unit tests for {@link SaxRepresentation}. */
class SaxRepresentationTestCase {

    private static final String XML = "<?xml version=\"1.0\"?><root><child>text</child></root>";

    @Test
    void isSecureProcessing_defaultTrue() {
        assertTrue(new SaxRepresentation().isSecureProcessing());
    }

    @Test
    void setSecureProcessing_changesFlag() {
        SaxRepresentation sr = new SaxRepresentation();
        sr.setSecureProcessing(false);
        assertFalse(sr.isSecureProcessing());
    }

    @Test
    void getSaxSource_fromRepresentation_returnsNonNull() throws Exception {
        SaxRepresentation sr =
                new SaxRepresentation(new StringRepresentation(XML, MediaType.TEXT_XML));
        assertNotNull(sr.getSaxSource());
    }

    @Test
    void parse_withContentHandler_firesElementEvents() throws Exception {
        SaxRepresentation sr =
                new SaxRepresentation(new StringRepresentation(XML, MediaType.TEXT_XML));
        List<String> names = new ArrayList<>();
        sr.parse(
                new DefaultHandler() {
                    @Override
                    public void startElement(
                            String uri, String localName, String qName, Attributes atts) {
                        names.add(qName);
                    }
                });
        assertEquals(List.of("root", "child"), names);
    }

    @Test
    void parse_withNullContentHandler_throwsIoException() {
        SaxRepresentation sr =
                new SaxRepresentation(new StringRepresentation(XML, MediaType.TEXT_XML));
        assertThrows(IOException.class, () -> sr.parse(null));
    }

    @Test
    void release_clearsSource() throws Exception {
        SAXSource source = new SAXSource(new InputSource(new StringReader(XML)));
        SaxRepresentation sr = new SaxRepresentation(MediaType.TEXT_XML, source);
        assertNotNull(sr.getSaxSource());
        sr.release();
        assertNull(sr.getSaxSource());
    }
}
