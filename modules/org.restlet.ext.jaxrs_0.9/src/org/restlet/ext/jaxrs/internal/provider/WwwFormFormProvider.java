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
import org.restlet.ext.jaxrs.internal.util.Converter;
import org.restlet.ext.jaxrs.internal.util.Util;
import org.restlet.resource.InputRepresentation;
import org.restlet.resource.Representation;

/**
 * This {@link Provider} converts Restlet {@link Form}s to
 * application/x-www-form-urlencoded and vice versa.<br>
 * For encoding or not the same conventions are valid than for
 * {@link WwwFormMmapProvider}.
 * 
 * @author Stephan Koops
 * @see WwwFormMmapProvider
 */
@Provider
@ConsumeMime(MediaType.APPLICATION_FORM_URLENCODED)
@ProduceMime(MediaType.APPLICATION_FORM_URLENCODED)
public class WwwFormFormProvider extends AbstractProvider<Form> {

    /**
     * @see AbstractProvider#getSize(java.lang.Object)
     */
    @Override
    public long getSize(Form form) {
        return -1;
    }

    /**
     * @see AbstractProvider#supportedClass()
     */
    @Override
    protected Class<?> supportedClass() {
        return Form.class;
    }

    /**
     * @see AbstractProvider#writeTo(Object, Class, Type, Annotation[],
     *      MediaType, MultivaluedMap, OutputStream)
     */
    @Override
    public void writeTo(Form form, Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, Object> httpHeaders,
            OutputStream entityStream) throws IOException {
        Representation formRepr = form.getWebRepresentation();
        Util.copyStream(formRepr.getStream(), entityStream);
    }

    /**
     * @see AbstractProvider#readFrom(Class, Type, Annotation[], MediaType,
     *      MultivaluedMap, InputStream)
     */
    @Override
    public Form readFrom(Class<Form> type, Type genericType,
            Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, String> httpResponseHeaders,
            InputStream entityStream) throws IOException {
        org.restlet.data.MediaType restletMediaType = Converter
                .toRestletMediaType(mediaType);
        return new Form(new InputRepresentation(entityStream, restletMediaType));
    }
}