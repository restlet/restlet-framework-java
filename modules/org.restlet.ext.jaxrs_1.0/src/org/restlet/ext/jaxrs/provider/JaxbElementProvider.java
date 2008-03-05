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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.logging.Logger;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.restlet.ext.jaxrs.exceptions.ImplementationException;

/**
 * @author Stephan Koops
 */
@Provider
public class JaxbElementProvider extends AbstractJaxbProvider<JAXBElement<?>> {

    private Logger logger = Logger.getLogger(JaxbElementProvider.class
            .getName());

    /**
     * @see org.restlet.ext.jaxrs.provider.AbstractProvider#isReadableAndWriteable(java.lang.Class,
     *      Type, Annotation[])
     */
    @Override
    public boolean isReadableAndWriteable(Class<?> type, Type genericType,
            Annotation[] annotations) {
        if (!JAXBElement.class.isAssignableFrom(type))
            return false;
        if (!(genericType instanceof ParameterizedType))
            return false;
        ParameterizedType pt = (ParameterizedType) genericType;
        Type atp = pt.getActualTypeArguments()[0];
        if (atp instanceof Class)
            return true;
        if (atp instanceof ParameterizedType)
            return (((ParameterizedType) atp).getRawType() instanceof Class);
        return false;
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
        marshal(jaxbElement.getValue(), entityStream);
    }

    @Override
    @SuppressWarnings("unchecked")
    public JAXBElement<?> readFrom(Class<JAXBElement<?>> type,
            Type genericType, MediaType mediaType, Annotation[] annotations,
            MultivaluedMap<String, String> httpResponseHeaders,
            InputStream entityStream) throws IOException {
        ParameterizedType pt = (ParameterizedType) genericType;
        Type atp = pt.getActualTypeArguments()[0];
        Class<?> clazz;
        if (atp instanceof Class)
            clazz = (Class<?>) atp;
        else if (atp instanceof ParameterizedType)
            clazz = (Class<?>) ((ParameterizedType) atp).getRawType();
        else
            throw new ImplementationException(
                    "The JaxbElement provider has gotten a type it could not unmarshal. Perhaps is the JaxbElementProvider not consistent to itself.");
        QName qName = new QName("testQName"); // LATER QName for JAXBElement?
        Class<?> declaredType = clazz;
        Object value = unmarshal(clazz, entityStream);
        return new JAXBElement(qName, declaredType, value);
    }

    @Override
    Logger getLogger() {
        return this.logger;
    }
}