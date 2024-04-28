/**
 * Copyright 2005-2024 Qlik
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
 * https://restlet.talend.com/
 * 
 * Restlet is a registered trademark of QlikTech International AB.
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

import org.restlet.engine.io.IoUtils;

/**
 * {@link Provider} to read and write byte[].
 * 
 * @author Stephan Koops
 * @deprecated Will be removed in next minor release.
 */
@Deprecated
@Provider
public class ByteArrayProvider extends AbstractProvider<byte[]> {

    /**
     * @see javax.ws.rs.ext.MessageBodyWriter#getSize(java.lang.Object,
     *      java.lang.Class, java.lang.reflect.Type,
     *      java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType)
     */
    @Override
    public long getSize(byte[] t, Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType) {
        return t.length;
    }

    @Override
    public byte[] readFrom(Class<byte[]> type, Type genericType,
            Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
            throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        IoUtils.copy(entityStream, baos);
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
