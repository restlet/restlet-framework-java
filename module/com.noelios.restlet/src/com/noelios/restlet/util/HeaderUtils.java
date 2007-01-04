/*
 * Copyright 2005-2007 Noelios Consulting.
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

package com.noelios.restlet.util;

import java.io.IOException;

import org.restlet.data.CharacterSet;
import org.restlet.data.Parameter;
import org.restlet.data.Reference;

/**
 * HTTP-style header manipulation utilities.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class HeaderUtils {
    /**
     * Appends a source string as an HTTP comment.
     * 
     * @param source
     *            The source string to format.
     * @param destination
     *            The appendable destination.
     * @throws IOException
     */
    public Appendable appendComment(CharSequence source, Appendable destination)
            throws IOException {
        destination.append('(');

        char c;
        for (int i = 0; i < source.length(); i++) {
            c = source.charAt(i);

            if (c == '(') {
                destination.append("\\(");
            } else if (c == ')') {
                destination.append("\\)");
            } else if (c == '\\') {
                destination.append("\\\\");
            } else {
                destination.append(c);
            }
        }

        destination.append(')');
        return destination;
    }

    /**
     * Creates a parameter.
     * 
     * @param name
     *            The parameter name buffer.
     * @param value
     *            The parameter value buffer (can be null).
     * @return The created parameter.
     * @throws IOException
     */
    public static Parameter createParameter(CharSequence name,
            CharSequence value) throws IOException {
        if (value != null) {
            return new Parameter(name.toString(), value.toString());
        } else {
            return new Parameter(name.toString(), null);
        }
    }

    /**
     * Appends a source string as an HTTP quoted string.
     * 
     * @param source
     *            The unquoted source string.
     * @param destination
     *            The destination to append to.
     * @throws IOException
     */
    public static Appendable appendQuote(CharSequence source,
            Appendable destination) throws IOException {
        destination.append('"');

        char c;
        for (int i = 0; i < source.length(); i++) {
            c = source.charAt(i);

            if (c == '"') {
                destination.append("\\\"");
            } else if (c == '\\') {
                destination.append("\\\\");
            } else {
                destination.append(c);
            }
        }

        destination.append('"');
        return destination;
    }

    /**
     * Appends a source string as an URI encoded string.
     * 
     * @param source
     *            The source string to format.
     * @param destination
     *            The appendable destination.
     * @throws IOException
     * @deprecated Use the
     *             appendUriEncoded(CharSequence,Appendable,CharacterSet) method
     *             to specify the encoding. This method uses the UTF-8 character
     *             set.
     */
    @Deprecated
    public static Appendable appendUriEncoded(CharSequence source,
            Appendable destination) throws IOException {
        return appendUriEncoded(source, destination, CharacterSet.UTF_8);
    }

    /**
     * Appends a source string as an URI encoded string.
     * 
     * @param source
     *            The source string to format.
     * @param destination
     *            The appendable destination.
     * @param characterSet
     *            The supported character encoding.
     * @throws IOException
     */
    public static Appendable appendUriEncoded(CharSequence source,
            Appendable destination, CharacterSet characterSet)
            throws IOException {
        destination.append(Reference.encode(source.toString(), characterSet));
        return destination;
    }

    /**
     * Formats a product description.
     * 
     * @param nameToken
     *            The product name token.
     * @param versionToken
     *            The product version token.
     * @param destination
     *            The appendable destination;
     * @throws IOException
     */
    public static void formatProduct(CharSequence nameToken,
            CharSequence versionToken, Appendable destination)
            throws IOException {
        if (!isToken(nameToken)) {
            throw new IllegalArgumentException(
                    "Invalid product name detected. Only token characters are allowed.");
        } else {
            destination.append(nameToken);

            if (versionToken != null) {
                if (!isToken(versionToken)) {
                    throw new IllegalArgumentException(
                            "Invalid product version detected. Only token characters are allowed.");
                } else {
                    destination.append('/').append(versionToken);
                }
            }
        }
    }

    /**
     * Indicates if the given character is in ASCII range.
     * 
     * @param character
     *            The character to test.
     * @return True if the given character is in ASCII range.
     */
    public static boolean isAsciiChar(int character) {
        return (character >= 0) && (character <= 127);
    }

    /**
     * Indicates if the given character is upper case (A-Z).
     * 
     * @param character
     *            The character to test.
     * @return True if the given character is upper case (A-Z).
     */
    public static boolean isUpperCase(int character) {
        return (character >= 'A') && (character <= 'Z');
    }

    /**
     * Indicates if the given character is lower case (a-z).
     * 
     * @param character
     *            The character to test.
     * @return True if the given character is lower case (a-z).
     */
    public static boolean isLowerCase(int character) {
        return (character >= 'a') && (character <= 'z');
    }

    /**
     * Indicates if the given character is alphabetical (a-z or A-Z).
     * 
     * @param character
     *            The character to test.
     * @return True if the given character is alphabetical (a-z or A-Z).
     */
    public static boolean isAlpha(int character) {
        return isUpperCase(character) || isLowerCase(character);
    }

    /**
     * Indicates if the given character is a digit (0-9).
     * 
     * @param character
     *            The character to test.
     * @return True if the given character is a digit (0-9).
     */
    public static boolean isDigit(int character) {
        return (character >= '0') && (character <= '9');
    }

    /**
     * Indicates if the given character is a control character.
     * 
     * @param character
     *            The character to test.
     * @return True if the given character is a control character.
     */
    public static boolean isControlChar(int character) {
        return ((character >= 0) && (character <= 31)) || (character == 127);
    }

    /**
     * Indicates if the given character is a carriage return.
     * 
     * @param character
     *            The character to test.
     * @return True if the given character is a carriage return.
     */
    public static boolean isCarriageReturn(int character) {
        return (character == 13);
    }

    /**
     * Indicates if the given character is a line feed.
     * 
     * @param character
     *            The character to test.
     * @return True if the given character is a line feed.
     */
    public static boolean isLineFeed(int character) {
        return (character == 10);
    }

    /**
     * Indicates if the given character is a space.
     * 
     * @param character
     *            The character to test.
     * @return True if the given character is a space.
     */
    public static boolean isSpace(int character) {
        return (character == 32);
    }

    /**
     * Indicates if the given character is an horizontal tab.
     * 
     * @param character
     *            The character to test.
     * @return True if the given character is an horizontal tab.
     */
    public static boolean isHorizontalTab(int character) {
        return (character == 9);
    }

    /**
     * Indicates if the given character is a double quote.
     * 
     * @param character
     *            The character to test.
     * @return True if the given character is a double quote.
     */
    public static boolean isDoubleQuote(int character) {
        return (character == 34);
    }

    /**
     * Indicates if the given character is textual (ASCII and not a control
     * character).
     * 
     * @param character
     *            The character to test.
     * @return True if the given character is textual (ASCII and not a control
     *         character).
     */
    public static boolean isText(int character) {
        return isAsciiChar(character) && !isControlChar(character);
    }

    /**
     * Indicates if the given character is a separator.
     * 
     * @param character
     *            The character to test.
     * @return True if the given character is a separator.
     */
    public static boolean isSeparator(int character) {
        switch (character) {
        case '(':
        case ')':
        case '<':
        case '>':
        case '@':
        case ',':
        case ';':
        case ':':
        case '\\':
        case '"':
        case '/':
        case '[':
        case ']':
        case '?':
        case '=':
        case '{':
        case '}':
        case ' ':
        case '\t':
            return true;

        default:
            return false;
        }
    }

    /**
     * Indicates if the given character is a token character (text and not a
     * separator).
     * 
     * @param character
     *            The character to test.
     * @return True if the given character is a token character (text and not a
     *         separator).
     */
    public static boolean isTokenChar(int character) {
        return isText(character) && !isSeparator(character);
    }

    /**
     * Indicates if the token is valid.<br/> Only contains valid token
     * characters.
     * 
     * @param token
     *            The token to check
     * @return True if the token is valid.
     */
    public static boolean isToken(CharSequence token) {
        for (int i = 0; i < token.length(); i++) {
            if (!isTokenChar(token.charAt(i)))
                return false;
        }

        return true;
    }
}
