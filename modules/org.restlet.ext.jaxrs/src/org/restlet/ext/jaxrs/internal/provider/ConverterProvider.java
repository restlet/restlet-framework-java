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
import java.lang.reflect.Type;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

import org.restlet.Application;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.service.ConverterService;

/**
 * This Provider is based on the pluggable Restlet's {@link ConverterService}.
 * 
 * @author Jerome Louvel
 */
@Provider
public class ConverterProvider extends AbstractProvider<Object> {

    /** The Restlet converter service. */
    private ConverterService converterService;

    /**
     * Constructor.
     */
    public ConverterProvider() {
        Application application = Application.getCurrent();

        if (application != null) {
            this.converterService = application.getConverterService();
        }

        if (this.converterService == null) {
            this.converterService = new ConverterService();
        }
    }

    /**
     * Return the Restlet converter service.
     * 
     * @return The Restlet converter service.
     */
    private ConverterService getConverterService() {
        return converterService;
    }

    @Override
    public long getSize(Object object, Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType) {

        // Convert the object into a representation
        Variant targetVariant = new Variant(new org.restlet.data.MediaType(
                mediaType.toString()));
        Representation representation = getConverterService().toRepresentation(
                object, targetVariant, null);
        return (representation == null) ? -1 : representation.getSize();
    }

    @Override
    public boolean isReadable(Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType) {

        Variant sourceVariant = new Variant(new org.restlet.data.MediaType(
                mediaType.toString()));
        List<Class<?>> classes = getConverterService().getObjectClasses(
                sourceVariant);

        for (Class<?> clazz : classes) {
            if (clazz.isAssignableFrom(type)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType) {
        Variant targetVariant = new Variant(new org.restlet.data.MediaType(
                mediaType.toString()));
        List<? extends Variant> variants = getConverterService().getVariants(
                type, targetVariant);
        return (variants != null) && !variants.isEmpty();
    }

    @Override
    public Object readFrom(Class<Object> type, Type genericType,
            Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
            throws IOException {

        Representation sourceRepresentation = new InputRepresentation(
                entityStream, new org.restlet.data.MediaType(
                        mediaType.toString()));
        return getConverterService().toObject(sourceRepresentation, type, null);
    }

    @Override
    public void writeTo(Object object, Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, Object> httpHeaders,
            OutputStream entityStream) throws IOException {

        // Convert the object into a representation
        Variant targetVariant = new Variant(new org.restlet.data.MediaType(
                mediaType.toString()));
        Representation representation = getConverterService().toRepresentation(
                object, targetVariant, null);

        // Copy entity headers (NOT SUPPORTED)
        // Series<Parameter> entityHeaders = new Form();
        // HttpServerAdapter.addEntityHeaders(representation, entityHeaders);
        //
        // for (Parameter header : entityHeaders) {
        // httpHeaders.add(header.getName(), header.getValue());
        // }

        // Write the representation
        if (representation != null) {
            representation.write(entityStream);
        }
    }
}