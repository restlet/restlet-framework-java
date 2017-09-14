/**
 * Copyright 2005-2014 Restlet
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
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
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
import org.restlet.engine.io.IoUtils;
import org.restlet.ext.jaxrs.internal.core.UnmodifiableMultivaluedMap;
import org.restlet.ext.jaxrs.internal.util.Converter;
import org.restlet.ext.jaxrs.internal.wrappers.provider.ProviderWrapper;
import org.restlet.representation.Representation;

/**
 * This {@link ProviderWrapper} converts MultivaluedMap&lt;String,String&gt;
 * form content to application/x-www-form-urlencoded and vice versa.
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
    public long getSize(MultivaluedMap<String, String> mmap, Class<?> type,
            Type genericType, Annotation[] annotations, MediaType mediaType) {
        return -1;
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
        return UnmodifiableMultivaluedMap.getFromSeries(form, false);
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
        IoUtils.copy(formRepr.getStream(), entityStream);
    }
}
