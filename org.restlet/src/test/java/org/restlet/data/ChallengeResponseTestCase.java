/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.data;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

/** Test {@link org.restlet.data.ChallengeResponse}. */
class ChallengeResponseTestCase {

    @Test
    void constructor_withSchemeOnly_hasNoCredentials() {
        ChallengeResponse response = new ChallengeResponse(ChallengeScheme.HTTP_BASIC);
        assertEquals(ChallengeScheme.HTTP_BASIC, response.getScheme());
        assertNull(response.getIdentifier());
        assertNull(response.getSecret());
    }

    @Test
    void constructor_withIdentifierAndCharArraySecret() {
        ChallengeResponse response =
                new ChallengeResponse(ChallengeScheme.HTTP_BASIC, "user", "pass".toCharArray());
        assertEquals("user", response.getIdentifier());
        assertArrayEquals("pass".toCharArray(), response.getSecret());
    }

    @Test
    void constructor_withIdentifierAndStringSecret() {
        ChallengeResponse response =
                new ChallengeResponse(ChallengeScheme.HTTP_BASIC, "user", "pass");
        assertEquals("user", response.getIdentifier());
        assertArrayEquals("pass".toCharArray(), response.getSecret());
    }

    @Test
    void constructor_withNullStringSecret_hasNullSecret() {
        ChallengeResponse response =
                new ChallengeResponse(ChallengeScheme.HTTP_BASIC, "user", (String) null);
        assertNull(response.getSecret());
    }

    @Test
    void getPrincipal_returnsIdentifierBackedPrincipal() {
        ChallengeResponse response =
                new ChallengeResponse(ChallengeScheme.HTTP_BASIC, "user", "pass");
        assertEquals("user", response.getPrincipal().getName());
    }

    @Test
    void settersUpdateState() {
        ChallengeResponse response = new ChallengeResponse(ChallengeScheme.HTTP_BASIC);
        Reference digestRef = new Reference("http://example.com/resource");
        response.setClientNonce("clientNonce");
        response.setDigestRef(digestRef);
        response.setIdentifier("user");
        response.setQuality("auth");
        response.setSecret("secret");
        response.setSecretAlgorithm(Digest.ALGORITHM_MD5);
        response.setServerNonceCount(5);
        response.setTimeIssued(12345L);

        assertEquals("clientNonce", response.getClientNonce());
        assertEquals(digestRef, response.getDigestRef());
        assertEquals("user", response.getIdentifier());
        assertEquals("auth", response.getQuality());
        assertArrayEquals("secret".toCharArray(), response.getSecret());
        assertEquals(Digest.ALGORITHM_MD5, response.getSecretAlgorithm());
        assertEquals(5, response.getServerNonceCount());
        assertEquals(12345L, response.getTimeIssued());
    }

    @Test
    void getServerNonceCountAsHex_formatsAsEightHexCharacters() {
        ChallengeResponse response = new ChallengeResponse(ChallengeScheme.HTTP_BASIC);
        response.setServerNonceCount(255);
        assertEquals("000000ff", response.getServerNonceCountAsHex());
    }

    @Test
    void equals_sameRawValueIdentifierSchemeAndSecret_returnsTrue() {
        ChallengeResponse r1 = new ChallengeResponse(ChallengeScheme.HTTP_BASIC, "user", "pass");
        ChallengeResponse r2 = new ChallengeResponse(ChallengeScheme.HTTP_BASIC, "user", "pass");
        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
    }

    @Test
    void equals_sameInstance_returnsTrue() {
        ChallengeResponse response =
                new ChallengeResponse(ChallengeScheme.HTTP_BASIC, "user", "pass");
        assertEquals(response, response);
    }

    @Test
    void equals_differentType_returnsFalse() {
        ChallengeResponse response =
                new ChallengeResponse(ChallengeScheme.HTTP_BASIC, "user", "pass");
        assertNotEquals(response, "not a challenge response");
    }

    @Test
    void equals_differentIdentifier_returnsFalse() {
        ChallengeResponse r1 = new ChallengeResponse(ChallengeScheme.HTTP_BASIC, "user1", "pass");
        ChallengeResponse r2 = new ChallengeResponse(ChallengeScheme.HTTP_BASIC, "user2", "pass");
        assertNotEquals(r1, r2);
    }

    @Test
    void equals_differentSecret_returnsFalse() {
        ChallengeResponse r1 = new ChallengeResponse(ChallengeScheme.HTTP_BASIC, "user", "pass1");
        ChallengeResponse r2 = new ChallengeResponse(ChallengeScheme.HTTP_BASIC, "user", "pass2");
        assertNotEquals(r1, r2);
    }
}
