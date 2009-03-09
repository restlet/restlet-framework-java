/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
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

package org.restlet.engine.util;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Security data manipulation utilities.
 * 
 * @author Jerome Louvel
 */
public class DigestUtils {
    /**
     * General regex pattern to extract comma separated name-value components.
     * This pattern captures one name and value per match(), and is repeatedly
     * applied to the input string to extract all components. Must handle both
     * quoted and unquoted values as RFC2617 isn't consistent in this respect.
     * Pattern is immutable and thread-safe so reuse one static instance.
     */
    private static final char[] HEXDIGITS = "0123456789abcdef".toCharArray();

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
        return Base64.encode((currentTimeMS + ":" + toMd5(currentTimeMS + ":"
                + secretKey)).getBytes(), true);
    }

    /**
     * Converts a source string to its HMAC/SHA-1 value.
     * 
     * @param source
     *            The source string to convert.
     * @param secretKey
     *            The secret key to use for conversion.
     * @return The HMac value of the source string.
     */
    public static byte[] toHMac(String source, byte[] secretKey) {
        byte[] result = null;

        try {
            // Create the HMAC/SHA1 key
            final SecretKeySpec signingKey = new SecretKeySpec(secretKey,
                    "HmacSHA1");

            // Create the message authentication code (MAC)
            final Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(signingKey);

            // Compute the HMAC value
            result = mac.doFinal(source.getBytes());
        } catch (NoSuchAlgorithmException nsae) {
            throw new RuntimeException(
                    "Could not find the SHA-1 algorithm. HMac conversion failed.",
                    nsae);
        } catch (InvalidKeyException ike) {
            throw new RuntimeException(
                    "Invalid key exception detected. HMac conversion failed.",
                    ike);
        }

        return result;
    };

    /**
     * Converts a source string to its HMAC/SHA-1 value.
     * 
     * @param source
     *            The source string to convert.
     * @param secretKey
     *            The secret key to use for conversion.
     * @return The HMac value of the source string.
     */
    public static byte[] toHMac(String source, String secretKey) {
        return toHMac(source, secretKey.getBytes());
    }

    /**
     * Converts a source string to its HMAC/SHA256 value.
     * 
     * @param source
     *            The source string to convert.
     * @param secretKey
     *            The secret key to use for conversion.
     * @return The HMac value of the source string.
     */
    public static byte[] toHMac256(String source, byte[] secretKey) {
        byte[] result = null;

        try {
            // Create the HMAC/SHA256 key
            final SecretKeySpec signingKey = new SecretKeySpec(secretKey,
                    "HmacSHA256");

            // Create the message authentication code (MAC)
            final Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(signingKey);

            // Compute the HMAC value
            result = mac.doFinal(source.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException nsae) {
            throw new RuntimeException(
                    "Could not find the SHA256 algorithm. HMac conversion failed.",
                    nsae);
        } catch (InvalidKeyException ike) {
            throw new RuntimeException(
                    "Invalid key exception detected. HMac conversion failed.",
                    ike);
        } catch (IllegalStateException ise) {
            throw new RuntimeException(
                    "IIllegal state exception detected. HMac conversion failed.",
                    ise);
        } catch (UnsupportedEncodingException uee) {
            throw new RuntimeException(
                    "Unsuported encoding UTF-8. HMac conversion failed.", uee);
        }

        return result;
    }

    /**
     * Converts a source string to its HMAC/SHA256 value.
     * 
     * @param source
     *            The source string to convert.
     * @param secretKey
     *            The secret key to use for conversion.
     * @return The HMac value of the source string.
     */
    public static byte[] toHMac256(String source, String secretKey) {
        return toHMac256(source, secretKey.getBytes());
    }

    /**
     * Returns the MD5 digest of the target string. Target is decoded to bytes
     * using the US-ASCII charset. The returned hexadecimal String always
     * contains 32 lowercase alphanumeric characters. For example, if target is
     * "HelloWorld", this method returns "68e109f0f40ca72a15e05cc22786f8e6".
     * 
     * @param target
     *            The string to encode.
     * @return The MD5 digest of the target string.
     */
    public static String toMd5(String target) {
        try {
            return toMd5(target, "US-ASCII");
        } catch (UnsupportedEncodingException uee) {
            // Unlikely, US-ASCII comes with every JVM
            throw new RuntimeException(
                    "US-ASCII is an unsupported encoding, unable to compute MD5");
        }
    }

    /**
     * Returns the MD5 digest of target string. Target is decoded to bytes using
     * the named charset. The returned hexadecimal String always contains 32
     * lowercase alphanumeric characters. For example, if target is
     * "HelloWorld", this method returns "68e109f0f40ca72a15e05cc22786f8e6".
     * 
     * @param target
     *            The string to encode.
     * @param charsetName
     *            The character set.
     * @return The MD5 digest of the target string.
     * 
     * @throws UnsupportedEncodingException
     */
    public static String toMd5(String target, String charsetName)
            throws UnsupportedEncodingException {
        try {
            final byte[] md5 = MessageDigest.getInstance("MD5").digest(
                    target.getBytes(charsetName));
            final char[] md5Chars = new char[32];
            int i = 0;
            for (final byte b : md5) {
                md5Chars[i++] = HEXDIGITS[(b >> 4) & 0xF];
                md5Chars[i++] = HEXDIGITS[b & 0xF];
            }
            return new String(md5Chars);
        } catch (NoSuchAlgorithmException nsae) {
            throw new RuntimeException(
                    "No MD5 algorithm, unable to compute MD5");
        }
    }

    /**
     * Returns the SHA1 digest of the target string. Target is decoded to bytes
     * using the US-ASCII charset.
     * 
     * @param target
     *            The string to encode.
     * @return The MD5 digest of the target string.
     */
    public static String toSha1(String target) {
        try {
            return toSha1(target, "US-ASCII");
        } catch (UnsupportedEncodingException uee) {
            // Unlikely, US-ASCII comes with every JVM
            throw new RuntimeException(
                    "US-ASCII is an unsupported encoding, unable to compute SHA1");
        }
    }

    /**
     * Returns the SHA1 digest of target string. Target is decoded to bytes
     * using the named charset.
     * 
     * @param target
     *            The string to encode.
     * @param charsetName
     *            The character set.
     * @return The SHA1 digest of the target string.
     * 
     * @throws UnsupportedEncodingException
     */
    public static String toSha1(String target, String charsetName)
            throws UnsupportedEncodingException {
        try {
            return Base64.encode(MessageDigest.getInstance("SHA1").digest(
                    target.getBytes(charsetName)), false);
        } catch (NoSuchAlgorithmException nsae) {
            throw new RuntimeException(
                    "No SHA1 algorithm, unable to compute SHA1");
        }
    }

}
