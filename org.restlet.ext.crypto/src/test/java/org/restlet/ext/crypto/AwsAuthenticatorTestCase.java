/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.crypto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.restlet.Context;
import org.restlet.data.ChallengeScheme;
import org.restlet.ext.crypto.internal.AwsVerifier;
import org.restlet.security.MapVerifier;
import org.restlet.security.Verifier;

/** Unit tests for {@link AwsAuthenticator}. */
class AwsAuthenticatorTestCase {

    @Test
    void constructor_withContextAndRealm_isNotOptional() {
        AwsAuthenticator authenticator = new AwsAuthenticator(new Context(), "realm");
        assertFalse(authenticator.isOptional());
        assertEquals(ChallengeScheme.HTTP_AWS_S3, authenticator.getScheme());
        assertNotNull(authenticator.getVerifier());
    }

    @Test
    void constructor_withOptionalFlag_setsOptional() {
        AwsAuthenticator authenticator = new AwsAuthenticator(new Context(), true, "realm");
        assertTrue(authenticator.isOptional());
    }

    @Test
    void constructor_withExplicitVerifier_usesIt() {
        AwsVerifier verifier = new AwsVerifier(null);
        AwsAuthenticator authenticator =
                new AwsAuthenticator(new Context(), false, "realm", verifier);
        assertSame(verifier, authenticator.getVerifier());
    }

    @Test
    void maxRequestAge_getterAndSetter_delegateToVerifier() {
        AwsAuthenticator authenticator = new AwsAuthenticator(new Context(), "realm");
        authenticator.setMaxRequestAge(1000L);
        assertEquals(1000L, authenticator.getMaxRequestAge());
    }

    @Test
    void wrappedVerifier_getterAndSetter_delegateToVerifier() {
        AwsAuthenticator authenticator = new AwsAuthenticator(new Context(), "realm");
        MapVerifier wrapped = new MapVerifier();
        authenticator.setWrappedVerifier(wrapped);
        assertSame(wrapped, authenticator.getWrappedVerifier());
    }

    @Test
    void setVerifier_withNonAwsVerifier_throwsIllegalArgumentException() {
        AwsAuthenticator authenticator = new AwsAuthenticator(new Context(), "realm");
        Verifier notAnAwsVerifier = new MapVerifier();
        assertThrows(
                IllegalArgumentException.class, () -> authenticator.setVerifier(notAnAwsVerifier));
    }

    @Test
    void setVerifier_withAwsVerifier_replacesIt() {
        AwsAuthenticator authenticator = new AwsAuthenticator(new Context(), "realm");
        AwsVerifier newVerifier = new AwsVerifier(null);
        authenticator.setVerifier(newVerifier);
        assertSame(newVerifier, authenticator.getVerifier());
    }
}
