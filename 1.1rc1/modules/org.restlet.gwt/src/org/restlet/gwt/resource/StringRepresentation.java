/**
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royaltee free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.gwt.resource;

import org.restlet.gwt.data.CharacterSet;
import org.restlet.gwt.data.Language;
import org.restlet.gwt.data.MediaType;

/**
 * Represents an Unicode string that can be converted to any character set
 * supported by Java.
 * 
 * @author Jerome Louvel
 */
public class StringRepresentation extends Representation {

    /** The string value. */
    private String text;

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
