/**
 * Copyright 2005-2024 Qlik
 *
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 *
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.ext.jackson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;

/** Unit tests for {@link JacksonConverter}. */
public class JacksonConverterTestCase {

    private final JacksonConverter converter = new JacksonConverter();

    // #1185: application/*+json media types are JSON.

    private static final MediaType VND_JSON = new MediaType("application/vnd.example+json");

    @Test
    void isCompatible_forJsonSuffixMediaType_returnsTrue() {
        assertTrue(converter.isCompatible(new Variant(VND_JSON)));
    }

    @Test
    void isCompatible_forXmlSuffixMediaType_returnsFalse() {
        assertFalse(
                converter.isCompatible(new Variant(new MediaType("application/vnd.example+xml"))));
    }

    @Test
    void score_forJsonSuffixTarget_returnsPointEight() {
        assertEquals(0.8f, converter.score("text", new Variant(VND_JSON), null));
    }

    @Test
    void score_forJsonSuffixTarget_beatsAConverterScoringPointSeven() {
        assertTrue(converter.score("text", new Variant(VND_JSON), null) > 0.7f);
    }

    @Test
    void scoreRepresentation_forJsonSuffixSource_returnsPointEight() {
        assertEquals(
                0.8f,
                converter.score(new StringRepresentation("text", VND_JSON), String.class, null));
    }

    @Test
    void toObject_fromJsonSuffixRepresentation_returnsParsedObject() throws Exception {
        StringRepresentation source = new StringRepresentation("\"text\"", VND_JSON);
        assertEquals("text", converter.toObject(source, String.class, null));
    }

    @Test
    void toRepresentation_forJsonSuffixTarget_keepsMediaType() {
        Representation result = converter.toRepresentation("text", new Variant(VND_JSON), null);
        assertTrue(result instanceof JacksonRepresentation<?>);
        assertEquals(VND_JSON, result.getMediaType());
    }
}
