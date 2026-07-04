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
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.sax.SAXSource;
import org.junit.jupiter.api.Test;
import org.restlet.data.MediaType;
import org.restlet.representation.StringRepresentation;
import org.w3c.dom.Document;
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

    @Test
    void defaultConstructor_usesTextXmlMediaType() {
        SaxRepresentation sr = new SaxRepresentation();
        assertEquals(MediaType.TEXT_XML, sr.getMediaType());
    }

    @Test
    void constructor_withInputSource_wrapsItInSaxSource() throws Exception {
        InputSource inputSource = new InputSource(new StringReader(XML));
        SaxRepresentation sr = new SaxRepresentation(MediaType.TEXT_XML, inputSource);
        assertNotNull(sr.getSaxSource());
        assertNotNull(sr.getInputSource());
    }

    @Test
    void constructor_withDomDocument_wrapsItInSaxSource() throws Exception {
        Document document =
                DocumentBuilderFactory.newInstance()
                        .newDocumentBuilder()
                        .parse(new org.xml.sax.InputSource(new StringReader(XML)));
        SaxRepresentation sr = new SaxRepresentation(MediaType.TEXT_XML, document);
        assertNotNull(sr.getSaxSource());
    }

    @Test
    void constructor_withNullRepresentation_hasNullMediaType() {
        SaxRepresentation sr = new SaxRepresentation((StringRepresentation) null);
        assertNull(sr.getMediaType());
    }

    @Test
    void getInputSource_withoutSaxSource_returnsNull() throws Exception {
        SaxRepresentation sr = new SaxRepresentation();
        assertNull(sr.getInputSource());
    }

    @Test
    void write_writer_serializesUsingXmlWriter() throws Exception {
        SaxRepresentation sr =
                new SaxRepresentation(new StringRepresentation(XML, MediaType.TEXT_XML));
        StringWriter writer = new StringWriter();
        sr.write(writer);
        assertTrue(writer.toString().contains("<root>"));
        assertTrue(writer.toString().contains("<child>text"));
    }

    @Test
    void setSaxSource_overridesSource() throws Exception {
        SaxRepresentation sr = new SaxRepresentation();
        SAXSource source = new SAXSource(new InputSource(new StringReader(XML)));
        sr.setSaxSource(source);
        assertEquals(source, sr.getSaxSource());
    }
}
