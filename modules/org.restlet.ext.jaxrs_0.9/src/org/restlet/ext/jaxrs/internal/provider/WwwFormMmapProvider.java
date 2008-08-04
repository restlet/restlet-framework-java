/*
 * Copyright 2005-2008 Noelios Technologies.
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
package org.restlet.ext.jaxrs.internal.provider;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.restlet.data.Form;
import org.restlet.ext.jaxrs.internal.core.UnmodifiableMultivaluedMap;
import org.restlet.ext.jaxrs.internal.util.Converter;
import org.restlet.ext.jaxrs.internal.util.Util;
import org.restlet.resource.Representation;

/**
 * This {@link Provider} converts MultivaluedMap&lt;String,String&gt; form
 * content to application/x-www-form-urlencoded and vice versa.
 * 
 * @author Stephan Koops
 * @see WwwFormFormProvider
 */
@Provider
@Consumes("application/x-www-form-urlencoded")
@Produces("application/x-www-form-urlencoded")
public class WwwFormMmapProvider extends
        AbstractProvider<MultivaluedMap<String, String>> {

    /**
     * @see MessageBodyWriter#getSize(Object)
     */
    @Override
    public long getSize(MultivaluedMap<String, String> mmap) {
        return -1;
    }

    /**
     * @see org.restlet.ext.jaxrs.internal.provider.AbstractProvider#supportedClass()
     */
    @Override
    protected Class<?> supportedClass() {
        return MultivaluedMap.class;
    }

    /**
     * @see MessageBodyWriter#writeTo(Object, Class, Type, Annotation[],
     *      MediaType, MultivaluedMap, OutputStream)
     */
    @Override
    public void writeTo(MultivaluedMap<String, String> mmap, Class<?> type,
            Type genericType, Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, Object> httpHeaders,
            OutputStream entityStream) throws IOException {
        Form form = Converter.toForm(mmap);
        Representation formRepr = form.getWebRepresentation();
        Util.copyStream(formRepr.getStream(), entityStream);
    }

    /**
     * @see MessageBodyReader#readFrom(Class, Type, MediaType, Annotation[],
     *      MultivaluedMap, InputStream)
     */
    @Override
    public MultivaluedMap<String, String> readFrom(
            Class<MultivaluedMap<String, String>> type, Type genericType,
            Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, String> httpResponseHeaders,
            InputStream entityStream) throws IOException {
        Form form = WwwFormFormProvider.getForm(mediaType, entityStream);
        return UnmodifiableMultivaluedMap.getFromForm(form, false);
    }
}