/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.xml;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;
import java.util.Iterator;
import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.junit.jupiter.api.Test;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
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
        dr.getNamespaces().put("cat", "https://example.com/catalog");
        assertEquals("https://example.com/catalog", dr.getNamespaceURI("cat"));
    }

    @Test
    void getNamespaceURI_withNullNamespacesMap_returnsNull() {
        DomRepresentation dr = dom();
        assertNull(dr.getNamespaceURI("any"));
    }

    @Test
    void getPrefix_whenRegistered_returnsPrefix() {
        DomRepresentation dr = dom();
        dr.getNamespaces().put("ex", "https://example.com/");
        assertEquals("ex", dr.getPrefix("https://example.com/"));
    }

    @Test
    void getPrefixes_returnsIteratorWithMatchingEntries() {
        DomRepresentation dr = dom();
        dr.getNamespaces().put("ex", "https://example.com/");
        Iterator<String> it = dr.getPrefixes("https://example.com/");
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

    private static final String XSD =
            "<?xml version=\"1.0\"?>"
                    + "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">"
                    + "<xs:element name=\"root\">"
                    + "<xs:complexType><xs:sequence>"
                    + "<xs:element name=\"child\" type=\"xs:string\"/>"
                    + "</xs:sequence></xs:complexType>"
                    + "</xs:element>"
                    + "</xs:schema>";

    private static final String VALID_FOR_XSD = "<root><child>hello</child></root>";

    @Test
    void getSaxSource_static_withRepresentation_returnsNonNull() throws Exception {
        assertNotNull(
                XmlRepresentation.getSaxSource(new StringRepresentation(XML, MediaType.TEXT_XML)));
    }

    @Test
    void getSaxSource_static_withNullRepresentation_returnsNull() throws Exception {
        assertNull(XmlRepresentation.getSaxSource(null));
    }

    @Test
    void getSaxSource_static_withLocationRef_setsSystemId() throws Exception {
        StringRepresentation rep = new StringRepresentation(XML, MediaType.TEXT_XML);
        rep.setLocationRef(new Reference("https://example.com/doc.xml"));
        assertEquals(
                "https://example.com/doc.xml", XmlRepresentation.getSaxSource(rep).getSystemId());
    }

    @Test
    void getSaxSource_instance_returnsNonNull() throws Exception {
        assertNotNull(dom().getSaxSource());
    }

    @Test
    void hashCode_isStable() {
        DomRepresentation dr = dom();
        assertEquals(dr.hashCode(), dr.hashCode());
    }

    @Test
    void release_clearsNamespacesMap() {
        DomRepresentation dr = dom();
        dr.getNamespaces().put("ex", "https://example.com/");
        dr.release();
        assertTrue(dr.getNamespaces().isEmpty());
    }

    @Test
    void setEntityResolver_getEntityResolver_roundTrip() {
        DomRepresentation dr = dom();
        org.xml.sax.EntityResolver resolver = (publicId, systemId) -> null;
        dr.setEntityResolver(resolver);
        assertEquals(resolver, dr.getEntityResolver());
    }

    @Test
    void setErrorHandler_getErrorHandler_roundTrip() {
        DomRepresentation dr = dom();
        org.xml.sax.ErrorHandler handler = new org.xml.sax.helpers.DefaultHandler();
        dr.setErrorHandler(handler);
        assertEquals(handler, dr.getErrorHandler());
    }

    @Test
    void setExpandingEntityRefs_isExpandingEntityRefs_roundTrip() {
        DomRepresentation dr = dom();
        dr.setExpandingEntityRefs(true);
        assertTrue(dr.isExpandingEntityRefs());
    }

    @Test
    void setIgnoringComments_isIgnoringComments_roundTrip() {
        DomRepresentation dr = dom();
        dr.setIgnoringComments(true);
        assertTrue(dr.isIgnoringComments());
    }

    @Test
    void setNamespaces_getNamespaces_roundTrip() {
        DomRepresentation dr = dom();
        java.util.Map<String, String> map = new java.util.HashMap<>();
        map.put("a", "b");
        dr.setNamespaces(map);
        assertEquals(map, dr.getNamespaces());
    }

    @Test
    void setXIncludeAware_isXIncludeAware_roundTrip() {
        DomRepresentation dr = dom();
        assertFalse(dr.isXIncludeAware());
        dr.setXIncludeAware(true);
        assertTrue(dr.isXIncludeAware());
    }

    @Test
    void setSchema_compiledSchema_getSchema_roundTrip() throws Exception {
        Schema schema =
                SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
                        .newSchema(new StreamSource(new StringReader(XSD)));
        DomRepresentation dr = dom();
        dr.setSchema(schema);
        assertEquals(schema, dr.getSchema());
    }

    @Test
    void setSchema_withW3cSchemaRepresentation_compilesSchema() {
        DomRepresentation dr = dom();
        dr.setSchema(new StringRepresentation(XSD, MediaType.APPLICATION_W3C_SCHEMA));
        assertNotNull(dr.getSchema());
    }

    @Test
    void setSchema_withNullRepresentation_leavesSchemaNull() {
        DomRepresentation dr = dom();
        dr.setSchema((Representation) null);
        assertNull(dr.getSchema());
    }

    @Test
    void setSchema_withRelaxNgCompactMediaType_doesNotThrow() {
        DomRepresentation dr = dom();
        assertDoesNotThrow(
                () ->
                        dr.setSchema(
                                new StringRepresentation(
                                        "not a schema", MediaType.APPLICATION_RELAXNG_COMPACT)));
    }

    @Test
    void setSchema_withRelaxNgXmlMediaType_doesNotThrow() {
        DomRepresentation dr = dom();
        assertDoesNotThrow(
                () ->
                        dr.setSchema(
                                new StringRepresentation(
                                        "<grammar/>", MediaType.APPLICATION_RELAXNG_XML)));
    }

    @Test
    void setSchema_withUnknownMediaType_doesNotThrow() {
        DomRepresentation dr = dom();
        assertDoesNotThrow(
                () -> dr.setSchema(new StringRepresentation("data", MediaType.TEXT_PLAIN)));
        assertNull(dr.getSchema());
    }

    @Test
    void validate_withCompiledSchema_succeedsForValidDocument() throws Exception {
        Schema schema =
                SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
                        .newSchema(new StreamSource(new StringReader(XSD)));
        DomRepresentation dr =
                new DomRepresentation(new StringRepresentation(VALID_FOR_XSD, MediaType.TEXT_XML));
        dr.validate(schema);
    }

    @Test
    void validate_withSchemaRepresentation_succeedsForValidDocument() throws Exception {
        DomRepresentation dr =
                new DomRepresentation(new StringRepresentation(VALID_FOR_XSD, MediaType.TEXT_XML));
        dr.validate(new StringRepresentation(XSD, MediaType.APPLICATION_W3C_SCHEMA));
    }

    @Test
    void validate_withSchemaRepresentationAndResult_writesAugmentedResult() throws Exception {
        DomRepresentation dr =
                new DomRepresentation(new StringRepresentation(VALID_FOR_XSD, MediaType.TEXT_XML));
        // The validated source is a SAXSource, so the result must be null or a SAXResult.
        javax.xml.transform.sax.SAXResult result =
                new javax.xml.transform.sax.SAXResult(new org.xml.sax.helpers.DefaultHandler());
        dr.validate(new StringRepresentation(XSD, MediaType.APPLICATION_W3C_SCHEMA), result);
    }

    @Test
    void getStreamSource_withLocationRef_setsSystemId() throws Exception {
        DomRepresentation dr = dom();
        dr.setLocationRef(new Reference("https://example.com/catalog.xml"));
        assertEquals("https://example.com/catalog.xml", dr.getStreamSource().getSystemId());
    }

    @Test
    void getText_withMalformedXml_throwsIllegalArgumentException() {
        DomRepresentation dr =
                new DomRepresentation(new StringRepresentation("not xml", MediaType.TEXT_XML));
        assertThrows(IllegalArgumentException.class, () -> dr.getText("/a"));
    }

    @Test
    void getText_withInvalidXPathExpression_throwsIllegalArgumentException() {
        DomRepresentation dom = dom();
        assertThrows(IllegalArgumentException.class, () -> dom.getText("///invalid[["));
    }

    @Test
    void getDomSource_baseImplementation_returnsWrappedDocument() throws Exception {
        SaxRepresentation sr =
                new SaxRepresentation(new StringRepresentation(XML, MediaType.TEXT_XML));
        javax.xml.transform.dom.DOMSource source = sr.getDomSource();
        assertNotNull(source);
        assertNotNull(source.getNode());
    }

    @Test
    void getDomSource_withLocationRef_setsSystemId() throws Exception {
        StringRepresentation rep = new StringRepresentation(XML, MediaType.TEXT_XML);
        SaxRepresentation sr = new SaxRepresentation(rep);
        sr.setLocationRef(new Reference("https://example.com/loc.xml"));
        assertEquals("https://example.com/loc.xml", sr.getDomSource().getSystemId());
    }

    @Test
    void getDomSource_withMalformedXml_throwsIOException() {
        SaxRepresentation sr =
                new SaxRepresentation(new StringRepresentation("not xml", MediaType.TEXT_XML));
        assertThrows(java.io.IOException.class, sr::getDomSource);
    }

    @Test
    void getDocument_baseImplementation_parsesSourceRepresentation() throws Exception {
        SaxRepresentation sr =
                new SaxRepresentation(new StringRepresentation(XML, MediaType.TEXT_XML));
        Document document = sr.getDocument();
        assertNotNull(document);
        assertEquals("catalog", document.getDocumentElement().getTagName());
    }

    @Test
    void getDocumentBuilder_withVariousOptions_stillBuilds() throws Exception {
        DomRepresentation dr = dom();
        dr.setCoalescing(true);
        dr.setIgnoringComments(true);
        dr.setXIncludeAware(true);
        assertNotNull(dr.getDocumentBuilder());
    }

    @Test
    void appendTextContent_coversCommentCdataAndProcessingInstructionNodes() throws Exception {
        Document doc =
                javax.xml.parsers.DocumentBuilderFactory.newInstance()
                        .newDocumentBuilder()
                        .newDocument();
        assertEquals("a comment", XmlRepresentation.getTextContent(doc.createComment("a comment")));
        assertEquals(
                "cdata text",
                XmlRepresentation.getTextContent(doc.createCDATASection("cdata text")));
        assertEquals(
                "pi data",
                XmlRepresentation.getTextContent(
                        doc.createProcessingInstruction("target", "pi data")));
    }

    @Test
    void appendTextContent_coversAttributeAndDocumentFragmentNodes() throws Exception {
        Document doc =
                javax.xml.parsers.DocumentBuilderFactory.newInstance()
                        .newDocumentBuilder()
                        .newDocument();
        Attr attr = doc.createAttribute("attr");
        attr.setValue("attr-value");
        assertEquals("attr-value", XmlRepresentation.getTextContent(attr));

        DocumentFragment fragment = doc.createDocumentFragment();
        fragment.appendChild(doc.createTextNode("frag-text"));
        assertEquals("frag-text", XmlRepresentation.getTextContent(fragment));
    }

    @Test
    void appendTextContent_unhandledNodeType_returnsEmptyString() throws Exception {
        Document doc =
                javax.xml.parsers.DocumentBuilderFactory.newInstance()
                        .newDocumentBuilder()
                        .newDocument();
        // Document nodes are not text-bearing and fall into the default (no-op) branch.
        assertEquals("", XmlRepresentation.getTextContent(doc));
    }
}
