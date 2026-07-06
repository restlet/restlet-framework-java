/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link LocalVerifier}. */
class LocalVerifierTestCase {

    /** Minimal concrete fixture, since {@link LocalVerifier} is abstract. */
    private static class MapBackedLocalVerifier extends LocalVerifier {

        private final Map<String, char[]> secrets = new HashMap<>();

        void putSecret(String identifier, char[] secret) {
            secrets.put(identifier, secret);
        }

        @Override
        public char[] getLocalSecret(String identifier) {
            return secrets.get(identifier);
        }
    }

    @Test
    void verify_withMatchingSecret_returnsValid() {
        MapBackedLocalVerifier verifier = new MapBackedLocalVerifier();
        verifier.putSecret("alice", "secret".toCharArray());

        assertEquals(Verifier.RESULT_VALID, verifier.verify("alice", "secret".toCharArray()));
    }

    @Test
    void verify_withNonMatchingSecret_returnsInvalid() {
        MapBackedLocalVerifier verifier = new MapBackedLocalVerifier();
        verifier.putSecret("alice", "secret".toCharArray());

        assertEquals(Verifier.RESULT_INVALID, verifier.verify("alice", "wrong".toCharArray()));
    }

    @Test
    void verify_withUnknownIdentifier_returnsInvalid() {
        MapBackedLocalVerifier verifier = new MapBackedLocalVerifier();

        assertEquals(Verifier.RESULT_INVALID, verifier.verify("unknown", "secret".toCharArray()));
    }

    @Test
    void getLocalSecret_returnsConfiguredValue() {
        MapBackedLocalVerifier verifier = new MapBackedLocalVerifier();
        char[] secret = "secret".toCharArray();
        verifier.putSecret("alice", secret);

        assertSame(secret, verifier.getLocalSecret("alice"));
    }
}
