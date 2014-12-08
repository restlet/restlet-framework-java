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

import java.io.IOException;

import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;

/**
 * Association of a media type, a character set and modifiers.
 * 
 * @author Jerome Louvel
 */
public class ContentType {

    /**
     * Parses the given content type header and returns the character set.
     * 
     * @param contentType
     *            The content type header to parse.
     * @return The character set.
     */
    public static CharacterSet readCharacterSet(String contentType) {
        return new ContentType(contentType).getCharacterSet();
    }

    /**
     * Parses the given content type header and returns the media type.
     * 
     * @param contentType
     *            The content type header to parse.
     * @return The media type.
     */
    public static MediaType readMediaType(String contentType) {
        return new ContentType(contentType).getMediaType();
    }

    /**
     * Writes the HTTP "Content-Type" header.
     * 
     * @param mediaType
     *            The representation media type.
     * @param characterSet
     *            The representation character set.
     * @return The HTTP "Content-Type" header.
     */
    public static String writeHeader(MediaType mediaType,
            CharacterSet characterSet) {
        String result = mediaType.toString();

        // Specify the character set parameter if required
        if ((mediaType.getParameters().getFirstValue("charset") == null)
                && (characterSet != null)) {
            result = result + "; charset=" + characterSet.getName();
        }

        return result;

    }

    /**
     * Writes the HTTP "Content-Type" header.
     * 
     * @param representation
     *            The related representation.
     * @return The HTTP "Content-Type" header.
     */
    public static String writeHeader(Representation representation) {
        return writeHeader(representation.getMediaType(),
                representation.getCharacterSet());
    }

    /**
     * The content character set.
     */
    private volatile CharacterSet characterSet;

    /**
     * The content media type.
     */
    private volatile MediaType mediaType;

    /**
     * Constructor.
     * 
     * @param mediaType
     *            The media type.
     * @param characterSet
     *            The character set.
     */
    public ContentType(MediaType mediaType, CharacterSet characterSet) {
        this.mediaType = mediaType;
        this.characterSet = characterSet;
    }

    /**
     * Constructor.
     * 
     * @param representation
     *            The representation.
     */
    public ContentType(Representation representation) {
        this(representation.getMediaType(), representation.getCharacterSet());
    }

    /**
     * Constructor.
     * 
     * @param headerValue
     *            The "Content-type" header to parse.
     */
    public ContentType(String headerValue) {
        try {
            ContentTypeReader ctr = new ContentTypeReader(headerValue);
            ContentType ct = ctr.readValue();

            if (ct != null) {
                this.mediaType = ct.getMediaType();
                this.characterSet = ct.getCharacterSet();
            }
        } catch (IOException ioe) {
            throw new IllegalArgumentException(
                    "The Content Type could not be read.", ioe);
        }
    }

    /**
     * Returns the character set.
     * 
     * @return The character set.
     */
    public CharacterSet getCharacterSet() {
        return this.characterSet;
    }

    /**
     * Returns the media type.
     * 
     * @return The media type.
     */
    public MediaType getMediaType() {
        return this.mediaType;
    }

    @Override
    public String toString() {
        return writeHeader(getMediaType(), getCharacterSet());
    }
}
