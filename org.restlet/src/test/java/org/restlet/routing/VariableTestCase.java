/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.routing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Unit tests for {@link Variable}. */
class VariableTestCase {

    @Test
    void defaultConstructor_hasExpectedDefaults() {
        Variable variable = new Variable();

        assertEquals(Variable.TYPE_ALL, variable.getType());
        assertEquals("", variable.getDefaultValue());
        assertTrue(variable.isRequired());
        assertFalse(variable.isFixed());
        assertFalse(variable.isDecodingOnParse());
        assertFalse(variable.isEncodingOnFormat());
    }

    @Test
    void typeConstructor_setsTypeAndDefaults() {
        Variable variable = new Variable(Variable.TYPE_ALPHA);

        assertEquals(Variable.TYPE_ALPHA, variable.getType());
        assertEquals("", variable.getDefaultValue());
        assertTrue(variable.isRequired());
        assertFalse(variable.isFixed());
    }

    @Test
    void fourArgConstructor_setsAllFields() {
        Variable variable = new Variable(Variable.TYPE_DIGIT, "42", false, true);

        assertEquals(Variable.TYPE_DIGIT, variable.getType());
        assertEquals("42", variable.getDefaultValue());
        assertFalse(variable.isRequired());
        assertTrue(variable.isFixed());
    }

    @Test
    void sixArgConstructor_setsEncodingFlags() {
        Variable variable = new Variable(Variable.TYPE_URI_ALL, "x", true, false, true, true);

        assertTrue(variable.isDecodingOnParse());
        assertTrue(variable.isEncodingOnFormat());
    }

    @Test
    void gettersAndSetters_roundTrip() {
        Variable variable = new Variable();

        variable.setType(Variable.TYPE_WORD);
        assertEquals(Variable.TYPE_WORD, variable.getType());

        variable.setDefaultValue("abc");
        assertEquals("abc", variable.getDefaultValue());

        variable.setRequired(false);
        assertFalse(variable.isRequired());

        variable.setFixed(true);
        assertTrue(variable.isFixed());

        variable.setDecodingOnParse(true);
        assertTrue(variable.isDecodingOnParse());

        variable.setEncodingOnFormat(true);
        assertTrue(variable.isEncodingOnFormat());
    }

    @Test
    void encode_withUriPathType_encodesReservedCharacters() {
        Variable variable = new Variable(Variable.TYPE_URI_PATH);

        String encoded = variable.encode("a b/c");

        assertEquals(org.restlet.data.Reference.encode("a b/c"), encoded);
    }

    @Test
    void encode_withNonUriType_returnsValueUnchanged() {
        Variable variable = new Variable(Variable.TYPE_ALPHA);

        String encoded = variable.encode("a b/c");

        assertEquals("a b/c", encoded);
    }

    @Test
    void encode_withUriAllType_encodesValue() {
        Variable variable = new Variable(Variable.TYPE_URI_ALL);

        assertEquals(
                org.restlet.data.Reference.encode("hello world"), variable.encode("hello world"));
    }
}
