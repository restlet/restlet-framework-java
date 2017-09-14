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

package org.restlet.engine.application;

import org.restlet.data.CharacterSet;
import org.restlet.data.Encoding;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Metadata;

/**
 * Associates an extension name and a metadata.
 * 
 * @author Alex Milowski (alexml@milowski.org)
 * @author Thierry Boileau
 */
public class MetadataExtension {

    /** The mapped metadata. */
    private final Metadata metadata;

    /** The name of the extension. */
    private final String name;

    /**
     * Constructor.
     * 
     * @param name
     *            The extension name.
     * @param metadata
     *            The metadata.
     */
    public MetadataExtension(String name, Metadata metadata) {
        this.name = name;
        this.metadata = metadata;
    }

    /**
     * Returns the character set.
     * 
     * @return the character set.
     */
    public CharacterSet getCharacterSet() {
        return (CharacterSet) getMetadata();
    }

    /**
     * Returns the encoding.
     * 
     * @return the encoding.
     */
    public Encoding getEncoding() {
        return (Encoding) getMetadata();
    }

    /**
     * Returns the language.
     * 
     * @return the language.
     */
    public Language getLanguage() {
        return (Language) getMetadata();
    }

    /**
     * Returns the media type.
     * 
     * @return the media type.
     */
    public MediaType getMediaType() {
        return (MediaType) getMetadata();
    }

    /**
     * Returns the metadata.
     * 
     * @return the metadata.
     */
    public Metadata getMetadata() {
        return this.metadata;
    }

    /**
     * Returns the extension name.
     * 
     * @return The extension name.
     */
    public String getName() {
        return this.name;
    }

}
