/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/** Test {@link org.restlet.data.ChallengeScheme}. */
class ChallengeSchemeTestCase {

    @Test
    void valueOf_knownName_returnsSharedConstant() {
        assertSame(ChallengeScheme.HTTP_BASIC, ChallengeScheme.valueOf("HTTP_BASIC"));
    }

    @Test
    void valueOf_isCaseInsensitive() {
        assertSame(ChallengeScheme.HTTP_BASIC, ChallengeScheme.valueOf("http_basic"));
    }

    @Test
    void valueOf_unknownName_createsNewInstance() {
        ChallengeScheme scheme = ChallengeScheme.valueOf("CUSTOM_SCHEME");
        assertEquals("CUSTOM_SCHEME", scheme.getName());
        assertNull(scheme.getTechnicalName());
        assertNull(scheme.getDescription());
    }

    @Test
    void valueOf_nullName_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> ChallengeScheme.valueOf(null));
    }

    @Test
    void constructor_withNameAndTechnicalName() {
        ChallengeScheme scheme = new ChallengeScheme("SCHEME", "TECH");
        assertEquals("SCHEME", scheme.getName());
        assertEquals("TECH", scheme.getTechnicalName());
        assertNull(scheme.getDescription());
    }

    @Test
    void constructor_withDescription() {
        ChallengeScheme scheme = new ChallengeScheme("SCHEME", "TECH", "A description");
        assertEquals("A description", scheme.getDescription());
    }

    @Test
    void equals_isCaseInsensitiveOnName() {
        ChallengeScheme scheme1 = new ChallengeScheme("scheme", "tech");
        ChallengeScheme scheme2 = new ChallengeScheme("SCHEME", "other");
        assertEquals(scheme1, scheme2);
        assertEquals(scheme1.hashCode(), scheme2.hashCode());
    }

    @Test
    void equals_differentName_returnsFalse() {
        assertNotEquals(ChallengeScheme.HTTP_BASIC, ChallengeScheme.HTTP_DIGEST);
    }

    @Test
    void toString_returnsName() {
        assertEquals(ChallengeScheme.HTTP_BASIC.getName(), ChallengeScheme.HTTP_BASIC.toString());
    }

    @Test
    void hashCode_isCaseInsensitive() {
        ChallengeScheme scheme1 = new ChallengeScheme("scheme", "tech");
        ChallengeScheme scheme2 = new ChallengeScheme("SCHEME", "tech");
        assertEquals(scheme1.hashCode(), scheme2.hashCode());
    }
}
