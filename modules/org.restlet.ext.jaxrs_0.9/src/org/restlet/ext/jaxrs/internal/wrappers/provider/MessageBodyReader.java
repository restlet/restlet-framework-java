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
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */
package org.restlet.ext.jaxrs.internal.wrappers.provider;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MultivaluedMap;

import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;

/**
 * Class to wrap a {@link javax.ws.rs.ext.MessageBodyWriter}
 * 
 * @author Stephan Koops
 */
public interface MessageBodyReader {

    /**
     * 
     * @param type
     * @param genericType
     * @param annotations
     * @return
     * @see {@link javax.ws.rs.ext.MessageBodyReader#isReadable(Class, Type, Annotation[])}
     */
    public boolean isReadable(Class<?> type, Type genericType,
            Annotation annotations[]);

    /**
     * 
     * @param type
     * @param genericType
     * @param annotations
     * @param mediaType
     * @param characterSet
     * @param httpHeaders
     * @param entityStream
     * @return
     * @throws IOException
     * @throws WebApplicationException
     * @see {@link javax.ws.rs.ext.MessageBodyReader#readFrom(Class, Type, Annotation[], javax.ws.rs.core.MediaType, MultivaluedMap, InputStream)}
     */
    public Object readFrom(Class<?> type, Type genericType,
            Annotation annotations[], MediaType mediaType,
            CharacterSet characterSet,
            MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
            throws IOException, WebApplicationException, InvocationTargetException;

    /**
     * Returns the list of produced {@link MediaType}s of the wrapped
     * {@link javax.ws.rs.ext.MessageBodyWriter}.
     * 
     * @return List of produced {@link MediaType}s.
     */
    public List<MediaType> getConsumedMimes();

    /**
     * Returns the JAX-RS {@link javax.ws.rs.ext.MessageBodyReader}.
     * 
     * @return the JAX-RS MessageBodyReader
     */
    public javax.ws.rs.ext.MessageBodyReader<?> getJaxRsReader();

    /**
     * Checks, if this MessageBodyReader supports the given MediaType.
     * 
     * @param mediaType
     * @return true, if the given media type could be read, or false if not.
     */
    public boolean supportsRead(MediaType mediaType);
}
