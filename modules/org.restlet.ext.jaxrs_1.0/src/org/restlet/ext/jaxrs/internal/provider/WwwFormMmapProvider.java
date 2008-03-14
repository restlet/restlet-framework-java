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
package org.restlet.ext.jaxrs.internal.provider;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.ConsumeMime;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

import org.restlet.data.Form;
import org.restlet.ext.jaxrs.internal.core.UnmodifiableMultivaluedMap;
import org.restlet.ext.jaxrs.internal.util.Converter;
import org.restlet.resource.InputRepresentation;
import org.restlet.resource.Representation;

/**
 * This {@link Provider} converts MultivaluedMap&lt;String,String&gt; form
 * content to application/x-www-form-urlencoded and vice versa.
 * 
 * @author Stephan Koops
 */
@Provider
@ConsumeMime("application/x-www-form-urlencoded")
@ProduceMime("application/x-www-form-urlencoded")
public class WwwFormMmapProvider extends
        AbstractProvider<MultivaluedMap<String, String>> {

    /**
     * 
     */
    public WwwFormMmapProvider() {
    }

    /**
     * @see org.restlet.ext.jaxrs.internal.provider.AbstractProvider#getSize(java.lang.Object)
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
     * 
     * @see org.restlet.ext.jaxrs.internal.provider.AbstractProvider#writeTo(java.lang.Object,
     *      Type, Annotation[],
     *      javax.ws.rs.core.MediaType, javax.ws.rs.core.MultivaluedMap, java.io.OutputStream)
     */
    @Override
    public void writeTo(MultivaluedMap<String, String> mmap,
            Type genericType,
            Annotation[] annotations,
            MediaType mediaType,  MultivaluedMap<String, Object> httpHeaders,
            OutputStream entityStream) throws IOException {
        Form form = Converter.toForm(mmap);
        Representation formRepr = form.getWebRepresentation();
        super.copyAndCloseStream(formRepr.getStream(), entityStream);
    }

    /**
     * 
     * @see org.restlet.ext.jaxrs.internal.provider.AbstractProvider#readFrom(java.lang.Class,
     *      Type, javax.ws.rs.core.MediaType,
     *      Annotation[], javax.ws.rs.core.MultivaluedMap, java.io.InputStream)
     */
    @Override
    public MultivaluedMap<String, String> readFrom(
            Class<MultivaluedMap<String, String>> type, Type genericType,
            MediaType mediaType,
            Annotation[] annotations, MultivaluedMap<String, String> httpResponseHeaders, InputStream entityStream) throws IOException {
        org.restlet.data.MediaType restletMediaType = Converter
                .toRestletMediaType(mediaType);
        Form form = new Form(new InputRepresentation(entityStream,
                restletMediaType));
        return UnmodifiableMultivaluedMap.getFromForm(form, false);
    }
}