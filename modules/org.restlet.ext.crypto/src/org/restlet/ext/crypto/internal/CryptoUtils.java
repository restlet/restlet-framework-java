/**
 * Copyright 2005-2014 Restlet
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
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.ext.crypto.internal;

import java.nio.charset.Charset;
import java.security.GeneralSecurityException;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.restlet.engine.util.Base64;
import org.restlet.ext.crypto.DigestUtils;

/**
 * Simple usage of standard cipher features from JRE.
 * 
 * @author Remi Dewitte <remi@gide.net>
 * @author Jerome Louvel
 */
public final class CryptoUtils {

    /**
     * Creates a cipher for a given algorithm and secret.
     * 
     * @param algorithm
     *            The cryptographic algorithm.
     * @param secretKey
     *            The cryptographic secret.
     * @param mode
     *            The cipher mode, either {@link Cipher#ENCRYPT_MODE} or
     *            {@link Cipher#DECRYPT_MODE}.
     * @return The new cipher.
     * @throws GeneralSecurityException
     */
    private static Cipher createCipher(String algorithm, byte[] secretKey,
            int mode) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(mode, new SecretKeySpec(secretKey, algorithm));
        return cipher;
    }

    /**
     * Decrypts a bytes array.
     * 
     * @param algo
     *            The cryptographic algorithm.
     * @param secretKey
     *            The cryptographic secret key.
     * @param encrypted
     *            The encrypted bytes.
     * @return The decrypted content string.
     * @throws GeneralSecurityException
     */
    public static String decrypt(String algo, byte[] secretKey, byte[] encrypted)
            throws GeneralSecurityException {
        byte[] original = doFinal(algo, secretKey, Cipher.DECRYPT_MODE,
                encrypted);
        return new String(original, Charset.forName("UTF-8"));
    }

    /**
     * Decrypts a bytes array.
     * 
     * @param algo
     *            The cryptographic algorithm.
     * @param base64Secret
     *            The cryptographic secret key, encoded as a Base64 string.
     * @param encrypted
     *            The encrypted bytes.
     * @return The decrypted content string.
     * @throws GeneralSecurityException
     */
    public static String decrypt(String algo, String base64Secret,
            byte[] encrypted) throws GeneralSecurityException {
        return decrypt(algo, Base64.decode(base64Secret), encrypted);
    }

    /**
     * Does final processing.
     * 
     * @param algo
     *            The cryptographic algorithm.
     * @param secretKey
     *            The cryptographic secret key.
     * @param mode
     *            The processing mode, either {@link Cipher#DECRYPT_MODE} or
     *            {@link Cipher#ENCRYPT_MODE}.
     * @param what
     *            The byte array to process.
     * @return The processed byte array.
     * @throws GeneralSecurityException
     */
    private static byte[] doFinal(String algo, byte[] secretKey, int mode,
            byte[] what) throws GeneralSecurityException {
        return createCipher(algo, secretKey, mode).doFinal(what);
    }

    /**
     * Encrypts a content string.
     * 
     * @param algo
     *            The cryptographic algorithm.
     * @param secretKey
     *            The cryptographic secret key.
     * @param content
     *            The content string to encrypt.
     * @return The encrypted bytes.
     * @throws GeneralSecurityException
     */
    public static byte[] encrypt(String algo, byte[] secretKey, String content)
            throws GeneralSecurityException {
        return doFinal(algo, secretKey, Cipher.ENCRYPT_MODE, content.getBytes(Charset.forName("UTF-8")));
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
        return encrypt(algo, Base64.decode(base64Secret), content);
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
        return Base64.encode(
                (currentTimeMS + ":" + DigestUtils.toMd5(currentTimeMS + ":"
                        + secretKey)).getBytes(), true);
    }

    /**
     * Private constructor to ensure that the class acts as a true utility class
     * i.e. it isn't instantiable and extensible.
     */
    private CryptoUtils() {
    }
}
