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

package org.restlet.engine.http.header;

import static org.restlet.engine.http.header.HeaderUtils.isCarriageReturn;
import static org.restlet.engine.http.header.HeaderUtils.isComma;
import static org.restlet.engine.http.header.HeaderUtils.isCommentText;
import static org.restlet.engine.http.header.HeaderUtils.isDoubleQuote;
import static org.restlet.engine.http.header.HeaderUtils.isLineFeed;
import static org.restlet.engine.http.header.HeaderUtils.isLinearWhiteSpace;
import static org.restlet.engine.http.header.HeaderUtils.isQuoteCharacter;
import static org.restlet.engine.http.header.HeaderUtils.isQuotedText;
import static org.restlet.engine.http.header.HeaderUtils.isSemiColon;
import static org.restlet.engine.http.header.HeaderUtils.isSpace;
import static org.restlet.engine.http.header.HeaderUtils.isTokenChar;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.data.Encoding;
import org.restlet.data.Parameter;
import org.restlet.engine.util.DateUtils;

/**
 * HTTP-style header reader.
 * 
 * @param <V>
 *            The header value target type. There can be multiple values for a
 *            single header.
 * @author Jerome Louvel
 */
public class HeaderReader<V> {
    /**
     * Parses a date string.
     * 
     * @param date
     *            The date string to parse.
     * @param cookie
     *            Indicates if the date is in the cookie format.
     * @return The parsed date.
     */
    public static Date readDate(String date, boolean cookie) {
        if (cookie) {
            return DateUtils.parse(date, DateUtils.FORMAT_RFC_1036);
        }

        return DateUtils.parse(date, DateUtils.FORMAT_RFC_1123);
    }

    /**
     * Read a header. Return null if the last header was already read.
     * 
     * @param header
     *            The header line to parse.
     * @return The header read or null.
     * @throws IOException
     */
    public static Parameter readHeader(CharSequence header) throws IOException {
        Parameter result = null;

        if (header.length() > 0) {
            // Detect the end of headers
            int start = 0;
            int index = 0;
            int next = header.charAt(index++);

            if (isCarriageReturn(next)) {
                next = header.charAt(index++);

                if (!isLineFeed(next)) {
                    throw new IOException(
                            "Invalid end of headers. Line feed missing after the carriage return.");
                }
            } else {
                result = new Parameter();

                // Parse the header name
                while ((index < header.length()) && (next != ':')) {
                    next = header.charAt(index++);
                }

                if (index == header.length()) {
                    throw new IOException(
                            "Unable to parse the header name. End of line reached too early.");
                }

                result.setName(header.subSequence(start, index - 1).toString());
                next = header.charAt(index++);

                while (isSpace(next)) {
                    // Skip any separator space between colon and header value
                    next = header.charAt(index++);
                }

                start = index - 1;

                // Parse the header value
                result.setValue(header.subSequence(start, header.length())
                        .toString());
            }
        }

        return result;
    }

    /**
     * Read a header. Return null if the last header was already read.
     * 
     * @param is
     *            The message input stream.
     * @param sb
     *            The string builder to reuse.
     * @return The header read or null.
     * @throws IOException
     */
    public static Parameter readHeader(InputStream is, StringBuilder sb)
            throws IOException {
        Parameter result = null;

        // Detect the end of headers
        int next = is.read();
        if (isCarriageReturn(next)) {
            next = is.read();
            if (!isLineFeed(next)) {
                throw new IOException(
                        "Invalid end of headers. Line feed missing after the carriage return.");
            }
        } else {
            result = new Parameter();

            // Parse the header name
            while ((next != -1) && (next != ':')) {
                sb.append((char) next);
                next = is.read();
            }

            if (next == -1) {
                throw new IOException(
                        "Unable to parse the header name. End of stream reached too early.");
            }

            result.setName(sb.toString());
            sb.delete(0, sb.length());

            next = is.read();
            while (isSpace(next)) {
                // Skip any separator space between colon and header value
                next = is.read();
            }

            // Parse the header value
            while ((next != -1) && (!isCarriageReturn(next))) {
                sb.append((char) next);
                next = is.read();
            }

            if (next == -1) {
                throw new IOException(
                        "Unable to parse the header value. End of stream reached too early.");
            }
            next = is.read();

            if (isLineFeed(next)) {
                result.setValue(sb.toString());
                sb.delete(0, sb.length());
            } else {
                throw new IOException(
                        "Unable to parse the HTTP header value. The carriage return must be followed by a line feed.");
            }
        }

        return result;
    }

    /** The header to read. */
    private final String header;

    /** The current read index (or -1 if not reading anymore). */
    private volatile int index;

    /** The current mark. */
    private volatile int mark;

    /**
     * Constructor.
     * 
     * @param header
     *            The header to read.
     */
    public HeaderReader(String header) {
        this.header = header;
        this.index = ((header == null) || (header.length() == 0)) ? -1 : 0;
        this.mark = index;
    }

    /**
     * Adds values to the given list.
     * 
     * @param values
     *            The list of values to update.
     */
    public void addValues(Collection<V> values) {
        try {
            // Skip leading spaces
            skipSpaces();

            do {
                // Read the first value
                V nextValue = readValue();
                if (canAdd(nextValue, values)) {
                    // Add the value to the list
                    values.add(nextValue);
                }

                // Attempt to skip the value separator
                skipValueSeparator();
            } while (peek() != -1);
        } catch (IOException ioe) {
            Context.getCurrentLogger().log(Level.INFO,
                    "Unable to read a header", ioe);
        }
    }

    /**
     * Indicates if the value can be added the the list. Useful to prevent the
     * addition of {@link Encoding#IDENTITY} constants for example. By default
     * it returns true for non null values.
     * 
     * @param value
     *            The value to add.
     * 
     * @param values
     *            The target collection.
     * @return True if the value can be added.
     */
    protected boolean canAdd(V value, Collection<V> values) {
        return value != null && !values.contains(value);
    }

    /**
     * Creates a new parameter with a null value. Can be overridden.
     * 
     * @param name
     *            The parameter name.
     * @return The new parameter.
     */
    protected final Parameter createParameter(String name) {
        return createParameter(name, null);
    }

    /**
     * Creates a new parameter. Can be overridden.
     * 
     * @param name
     *            The parameter name.
     * @param value
     *            The parameter value or null.
     * @return The new parameter.
     */
    protected Parameter createParameter(String name, String value) {
        return new Parameter(name, value);
    }

    /**
     * Marks the current position in this reader. A subsequent call to the
     * <code>reset</code> method repositions this reader at the last marked
     * position.
     */
    public void mark() {
        mark = index;
    }

    /**
     * Reads the next character.
     * 
     * @return The next character.
     */
    public int peek() {
        int result = -1;

        if (this.index != -1) {
            result = this.header.charAt(this.index);
        }

        return result;
    }

    /**
     * Reads the next character.
     * 
     * @return The next character.
     */
    public int read() {
        int result = -1;

        if (this.index >= 0) {
            result = this.header.charAt(this.index++);

            if (this.index >= this.header.length()) {
                this.index = -1;
            }
        }

        return result;
    }

    /**
     * Reads the next comment. The first character must be a parenthesis.
     * 
     * @return The next comment.
     * @throws IOException
     */
    public String readComment() throws IOException {
        String result = null;
        int next = read();

        // First character must be a parenthesis
        if (next == '(') {
            StringBuilder buffer = new StringBuilder();

            while (result == null) {
                next = read();

                if (isCommentText(next)) {
                    buffer.append((char) next);
                } else if (isQuoteCharacter(next)) {
                    // Start of a quoted pair (escape sequence)
                    buffer.append((char) read());
                } else if (next == '(') {
                    // Nested comment
                    buffer.append('(').append(readComment()).append(')');
                } else if (next == ')') {
                    // End of comment
                    result = buffer.toString();
                } else if (next == -1) {
                    throw new IOException(
                            "Unexpected end of comment. Please check your value");
                } else {
                    throw new IOException("Invalid character \"" + next
                            + "\" detected in comment. Please check your value");
                }
            }
        } else {
            throw new IOException("A comment must start with a parenthesis");
        }

        return result;
    }

    /**
     * Reads the next digits.
     * 
     * @return The next digits.
     */
    public String readDigits() {
        StringBuilder sb = new StringBuilder();
        int next = read();

        while (isTokenChar(next)) {
            sb.append((char) next);
            next = read();
        }

        // Unread the last character (separator or end marker)
        unread();

        return sb.toString();
    }

    /**
     * Reads the next pair as a parameter.
     * 
     * @return The next pair as a parameter.
     * @throws IOException
     */
    public Parameter readParameter() throws IOException {
        Parameter result = null;
        String name = readToken();
        int nextChar = read();

        if (name.length() > 0) {
            if (nextChar == '=') {
                // The parameter has a value
                result = createParameter(name, readParameterValue());
            } else {
                // The parameter has not value
                unread();
                result = createParameter(name);
            }
        } else {
            throw new IOException(
                    "Parameter or extension has no name. Please check your value");
        }

        return result;
    }

    /**
     * Reads a parameter value which is either a token or a quoted string.
     * 
     * @return A parameter value.
     * @throws IOException
     */
    public String readParameterValue() throws IOException {
        String result = null;

        // Discard any leading space
        skipSpaces();

        // Detect if quoted string or token available
        int nextChar = peek();

        if (isDoubleQuote(nextChar)) {
            result = readQuotedString();
        } else if (isTokenChar(nextChar)) {
            result = readToken();
        }

        return result;
    }

    /**
     * Reads the next quoted string. The first character must be a double quote.
     * 
     * @return The next quoted string.
     * @throws IOException
     */
    public String readQuotedString() throws IOException {
        String result = null;
        int next = read();

        // First character must be a double quote
        if (isDoubleQuote(next)) {
            StringBuilder buffer = new StringBuilder();

            while (result == null) {
                next = read();

                if (isQuotedText(next)) {
                    buffer.append((char) next);
                } else if (isQuoteCharacter(next)) {
                    // Start of a quoted pair (escape sequence)
                    buffer.append((char) read());
                } else if (isDoubleQuote(next)) {
                    // End of quoted string
                    result = buffer.toString();
                } else if (next == -1) {
                    throw new IOException(
                            "Unexpected end of quoted string. Please check your value");
                } else {
                    throw new IOException(
                            "Invalid character \""
                                    + next
                                    + "\" detected in quoted string. Please check your value");
                }
            }
        } else {
            throw new IOException(
                    "A quoted string must start with a double quote");
        }

        return result;
    }

    /**
     * Read the next text until a space separator is reached.
     * 
     * @return The next text.
     */
    public String readRawText() {
        // Read value until end or space
        StringBuilder sb = null;
        int next = read();

        while ((next != -1) && !isSpace(next) && !isComma(next)) {
            if (sb == null) {
                sb = new StringBuilder();
            }

            sb.append((char) next);
            next = read();
        }

        // Unread the separator
        if (isSpace(next) || isComma(next)) {
            unread();
        }

        return (sb == null) ? null : sb.toString();
    }

    /**
     * Read the next header value of a multi-value header. It skips leading and
     * trailing spaces.
     * 
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec2.html#sec2">HTTP
     *      parsing rule</a>
     * 
     * @return The next header value or null.
     */
    public String readRawValue() {
        // Skip leading spaces
        skipSpaces();

        // Read value until end or comma
        StringBuilder sb = null;
        int next = read();

        while ((next != -1) && !isComma(next)) {
            if (sb == null) {
                sb = new StringBuilder();
            }

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

        // Unread the separator
        if (isComma(next)) {
            unread();
        }

        return (sb == null) ? null : sb.toString();
    }

    /**
     * Reads the next token.
     * 
     * @return The next token.
     */
    public String readToken() {
        StringBuilder sb = new StringBuilder();
        int next = read();

        while (isTokenChar(next)) {
            sb.append((char) next);
            next = read();
        }

        // Unread the last character (separator or end marker)
        unread();

        return sb.toString();
    }

    /**
     * Read the next value. There can be multiple values for a single header.
     * 
     * @return The next value.
     */
    public V readValue() throws IOException {
        return null;
    }

    /**
     * Returns a new list with all values added.
     * 
     * @return A new list with all values added.
     */
    public List<V> readValues() {
        List<V> result = new CopyOnWriteArrayList<V>();
        addValues(result);
        return result;
    }

    /**
     * Repositions this stream to the position at the time the <code>mark</code>
     * method was last called on this input stream.
     */
    public void reset() {
        index = mark;
    }

    /**
     * Skips the next parameter separator (semi-colon) including leading and
     * trailing spaces.
     * 
     * @return True if a separator was effectively skipped.
     */
    public boolean skipParameterSeparator() {
        boolean result = false;

        // Skip leading spaces
        skipSpaces();

        // Check if next character is a parameter separator
        if (isSemiColon(read())) {
            result = true;

            // Skip trailing spaces
            skipSpaces();
        } else {
            // Probably reached the end of the header
            unread();
        }

        return result;
    }

    /**
     * Skips the next spaces.
     * 
     * @return True if spaces were skipped.
     */
    public boolean skipSpaces() {
        boolean result = false;
        int next = peek();

        while (isLinearWhiteSpace(next) && (next != -1)) {
            result = result || isLinearWhiteSpace(next);
            read();
            next = peek();
        }

        return result;
    }

    /**
     * Skips the next value separator (comma) including leading and trailing
     * spaces.
     * 
     * @return True if a separator was effectively skipped.
     */
    public boolean skipValueSeparator() {
        boolean result = false;

        // Skip leading spaces
        skipSpaces();

        // Check if next character is a value separator
        if (isComma(read())) {
            result = true;

            // Skip trailing spaces
            skipSpaces();
        } else {
            // Probably reached the end of the header
            unread();
        }

        return result;
    }

    /**
     * Unreads the last character.
     */
    public void unread() {
        if (this.index > 0) {
            this.index--;
        }
    }

}
