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

package org.restlet.representation;

import java.io.IOException;

import org.restlet.data.CharacterSet;
import org.restlet.data.Language;
import org.restlet.data.MediaType;

/**
 * Represents an appendable sequence of characters.
 * 
 * @author Jerome Louvel
 */
public class AppendableRepresentation extends StringRepresentation implements
        Appendable {

    /** The appendable text. */
    private volatile StringBuilder appendableText;

    /**
     * Constructor. The following metadata are used by default: "text/plain"
     * media type, no language and the ISO-8859-1 character set.
     * 
     */
    public AppendableRepresentation() {
        this(null);
    }

    /**
     * Constructor. The following metadata are used by default: "text/plain"
     * media type, no language and the ISO-8859-1 character set.
     * 
     * @param text
     *            The string value.
     */
    public AppendableRepresentation(CharSequence text) {
        super(text);
    }

    /**
     * Constructor. The following metadata are used by default: "text/plain"
     * media type, no language and the ISO-8859-1 character set.
     * 
     * @param text
     *            The string value.
     * @param language
     *            The language.
     */
    public AppendableRepresentation(CharSequence text, Language language) {
        super(text, language);
    }

    /**
     * Constructor. The following metadata are used by default: no language and
     * the ISO-8859-1 character set.
     * 
     * @param text
     *            The string value.
     * @param mediaType
     *            The media type.
     */
    public AppendableRepresentation(CharSequence text, MediaType mediaType) {
        super(text, mediaType);
    }

    /**
     * Constructor. The following metadata are used by default: ISO-8859-1
     * character set.
     * 
     * @param text
     *            The string value.
     * @param mediaType
     *            The media type.
     * @param language
     *            The language.
     */
    public AppendableRepresentation(CharSequence text, MediaType mediaType,
            Language language) {
        super(text, mediaType, language);
    }

    /**
     * Constructor.
     * 
     * @param text
     *            The string value.
     * @param mediaType
     *            The media type.
     * @param language
     *            The language.
     * @param characterSet
     *            The character set.
     */
    public AppendableRepresentation(CharSequence text, MediaType mediaType,
            Language language, CharacterSet characterSet) {
        super(text, mediaType, language, characterSet);
    }

    public Appendable append(char c) throws IOException {
        if (this.appendableText == null) {
            this.appendableText = new StringBuilder(c);
        } else {
            this.appendableText.append(c);
        }

        return this;
    }

    public Appendable append(CharSequence csq) throws IOException {
        if (this.appendableText == null) {
            this.appendableText = new StringBuilder(csq);
        } else {
            this.appendableText.append(csq);
        }

        return this;
    }

    public Appendable append(CharSequence csq, int start, int end)
            throws IOException {
        if (this.appendableText == null) {
            this.appendableText = new StringBuilder();
        }

        this.appendableText.append(csq, start, end);

        return this;
    }

    @Override
    public String getText() {
        return (this.appendableText == null) ? null : this.appendableText
                .toString();
    }

    @Override
    public void setText(CharSequence text) {
        if (text != null) {
            if (this.appendableText == null) {
                this.appendableText = new StringBuilder(text);
            } else {
                this.appendableText.delete(0, this.appendableText.length());
                this.appendableText.append(text);
            }
        } else {
            this.appendableText = null;
        }
    }
}
