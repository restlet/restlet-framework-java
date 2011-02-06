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
        final StringBuilder stb = new StringBuilder();
        for (char c = 0; c < 256; c++) {
            final String cc = new String(new char[] { c });
            if ((c != '%') && (c != '{') && (c != '}')
                    && !RESERVED.contains(cc) && !UNRESERVED.contains(cc)) {
                stb.append(c);
            }
        }
        FRAGMENT_FORBIDDEN = stb.toString();
    }

    /**
     * Checks / encodes all chars of the given char sequence.
     * 
     * @param string
     * @param encode
     *            true, if the value should be encoded, or false if not.
     * @return the enoced string (if it should be encoded)
     * @throws IllegalArgumentException
     *             if encode is false and at least one character of the
     *             CharSequence is invalid.
     */
    public static String all(CharSequence string, boolean encode)
            throws IllegalArgumentException {
        final int length = string.length();
        final StringBuilder stb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            final char c = string.charAt(i);
            if (c == '%') {
                processPercent(i, encode, string, stb);
            } else if (Reference.isValid(c)) {
                stb.append(c);
            } else {
                toHexOrReject(c, stb, encode);
            }
        }
        return stb.toString();
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
     *             if the CharSequence to test contains illegal characters.
     */
    public static void checkForInvalidUriChars(CharSequence uriPart,
            int indexForErrMessage, String errMessName)
            throws IllegalArgumentException {
        final int l = uriPart.length();
        boolean inVar = false;
        for (int i = 0; i < l; i++) {
            final char c = uriPart.charAt(i);
            if (inVar) {
                if (c == '}') {
                    inVar = false;
                }
                continue;
            }
            switch (c) {
            case '{':
                inVar = true;
                continue;
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
            if ((c == ' ') || (c < 32) || (c >= 127)) {
                throw throwIllegalArgExc(indexForErrMessage, errMessName,
                        uriPart, " contains at least one illegal character: "
                                + c + ". They must be encoded.");
            }
        }
    }

    /**
     * Appends the given character encoded (if not unreserved) to the
     * {@link StringBuilder}.
     * 
     * @param toEncode
     *            the character to encode.
     * @param stb
     *            the {@link StringBuilder} to append.
     * @return the number of added chars
     * @see <a href="http://tools.ietf.org/html/rfc3986#section-2.2"> RFC 3986,
     *      section 2.2</a>
     */
    static int encode(char toEncode, StringBuilder stb) {
        // this are all unreserved characters, see RFC 3986 (section 2.2)
        // http://tools.ietf.org/html/rfc3986#section-2.2
        if (((toEncode >= 'A') && (toEncode <= 'Z'))
                || ((toEncode >= 'a') && (toEncode <= 'z'))
                || ((toEncode >= '0') && (toEncode <= '9'))
                || (toEncode == '-') || (toEncode == '.') || (toEncode == '_')
                || (toEncode == '~')) {
            stb.append(toEncode);
            return 1;
        }

        toHex(toEncode, stb);
        return 3;
    }

    /**
     * This methods encodes the given String, but doesn't encode braces.
     * 
     * @param uriPart
     *            the String to encode
     * @param allowSemicolon
     *            if true, a ';' is encoded, otherwise an
     *            {@link IllegalArgumentException} is thrown.
     * @param encodeSlash
     *            if encodeSlash is true, than slashes are also converted,
     *            otherwise not.
     * @return the encoded String
     * @throws IllegalArgumentException
     */
    private static String encodeNotBraces(CharSequence uriPart,
            boolean allowSemicolon, boolean encodeSlash)
            throws IllegalArgumentException {
        final StringBuilder stb = new StringBuilder();
        final int l = uriPart.length();
        for (int i = 0; i < l; i++) {
            final char c = uriPart.charAt(i);
            if (c == '{') {
                i = processTemplVarname(uriPart, i, stb);
            } else if (c == '%') {
                processPercent(i, true, uriPart, stb);
            } else if (c == '}') {
                throw new IllegalArgumentException("'}' is only allowed as "
                        + "end of a variable name in \"" + uriPart + "\"");
            } else if (c == ';') {
                if (allowSemicolon) {
                    encode(c, stb);
                } else {
                    throw new IllegalArgumentException(
                            "A semicolon is not allowed in a path");
                }
            } else if (!encodeSlash && (c == '/')) {
                stb.append(c);
            } else {
                encode(c, stb);
            }
        }
        return stb.toString();
    }

    /**
     * @param fragment
     *            the fragment, may contain URI template parameters. Must not be
     *            null.
     * @return the encoced character sequence (if it should be encoded)
     * @throws IllegalArgumentException
     *             if encode is false and the fragment contains at least one
     *             invalid character.
     */
    public static CharSequence fragment(CharSequence fragment)
            throws IllegalArgumentException {
        // This method is optimized for speed, so it is not very good readable.
        final StringBuilder stb = new StringBuilder(fragment.length());
        final int length = fragment.length();
        for (int i = 0; i < length; i++) {
            final char c = fragment.charAt(i);
            if (c == '{') {
                i = processTemplVarname(fragment, i, stb);
            } else if ((c >= 97) && (c <= 122)) {
                stb.append(c);
            } else if ((c >= 63) && (c <= 91)) {
                stb.append(c);
            } else if ((c >= 38) && (c <= 59)) {
                stb.append(c);
            } else if ((c == '!') || (c == '#') || (c == '$') || (c == '=')
                    || (c == ']') || (c == '_') || (c == '~')) {
                stb.append(c);
            } else if (c == '}') {
                throw new IllegalArgumentException(
                        "'}' is only allowed as end of an template variable name");
            } else if (c == '%') {
                processPercent(i, true, fragment, stb);
            } else {
                toHexOrReject(c, stb, true);
                // allowed is: 033 / 035 / 036 / 038 / 039 / 040 / 041 / 042 /
                // 043 /
                // 044 / 045 / 046 / 047 / 48-57 / 058 /059 / 061 / 063 / 064 /
                // 65-90 / 091 / 093 / 095 / 97-122 / 123 / 125 / 126
            }
        }
        return stb;
    }

    /**
     * encodes respectively checks a full matrix parameter list.
     * 
     * @param matrix
     *            matrix parameters to convert or check, may contain URI
     *            template parameters. Must not be null.
     * @return the encoced character sequence (if it should be encoded)
     * @throws IllegalArgumentException
     *             if encode is false and at least one character is invalid.
     */
    public static CharSequence fullMatrix(CharSequence matrix)
            throws IllegalArgumentException {
        return fullQueryOrMatrix(matrix, ';', "%20", true);
    }

    /**
     * encodes respectively checks a full query.
     * 
     * @param query
     *            query to convert or check, may contain URI template
     *            parameters. Must not be null.
     * @param encode
     * @return the encoced character sequence (if it should be encoded)
     */
    public static CharSequence fullQuery(CharSequence query, boolean encode) {
        return fullQueryOrMatrix(query, '&', "+", encode);
    }

    /**
     * @param string
     * @param delimiter
     * @param spaceReplace
     *            The String to replace a space with ("+" or "%20")
     * @param encode
     * @return
     * @throws IllegalArgumentException
     */
    private static CharSequence fullQueryOrMatrix(CharSequence string,
            char delimiter, String spaceReplace, boolean encode)
            throws IllegalArgumentException {
        final int l = string.length();
        final StringBuilder stb = new StringBuilder(l + 6);
        for (int i = 0; i < l; i++) {
            final char c = string.charAt(i);
            if (c == '{') {
                i = processTemplVarname(string, i, stb);
            } else if ((c == delimiter) || (c == '=')) {
                stb.append(c);
            } else if (c == ' ') {
                if (encode) {
                    stb.append(spaceReplace);
                } else {
                    throw new IllegalArgumentException(
                            "A space is not allowed. Switch encode to on to auto encode it.");
                }
            } else if (Reference.isUnreserved(c)) {
                stb.append(c);
            } else if (c == '}') {
                throw new IllegalArgumentException("'}' is only allowed as "
                        + "end of a variable name in \"" + string + "\"");
            } else if (c == '%') {
                processPercent(i, encode, string, stb);
            } else {
                toHexOrReject(c, stb, encode);
            }
        }
        return stb;
    }

    /**
     * Checks, if the host String contains is valid.
     * 
     * @param host
     *            Could include template variable names. Must not be null, will
     *            throw a NullPointerException.
     * @return the valid host.
     * @throws IllegalArgumentException
     *             if the host contains an invalid character.
     */
    public static String host(String host) throws IllegalArgumentException {
        if (host.length() == 0) {
            throw new IllegalArgumentException("The host must not be empty");
        }
        final int length = host.length();
        // LATER de/encode: host
        for (int i = 0; i < length; i++) {
            final char ch = host.charAt(i);
            if ((ch <= ' ') || (ch >= 127)) {
                throw new IllegalArgumentException(
                        ("The " + i + ". character is not valid"));
            }
        }
        return host;
    }

    /**
     * Encodes the given string, if encoding is enabled. If encoding is
     * disabled, the methods checks the validaty of the containing characters.
     * 
     * @param nameOrValue
     *            the string to encode or check. Must not be null; result are
     *            not defined. May contain URI template parameters.
     * @param encode
     *            if true, the String is encoded, if false it is checked, if all
     *            chars are valid.
     * @param nameForMessage
     *            The name for the message
     * @return the encoced character sequence (if it should be encoded)
     * @throws IllegalArgumentException
     *             if the given String is null, or at least one char is invalid.
     */
    public static String nameOrValue(Object nameOrValue, boolean encode,
            String nameForMessage) throws IllegalArgumentException {
        if (nameOrValue == null) {
            throw throwIllegalArgExc(Integer.MIN_VALUE, nameForMessage,
                    nameOrValue, " must not be null");
        }
        CharSequence nov;
        if (nameOrValue instanceof CharSequence) {
            nov = (CharSequence) nameOrValue;
        } else {
            nov = nameOrValue.toString();
        }
        if (encode) {
            return encodeNotBraces(nov, true, true);
        }

        EncodeOrCheck.checkForInvalidUriChars(nov, Integer.MIN_VALUE,
                nameForMessage);
        return nov.toString();
    }

    /**
     * 
     * @param path
     * @param encode
     * @return the encoced character sequence (if it should be encoded)
     */
    public static CharSequence pathSegmentsWithMatrix(CharSequence path,
            boolean encode) {
        return pathSegmentWithMatrix(path, encode, false);
    }

    /**
     * @param pathSegments
     *            the path to check; must not be null.
     * @param encode
     * @return the encoced character sequence (if it should be encoded)
     * @throws IllegalArgumentException
     *             id encode is false and the path contains an invalid
     *             character.
     */
    public static CharSequence pathSegmentWithMatrix(CharSequence pathSegments,
            boolean encode) throws IllegalArgumentException {
        return pathSegmentWithMatrix(pathSegments, encode, true);
    }

    /**
     * @param pathSegments
     *            the path to check; must not be null.
     * @param encode
     * @return
     * @throws IllegalArgumentException
     *             id encode is false and the path contains an invalid
     *             character.
     */
    private static CharSequence pathSegmentWithMatrix(
            CharSequence pathSegments, boolean encode, boolean encodeSlash)
            throws IllegalArgumentException {
        final int l = pathSegments.length();
        final StringBuilder stb = new StringBuilder(l + 6);
        for (int i = 0; i < l; i++) {
            final char c = pathSegments.charAt(i);
            if (c == '{') {
                i = processTemplVarname(pathSegments, i, stb);
            } else if (c == '%') {
                processPercent(i, encode, pathSegments, stb);
            } else if (c == '/') {
                if (encodeSlash) {
                    toHex('/', stb);
                } else {
                    stb.append('/');
                }
            } else if (Reference.isUnreserved(c) || Reference.isReserved(c)) {
                stb.append(c);
            } else if (c == '}') {
                throw new IllegalArgumentException("'}' is only allowed "
                        + "as end of a variable name in \"" + pathSegments
                        + "\"");
            } else {
                toHexOrReject(c, stb, encode);
            }
        }
        return stb;
    }

    /**
     * This methods encodes the given path, but doesn't encode braces.
     * 
     * @param path
     *            the path to encode
     * @return the encoded String
     * @throws IllegalArgumentException
     *             if the path is not valid
     */
    public static CharSequence pathWithoutMatrix(CharSequence path)
            throws IllegalArgumentException {
        return encodeNotBraces(path, false, false);
    }

    /**
     * appends the '%' at the given position i to the given StringBuilder. <br>
     * Preconditions (not checked !!):
     * <ul>
     * <li>i > 0</li>
     * <li>at position i is a '%'.</li>
     * </ul>
     * 
     * @param i
     *            the index in the uriPart
     * @param encode
     * @param uriPart
     * @param stb
     */
    static void processPercent(int i, boolean encode, CharSequence uriPart,
            StringBuilder stb) {
        if (encode) {
            toHex('%', stb);
            return;
        }
        if (uriPart.length() <= i + 2) {
            final CharSequence hexDigits = uriPart.subSequence(i, uriPart
                    .length());
            throw new IllegalArgumentException(
                    "A percent encoding must have two charachters, so "
                            + hexDigits + " is not allowed");
        }

        char c1 = uriPart.charAt(i + 1);
        char c2 = uriPart.charAt(i + 2);
        if (!(((c1 >= '0') && (c1 <= '9')) || ((c1 >= 'A') && (c1 <= 'F')) || ((c1 >= 'a') && (c1 <= 'f')))
                || !(((c2 >= '0') && (c2 <= '9'))
                        || ((c2 >= 'A') && (c2 <= 'F')) || ((c2 >= 'a') && (c2 <= 'f')))) {
            throw new IllegalArgumentException("The percent encoded char %"
                    + c1 + c2 + " is not valid");
        }
        stb.append('%');
    }

    /**
     * Reads and checks the template variable name. The starting "{" is at given
     * position i.
     * 
     * @param uriPart
     *            the processed part of the uri
     * @param braceIndex
     *            the current for variable value (position of "{"). The method
     *            do not check, if this is the position of the '{'.
     * @param stb
     *            the StringBuilder to append the variable name with "{" and "}"
     *            to. Will be changed, also if an error occurs. May be null;
     *            then nothing is appended.
     * @return the position of the corresponding "}" to assign to the for
     *         variable.
     * @throws IllegalArgumentException
     *             if the rest of the uriPart contains a '{' or no '}'.
     */
    private static int processTemplVarname(CharSequence uriPart,
            int braceIndex, StringBuilder stb) throws IllegalArgumentException {
        final int l = uriPart.length();
        if (stb != null) {
            stb.append('{');
        }
        for (int i = braceIndex + 1; i < l; i++) {
            final char c = uriPart.charAt(i);
            if (c == '{') {
                throw new IllegalArgumentException("A variable must not "
                        + "contain an extra '{' in \"" + uriPart + "\"");
            }
            if (stb != null) {
                stb.append(c);
            }
            if (c == '}') {
                if (i == braceIndex + 1) {
                    throw new IllegalArgumentException(
                            "The template variable name '{}' is not allowed in "
                                    + "\"" + uriPart + "\"");
                }
                return i;
            }
        }
        throw new IllegalArgumentException("No '}' found after '{' "
                + "at position " + braceIndex + " of \"" + uriPart + "\"");
    }

    /**
     * Checks, if the String is a valid URI scheme
     * 
     * @param scheme
     *            the String to check. May contain template variables.
     * @return the valid scheme
     * @throws IllegalArgumentException
     *             If the string is not a valid URI scheme.
     */
    public static String scheme(String scheme) throws IllegalArgumentException {
        if (scheme == null) {
            throw new IllegalArgumentException("The scheme must not be null");
        }
        final int schemeLength = scheme.length();
        if (schemeLength == 0) {
            throw new IllegalArgumentException(
                    "The scheme must not be an empty String");
        }
        for (int i = 0; i < schemeLength; i++) {
            final char c = scheme.charAt(i);
            if (c == '{') {
                i = processTemplVarname(scheme, i, null);
            } else if (c == '}') {
                throw new IllegalArgumentException("The '}' is only allowed "
                        + "as end of a variable name in \"" + scheme + "\"");
            } else if (((c > 64) && (c <= 90)) || ((c > 92) && (c <= 118))) {
                continue;
            } else if ((((c >= '0') && (c <= '9')) || (c == '+') || (c == '-') || (c == '.'))
                    && (i > 0)) {
                continue;
            } else {
                if (i == 0) {
                    throw new IllegalArgumentException(
                            "The first character of a scheme must be an alphabetic character or a template variable name begin with '{'. Scheme is \""
                                    + scheme + "\"");
                }
                throw new IllegalArgumentException(
                        "The "
                                + i
                                + ". character of a scheme "
                                + "must be an alphabetic character, a number, a '+', a '-' or a '.'. Template variable names are also allowed. Scheme is \""
                                + scheme + "\"");
            }
        }
        return scheme;
    }

    /**
     * 
     * @param index
     *            index, starting with zero.
     * @param errMessName
     *            the name of the string with illegal characters
     * @param illegalString
     *            the illegal String
     * @param messageEnd
     * @return
     */
    private static IllegalArgumentException throwIllegalArgExc(int index,
            String errMessName, Object illegalString, String messageEnd) {
        final StringBuilder stb = new StringBuilder();
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
        if (index >= 0) {
            stb.append(" (index starting with 0)");
        }
        throw new IllegalArgumentException(stb.toString());
    }

    /**
     * Appends the given character precent-encoded to the {@link StringBuilder}.
     * Example: ' ' -> "%20%
     * 
     * @param toEncode
     * @param stb
     */
    public static void toHex(char toEncode, StringBuilder stb) {
        stb.append('%');
        stb.append(HEX_DIGITS[(toEncode >> 4) & 0xF]);
        stb.append(HEX_DIGITS[toEncode & 0xF]);
    }

    /**
     * if encode is true, the given char is appended as hex string to the
     * {@link StringBuilder}. If encode is false, an
     * {@link IllegalArgumentException} is thrown.
     * 
     * @param c
     * @param stb
     * @param encode
     *            true, if the value should be encoded, or false if not.
     * @throws IllegalArgumentException
     */
    private static void toHexOrReject(char c, StringBuilder stb, boolean encode)
            throws IllegalArgumentException {
        if (encode) {
            stb.append('%');
            stb.append(HEX_DIGITS[(c >> 4) & 0xF]);
            stb.append(HEX_DIGITS[c & 0xF]);
        } else {
            throw new IllegalArgumentException(
                    ("The character " + c + " is not valid"));
        }
    }

    /**
     * @param userInfo
     *            the URI user-info, may contain URI template parameters
     * @param encode
     * @return the encoced character sequence (if it should be encoded)
     * @throws IllegalArgumentException
     *             if automatic encoding is disabled and the userInfo contains
     *             illegal characters, or if the userInfo is null.
     */
    public static CharSequence userInfo(CharSequence userInfo, boolean encode)
            throws IllegalArgumentException {
        final int length = userInfo.length();
        final StringBuilder stb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            final char c = userInfo.charAt(i);
            if (c == '{') {
                i = processTemplVarname(userInfo, i, stb);
            } else if (c == '%') {
                processPercent(i, encode, userInfo, stb);
            } else if (Reference.isUnreserved(c) || Reference.isSubDelimiter(c)) {
                stb.append(c);
            } else if (c == '}') {
                throw new IllegalArgumentException("'}' is only allowed "
                        + "as end of a variable name in \"" + userInfo + "\"");
            } else if (c == ':') {
                stb.append(c);
            } else {
                toHexOrReject(c, stb, encode);
            }
        }
        return stb;
    }
}