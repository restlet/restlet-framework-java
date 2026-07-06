/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.representation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.restlet.data.CharacterSet;
import org.restlet.data.ClientInfo;
import org.restlet.data.Encoding;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;

/**
 * Unit tests for the {@link Variant} class.
 *
 * @author Jerome Louvel
 */
class VariantTestCase {

    @Test
    void defaultConstructor_hasNoMediaType() {
        Variant variant = new Variant();

        assertNull(variant.getMediaType());
        assertTrue(variant.getLanguages().isEmpty());
        assertTrue(variant.getEncodings().isEmpty());
    }

    @Test
    void constructor_withMediaType_setsMediaType() {
        Variant variant = new Variant(MediaType.TEXT_PLAIN);

        assertEquals(MediaType.TEXT_PLAIN, variant.getMediaType());
    }

    @Test
    void constructor_withMediaTypeAndLanguage_addsLanguage() {
        Variant variant = new Variant(MediaType.TEXT_PLAIN, Language.ENGLISH);

        assertTrue(variant.getLanguages().contains(Language.ENGLISH));
    }

    @Test
    void settersAndGetters_roundTrip() {
        Variant variant = new Variant();

        variant.setCharacterSet(CharacterSet.UTF_8);
        variant.setMediaType(MediaType.APPLICATION_JSON);
        variant.setLocationRef(new Reference("http://localhost/resource"));

        assertEquals(CharacterSet.UTF_8, variant.getCharacterSet());
        assertEquals(MediaType.APPLICATION_JSON, variant.getMediaType());
        assertEquals(new Reference("http://localhost/resource"), variant.getLocationRef());
    }

    @Test
    void setLocationRefFromString_parsesUri() {
        Variant variant = new Variant();

        variant.setLocationRef("http://localhost/other");

        assertEquals(new Reference("http://localhost/other"), variant.getLocationRef());
    }

    @Test
    void getEncodings_rejectsNullElement() {
        Variant variant = new Variant();
        List<Encoding> encodings = variant.getEncodings();

        assertThrows(IllegalArgumentException.class, () -> encodings.add(null));
    }

    @Test
    void getLanguages_rejectsNullElement() {
        Variant variant = new Variant();
        List<Language> languages = variant.getLanguages();

        assertThrows(IllegalArgumentException.class, () -> languages.add(null));
    }

    @Test
    void equals_isTrueForEquivalentVariants() {
        Variant v1 = new Variant(MediaType.TEXT_PLAIN);
        v1.setCharacterSet(CharacterSet.UTF_8);
        Variant v2 = new Variant(MediaType.TEXT_PLAIN);
        v2.setCharacterSet(CharacterSet.UTF_8);

        assertEquals(v1, v2);
        assertEquals(v1.hashCode(), v2.hashCode());
    }

    @Test
    void equals_isFalseForDifferentMediaType() {
        Variant v1 = new Variant(MediaType.TEXT_PLAIN);
        Variant v2 = new Variant(MediaType.APPLICATION_JSON);

        assertNotEquals(v1, v2);
    }

    @Test
    void equals_isFalseForNonVariantObject() {
        Variant variant = new Variant(MediaType.TEXT_PLAIN);

        assertNotEquals("not a variant", variant);
    }

    @Test
    void includes_isTrueWhenMediaTypeIsBroader() {
        Variant textAll = new Variant(MediaType.TEXT_ALL);
        Variant textPlain = new Variant(MediaType.TEXT_PLAIN);

        assertTrue(textAll.includes(textPlain));
        assertFalse(textPlain.includes(textAll));
    }

    @Test
    void includes_isFalseForNullOther() {
        Variant variant = new Variant(MediaType.TEXT_PLAIN);

        assertFalse(variant.includes(null));
    }

    @Test
    void isCompatible_isTrueWhenEitherIncludesTheOther() {
        Variant textAll = new Variant(MediaType.TEXT_ALL);
        Variant textPlain = new Variant(MediaType.TEXT_PLAIN);

        assertTrue(textAll.isCompatible(textPlain));
        assertTrue(textPlain.isCompatible(textAll));
    }

    @Test
    void isCompatible_isFalseForNullOther() {
        Variant variant = new Variant(MediaType.TEXT_PLAIN);

        assertFalse(variant.isCompatible(null));
    }

    @Test
    void createClientInfo_reflectsVariantPreferences() {
        Variant variant = new Variant(MediaType.APPLICATION_JSON, Language.ENGLISH);
        variant.setCharacterSet(CharacterSet.UTF_8);
        variant.getEncodings().add(Encoding.GZIP);

        ClientInfo clientInfo = variant.createClientInfo();

        assertTrue(
                clientInfo.getAcceptedMediaTypes().stream()
                        .anyMatch(
                                preference ->
                                        MediaType.APPLICATION_JSON.equals(
                                                preference.getMetadata())));
        assertTrue(
                clientInfo.getAcceptedCharacterSets().stream()
                        .anyMatch(
                                preference -> CharacterSet.UTF_8.equals(preference.getMetadata())));
        assertTrue(
                clientInfo.getAcceptedLanguages().stream()
                        .anyMatch(preference -> Language.ENGLISH.equals(preference.getMetadata())));
        assertTrue(
                clientInfo.getAcceptedEncodings().stream()
                        .anyMatch(preference -> Encoding.GZIP.equals(preference.getMetadata())));
    }

    @Test
    void toString_includesMediaTypeAndCharacterSet() {
        Variant variant = new Variant(MediaType.TEXT_PLAIN);
        variant.setCharacterSet(CharacterSet.UTF_8);

        String result = variant.toString();

        assertTrue(result.contains("text/plain"));
        assertTrue(result.contains("UTF-8"));
    }

    @Test
    void toString_isEmptyBracketsWhenNoMetadataSet() {
        Variant variant = new Variant();

        assertEquals("[]", variant.toString());
    }
}
