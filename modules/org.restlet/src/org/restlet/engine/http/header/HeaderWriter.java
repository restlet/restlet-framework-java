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

import java.io.StringWriter;
import java.util.Date;

import org.restlet.data.CharacterSet;
import org.restlet.data.Parameter;
import org.restlet.data.Reference;
import org.restlet.engine.util.DateUtils;

/**
 * HTTP-style header writer.
 * 
 * @author Jerome Louvel
 */
public class HeaderWriter extends StringWriter {

    /**
     * Formats a date as a header string.
     * 
     * @param date
     *            The date to format.
     * @param cookie
     *            Indicates if the date should be in the cookie format.
     * @return The formatted date.
     */
    public static String write(Date date, boolean cookie) {
        if (cookie) {
            return DateUtils.format(date, DateUtils.FORMAT_RFC_1036.get(0));
        }

        return DateUtils.format(date, DateUtils.FORMAT_RFC_1123.get(0));
    }

    @Override
    public HeaderWriter append(char c) {
        super.append(c);
        return this;
    }

    @Override
    public HeaderWriter append(CharSequence csq) {
        super.append(csq);
        return this;
    }

    /**
     * Appends an integer.
     * 
     * @param i
     *            The value to append.
     * @return This writer.
     */
    public HeaderWriter append(int i) {
        return append(Integer.toString(i));
    }

    /**
     * Appends a long.
     * 
     * @param l
     *            The value to append.
     * @return This writer.
     */
    public HeaderWriter append(long l) {
        return append(Long.toString(l));
    }

    /**
     * Appends a string as an HTTP comment, surrounded by parenthesis and with
     * quoted pairs if needed.
     * 
     * @param content
     *            The comment to write.
     * @return The current builder.
     */
    public HeaderWriter appendComment(String content) {
        append('(');
        char c;

        for (int i = 0; i < content.length(); i++) {
            c = content.charAt(i);

            if (HeaderUtils.isCommentText(c)) {
                append(c);
            } else {
                appendQuotedPair(c);
            }
        }

        return append(')');
    }

    /**
     * Formats and appends a parameter as an extension. If the value is not a
     * token, then it is quoted.
     * 
     * @param extension
     *            The parameter to format as an extension.
     * @param destination
     *            The appendable destination.
     * @return This writer.
     */
    public HeaderWriter appendExtension(Parameter extension) {
        if (extension != null) {
            return appendExtension(extension.getName(), extension.getValue());
        } else {
            return this;
        }
    }

    /**
     * Appends an extension. If the value is not a token, then it is quoted.
     * 
     * @param name
     *            The extension name.
     * @param destination
     *            The extension value.
     * @return This writer.
     */
    public HeaderWriter appendExtension(String name, String value) {
        if ((name != null) && (name.length() > 0)) {
            append(name);

            if ((value != null) && (value.length() > 0)) {
                append("=");

                if (HeaderUtils.isToken(value)) {
                    append(value);
                } else {
                    appendQuotedString(value);
                }
            }
        }

        return this;
    }

    /**
     * Appends a product description.
     * 
     * @param name
     *            The product name token.
     * @param version
     *            The product version token.
     * @return This writer.
     */
    public HeaderWriter appendProduct(String name, String version) {
        appendToken(name);

        if (version != null) {
            append('/').appendToken(version);
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
    public HeaderWriter appendQuotedPair(char character) {
        return append('\\').append(character);
    }

    /**
     * Appends a quoted string.
     * 
     * @param content
     *            The string to quote and write.
     * @return The current builder.
     */
    public HeaderWriter appendQuotedString(String content) {
        if ((content != null) && (content.length() > 0)) {
            append('"');
            char c;

            for (int i = 0; i < content.length(); i++) {
                c = content.charAt(i);

                if (HeaderUtils.isQuotedText(c)) {
                    append(c);
                } else {
                    appendQuotedPair(c);
                }
            }

            append('"');
        }

        return this;
    }

    /**
     * Appends a space character.
     * 
     * @return The current builder.
     */
    public HeaderWriter appendSpace() {
        return append(' ');
    }

    /**
     * Appends a token.
     * 
     * @param token
     *            The token to write.
     * @return The current builder.
     */
    public HeaderWriter appendToken(String token) {
        if (HeaderUtils.isToken(token)) {
            return append(token);
        } else {
            throw new IllegalArgumentException(
                    "Unexpected character found in token: " + token);
        }
    }

    /**
     * Formats and appends a source string as an URI encoded string.
     * 
     * @param source
     *            The source string to format.
     * @param characterSet
     *            The supported character encoding.
     * @return This writer.
     */
    public HeaderWriter appendUriEncoded(CharSequence source,
            CharacterSet characterSet) {
        return append(Reference.encode(source.toString(), characterSet));
    }

}
