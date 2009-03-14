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

package org.restlet.gwt.engine.http;

import java.io.IOException;

import org.restlet.gwt.data.CharacterSet;
import org.restlet.gwt.data.MediaType;
import org.restlet.gwt.data.Preference;

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
            throws Exception {
        final PreferenceReader<MediaType> pr = new PreferenceReader<MediaType>(
                PreferenceReader.TYPE_MEDIA_TYPE, headerValue);
        final Preference<MediaType> pref = pr.readPreference();
        return pref.getMetadata();
    }

    /**
     * The content media type.
     */
    private MediaType mediaType;

    /**
     * The content character set.
     */
    private CharacterSet characterSet;

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
                this.characterSet = new CharacterSet(charSet);
            }
        } catch (Exception ioe) {
            ioe.printStackTrace();
            // TODO IllegalArgumentException? remove line after think about :-)
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
