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

package org.restlet.ext.jaxrs.internal.util;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MultivaluedMap;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.ext.jaxrs.internal.wrappers.provider.MessageBodyWriter;
import org.restlet.representation.OutputRepresentation;

/**
 * This representation is used to write the Representations with a
 * {@link javax.ws.rs.ext.MessageBodyWriter}.
 * 
 * @author Stephan Koops
 * @param <T>
 *            type of the object to serialize.
 */
public class JaxRsOutputRepresentation<T> extends OutputRepresentation {

    private static final Logger LOGGER = Context.getCurrentLogger();

    private final MessageBodyWriter mbw;

    private final T object;

    private final Type genericType;

    private final Annotation[] annotations;

    private final MultivaluedMap<String, Object> httpHeaders;

    /**
     * Creates a new JaxRsOutputRepresentation.
     * 
     * @param object
     *            the object to serialize The generic {@link Type} to convert
     *            to.
     * @param genericType
     *            The generic {@link Type} to convert to.
     * @param mediaType
     *            the MediaType of the object. Must be concrete, see
     *            {@link MediaType#isConcrete()}.
     * @param annotations
     *            the annotations of the artefact to convert to
     * @param mbw
     *            the MessageBodyWriter which will serialize the object.
     * @param httpHeaders
     *            the mutable Map of HTTP response headers.
     */
    public JaxRsOutputRepresentation(T object, Type genericType,
            MediaType mediaType, Annotation[] annotations,
            MessageBodyWriter mbw, MultivaluedMap<String, Object> httpHeaders) {
        super(mediaType, mbw.getSize(object, (object != null ? object
                .getClass() : null), genericType, annotations, mediaType));
        if (!mediaType.isConcrete()) {
            throw new IllegalArgumentException(mediaType + " is not concrete");
        }
        this.genericType = genericType;
        this.annotations = annotations;
        this.mbw = mbw;
        this.httpHeaders = httpHeaders;
        this.object = object;
    }

    /**
     * @see org.restlet.representation.Representation#write(java.io.OutputStream)
     */
    @Override
    public void write(OutputStream outputStream) throws IOException {
        try {
            this.mbw.writeTo(this.object, this.object.getClass(),
                    this.genericType, this.annotations, getMediaType(),
                    this.httpHeaders, outputStream);
        } catch (WebApplicationException e) {
            final String msg = "The Restlet extension for JAX-RS do not support the throwing of WebApplicationException in a MessageBodyWriter.";
            LOGGER.config(msg);
            throw e;
        } catch (UnsupportedOperationException e) {
            LOGGER.log(Level.CONFIG, "operation not supported", e);
            throw e;
        } catch (RuntimeException e) {
            final String msg = e.getClass().getName()
                    + " while running MessageOutputWriter:";
            LOGGER.log(Level.CONFIG, msg, e);
            throw e;
        }
    }
}