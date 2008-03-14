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
package org.restlet.ext.jaxrs.internal.util;

import org.restlet.data.Reference;

/**
 * Utility class to encode or check data.
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
            if (c != '%' && c != '{' && c != '}' && !RESERVED.contains(cc)
                    && !UNRESERVED.contains(cc))
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
     * @param string
     * @param i
     * @param stb
     */
    private static void checkForHexDigitAndAppend(CharSequence string, int i,
            StringBuilder stb) {
        checkForHexDigit(string, i);
        stb.append('%');
        stb.append(string.charAt(i + 1));
        stb.append(string.charAt(i + 2));
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
     *                 if the CharSequence to test contains illegal characters.
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
     * Checks, if the String is a valid URI scheme
     * 
     * @param scheme
     *                the String to check. May contain template variables.
     * @throws IllegalArgumentException
     *                 If the string is not a valid URI scheme.
     */
    public static void checkValidScheme(String scheme)
            throws IllegalArgumentException {
        if (scheme == null)
            throw new IllegalArgumentException("The scheme must not be null");
        int schemeLength = scheme.length();
        if (schemeLength == 0)
            throw new IllegalArgumentException(
                    "The scheme must not be an empty String");
        char c = scheme.charAt(0);
        if (!((c > 64 && c <= 90) || (c > 92 && c <= 118) || (c == '{') || (c == '}')))
            throw new IllegalArgumentException(
                    "The first character of a scheme must be an alphabetic character");
        for (int i = 1; i < schemeLength; i++) {
            c = scheme.charAt(i);
            if ((c > 64 && c <= 90) || (c > 92 && c <= 118)
                    || (c >= '0' && c <= '9') || (c == '+') || (c == '-')
                    || (c == '.') || (c == '{') || (c == '}'))
                continue;
            String message = "The "
                    + i
                    + ". character of a scheme must be an alphabetic character, a number, a '+', a '-' or a '.'";
            throw new IllegalArgumentException(message);
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
     *                the fragment, may contain URI template parameters. Must
     *                not be null.
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
                    checkForHexDigitAndAppend(fragment, i, stb);
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
     * encodes respectively checks a full matrix parameter list.
     * 
     * @param matrix
     *                matrix parameters to convert or check, may contain URI
     *                template parameters. Must not be null.
     * @param encode
     * @return
     */
    @SuppressWarnings("unused")
    public static CharSequence fullMatrix(CharSequence matrix, boolean encode) {
        // this method is also used by #fullQuery(query, encode);
        int l = matrix.length();
        StringBuilder stb = new StringBuilder(l + 6);
        for (int i = 0; i < l; i++) {
            char c = matrix.charAt(i);
            if (Reference.isUnreserved(c) || Reference.isReserved(c))
                stb.append(c);
            else if (c == '{' || c == '}')
                stb.append(c);
            else if (c == '%') {
                if (encode)
                    toHex(c, stb);
                else {
                    checkForHexDigitAndAppend(matrix, i, stb);
                    i += 2;
                }
            } else
                toHexOrReject(c, stb, encode);
        }
        return stb;
    }

    /**
     * encodes respectively checks a full query.
     * 
     * @param query
     *                query to convert or check, may contain URI template
     *                parameters. Must not be null.
     * @param encode
     * @return
     */
    @SuppressWarnings("unused")
    public static CharSequence fullQuery(CharSequence query, boolean encode) {
        return fullMatrix(query, encode);
    }

    /**
     * @param host
     *                must not be null
     * @param encode
     * @return
     * @throws IllegalArgumentException
     *                 if the host contains an invalid character.
     */
    public static String host(String host) throws IllegalArgumentException {
        if (host.length() == 0)
            throw new IllegalArgumentException("The host must not be empty");
        int length = host.length();
        for (int i = 0; i < length; i++) {
            char ch = host.charAt(i);
            if (ch <= ' ' || ch >= 127) {
                String message = "The " + i + ". character is not valid";
                throw new IllegalArgumentException(message);
            }
        }
        return host;
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
     * Encodes the given string, if encoding is enabled. If encoding is
     * disabled, the methods checks the validaty of the containing characters.
     * 
     * @param string
     *                the string to encode or check. Must not be null; result
     *                are not defined, may contain URI template parameters.
     * @param encode
     *                see {@link #encode}
     * @param encodeSlash
     *                if encode is true: if encodeSlash is true, than slashes
     *                are also converted, otherwise not. if encode is false,
     *                this is ignored.
     * @param indexForErrMessage
     *                index in an array or list if necessary. If not necessary,
     *                set it lower than zero.
     * @param nameForMessage
     *                The name for the message
     * @return
     * @throws IllegalArgumentException
     *                 if the given String is null, or at least one char is
     *                 invalid.
     */
    public static CharSequence nameOrValue(CharSequence string, boolean encode,
            int indexForErrMessage, String nameForMessage)
            throws IllegalArgumentException {
        // LATER matrixParam(..): hier gilt, was im pathSegment erlaubt ist,
        // "=" und "&" und ";" nicht kodieren
        if (string == null)
            throw throwIllegalArgExc(indexForErrMessage, nameForMessage,
                    string, " must not be null");
        if (encode)
            return encodeNotBraces(string, true);
        else
            EncodeOrCheck.checkForInvalidUriChars(string, indexForErrMessage,
                    nameForMessage);

        return string.toString();
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
    public static CharSequence nameOrValue(CharSequence string, boolean encode,
            String nameForMessage) throws IllegalArgumentException {
        return nameOrValue(string, encode, Integer.MIN_VALUE, nameForMessage);
    }

    /**
     * @param path
     *                must not be null.
     * @param encode
     * @return
     */
    public static CharSequence pathSegmentWithMatrix(CharSequence path,
            boolean encode) {
        int l = path.length();
        StringBuilder stb = new StringBuilder(l + 6);
        for (int i = 0; i < l; i++) {
            char c = path.charAt(i);
            if (Reference.isUnreserved(c) || Reference.isReserved(c))
                stb.append(c);
            else if (c == '{' || c == '}')
                stb.append(c);
            else if (c == '%') {
                if (encode)
                    toHex(c, stb);
                else {
                    checkForHexDigitAndAppend(path, i, stb);
                    i += 2;
                }
            } else
                toHexOrReject(c, stb, encode);
        }
        return stb;
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
        // This method is optimized for speed, so it is not very good readable.
        int length = userInfo.length();
        StringBuilder stb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            char c = userInfo.charAt(i);
            if (Reference.isUnreserved(c) || Reference.isSubDelimiter(c))
                stb.append(c);
            else if (c == '{' || c == '}')
                stb.append(c);
            else if (c == ':')
                stb.append(c);
            else if (c == '%') {
                if (encode)
                    toHex(c, stb);
                else {
                    checkForHexDigitAndAppend(userInfo, i, stb);
                    i += 2;
                }
            } else
                toHexOrReject(c, stb, encode);
        }
        return stb;
        // LATER userinfo = *( unreserved / pct-encoded / sub-delims / ":" )
    }
}