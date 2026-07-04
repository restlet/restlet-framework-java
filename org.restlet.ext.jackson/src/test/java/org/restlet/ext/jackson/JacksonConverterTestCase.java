/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.jackson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.restlet.data.MediaType;
import org.restlet.data.Preference;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;

/** Unit tests for {@link JacksonConverter}. */
class JacksonConverterTestCase {

    private final JacksonConverter converter = new JacksonConverter();

    @Test
    void isCompatible_forSupportedMediaTypes_returnsTrue() {
        assertTrue(converter.isCompatible(new Variant(MediaType.APPLICATION_JSON)));
        assertTrue(converter.isCompatible(new Variant(MediaType.APPLICATION_JSON_SMILE)));
        assertTrue(converter.isCompatible(new Variant(MediaType.APPLICATION_XML)));
        assertTrue(converter.isCompatible(new Variant(MediaType.TEXT_XML)));
        assertTrue(converter.isCompatible(new Variant(MediaType.APPLICATION_YAML)));
        assertTrue(converter.isCompatible(new Variant(MediaType.TEXT_YAML)));
        assertTrue(converter.isCompatible(new Variant(MediaType.TEXT_CSV)));
    }

    @Test
    void isCompatible_forUnsupportedMediaType_returnsFalse() {
        assertFalse(converter.isCompatible(new Variant(MediaType.IMAGE_PNG)));
    }

    @Test
    void isCompatible_forNullVariant_returnsFalse() {
        assertFalse(converter.isCompatible(null));
    }

    @Test
    void getObjectClasses_forCompatibleVariant_returnsObjectAndJacksonRepresentation() {
        List<Class<?>> result = converter.getObjectClasses(new Variant(MediaType.APPLICATION_JSON));
        assertEquals(List.of(Object.class, JacksonRepresentation.class), result);
    }

    @Test
    void getObjectClasses_forIncompatibleVariant_returnsNull() {
        assertNull(converter.getObjectClasses(new Variant(MediaType.IMAGE_PNG)));
    }

    @Test
    void getVariants_forNonNullClass_returnsAllSupportedVariants() {
        List<?> variants = converter.getVariants(String.class);
        assertEquals(7, variants.size());
    }

    @Test
    void getVariants_forNullClass_returnsEmptyList() {
        assertTrue(converter.getVariants(null).isEmpty());
    }

    @Test
    void score_jacksonRepresentation_returnsOne() {
        JacksonRepresentation<String> source =
                new JacksonRepresentation<>(MediaType.APPLICATION_JSON, "text");
        assertEquals(1.0f, converter.score(source, new Variant(MediaType.APPLICATION_JSON), null));
    }

    @Test
    void score_nullTarget_returnsHalf() {
        assertEquals(0.5f, converter.score("text", null, null));
    }

    @Test
    void score_compatibleTarget_returnsPointEight() {
        assertEquals(0.8f, converter.score("text", new Variant(MediaType.APPLICATION_JSON), null));
    }

    @Test
    void score_incompatibleTarget_returnsHalf() {
        assertEquals(0.5f, converter.score("text", new Variant(MediaType.IMAGE_PNG), null));
    }

    @Test
    void scoreRepresentation_jacksonRepresentation_returnsOne() {
        JacksonRepresentation<String> source =
                new JacksonRepresentation<>(MediaType.APPLICATION_JSON, "text");
        assertEquals(1.0f, converter.score(source, String.class, null));
    }

    @Test
    void scoreRepresentation_forJacksonRepresentationTarget_returnsOne() {
        assertEquals(
                1.0f,
                converter.score(
                        new StringRepresentation("text", MediaType.TEXT_PLAIN),
                        JacksonRepresentation.class,
                        null));
    }

    @Test
    void scoreRepresentation_forCompatibleSource_returnsPointEight() {
        assertEquals(
                0.8f,
                converter.score(
                        new StringRepresentation("text", MediaType.APPLICATION_JSON),
                        String.class,
                        null));
    }

    @Test
    void scoreRepresentation_forIncompatibleSource_returnsMinusOne() {
        assertEquals(
                -1.0f,
                converter.score(
                        new StringRepresentation("text", MediaType.IMAGE_PNG), String.class, null));
    }

    @Test
    void toObject_fromJacksonRepresentation_returnsUnderlyingObject() throws Exception {
        JacksonRepresentation<String> source =
                new JacksonRepresentation<>(MediaType.APPLICATION_JSON, "text");
        assertEquals("text", converter.toObject(source, String.class, null));
    }

    @Test
    void toObject_fromJacksonRepresentation_forJacksonRepresentationTarget_returnsRepresentation()
            throws Exception {
        JacksonRepresentation<String> source =
                new JacksonRepresentation<>(MediaType.APPLICATION_JSON, "text");
        Object result = converter.toObject(source, JacksonRepresentation.class, null);
        assertTrue(result instanceof JacksonRepresentation<?>);
    }

    @Test
    void toObject_fromCompatibleRepresentation_returnsParsedObject() throws Exception {
        StringRepresentation source =
                new StringRepresentation("\"text\"", MediaType.APPLICATION_JSON);
        assertEquals("text", converter.toObject(source, String.class, null));
    }

    @Test
    void toObject_fromIncompatibleRepresentation_returnsNull() throws Exception {
        StringRepresentation source = new StringRepresentation("text", MediaType.IMAGE_PNG);
        assertNull(converter.toObject(source, String.class, null));
    }

    @Test
    void toRepresentation_fromJacksonRepresentation_returnsSameInstance() {
        JacksonRepresentation<String> source =
                new JacksonRepresentation<>(MediaType.APPLICATION_JSON, "text");
        Representation result =
                converter.toRepresentation(source, new Variant(MediaType.APPLICATION_JSON), null);
        assertEquals(source, result);
    }

    @Test
    void toRepresentation_withoutMediaType_defaultsToJson() {
        Variant target = new Variant();
        Representation result = converter.toRepresentation("text", target, null);
        assertEquals(MediaType.APPLICATION_JSON, target.getMediaType());
        assertTrue(result instanceof JacksonRepresentation<?>);
    }

    @Test
    void toRepresentation_forIncompatibleTarget_returnsNull() {
        Representation result =
                converter.toRepresentation("text", new Variant(MediaType.IMAGE_PNG), null);
        assertNull(result);
    }

    @Test
    void updatePreferences_addsAllSupportedMediaTypesWithFullPriority() {
        List<Preference<MediaType>> preferences = new ArrayList<>();
        converter.updatePreferences(preferences, String.class);
        assertEquals(7, preferences.size());
        for (Preference<MediaType> preference : preferences) {
            assertEquals(1.0f, preference.getQuality());
        }
    }
}
