/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.engine.header;

import java.io.StringWriter;
import java.util.Collection;

import org.restlet.data.CharacterSet;
import org.restlet.data.Encoding;
import org.restlet.data.Reference;
import org.restlet.util.NamedValue;

/**
 * HTTP-style header writer.
 * 
 * @param <V>
 *            The value type.
 * @author Jerome Louvel
 */
public abstract class HeaderWriter<V> extends StringWriter {

    @Override
    public HeaderWriter<V> append(char c) {
        super.append(c);
        return this;
    }

    /**
     * Appends an array of characters.
     * 
     * @param cs
     *            The array of characters.
     * @return This writer.
     */
    public HeaderWriter<V> append(char[] cs) {
        if (cs != null) {
            for (char c : cs) {
                append(c);
            }
        }

        return this;
    }

    @Override
    public HeaderWriter<V> append(CharSequence csq) {
        super.append(csq);
        return this;
    }

    /**
     * Appends a collection of values.
     * 
     * @param values
     *            The collection of values to append.
     * @return This writer.
     */
    public HeaderWriter<V> append(Collection<V> values) {
        if ((values != null) && !values.isEmpty()) {
            boolean first = true;

            for (V value : values) {
                if (canWrite(value)) {
                    if (first) {
                        first = false;
                    } else {
                        appendValueSeparator();
                    }

                    append(value);
                }
            }
        }

        return this;
    }

    /**
     * Appends an integer.
     * 
     * @param i
     *            The value to append.
     * @return This writer.
     */
    public HeaderWriter<V> append(int i) {
        return append(Integer.toString(i));
    }

    /**
     * Appends a long.
     * 
     * @param l
     *            The value to append.
     * @return This writer.
     */
    public HeaderWriter<V> append(long l) {
        return append(Long.toString(l));
    }

    /**
     * Appends a value.
     * 
     * @param value
     *            The value.
     * @return This writer.
     */
    public abstract HeaderWriter<V> append(V value);

    /**
     * Appends a string as an HTTP comment, surrounded by parenthesis and with
     * quoted pairs if needed.
     * 
     * @param content
     *            The comment to write.
     * @return This writer.
     */
    public HeaderWriter<V> appendComment(String content) {
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
     * @return This writer.
     */
    public HeaderWriter<V> appendExtension(NamedValue<String> extension) {
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
     * @param value
     *            The extension value.
     * @return This writer.
     */
    public HeaderWriter<V> appendExtension(String name, String value) {
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
     * Appends a semicolon as a parameter separator.
     * 
     * @return This writer.
     */
    public HeaderWriter<V> appendParameterSeparator() {
        return append(";");
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
    public HeaderWriter<V> appendProduct(String name, String version) {
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
     * @return This writer.
     */
    public HeaderWriter<V> appendQuotedPair(char character) {
        return append('\\').append(character);
    }

    /**
     * Appends a quoted string.
     * 
     * @param content
     *            The string to quote and write.
     * @return This writer.
     */
    public HeaderWriter<V> appendQuotedString(String content) {
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
     * @return This writer.
     */
    public HeaderWriter<V> appendSpace() {
        return append(' ');
    }

    /**
     * Appends a token.
     * 
     * @param token
     *            The token to write.
     * @return This writer.
     */
    public HeaderWriter<V> appendToken(String token) {
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
    public HeaderWriter<V> appendUriEncoded(CharSequence source,
            CharacterSet characterSet) {
        return append(Reference.encode(source.toString(), characterSet));
    }

    /**
     * Appends a comma as a value separator.
     * 
     * @return This writer.
     */
    public HeaderWriter<V> appendValueSeparator() {
        return append(", ");
    }

    /**
     * Indicates if the value can be written to the header. Useful to prevent
     * the writing of {@link Encoding#IDENTITY} constants for example. By
     * default it returns true for non null values.
     * 
     * @param value
     *            The value to add.
     * @return True if the value can be added.
     */
    protected boolean canWrite(V value) {
        return (value != null);
    }

}
