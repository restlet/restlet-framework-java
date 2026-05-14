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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import org.junit.jupiter.api.Test;
import org.xml.sax.helpers.AttributesImpl;

/** Unit tests for {@link XmlWriter}. */
class XmlWriterTestCase {

    private static StringWriter sw() {
        return new StringWriter();
    }

    @Test
    void basicDocument_containsXmlDeclarationAndElement() throws Exception {
        StringWriter out = sw();
        XmlWriter w = new XmlWriter(out);
        w.startDocument();
        w.startElement("root");
        w.characters("hello");
        w.endElement("root");
        w.endDocument();
        String result = out.toString();
        assertTrue(result.startsWith("<?xml version=\"1.0\" standalone='yes'?>"));
        assertTrue(result.contains("<root>"));
        assertTrue(result.contains("hello"));
        assertTrue(result.contains("</root>"));
    }

    @Test
    void dataElement_localName_writesElement() throws Exception {
        StringWriter out = sw();
        XmlWriter w = new XmlWriter(out);
        w.startDocument();
        w.dataElement("greeting", "Hello, world!");
        w.endDocument();
        assertTrue(out.toString().contains("<greeting>Hello, world!</greeting>"));
    }

    @Test
    void dataElement_uriAndLocalName_writesElement() throws Exception {
        StringWriter out = sw();
        XmlWriter w = new XmlWriter(out);
        w.startDocument();
        w.dataElement("", "name", "Alice");
        w.endDocument();
        assertTrue(out.toString().contains("<name>Alice</name>"));
    }

    @Test
    void emptyElement_localName_writesEmptyTag() throws Exception {
        StringWriter out = sw();
        XmlWriter w = new XmlWriter(out);
        w.startDocument();
        w.startElement("root");
        w.emptyElement("br");
        w.endElement("root");
        w.endDocument();
        assertTrue(out.toString().contains("<br/>"));
    }

    @Test
    void emptyElement_uriAndLocalName_writesEmptyTag() throws Exception {
        StringWriter out = sw();
        XmlWriter w = new XmlWriter(out);
        w.startDocument();
        w.startElement("root");
        w.emptyElement("", "hr");
        w.endElement("root");
        w.endDocument();
        assertTrue(out.toString().contains("<hr/>"));
    }

    @Test
    void startElement_withAttributes_writesAttributeValue() throws Exception {
        StringWriter out = sw();
        XmlWriter w = new XmlWriter(out);
        AttributesImpl atts = new AttributesImpl();
        atts.addAttribute("", "id", "id", "CDATA", "42");
        w.startDocument();
        w.startElement("", "item", "", atts);
        w.endElement("item");
        w.endDocument();
        assertTrue(out.toString().contains("id=\"42\""));
    }

    @Test
    void characters_specialChars_areEscaped() throws Exception {
        StringWriter out = sw();
        XmlWriter w = new XmlWriter(out);
        w.startDocument();
        w.startElement("root");
        w.characters("a<b>&c>d");
        w.endElement("root");
        w.endDocument();
        String result = out.toString();
        assertTrue(result.contains("&lt;"));
        assertTrue(result.contains("&amp;"));
        assertTrue(result.contains("&gt;"));
    }

    @Test
    void attributeValue_withQuote_isEscaped() throws Exception {
        StringWriter out = sw();
        XmlWriter w = new XmlWriter(out);
        AttributesImpl atts = new AttributesImpl();
        atts.addAttribute("", "val", "val", "CDATA", "say \"hello\"");
        w.startDocument();
        w.startElement("", "e", "", atts);
        w.endElement("e");
        w.endDocument();
        assertTrue(out.toString().contains("&quot;"));
    }

    @Test
    void characters_highUnicodeChar_isNumericEscaped() throws Exception {
        StringWriter out = sw();
        XmlWriter w = new XmlWriter(out);
        w.startDocument();
        w.startElement("root");
        w.characters("café");
        w.endElement("root");
        w.endDocument();
        assertTrue(out.toString().contains("&#233;"));
    }

    @Test
    void setDataFormat_isDataFormat_roundTrip() {
        XmlWriter w = new XmlWriter(sw());
        assertFalse(w.isDataFormat());
        w.setDataFormat(true);
        assertTrue(w.isDataFormat());
    }

    @Test
    void setIndentStep_getIndentStep_roundTrip() {
        XmlWriter w = new XmlWriter(sw());
        assertEquals(0, w.getIndentStep());
        w.setIndentStep(4);
        assertEquals(4, w.getIndentStep());
    }

    @Test
    void setPrefix_getPrefix_roundTrip() {
        XmlWriter w = new XmlWriter(sw());
        w.setPrefix("http://example.com/", "ex");
        assertEquals("ex", w.getPrefix("http://example.com/"));
    }

    @Test
    void forceNSDecl_withPrefix_declaresOnRootElement() throws Exception {
        StringWriter out = sw();
        XmlWriter w = new XmlWriter(out);
        w.forceNSDecl("http://example.com/ns", "ex");
        w.startDocument();
        w.startElement("root");
        w.endElement("root");
        w.endDocument();
        assertTrue(out.toString().contains("xmlns:ex=\"http://example.com/ns\""));
    }

    @Test
    void processingInstruction_writesPI() throws Exception {
        StringWriter out = sw();
        XmlWriter w = new XmlWriter(out);
        w.startDocument();
        w.processingInstruction("xml-stylesheet", "type=\"text/css\" href=\"style.css\"");
        w.startElement("root");
        w.endElement("root");
        w.endDocument();
        assertTrue(out.toString().contains("<?xml-stylesheet"));
    }

    @Test
    void getWriter_returnsNonNull() {
        assertNotNull(new XmlWriter(sw()).getWriter());
    }

    @Test
    void constructor_outputStream_writesDocument() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XmlWriter w = new XmlWriter(baos);
        w.startDocument();
        w.dataElement("root", "ok");
        w.endDocument();
        assertTrue(baos.toString().contains("<root>ok</root>"));
    }

    @Test
    void reset_inDataFormatMode_allowsNewDocument() throws Exception {
        StringWriter first = sw();
        XmlWriter w = new XmlWriter(first);
        w.setDataFormat(true);
        w.setIndentStep(2);
        w.startDocument();
        w.startElement("root");
        w.reset();
        StringWriter second = sw();
        w.setOutput(second);
        w.startDocument();
        w.dataElement("fresh", "yes");
        w.endDocument();
        assertTrue(second.toString().contains("<fresh>yes</fresh>"));
    }

    @Test
    void ignorableWhitespace_writesWhitespaceChars() throws Exception {
        StringWriter out = sw();
        XmlWriter w = new XmlWriter(out);
        w.startDocument();
        w.startElement("root");
        char[] ws = {' ', ' '};
        w.ignorableWhitespace(ws, 0, 2);
        w.endElement("root");
        w.endDocument();
        assertTrue(out.toString().contains("  "));
    }

    @Test
    void dataFormat_withIndentStep_producesNewlinesAndIndentation() throws Exception {
        StringWriter out = sw();
        XmlWriter w = new XmlWriter(out);
        w.setDataFormat(true);
        w.setIndentStep(2);
        w.startDocument();
        w.startElement("Person");
        w.dataElement("name", "Jane");
        w.endElement("Person");
        w.endDocument();
        String result = out.toString();
        assertTrue(result.contains("\n"));
        assertTrue(result.contains("  "));
    }
}
