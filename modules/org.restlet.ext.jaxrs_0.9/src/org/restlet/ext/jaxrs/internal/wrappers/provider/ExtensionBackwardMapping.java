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
     *                the metadata service of the {@link JaxRsApplication}.
     */
    public ExtensionBackwardMapping(MetadataService metadataService) {
        this.metadataService = metadataService;
    }

    /**
     * Returns the virtual extension for the given {@link MediaType}.
     * 
     * @param mediaType
     *                the JAX-RS media type to get the virtual file extension
     *                for.
     * @return the extension for the given {@link MediaType}. Returns null, if
     *         no mapping could be found.
     */
    public String getByMediaType(MediaType mediaType) {
        org.restlet.data.MediaType restletMediaType;
        restletMediaType = Converter.toRestletMediaType(mediaType);
        return metadataService.getExtension(restletMediaType);
    }

    /**
     * Returns the virtual extension for the given language.
     * 
     * @param language
     *                the language to get the virtual file extension for.
     * @return the extension for the given language. Returns null, if no mapping
     *         could be found.
     */
    public String getByLanguage(Locale language) {
        return metadataService.getExtension(Converter.toLanguage(language));
    }

    /**
     * Returns the virtual extension for the given encoding.
     * 
     * @param encoding
     *                the encoding to get the virtual file extension for.
     * @return the extension for the given encoding. Returns null, if no mapping
     *         could be found.
     */
    public String getByEncoding(String encoding) {
        return metadataService.getExtension(Encoding.valueOf(encoding));
    }
}