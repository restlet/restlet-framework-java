/*
 * Copyright 2005-2008 Noelios Technologies.
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
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.restlet.ext.jaxrs.internal.exceptions.ImplementationException;
import org.restlet.ext.jaxrs.internal.util.Util;

/**
 * Provider for {@link JAXBElement}s.
 * 
 * @author Stephan Koops
 * @see MessageBodyReader
 * @see MessageBodyWriter
 */
@Provider
@Produces( { "application/xml", MediaType.TEXT_XML, "application/*+xml" })
@Consumes( { "application/xml", MediaType.TEXT_XML, "application/*+xml" })
public class JaxbElementProvider extends AbstractJaxbProvider<JAXBElement<?>> {

    private final Logger logger = Logger.getLogger(JaxbElementProvider.class
            .getName());

    @Override
    Logger getLogger() {
        return this.logger;
    }

    /**
     * @see MessageBodyReader#isReadable(Class, Type, Annotation[])
     */
    @Override
    public boolean isReadable(Class<?> type, Type genericType,
            Annotation[] annotations) {
        if (!JAXBElement.class.isAssignableFrom(type)) {
            return false;
        }
        return Util.getGenericClass(genericType) != null;
    }

    /**
     * @see MessageBodyWriter#isWriteable(Class, Type, Annotation[])
     */
    @Override
    public boolean isWriteable(Class<?> type, Type genericType,
            Annotation[] annotations) {
        if (!type.isAssignableFrom(JAXBElement.class)) {
            return false;
        }
        return Util.getGenericClass(genericType) != null;
    }

    /**
     * @see MessageBodyReader#readFrom(Class, Type, MediaType, Annotation[],
     *      MultivaluedMap, InputStream)
     */
    @Override
    public JAXBElement<?> readFrom(Class<JAXBElement<?>> type,
            Type genericType, Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, String> httpResponseHeaders,
            InputStream entityStream) throws IOException {
        final Class<?> clazz = Util.getGenericClass(genericType);
        if (clazz == null) {
            throw new ImplementationException(
                    "The JaxbElement provider has gotten a type it could not unmarshal. Perhaps is the JaxbElementProvider not consistent to itself.");
        }
        try {
            final JAXBContext jaxbContext = getJaxbContext(clazz);
            final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            JAXBElement<?> je = unmarshaller.unmarshal(new StreamSource(entityStream), type);
            return je;
        } catch (final JAXBException e) {
            final String message = "Could not unmarshal to " + type.getName();
            throw logAndIOExc(getLogger(), message, e);
        }
    }

    /**
     * @see MessageBodyWriter#writeTo(Object, Class, Type, Annotation[],
     *      MediaType, MultivaluedMap, OutputStream)
     */
    @Override
    public void writeTo(JAXBElement<?> jaxbElement, Class<?> type,
            Type genericType, Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, Object> httpResponseHeaders,
            OutputStream entityStream) throws IOException {
        marshal(jaxbElement.getValue(), entityStream);
    }
}