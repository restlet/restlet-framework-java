/**
 * Copyright 2005-2011 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.ext.crypto.internal;

import java.security.GeneralSecurityException;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.restlet.engine.util.Base64;
import org.restlet.ext.crypto.DigestUtils;

/**
 * Simple usage of standard cipher features from JRE.
 * 
 * @author Remi Dewitte <remi@gide.net>
 */
public final class CryptoUtils {

    /**
     * Creates a cipher for a given algorithm and secret.
     * 
     * @param algo
     *            The cryptographic algorithm.
     * @param base64Secret
     *            The cryptographic secret, encoded as a Base64 string.
     * @param mode
     *            The cipher mode, either {@link Cipher#ENCRYPT_MODE} or
     *            {@link Cipher#DECRYPT_MODE}.
     * @return The new cipher.
     * @throws GeneralSecurityException
     */
    private static Cipher createCipher(String algo, String base64Secret,
            int mode) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance(algo);
        cipher.init(mode, new SecretKeySpec(Base64.decode(base64Secret), algo));
        return cipher;
    }

    /**
     * Decrypts a bytes array.
     * 
     * @param algo
     *            The cryptographic algorithm.
     * @param base64Secret
     *            The cryptographic secret, encoded as a Base64 string.
     * @param encrypted
     *            The encrypted bytes.
     * @return The decrypted content string.
     * @throws GeneralSecurityException
     */
    public static String decrypt(String algo, String base64Secret,
            byte[] encrypted) throws GeneralSecurityException {
        byte[] original = doFinal(algo, base64Secret, Cipher.DECRYPT_MODE,
                encrypted);
        return new String(original);
    }

    /**
     * Does final processing.
     * 
     * @param algo
     *            The cryptographic algorithm.
     * @param base64Secret
     *            The cryptographic secret, encoded as a Base64 string.
     * @param mode
     *            The processing mode, either {@link Cipher#DECRYPT_MODE} or
     *            {@link Cipher#ENCRYPT_MODE}.
     * @param what
     *            The byte array to process.
     * @return The processed byte array.
     * @throws GeneralSecurityException
     */
    private static byte[] doFinal(String algo, String base64Secret, int mode,
            byte[] what) throws GeneralSecurityException {
        return createCipher(algo, base64Secret, mode).doFinal(what);
    }

    /**
     * Encrypts a content string.
     * 
     * @param algo
     *            The cryptographic algorithm.
     * @param base64Secret
     *            The cryptographic secret, encoded as a Base64 string.
     * @param content
     *            The content string to encrypt.
     * @return The encrypted bytes.
     * @throws GeneralSecurityException
     */
    public static byte[] encrypt(String algo, String base64Secret,
            String content) throws GeneralSecurityException {
        return doFinal(algo, base64Secret, Cipher.ENCRYPT_MODE, content
                .getBytes());
    }

    /**
     * Generates a nonce as recommended in section 3.2.1 of RFC-2617, but
     * without the ETag field. The format is: <code><pre>
     * Base64.encodeBytes(currentTimeMS + &quot;:&quot;
     *         + md5String(currentTimeMS + &quot;:&quot; + secretKey))
     * </pre></code>
     * 
     * @param secretKey
     *            a secret value known only to the creator of the nonce. It's
     *            inserted into the nonce, and can be used later to validate the
     *            nonce.
     */
    public static String makeNonce(String secretKey) {
        final long currentTimeMS = System.currentTimeMillis();
        return Base64.encode((currentTimeMS + ":" + DigestUtils
                .toMd5(currentTimeMS + ":" + secretKey)).getBytes(), true);
    }

    /**
     * Private constructor to ensure that the class acts as a true utility class
     * i.e. it isn't instantiable and extensible.
     */
    private CryptoUtils() {
    }
}
