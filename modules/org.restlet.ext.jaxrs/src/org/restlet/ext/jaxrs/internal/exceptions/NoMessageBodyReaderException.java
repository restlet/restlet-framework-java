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
     */
    public NoMessageBodyReaderException(MediaType mediaType, Class<?> paramType) {
        super(Status.UNSUPPORTED_MEDIA_TYPE);
        // NICE super("No MessageBodyR found for "+mediaType+" and "+paramType);
        this.mediaType = mediaType;
        this.paramType = paramType;
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
}