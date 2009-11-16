/**
 * Copyright 2005-2009 Noelios Technologies.
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

package org.restlet.engine.http;

import java.io.IOException;

import org.restlet.data.Parameter;

/**
 * HTTP-style header builder. It builds an internal buffer that can be retrieved
 * at the end via the {@link #toString()} method.
 * 
 * @author Jerome Louvel
 */
public class HeaderBuilder implements Appendable {
    /** The header buffer. */
    private final StringBuilder header;

    /**
     * Constructor.
     */
    public HeaderBuilder() {
        this.header = new StringBuilder();
    }

    /**
     * Appends a character.
     * 
     * @param c
     *            The character to append.
     * @return The current builder.
     */
    public HeaderBuilder append(char c) throws IOException {
        this.header.append(c);
        return this;
    }

    /**
     * Appends a sequence of characters.
     * 
     * @param csq
     *            The sequence of characters.
     * @return The current builder.
     */
    public HeaderBuilder append(CharSequence csq) throws IOException {
        this.header.append(csq);
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
    public HeaderBuilder append(CharSequence csq, int start, int end)
            throws IOException {
        this.header.append(csq, start, end);
        return this;
    }

    /**
     * Appends a string as an HTTP comment, surrounded by parenthesis and with
     * properly quote content if needed.
     * 
     * @param content
     *            The comment to write.
     * @throws IOException
     */
    public HeaderBuilder appendComment(String content) throws IOException {
        this.header.append('(');

        char c;
        for (int i = 0; i < content.length(); i++) {
            c = content.charAt(i);

            if (HttpUtils.isCommentText(c)) {
                this.header.append(c);
            } else {
                appendQuotedPair(c);
            }
        }

        this.header.append(')');
        return this;
    }

    /**
     * Appends a new parameter, prefixed with a comma. The value is separated
     * from the name by an '=' character.
     * 
     * @param parameter
     *            The parameter.
     * @throws IOException
     */
    public HeaderBuilder appendFirstParameter(Parameter parameter)
            throws IOException {
        return appendFirstParameter(parameter.getName(), parameter.getValue());
    }

    /**
     * Appends a new parameter, prefixed with a comma.
     * 
     * @param name
     *            The parameter name.
     * @throws IOException
     */
    public HeaderBuilder appendFirstParameter(String name) throws IOException {
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
     * @throws IOException
     */
    public HeaderBuilder appendFirstParameter(String name, String value)
            throws IOException {
        if (name != null) {
            appendToken(name);
        }

        if (value != null) {
            this.header.append('=');
            appendToken(value);
        }

        return this;
    }

    /**
     * Appends a new parameter, prefixed with a comma. The value is separated
     * from the name by an '=' character.
     * 
     * @param parameter
     *            The parameter.
     * @throws IOException
     */
    public HeaderBuilder appendFirstQuotedParameter(Parameter parameter)
            throws IOException {
        return appendFirstQuotedParameter(parameter.getName(), parameter
                .getValue());
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
     */
    public HeaderBuilder appendFirstQuotedParameter(String name, String value)
            throws IOException {
        if (name != null) {
            appendToken(name);
        }

        if (value != null) {
            this.header.append('=');
            appendQuotedString(value);
        }

        return this;
    }

    /**
     * Appends a new parameter, prefixed with a comma. The value is separated
     * from the name by an '=' character.
     * 
     * @param parameter
     *            The parameter.
     * @throws IOException
     */
    public HeaderBuilder appendParameter(Parameter parameter)
            throws IOException {
        return appendParameter(parameter.getName(), parameter.getValue());
    }

    /**
     * Appends a new parameter, prefixed with a comma.
     * 
     * @param name
     *            The parameter name.
     * @throws IOException
     */
    public HeaderBuilder appendParameter(String name) throws IOException {
        this.header.append(',');
        return appendParameter(name);
    }

    /**
     * Appends a new parameter, prefixed with a comma. The value is separated
     * from the name by an '=' character.
     * 
     * @param name
     *            The parameter name.
     * @param value
     *            The parameter value.
     * @throws IOException
     */
    public HeaderBuilder appendParameter(String name, String value)
            throws IOException {
        this.header.append(',');
        return appendFirstParameter(name, value);
    }

    /**
     * Appends a quoted character, prefixing it with a backslash.
     * 
     * @param character
     *            The character to quote.
     */
    protected HeaderBuilder appendQuotedPair(char character) {
        this.header.append('\\').append(character);
        return this;
    }

    /**
     * Appends a new parameter, prefixed with a comma. The value is separated
     * from the name by an '=' character.
     * 
     * @param parameter
     *            The parameter.
     * @throws IOException
     */
    public HeaderBuilder appendQuotedParameter(Parameter parameter)
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
     */
    public HeaderBuilder appendQuotedParameter(String name, String value)
            throws IOException {
        this.header.append(',');
        return appendFirstQuotedParameter(name, value);
    }

    /**
     * Appends a quoted string.
     * 
     * @param content
     *            The string to quote and write.
     */
    public HeaderBuilder appendQuotedString(String content) {
        this.header.append('"');

        char c;
        for (int i = 0; i < content.length(); i++) {
            c = content.charAt(i);

            if (HttpUtils.isQuotedText(c)) {
                this.header.append(c);
            } else {
                appendQuotedPair(c);
            }
        }

        this.header.append('"');
        return this;
    }

    /**
     * Appends a space character.
     */
    public HeaderBuilder appendSpace() {
        this.header.append(' ');
        return this;
    }

    /**
     * Appends a token.
     * 
     * @param token
     *            The token to write.
     * @throws IOException
     */
    public HeaderBuilder appendToken(String token) throws IOException {
        if (HttpUtils.isToken(token)) {
            this.header.append(token);
        } else {
            throw new IOException("Unexpected character found in token: "
                    + token);
        }

        return this;
    }

    /**
     * Returns the header value built.
     * 
     * @return The header value built.
     */
    public String toString() {
        return this.header.toString();
    }

}
