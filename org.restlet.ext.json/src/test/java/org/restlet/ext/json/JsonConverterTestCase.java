/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.json;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.jupiter.api.Test;
import org.restlet.data.MediaType;
import org.restlet.data.Preference;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;

/** Unit tests for {@link JsonConverter}. */
class JsonConverterTestCase {

    @Test
    void getObjectClasses_withJsonVariant_returnsThreeClasses() {
        List<Class<?>> classes =
                new JsonConverter().getObjectClasses(new Variant(MediaType.APPLICATION_JSON));
        assertNotNull(classes);
        assertEquals(3, classes.size());
        assertTrue(classes.contains(JSONArray.class));
        assertTrue(classes.contains(JSONObject.class));
        assertTrue(classes.contains(JSONTokener.class));
    }

    @Test
    void getObjectClasses_withNonJsonVariant_returnsNull() {
        assertNull(new JsonConverter().getObjectClasses(new Variant(MediaType.TEXT_PLAIN)));
    }

    @Test
    void getVariants_forJsonArray_returnsApplicationJson() {
        List<?> variants = new JsonConverter().getVariants(JSONArray.class);
        assertEquals(1, variants.size());
        assertTrue(
                MediaType.APPLICATION_JSON.isCompatible(
                        ((Variant) variants.getFirst()).getMediaType()));
    }

    @Test
    void getVariants_forJsonObject_returnsApplicationJson() {
        List<?> variants = new JsonConverter().getVariants(JSONObject.class);
        assertEquals(1, variants.size());
        assertTrue(
                MediaType.APPLICATION_JSON.isCompatible(
                        ((Variant) variants.getFirst()).getMediaType()));
    }

    @Test
    void getVariants_forJsonTokener_returnsApplicationJson() {
        List<?> variants = new JsonConverter().getVariants(JSONTokener.class);
        assertEquals(1, variants.size());
        assertTrue(
                MediaType.APPLICATION_JSON.isCompatible(
                        ((Variant) variants.getFirst()).getMediaType()));
    }

    @Test
    void getVariants_forOtherClass_returnsEmptyList() {
        assertTrue(new JsonConverter().getVariants(String.class).isEmpty());
    }

    @Test
    void score_jsonObjectWithNullTarget_returnsHalf() {
        assertEquals(0.5f, new JsonConverter().score(new JSONObject(), null, null));
    }

    @Test
    void score_jsonObjectWithJsonVariant_returnsOne() {
        assertEquals(
                1.0f,
                new JsonConverter()
                        .score(new JSONObject(), new Variant(MediaType.APPLICATION_JSON), null));
    }

    @Test
    void score_jsonObjectWithOtherMediaType_returnsHalf() {
        assertEquals(
                0.5f,
                new JsonConverter()
                        .score(new JSONObject(), new Variant(MediaType.TEXT_PLAIN), null));
    }

    @Test
    void score_nonJsonObject_returnsMinusOne() {
        assertEquals(
                -1.0f,
                new JsonConverter().score("string", new Variant(MediaType.APPLICATION_JSON), null));
    }

    @Test
    void score_repr_withNullTarget_returnsMinusOne() {
        assertEquals(
                -1.0f,
                new JsonConverter().score(new JsonRepresentation("{}"), (Class<?>) null, null));
    }

    @Test
    void score_repr_forJsonRepresentationTarget_returnsOne() {
        assertEquals(
                1.0f,
                new JsonConverter()
                        .score(new JsonRepresentation("{}"), JsonRepresentation.class, null));
    }

    @Test
    void score_repr_forJsonArrayTargetWithJsonMedia_returnsOne() {
        assertEquals(
                1.0f,
                new JsonConverter().score(new JsonRepresentation("[]"), JSONArray.class, null));
    }

    @Test
    void score_repr_forJsonArrayTargetWithOtherMedia_returnsHalf() {
        assertEquals(
                0.5f,
                new JsonConverter()
                        .score(
                                new StringRepresentation("[]", MediaType.TEXT_PLAIN),
                                JSONArray.class,
                                null));
    }

    @Test
    void score_repr_forOtherTarget_returnsMinusOne() {
        assertEquals(
                -1.0f, new JsonConverter().score(new JsonRepresentation("{}"), String.class, null));
    }

    @Test
    void toObject_withNullTarget_returnsNull() throws Exception {
        assertNull(new JsonConverter().toObject(new JsonRepresentation("{}"), null, null));
    }

    @Test
    void toObject_toJsonArray_returnsJsonArray() throws Exception {
        JSONArray result =
                new JsonConverter()
                        .toObject(new JsonRepresentation("[1,2,3]"), JSONArray.class, null);
        assertNotNull(result);
        assertEquals(3, result.length());
    }

    @Test
    void toObject_toJsonObject_returnsJsonObject() throws Exception {
        JSONObject result =
                new JsonConverter()
                        .toObject(new JsonRepresentation("{\"k\":\"v\"}"), JSONObject.class, null);
        assertNotNull(result);
        assertEquals("v", result.getString("k"));
    }

    @Test
    void toObject_toJsonTokener_returnsJsonTokener() throws Exception {
        JSONTokener result =
                new JsonConverter()
                        .toObject(new JsonRepresentation("{\"k\":\"v\"}"), JSONTokener.class, null);
        assertNotNull(result);
    }

    @Test
    void toObject_toJsonRepresentation_returnsJsonRepresentation() throws Exception {
        JsonRepresentation result =
                new JsonConverter()
                        .toObject(new JsonRepresentation("{}"), JsonRepresentation.class, null);
        assertNotNull(result);
    }

    @Test
    void toRepresentation_fromJsonArray_returnsJsonRepresentation() {
        Representation result =
                new JsonConverter()
                        .toRepresentation(
                                new JSONArray("[1,2,3]"),
                                new Variant(MediaType.APPLICATION_JSON),
                                null);
        assertInstanceOf(JsonRepresentation.class, result);
    }

    @Test
    void toRepresentation_fromJsonObject_returnsJsonRepresentation() {
        Representation result =
                new JsonConverter()
                        .toRepresentation(
                                new JSONObject("{\"k\":\"v\"}"),
                                new Variant(MediaType.APPLICATION_JSON),
                                null);
        assertInstanceOf(JsonRepresentation.class, result);
    }

    @Test
    void toRepresentation_fromJsonTokener_returnsJsonRepresentation() {
        Representation result =
                new JsonConverter()
                        .toRepresentation(
                                new JSONTokener("{\"k\":\"v\"}"),
                                new Variant(MediaType.APPLICATION_JSON),
                                null);
        assertInstanceOf(JsonRepresentation.class, result);
    }

    @Test
    void toRepresentation_fromOtherObject_returnsNull() {
        assertNull(
                new JsonConverter()
                        .toRepresentation("string", new Variant(MediaType.APPLICATION_JSON), null));
    }

    @Test
    void updatePreferences_forJsonArray_addsApplicationJsonPreference() {
        List<Preference<MediaType>> prefs = new ArrayList<>();
        new JsonConverter().updatePreferences(prefs, JSONArray.class);
        assertEquals(1, prefs.size());
        assertEquals(MediaType.APPLICATION_JSON, prefs.getFirst().getMetadata());
    }

    @Test
    void updatePreferences_forOtherClass_doesNotModifyList() {
        List<Preference<MediaType>> prefs = new ArrayList<>();
        new JsonConverter().updatePreferences(prefs, String.class);
        assertTrue(prefs.isEmpty());
    }
}
