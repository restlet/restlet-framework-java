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

import static org.restlet.ext.jaxrs.internal.util.Converter.toRestletMediaType;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;
import java.util.logging.Level;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
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
        Representation representation = null;

        try {
            // Convert the object into a representation
            Variant targetVariant = new Variant(toRestletMediaType(mediaType));
            representation = getConverterService().toRepresentation(object, targetVariant, null);
        } catch (IOException e) {
            Context.getCurrentLogger().log(Level.FINE, "Unable to get the size", e);
        }

        return (representation == null) ? -1 : representation.getSize();
    }

    @Override
    public boolean isReadable(Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType) {

        Variant sourceVariant = new Variant(toRestletMediaType(mediaType));
        List<Class<?>> classes = getConverterService().getObjectClasses(sourceVariant);

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
        List<? extends Variant> variants;

        try {
            Variant targetVariant = new Variant(toRestletMediaType(mediaType));
            variants = getConverterService().getVariants(type, targetVariant);
        } catch (IOException e) {
            throw new ResourceException(e);
        }

        return (variants != null) && !variants.isEmpty();
    }

    @Override
    public Object readFrom(Class<Object> type, Type genericType,
            Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
            throws IOException {

        Representation sourceRepresentation = new InputRepresentation(entityStream, toRestletMediaType(mediaType));
        return getConverterService().toObject(sourceRepresentation, type, null);
    }

    @Override
    public void writeTo(Object object, Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, Object> httpHeaders,
            OutputStream entityStream) throws IOException {

        // Convert the object into a representation
        Variant targetVariant = new Variant(toRestletMediaType(mediaType));
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
