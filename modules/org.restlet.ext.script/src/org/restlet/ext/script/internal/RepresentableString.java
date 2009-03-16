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

package org.restlet.ext.script.internal;

import org.restlet.data.CharacterSet;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.representation.StringRepresentation;

/**
 * Creates {@link StringRepresentation} instances on the fly from stored
 * parameters.
 * 
 * @author Tal Liron
 */
public class RepresentableString {
    private final String string;

    private final MediaType mediaType;

    private final Language language;

    private final CharacterSet characterSet;

    /**
     * Constructor.
     * 
     * @param string
     *            The string
     * @param mediaType
     *            The media type
     * @param language
     *            The language
     * @param characterSet
     *            The character set
     */
    public RepresentableString(String string, MediaType mediaType,
            Language language, CharacterSet characterSet) {
        this.string = string;
        this.mediaType = mediaType;
        this.language = language;
        this.characterSet = characterSet;
    }

    /**
     * The stored string.
     * 
     * @return The string
     */
    public String getString() {
        return this.string;
    }

    /**
     * Creates a {@link StringRepresentation}.
     * 
     * @return A {@link StringRepresentation}
     */
    public StringRepresentation represent() {
        return new StringRepresentation(this.string, this.mediaType,
                this.language, this.characterSet);
    }
}