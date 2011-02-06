/**
 * Copyright 2005-2011 Noelios Technologies.
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

import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;

/**
 * Association of a media type and a character set.
 * 
 * @author Jerome Louvel
 */
public class ContentType {

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
}
