/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;
import org.restlet.representation.Variant;

class VariantInfoTestCase {

    @Test
    void constructor_mediaTypeOnly_setsDefaultInputScore() {
        VariantInfo variant = new VariantInfo(MediaType.APPLICATION_JSON);
        assertEquals(MediaType.APPLICATION_JSON, variant.getMediaType());
        assertNull(variant.getAnnotationInfo());
        assertEquals(1.0f, variant.getInputScore());
    }

    @Test
    void constructor_fromVariant_copiesCharacterSetEncodingsAndLanguages() {
        Variant source = new Variant(MediaType.TEXT_PLAIN);
        source.setCharacterSet(CharacterSet.UTF_8);
        source.setLanguages(Collections.singletonList(org.restlet.data.Language.ENGLISH));

        VariantInfo variant = new VariantInfo(source, null);

        assertEquals(MediaType.TEXT_PLAIN, variant.getMediaType());
        assertEquals(CharacterSet.UTF_8, variant.getCharacterSet());
        assertEquals(1, variant.getLanguages().size());
    }

    @Test
    void setInputScore_updatesValue() {
        VariantInfo variant = new VariantInfo(MediaType.APPLICATION_JSON);
        variant.setInputScore(0.5f);
        assertEquals(0.5f, variant.getInputScore());
    }

    @Test
    void equals_sameMediaTypeAndAnnotationInfo_returnsTrue() {
        VariantInfo variant1 = new VariantInfo(MediaType.APPLICATION_JSON, null);
        VariantInfo variant2 = new VariantInfo(MediaType.APPLICATION_JSON, null);
        assertTrue(variant1.equals(variant2));
        assertEquals(variant1.hashCode(), variant2.hashCode());
    }

    @Test
    void equals_differentMediaType_returnsFalse() {
        VariantInfo variant1 = new VariantInfo(MediaType.APPLICATION_JSON, null);
        VariantInfo variant2 = new VariantInfo(MediaType.TEXT_PLAIN, null);
        assertFalse(variant1.equals(variant2));
    }

    @Test
    void equals_differentType_returnsFalse() {
        VariantInfo variant = new VariantInfo(MediaType.APPLICATION_JSON, null);
        assertFalse(variant.equals("not a variant"));
    }

    @Test
    void equals_sameInstance_returnsTrue() {
        VariantInfo variant = new VariantInfo(MediaType.APPLICATION_JSON, null);
        assertTrue(variant.equals(variant));
    }
}
