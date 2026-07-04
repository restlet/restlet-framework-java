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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.restlet.Component;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.CharacterSet;
import org.restlet.data.Encoding;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.routing.Filter;

/**
 * Test case for the Transformer class.
 *
 * @author Jerome Louvel
 */
class TransformerTestCase {

    static class FailureTracker {
        final StringBuffer trackedMessages = new StringBuffer();

        void report() {
            if (!trackedMessages.isEmpty()) {
                fail("TRACKER REPORT: \n" + this.trackedMessages);
            }
        }

        void trackFailure(String message) {
            this.trackedMessages.append(message).append("\n");
        }

        void trackFailure(String message, int index, Throwable e) {
            trackFailure(message + " " + index + ": " + e.getMessage());
        }
    }

    final String output = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><buyer>cust123</buyer>23.45";

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

    // Create a transform XSLT sheet
    final Representation xslt =
            new StringRepresentation(
                    "<?xml version=\"1.0\"?>"
                            + "<xsl:transform xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" version=\"1.0\">"
                            + "<xsl:template match =\"customer\">"
                            + "<buyer><xsl:value-of select=\"@db\"/></buyer>"
                            + "</xsl:template>"
                            + "</xsl:transform>",
                    MediaType.TEXT_XML);

    @Test
    void parallelTestTransform() {
        Component comp = new Component();
        final TransformRepresentation tr =
                new TransformRepresentation(comp.getContext(), this.source, this.xslt);
        final FailureTracker tracker = new FailureTracker();

        final int testVolume = 500;
        final Thread[] parallelTransform = new Thread[testVolume];
        for (int i = 0; i < parallelTransform.length; i++) {
            final int index = i;
            parallelTransform[i] =
                    new Thread(
                            () -> {
                                try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                                    tr.write(out);
                                    final String result = out.toString();
                                    assertEquals(TransformerTestCase.this.output, result);
                                } catch (IOException e) {
                                    tracker.trackFailure(
                                            "Exception during write in thread ", index, e);
                                }
                            });
        }

        for (final Thread pt : parallelTransform) {
            pt.start();
        }

        tracker.report();
    }

    @Test
    void testTransform() throws Exception {
        final Transformer transformer = new Transformer(Transformer.MODE_REQUEST, this.xslt);
        final String result = transformer.transform(this.source).getText();

        assertEquals(this.output, result);
    }

    @Test
    void constructor_setsDefaults() {
        Transformer transformer = new Transformer(Transformer.MODE_REQUEST, this.xslt);
        assertEquals(Transformer.MODE_REQUEST, transformer.getMode());
        assertSame(this.xslt, transformer.getTransformSheet());
        assertEquals(MediaType.APPLICATION_XML, transformer.getResultMediaType());
        assertNull(transformer.getResultCharacterSet());
    }

    @Test
    void gettersAndSetters_roundTrip() {
        Transformer transformer = new Transformer(Transformer.MODE_REQUEST, this.xslt);

        transformer.setMode(Transformer.MODE_RESPONSE);
        assertEquals(Transformer.MODE_RESPONSE, transformer.getMode());

        transformer.setResultCharacterSet(CharacterSet.UTF_8);
        assertEquals(CharacterSet.UTF_8, transformer.getResultCharacterSet());

        transformer.setResultMediaType(MediaType.TEXT_XML);
        assertEquals(MediaType.TEXT_XML, transformer.getResultMediaType());

        Representation otherSheet = new StringRepresentation("sheet");
        transformer.setTransformSheet(otherSheet);
        assertSame(otherSheet, transformer.getTransformSheet());

        assertTrue(transformer.getResultEncodings().isEmpty());
        transformer.setResultEncodings(List.of(Encoding.GZIP));
        assertEquals(List.of(Encoding.GZIP), transformer.getResultEncodings());

        assertTrue(transformer.getResultLanguages().isEmpty());
        transformer.setResultLanguages(List.of(Language.ENGLISH));
        assertEquals(List.of(Language.ENGLISH), transformer.getResultLanguages());
    }

    @Test
    void canTransform_alwaysReturnsTrue() {
        Transformer transformer = new Transformer(Transformer.MODE_REQUEST, this.xslt);
        assertTrue(transformer.canTransform(null));
        assertTrue(transformer.canTransform(this.source));
    }

    @Test
    void beforeHandle_requestMode_transformsRequestEntity() {
        Transformer transformer = new Transformer(Transformer.MODE_REQUEST, this.xslt);
        Request request = new Request();
        request.setEntity(this.source);
        Response response = new Response(request);

        int result = transformer.beforeHandle(request, response);

        assertEquals(Filter.CONTINUE, result);
        assertTrue(request.getEntity() instanceof TransformRepresentation);
    }

    @Test
    void beforeHandle_responseMode_doesNotTransformRequestEntity() {
        Transformer transformer = new Transformer(Transformer.MODE_RESPONSE, this.xslt);
        Request request = new Request();
        request.setEntity(this.source);
        Response response = new Response(request);

        transformer.beforeHandle(request, response);

        assertSame(this.source, request.getEntity());
    }

    @Test
    void afterHandle_responseMode_transformsResponseEntity() {
        Transformer transformer = new Transformer(Transformer.MODE_RESPONSE, this.xslt);
        Request request = new Request();
        Response response = new Response(request);
        response.setEntity(this.source);

        transformer.afterHandle(request, response);

        assertTrue(response.getEntity() instanceof TransformRepresentation);
    }

    @Test
    void afterHandle_requestMode_doesNotTransformResponseEntity() {
        Transformer transformer = new Transformer(Transformer.MODE_REQUEST, this.xslt);
        Request request = new Request();
        Response response = new Response(request);
        response.setEntity(this.source);

        transformer.afterHandle(request, response);

        assertSame(this.source, response.getEntity());
    }

    @Test
    void transform_withResultLanguagesAndEncodings_appliesThemToResult() {
        Transformer transformer = new Transformer(Transformer.MODE_REQUEST, this.xslt);
        transformer.setResultLanguages(List.of(Language.FRENCH));
        transformer.setResultEncodings(List.of(Encoding.GZIP));
        transformer.setResultCharacterSet(CharacterSet.UTF_8);
        transformer.setResultMediaType(MediaType.TEXT_XML);

        Representation result = transformer.transform(this.source);

        assertTrue(result.getLanguages().contains(Language.FRENCH));
        assertTrue(result.getEncodings().contains(Encoding.GZIP));
        assertEquals(CharacterSet.UTF_8, result.getCharacterSet());
        assertEquals(MediaType.TEXT_XML, result.getMediaType());
    }
}
