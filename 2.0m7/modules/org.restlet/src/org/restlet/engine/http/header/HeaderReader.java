/**
 * Copyright 2005-2010 Noelios Technologies.
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

package org.restlet.engine.http.header;

import java.io.IOException;

import org.restlet.data.Parameter;

/**
 * HTTP-style header reader.
 * 
 * @author Jerome Louvel
 */
public class HeaderReader {
    /** The header to read. */
    private final String header;

    /** The current read index (or -1 if not reading anymore). */
    private volatile int index;

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
     * Indicates if the given character is a value separator.
     * 
     * @param character
     *            The character to test.
     * @return True if the given character is a value separator.
     * @see HeaderUtils#isValueSeparator(int)
     */
    public boolean isValueSeparator(int character) {
        return HeaderUtils.isValueSeparator(character);
    }

    /**
     * Reads the next character.
     * 
     * @return The next character.
     */
    public int read() {
        int result = -1;

        if (this.index != -1) {
            result = this.header.charAt(this.index++);
            if (this.index >= this.header.length()) {
                this.index = -1;
            }
        }

        return result;
    }

    /**
     * Reads the next pair as a parameter.
     * 
     * @return The next pair as a parameter.
     * @throws IOException
     */
    public Parameter readParameter() throws IOException {
        Parameter result = null;

        boolean readingName = true;
        boolean readingValue = false;
        StringBuilder nameBuffer = new StringBuilder();
        StringBuilder valueBuffer = new StringBuilder();
        int nextChar = 0;

        while ((result == null) && (nextChar != -1)) {
            nextChar = read();

            if (readingName) {
                if ((HeaderUtils.isSpace(nextChar)) && (nameBuffer.length() == 0)) {
                    // Skip spaces
                } else if ((nextChar == -1) || (nextChar == ',')) {
                    if (nameBuffer.length() > 0) {
                        // End of pair with no value
                        result = Parameter.create(nameBuffer, null);
                    } else if (nextChar == -1) {
                        // Do nothing return null preference
                    } else {
                        throw new IOException(
                                "Empty parameter name detected. Please check your HTTP header");
                    }
                } else if (nextChar == '=') {
                    readingName = false;
                    readingValue = true;
                } else if (HeaderUtils.isTokenChar(nextChar)) {
                    nameBuffer.append((char) nextChar);
                } else {
                    throw new IOException(
                            "Separator and control characters are not allowed within a token. Please check your HTTP header");
                }
            } else if (readingValue) {
                if ((HeaderUtils.isSpace(nextChar))
                        && (valueBuffer.length() == 0)) {
                    // Skip spaces
                } else if ((nextChar == -1) || (nextChar == ',')) {
                    // End of pair
                    result = Parameter.create(nameBuffer, valueBuffer);
                } else if ((nextChar == '"') && (valueBuffer.length() == 0)) {
                    valueBuffer.append(readQuotedString());
                } else if (HeaderUtils.isTokenChar(nextChar)) {
                    valueBuffer.append((char) nextChar);
                } else {
                    throw new IOException(
                            "Separator and control characters are not allowed within a token. Please check your HTTP header");
                }
            }
        }

        return result;
    }

    /**
     * Reads the next quoted string.
     * 
     * @return The next quoted string.
     * @throws IOException
     */
    public String readQuotedString() throws IOException {
        final StringBuilder sb = new StringBuilder();
        readQuotedString(sb);
        return sb.toString();
    }

    /**
     * Appends the next quoted string.
     * 
     * @param buffer
     *            The buffer to append.
     * @throws IOException
     */
    public void readQuotedString(Appendable buffer) throws IOException {
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

    /**
     * Reads the next token.
     * 
     * @return The next token or null.
     */
    public String readToken() {
        StringBuilder sb = null;
        int next = read();

        // Skip leading spaces
        while ((next != -1) && HeaderUtils.isLinearWhiteSpace(next)) {
            next = read();
        }

        while ((next != -1) && HeaderUtils.isTokenChar(next)) {
            if (sb == null) {
                sb = new StringBuilder();
            }

            sb.append((char) next);
            next = read();
        }

        return (sb == null) ? null : sb.toString();
    }

    /**
     * Read the next value of a multi-value header. It skips separator commas
     * and spaces.
     * 
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec2.html#sec2">HTTP
     *      parsing rule</a>
     * 
     * @return The next value or null.
     */
    public String readValue() {
        StringBuilder sb = null;
        int next = read();

        // Skip leading spaces
        while ((next != -1) && HeaderUtils.isLinearWhiteSpace(next)) {
            next = read();
        }

        while ((next != -1) && !isValueSeparator(next)) {
            if (sb == null) {
                sb = new StringBuilder();
            }

            sb.append((char) next);
            next = read();
        }

        // Remove trailing spaces
        if (sb != null) {
            for (int i = sb.length() - 1; (i >= 0)
                    && HeaderUtils.isLinearWhiteSpace(sb.charAt(i)); i--) {
                sb.deleteCharAt(i);
            }
        }

        return (sb == null) ? null : sb.toString();
    }

}
