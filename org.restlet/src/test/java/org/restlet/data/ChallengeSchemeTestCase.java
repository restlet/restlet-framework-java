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

    /** Issue #1618: the Bearer and MAC schemes used to be mapped to HTTP_OAUTH. */
    @Test
    void valueOf_oauthBearerAndMac_returnTheirOwnConstants() {
        assertSame(ChallengeScheme.HTTP_OAUTH_BEARER, ChallengeScheme.valueOf("HTTP_Bearer"));
        assertSame(ChallengeScheme.HTTP_OAUTH_BEARER, ChallengeScheme.valueOf("http_bearer"));
        assertSame(ChallengeScheme.HTTP_OAUTH_MAC, ChallengeScheme.valueOf("HTTP_MAC"));
        assertSame(ChallengeScheme.HTTP_OAUTH, ChallengeScheme.valueOf("HTTP_OAuth"));
    }

    /**
     * Every constant must be found again by its name, so that a missing or mistaken entry in the
     * lookup table is detected. HTTP_AWS_IAM and HTTP_AWS_QUERY are deliberately resolved to
     * HTTP_AWS_S3.
     */
    @Test
    void valueOf_everyConstant_isFoundByItsName() throws IllegalAccessException {
        int checked = 0;
        for (java.lang.reflect.Field field : ChallengeScheme.class.getFields()) {
            if (field.getType() != ChallengeScheme.class
                    || !java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            ChallengeScheme constant = (ChallengeScheme) field.get(null);
            if (constant == ChallengeScheme.HTTP_AWS_IAM
                    || constant == ChallengeScheme.HTTP_AWS_QUERY) {
                continue;
            }
            assertSame(constant, ChallengeScheme.valueOf(constant.getName()), field.getName());
            checked++;
        }
        assertEquals(12, checked);
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
