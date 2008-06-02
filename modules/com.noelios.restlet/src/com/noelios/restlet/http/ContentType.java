/*
 * Copyright 2005-2008 Noelios Consulting.
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

package com.noelios.restlet.http;

import java.io.IOException;

import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;
import org.restlet.data.Preference;

/**
 * Association of a media type and a character set.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class ContentType {
    /**
     * The content media type.
     */
    private volatile MediaType mediaType;

    /**
     * The content character set.
     */
    private volatile CharacterSet characterSet;

    /**
     * Constructor.
     * 
     * @param headerValue
     *                The "Content-type" header to parse.
     */
    public ContentType(String headerValue) {
        try {
            this.mediaType = parseContentType(headerValue);
            String charSet = this.mediaType.getParameters().getFirstValue(
                    "charset");
            if (charSet != null) {
                this.characterSet = new CharacterSet(charSet);
            }
        } catch (IOException ioe) {
            throw new IllegalArgumentException(
                    "The Content Type could not be read.", ioe);
        }
    }

    /**
     * Parses the Content Type.
     * 
     * @param headerValue
     * @return MediaType
     * @throws IOException
     */
    public static MediaType parseContentType(String headerValue)
            throws IOException {
        PreferenceReader<MediaType> pr = new PreferenceReader<MediaType>(
                PreferenceReader.TYPE_MEDIA_TYPE, headerValue);
        Preference<MediaType> pref = pr.readPreference();
        return pref.getMetadata();
    }

    /**
     * Returns the media type.
     * 
     * @return The media type.
     */
    public MediaType getMediaType() {
        return mediaType;
    }

    /**
     * Returns the character set.
     * 
     * @return The character set.
     */
    public CharacterSet getCharacterSet() {
        return characterSet;
    }
}
