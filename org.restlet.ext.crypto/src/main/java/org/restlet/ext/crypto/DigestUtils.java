/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.crypto;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.restlet.data.Digest;

/**
 * Security data manipulation utilities.
 *
 * @author Jerome Louvel
 */
public class DigestUtils {

    /**
     * General regex pattern to extract comma separated name-value components. This pattern captures
     * one name and value per match() and is repeatedly applied to the input string to extract all
     * components. Must handle both quoted and unquoted values as RFC2617 isn't consistent in this
     * respect. Pattern is immutable and thread-safe, so reuse one static instance.
     */
    private static final char[] HEX_DIGITS = "0123456789abcdef".toCharArray();

    /**
     * Returns the digest of the target string. Target is decoded to bytes using the US-ASCII
     * charset. Supports MD5 and SHA-1 algorithms.
     *
     * @param target The string to encode.
     * @param algorithm The digest algorithm to use.
     * @return The digest of the target string.
     */
    public static char[] digest(char[] target, String algorithm) {
        return DigestUtils.digest(new String(target), algorithm).toCharArray();
    }

    /**
     * Returns the digest of the target string. Target is decoded to bytes using the US-ASCII
     * charset. Supports MD5 and SHA-1 algorithms.
     *
     * @param target The string to encode.
     * @param algorithm The digest algorithm to use.
     * @return The digest of the target string.
     */
    public static String digest(String target, String algorithm) {
        if (Digest.ALGORITHM_MD5.equals(algorithm)) {
            return toMd5(target);
        } else if (Digest.ALGORITHM_SHA_1.equals(algorithm)) {
            return toSha1(target);
        }

        throw new IllegalArgumentException("Unsupported algorithm.");
    }

    /**
     * Converts a source string to its HMAC/SHA-1 value.
     *
     * @param source The source string to convert.
     * @param secretKey The secret key to use for conversion.
     * @return The HMac value of the source string.
     */
    public static byte[] toHMacSha1(String source, byte[] secretKey) {
        byte[] result = null;

        try {
            // Create the HMAC/SHA1 key
            SecretKeySpec signingKey = new SecretKeySpec(secretKey, "HmacSHA1");

            // Create the message authentication code (MAC)
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(signingKey);

            // Compute the HMAC value
            result = mac.doFinal(source.getBytes());
        } catch (NoSuchAlgorithmException nsae) {
            throw new IllegalArgumentException(
                    "Could not find the SHA-1 algorithm. HMac conversion failed.", nsae);
        } catch (InvalidKeyException ike) {
            throw new IllegalArgumentException(
                    "Invalid key exception detected. HMac conversion failed.", ike);
        }

        return result;
    }

    /**
     * Converts a source string to its HMAC/SHA-1 value.
     *
     * @param source The source string to convert.
     * @param secretKey The secret key to use for conversion.
     * @return The HMac value of the source string.
     */
    public static byte[] toHMacSha1(String source, String secretKey) {
        return toHMacSha1(source, secretKey.getBytes());
    }

    /**
     * Converts a source string to its HMAC/SHA256 value.
     *
     * @param source The source string to convert.
     * @param secretKey The secret key to use for conversion.
     * @return The HMac value of the source string.
     */
    public static byte[] toHMacSha256(String source, byte[] secretKey) {
        byte[] result = null;

        try {
            // Create the HMAC/SHA256 key
            SecretKeySpec signingKey = new SecretKeySpec(secretKey, "HmacSHA256");

            // Create the message authentication code (MAC)
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(signingKey);

            // Compute the HMAC value
            result = mac.doFinal(source.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException nsae) {
            throw new IllegalArgumentException(
                    "Could not find the SHA256 algorithm. HMac conversion failed.", nsae);
        } catch (InvalidKeyException ike) {
            throw new IllegalArgumentException(
                    "Invalid key exception detected. HMac conversion failed.", ike);
        } catch (IllegalStateException ise) {
            throw new IllegalArgumentException(
                    "IIllegal state exception detected. HMac conversion failed.", ise);
        }

        return result;
    }

    /**
     * Converts a source string to its HMAC/SHA256 value.
     *
     * @param source The source string to convert.
     * @param secretKey The secret key to use for conversion.
     * @return The HMac value of the source string.
     */
    public static byte[] toHMacSha256(String source, String secretKey) {
        return toHMacSha256(source, secretKey.getBytes());
    }

    /**
     * Return the HTTP DIGEST hashed secret. It concatenates the identifier, realm and secret,
     * separated by a comma and digests them using MD5.
     *
     * @param identifier The user identifier to hash.
     * @param secret The user secret.
     * @param realm The authentication realm.
     * @return A hash of the username, realm, and password, specified as A1 in section 3.2.2.2 of
     *     RFC2617, or null if the identifier has no corresponding secret.
     */
    public static String toHttpDigest(String identifier, char[] secret, String realm) {
        if (secret != null) {
            return toMd5(identifier + ":" + realm + ":" + new String(secret));
        }

        return null;
    }

    /**
     * Returns the MD5 digest of the target string. Target is decoded to bytes using the US-ASCII
     * charset. The returned hexadecimal String always contains 32 lowercase alphanumeric
     * characters. For example, if the target is "HelloWorld", this method returns
     * "68e109f0f40ca72a15e05cc22786f8e6".
     *
     * @param target The string to encode.
     * @return The MD5 digest of the target string.
     */
    public static String toMd5(String target) {
        try {
            return toMd5(target, "US-ASCII");
        } catch (UnsupportedEncodingException uee) {
            // Unlikely, US-ASCII comes with every JVM
            throw new IllegalArgumentException(
                    "US-ASCII is an unsupported encoding, unable to compute MD5");
        }
    }

    /**
     * Returns the MD5 digest of the target string. Target is decoded to bytes using the named
     * charset. The returned hexadecimal String always contains 32 lowercase alphanumeric
     * characters. For example, if the target is "HelloWorld", this method returns
     * "68e109f0f40ca72a15e05cc22786f8e6".
     *
     * @param target The string to encode.
     * @param charsetName The character set.
     * @return The MD5 digest of the target string.
     * @throws UnsupportedEncodingException
     */
    public static String toMd5(String target, String charsetName)
            throws UnsupportedEncodingException {
        try {
            final byte[] md5 =
                    MessageDigest.getInstance("MD5").digest(target.getBytes(charsetName));
            final char[] md5Chars = new char[32];
            int i = 0;
            for (final byte b : md5) {
                md5Chars[i++] = HEX_DIGITS[(b >> 4) & 0xF];
                md5Chars[i++] = HEX_DIGITS[b & 0xF];
            }
            return new String(md5Chars);
        } catch (NoSuchAlgorithmException nsae) {
            throw new IllegalArgumentException("No MD5 algorithm, unable to compute MD5");
        }
    }

    /**
     * Returns the SHA1 digest of the target string. Target is decoded to bytes using the US-ASCII
     * charset.
     *
     * @param target The string to encode.
     * @return The MD5 digest of the target string.
     */
    public static String toSha1(String target) {
        try {
            return toSha1(target, "US-ASCII");
        } catch (UnsupportedEncodingException uee) {
            // Unlikely, US-ASCII comes with every JVM
            throw new IllegalArgumentException(
                    "US-ASCII is an unsupported encoding, unable to compute SHA1");
        }
    }

    /**
     * Returns the SHA1 digest of the target string. Target is decoded to bytes using the named
     * charset.
     *
     * @param target The string to encode.
     * @param charsetName The character set.
     * @return The SHA1 digest of the target string.
     * @throws UnsupportedEncodingException
     */
    public static String toSha1(String target, String charsetName)
            throws UnsupportedEncodingException {
        try {
            return Base64.getEncoder()
                    .encodeToString(
                            MessageDigest.getInstance("SHA1").digest(target.getBytes(charsetName)));
        } catch (NoSuchAlgorithmException nsae) {
            throw new IllegalArgumentException("No SHA1 algorithm, unable to compute SHA1");
        }
    }

    /**
     * Private constructor to ensure that the class acts as a true utility class i.e., it isn't
     * instantiable and extensible.
     */
    private DigestUtils() {}
}
