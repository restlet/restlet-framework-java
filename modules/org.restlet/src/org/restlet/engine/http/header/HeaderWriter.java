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
 * HTTP-style header builder. It builds an internal buffer that can be retrieved
 * at the end via the {@link #toString()} method.
 * 
 * @author Jerome Louvel
 */
public class HeaderWriter implements Appendable {
    /** The header buffer. */
    private final StringBuilder wrappedBuilder;

    /** Indicates if the first parameter is written. */
    private volatile boolean firstParameter;

    /**
     * Constructor.
     */
    public HeaderWriter() {
        this.firstParameter = true;
        this.wrappedBuilder = new StringBuilder();
    }

    /**
     * Appends a character.
     * 
     * @param c
     *            The character to append.
     * @return The current builder.
     */
    public HeaderWriter append(char c) {
        this.wrappedBuilder.append(c);
        return this;
    }

    /**
     * Appends a sequence of characters.
     * 
     * @param csq
     *            The sequence of characters.
     * @return The current builder.
     */
    public HeaderWriter append(CharSequence csq) {
        this.wrappedBuilder.append(csq);
        return this;
    }

    /**
     * Appends a sequence of characters.
     * 
     * @param csq
     *            The sequence to add.
     * @param start
     *            The start index.
     * @param end
     *            The end index.
     * @return The current builder.
     */
    public HeaderWriter append(CharSequence csq, int start, int end) {
        this.wrappedBuilder.append(csq, start, end);
        return this;
    }

    /**
     * Appends a string as an HTTP comment, surrounded by parenthesis and with
     * properly quote content if needed.
     * 
     * @param content
     *            The comment to write.
     * @return The current builder.
     * @throws IOException
     */
    public HeaderWriter appendComment(String content) throws IOException {
        this.wrappedBuilder.append('(');

        char c;
        for (int i = 0; i < content.length(); i++) {
            c = content.charAt(i);

            if (HeaderUtils.isCommentText(c)) {
                this.wrappedBuilder.append(c);
            } else {
                appendQuotedPair(c);
            }
        }

        this.wrappedBuilder.append(')');
        return this;
    }

    /**
     * Appends a new parameter, prefixed with a comma. The value is separated
     * from the name by an '=' character.
     * 
     * @param parameter
     *            The parameter.
     * @return The current builder.
     * @throws IOException
     */
    public HeaderWriter appendParameter(Parameter parameter)
            throws IOException {
        return appendParameter(parameter.getName(), parameter.getValue());
    }

    /**
     * Appends a new parameter, prefixed with a comma.
     * 
     * @param name
     *            The parameter name.
     * @return The current builder.
     * @throws IOException
     */
    public HeaderWriter appendParameter(String name) throws IOException {
        appendParameterSeparator();
        return appendToken(name);
    }

    /**
     * Appends a new parameter, prefixed with a comma. The value is separated
     * from the name by an '=' character.
     * 
     * @param name
     *            The parameter name.
     * @param value
     *            The parameter value.
     * @return The current builder.
     * @throws IOException
     */
    public HeaderWriter appendParameter(String name, String value)
            throws IOException {
        appendParameterSeparator();

        if (name != null) {
            appendToken(name);
        }

        if (value != null) {
            this.wrappedBuilder.append('=');
            appendToken(value);
        }

        return this;
    }

    /**
     * Appends a comma as a separator if the first parameter has already been
     * written.
     * 
     * @return The current builder.
     * @throws IOException
     */
    public HeaderWriter appendParameterSeparator() throws IOException {
        if (isFirstParameter()) {
            setFirstParameter(false);
        } else {
            this.wrappedBuilder.append(", ");
        }

        return this;
    }

    /**
     * Appends a quoted character, prefixing it with a backslash.
     * 
     * @param character
     *            The character to quote.
     * @return The current builder.
     */
    protected HeaderWriter appendQuotedPair(char character) {
        this.wrappedBuilder.append('\\').append(character);
        return this;
    }

    /**
     * Appends a new parameter, prefixed with a comma. The value is separated
     * from the name by an '=' character.
     * 
     * @param parameter
     *            The parameter.
     * @throws IOException
     * @return The current builder.
     */
    public HeaderWriter appendQuotedParameter(Parameter parameter)
            throws IOException {
        return appendQuotedParameter(parameter.getName(), parameter.getValue());
    }

    /**
     * Appends a new parameter, prefixed with a comma. The value is quoted and
     * separated from the name by an '=' character.
     * 
     * @param name
     *            The parameter name.
     * @param value
     *            The parameter value to quote.
     * @throws IOException
     * @return The current builder.
     */
    public HeaderWriter appendQuotedParameter(String name, String value)
            throws IOException {
        appendParameterSeparator();

        if (name != null) {
            appendToken(name);
        }

        if (value != null) {
            this.wrappedBuilder.append('=');
            appendQuotedString(value);
        }

        return this;
    }

    /**
     * Appends a quoted string.
     * 
     * @param content
     *            The string to quote and write.
     * @return The current builder.
     */
    public HeaderWriter appendQuotedString(String content) {
        this.wrappedBuilder.append('"');

        char c;
        for (int i = 0; i < content.length(); i++) {
            c = content.charAt(i);

            if (HeaderUtils.isQuotedText(c)) {
                this.wrappedBuilder.append(c);
            } else {
                appendQuotedPair(c);
            }
        }

        this.wrappedBuilder.append('"');
        return this;
    }

    /**
     * Appends a space character.
     * 
     * @return The current builder.
     */
    public HeaderWriter appendSpace() {
        this.wrappedBuilder.append(' ');
        return this;
    }

    /**
     * Appends a token.
     * 
     * @param token
     *            The token to write.
     * @return The current builder.
     * @throws IOException
     */
    public HeaderWriter appendToken(String token) throws IOException {
        if (HeaderUtils.isToken(token)) {
            this.wrappedBuilder.append(token);
        } else {
            throw new IOException("Unexpected character found in token: "
                    + token);
        }

        return this;
    }

    /**
     * Returns the wrapped string builder.
     * 
     * @return The wrapped string builder.
     */
    public StringBuilder getWrappedBuilder() {
        return wrappedBuilder;
    }

    /**
     * Indicates if the first parameter is written.
     * 
     * @return True if the first parameter is written.
     */
    public boolean isFirstParameter() {
        return firstParameter;
    }

    /**
     * Indicates if the first parameter is written.
     * 
     * @param firstParameter
     *            True if the first parameter is written.
     */
    public void setFirstParameter(boolean firstParameter) {
        this.firstParameter = firstParameter;
    }

    /**
     * Returns the header value built.
     * 
     * @return The header value built.
     */
    public String toString() {
        return this.wrappedBuilder.toString();
    }

}
