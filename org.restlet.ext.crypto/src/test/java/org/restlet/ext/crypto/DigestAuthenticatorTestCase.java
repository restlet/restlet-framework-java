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

import java.util.Base64;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.restlet.Context;
import org.restlet.data.ChallengeRequest;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Reference;
import org.restlet.security.MapVerifier;
import org.restlet.security.Verifier;

/** Unit tests for {@link DigestAuthenticator}. */
class DigestAuthenticatorTestCase {

    @Test
    void constructor_withRealmAndServerKey_setsDefaults() {
        DigestAuthenticator authenticator =
                new DigestAuthenticator(new Context(), "realm", "mySecretKey");

        assertFalse(authenticator.isOptional());
        assertEquals(ChallengeScheme.HTTP_DIGEST, authenticator.getScheme());
        assertEquals("realm", authenticator.getRealm());
        assertEquals("mySecretKey", authenticator.getServerKey());
        assertEquals(5 * 60 * 1000L, authenticator.getMaxServerNonceAge());
        assertNotNull(authenticator.getVerifier());
    }

    @Test
    void constructor_withExplicitDomainRefs_usesProvidedList() {
        List<Reference> domainRefs = Collections.singletonList(new Reference("/protected/"));
        DigestAuthenticator authenticator =
                new DigestAuthenticator(new Context(), true, "realm", domainRefs, "key");

        assertTrue(authenticator.isOptional());
        assertSame(domainRefs, authenticator.getDomainRefs());
    }

    @Test
    void getDomainRefs_withNoDomainRefsProvided_lazilyDefaultsToRoot() {
        DigestAuthenticator authenticator = new DigestAuthenticator(new Context(), "realm", "key");

        List<Reference> domainRefs = authenticator.getDomainRefs();

        assertEquals(1, domainRefs.size());
        assertEquals("/", domainRefs.getFirst().toString());
        // Second call returns the same cached list instance.
        assertSame(domainRefs, authenticator.getDomainRefs());
    }

    @Test
    void setDomainRefs_updatesGetDomainRefs() {
        DigestAuthenticator authenticator = new DigestAuthenticator(new Context(), "realm", "key");
        List<Reference> domainRefs = Collections.singletonList(new Reference("/other/"));

        authenticator.setDomainRefs(domainRefs);

        assertSame(domainRefs, authenticator.getDomainRefs());
    }

    @Test
    void generateServerNonce_returnsDecodableBase64Value() {
        DigestAuthenticator authenticator = new DigestAuthenticator(new Context(), "realm", "key");

        String nonce = authenticator.generateServerNonce();

        assertNotNull(nonce);
        String decoded = new String(Base64.getDecoder().decode(nonce));
        assertTrue(decoded.contains(":"));
    }

    @Test
    void createChallengeRequest_stale_populatesDomainRefsStaleAndNonce() {
        DigestAuthenticator authenticator = new DigestAuthenticator(new Context(), "realm", "key");

        ChallengeRequest challengeRequest = authenticator.createChallengeRequest(true);

        assertEquals(ChallengeScheme.HTTP_DIGEST, challengeRequest.getScheme());
        assertEquals("realm", challengeRequest.getRealm());
        assertTrue(challengeRequest.isStale());
        assertNotNull(challengeRequest.getServerNonce());
        assertEquals(authenticator.getDomainRefs(), challengeRequest.getDomainRefs());
    }

    @Test
    void createChallengeRequest_notStale_setsStaleFalse() {
        DigestAuthenticator authenticator = new DigestAuthenticator(new Context(), "realm", "key");

        ChallengeRequest challengeRequest = authenticator.createChallengeRequest(false);

        assertFalse(challengeRequest.isStale());
    }

    @Test
    void getHashedSecret_httpDigestScheme_hashesIdentifierRealmAndSecret() {
        DigestAuthenticator authenticator = new DigestAuthenticator(new Context(), "realm", "key");

        String expected = DigestUtils.toHttpDigest("scott", "tiger".toCharArray(), "realm");

        assertEquals(expected, authenticator.getHashedSecret("scott", "tiger".toCharArray()));
    }

    @Test
    void maxServerNonceAge_getterAndSetter() {
        DigestAuthenticator authenticator = new DigestAuthenticator(new Context(), "realm", "key");

        authenticator.setMaxServerNonceAge(12345L);

        assertEquals(12345L, authenticator.getMaxServerNonceAge());
    }

    @Test
    void serverKey_getterAndSetter() {
        DigestAuthenticator authenticator = new DigestAuthenticator(new Context(), "realm", "key");

        authenticator.setServerKey("anotherKey");

        assertEquals("anotherKey", authenticator.getServerKey());
    }

    @Test
    void getVerifier_returnsDigestVerifierSetByConstructor() {
        DigestAuthenticator authenticator = new DigestAuthenticator(new Context(), "realm", "key");

        assertNotNull(authenticator.getVerifier());
    }

    @Test
    void setVerifier_withNonDigestVerifier_throwsIllegalArgumentException() {
        DigestAuthenticator authenticator = new DigestAuthenticator(new Context(), "realm", "key");
        Verifier notADigestVerifier = new MapVerifier();

        assertThrows(
                IllegalArgumentException.class,
                () -> authenticator.setVerifier(notADigestVerifier));
    }

    @Test
    void setVerifier_withDigestVerifier_replacesIt() {
        DigestAuthenticator authenticator = new DigestAuthenticator(new Context(), "realm", "key");
        DigestVerifier<MapVerifier> newVerifier =
                new DigestVerifier<>(null, new MapVerifier(), null);

        authenticator.setVerifier(newVerifier);

        assertSame(newVerifier, authenticator.getVerifier());
    }

    @Test
    void setWrappedAlgorithm_delegatesToVerifier() {
        DigestAuthenticator authenticator = new DigestAuthenticator(new Context(), "realm", "key");

        authenticator.setWrappedAlgorithm("SHA-1");

        assertEquals("SHA-1", authenticator.getVerifier().getWrappedAlgorithm());
    }

    @Test
    void setWrappedVerifier_delegatesToVerifier() {
        DigestAuthenticator authenticator = new DigestAuthenticator(new Context(), "realm", "key");
        MapVerifier wrapped = new MapVerifier();

        authenticator.setWrappedVerifier(wrapped);

        assertSame(wrapped, authenticator.getVerifier().getWrappedVerifier());
    }
}
