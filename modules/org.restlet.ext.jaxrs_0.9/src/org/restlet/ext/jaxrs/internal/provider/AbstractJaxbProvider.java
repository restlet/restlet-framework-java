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
import java.util.logging.Logger;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 * @author Stephan Koops
 */
abstract class AbstractJaxbProvider<T> extends AbstractProvider<T> {

    @Context ContextResolver<JAXBContext> contextResolver;
    
    /**
     * @see MessageBodyWriter#getSize(Object)
     */
    @Override
    public final long getSize(T object) {
        return -1;
    }

    Object unmarshal(Class<?> type, InputStream entityStream)
            throws IOException {
        try {
            JAXBContext jaxbContext = getJaxbContext(type);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            return unmarshaller.unmarshal(entityStream);
        } catch (JAXBException e) {
            String message = "Could not unmarshal to " + type.getName();
            throw logAndIOExc(getLogger(), message, e);
        }
    }

    abstract Logger getLogger();

    void marshal(Object object, OutputStream entityStream) throws IOException {
        Class<? extends Object> type = object.getClass();
        try {
            JAXBContext jaxbContext = getJaxbContext(object.getClass());
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.marshal(object, entityStream);
        } catch (JAXBException e) {
            throw logAndIOExc(getLogger(), "Could not marshal the "
                    + type.getName(), e);
        }
    }

    private JAXBContext getJaxbContext(Class<?> type) throws JAXBException {
        // LATER perhaps caching the JAXBContext
        JAXBContext jaxbContext = contextResolver.getContext(type);
        if(jaxbContext != null)
            return jaxbContext;
        try {
            return JAXBContext.newInstance(type);
        } catch (NoClassDefFoundError e) {
            throw new WebApplicationException(Response.serverError().entity(
                    e.getMessage()).build());
        }
    }
}