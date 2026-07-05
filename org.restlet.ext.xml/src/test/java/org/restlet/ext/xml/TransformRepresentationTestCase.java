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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.junit.jupiter.api.Test;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.xml.sax.XMLFilter;

/**
 * Test case for the {@link TransformRepresentation} class.
 *
 * @author Jerome Louvel
 */
class TransformRepresentationTestCase {

    final String output1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><buyer>cust123</buyer>";

    final String output2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><myBuyer>cust123</myBuyer>";

    // Create a source XML document
    final Representation source =
            new StringRepresentation(
                    "<?xml version=\"1.0\"?>"
                            + "<purchase id=\"p001\">"
                            + "<customer db=\"cust123\"/>"
                            + "<product db=\"prod345\">"
                            + "<amount>23.45</amount>"
                            + "</product>"
                            + "</purchase>",
                    MediaType.TEXT_XML);

    // Create a first transform XSLT sheet
    final Representation xslt1 =
            new StringRepresentation(
                    "<?xml version=\"1.0\"?>"
                            + "<xsl:transform xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" version=\"1.0\">"
                            + "<xsl:template match =\"customer\">"
                            + "<buyer><xsl:value-of select=\"@db\"/></buyer>"
                            + "</xsl:template><xsl:template match =\"amount\"/>"
                            + "</xsl:transform>",
                    MediaType.TEXT_XML);

    // Create a second transform XSLT sheet
    final Representation xslt2 =
            new StringRepresentation(
                    "<?xml version=\"1.0\"?>"
                            + "<xsl:transform xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" version=\"1.0\">"
                            + "<xsl:template match =\"buyer\">"
                            + "<myBuyer><xsl:value-of select=\"text()\"/></myBuyer>"
                            + "</xsl:template>"
                            + "</xsl:transform>",
                    MediaType.TEXT_XML);

    @Test
    void testSingleTransform() throws Exception {
        TransformRepresentation tr1 = new TransformRepresentation(this.source, this.xslt1);
        final String result = tr1.getText();
        assertEquals(this.output1, result);
    }

    @Test
    void testDoubleTransform() throws Exception {
        TransformRepresentation tr1 = new TransformRepresentation(this.source, this.xslt1);
        TransformRepresentation tr2 = new TransformRepresentation(tr1, this.xslt2);
        final String result = tr2.getText();
        assertEquals(this.output2, result);
    }

    @Test
    void toSaxSource_withXmlRepresentationSource_usesItsSaxSource() throws Exception {
        DomRepresentation domSource = new DomRepresentation(this.source);
        TransformRepresentation tr = new TransformRepresentation(domSource, this.xslt1);
        assertEquals(this.output1, tr.getText());
    }

    @Test
    void toSaxSource_defaultCase_setsSystemIdFromLocationRef() throws Exception {
        StringRepresentation rep = new StringRepresentation("<a/>", MediaType.TEXT_XML);
        rep.setLocationRef(new Reference("http://example.com/src.xml"));
        assertEquals(
                "http://example.com/src.xml",
                TransformRepresentation.toSaxSource(rep).getSystemId());
    }

    @Test
    void constructor_withPrecompiledTemplates_transformsUsingThem() throws Exception {
        Templates templates =
                TransformerFactory.newInstance()
                        .newTemplates(new StreamSource(this.xslt1.getStream()));
        TransformRepresentation tr =
                new TransformRepresentation((URIResolver) null, this.source, templates);
        assertEquals(this.output1, tr.getText());
    }

    @Test
    void getTransformerHandler_returnsHandlerWhenTemplatesAvailable() throws Exception {
        TransformRepresentation tr = new TransformRepresentation(this.source, this.xslt1);
        TransformerHandler handler = tr.getTransformerHandler();
        assertNotNull(handler);
    }

    @Test
    void getTransformerHandler_withoutTransformSheetOrTemplates_returnsNull() throws Exception {
        TransformRepresentation tr =
                new TransformRepresentation(this.source, (Representation) null);
        assertNull(tr.getTransformerHandler());
    }

    @Test
    void getXmlFilter_returnsFilterWhenTemplatesAvailable() throws Exception {
        TransformRepresentation tr = new TransformRepresentation(this.source, this.xslt1);
        XMLFilter filter = tr.getXmlFilter();
        assertNotNull(filter);
    }

    @Test
    void getXmlFilter_withoutTransformSheetOrTemplates_returnsNull() throws Exception {
        TransformRepresentation tr =
                new TransformRepresentation(this.source, (Representation) null);
        assertNull(tr.getXmlFilter());
    }

    @Test
    void getTemplates_withTransformSheetLocationRef_setsSystemId() throws Exception {
        StringRepresentation sheetWithLoc =
                new StringRepresentation(
                        "<?xml version=\"1.0\"?>"
                                + "<xsl:transform xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" version=\"1.0\">"
                                + "<xsl:template match=\"customer\">"
                                + "<buyer><xsl:value-of select=\"@db\"/></buyer>"
                                + "</xsl:template>"
                                + "</xsl:transform>",
                        MediaType.TEXT_XML);
        sheetWithLoc.setLocationRef(new Reference("http://example.com/sheet.xsl"));
        TransformRepresentation tr = new TransformRepresentation(this.source, sheetWithLoc);
        assertNotNull(tr.getTemplates());
    }

    @Test
    void getTransformer_appliesErrorListenerUriResolverParametersAndOutputProperties()
            throws Exception {
        TransformRepresentation tr = new TransformRepresentation(this.source, this.xslt1);
        ErrorListener listener =
                new ErrorListener() {
                    @Override
                    public void warning(TransformerException exception) {}

                    @Override
                    public void error(TransformerException exception) {}

                    @Override
                    public void fatalError(TransformerException exception)
                            throws TransformerException {
                        throw exception;
                    }
                };
        tr.setErrorListener(listener);
        tr.setUriResolver((href, base) -> null);
        tr.getParameters().put("p1", "v1");
        tr.getOutputProperties().put(javax.xml.transform.OutputKeys.INDENT, "yes");

        Transformer transformer = tr.getTransformer();

        assertNotNull(transformer);
        assertEquals("v1", transformer.getParameter("p1"));
    }

    @Test
    void release_clearsSourceTransformSheetTemplatesAndUriResolver() throws Exception {
        TransformRepresentation tr =
                new TransformRepresentation((href, base) -> null, this.source, this.xslt1);
        // Force templates to be lazily created before release.
        assertNotNull(tr.getTemplates());
        assertNotNull(tr.getSourceRepresentation());
        assertNotNull(tr.getTransformSheet());
        assertNotNull(tr.getUriResolver());

        tr.release();

        assertNull(tr.getSourceRepresentation());
        assertNull(tr.getTransformSheet());
        assertNull(tr.getUriResolver());
    }

    @Test
    void settersAndGetters_roundTrip() throws Exception {
        TransformRepresentation tr = new TransformRepresentation(this.source, this.xslt1);

        ErrorListener listener =
                new ErrorListener() {
                    @Override
                    public void warning(TransformerException exception) {}

                    @Override
                    public void error(TransformerException exception) {}

                    @Override
                    public void fatalError(TransformerException exception) {}
                };
        tr.setErrorListener(listener);
        assertSame(listener, tr.getErrorListener());

        Map<String, String> outputProps = new HashMap<>();
        tr.setOutputProperties(outputProps);
        assertSame(outputProps, tr.getOutputProperties());

        Map<String, Object> params = new HashMap<>();
        tr.setParameters(params);
        assertSame(params, tr.getParameters());

        Representation newSource = new StringRepresentation("<a/>", MediaType.TEXT_XML);
        tr.setSourceRepresentation(newSource);
        assertSame(newSource, tr.getSourceRepresentation());

        Templates templates =
                TransformerFactory.newInstance()
                        .newTemplates(new StreamSource(this.xslt1.getStream()));
        tr.setTemplates(templates);
        assertSame(templates, tr.getTemplates());

        Representation newSheet = new StringRepresentation("<b/>", MediaType.TEXT_XML);
        tr.setTransformSheet(newSheet);
        assertSame(newSheet, tr.getTransformSheet());

        URIResolver resolver = (href, base) -> null;
        tr.setUriResolver(resolver);
        assertSame(resolver, tr.getUriResolver());
    }

    @Test
    void transform_withoutTransformSheetOrTemplates_logsWarningAndDoesNothing() throws Exception {
        TransformRepresentation tr =
                new TransformRepresentation(this.source, (Representation) null);
        StreamResult result = new StreamResult(new StringWriter());
        tr.transform(new StreamSource(new StringReader("<a/>")), result);
    }

    @Test
    void doubleTransform_withFailingInnerTransform_wrapsTransformerExceptionAsIOException() {
        Representation failingXslt =
                new StringRepresentation(
                        "<?xml version=\"1.0\"?>"
                                + "<xsl:transform xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" version=\"1.0\">"
                                + "<xsl:template match=\"/\">"
                                + "<xsl:message terminate=\"yes\">boom</xsl:message>"
                                + "</xsl:template>"
                                + "</xsl:transform>",
                        MediaType.TEXT_XML);
        TransformRepresentation tr1 = new TransformRepresentation(this.source, failingXslt);
        TransformRepresentation tr2 = new TransformRepresentation(tr1, this.xslt2);
        assertThrows(IOException.class, tr2::getText);
    }
}
