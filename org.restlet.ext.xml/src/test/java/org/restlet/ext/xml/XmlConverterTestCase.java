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
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.Test;
import org.restlet.data.MediaType;
import org.restlet.data.Preference;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.w3c.dom.Document;

/** Unit tests for {@link XmlConverter}. */
class XmlConverterTestCase {

    private static final String XML = "<?xml version=\"1.0\"?><root/>";

    private static Document buildDocument() throws Exception {
        return DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
    }

    @Test
    void getObjectClasses_withTextXmlVariant_returnsThreeClasses() {
        List<Class<?>> classes =
                new XmlConverter().getObjectClasses(new Variant(MediaType.TEXT_XML));
        assertNotNull(classes);
        assertEquals(3, classes.size());
        assertTrue(classes.contains(Document.class));
        assertTrue(classes.contains(DomRepresentation.class));
        assertTrue(classes.contains(SaxRepresentation.class));
    }

    @Test
    void getObjectClasses_withNonXmlVariant_returnsNull() {
        assertNull(new XmlConverter().getObjectClasses(new Variant(MediaType.TEXT_PLAIN)));
    }

    @Test
    void getVariants_forDocument_returnsThreeVariants() {
        assertEquals(3, new XmlConverter().getVariants(Document.class).size());
    }

    @Test
    void getVariants_forSaxRepresentation_returnsThreeVariants() {
        assertEquals(3, new XmlConverter().getVariants(SaxRepresentation.class).size());
    }

    @Test
    void getVariants_forOtherClass_returnsEmptyList() {
        assertTrue(new XmlConverter().getVariants(String.class).isEmpty());
    }

    @Test
    void score_documentWithNullTarget_returnsHalf() throws Exception {
        assertEquals(0.5f, new XmlConverter().score(buildDocument(), null, null));
    }

    @Test
    void score_documentWithTextXmlTarget_returns0_9() throws Exception {
        assertEquals(
                0.9f,
                new XmlConverter().score(buildDocument(), new Variant(MediaType.TEXT_XML), null));
    }

    @Test
    void score_documentWithApplicationXmlTarget_returns0_8() throws Exception {
        // APPLICATION_ALL_XML.isCompatible(APPLICATION_XML) is true, so the first branch wins
        assertEquals(
                0.8f,
                new XmlConverter()
                        .score(buildDocument(), new Variant(MediaType.APPLICATION_XML), null));
    }

    @Test
    void score_documentWithAllXmlTarget_returns0_8() throws Exception {
        assertEquals(
                0.8f,
                new XmlConverter()
                        .score(buildDocument(), new Variant(MediaType.APPLICATION_ALL_XML), null));
    }

    @Test
    void score_nonDocument_returnsMinusOne() {
        assertEquals(
                -1.0f, new XmlConverter().score("not xml", new Variant(MediaType.TEXT_XML), null));
    }

    @Test
    void score_repr_documentTargetWithTextXml_returns0_9() {
        assertEquals(
                0.9f,
                new XmlConverter()
                        .score(
                                new StringRepresentation(XML, MediaType.TEXT_XML),
                                Document.class,
                                null));
    }

    @Test
    void score_repr_nullTarget_returnsMinusOne() {
        assertEquals(
                -1.0f,
                new XmlConverter()
                        .score(
                                new StringRepresentation(XML, MediaType.TEXT_XML),
                                (Class<?>) null,
                                null));
    }

    @Test
    void score_repr_nonXmlTarget_returnsMinusOne() {
        assertEquals(
                -1.0f,
                new XmlConverter()
                        .score(
                                new StringRepresentation(XML, MediaType.TEXT_XML),
                                String.class,
                                null));
    }

    @Test
    void toObject_nullTarget_returnsNull() throws Exception {
        assertNull(
                new XmlConverter()
                        .toObject(new StringRepresentation(XML, MediaType.TEXT_XML), null, null));
    }

    @Test
    void toObject_toDocument_returnsDocument() throws Exception {
        Document result =
                new XmlConverter()
                        .toObject(
                                new StringRepresentation(XML, MediaType.TEXT_XML),
                                Document.class,
                                null);
        assertNotNull(result);
    }

    @Test
    void toObject_toDomRepresentation_returnsInstance() throws Exception {
        DomRepresentation result =
                new XmlConverter()
                        .toObject(
                                new StringRepresentation(XML, MediaType.TEXT_XML),
                                DomRepresentation.class,
                                null);
        assertNotNull(result);
    }

    @Test
    void toObject_toSaxRepresentation_returnsInstance() throws Exception {
        SaxRepresentation result =
                new XmlConverter()
                        .toObject(
                                new StringRepresentation(XML, MediaType.TEXT_XML),
                                SaxRepresentation.class,
                                null);
        assertNotNull(result);
    }

    @Test
    void toRepresentation_fromDocument_returnsDomRepresentation() throws Exception {
        assertInstanceOf(
                DomRepresentation.class,
                new XmlConverter()
                        .toRepresentation(buildDocument(), new Variant(MediaType.TEXT_XML), null));
    }

    @Test
    void toRepresentation_fromRepresentation_returnsSameInstance() {
        StringRepresentation rep = new StringRepresentation(XML, MediaType.TEXT_XML);
        assertSame(
                rep,
                new XmlConverter().toRepresentation(rep, new Variant(MediaType.TEXT_XML), null));
    }

    @Test
    void toRepresentation_fromOtherObject_returnsNull() {
        assertNull(
                new XmlConverter()
                        .toRepresentation("other", new Variant(MediaType.TEXT_XML), null));
    }

    @Test
    void updatePreferences_forDocument_addsThreePreferences() {
        List<Preference<MediaType>> prefs = new ArrayList<>();
        new XmlConverter().updatePreferences(prefs, Document.class);
        assertEquals(3, prefs.size());
    }

    @Test
    void updatePreferences_forOtherClass_doesNotModify() {
        List<Preference<MediaType>> prefs = new ArrayList<>();
        new XmlConverter().updatePreferences(prefs, String.class);
        assertTrue(prefs.isEmpty());
    }
}
