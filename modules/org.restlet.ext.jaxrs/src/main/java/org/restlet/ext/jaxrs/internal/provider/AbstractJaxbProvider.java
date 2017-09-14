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
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.logging.Logger;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Providers;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

/**
 * @author Stephan Koops
 */
abstract class AbstractJaxbProvider<T> extends AbstractProvider<T> {

    private ContextResolver<JAXBContext> contextResolver;

    protected JAXBContext getJaxbContext(Class<?> type) throws JAXBException {
        // NICE perhaps caching the JAXBContext
        if (this.contextResolver != null) {
            JAXBContext jaxbContext = this.contextResolver.getContext(type);
            if (jaxbContext != null) {
                return jaxbContext;
            }
        }
        try {
            return JAXBContext.newInstance(type);
        } catch (NoClassDefFoundError e) {
            throw new WebApplicationException(Response.serverError()
                    .entity(e.getMessage()).build());
        }
    }

    abstract Logger getLogger();

    /**
     * @see MessageBodyWriter#getSize(Object)
     */
    @Override
    public final long getSize(T object, Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    void marshal(Object object, OutputStream entityStream) throws IOException {
        final Class<? extends Object> type = object.getClass();
        try {
            final JAXBContext jaxbContext = getJaxbContext(object.getClass());
            final Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.marshal(object, entityStream);
        } catch (JAXBException e) {
            throw logAndIOExc(getLogger(),
                    "Could not marshal the " + type.getName(), e);
        }
    }

    public void setContextResolver(ContextResolver<JAXBContext> contextResolver) {
        this.contextResolver = contextResolver;
    }
    
    @Context
    void setContextResolver(Providers providers) {
        this.contextResolver = providers.getContextResolver(JAXBContext.class,
                MediaType.APPLICATION_XML_TYPE);
    }
}
