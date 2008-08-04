/*
 * Copyright 2005-2008 Noelios Technologies.
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

package org.restlet.gwt.resource;

import org.restlet.gwt.data.CharacterSet;
import org.restlet.gwt.data.Language;
import org.restlet.gwt.data.MediaType;

/**
 * Represents an Unicode string that can be converted to any character set
 * supported by Java.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class StringRepresentation extends Representation {

    /** The string value. */
    private volatile String text;

    /**
     * Constructor. The following metadata are used by default: "text/plain"
     * media type, no language and the ISO-8859-1 character set.
     * 
     * @param text
     *            The string value.
     */
    public StringRepresentation(String text) {
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
    public StringRepresentation(String text, Language language) {
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
    public StringRepresentation(String text, MediaType mediaType) {
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
    public StringRepresentation(String text, MediaType mediaType,
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
    public StringRepresentation(String text, MediaType mediaType,
            Language language, CharacterSet characterSet) {
        super(mediaType);
        this.text = text;
        setMediaType(mediaType);
        if (language != null) {
            getLanguages().add(language);
        }

        setCharacterSet(characterSet);
        updateSize();
    }

    @Override
    public String getText() {
        return this.text;
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
    public void setText(String text) {
        this.text = text;
        updateSize();
    }

    /**
     * Updates the expected size according to the current string value.
     */
    protected void updateSize() {
        if (getText() != null) {
            try {
                setSize(getText().length());
            } catch (final Exception e) {
                setSize(UNKNOWN_SIZE);
            }
        } else {
            setSize(UNKNOWN_SIZE);
        }
    }

}
