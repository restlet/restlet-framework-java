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

import java.util.List;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.restlet.data.MediaType;
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
}
