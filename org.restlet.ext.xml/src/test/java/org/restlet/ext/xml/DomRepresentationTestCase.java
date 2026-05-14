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
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.DOMSource;
import org.junit.jupiter.api.Test;
import org.restlet.data.MediaType;
import org.restlet.representation.StringRepresentation;
import org.w3c.dom.Document;

/** Unit tests for {@link DomRepresentation}. */
class DomRepresentationTestCase {

    private static final String XML = "<?xml version=\"1.0\"?><root><child>hello</child></root>";

    @Test
    void constructor_default_createsEmptyDocument() throws Exception {
        DomRepresentation dr = new DomRepresentation();
        assertNotNull(dr.getDocument());
        assertEquals(MediaType.TEXT_XML, dr.getMediaType());
    }

    @Test
    void constructor_fromRepresentation_parsesDocumentElement() throws Exception {
        DomRepresentation dr =
                new DomRepresentation(new StringRepresentation(XML, MediaType.TEXT_XML));
        assertEquals("root", dr.getDocument().getDocumentElement().getTagName());
    }

    @Test
    void constructor_fromDocument_returnsSameDocument() throws Exception {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        doc.appendChild(doc.createElement("root"));
        DomRepresentation dr = new DomRepresentation(MediaType.TEXT_XML, doc);
        assertSame(doc, dr.getDocument());
    }

    @Test
    void write_serialisesDocumentToXmlText() throws Exception {
        DomRepresentation dr =
                new DomRepresentation(new StringRepresentation(XML, MediaType.TEXT_XML));
        String result = dr.getText();
        assertTrue(result.contains("root"));
        assertTrue(result.contains("hello"));
    }

    @Test
    void isIndenting_defaultFalse() throws Exception {
        assertFalse(new DomRepresentation().isIndenting());
    }

    @Test
    void setIndenting_changesFlag() throws Exception {
        DomRepresentation dr = new DomRepresentation();
        dr.setIndenting(true);
        assertTrue(dr.isIndenting());
    }

    @Test
    void getDomSource_returnsSourceWrappingDocument() throws Exception {
        DomRepresentation dr =
                new DomRepresentation(new StringRepresentation(XML, MediaType.TEXT_XML));
        DOMSource source = dr.getDomSource();
        assertNotNull(source);
        assertNotNull(source.getNode());
    }

    @Test
    void release_onDocumentWrapper_createsNewDocumentOnNextAccess() throws Exception {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        doc.appendChild(doc.createElement("root"));
        DomRepresentation dr = new DomRepresentation(MediaType.TEXT_XML, doc);
        assertSame(doc, dr.getDocument());
        dr.release();
        // document is nulled; xmlRepresentation is null → getDocument() creates a new empty one
        Document newDoc = dr.getDocument();
        assertNotNull(newDoc);
        assertNotSame(doc, newDoc);
    }
}
