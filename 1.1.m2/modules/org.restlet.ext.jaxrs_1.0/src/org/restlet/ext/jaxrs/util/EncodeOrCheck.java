/*
 * Copyright 2005-2008 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */
package org.restlet.ext.jaxrs.util;

// unreserv = ALPHA / DIGIT / "-" / "." / "_" / "~"
// unreserv = ALPHA / DIGIT / 045 / 046 / 095 / 126
// reserved = gen-delims / sub-delims
// gen-del = ":" / "/" / "?" / "#" / "[" / "]" / "@"
// gen-del = 058 / 047 / 063 / 035 / 091 / 093 / 064
// sub-del = "!" / "$" / "&" / "'" / "(" / ")" / "*" / "+" / "," / ";" / "="
// sub-del = 033 / 036 / 038 / 039 / 040 / 041 / 042 / 043 / 044 / 059 / 061
// pct-enc = "%" HEXDIG HEXDIG

/**
 * Utility class to encode or check data
 * 
 * @author Stephan Koops
 */
public class EncodeOrCheck {

    /** A table of hex digits */
    private static final char[] HEX_DIGITS = { '0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

    /**
     * the unreserved characters in URIs
     */
    public static final String UNRESERVED = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890-._~";

    /**
     * the gen-delimiter characters in URIs
     */
    public static final String GEN_DELIMITERS = ":/?#[]@";

    /**
     * the sub-delimiter characters in URIs
     */
    public static final String SUB_DELIMITERS = "!$&'()*+,;=";

    /**
     * the additional allowed characters for template parameters in URIs.
     */
    public static final String TEMPL_PARAMS = "{}";

    /**
     * the reserved characters in URIs
     */
    public static final String RESERVED = GEN_DELIMITERS + SUB_DELIMITERS;

    /**
     * the characters forbidden in a fragment.
     */
    public static final String FRAGMENT_FORBIDDEN;
    static {
        StringBuilder stb = new StringBuilder();
        for (char c = 0; c < 256; c++) {
            String cc = new String(new char[] { c });
            if (c != '%' && c != '{' && c != '}' && !RESERVED.contains(cc) && !UNRESERVED.contains(cc))
                stb.append(c);
        }
        FRAGMENT_FORBIDDEN = stb.toString();
    }

    /**
     * Checks, if the given {@link CharSequence} has valid hex digits at the
     * both positions after the given index.
     * 
     * @param charSequence
     * @param percentPos
     * @throws IllegalArgumentException
     */
    public static void checkForHexDigit(CharSequence charSequence,
            int percentPos) throws IllegalArgumentException {
        if (percentPos < 0)
            throw new RuntimeException("the precentPos must be >= 0");
        if (charSequence.length() <= percentPos + 2) {
            CharSequence hexDigits = charSequence.subSequence(percentPos,
                    charSequence.length());
            throw new IllegalArgumentException(
                    "A percent encoding must have two charachters, so "
                            + hexDigits + " is not allowed");
        }
        char c1 = charSequence.charAt(percentPos + 1);
        char c2 = charSequence.charAt(percentPos + 2);
        if (!((c1 >= '0' && c1 <= '9') || (c1 >= 'A' && c1 <= 'F') || (c1 >= 'a' && c1 <= 'f'))
                || !((c2 >= '0' && c2 <= '9') || (c2 >= 'A' && c2 <= 'F') || (c2 >= 'a' && c2 <= 'f'))) {
            String message;
            message = "The percent encoded char %" + c1 + c2 + " is not valid";
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Checks, if the string contains characters that are reserved in URIs.
     * 
     * @see <a href="http://tools.ietf.org/html/rfc3986#section-2.2">RFC 3986,
     *      Section 2.2</a>
     * @param uriPart
     * @param indexForErrMessage
     * @param errMessName
     * @throws IllegalArgumentException
     */
    public static void checkForInvalidUriChars(CharSequence uriPart,
            int indexForErrMessage, String errMessName)
            throws IllegalArgumentException {
        // LATER Characters in variables should not be checked.
        int l = uriPart.length();
        for (int i = 0; i < l; i++) {
            char c = uriPart.charAt(i);
            switch (c) {
            case ':':
            case '/':
            case '?':
            case '#':
            case '[':
            case ']':
            case '@':
            case '!':
            case '$':
            case '&':
            case '\'':
            case '(':
            case ')':
            case '*':
            case '+':
            case ',':
            case ';':
            case '=':
                throw throwIllegalArgExc(indexForErrMessage, errMessName,
                        uriPart, " contains at least one reservec character: "
                                + c + ". They must be encoded.");
            }
            if (c == ' ' || c < 32 || c >= 127)
                throw throwIllegalArgExc(indexForErrMessage, errMessName,
                        uriPart, " contains at least one illegal character: "
                                + c + ". They must be encoded.");
        }
    }

    /**
     * Appends the given character encoded (if not unreserved) to the
     * {@link StringBuilder}.
     * 
     * @param toEncode
     *                the character to encode.
     * @param stb
     *                the {@link StringBuilder} to append.
     * @see <a href="http://tools.ietf.org/html/rfc3986#section-2.2"> RFC 3986,
     *      section 2.2</a>
     */
    public static void encode(char toEncode, StringBuilder stb) {
        // this are all unreserved characters, see RFC 3986 (section 2.2)
        // http://tools.ietf.org/html/rfc3986#section-2.2
        if ((toEncode >= 'A' && toEncode <= 'Z')
                || (toEncode >= 'a' && toEncode <= 'z')
                || (toEncode >= '0' && toEncode <= '9') || (toEncode == '-')
                || (toEncode == '.') || (toEncode == '_') || (toEncode == '~')) {
            stb.append(toEncode);
        } else {
            toHex(toEncode, stb);
        }
    }

    /**
     * Encodes the given string, if encoding is enabled. If encoding is
     * disabled, the methods checks the validaty of the containing characters.
     * 
     * @param uriPart
     *                the string to encode or check. Must not be null; result
     *                are not defined.
     * @param encode
     *                see {@link #encode}
     * @param encodeSlash
     *                if encode is true: if encodeSlash is true, than slashes
     *                are also converted, otherwise not. if encode is false,
     *                this is ignored.
     * @param indexForErrMessage
     *                index in an array or list if necessary. If not necessary,
     *                set it lower than zero.
     * @param errMessName
     *                The name for the message
     * @return
     * @throws IllegalArgumentException
     *                 if the given String is null, or at least one char is
     *                 invalid.
     */
    public static CharSequence encode(CharSequence uriPart, boolean encode,
            boolean encodeSlash, int indexForErrMessage, String errMessName)
            throws IllegalArgumentException {
        if (uriPart == null)
            throw throwIllegalArgExc(indexForErrMessage, errMessName, uriPart,
                    " must not be null");
        if (encode)
            return encodeNotBraces(uriPart, encodeSlash);
        EncodeOrCheck.checkForInvalidUriChars(uriPart, indexForErrMessage,
                errMessName);
        return uriPart;
    }

    /**
     * This methods encodes the given String, but doesn't encode braces.
     * 
     * @param uriPart
     *                the String to encode
     * @param encodeSlash
     *                if encodeSlash is true, than slashes are also converted,
     *                otherwise not.
     * @return the encoded String
     */
    public static CharSequence encodeNotBraces(CharSequence uriPart,
            boolean encodeSlash) {
        StringBuilder stb = new StringBuilder();
        int l = uriPart.length();
        for (int i = 0; i < l; i++) {
            char c = uriPart.charAt(i);
            if (c == '{' || c == '}' || (!encodeSlash && c == '/'))
                stb.append(c);
            else
                encode(c, stb);
        }
        return stb;
    }

    /**
     * @param fragment
     *                the fragment, may contain URI template parameters.
     * @param encode
     * @return
     * @throws IllegalArgumentException
     */
    public static CharSequence fragment(CharSequence fragment, boolean encode)
            throws IllegalArgumentException {
        // This method is optimized for speed, so it is not very good readable.
        StringBuilder stb = new StringBuilder(fragment.length());
        int length = fragment.length();
        for (int i = 0; i < length; i++) {
            char c = fragment.charAt(i);
            if (c >= 97 && c <= 123) // lower chars and beside them
                stb.append(c);
            else if (c >= 63 && c <= 91) // upper chars and beside them
                stb.append(c);
            else if (c >= 38 && c <= 59) // digits and beside them
                stb.append(c);
            else if (c == '!' || c == '#' || c == '$' || c == '=' || c == ']'
                    || c == '_' || c == '}' || c == '~') // other allowd char
                stb.append(c);
            else if (c == '%') {
                if (encode)
                    toHex(c, stb);
                else {
                    checkForHexDigit(fragment, i);
                    i += 2;
                }
            } else
                toHexOrReject(c, stb, encode);
            // allowed is: 033 / 035 / 036 / 038 / 039 / 040 / 041 / 042 / 043 /
            // 044 / 045 / 046 / 047 / 48-57 / 058 /059 / 061 / 063 / 064 /
            // 65-90 / 091 / 093 / 095 / 97-122 / 123 / 125 / 126
        }
        return stb;
    }

    /**
     * encodes respectively checks a full query.
     * 
     * @param query
     *                query to convert or check, may contain URI template
     *                parameters.
     * @param encode
     * @return
     */
    @SuppressWarnings("unused")
    public static StringBuilder fullQuery(CharSequence query, boolean encode) {
        // LATER replaceQueryParams: alles außer "=" und "?" kodieren/verbieten
        // LATER query = *( pchar / "/" / "?" )
        // "=" und "&" nicht kodieren.
        if (query == null)
            return null;
        if (query instanceof StringBuilder)
            return (StringBuilder) query;
        return new StringBuilder(query);
    }

    /**
     * Writes the ASCII chars from 32 to 127 to System.out
     * 
     * @param args
     */
    public static void main(String[] args) {
        for (char c = 32; c <= 127; c++) {
            System.out.println(((int) c) + " " + c);
        }
    }

    /**
     * @param string
     *                String to convert, may contain URI template parameters.
     * @param encode
     * @param indexForErrMessage
     *                index in an array or list if necessary. If not necessary,
     *                set it lower than zero.
     * @param nameForMessage
     *                The name for the message
     * @return
     * @throws IllegalArgumentException
     */
    public static String nameOrValue(CharSequence string, boolean encode,
            int indexForErrMessage, String nameForMessage)
            throws IllegalArgumentException {
        // LATER matrixParam(..): hier gilt, was im pathSegment erlaubt ist,
        // "=" und "&" und ";" nicht kodieren
        CharSequence encoded = encode(string, encode, true, indexForErrMessage,
                nameForMessage);
        if (encoded == null)
            return null;
        return encoded.toString();
    }

    /**
     * @param string
     *                String to convert, may contain URI template parameters.
     * @param encode
     *                if true, the String is encoded, if false it is checked, if
     *                all chars are valid.
     * @param nameForMessage
     *                The name for the message
     * @return
     * @throws IllegalArgumentException
     */
    public static String nameOrValue(CharSequence string, boolean encode,
            String nameForMessage) throws IllegalArgumentException {
        return nameOrValue(string, encode, Integer.MIN_VALUE, nameForMessage);
    }

    /**
     * 
     * @param index
     *                index, starting with zero.
     * @param errMessName
     *                the name of the string with illegal characters
     * @param illegalString
     *                the illegal String
     * @param messageEnd
     * @return
     */
    private static IllegalArgumentException throwIllegalArgExc(int index,
            String errMessName, CharSequence illegalString, String messageEnd) {
        StringBuilder stb = new StringBuilder();
        stb.append("The ");
        if (index >= 0) {
            stb.append(index);
            stb.append(". ");
        }
        stb.append(errMessName);
        stb.append(" (");
        stb.append(illegalString);
        stb.append(")");
        stb.append(messageEnd);
        if (index >= 0)
            stb.append(" (index starting with 0)");
        throw new IllegalArgumentException(stb.toString());
    }

    /**
     * Appends the given character precent-encoded to the {@link StringBuilder}.
     * Example: ' ' -> "%20%
     * 
     * @param toEncode
     * @param stb
     */
    private static void toHex(char toEncode, StringBuilder stb) {
        stb.append('%');
        stb.append(HEX_DIGITS[(toEncode >> 4) & 0xF]);
        stb.append(HEX_DIGITS[toEncode & 0xF]);
    }

    private static void toHexOrReject(char c, StringBuilder stb, boolean encode) {
        if (encode)
            toHex(c, stb);
        else {
            String message = "The character " + c + " is not valid";
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * @param userInfo
     *                the URI user-info, may contain URI template parameters
     * @param encode
     * @return
     * @throws IllegalArgumentException
     *                 if automatic encoding is disabled and the userInfo
     *                 contains illegal characters, or if the userInfo is null.
     */
    public static CharSequence userInfo(CharSequence userInfo, boolean encode)
            throws IllegalArgumentException {
        return encode(userInfo, encode, true, Integer.MIN_VALUE, "userInfo");
        // LATER userinfo = *( unreserved / pct-encoded / sub-delims / ":" )
    }
}