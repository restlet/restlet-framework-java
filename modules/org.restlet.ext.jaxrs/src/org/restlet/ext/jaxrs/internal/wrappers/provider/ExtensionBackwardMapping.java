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

package org.restlet.ext.jaxrs.internal.wrappers.provider;

import java.util.Locale;

import javax.ws.rs.core.MediaType;

import org.restlet.data.Encoding;
import org.restlet.ext.jaxrs.JaxRsApplication;
import org.restlet.ext.jaxrs.internal.util.Converter;
import org.restlet.service.MetadataService;

/**
 * Allows a backward mapping for the extension mapping.
 * 
 * @author Stephan Koops
 */
public class ExtensionBackwardMapping {

    private final MetadataService metadataService;

    /**
     * Creates a new ExtensionBackwardMapping
     * 
     * @param metadataService
     *            the metadata service of the {@link JaxRsApplication}.
     */
    public ExtensionBackwardMapping(MetadataService metadataService) {
        this.metadataService = metadataService;
    }

    /**
     * Returns the virtual extension for the given encoding.
     * 
     * @param encoding
     *            the encoding to get the virtual file extension for.
     * @return the extension for the given encoding. Returns null, if no mapping
     *         could be found.
     */
    public String getByEncoding(String encoding) {
        return this.metadataService.getExtension(Encoding.valueOf(encoding));
    }

    /**
     * Returns the virtual extension for the given language.
     * 
     * @param language
     *            the language to get the virtual file extension for.
     * @return the extension for the given language. Returns null, if no mapping
     *         could be found.
     */
    public String getByLanguage(Locale language) {
        return this.metadataService
                .getExtension(Converter.toLanguage(language));
    }

    /**
     * Returns the virtual extension for the given {@link MediaType}.
     * 
     * @param mediaType
     *            the JAX-RS media type to get the virtual file extension for.
     * @return the extension for the given {@link MediaType}. Returns null, if
     *         no mapping could be found.
     */
    public String getByMediaType(MediaType mediaType) {
        org.restlet.data.MediaType restletMediaType;
        restletMediaType = Converter.toRestletMediaType(mediaType);
        return this.metadataService.getExtension(restletMediaType);
    }
}