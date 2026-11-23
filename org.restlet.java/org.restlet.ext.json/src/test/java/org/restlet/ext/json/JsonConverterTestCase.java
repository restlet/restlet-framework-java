/**
 * Copyright 2005-2024 Qlik
 *
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 *
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.ext.json;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.jupiter.api.Test;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;

/** Unit tests for {@link JsonConverter}. */
public class JsonConverterTestCase {

    // #1185: application/*+json media types are JSON.

    private static final MediaType VND_JSON = new MediaType("application/vnd.example+json");

    @Test
    void getObjectClasses_withJsonSuffixVariant_returnsThreeClasses() {
        List<Class<?>> classes = new JsonConverter().getObjectClasses(new Variant(VND_JSON));
        assertNotNull(classes);
        assertEquals(3, classes.size());
    }

    @Test
    void getObjectClasses_withXmlSuffixVariant_returnsNull() {
        assertNull(
                new JsonConverter()
                        .getObjectClasses(
                                new Variant(new MediaType("application/vnd.example+xml"))));
    }

    @Test
    void score_forJsonSuffixTarget_returnsOne() {
        assertEquals(
                1.0f, new JsonConverter().score(new JSONObject(), new Variant(VND_JSON), null));
    }

    @Test
    void scoreRepresentation_forJsonSuffixSource_returnsOne() {
        assertEquals(
                1.0f,
                new JsonConverter()
                        .score(new StringRepresentation("{}", VND_JSON), JSONObject.class, null));
    }

    // Regression tests of the conversions, kept green by the refactoring.

    @Test
    void getVariants_forJsonApiClasses_returnsApplicationJson() {
        JsonConverter converter = new JsonConverter();
        for (Class<?> clazz : List.of(JSONArray.class, JSONObject.class,
                JSONTokener.class)) {
            List<?> variants = converter.getVariants(clazz);
            assertEquals(1, variants.size());
            assertEquals(MediaType.APPLICATION_JSON,
                    ((Variant) variants.get(0)).getMediaType());
        }
    }

    @Test
    void getVariants_forOtherClass_returnsNull() {
        assertNull(new JsonConverter().getVariants(String.class));
    }

    @Test
    void scoreRepresentation_forPlainTextSource_returnsHalf() {
        assertEquals(0.5f, new JsonConverter().score(
                new StringRepresentation("{}", MediaType.TEXT_PLAIN),
                JSONObject.class, null));
    }

    @Test
    void scoreRepresentation_forOtherTarget_returnsMinusOne() {
        assertEquals(-1.0f, new JsonConverter().score(
                new StringRepresentation("{}", MediaType.APPLICATION_JSON),
                String.class, null));
    }

    @Test
    void toObject_fromJson_returnsTheRequestedType() throws IOException {
        JsonConverter converter = new JsonConverter();
        assertEquals(1, converter.toObject(new StringRepresentation("[1]",
                MediaType.APPLICATION_JSON), JSONArray.class, null).length());
        assertEquals(1, converter.toObject(new StringRepresentation(
                "{\"a\":1}", MediaType.APPLICATION_JSON), JSONObject.class,
                null).getInt("a"));
        assertNotNull(converter.toObject(new StringRepresentation("{}",
                MediaType.APPLICATION_JSON), JSONTokener.class, null));
    }

    @Test
    void toObject_forJsonRepresentationTarget_returnsTheRepresentation()
            throws IOException {
        JsonRepresentation source = new JsonRepresentation(new JSONObject());
        assertSame(source, new JsonConverter().toObject(source,
                JsonRepresentation.class, null));
    }

    @Test
    void toObject_forOtherOrNullTarget_returnsNull() throws IOException {
        JsonConverter converter = new JsonConverter();
        StringRepresentation source = new StringRepresentation("{}",
                MediaType.APPLICATION_JSON);
        assertNull(converter.toObject(source, String.class, null));
        assertNull(converter.toObject(source, null, null));
    }

    @Test
    void toObject_fromInvalidJson_throwsIoExceptionWithMessage() {
        JsonConverter converter = new JsonConverter();
        IOException e = assertThrows(IOException.class,
                () -> converter.toObject(new StringRepresentation("not json",
                        MediaType.APPLICATION_JSON), JSONObject.class, null));
        assertEquals("Unable to convert to JSON object", e.getMessage());
        assertNotNull(e.getCause());
    }

    @Test
    void toRepresentation_fromJsonApiObjects_returnsJsonRepresentation() {
        JsonConverter converter = new JsonConverter();
        Variant variant = new Variant(MediaType.APPLICATION_JSON);
        for (Object source : List.of(new JSONArray(), new JSONObject(),
                new JSONTokener("{}"))) {
            Representation result = converter.toRepresentation(source,
                    variant, null);
            assertTrue(result instanceof JsonRepresentation);
        }
        assertNull(converter.toRepresentation("text", variant, null));
    }
}
