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
package org.restlet.ext.jaxrs.internal.exceptions;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.MessageBodyReader;

import org.restlet.data.MediaType;

/**
 * This kind of Exception is thrown, if MessageBodyReaders are used, but are not
 * available. Normally this does not occurs.
 * 
 * @author Stephan Koops
 */
public class NoMessageBodyReaderException extends WebApplicationException {

    private static final long serialVersionUID = 9177449724300611418L;

    private final Class<?> paramType;

    private final MediaType mediaType;

    /**
     * @param paramType
     * @param mediaType
     * 
     */
    public NoMessageBodyReaderException(MediaType mediaType, Class<?> paramType) {
        super(Status.UNSUPPORTED_MEDIA_TYPE);
        // NICE super("No MessageBodyReader found for "+mediaType+" and "+paramType);
        this.mediaType = mediaType;
        this.paramType = paramType;
    }

    /**
     * Returns the java parameter type for which (in combination with the media
     * type, see {@link #getMediaType()}) no {@link MessageBodyReader} was
     * found.
     * 
     * @return the java parameter type for which (in combination with the media
     *         type, see {@link #getMediaType()}) no {@link MessageBodyReader}
     *         was found.
     */
    public Class<?> getParamType() {
        return this.paramType;
    }

    /**
     * Returns the media type for which (in combination with the java parameter
     * type, see {@link #getParamType()}) no {@link MessageBodyReader} was
     * found.
     * 
     * @return the media type for which (in combination with the java parameter
     *         type, see {@link #getParamType()}) no {@link MessageBodyReader}
     *         was found.
     */
    public MediaType getMediaType() {
        return this.mediaType;
    }
}