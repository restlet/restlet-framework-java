/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.crypto;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Base64;
import org.junit.jupiter.api.Test;
import org.restlet.data.Digest;

/** Unit tests for {@link DigestUtils}. */
class DigestUtilsTestCase {

    @Test
    void toMd5_string_matchesKnownVector() {
        assertEquals("68e109f0f40ca72a15e05cc22786f8e6", DigestUtils.toMd5("HelloWorld"));
    }

    @Test
    void toMd5_withCharset_matchesKnownVector() throws Exception {
        assertEquals("68e109f0f40ca72a15e05cc22786f8e6", DigestUtils.toMd5("HelloWorld", "UTF-8"));
    }

    @Test
    void toSha1_string_isDeterministicBase64() {
        String result = DigestUtils.toSha1("HelloWorld");
        assertEquals(result, DigestUtils.toSha1("HelloWorld"));
        assertEquals(28, result.length());
    }

    @Test
    void toSha1_withCharset_isDeterministic() throws Exception {
        assertEquals(
                DigestUtils.toSha1("HelloWorld", "UTF-8"),
                DigestUtils.toSha1("HelloWorld", "UTF-8"));
    }

    @Test
    void digest_md5Algorithm_delegatesToToMd5() {
        assertEquals(
                DigestUtils.toMd5("HelloWorld"),
                DigestUtils.digest("HelloWorld", Digest.ALGORITHM_MD5));
    }

    @Test
    void digest_sha1Algorithm_delegatesToToSha1() {
        assertEquals(
                DigestUtils.toSha1("HelloWorld"),
                DigestUtils.digest("HelloWorld", Digest.ALGORITHM_SHA_1));
    }

    @Test
    void digest_unsupportedAlgorithm_throwsIllegalArgumentException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> DigestUtils.digest("HelloWorld", "unsupported"));
    }

    @Test
    void digest_charArrayOverload_matchesStringOverload() {
        assertArrayEquals(
                DigestUtils.digest("HelloWorld", Digest.ALGORITHM_MD5).toCharArray(),
                DigestUtils.digest("HelloWorld".toCharArray(), Digest.ALGORITHM_MD5));
    }

    @Test
    void toHMacSha256_bytesKey_matchesKnownVector() {
        byte[] result = DigestUtils.toHMacSha256("hello", "secret-key".getBytes());
        assertEquals(
                "mOf/uWS7Wj+QLbH8EBpbqpi28s1WhYIQydcPJqx2L8c=",
                Base64.getEncoder().encodeToString(result));
    }

    @Test
    void toHMacSha256_stringKey_matchesBytesKeyOverload() {
        assertArrayEquals(
                DigestUtils.toHMacSha256("hello", "secret-key".getBytes()),
                DigestUtils.toHMacSha256("hello", "secret-key"));
    }

    @Test
    void toHMacSha1_bytesKey_matchesKnownVector() {
        byte[] result = DigestUtils.toHMacSha1("hello", "secret-key".getBytes());
        assertEquals("J5rb+6R5G3Md2aC4ofFuty27aR0=", Base64.getEncoder().encodeToString(result));
    }

    @Test
    void toHMacSha1_stringKey_matchesBytesKeyOverload() {
        assertArrayEquals(
                DigestUtils.toHMacSha1("hello", "secret-key".getBytes()),
                DigestUtils.toHMacSha1("hello", "secret-key"));
    }

    @Test
    void toHttpDigest_withSecret_hashesIdentifierRealmAndSecret() {
        String expected = DigestUtils.toMd5("user:realm:pass");
        assertEquals(expected, DigestUtils.toHttpDigest("user", "pass".toCharArray(), "realm"));
    }

    @Test
    void toHttpDigest_withNullSecret_returnsNull() {
        assertNull(DigestUtils.toHttpDigest("user", null, "realm"));
    }
}
