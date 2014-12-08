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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.data.CharacterSet;
import org.restlet.data.Language;
import org.restlet.data.MediaType;

/**
 * Represents an Unicode string that can be converted to any character set
 * supported by Java.
 * 
 * @author Jerome Louvel
 */
public class StringRepresentation extends CharacterRepresentation {

    /** The string value. */
    private volatile CharSequence text;

    /**
     * Constructor. The following metadata are used by default: "text/plain"
     * media type, no language and the UTF-8 character set.
     * 
     * @param chars
     *            The characters array.
     */
    public StringRepresentation(char[] chars) {
        this(new String(chars), MediaType.TEXT_PLAIN);
    }

    /**
     * Constructor. The following metadata are used by default: "text/plain"
     * media type, no language and the UTF-8 character set.
     * 
     * @param text
     *            The string value.
     */
    public StringRepresentation(CharSequence text) {
        this(text, MediaType.TEXT_PLAIN);
    }

    /**
     * Constructor. The following metadata are used by default: "text/plain"
     * media type, no language and the UTF-8 character set.
     * 
     * @param text
     *            The string value.
     * @param language
     *            The language.
     */
    public StringRepresentation(CharSequence text, Language language) {
        this(text, MediaType.TEXT_PLAIN, language);
    }

    /**
     * Constructor. The following metadata are used by default: no language and
     * the UTF-8 character set.
     * 
     * @param text
     *            The string value.
     * @param mediaType
     *            The media type.
     */
    public StringRepresentation(CharSequence text, MediaType mediaType) {
        this(text, mediaType, null);
    }

    /**
     * Constructor. The following metadata are used by default: UTF-8 character
     * set.
     * 
     * @param text
     *            The string value.
     * @param mediaType
     *            The media type.
     * @param language
     *            The language.
     */
    public StringRepresentation(CharSequence text, MediaType mediaType,
            Language language) {
        this(text, mediaType, language, CharacterSet.UTF_8);
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
    public StringRepresentation(CharSequence text, MediaType mediaType,
            Language language, CharacterSet characterSet) {
        super(mediaType);
        setMediaType(mediaType);
        if (language != null) {
            getLanguages().add(language);
        }

        setCharacterSet(characterSet);
        setText(text);
    }

    @Override
    public Reader getReader() throws IOException {
        if (getText() != null) {
            return new StringReader(getText());
        }

        return null;
    }

    @Override
    public InputStream getStream() throws IOException {
        // [ifndef gwt]
        CharacterSet charset = getCharacterSet() == null ? CharacterSet.ISO_8859_1
                : getCharacterSet();
        ByteArrayInputStream result = new ByteArrayInputStream(getText()
                .getBytes(charset.getName()));
        // [enddef]
        // [ifdef gwt] instruction uncomment
        // InputStream result = new
        // org.restlet.engine.io.StringInputStream(getText());
        return result;
    }

    @Override
    public String getText() {
        return (this.text == null) ? null : this.text.toString();
    }

    /**
     * Closes and releases the input stream.
     */
    @Override
    public void release() {
        setText(null);
        super.release();
    }

    @Override
    public void setCharacterSet(CharacterSet characterSet) {
        super.setCharacterSet(characterSet);
        updateSize();
    }

    /**
     * Sets the string value.
     * 
     * @param text
     *            The string value.
     */
    public void setText(CharSequence text) {
        this.text = text;
        updateSize();
    }

    /**
     * Sets the string value.
     * 
     * @param text
     *            The string value.
     */
    public void setText(String text) {
        setText((CharSequence) text);
    }

    @Override
    public String toString() {
        return getText();
    }

    /**
     * Updates the expected size according to the current string value.
     */
    protected void updateSize() {
        if (getText() != null) {
            // [ifndef gwt]
            try {
                if (getCharacterSet() != null) {
                    setSize(getText().getBytes(getCharacterSet().getName()).length);
                } else {
                    setSize(getText().getBytes().length);
                }
            } catch (UnsupportedEncodingException e) {
                Context.getCurrentLogger().log(Level.WARNING,
                        "Unable to update size", e);
                setSize(UNKNOWN_SIZE);
            }
            // [enddef]
            // [ifdef gwt] instruction uncomment
            // setSize(getText().length());
        } else {
            setSize(UNKNOWN_SIZE);
        }
    }

    // [ifndef gwt] method
    @Override
    public void write(Writer writer) throws IOException {
        if (getText() != null) {
            writer.write(getText());
            writer.flush();
        }
    }

}
