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

import org.junit.jupiter.api.Test;
import org.restlet.data.Digest;
import org.restlet.security.MapVerifier;
import org.restlet.security.Verifier;

/**
 * Restlet unit tests for the DigestVerifierTestCase class.
 *
 * @author Jerome Louvel
 */
class DigestVerifierTestCase {

    @Test
    void test1() {
        MapVerifier mv = new MapVerifier();
        mv.getLocalSecrets().put("scott", "tiger".toCharArray());

        DigestVerifier<MapVerifier> sdv = new DigestVerifier<>(Digest.ALGORITHM_SHA_1, mv, null);

        assertEquals(
                Verifier.RESULT_VALID,
                sdv.verify("scott", "RuPXcqGIjq3/JsetpH/XUC15bgc=".toCharArray()));
    }

    @Test
    void test2() {
        MapVerifier mv = new MapVerifier();
        mv.getLocalSecrets().put("scott", "RuPXcqGIjq3/JsetpH/XUC15bgc=".toCharArray());

        DigestVerifier<MapVerifier> sdv =
                new DigestVerifier<>(Digest.ALGORITHM_SHA_1, mv, Digest.ALGORITHM_SHA_1);

        assertEquals(
                Verifier.RESULT_VALID,
                sdv.verify("scott", "RuPXcqGIjq3/JsetpH/XUC15bgc=".toCharArray()));

        assertEquals(Verifier.RESULT_INVALID, sdv.verify("scott", "xxxxx".toCharArray()));

        assertEquals(
                Verifier.RESULT_INVALID,
                sdv.verify("tom", "RuPXcqGIjq3/JsetpH/XUC15bgc=".toCharArray()));
    }

    @Test
    void test3() {
        MapVerifier mv = new MapVerifier();
        mv.getLocalSecrets().put("scott", "RuPXcqGIjq3/JsetpH/XUC15bgc=".toCharArray());

        DigestVerifier<MapVerifier> sdv = new DigestVerifier<>(null, mv, Digest.ALGORITHM_SHA_1);

        assertEquals(Verifier.RESULT_VALID, sdv.verify("scott", "tiger".toCharArray()));
    }
}
