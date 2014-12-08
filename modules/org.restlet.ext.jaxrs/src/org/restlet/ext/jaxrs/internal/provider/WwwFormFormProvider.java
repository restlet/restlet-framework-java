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
import javax.ws.rs.ext.Provider;

import org.restlet.Request;
import org.restlet.data.Form;
import org.restlet.engine.io.IoUtils;
import org.restlet.ext.jaxrs.internal.util.Converter;
import org.restlet.ext.jaxrs.internal.wrappers.provider.ProviderWrapper;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;

/**
 * This {@link ProviderWrapper} converts Restlet {@link Form}s to
 * application/x-www-form-urlencoded and vice versa.<br>
 * For encoding or not the same conventions are valid than for
 * {@link WwwFormMmapProvider}.
 * 
 * @author Stephan Koops
 * @see WwwFormMmapProvider
 */
@Provider
@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
@Produces(MediaType.APPLICATION_FORM_URLENCODED)
public class WwwFormFormProvider extends AbstractProvider<Form> {

    /**
     * @param mediaType
     * @param entityStream
     * @return
     */
    static Form getForm(MediaType mediaType, InputStream entityStream) {
        org.restlet.data.MediaType restletMediaType = Converter
                .toRestletMediaType(mediaType);
        Form form = new Form(new InputRepresentation(entityStream,
                restletMediaType));
        Request.getCurrent().setEntity(form.getWebRepresentation());
        return form;
    }

    /**
     * @see AbstractProvider#getSize(java.lang.Object)
     */
    @Override
    public long getSize(Form form, Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType) {
        return -1;
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
        return getForm(mediaType, entityStream);
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
        IoUtils.copy(formRepr.getStream(), entityStream);
    }
}
