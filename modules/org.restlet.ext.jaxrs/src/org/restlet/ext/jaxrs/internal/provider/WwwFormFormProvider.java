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

package org.restlet.ext.jaxrs.internal.provider;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

import org.restlet.Context;
import org.restlet.Message;
import org.restlet.Request;
import org.restlet.data.Form;
import org.restlet.engine.io.BioUtils;
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

    private static Logger logger = Context.getCurrentLogger();

    /**
     * @see AbstractProvider#getSize(java.lang.Object)
     */
    @Override
    public long getSize(Form form, Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType) {
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
        BioUtils.copy(formRepr.getStream(), entityStream);
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
     * @param mediaType
     * @param entityStream
     * @return
     */
    static Form getForm(MediaType mediaType, InputStream entityStream) {
        org.restlet.data.MediaType restletMediaType = Converter
                .toRestletMediaType(mediaType);
        final Form form;
        form = new Form(new InputRepresentation(entityStream, restletMediaType));
        saveToThreadsRequest(form);
        return form;
    }

    /**
     * @param form
     */
    private static void saveToThreadsRequest(Form form) {
        try {
            Field formField = Message.class.getDeclaredField("entityForm");
            formField.setAccessible(true);
            formField.set(Request.getCurrent(), form);
        } catch (SecurityException e) {
            logger.log(Level.WARNING,
                    "Could not put the Form into the Restlet request", e);
        } catch (IllegalArgumentException e) {
            logger.log(Level.WARNING,
                    "Could not put the Form into the Restlet request", e);
        } catch (NoSuchFieldException e) {
            logger.log(Level.WARNING,
                    "Could not put the Form into the Restlet request", e);
        } catch (IllegalAccessException e) {
            logger.log(Level.WARNING,
                    "Could not put the Form into the Restlet request", e);
        }
    }
}