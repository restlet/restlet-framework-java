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
 * http://www.noelios.com/products/restlet-engine/.
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */
package org.restlet.ext.jaxrs.internal.provider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

/**
 * This {@link MessageBodyReader} is used to read data from the network to a
 * {@link BufferedReader}.
 * 
 * @author Stephan Koops
 * @see ReaderProvider
 */
@Provider
public class BufferedReaderProvider implements
        MessageBodyReader<BufferedReader> {

    public boolean isReadable(Class<?> type, Type genericType,
            Annotation[] annotations) {
        return BufferedReader.class.isAssignableFrom(type);
    }

    /**
     * @see MessageBodyReader#readFrom(Class, Type, MediaType, Annotation[],
     *      MultivaluedMap, InputStream)
     */
    public BufferedReader readFrom(Class<BufferedReader> type,
            Type genericType, Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
            throws IOException {
        return new BufferedReader(ReaderProvider.getReader(entityStream));
    }
}