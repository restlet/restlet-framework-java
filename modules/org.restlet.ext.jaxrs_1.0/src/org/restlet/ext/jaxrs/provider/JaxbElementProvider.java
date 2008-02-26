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
package org.restlet.ext.jaxrs.provider;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.logging.Logger;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.restlet.ext.jaxrs.todo.NotYetImplementedException;

/**
 * @author Stephan Koops
 */
@Provider
public class JaxbElementProvider extends AbstractProvider<JAXBElement<?>> {

    private Logger logger = Logger.getLogger(JaxbElementProvider.class
            .getName());

    /**
     * @see org.restlet.ext.jaxrs.provider.AbstractProvider#getSize(java.lang.Object)
     */
    @Override
    public long getSize(JAXBElement<?> object) {
        return -1;
    }

    /**
     * @see org.restlet.ext.jaxrs.provider.AbstractProvider#isReadableAndWriteable(java.lang.Class,
     *      Type, Annotation[])
     */
    @Override
    public boolean isReadableAndWriteable(Class<?> type, Type genericType,
            Annotation[] annotations) {
        return JAXBElement.class.isAssignableFrom(type);
    }

    /**
     * Method to show how to get the parameter class of the JAXBElement (for
     * reading of the concrete type), but it requires to get the generic
     * parameter types from the method. The interface MessageBodyReader only
     * supports the Class (JAXBElement) which has no informtaion about it's
     * concrete parameter.
     * 
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        Method m = JaxbElementProvider.class.getMethod("x", JAXBElement.class);
        Type t = m.getGenericParameterTypes()[0];
        if (t instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) t;
            Type atp = pt.getActualTypeArguments()[0];
            System.out.println(atp);
        }
    }

    /**
     * only for test in {@link #main(String[])}
     * 
     * @param string
     */
    void x(JAXBElement<String> string) {
        string.toString();
    }

    /**
     * @see org.restlet.ext.jaxrs.provider.AbstractProvider#writeTo(Object,
     *      Type, Annotation[], MediaType, MultivaluedMap, OutputStream)
     */
    @Override
    public void writeTo(JAXBElement<?> jaxbElement, Type genericType,
            Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, Object> httpResponseHeaders,
            OutputStream entityStream) throws IOException {
        try {
            JAXBContext jaxbContext = getJaxbContext(jaxbElement
                    .getDeclaredType());
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.marshal(jaxbElement.getValue(), entityStream);
        } catch (JAXBException e) {
            throw AbstractProvider.logAndIOExc(logger, "Could not marshal the "
                    + jaxbElement.getDeclaredType().getName(), e);
        }
    }

    private JAXBContext getJaxbContext(Class<?> clazz) throws JAXBException {
        // LATER perhaps caching the JAXBContext
        try {
            return JAXBContext.newInstance(clazz);
        } catch (LinkageError e) {
            throw new WebApplicationException(Response.serverError().entity(
                    e.getMessage()).build());
        }
    }

    @Override
    public JAXBElement<?> readFrom(Class<JAXBElement<?>> type,
            Type genericType, MediaType mediaType, Annotation[] annotations,
            MultivaluedMap<String, String> httpResponseHeaders,
            InputStream entityStream) throws IOException {
        // TODO JaxBElementProvider.readFrom
        throw new NotYetImplementedException();
    }
}