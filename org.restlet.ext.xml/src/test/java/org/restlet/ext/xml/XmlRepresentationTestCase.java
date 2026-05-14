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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Iterator;
import javax.xml.transform.stream.StreamSource;
import org.junit.jupiter.api.Test;
import org.restlet.data.MediaType;
import org.restlet.representation.StringRepresentation;
import org.w3c.dom.Node;

/**
 * Unit tests for the XPath API of {@link XmlRepresentation} (exercised via {@link
 * DomRepresentation}) and for {@link NodeList}.
 */
class XmlRepresentationTestCase {

    private static final String XML =
            "<?xml version=\"1.0\"?>"
                    + "<catalog>"
                    + "<book id=\"b1\" price=\"29.99\">"
                    + "<title>XML Guide</title><available>true</available>"
                    + "</book>"
                    + "<book id=\"b2\" price=\"15.00\">"
                    + "<title>Java Basics</title><available>false</available>"
                    + "</book>"
                    + "</catalog>";

    private static DomRepresentation dom() {
        return new DomRepresentation(new StringRepresentation(XML, MediaType.TEXT_XML));
    }

    @Test
    void getText_evaluatesStringExpression() {
        assertEquals("XML Guide", dom().getText("/catalog/book[1]/title"));
    }

    @Test
    void getNumber_evaluatesNumericExpression() {
        assertEquals(29.99, dom().getNumber("/catalog/book[1]/@price"), 0.001);
    }

    @Test
    void getBoolean_evaluatesBooleanPredicate() {
        assertTrue(dom().getBoolean("count(/catalog/book) = 2"));
    }

    @Test
    void getNode_returnsSingleNode() {
        Node node = dom().getNode("/catalog/book[2]/title");
        assertNotNull(node);
        assertEquals("Java Basics", node.getTextContent());
    }

    @Test
    void getNodes_returnsNodeListCoveringAllMethods() {
        NodeList nodes = dom().getNodes("/catalog/book");
        assertNotNull(nodes);
        // NodeList.size() and NodeList.getLength() both delegate to the underlying list length
        assertEquals(2, nodes.size());
        assertEquals(nodes.size(), nodes.getLength());
        // NodeList.get(index) and NodeList.item(index) both retrieve the node at that position
        assertNotNull(nodes.getFirst());
        assertNotNull(nodes.item(1));
        assertEquals("book", nodes.getFirst().getNodeName());
    }

    @Test
    void getNamespaceURI_whenRegistered_returnsUri() {
        DomRepresentation dr = dom();
        dr.getNamespaces().put("cat", "http://example.com/catalog");
        assertEquals("http://example.com/catalog", dr.getNamespaceURI("cat"));
    }

    @Test
    void getNamespaceURI_withNullNamespacesMap_returnsNull() {
        DomRepresentation dr = dom();
        assertNull(dr.getNamespaceURI("any"));
    }

    @Test
    void getPrefix_whenRegistered_returnsPrefix() {
        DomRepresentation dr = dom();
        dr.getNamespaces().put("ex", "http://example.com/");
        assertEquals("ex", dr.getPrefix("http://example.com/"));
    }

    @Test
    void getPrefixes_returnsIteratorWithMatchingEntries() {
        DomRepresentation dr = dom();
        dr.getNamespaces().put("ex", "http://example.com/");
        Iterator<String> it = dr.getPrefixes("http://example.com/");
        assertTrue(it.hasNext());
        assertEquals("ex", it.next());
    }

    @Test
    void getStreamSource_returnsNonNull() throws Exception {
        StreamSource source = dom().getStreamSource();
        assertNotNull(source);
    }

    @Test
    void getTextContent_returnsNodeTextContent() {
        Node node = dom().getNode("/catalog/book[1]/title");
        assertNotNull(node);
        assertEquals("XML Guide", XmlRepresentation.getTextContent(node));
    }

    @Test
    void setNamespaceAware_isNamespaceAware_roundTrip() {
        DomRepresentation dr = dom();
        assertFalse(dr.isNamespaceAware());
        dr.setNamespaceAware(true);
        assertTrue(dr.isNamespaceAware());
    }

    @Test
    void setCoalescing_isCoalescing_roundTrip() {
        DomRepresentation dr = dom();
        assertFalse(dr.isCoalescing());
        dr.setCoalescing(true);
        assertTrue(dr.isCoalescing());
    }

    @Test
    void setIgnoringExtraWhitespaces_alsoSetsValidatingDtd() {
        DomRepresentation dr = dom();
        assertFalse(dr.isIgnoringExtraWhitespaces());
        dr.setIgnoringExtraWhitespaces(true);
        assertTrue(dr.isIgnoringExtraWhitespaces());
        assertTrue(dr.isValidatingDtd());
    }
}
