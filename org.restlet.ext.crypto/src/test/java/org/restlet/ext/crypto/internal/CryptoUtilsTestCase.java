/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.crypto.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Base64;
import org.junit.jupiter.api.Test;
import org.restlet.ext.crypto.DigestUtils;

/** Unit tests for {@link CryptoUtils}. */
class CryptoUtilsTestCase {

    private static final String ALGORITHM = "AES";

    private static final byte[] SECRET_KEY_BYTES =
            "0123456789ABCDEF".getBytes(StandardCharsets.UTF_8);

    @Test
    void encryptThenDecrypt_bytesKey_roundTripsContent() throws GeneralSecurityException {
        byte[] encrypted = CryptoUtils.encrypt(ALGORITHM, SECRET_KEY_BYTES, "hello, world");

        String decrypted = CryptoUtils.decrypt(ALGORITHM, SECRET_KEY_BYTES, encrypted);

        assertEquals("hello, world", decrypted);
    }

    @Test
    void encryptThenDecrypt_base64Key_roundTripsContent() throws GeneralSecurityException {
        String base64Secret = Base64.getEncoder().encodeToString(SECRET_KEY_BYTES);

        byte[] encrypted = CryptoUtils.encrypt(ALGORITHM, base64Secret, "another secret message");
        String decrypted = CryptoUtils.decrypt(ALGORITHM, base64Secret, encrypted);

        assertEquals("another secret message", decrypted);
    }

    @Test
    void encrypt_unsupportedAlgorithm_throwsGeneralSecurityException() {
        assertThrows(
                GeneralSecurityException.class,
                () -> CryptoUtils.encrypt("NoSuchAlgorithm", SECRET_KEY_BYTES, "content"));
    }

    @Test
    void decrypt_unsupportedAlgorithm_throwsGeneralSecurityException() {
        assertThrows(
                GeneralSecurityException.class,
                () ->
                        CryptoUtils.decrypt(
                                "NoSuchAlgorithm",
                                SECRET_KEY_BYTES,
                                "content".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void makeNonce_returnsBase64EncodedTimestampAndDigest() {
        String nonce = CryptoUtils.makeNonce("mySecretKey");

        assertNotNull(nonce);
        String decoded = new String(Base64.getDecoder().decode(nonce), StandardCharsets.UTF_8);
        assertTrue(decoded.contains(":"));

        String timestampPart = decoded.substring(0, decoded.indexOf(':'));
        String digestPart = decoded.substring(decoded.indexOf(':') + 1);
        assertEquals(DigestUtils.toMd5(timestampPart + ":" + "mySecretKey"), digestPart);
    }
}
