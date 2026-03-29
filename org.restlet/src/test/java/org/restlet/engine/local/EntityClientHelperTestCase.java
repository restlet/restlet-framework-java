/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.local;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Unit tests for {@link EntityClientHelper#getReencodedVariantEntityName}.
 *
 * @author Thierry Boileau
 */
class EntityClientHelperTestCase {

    /** Minimal concrete subclass to expose the protected method under test. */
    private static class TestableEntityClientHelper extends EntityClientHelper {
        TestableEntityClientHelper() {
            super(null);
        }

        @Override
        public Entity getEntity(String path) {
            return null;
        }
    }

    private TestableEntityClientHelper helper;

    @BeforeEach
    void setUp() {
        helper = new TestableEntityClientHelper();
    }

    /**
     * When encoded and decoded names are identical plain strings, the encoded name is returned
     * as-is.
     */
    @Test
    void testExactPlainMatch() {
        assertEquals("file.txt", helper.getReencodedVariantEntityName("file.txt", "file.txt"));
    }

    /**
     * When the encoded name contains a percent-encoded character that matches the decoded variant,
     * the encoding is preserved.
     */
    @ParameterizedTest
    @CsvSource({"file%20name.txt", "file+name.txt"})
    void testEncodedNameSameDecodedVariant(final String encodedName) {
        assertEquals(
                encodedName, helper.getReencodedVariantEntityName(encodedName, "file name.txt"));
    }

    /**
     * When the variant shares the base name but has a different extension, the extension is taken
     * from the decoded variant.
     */
    @Test
    void testDifferentExtension() {
        assertEquals("file.json", helper.getReencodedVariantEntityName("file.txt", "file.json"));
    }

    /** Combination: percent-encoded base name with a variant using a different extension. */
    @Test
    void testEncodedNameDifferentExtension() {
        assertEquals(
                "file%20name.json",
                helper.getReencodedVariantEntityName("file%20name.txt", "file name.json"));
    }

    /** When there is no common prefix at all, the full decoded variant name is returned. */
    @Test
    void testNoCommonPrefix() {
        assertEquals("xyz.txt", helper.getReencodedVariantEntityName("abc.txt", "xyz.txt"));
    }

    /**
     * When the decoded variant name is a prefix of the full encoded name, only the matched encoded
     * prefix is returned.
     */
    @Test
    void testDecodedVariantIsPrefixOfEncoded() {
        assertEquals("filename", helper.getReencodedVariantEntityName("filename.txt", "filename"));
    }
}
