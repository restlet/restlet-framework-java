/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Preference;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.ObjectRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;

class DefaultConverterTestCase {

    private final DefaultConverter converter = new DefaultConverter();

    @Test
    void getObjectClasses_defaultVariant_includesBaseClasses() {
        List<Class<?>> classes = converter.getObjectClasses(new Variant());
        assertTrue(classes.contains(String.class));
        assertTrue(classes.contains(InputStream.class));
        assertTrue(classes.contains(Reader.class));
    }

    @Test
    void getObjectClasses_formVariant_includesFormClass() {
        List<Class<?>> classes =
                converter.getObjectClasses(new Variant(MediaType.APPLICATION_WWW_FORM));
        assertTrue(classes.contains(Form.class));
    }

    @Test
    void getVariants_stringClass_returnsAllVariant() {
        assertEquals(1, converter.getVariants(String.class).size());
    }

    @Test
    void getVariants_formClass_returnsFormVariant() {
        assertEquals(1, converter.getVariants(Form.class).size());
    }

    @Test
    void getVariants_nullClass_returnsEmptyList() {
        assertTrue(converter.getVariants(null).isEmpty());
    }

    @Test
    void getVariants_unrelatedClass_returnsEmptyList() {
        assertTrue(converter.getVariants(Object.class).isEmpty());
    }

    @Test
    void score_string_returnsMaxScore() {
        assertEquals(1.0F, converter.score("text", null, null));
    }

    @Test
    void score_inputStream_returnsMaxScore() {
        assertEquals(1.0F, converter.score(new ByteArrayInputStream(new byte[0]), null, null));
    }

    @Test
    void score_form_compatibleTarget_returnsMaxScore() {
        Form form = new Form();
        assertEquals(
                1.0F, converter.score(form, new Variant(MediaType.APPLICATION_WWW_FORM), null));
    }

    @Test
    void score_form_incompatibleTarget_returnsLowerScore() {
        Form form = new Form();
        assertEquals(0.6F, converter.score(form, new Variant(MediaType.TEXT_PLAIN), null));
    }

    @Test
    void score_null_returnsNegative() {
        assertEquals(-1.0F, converter.score(null, (Variant) null, null));
    }

    @Test
    void score_representationTarget_stringClass_returnsMaxScore() {
        Representation source = new StringRepresentation("body");
        assertEquals(1.0F, converter.score(source, String.class, null));
    }

    @Test
    void score_representationTarget_nullTargetNonObjectRepresentation_returnsNegative() {
        Representation source = new StringRepresentation("body");
        assertEquals(-1.0F, converter.score(source, (Class<?>) null, null));
    }

    @Test
    void score_representationTarget_fileClassWithNonFileRepresentation_returnsNegative() {
        Representation source = new StringRepresentation("body");
        assertEquals(-1.0F, converter.score(source, java.io.File.class, null));
    }

    @Test
    void score_representationTarget_formClassCompatible_returnsMaxScore() {
        Representation source = new StringRepresentation("a=b", MediaType.APPLICATION_WWW_FORM);
        assertEquals(1.0F, converter.score(source, Form.class, null));
    }

    @Test
    void toObject_targetIsSourceType_returnsSourceUnchanged() throws IOException {
        Representation source = new StringRepresentation("body");
        assertEquals(source, converter.toObject(source, Representation.class, null));
    }

    @Test
    void toObject_targetIsString_returnsText() throws IOException {
        Representation source = new StringRepresentation("hello");
        assertEquals("hello", converter.toObject(source, String.class, null));
    }

    @Test
    void toObject_targetIsForm_parsesFormEntity() throws IOException {
        Representation source = new StringRepresentation("a=b", MediaType.APPLICATION_WWW_FORM);
        Form form = converter.toObject(source, Form.class, null);
        assertEquals("b", form.getFirstValue("a"));
    }

    @Test
    void toObject_targetIsInputStream_returnsStream() throws IOException {
        Representation source = new StringRepresentation("hello");
        assertInstanceOf(InputStream.class, converter.toObject(source, InputStream.class, null));
    }

    @Test
    void toObject_targetIsInputRepresentation_wrapsStream() throws IOException {
        Representation source = new StringRepresentation("hello");
        assertInstanceOf(
                InputRepresentation.class,
                converter.toObject(source, InputRepresentation.class, null));
    }

    @Test
    void toObject_targetIsReader_returnsReader() throws IOException {
        Representation source = new StringRepresentation("hello");
        assertInstanceOf(Reader.class, converter.toObject(source, Reader.class, null));
    }

    @Test
    void toObject_unrelatedNullTarget_nonObjectRepresentation_returnsNull() throws IOException {
        Representation source = new StringRepresentation("hello");
        assertNull(converter.toObject(source, null, null));
    }

    @Test
    void toRepresentation_string_usesTextPlainByDefault() throws IOException {
        Representation result = converter.toRepresentation("hello", new Variant(), null);
        assertEquals("hello", result.getText());
        assertEquals(MediaType.TEXT_PLAIN, result.getMediaType());
    }

    @Test
    void toRepresentation_inputStream_usesOctetStreamByDefault() throws IOException {
        Representation result =
                converter.toRepresentation(
                        new ByteArrayInputStream("hi".getBytes()), new Variant(), null);
        assertEquals(MediaType.APPLICATION_OCTET_STREAM, result.getMediaType());
    }

    @Test
    void toRepresentation_reader_usesTextPlainByDefault() throws IOException {
        Representation result =
                converter.toRepresentation(new StringReader("hi"), new Variant(), null);
        assertEquals("hi", result.getText());
    }

    @Test
    void toRepresentation_representation_returnsUnchanged() throws IOException {
        Representation source = new StringRepresentation("hi");
        assertEquals(source, converter.toRepresentation(source, new Variant(), null));
    }

    @Test
    void toRepresentation_form_returnsWebRepresentation() throws IOException {
        Form form = new Form();
        form.add("a", "b");
        Representation result = converter.toRepresentation(form, new Variant(), null);
        assertEquals(MediaType.APPLICATION_WWW_FORM, result.getMediaType());
    }

    @Test
    void toRepresentation_serializable_returnsObjectRepresentation() throws IOException {
        Representation result =
                converter.toRepresentation(
                        "hello", new Variant(MediaType.APPLICATION_JAVA_OBJECT), null);
        assertTrue(
                result instanceof ObjectRepresentation<?>
                        || result instanceof StringRepresentation);
    }

    @Test
    void toRepresentation_null_returnsNull() throws IOException {
        assertNull(converter.toRepresentation(null, new Variant(), null));
    }

    @Test
    void updatePreferences_form_addsFormPreference() {
        List<Preference<MediaType>> preferences = new ArrayList<>();
        converter.updatePreferences(preferences, Form.class);
        assertEquals(MediaType.APPLICATION_WWW_FORM, preferences.getFirst().getMetadata());
    }

    @Test
    void updatePreferences_string_addsTextPreferences() {
        List<Preference<MediaType>> preferences = new ArrayList<>();
        converter.updatePreferences(preferences, String.class);
        assertTrue(
                preferences.stream().anyMatch(p -> p.getMetadata().equals(MediaType.TEXT_PLAIN)));
    }

    @Test
    void updatePreferences_inputStream_addsOctetStreamPreferences() {
        List<Preference<MediaType>> preferences = new ArrayList<>();
        converter.updatePreferences(preferences, InputStream.class);
        assertTrue(
                preferences.stream()
                        .anyMatch(p -> p.getMetadata().equals(MediaType.APPLICATION_OCTET_STREAM)));
    }

    @Test
    void updatePreferences_unrelatedClass_leavesPreferencesEmpty() {
        List<Preference<MediaType>> preferences = new ArrayList<>();
        converter.updatePreferences(preferences, Object.class);
        assertTrue(preferences.isEmpty());
    }
}
