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
 * http://www.noelios.com/products/restlet-engine/.
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package com.noelios.restlet.http;

import java.io.IOException;

import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;
import org.restlet.data.Preference;

/**
 * Association of a media type and a character set.
 * 
 * @author Jerome Louvel
 */
public class ContentType {
    /**
     * Parses the Content Type.
     * 
     * @param headerValue
     * @return MediaType
     * @throws IOException
     */
    public static MediaType parseContentType(String headerValue)
            throws IOException {
        final PreferenceReader<MediaType> pr = new PreferenceReader<MediaType>(
                PreferenceReader.TYPE_MEDIA_TYPE, headerValue);
        final Preference<MediaType> pref = pr.readPreference();
        return pref.getMetadata();
    }

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
     *            The "Content-type" header to parse.
     */
    public ContentType(String headerValue) {
        try {
            this.mediaType = parseContentType(headerValue);
            final String charSet = this.mediaType.getParameters()
                    .getFirstValue("charset");
            if (charSet != null) {
                this.mediaType.getParameters().removeAll("charset");
                this.characterSet = new CharacterSet(charSet);
            }
        } catch (final IOException ioe) {
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
