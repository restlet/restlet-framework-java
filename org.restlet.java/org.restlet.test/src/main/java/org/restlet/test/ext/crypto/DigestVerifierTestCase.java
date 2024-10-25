/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * https://restlet.talend.com/
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.test.ext.crypto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.restlet.data.Digest;
import org.restlet.ext.crypto.DigestVerifier;
import org.restlet.security.MapVerifier;
import org.restlet.security.Verifier;
import org.restlet.test.RestletTestCase;

/**
 * Restlet unit tests for the DigestVerifierTestCase class.
 * 
 * @author Jerome Louvel
 */
public class DigestVerifierTestCase extends RestletTestCase {

    @Test
    public void test1() {
        MapVerifier mv = new MapVerifier();
        mv.getLocalSecrets().put("scott", "tiger".toCharArray());

        DigestVerifier<MapVerifier> sdv = new DigestVerifier<>(
                Digest.ALGORITHM_SHA_1, mv, null);

        assertEquals(
                Verifier.RESULT_VALID,
                sdv.verify("scott",
                        "RuPXcqGIjq3/JsetpH/XUC15bgc=".toCharArray()));
    }

    @Test
    public void test2() {
        MapVerifier mv = new MapVerifier();
        mv.getLocalSecrets().put("scott",
                "RuPXcqGIjq3/JsetpH/XUC15bgc=".toCharArray());

        DigestVerifier<MapVerifier> sdv = new DigestVerifier<>(
                Digest.ALGORITHM_SHA_1, mv, Digest.ALGORITHM_SHA_1);

        assertEquals(
                Verifier.RESULT_VALID,
                sdv.verify("scott",
                        "RuPXcqGIjq3/JsetpH/XUC15bgc=".toCharArray()));

        assertEquals(Verifier.RESULT_INVALID,
                sdv.verify("scott", "xxxxx".toCharArray()));

        assertEquals(Verifier.RESULT_INVALID,
                sdv.verify("tom", "RuPXcqGIjq3/JsetpH/XUC15bgc=".toCharArray()));
    }

    @Test
    public void test3() {
        MapVerifier mv = new MapVerifier();
        mv.getLocalSecrets().put("scott",
                "RuPXcqGIjq3/JsetpH/XUC15bgc=".toCharArray());

        DigestVerifier<MapVerifier> sdv = new DigestVerifier<>(null,
                mv, Digest.ALGORITHM_SHA_1);

        assertEquals(Verifier.RESULT_VALID,
                sdv.verify("scott", "tiger".toCharArray()));
    }
}
