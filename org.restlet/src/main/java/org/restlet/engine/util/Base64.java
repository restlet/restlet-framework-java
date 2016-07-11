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

package org.restlet.engine.util;

import java.util.Arrays;

import org.restlet.engine.io.IoUtils;

/**
 * Minimal but fast Base64 codec.
 * 
 * @author Ray Waldin (ray@waldin.net)
 */
public class Base64 {

    /** alphabet used for encoding bytes into base64 */
    private static final char[] BASE64_DIGITS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"
            .toCharArray();

    /**
     * Decoding involves replacing each character with the character's value, or
     * position, from the above alphabet, and this table makes such lookups
     * quick and easy. Couldn't help myself with the corny name :)
     */
    private static final byte[] DECODER_RING = new byte[128];

    /**
     * Initializes the decoder ring.
     */
    static {
        Arrays.fill(DECODER_RING, (byte) -1);
        int i = 0;
        for (final char c : BASE64_DIGITS) {
            DECODER_RING[c] = (byte) i++;
        }
        DECODER_RING['='] = 0;
    }

    /**
     * Returns the byte value at a given position in a bytes array.
     * 
     * @param data
     *            The bytes array.
     * @param block
     *            The block size.
     * @param off
     *            The offset value.
     * @return The extracted byte.
     */
    private final static int byteAt(byte[] data, int block, int off) {
        return unsign(data[(block * 3) + off]);
    }

    /**
     * Decodes base64 characters into bytes. Newline characters found at block
     * boundaries will be ignored.
     * 
     * @param chars
     *            The characters array to decode.
     * @return The decoded byte array.
     */
    public static byte[] decode(final char[] chars) {
        // prepare to ignore newline chars
        int newlineCount = 0;
        for (char c : chars) {
            switch (c) {
            case '\r':
            case '\n':
                newlineCount++;
                break;
            default:
            }
        }

        int len = chars.length - newlineCount;

        if (len % 4 != 0) {
            throw new IllegalArgumentException(
                    "Base64.decode() requires input length to be a multiple of 4");
        }

        int numBytes = ((len + 3) / 4) * 3;

        // fix up length relative to padding
        if (len > 1) {
            if (chars[chars.length - 2] == '=') {
                numBytes -= 2;
            } else if (chars[chars.length - 1] == '=') {
                numBytes--;
            }
        }

        byte[] result = new byte[numBytes];
        int newlineOffset = 0;

        // decode each block of 4 chars into 3 bytes
        for (int i = 0; i < (len + 3) / 4; ++i) {
            int charOffset = newlineOffset + (i * 4);

            final char c1 = chars[charOffset++];
            final char c2 = chars[charOffset++];
            final char c3 = chars[charOffset++];
            final char c4 = chars[charOffset++];

            if (!(validChar(c1) && validChar(c2) && validChar(c3) && validChar(c4))) {
                throw new IllegalArgumentException(
                        "Invalid Base64 character in block: '" + c1 + c2 + c3
                                + c4 + "'");
            }

            // pack
            final int x = DECODER_RING[c1] << 18 | DECODER_RING[c2] << 12
                    | (c3 == '=' ? 0 : DECODER_RING[c3] << 6)
                    | (c4 == '=' ? 0 : DECODER_RING[c4]);

            // unpack
            int byteOffset = i * 3;
            result[byteOffset++] = (byte) (x >> 16);
            if (c3 != '=') {
                result[byteOffset++] = (byte) ((x >> 8) & 0xFF);
                if (c4 != '=') {
                    result[byteOffset++] = (byte) (x & 0xFF);
                }
            }

            // skip newlines after block
            outer: while (chars.length > charOffset) {
                switch (chars[charOffset++]) {
                case '\r':
                case '\n':
                    newlineOffset++;
                    break;

                default:
                    break outer;
                }
            }
        }
        return result;
    };

    /**
     * Decodes a base64 string into bytes. Newline characters found at block
     * boundaries will be ignored.
     * 
     * @param encodedString
     *            The string to decode.
     * @return The decoded byte array.
     */
    public static byte[] decode(String encodedString) {
        return decode(encodedString.toCharArray());
    }

    /**
     * Encodes an entire byte array into a Base64 string, with optional newlines
     * after every 76 characters.
     * 
     * @param bytes
     *            The byte array to encode.
     * @param newlines
     *            Indicates whether or not newlines are desired.
     * @return The encoded string.
     */
    public static String encode(byte[] bytes, boolean newlines) {
        return encode(bytes, 0, bytes.length, newlines);
    }

    /**
     * Encodes specified bytes into a Base64 string, with optional newlines
     * after every 76 characters.
     * 
     * @param bytes
     *            The byte array to encode.
     * @param off
     *            The starting offset.
     * @param len
     *            The number of bytes to encode.
     * @param newlines
     *            Indicates whether or not newlines are desired.
     * 
     * @return The encoded string.
     */
    public static String encode(byte[] bytes, int off, int len, boolean newlines) {
        char[] output = new char[(((len + 2) / 3) * 4)
                + (newlines ? len / 43 : 0)];
        int pos = 0;

        // encode each block of 3 bytes into 4 chars
        for (int i = 0; i < (len + 2) / 3; ++i) {
            int pad = 0;

            if (len + 1 < (i + 1) * 3) {
                // two trailing '='s
                pad = 2;
            } else if (len < (i + 1) * 3) {
                // one trailing '='
                pad = 1;
            }

            // pack
            int x = (byteAt(bytes, i, off) << 16)
                    | (pad > 1 ? 0 : (byteAt(bytes, i, off + 1) << 8))
                    | (pad > 0 ? 0 : (byteAt(bytes, i, off + 2)));

            // unpack
            output[pos++] = BASE64_DIGITS[x >> 18];
            output[pos++] = BASE64_DIGITS[(x >> 12) & 0x3F];
            output[pos++] = pad > 1 ? '=' : BASE64_DIGITS[(x >> 6) & 0x3F];
            output[pos++] = pad > 0 ? '=' : BASE64_DIGITS[x & 0x3F];

            if (newlines && ((i + 1) % 19 == 0)) {
                output[pos++] = '\n';
            }
        }
        return new String(output, 0, pos);
    }

    // [ifndef gwt] method
    /**
     * Encodes an entire chars array into a Base64 string, with optional
     * newlines after every 76 characters.
     * 
     * @param chars
     *            The characters array to encode.
     * @param newlines
     *            Indicates whether or not newlines are desired.
     * @return The encoded string.
     */
    public static String encode(char[] chars, boolean newlines) {
        return encode(IoUtils.toByteArray(chars), newlines);
    }

    // [ifndef gwt] method
    /**
     * Encodes an entire chars array into a Base64 string, with optional
     * newlines after every 76 characters.
     * 
     * @param chars
     *            The characters array to encode.
     * @param charset
     *            The character set to use for the character to byte conversion.
     * @param newlines
     *            Indicates whether or not newlines are desired.
     * @return The encoded string.
     */
    public static String encode(char[] chars, String charset, boolean newlines) {
        return encode(IoUtils.toByteArray(chars, charset), newlines);
    }

    /**
     * Computes the unsigned value of a byte.
     * 
     * @param b
     *            The input byte.
     * @return The output unsigned value.
     */
    private final static int unsign(byte b) {
        return b < 0 ? b + 256 : b;
    }

    /**
     * Indicates if the character is valid and can be decoded.
     * 
     * @param c
     *            The input character.
     * @return True if the character is valid.
     */
    private final static boolean validChar(char c) {
        return (c < 128) && (DECODER_RING[c] != -1);
    }
}
