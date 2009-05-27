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

package org.restlet.representation;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
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
public class StringRepresentation extends StreamRepresentation {

    /** The string value. */
    private volatile CharSequence text;

    /**
     * Constructor. The following metadata are used by default: "text/plain"
     * media type, no language and the ISO-8859-1 character set.
     * 
     * @param text
     *            The string value.
     */
    public StringRepresentation(CharSequence text) {
        this(text, MediaType.TEXT_PLAIN);
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
    public StringRepresentation(CharSequence text, Language language) {
        this(text, MediaType.TEXT_PLAIN, language);
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
    public StringRepresentation(CharSequence text, MediaType mediaType) {
        this(text, mediaType, null);
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
    public StringRepresentation(CharSequence text, MediaType mediaType,
            Language language) {
        this(text, mediaType, language, CharacterSet.ISO_8859_1);
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
        setText(text);
        setMediaType(mediaType);
        if (language != null) {
            getLanguages().add(language);
        }

        setCharacterSet(characterSet);
        updateSize();
    }

    @Override
    public InputStream getStream() throws IOException {
        if (getText() != null) {
            if (getCharacterSet() != null) {
                return new ByteArrayInputStream(getText().getBytes(
                        getCharacterSet().getName()));
            }

            return new ByteArrayInputStream(getText().getBytes());
        }

        return null;
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

    /**
     * Updates the expected size according to the current string value.
     */
    protected void updateSize() {
        if (getText() != null) {
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
        } else {
            setSize(UNKNOWN_SIZE);
        }
    }

    @Override
    public void write(OutputStream outputStream) throws IOException {
        if (getText() != null) {
            OutputStreamWriter osw = null;

            if (getCharacterSet() != null) {
                osw = new OutputStreamWriter(outputStream, getCharacterSet()
                        .getName());
            } else {
                osw = new OutputStreamWriter(outputStream);
            }

            osw.write(getText());
            osw.flush();
        }
    }

}
