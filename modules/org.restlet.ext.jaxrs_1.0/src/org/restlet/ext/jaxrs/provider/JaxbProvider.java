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
import java.util.logging.Logger;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Stephan Koops
 * 
 */
@Provider
public class JaxbProvider extends AbstractProvider<Object> {

    // TODO JSR311: JAXB-Version nicht festgelet.

    private Logger logger = Logger.getLogger(JaxbProvider.class.getName());

    /**
     * @see org.restlet.ext.jaxrs.provider.AbstractProvider#getSize(java.lang.Object)
     */
    @Override
    public long getSize(Object object) {
        return -1;
    }

    /**
     * @see org.restlet.ext.jaxrs.provider.AbstractProvider#isReadableAndWriteable(java.lang.Class)
     */
    @Override
    protected boolean isReadableAndWriteable(Class<?> type) {
        return type.isAnnotationPresent(XmlRootElement.class);
    }

    /**
     * @see org.restlet.ext.jaxrs.provider.AbstractProvider#readFrom(java.lang.Class,
     *      javax.ws.rs.core.MediaType, javax.ws.rs.core.MultivaluedMap,
     *      java.io.InputStream)
     */
    @Override
    public Object readFrom(Class<Object> type, MediaType mediaType,
            MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
            throws IOException {
        try {
            JAXBContext jaxbContext = getJaxbContext(type);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            return unmarshaller.unmarshal(entityStream);
        } catch (JAXBException e) {
            String message = "Could not unmarshal to " + type.getName();
            throw logAndIOExc(logger, message, e);
        }
    }

    /**
     * @see org.restlet.ext.jaxrs.provider.AbstractProvider#writeTo(java.lang.Object,
     *      javax.ws.rs.core.MediaType, javax.ws.rs.core.MultivaluedMap,
     *      java.io.OutputStream)
     */
    @Override
    public void writeTo(Object object, MediaType mediaType,
            MultivaluedMap<String, Object> httpResponseHeaders,
            OutputStream entityStream) throws IOException {
        try {
            JAXBContext jaxbContext = getJaxbContext(object.getClass());
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.marshal(object, entityStream);
        } catch (JAXBException e) {
            throw logAndIOExc(logger, "Could not marshal the "
                    + object.getClass().getName(), e);
        }
    }

    private JAXBContext getJaxbContext(Class<?> clazz) throws JAXBException {
        // LATER perhaps caching the JAXBContext
        return JAXBContext.newInstance(clazz);
    }
}