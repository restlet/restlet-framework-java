/**
 * Copyright 2005-2008 Noelios Technologies.
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
 * Alternatively, you can obtain a royaltee free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.engine.util;

import java.util.Arrays;

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
     * decoding involves replacing each character with the character's value, or
     * position, from the above alphabet, and this table makes such lookups
     * quick and easy. Couldn't help myself with the corny name :)
     */
    private static final byte[] DECODER_RING = new byte[128];

    static {
        Arrays.fill(DECODER_RING, (byte) -1);
        int i = 0;
        for (final char c : BASE64_DIGITS) {
            DECODER_RING[c] = (byte) i++;
        }
        DECODER_RING['='] = 0;
    }

    private final static int byteAt(byte[] data, int block, int off) {
        return unsign(data[(block * 3) + off]);
    }

    /**
     * Decodes a base64 string into bytes. Newline characters found at block
     * boundaries will be ignored.
     * 
     * @param encodedString
     *            The string to decode.
     * @return The decoded byte array.
     */
    public static byte[] decode(String encodedString) {
        final char[] chars = encodedString.toCharArray();

        // prepare to ignore newline chars
        int newlineCount = 0;
        for (final char c : chars) {
            switch (c) {
            case '\r':
            case '\n':
                newlineCount++;
                break;

            default:
            }
        }

        final int len = chars.length - newlineCount;
        int numBytes = ((len + 3) / 4) * 3;

        // fix up length relative to padding
        if (len > 1) {
            if (chars[chars.length - 2] == '=') {
                numBytes -= 2;
            } else if (chars[chars.length - 1] == '=') {
                numBytes--;
            }
        }

        final byte[] result = new byte[numBytes];
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
        final char[] output = new char[(((len + 2) / 3) * 4)
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
            final int x = (byteAt(bytes, i, off) << 16)
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

    private final static int unsign(byte b) {
        return b < 0 ? b + 256 : b;
    }

    private final static boolean validChar(char c) {
        return (c < 128) && (DECODER_RING[c] != -1);
    }
}
