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

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MultivaluedMap;

import org.restlet.data.MediaType;

/**
 * Interface to access a wrapped an initialized
 * {@link javax.ws.rs.ext.MessageBodyWriter}
 * 
 * @author Stephan Koops
 * @see javax.ws.rs.ext.MessageBodyWriter
 */
public interface MessageBodyWriter {

    /**
     * 
     * @param type
     * @param genericType
     * @param annotations
     * @param mediaType
     *            The JAX-RS MediaType
     * @return true, if the wrapped writer could write an object of the given
     *         class with the given annotations and media type.
     * @see javax.ws.rs.ext.MessageBodyWriter#isWriteable(Class, Type,
     *      Annotation[])
     */
    public boolean isWriteable(Class<?> type, Type genericType,
            Annotation annotations[], javax.ws.rs.core.MediaType mediaType);

    /**
     * Called before <code>writeTo</code> to ascertain the length in bytes of
     * the serialized form of <code>t</code>. A non-negative return value is
     * used in a HTTP <code>Content-Length</code> header.
     * 
     * @param t
     *            the instance to write
     * @param type
     * @param genericType
     * @param annotations
     * @param mediaType
     *            The Restlet MediaType
     * @return length in bytes or -1 if the length cannot be determined in
     *         advance
     * @see javax.ws.rs.ext.MessageBodyWriter#getSize(Object, Class, Type,
     *      Annotation[], javax.ws.rs.core.MediaType)
     */
    public long getSize(Object t, Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType);

    /**
     * @param object
     * @param type
     * @param genericType
     * @param annotations
     * @param mediaType
     *            The Restlet MediaType
     * @param httpHeaders
     * @param entityStream
     * @throws IOException
     * @throws WebApplicationException
     * @see javax.ws.rs.ext.MessageBodyWriter#writeTo(Object, Class, Type,
     *      Annotation[], javax.ws.rs.core.MediaType, MultivaluedMap,
     *      OutputStream)
     */
    public void writeTo(Object object, Class<?> type, Type genericType,
            Annotation annotations[], MediaType mediaType,
            MultivaluedMap<String, Object> httpHeaders,
            OutputStream entityStream) throws IOException,
            WebApplicationException;

    /**
     * Returns the JAX-RS {@link javax.ws.rs.ext.MessageBodyWriter}.
     * 
     * @return the JAX-RS MessageBodyWriter
     */
    public javax.ws.rs.ext.MessageBodyWriter<?> getJaxRsWriter();

    /**
     * Returns the list of produced {@link MediaType}s of the wrapped
     * {@link javax.ws.rs.ext.MessageBodyWriter}.
     * 
     * @return List of produced Restlet {@link MediaType}s. If the entity
     *         provider is not annotated with &#64; {@link javax.ws.rs.Produces}
     *         , '*<!---->/*' is returned.
     */
    public List<MediaType> getProducedMimes();

    /**
     * Checks, if the wrapped MessageBodyWriter supports at least one of the
     * given {@link MediaType}s.
     * 
     * @param mediaTypes
     *            the Restlet {@link MediaType}s
     * @return true, if at least one of the requested {@link MediaType}s is
     *         supported, otherwise false.
     */
    public boolean supportsWrite(Iterable<MediaType> mediaTypes);

    /**
     * Checks, if the wrapped MessageBodyWriter supports the given
     * {@link MediaType}.
     * 
     * @param mediaType
     *            the Restlet {@link MediaType}
     * @return true, if the requested {@link MediaType} is supported, otherwise
     *         false.
     */
    public boolean supportsWrite(MediaType mediaType);

    /**
     * Checks, if this message body writer supports the given type (by the type
     * parameter of the {@link javax.ws.rs.ext.MessageBodyWriter})
     * 
     * @param entityClass
     *            the type
     * @param genericType
     *            the generic type
     * @return true, if this MessageBodyWriter supports the given type, false,
     *         if not.
     */
    public boolean supportsWrite(Class<?> entityClass, Type genericType);
}