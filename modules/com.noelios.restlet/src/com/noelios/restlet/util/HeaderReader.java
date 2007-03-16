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

import com.noelios.restlet.http.HeaderUtils;

/**
 * HTTP-style header reader.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class HeaderReader {
    /** The header to read. */
    private String header;

    /** The current read index (or -1 if not reading anymore). */
    private int index;

    /**
     * Constructor.
     * 
     * @param header
     *            The header to read.
     */
    public HeaderReader(String header) {
        this.header = header;
        this.index = ((header == null) || (header.length() == 0)) ? -1 : 0;
    }

    /**
     * Reads the next character.
     * 
     * @return The next character.
     */
    public int read() {
        int result = -1;

        if (index != -1) {
            result = this.header.charAt(index++);
            if (index >= this.header.length())
                index = -1;
        }

        return result;
    }

    /**
     * Read the next value of a multi-value header. It skips separator commas
     * and spaces.
     * 
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec2.html#sec2">HTTP
     *      parsing rule</a>
     * @return The next value or null.
     */
    public String readValue() {
        StringBuilder sb = null;
        int next = read();

        // Skip leading spaces
        while ((next != -1) && isLinearWhiteSpace(next)) {
            next = read();
        }

        while ((next != -1) && !isValueSeparator(next)) {
            if (sb == null)
                sb = new StringBuilder();
            sb.append((char) next);
            next = read();
        }

        // Remove trailing spaces
        if (sb != null) {
            for (int i = sb.length() - 1; (i >= 0)
                    && isLinearWhiteSpace(sb.charAt(i)); i--) {
                sb.deleteCharAt(i);
            }
        }

        return (sb == null) ? null : sb.toString();
    }

    /**
     * Indicates if the given character is a value separator.
     * 
     * @param character
     *            The character to test.
     * @return True if the given character is a value separator.
     */
    protected boolean isValueSeparator(int character) {
        return (character == ',');
    }

    /**
     * Indicates if the given character is a value separator.
     * 
     * @param character
     *            The character to test.
     * @return True if the given character is a value separator.
     */
    protected boolean isLinearWhiteSpace(int character) {
        return (HeaderUtils.isCarriageReturn(character)
                || HeaderUtils.isSpace(character)
                || HeaderUtils.isLineFeed(character) || HeaderUtils
                .isHorizontalTab(character));
    }

    /**
     * Reads the next quoted string.
     * 
     * @return The next quoted string.
     * @throws IOException
     */
    protected String readQuotedString() throws IOException {
        StringBuilder sb = new StringBuilder();
        appendQuotedString(sb);
        return sb.toString();
    }

    /**
     * Appends the next quoted string.
     * 
     * @param buffer
     *            The buffer to append.
     * @throws IOException
     */
    protected void appendQuotedString(Appendable buffer) throws IOException {
        boolean done = false;
        boolean quotedPair = false;
        int nextChar = 0;

        while ((!done) && (nextChar != -1)) {
            nextChar = read();

            if (quotedPair) {
                // End of quoted pair (escape sequence)
                if (HeaderUtils.isText(nextChar)) {
                    buffer.append((char) nextChar);
                    quotedPair = false;
                } else {
                    throw new IOException(
                            "Invalid character detected in quoted string. Please check your value");
                }
            } else if (HeaderUtils.isDoubleQuote(nextChar)) {
                // End of quoted string
                done = true;
            } else if (nextChar == '\\') {
                // Begin of quoted pair (escape sequence)
                quotedPair = true;
            } else if (HeaderUtils.isText(nextChar)) {
                buffer.append((char) nextChar);
            } else {
                throw new IOException(
                        "Invalid character detected in quoted string. Please check your value");
            }
        }
    }

}
