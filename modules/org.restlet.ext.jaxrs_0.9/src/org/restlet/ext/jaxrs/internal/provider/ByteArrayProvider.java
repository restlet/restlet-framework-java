/*
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). 
 * You can select the license that you prefer but you may not use this file 
 * except in compliance with one of these Licenses.
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
 * Alternatively, you can obtain a royaltee free commercial license with 
 * less limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine/.
 *
 * Restlet is a registered trademark of Noelios Technologies.
 */
package org.restlet.ext.jaxrs.internal.provider;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.restlet.ext.jaxrs.internal.util.Util;

/**
 * {@link Provider} to read and write byte[].
 * 
 * @author Stephan Koops
 */
@Provider
public class ByteArrayProvider extends AbstractProvider<byte[]> {

    /**
     * @see MessageBodyWriter#getSize(Object)
     */
    @Override
    public long getSize(byte[] t) {
        return t.length;
    }

    @Override
    public byte[] readFrom(Class<byte[]> type, Type genericType,
            Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
            throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Util.copyStream(entityStream, baos);
        return baos.toByteArray();
    }

    /**
     * @see MessageBodyWriter#isWriteable(Class)
     */
    @Override
    protected Class<?> supportedClass() {
        return byte[].class;
    }

    /**
     * @see MessageBodyWriter#writeTo(Object, Class, Type, Annotation[],
     *      MediaType, MultivaluedMap, OutputStream)
     */
    @Override
    public void writeTo(byte[] data, Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, Object> httpHeaders,
            OutputStream entityStream) throws IOException {
        entityStream.write(data);
    }
}